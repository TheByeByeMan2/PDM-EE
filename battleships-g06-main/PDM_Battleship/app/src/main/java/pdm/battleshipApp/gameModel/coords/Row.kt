package pdm.battleshipApp.battleShip.model.coords

val ROW_DIM = Row.values.size

enum class Row (private val n: Int){
    ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5),
    SIX(6), SEVEN(7), EIGHT(8), NINE(9), TEN(10);

    companion object {
        val values = values()
    }

    val number: Int = this.n
}

fun Int.toRowOrNull() = if (this-1 in 0..9) Row.values[this-1] else null

fun Int.toRow()= this.toRowOrNull() ?: throw IndexOutOfBoundsException()

fun Int.indexToRowOrNull() = if (this in 0..9) Row.values[this] else null

fun Int.indexToRow()= this.indexToRowOrNull() ?: throw IndexOutOfBoundsException()
