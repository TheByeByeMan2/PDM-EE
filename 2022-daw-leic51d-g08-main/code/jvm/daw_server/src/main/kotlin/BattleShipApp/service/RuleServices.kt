package BattleShipApp.service

import BattleShipApp.domain.Rule
import BattleShipApp.repository.Repositories
import BattleShipApp.transactions.Data
import BattleShipApp.transactions.Transaction
import org.springframework.stereotype.Component

@Component
class RuleServices(
    private val dataBase: Data,
    private val rep: Repositories,
) {

    private fun getAllRules(transaction: Transaction):List<Rule> = rep.rulesRep.getAllRules(transaction)

    fun getAllRulesTrans(): List<Rule>{
        val trans = dataBase.createTransaction()
        return trans.executeTransaction(trans){
            getAllRules(it)
        }
    }
}