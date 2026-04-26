package fr.vferries.cuisine.data.timers

import android.app.AlarmManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class TimerSchedulerTest {

    private lateinit var scheduler: TimerScheduler
    private lateinit var alarms: AlarmManager

    @Before
    fun setup() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        scheduler = TimerScheduler(ctx)
        alarms = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    @Test fun schedule_programs_an_exact_alarm_at_endTime() {
        val timer = RunningTimer(
            id = "porc:0:1:0",
            name = "Cuisson",
            durationSeconds = 180,
            startedAtMillis = 1_000_000L,
        )
        scheduler.schedule(timer)

        val all = shadowOf(alarms).scheduledAlarms
        assertEquals(1, all.size)
        assertEquals(AlarmManager.RTC_WAKEUP, all.first().type)
        assertEquals(1_000_000L + 180_000L, all.first().triggerAtMs)
    }

    @Test fun cancel_removes_the_alarm_for_a_given_id() {
        val timer = RunningTimer(
            id = "x",
            name = "C",
            durationSeconds = 60,
            startedAtMillis = 0L,
        )
        scheduler.schedule(timer)
        scheduler.cancel("x")

        assertEquals(0, shadowOf(alarms).scheduledAlarms.size)
    }

    @Test fun cancel_keeps_other_alarms_intact() {
        val a = RunningTimer(id = "a", name = "A", durationSeconds = 60, startedAtMillis = 0L)
        val b = RunningTimer(id = "b", name = "B", durationSeconds = 120, startedAtMillis = 0L)
        scheduler.schedule(a)
        scheduler.schedule(b)
        scheduler.cancel("a")

        val remaining = shadowOf(alarms).scheduledAlarms
        assertEquals(1, remaining.size)
        assertEquals(120_000L, remaining.first().triggerAtMs)
    }
}
