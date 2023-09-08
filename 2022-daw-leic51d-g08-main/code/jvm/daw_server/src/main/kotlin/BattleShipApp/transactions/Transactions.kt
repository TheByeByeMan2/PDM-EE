package BattleShipApp.transactions

import org.postgresql.ds.PGSimpleDataSource
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.sql.Connection
import java.sql.DatabaseMetaData

/**
 * Gets the data source
 * @return [PGSimpleDataSource] the data source
 */
@Bean
fun getDataSource(): PGSimpleDataSource {
    val dataSource = PGSimpleDataSource()
    dataSource.setURL("jdbc:postgresql://localhost:5432/postgres?user=postgres&password=postgres")
    return dataSource
}
@Component
class TransactionDB() : Transaction {
    val conn: Connection

    init {
        conn = initConnection()
    }

    /**
     * Starts the connection with data source
     * @return [Connection] the data source connection
     */
    private fun initConnection(): Connection {
        val conn = getDataSource().connection
        conn.autoCommit = false
        return conn
    }
    /**
     * Ends the transaction
     */
    private fun commit() {
        conn.commit()
    }

    /**
     * Closes the connection with database
     */
    private fun close() {
        conn.close()
    }

    /**
     * Aborts the transaction
     */
    private fun rollback() {
        conn.rollback()
    }

    /**
     * Executes the transaction
     * @param transaction the transaction to be executed
     * @param function what executes the transaction
     * @return [Any?] the result of the transaction
     */
    override fun <T> executeTransaction(transaction: Transaction, function: (Transaction) -> T): T {
        try {
            val res = function(transaction)
            commit()
            return res
        } catch (e: Exception) {
            rollback()
            throw e
        } finally {
            close()
        }
    }
}
