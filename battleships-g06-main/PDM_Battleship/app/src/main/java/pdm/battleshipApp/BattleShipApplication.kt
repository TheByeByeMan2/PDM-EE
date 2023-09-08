package pdm.battleshipApp

import android.app.Application
import android.content.Context
import okhttp3.Cache
import okhttp3.OkHttpClient
import pdm.battleshipApp.service.AppListener
import pdm.battleshipApp.service.Service
import pdm.battleshipApp.service.BattleShipService
import pdm.battleshipApp.service.FakeBattleShipService

data class UserInfo(
    var userId: Int? = null,
    var token: String? = null
)

interface DependenciesContainer {
    val service: Service
}

class BattleShipApplication: DependenciesContainer, Application() {

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cache(Cache(directory = cacheDir, maxSize = 50 * 1024 * 1024))
            .build()
    }
    private val listenerClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .eventListener(AppListener())
            .build()
    }

    override val service: Service by lazy {
        //FakeBattleShipService()
        BattleShipService(httpClient, listenerClient)
    }
}