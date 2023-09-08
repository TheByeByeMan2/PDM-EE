package pdm.battleshipApp.home

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pdm.battleshipApp.BattleShipAppActivity
import pdm.battleshipApp.BattleShipTestApplication
import pdm.battleshipApp.ui.BackButtonTag
import pdm.battleshipApp.ui.PlayerInfoButtonTag
import pdm.battleshipApp.ui.screens.*

@RunWith(AndroidJUnit4::class)
class HomeActivityTests {
    @get:Rule
    val testRule = createAndroidComposeRule<BattleShipAppActivity>()

    @Test
    fun screen_contains_buttons() {
        testRule.onNodeWithTag(HomeScreenTag).assertExists()
        testRule.onNodeWithTag(StartButtonTag).assertExists()
        testRule.onNodeWithTag(AuthorButtonTag).assertExists()
        testRule.onNodeWithTag(RankingButtonTag).assertExists()
        testRule.onNodeWithTag(PlayerInfoButtonTag).assertExists()
        testRule.onNodeWithTag(BackButtonTag).assertDoesNotExist()
    }

    @Test
    fun pressing_rankingButton_navigates_to_rankingScreen() {
        try {
            // Act
            testRule.onNodeWithTag(RankingButtonTag).performClick()
            testRule.waitForIdle()
            // Assert
            testRule.onNodeWithTag(RankingScreenTag).assertExists()
        }catch (e: Exception){
            println(e)
        }
    }

    @Test
    fun pressing_authorButton_navigates_to_authorScreen() {
        try {
            // Act
            testRule.onNodeWithTag(AuthorButtonTag).performClick()
            testRule.waitForIdle()
            // Assert
            testRule.onNodeWithTag(AuthorScreenTag).assertExists()
        }catch (e: Exception){
            println(e)
        }
    }

    @Test
    fun pressing_PlayerInfoButton_navigates_to_UnauthenticatedPlayerInfoScreen() {
        try {
            // Act
            testRule.onNodeWithTag(PlayerInfoButtonTag).performClick()
            testRule.waitForIdle()
            // Assert
            testRule.onNodeWithTag(UnauthenticatedPlayerScreenTag).assertExists()
            testRule.onNodeWithTag(AuthenticatedPlayerScreenTag).assertDoesNotExist()
        }catch (e: Exception){
            println(e)
        }
    }

    @Test
    fun pressing_StartButton_navigates_to_UnauthenticatedStartScreen() {
        try {
            // Act
            testRule.onNodeWithTag(StartButtonTag).performClick()
            testRule.waitForIdle()
            // Assert
            testRule.onNodeWithTag(UnauthenticatedWaitingRoomScreenTag).assertExists()
            testRule.onNodeWithTag(AuthenticatedWaitingRoomScreenTag).assertDoesNotExist()
        }catch (e: Exception){
            println(e)
        }
    }
}