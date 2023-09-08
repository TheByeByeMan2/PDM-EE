package BattleShipApp.transactions

import BattleShipApp.repository.GamesRepository
import BattleShipApp.repository.GridsRepository
import BattleShipApp.repository.UsersRepository
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component


/**
 * Represents the memory of the data and the functions that will be needed to manipulate the memory
 */

interface Data {
    fun usersRp(): UsersRepository

    fun gridsRp(): GridsRepository

    fun gamesRep(): GamesRepository

    fun createTransaction(): Transaction
}

@Component
class TypeDB() : Data {

    override fun usersRp(): UsersRepository {
        return UsersRepository()
    }

    override fun gridsRp(): GridsRepository {
        return GridsRepository()
    }

    override fun gamesRep(): GamesRepository {
        return GamesRepository()
    }

    /**
     * Creates the transaction needed to manipulate database
     * @return [Transaction] the transaction
     */

    override fun createTransaction(): Transaction {
        return TransactionDB()
    }

}
