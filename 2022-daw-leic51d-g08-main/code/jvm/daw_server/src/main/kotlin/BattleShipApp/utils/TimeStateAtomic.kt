package BattleShipApp.utils

import BattleShipApp.service.InitialTurnTimeControl
import java.util.concurrent.atomic.AtomicInteger

class TimeStateAtomic(private val i: Int) {
    private val atomicInt = AtomicInteger(0)
    val value get() = atomicInt.get()

    fun changeTimeState(t: InitialTurnTimeControl.TimeState): Int{
        while (true) {
            val observer = atomicInt.get()
            val next = t.ordinal
            if (atomicInt.compareAndSet(observer, next)) {
                return next
            }
        }
    }

}

fun Int.atomicToTimeState() = InitialTurnTimeControl.TimeState.values()[this]
