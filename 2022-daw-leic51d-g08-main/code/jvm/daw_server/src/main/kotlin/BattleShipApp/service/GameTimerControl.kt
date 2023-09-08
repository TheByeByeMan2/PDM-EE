package BattleShipApp.service

import BattleShipApp.errors.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class GameTimerControl(val initialTurnTimeControl: InitialTurnTimeControl) {
    private val timers = mutableMapOf<Int, InitialTurnTimeControl>()
    private val logger = LoggerFactory.getLogger(javaClass)

    fun startTimer(gameId: Int){
        if (timers[gameId] == null){
            timers[gameId] = initialTurnTimeControl
        }
        timers[gameId]?.updateInitialTurnLogicTrans(gameId)
    }

    fun updateTimerState(gameId: Int, timeState: InitialTurnTimeControl.TimeState){
        if (timers[gameId] == null) throw NotFoundException("Not found timer when update timer state")
        else timers[gameId]!!.atomic.changeTimeState(timeState)
    }

    fun endTimer(gameId: Int) {
        if (timers[gameId] != null) {
            updateTimerState(gameId, InitialTurnTimeControl.TimeState.INTERRUPTED)
            timers[gameId]!!.stopTimer()
        }
        timers.remove(gameId)
    }
}