package pdm.battleshipApp.battleShip.model.coords

val COLUMN_DIM = Column.values.size

enum class Column(private val c: Char) {
    A('A'), B('B'), C('C'), D('D'), E('E'),
    F('F'), G('G'), H('H'), I('I'), J('J');

    companion object {
        val values = values()
    }

    val letter: Char = this.c
}

fun Char.toColumnOrNull() = Column.values.find { it.letter == this.uppercaseChar() }

fun Char.toColumn() = this.toColumnOrNull() ?: throw NoSuchElementException()

fun Int.indexToColumnOrNull() = if (this in 0..9) Column.values[this] else null


fun Int.indexToColumn() = this.indexToColumnOrNull() ?: throw IndexOutOfBoundsException()