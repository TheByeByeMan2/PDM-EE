package BattleShipApp.repository

import BattleShipApp.domain.*
import BattleShipApp.errors.FailException
import BattleShipApp.transactions.Transaction
import BattleShipApp.transactions.TransactionDB
import BattleShipApp.utils.ShipState
import OutPutDeleteGridCell
import java.sql.ResultSet

class GridsRepository {
    /**
     *
     *
     */
    fun createGridCell(transaction: Transaction, createGridInfo: InputGrid, state:String = ShipState.ALIVE.name): GridCell {
        val conn = (transaction as TransactionDB).conn
        try {
            val pst = conn.prepareStatement("insert into grids(gameId,userId, col, row, shipstate, shipname) values(?,?,?,?,?,?)")
            pst.setInt(1, createGridInfo.gameId)
            pst.setInt(2, createGridInfo.userId)
            pst.setInt(3, createGridInfo.col)
            pst.setInt(4, createGridInfo.row)
            pst.setString(5, state)
            pst.setString(6, createGridInfo.shipName)
            val rst = pst.executeUpdate()
            if (rst == 1) {
                val stm = conn.prepareStatement("select * from grids where gameId = ? and shipname = ? and userid = ? and col =? and row = ?")
                stm.setInt(1, createGridInfo.gameId)
                stm.setString(2, createGridInfo.shipName)
                stm.setInt(3, createGridInfo.userId)
                stm.setInt(4, createGridInfo.col)
                stm.setInt(5, createGridInfo.row)
                val rst2 = stm.executeQuery()
                if (rst2.next()) {
                    return resGrid(rst2)
                } else throw FailException("Error select gridId")
            }
            throw FailException("Error creating grid")
        } catch (e: Exception) {
            throw e
        }
    }

    fun updateGridCell(transaction: Transaction, grid: InputUpdateGridShipState): GridCell {
        val conn = (transaction as TransactionDB).conn
        try {
            val pst =
                conn.prepareStatement("UPDATE grids set shipstate = ? where gameId = ? and userid = ? and col = ? and row = ?")
            pst.setString(1, grid.shipState)
            pst.setInt(2, grid.gameId)
            pst.setInt(3, grid.userId)
            pst.setInt(4, grid.col)
            pst.setInt(5, grid.row)
            val rst = pst.executeUpdate()
            if (rst == 1) {
                val stm =
                    conn.prepareStatement("select * from grids where gameId = ? and userid = ? and col = ? and row = ?")
                stm.setInt(1, grid.gameId)
                stm.setInt(2, grid.userId)
                stm.setInt(3, grid.col)
                stm.setInt(4, grid.row)
                val rst2 = stm.executeQuery()
                if (rst2.next() && rst2.getString(5).equals(grid.shipState)) {
                    return resGrid(rst2)
                }
            }
            throw FailException("Error update grid")
        } catch (e: Exception) {
            throw e
        }

    }

    fun getGridInfo(transaction: Transaction, grid: InputGetGridByGameIdAndUserId): List<GridCell> {
        val conn = (transaction as TransactionDB).conn
        val l = mutableListOf<GridCell>()
        val pst = conn.prepareStatement("select * from grids where gameId = ? and userid = ?")
        pst.setInt(1, grid.gameId)
        pst.setInt(2, grid.userId)
        val rst = pst.executeQuery()
        while (rst.next()) {
            l.add(resGrid(rst))
        }
        return l
    }

    fun getCellInfo(transaction: Transaction, inputGrid: InputGridNonShipName): GridCell?{
        val conn = (transaction as TransactionDB).conn
        val pst = conn.prepareStatement("select * from grids where gameid = ? and userid =? and col = ? and row = ?")
        pst.setInt(1, inputGrid.gameId)
        pst.setInt(2, inputGrid.userId)
        pst.setInt(3, inputGrid.col)
        pst.setInt(4, inputGrid.row)
        val rst = pst.executeQuery()
        if (rst.next()){
            return resGrid(rst)
        }
        return null
    }

    fun deleteGridCell(transaction: Transaction, inputGrid: InputGridNonShipName): OutPutDeleteGridCell{
        val conn = (transaction as TransactionDB).conn
        try {
            val pst = conn.prepareStatement("delete from grids where userid = ? and gameid = ? and col = ? and row=?")
            pst.setInt(1,inputGrid.userId)
            pst.setInt(2, inputGrid.gameId)
            pst.setInt(3, inputGrid.col)
            pst.setInt(4, inputGrid.row)
            val rst = pst.executeUpdate()
            if (rst == 1){
                return OutPutDeleteGridCell(inputGrid.gameId, inputGrid.userId, inputGrid.col, inputGrid.row)
            }
            throw FailException("Fail delete from grids table")
        } catch (e: Exception){
            throw e
        }
    }

    fun deleteGrid(transaction: Transaction, inputGridList: List<InputGridNonShipName>): List<OutPutDeleteGridCell>{
        try {
            val res = mutableListOf<OutPutDeleteGridCell>()
            inputGridList.forEach {
                res.add(deleteGridCell(transaction, InputGridNonShipName(it.gameId, it.userId, it.col, it.row)))
            }
            return res
        } catch (e: Exception){
            throw e
        }
    }


    private fun resGrid(rst: ResultSet) = GridCell(
        rst.getInt(1),
        rst.getInt(2),
        rst.getInt(3),
        rst.getInt(4),
        rst.getString(5),
        rst.getString(6),
    )

}