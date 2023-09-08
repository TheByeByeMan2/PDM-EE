package BattleShipApp.service

import BattleShipApp.domain.*
import BattleShipApp.errors.AlreadyInUse
import BattleShipApp.errors.SemanticErrorException
import BattleShipApp.repository.Repositories
import BattleShipApp.transactions.Data
import BattleShipApp.transactions.Transaction
import BattleShipApp.utils.checkUserById
import BattleShipApp.utils.noEmptyString
import BattleShipApp.utils.noNegative
import org.springframework.stereotype.Component

@Component
class UserServices(
    private val dataBase: Data,
    private val rep: Repositories,
) {
    private fun userLogin(transaction: Transaction, user: UsernameAndPass): UserAndToken {
        val id = rep.usersRep.getUserIdByNameAndPass(transaction = transaction, user = user)
        return rep.usersRep.getUserAndTokenById(transaction, id)
    }

    fun userLoginTrans(user: UsernameAndPass): UserAndToken {
        val trans = dataBase.createTransaction()
        noEmptyString(user.userName, "username")
        noEmptyString(user.pass, "password")
        return trans.executeTransaction(trans) {
            userLogin(trans, user)
        }
    }
    private fun checkToken(transaction: Transaction, token: String) {
        val check = getUserByToken(transaction, token)
        if (check != null) AlreadyInUse("Token in use")
    }

    fun getUserById(transaction: Transaction, userId:Int): User{
        return rep.usersRep.getUserById(transaction, userId)
    }

    fun getUserByToken(transaction: Transaction, token: String): User? {
        val userId = rep.tokensRepository.getUserIdByToken(transaction, token)
        return userId?.let { rep.usersRep.getUserById(transaction, it) }
    }

    fun getUserByTokenTrans(token: String): User? {
        val trans = dataBase.createTransaction()
        return trans.executeTransaction(trans){
            getUserByToken(it, token)
        }
    }

    fun updateUserState(transaction: Transaction, userIdAndUserState: InputUserIdAndUserState): User{
        return rep.usersRep.updateUserState(transaction, userIdAndUserState)
    }

    private fun userRegister(transaction: Transaction, user: UsernameAndPass, token: String): User {
        val res = rep.usersRep.createUser(transaction, user)
        checkToken(transaction,token)
        rep.tokensRepository.createUserToken(transaction, InputUserToken(res.userId, token))
        return res
    }

    fun userRegisterTrans(user: UsernameAndPass, token: String): User {
        val trans = dataBase.createTransaction()
        noEmptyString(user.userName, "username")
        noEmptyString(user.pass, "password")
        checkPassword(user.pass)
        return trans.executeTransaction(trans) {
            if (rep.usersRep.checkIfUserNameExists(it, user.userName)) throw IllegalStateException("The user is already registered")
            userRegister(it, user, token)
        }
    }

    /** user data **/

    private fun getUsersDetailsList(transaction: Transaction, limit: Int): List<UserDetail> {
        return rep.usersRep.usersDetailsList(transaction, limit)
    }

    fun getUsersDetailsListTrans(limit: Int): List<UserDetail> {
        val trans = dataBase.createTransaction()
        noNegative(limit, "Ranking limit")
        return trans.executeTransaction<List<UserDetail>>(trans) {
            getUsersDetailsList(it, limit)
        }
    }

    private fun getUserDetailsById(transaction: Transaction, id: Int): UserDetail {
        return rep.usersRep.getUserDetailsById(transaction, id)
    }

    fun getUserDetailsByIdTrans(id: Int): UserDetail {
        noNegative(id, "user id")
        val trans = dataBase.createTransaction()
        return trans.executeTransaction<UserDetail>(trans) {
            checkUserById(trans, id)
            getUserDetailsById(it, id)
        }
    }


    private fun updateUserDetails(transaction: Transaction, uus: InputUsersAndScores): List<UserDetail> {
        negativeNumberCheck(uus.userIdA, "user a id")
        negativeNumberCheck(uus.userIdB, "user b id")

        if (!rep.usersRep.checkIfUserIdExists(
                transaction,
                uus.userIdA
            )
        ) throw SemanticErrorException("The user a id not exist")
        if (!rep.usersRep.checkIfUserIdExists(
                transaction,
                uus.userIdB
            )
        ) throw SemanticErrorException("The user b id not exist")
        return rep.usersRep.updateUsersScoreAndGamePlayed(transaction, uus)

    }

    fun updateUserDetailsTrans(uus: InputUsersAndScores): List<UserDetail> {
        val trans = dataBase.createTransaction()
        return trans.executeTransaction(trans) {
            updateUserDetails(it, uus)
        }
    }

    /**
     * Checks if the password is valid
     * @param password the password
     */
    private fun checkPassword(password: String) {
        val specialCharacters = arrayOf('?', '#', '!', '*', '%', '&', '@')
        if (password.isBlank()) throw SemanticErrorException("The user email cannot be empty or full empty spaces")
        if (password.find { it.isUpperCase() } == null) throw SemanticErrorException("The user password need at least 1 uppercase character")
        if (password.find { it.isLowerCase() } == null) throw SemanticErrorException("The user password need at least 1 lowercase character")
        if (password.find { char -> char in specialCharacters } == null) throw SemanticErrorException("The user password need at least 1 special character")
        if (password.find { it.isDigit() } == null) throw SemanticErrorException("The user password need at least 1 digit")
    }

    private fun negativeNumberCheck(id: Int, parameter: String) {
        if (id <= 0) throw SemanticErrorException("The $parameter cannot be negative")
    }
}
