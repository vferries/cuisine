package fr.vferries.cuisine.ui

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import fr.vferries.cuisine.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    mode: ThemeMode,
    onModeChange: (ThemeMode) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Réglages") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = "Thème", style = MaterialTheme.typography.titleMedium)
            ThemeMode.entries.forEach { m ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .selectable(selected = m == mode, onClick = { onModeChange(m) })
                        .padding(vertical = 12.dp),
                ) {
                    RadioButton(selected = m == mode, onClick = null)
                    Text(
                        text = when (m) {
                            ThemeMode.SYSTEM -> "Système"
                            ThemeMode.LIGHT -> "Clair"
                            ThemeMode.DARK -> "Sombre"
                        },
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            ExactAlarmsSection()
        }
    }
}

@Composable
private fun ExactAlarmsSection() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    val context = LocalContext.current
    val alarms = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val granted = alarms.canScheduleExactAlarms()
    Text(text = "Minuteurs", style = MaterialTheme.typography.titleMedium)
    Text(
        text = if (granted) {
            "Les minuteurs s'arrêtent à l'heure exacte, même si l'app est fermée."
        } else {
            "Les minuteurs peuvent retarder de quelques minutes en veille. Active la précision dans les réglages système."
        },
        style = MaterialTheme.typography.bodyMedium,
    )
    if (!granted) {
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { context.startActivity(exactAlarmSettingsIntent(context)) }) {
            Text("Activer la précision")
        }
    }
}

private fun exactAlarmSettingsIntent(context: Context): Intent =
    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).setData(
        android.net.Uri.fromParts("package", context.packageName, null),
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
