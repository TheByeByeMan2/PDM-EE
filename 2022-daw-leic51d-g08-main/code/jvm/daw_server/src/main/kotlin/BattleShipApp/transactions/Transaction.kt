package BattleShipApp.transactions

import org.springframework.stereotype.Component

/**
 * Represents the function that is used to manipulate the database
 */


interface Transaction {
    fun <T> executeTransaction(transaction: Transaction, function: (Transaction)->T): T
}