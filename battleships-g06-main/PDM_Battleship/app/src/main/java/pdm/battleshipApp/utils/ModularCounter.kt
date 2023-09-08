package pdm.battleshipApp.utils

fun colCalculator(count: Int, modular: Int): Int {
    val div = count % modular
    return if (div == 0) modular
    else div
}

fun rowCounter(count: Int, modular: Int): Int{
    val div = count % modular
    return if (div == 0) count / modular
    else (count / modular)+1
}