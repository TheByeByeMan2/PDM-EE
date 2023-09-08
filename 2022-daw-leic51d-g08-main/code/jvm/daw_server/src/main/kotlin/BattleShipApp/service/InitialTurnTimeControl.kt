package BattleShipApp.service

import BattleShipApp.domain.Game
import BattleShipApp.domain.InputUpdatePerShot
import BattleShipApp.errors.SemanticErrorException
import BattleShipApp.repository.Repositories
import BattleShipApp.transactions.Data
import BattleShipApp.transactions.Transaction
import BattleShipApp.utils.TimeStateAtomic
import BattleShipApp.utils.atomicToTimeState
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.sql.Timestamp
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

@Component
class InitialTurnTimeControl(
    private val dataBase: Data,
    private val rep: Repositories,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val executor = Executors.newSingleThreadScheduledExecutor()


    enum class TimeState{
        STOP, START, END, INTERRUPTED
    }

    val atomic = TimeStateAtomic(0)
    val timeState  get() = atomic.value.atomicToTimeState()

    data class SupportClass1(val gameId: Int, val time: Timestamp, val timeout: Long)

    private fun startInitialTurnTimer(gameId: Int) {
        logger.info("Start timer")
        while (timeState == TimeState.START) {
            val trans1 = dataBase.createTransaction()
            val support = trans1.executeTransaction<SupportClass1>(trans1) {
                val game = rep.gamesRep.getGameById(it, gameId)
                val time = game.initialTurn!!
                val timeout = rep.rulesRep.getRule(it, game.ruleId).timeout
                SupportClass1(gameId, time, timeout.toLong())
            }
            timeCounter(support.time, support.timeout)
            if (timeState == TimeState.INTERRUPTED) break
            val trans2 = dataBase.createTransaction()
            if (timeState === TimeState.END) {
                trans2.executeTransaction<Unit>(trans2) {
                    updatePerShotAndTurn(it, gameId)
                    //timeState = TimeState.START
                    atomic.changeTimeState(TimeState.START)
                    updateInitialTurn(it, gameId)
                }
            }
        }
        logger.info("timer is stop by ${timeState.name}")
    }

    private fun updatePerShotAndTurn(transaction: Transaction, gameId: Int) {
        val gameInfo = rep.gamesRep.getGameById(transaction, gameId)
        val rule = rep.rulesRep.getRule(transaction, gameInfo.ruleId)
        val shot = gameInfo.remainingShot - 1
        val newGameInfo = rep.gamesRep.updatePerShot(transaction, InputUpdatePerShot(gameId, shot))
        if (newGameInfo.remainingShot == 0) {
            rep.gamesRep.switchTurn(transaction, gameId)
            rep.gamesRep.updatePerShot(transaction, InputUpdatePerShot(gameId, rule.shotNumber))
        }
    }

    private fun timeCounter(initialTime: Timestamp, timeout: Long) {
        val startTime = initialTime.time
        var currentTime = System.currentTimeMillis()
        var delta = currentTime - startTime
        while (delta < timeout && timeState == TimeState.START) {
            currentTime = System.currentTimeMillis()
            delta = currentTime - startTime
        }
        if (delta >= timeout && timeState == TimeState.START) {
            //timeState = TimeState.END
            atomic.changeTimeState(TimeState.END)
        }
    }

    private fun updateInitialTurn(transaction: Transaction, gameId: Int): Game {
        if (gameId <= 0) throw SemanticErrorException("The gameId id cannot be negative")
        if (!rep.gamesRep.checkIfGameIdExists(
                transaction,
                gameId
            )
        ) throw SemanticErrorException("The game id not exist")
        return rep.gamesRep.updateInitialTurn(transaction, gameId)
    }

    /**
     * update the game initial turn time and start the time counter
     *
     */
    fun updateInitialTurnLogicTrans(gameId: Int) {
        executor.submit(worker(gameId))
    }


    fun worker(gameId: Int) = Runnable {
        val trans = dataBase.createTransaction()
        trans.executeTransaction(trans) {
            updateInitialTurn(it, gameId)
        }
        atomic.changeTimeState(TimeState.START)
        startInitialTurnTimer(gameId)
    }

    fun stopTimer() {
        atomic.changeTimeState(TimeState.INTERRUPTED)
        executor.awaitTermination(100L, TimeUnit.MILLISECONDS)
    }

}