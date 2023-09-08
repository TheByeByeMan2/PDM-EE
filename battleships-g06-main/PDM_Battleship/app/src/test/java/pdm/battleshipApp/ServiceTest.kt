package pdm.battleshipApp

import junit.framework.Assert.assertNotNull
import kotlinx.coroutines.runBlocking
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.junit.Test
import pdm.battleshipApp.service.AppListener
import pdm.battleshipApp.service.BattleShipService

class ServiceTest {

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .build()
    }
    private val listenerClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .eventListener(AppListener())
            .build()
    }
    val service = BattleShipService(httpClient, listenerClient)

    @Test
    fun fetch_ranking(){
        runBlocking {
            val list = service.fetchRanking()
            assertNotNull(list)
            println(list)
        }
    }

    @Test
    fun login_test(){
        runBlocking {
            val res = service.login("Player2", "Player2Pass@")
            assertNotNull(res)
            println(res)
        }
    }

    @Test
    fun get_user_info(){
        runBlocking {
            val res = service.fetchUserInfo(1, "23cbe039-baf6-44ca-a8c8-6bbb22dbf724")
            assertNotNull(res)
            println(res)
        }
    }
}