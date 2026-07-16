# Notification timer → deep-link étape — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Taper la notification d'expiration d'un timer ouvre l'écran cuisson de la bonne recette, sur l'étape où le timer a été lancé, avec le back stack Home → Recette → Cuisson.

**Architecture:** L'id de timer encode déjà la cible (`slug:sectionIdx:stepIdx:tokIdx`). `TimerNotifier` le parse et attache un `contentIntent` (PendingIntent vers `MainActivity` avec extras). `MainActivity` (singleTop) transforme l'intent en état `TimerDeepLink?` ; `CuisineNavHost` le consomme dans un `LaunchedEffect` qui construit la pile de navigation. La route `cuisson` gagne des arguments optionnels `section`/`step` résolus en index flat par une fonction pure.

**Tech Stack:** Kotlin, Jetpack Compose + Navigation Compose, NotificationCompat/PendingIntent, JUnit4 + Robolectric (`@Config(sdk = [33])`).

**Spec:** `docs/superpowers/specs/2026-07-16-notification-timer-deeplink-design.md`

## Global Constraints

- Tout le code Android vit sous `android/app/src/main/java/fr/vferries/cuisine/`, tests JVM sous `android/app/src/test/java/fr/vferries/cuisine/` (miroir).
- Suite de tests : `cd android && ./gradlew test` (JVM + Robolectric, pas d'émulateur).
- TDD double-loop : chaque test doit échouer AVANT l'implémentation, pour la bonne raison (symbole manquant ou assertion, pas une erreur d'environnement).
- Logs : tag `"Cuisine.Timers"` pour tout ce qui touche aux timers ; les branches inattendues sont loggées (jamais avalées en silence).
- Commits : message court (sujet + 2-4 lignes de corps max), PAS de trailer `Co-Authored-By`, pas de push.
- Aucune nouvelle dépendance.
- Ne jamais utiliser `System.currentTimeMillis()`/`Calendar` dans une fonction testée — injecter (aucune fonction de ce plan n'en a besoin).

---

### Task 1: `parseTimerId` + contentIntent sur la notification

**Files:**
- Create: `android/app/src/main/java/fr/vferries/cuisine/data/timers/TimerTarget.kt`
- Modify: `android/app/src/main/java/fr/vferries/cuisine/data/timers/TimerNotifier.kt`
- Test: `android/app/src/test/java/fr/vferries/cuisine/data/timers/TimerTargetTest.kt`
- Test: `android/app/src/test/java/fr/vferries/cuisine/data/timers/TimerNotifierTest.kt`

**Interfaces:**
- Consumes: format d'id existant `"$slug:${sectionIdx}:${stepIdx}:$tokIdx"` (posé par `CuissonScreen.kt`, ex. `"porc:0:1:2"`).
- Produces: `data class TimerTarget(slug: String, sectionIdx: Int, stepIdx: Int, tokIdx: Int)` ; `fun parseTimerId(id: String): TimerTarget?` ; constantes `TimerNotifier.EXTRA_DEEPLINK_SLUG = "deeplink_slug"`, `TimerNotifier.EXTRA_DEEPLINK_SECTION = "deeplink_section"`, `TimerNotifier.EXTRA_DEEPLINK_STEP = "deeplink_step"` (utilisées par la Task 3).

- [ ] **Step 1: Écrire les tests de `parseTimerId` (échec attendu)**

Créer `android/app/src/test/java/fr/vferries/cuisine/data/timers/TimerTargetTest.kt` :

```kotlin
package fr.vferries.cuisine.data.timers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TimerTargetTest {

    @Test fun parses_a_wellformed_id() {
        assertEquals(
            TimerTarget("sukiyaki-udon", 1, 2, 3),
            parseTimerId("sukiyaki-udon:1:2:3"),
        )
    }

    @Test fun rejects_wrong_segment_count() {
        assertNull(parseTimerId(""))
        assertNull(parseTimerId("slug:1:2"))
        assertNull(parseTimerId("slug:1:2:3:4"))
    }

    @Test fun rejects_non_numeric_indices() {
        assertNull(parseTimerId("slug:a:2:3"))
        assertNull(parseTimerId("slug:1:b:3"))
        assertNull(parseTimerId("slug:1:2:c"))
    }

    @Test fun rejects_blank_slug() {
        assertNull(parseTimerId(":1:2:3"))
    }
}
```

- [ ] **Step 2: Vérifier l'échec pour la bonne raison**

Run: `cd android && ./gradlew :app:testDebugUnitTest --tests "fr.vferries.cuisine.data.timers.TimerTargetTest"`
Expected: FAIL à la compilation — `Unresolved reference: TimerTarget` / `parseTimerId`.

- [ ] **Step 3: Implémenter `TimerTarget.kt`**

Créer `android/app/src/main/java/fr/vferries/cuisine/data/timers/TimerTarget.kt` :

```kotlin
package fr.vferries.cuisine.data.timers

/** Cible d'un timer, encodée dans son id : "slug:sectionIdx:stepIdx:tokIdx". */
data class TimerTarget(
    val slug: String,
    val sectionIdx: Int,
    val stepIdx: Int,
    val tokIdx: Int,
)

fun parseTimerId(id: String): TimerTarget? {
    val parts = id.split(":")
    if (parts.size != 4 || parts[0].isBlank()) return null
    val sectionIdx = parts[1].toIntOrNull() ?: return null
    val stepIdx = parts[2].toIntOrNull() ?: return null
    val tokIdx = parts[3].toIntOrNull() ?: return null
    return TimerTarget(parts[0], sectionIdx, stepIdx, tokIdx)
}
```

- [ ] **Step 4: Vérifier que `TimerTargetTest` passe**

Run: `cd android && ./gradlew :app:testDebugUnitTest --tests "fr.vferries.cuisine.data.timers.TimerTargetTest"`
Expected: PASS (4 tests).

- [ ] **Step 5: Écrire les tests du contentIntent (échec attendu)**

Créer `android/app/src/test/java/fr/vferries/cuisine/data/timers/TimerNotifierTest.kt` :

```kotlin
package fr.vferries.cuisine.data.timers

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import fr.vferries.cuisine.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class TimerNotifierTest {

    private fun postedNotification(id: String): android.app.Notification {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        TimerNotifier(ctx).notifyExpired(id, "Cuisson")
        return shadowOf(nm).activeNotifications.first().notification
    }

    @Test fun expired_notification_carries_a_deeplink_to_the_step() {
        val n = postedNotification("porc:0:1:2")

        assertNotNull("La notif doit porter un contentIntent", n.contentIntent)
        val saved = shadowOf(n.contentIntent).savedIntent
        assertEquals(MainActivity::class.java.name, saved.component?.className)
        assertEquals("porc", saved.getStringExtra(TimerNotifier.EXTRA_DEEPLINK_SLUG))
        assertEquals(0, saved.getIntExtra(TimerNotifier.EXTRA_DEEPLINK_SECTION, -1))
        assertEquals(1, saved.getIntExtra(TimerNotifier.EXTRA_DEEPLINK_STEP, -1))
    }

    @Test fun malformed_id_yields_a_notification_without_deeplink() {
        val n = postedNotification("id-sans-indices")

        assertNull("Id non parsable → pas de contentIntent", n.contentIntent)
    }
}
```

- [ ] **Step 6: Vérifier l'échec pour la bonne raison**

Run: `cd android && ./gradlew :app:testDebugUnitTest --tests "fr.vferries.cuisine.data.timers.TimerNotifierTest"`
Expected: FAIL à la compilation — `Unresolved reference: EXTRA_DEEPLINK_SLUG` (les constantes n'existent pas encore).

- [ ] **Step 7: Modifier `TimerNotifier.kt`**

Dans `android/app/src/main/java/fr/vferries/cuisine/data/timers/TimerNotifier.kt` :

Ajouter les imports :

```kotlin
import android.app.PendingIntent
import android.content.Intent
import fr.vferries.cuisine.MainActivity
```

Remplacer `notifyExpired` :

```kotlin
    fun notifyExpired(id: String, name: String) {
        val title = if (name.isNotBlank()) "$name terminé" else "Timer terminé"
        val builder = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer_notification)
            .setContentTitle(title)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        val target = parseTimerId(id)
        if (target != null) {
            builder.setContentIntent(deepLinkIntent(id, target))
        } else {
            Log.w(TAG, "notifyExpired: id non parsable '$id' — notif sans deep-link")
        }
        nm.notify(id.hashCode(), builder.build())
        Log.d(TAG, "notifyExpired posted id=$id name='$name' deepLink=${target != null}")
    }

    /** Tap sur la notif → MainActivity avec la cible ; un PendingIntent par timer. */
    private fun deepLinkIntent(id: String, target: TimerTarget): PendingIntent {
        val intent = Intent(appContext, MainActivity::class.java).apply {
            putExtra(EXTRA_DEEPLINK_SLUG, target.slug)
            putExtra(EXTRA_DEEPLINK_SECTION, target.sectionIdx)
            putExtra(EXTRA_DEEPLINK_STEP, target.stepIdx)
        }
        return PendingIntent.getActivity(
            appContext,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
```

Compléter le companion :

```kotlin
    companion object {
        const val CHANNEL_ID = "timers"
        const val EXTRA_DEEPLINK_SLUG = "deeplink_slug"
        const val EXTRA_DEEPLINK_SECTION = "deeplink_section"
        const val EXTRA_DEEPLINK_STEP = "deeplink_step"
        private const val TAG = "Cuisine.Timers"
    }
```

- [ ] **Step 8: Vérifier que toute la suite passe**

Run: `cd android && ./gradlew test`
Expected: PASS (dont `TimerNotifierTest` 2 tests, `TimerTargetTest` 4 tests, et `TimerExpirationReceiverTest` inchangé toujours vert).

- [ ] **Step 9: Commit**

```bash
git add android/app/src/main/java/fr/vferries/cuisine/data/timers/TimerTarget.kt \
        android/app/src/main/java/fr/vferries/cuisine/data/timers/TimerNotifier.kt \
        android/app/src/test/java/fr/vferries/cuisine/data/timers/
git commit -m "feat(android): deep-link sur la notif de timer

L'id encode déjà slug:section:step:tok ; parseTimerId le décode et
la notif gagne un contentIntent vers MainActivity avec la cible."
```

---

### Task 2: route `section`/`step` + résolution de l'étape dans CuissonScreen

**Files:**
- Modify: `android/app/src/main/java/fr/vferries/cuisine/data/Cuisson.kt`
- Modify: `android/app/src/main/java/fr/vferries/cuisine/ui/CuissonScreen.kt`
- Modify: `android/app/src/main/java/fr/vferries/cuisine/ui/CuisineNavHost.kt` (route + arguments seulement)
- Test: `android/app/src/test/java/fr/vferries/cuisine/data/CuissonTest.kt`

**Interfaces:**
- Consumes: `FlatStep(sectionName, sectionIdx, stepIdx, tokens)` et `flattenSteps` existants (`data/Cuisson.kt`).
- Produces: `fun flatIndexOf(steps: List<FlatStep>, sectionIdx: Int, stepIdx: Int): Int` (−1 si absent) ; `CuissonScreen(state, onExit, targetSection: Int = -1, targetStep: Int = -1)` ; route `"cuisson/{slug}?section={section}&step={step}"` (défauts −1) — la Task 3 navigue vers cette route.

- [ ] **Step 1: Écrire les tests de `flatIndexOf` (échec attendu)**

Ajouter à `android/app/src/test/java/fr/vferries/cuisine/data/CuissonTest.kt` (les fixtures `Section`/`textStep` du fichier existent déjà — réutiliser le style en place) :

```kotlin
    @Test fun flatIndexOf_finds_the_flat_position() {
        val steps = flattenSteps(
            listOf(
                Section("Prep", listOf(textStep("a"), textStep("b"))),
                Section("Cuisson", listOf(textStep("c"))),
            ),
        )
        assertEquals(0, flatIndexOf(steps, 0, 0))
        assertEquals(2, flatIndexOf(steps, 1, 0))
    }

    @Test fun flatIndexOf_returns_minus_one_when_absent() {
        val steps = flattenSteps(
            listOf(Section("Prep", listOf(textStep("a")))),
        )
        assertEquals(-1, flatIndexOf(steps, 0, 5))
        assertEquals(-1, flatIndexOf(steps, 3, 0))
        assertEquals(-1, flatIndexOf(emptyList(), 0, 0))
    }
```

Note : si `textStep` n'existe pas sous ce nom dans le fichier, utiliser le helper privé déjà présent (`private fun textStep(text: String) = Step(listOf(StepToken.Text(text)))`) sans le dupliquer.

- [ ] **Step 2: Vérifier l'échec pour la bonne raison**

Run: `cd android && ./gradlew :app:testDebugUnitTest --tests "fr.vferries.cuisine.data.CuissonTest"`
Expected: FAIL à la compilation — `Unresolved reference: flatIndexOf`.

- [ ] **Step 3: Implémenter `flatIndexOf`**

Ajouter à la fin de `android/app/src/main/java/fr/vferries/cuisine/data/Cuisson.kt` :

```kotlin
/** Index flat d'une étape (sectionIdx, stepIdx), −1 si absente. */
fun flatIndexOf(steps: List<FlatStep>, sectionIdx: Int, stepIdx: Int): Int =
    steps.indexOfFirst { it.sectionIdx == sectionIdx && it.stepIdx == stepIdx }
```

- [ ] **Step 4: Vérifier que `CuissonTest` passe**

Run: `cd android && ./gradlew :app:testDebugUnitTest --tests "fr.vferries.cuisine.data.CuissonTest"`
Expected: PASS.

- [ ] **Step 5: Brancher la cible dans `CuissonScreen`**

Dans `android/app/src/main/java/fr/vferries/cuisine/ui/CuissonScreen.kt` :

Ajouter les imports :

```kotlin
import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import fr.vferries.cuisine.data.flatIndexOf
```

Changer la signature :

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuissonScreen(
    state: RecipeState,
    onExit: () -> Unit,
    targetSection: Int = -1,
    targetStep: Int = -1,
) {
```

Juste après la déclaration de `val clamped = ...`, ajouter (one-shot : ne saute à la cible qu'une fois les étapes chargées, puis ne rejoue plus — la navigation manuelle reprend la main) :

```kotlin
    var targetApplied by rememberSaveable(
        (state as? RecipeState.Success)?.recipe?.slug ?: "loading",
    ) { mutableStateOf(false) }
    LaunchedEffect(steps.isNotEmpty()) {
        if (targetApplied || targetSection < 0 || steps.isEmpty()) return@LaunchedEffect
        val i = flatIndexOf(steps, targetSection, targetStep)
        if (i >= 0) {
            index = i
        } else {
            Log.w("Cuisine.Timers", "étape cible ($targetSection,$targetStep) introuvable — étape 0")
        }
        targetApplied = true
    }
```

- [ ] **Step 6: Étendre la route dans `CuisineNavHost`**

Dans `android/app/src/main/java/fr/vferries/cuisine/ui/CuisineNavHost.kt`, remplacer le bloc `composable` de la route cuisson :

```kotlin
        composable(
            route = "cuisson/{slug}?section={section}&step={step}",
            arguments = listOf(
                navArgument("slug") { type = NavType.StringType },
                navArgument("section") { type = NavType.IntType; defaultValue = -1 },
                navArgument("step") { type = NavType.IntType; defaultValue = -1 },
            ),
        ) { entry ->
            val slug = entry.arguments?.getString("slug").orEmpty()
            val vm: RecipeViewModel = viewModel(
                key = "cuisson-$slug",
                factory = factoryOf { RecipeViewModel(repository, slug) },
            )
            val state by vm.state.collectAsState()
            CuissonScreen(
                state = state,
                onExit = { nav.popBackStack() },
                targetSection = entry.arguments?.getInt("section") ?: -1,
                targetStep = entry.arguments?.getInt("step") ?: -1,
            )
        }
```

La navigation interne existante `nav.navigate("cuisson/$slug")` continue de matcher (arguments optionnels avec défauts).

- [ ] **Step 7: Vérifier que tout compile et passe**

Run: `cd android && ./gradlew test`
Expected: PASS — aucune régression (notamment `CuissonScreenTest` inchangé).

- [ ] **Step 8: Commit**

```bash
git add android/app/src/main/java/fr/vferries/cuisine/data/Cuisson.kt \
        android/app/src/main/java/fr/vferries/cuisine/ui/CuissonScreen.kt \
        android/app/src/main/java/fr/vferries/cuisine/ui/CuisineNavHost.kt \
        android/app/src/test/java/fr/vferries/cuisine/data/CuissonTest.kt
git commit -m "feat(android): cuisson ciblable par section/step

Route cuisson?section&step (défaut -1 = comportement actuel) ;
flatIndexOf résout l'étape, introuvable → étape 0."
```

---

### Task 3: réception du deep-link (MainActivity singleTop + navigation)

**Files:**
- Create: `android/app/src/main/java/fr/vferries/cuisine/data/timers/TimerDeepLink.kt`
- Modify: `android/app/src/main/java/fr/vferries/cuisine/MainActivity.kt`
- Modify: `android/app/src/main/java/fr/vferries/cuisine/ui/CuisineNavHost.kt`
- Modify: `android/app/src/main/AndroidManifest.xml`
- Test: `android/app/src/test/java/fr/vferries/cuisine/data/timers/TimerDeepLinkTest.kt`

**Interfaces:**
- Consumes: constantes `TimerNotifier.EXTRA_DEEPLINK_SLUG/SECTION/STEP` (Task 1) ; route `"cuisson/{slug}?section=S&step=T"` (Task 2).
- Produces: `data class TimerDeepLink(slug: String, sectionIdx: Int, stepIdx: Int)` ; `fun timerDeepLinkFrom(intent: Intent): TimerDeepLink?` ; `CuisineNavHost(repository, themeMode, onThemeModeChange, deepLink: TimerDeepLink? = null, onDeepLinkConsumed: () -> Unit = {})`.

- [ ] **Step 1: Écrire les tests de `timerDeepLinkFrom` (échec attendu)**

Créer `android/app/src/test/java/fr/vferries/cuisine/data/timers/TimerDeepLinkTest.kt` :

```kotlin
package fr.vferries.cuisine.data.timers

import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class TimerDeepLinkTest {

    @Test fun reads_a_complete_deeplink_intent() {
        val intent = Intent().apply {
            putExtra(TimerNotifier.EXTRA_DEEPLINK_SLUG, "porc")
            putExtra(TimerNotifier.EXTRA_DEEPLINK_SECTION, 0)
            putExtra(TimerNotifier.EXTRA_DEEPLINK_STEP, 1)
        }

        assertEquals(TimerDeepLink("porc", 0, 1), timerDeepLinkFrom(intent))
    }

    @Test fun returns_null_without_slug() {
        assertNull(timerDeepLinkFrom(Intent()))
    }

    @Test fun returns_null_when_indices_are_missing() {
        val intent = Intent().apply {
            putExtra(TimerNotifier.EXTRA_DEEPLINK_SLUG, "porc")
        }

        assertNull(timerDeepLinkFrom(intent))
    }
}
```

- [ ] **Step 2: Vérifier l'échec pour la bonne raison**

Run: `cd android && ./gradlew :app:testDebugUnitTest --tests "fr.vferries.cuisine.data.timers.TimerDeepLinkTest"`
Expected: FAIL à la compilation — `Unresolved reference: TimerDeepLink`.

- [ ] **Step 3: Implémenter `TimerDeepLink.kt`**

Créer `android/app/src/main/java/fr/vferries/cuisine/data/timers/TimerDeepLink.kt` :

```kotlin
package fr.vferries.cuisine.data.timers

import android.content.Intent

/** Cible de navigation portée par le tap sur une notif de timer. */
data class TimerDeepLink(
    val slug: String,
    val sectionIdx: Int,
    val stepIdx: Int,
)

fun timerDeepLinkFrom(intent: Intent): TimerDeepLink? {
    val slug = intent.getStringExtra(TimerNotifier.EXTRA_DEEPLINK_SLUG) ?: return null
    val sectionIdx = intent.getIntExtra(TimerNotifier.EXTRA_DEEPLINK_SECTION, -1)
    val stepIdx = intent.getIntExtra(TimerNotifier.EXTRA_DEEPLINK_STEP, -1)
    if (sectionIdx < 0 || stepIdx < 0) return null
    return TimerDeepLink(slug, sectionIdx, stepIdx)
}
```

- [ ] **Step 4: Vérifier que `TimerDeepLinkTest` passe**

Run: `cd android && ./gradlew :app:testDebugUnitTest --tests "fr.vferries.cuisine.data.timers.TimerDeepLinkTest"`
Expected: PASS (3 tests).

- [ ] **Step 5: `MainActivity` — singleTop + état deep-link**

Dans `android/app/src/main/AndroidManifest.xml`, ajouter sur l'activity :

```xml
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:launchMode="singleTop"
            android:theme="@style/Theme.Cuisine">
```

Dans `android/app/src/main/java/fr/vferries/cuisine/MainActivity.kt` :

Ajouter les imports :

```kotlin
import android.content.Intent
import fr.vferries.cuisine.data.timers.TimerDeepLink
import fr.vferries.cuisine.data.timers.timerDeepLinkFrom
```

Ajouter le champ (au niveau de la classe) :

```kotlin
    // App fermée : onCreate lit l'intent ; app ouverte (singleTop) : onNewIntent.
    private val deepLink = mutableStateOf<TimerDeepLink?>(null)
```

Dans `onCreate`, après `ensureNotificationPermission()` :

```kotlin
        deepLink.value = timerDeepLinkFrom(intent)
```

Ajouter la méthode :

```kotlin
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        deepLink.value = timerDeepLinkFrom(intent)
    }
```

Dans `setContent`, passer l'état au NavHost :

```kotlin
                        CuisineNavHost(
                            repository = repository,
                            themeMode = mode,
                            onThemeModeChange = {
                                mode = it
                                themePrefs.set(it)
                            },
                            deepLink = deepLink.value,
                            onDeepLinkConsumed = { deepLink.value = null },
                        )
```

Note : `mutableStateOf` est déjà importé dans MainActivity ; `by remember` n'est PAS utilisé pour `deepLink` (champ d'activity, hors composition).

- [ ] **Step 6: `CuisineNavHost` — navigation à la consommation**

Dans `android/app/src/main/java/fr/vferries/cuisine/ui/CuisineNavHost.kt` :

Ajouter les imports :

```kotlin
import androidx.compose.runtime.LaunchedEffect
import fr.vferries.cuisine.data.timers.TimerDeepLink
```

Changer la signature :

```kotlin
@Composable
fun CuisineNavHost(
    repository: RecipeRepository,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    deepLink: TimerDeepLink? = null,
    onDeepLinkConsumed: () -> Unit = {},
) {
```

Après `val nav = rememberNavController()`, ajouter :

```kotlin
    LaunchedEffect(deepLink) {
        if (deepLink == null) return@LaunchedEffect
        // Pile Home → Recette → Cuisson, quel que soit l'écran courant.
        nav.navigate("recipe/${deepLink.slug}") {
            popUpTo("home")
            launchSingleTop = true
        }
        nav.navigate(
            "cuisson/${deepLink.slug}?section=${deepLink.sectionIdx}&step=${deepLink.stepIdx}",
        )
        onDeepLinkConsumed()
    }
```

- [ ] **Step 7: Vérifier que toute la suite passe**

Run: `cd android && ./gradlew test`
Expected: PASS, zéro régression.

- [ ] **Step 8: Build debug (sanité manifest + nav)**

Run: `cd android && ./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 9: Commit**

```bash
git add android/app/src/main/java/fr/vferries/cuisine/data/timers/TimerDeepLink.kt \
        android/app/src/main/java/fr/vferries/cuisine/MainActivity.kt \
        android/app/src/main/java/fr/vferries/cuisine/ui/CuisineNavHost.kt \
        android/app/src/main/AndroidManifest.xml \
        android/app/src/test/java/fr/vferries/cuisine/data/timers/TimerDeepLinkTest.kt
git commit -m "feat(android): tap sur la notif → cuisson à l'étape du timer

MainActivity singleTop lit les extras (onCreate/onNewIntent) ;
le NavHost construit la pile Home→Recette→Cuisson puis consomme."
```

---

### Task 4: docs (AGENTS.md)

**Files:**
- Modify: `AGENTS.md`

**Interfaces:**
- Consumes: mécanique livrée par les Tasks 1-3.
- Produces: rien de code — mémoire externe du projet à jour.

- [ ] **Step 1: Mettre à jour `AGENTS.md`**

Dans la liste d'idées en réserve, marquer « notification timer / deep-link » comme livrée. Dans la section décrivant l'app Android (timers), ajouter 2-3 lignes :

- Le tap sur la notif d'expiration ouvre la cuisson à l'étape du timer (pile Home → Recette → Cuisson).
- La cible vient de l'id du timer (`slug:sectionIdx:stepIdx:tokIdx`) parsé par `parseTimerId` ; extras lus par `timerDeepLinkFrom` ; route `cuisson/{slug}?section&step`.
- MainActivity est `singleTop` (onNewIntent quand l'app est déjà ouverte).

Adapter la formulation au style existant du fichier (le lire d'abord).

- [ ] **Step 2: Commit**

```bash
git add AGENTS.md
git commit -m "docs: deep-link notification timer dans AGENTS.md"
```

---

## Vérification finale (hors tasks)

- `cd android && ./gradlew test` vert.
- Revue finale de branche (subagent-driven-development).
- Test manuel sur device par l'owner : lancer un timer court depuis une étape ≥ 2, quitter l'app, taper la notif → cuisson sur la bonne étape ; back → recette → home. (AlarmManager/tap non testables en JVM.)
