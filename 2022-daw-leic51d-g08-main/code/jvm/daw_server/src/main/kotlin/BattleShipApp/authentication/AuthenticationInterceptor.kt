package BattleShipApp.authentication

import BattleShipApp.controllers.*
import BattleShipApp.domain.InputUserIdGameId
import BattleShipApp.domain.JoinWaitingRoomInfo
import BattleShipApp.domain.UsernameAndPass
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Component
class AuthenticationInterceptor(
    private val authorizationHeaderProcessor: AuthorizationHeaderProcessor
) : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler is HandlerMethod && handler.methodParameters.any {
                it.parameterType == JoinWaitingRoomInfo::class.java ||
                        it.parameterType == UserId::class.java ||
                        it.parameterType == UserPutOrRemoveShip::class.java ||
                        it.parameterType == UserIdAndPosition::class.java ||
                        it.parameterType == Int::class.java || it.parameterType == InputUserIdGameId::class.java
            }
        ) {
            val user = authorizationHeaderProcessor.process(request.getHeader(NAME_AUTHORIZATION_HEADER))
            return if (user == null) {
                response.status = 401
                response.addHeader(NAME_WWW_AUTHENTICATE_HEADER, AuthorizationHeaderProcessor.SCHEME)
                false
            } else {
                UserArgumentResolver.addUserTo(user, request)
                return true
            }
        }

        return true
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AuthenticationInterceptor::class.java)
        private const val NAME_AUTHORIZATION_HEADER = "Authorization"
        private const val NAME_WWW_AUTHENTICATE_HEADER = "WWW-Authenticate"
    }
}
