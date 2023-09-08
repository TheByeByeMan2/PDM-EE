package pdm.battleshipApp.battleShip.model.coords

import kotlin.random.Random

enum class Direction { HORIZONTAL, VERTICAL }

class Position private constructor(val column: Column, val row: Row) {

    companion object {
        operator fun get(indexColumn: Int, indexRow: Int) = values[indexColumn][indexRow]
        operator fun get(indexColumn: Column, indexRow: Row) = values[indexColumn.ordinal][indexRow.ordinal]

        val values = createList()

        private fun createList(): ArrayList<List<Position>> {
            val list = arrayListOf<List<Position>>()
            Column.values.forEach { col ->
                val ar = arrayListOf<Position>()
                Row.values.forEach { row ->
                    ar.add(Position(col, row))
                }
                list.add(ar)
            }
            return list
        }

        fun random(direction: Direction, size: Int): Position {
            return if (direction === Direction.HORIZONTAL){
                val r = Random.nextInt(0, ROW_DIM )
                val c = Random.nextInt(0, COLUMN_DIM - size)
                Position[c.indexToColumn(), r.indexToRow()]
            } else {
                val r = Random.nextInt(0, ROW_DIM - size)
                val c = Random.nextInt(0, COLUMN_DIM)
                Position[c.indexToColumn(), r.indexToRow()]
            }
        }
    }
    override fun toString() = "${column.letter}${row.number}"
}

fun String.toPositionOrNull(): Position? {
    val col = this[0].toColumnOrNull()
    val row = this.drop(1).toInt().toRowOrNull()
    if(col == null || row == null)
        return null
    return Position[col, row]
}

fun String.toPosition() = this.toPositionOrNull() ?: throw IllegalStateException()
