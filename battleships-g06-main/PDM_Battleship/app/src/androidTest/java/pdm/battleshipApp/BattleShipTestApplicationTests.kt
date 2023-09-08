package pdm.battleship

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import pdm.battleshipApp.BattleShipApplication
import pdm.battleshipApp.DependenciesContainer

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class BattleShipTestApplicationTests {
    @Test
    fun instrumented_tests_use_application_test_context() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("pdm.battleship", appContext.packageName)
        assertTrue(
            "Make sure the tests runner is correctly configured in build.gradle\n" +
                    "defaultConfig { testInstrumentationRunner <test runner class name> }",
            appContext.applicationContext is BattleShipApplication
        )
    }
    @Test
    fun application_context_contains_dependencies() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertTrue(context.applicationContext is DependenciesContainer)
    }
}