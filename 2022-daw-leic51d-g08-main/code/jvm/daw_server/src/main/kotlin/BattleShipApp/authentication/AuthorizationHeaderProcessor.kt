package BattleShipApp.authentication

import BattleShipApp.domain.User
import BattleShipApp.service.UserServices
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class AuthorizationHeaderProcessor(
    val usersService: UserServices
) {

    fun process(authorizationValue: String?): User? {
        if (authorizationValue == null) {
            return null
        }
        val parts = authorizationValue.trim()
        if (parts.isBlank()) {
            return null
        }
        val code = parts.split(' ')
        if (code[0] == SCHEME)
            return usersService.getUserByTokenTrans(code[1])
        return usersService.getUserByTokenTrans(parts)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AuthorizationHeaderProcessor::class.java)
        const val SCHEME = "Bearer"
    }
}
