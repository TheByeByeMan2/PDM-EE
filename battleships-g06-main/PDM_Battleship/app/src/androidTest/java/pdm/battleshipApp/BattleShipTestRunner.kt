package pdm.battleshipApp

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import pdm.battleshipApp.service.BattleShipService
import pdm.battleshipApp.service.FakeBattleShipService

class BattleShipTestApplication(override val service: FakeBattleShipService) : DependenciesContainer, Application(){

}

@Suppress("unused")
class BattleShipTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader, className: String, context: Context): Application {
        return super.newApplication(cl, BattleShipTestApplication::class.java.name, context)
    }
}