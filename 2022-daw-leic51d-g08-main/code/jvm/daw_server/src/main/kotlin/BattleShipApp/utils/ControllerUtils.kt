package BattleShipApp.utils

import BattleShipApp.controllers.PutRemove
import BattleShipApp.domain.Register
import BattleShipApp.domain.User
import BattleShipApp.domain.UsernameAndPass
import BattleShipApp.errors.SemanticErrorException
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import javax.servlet.http.HttpServletRequest

fun String.toPutRemove(): PutRemove {
    return this.toPutRemoveOrNull()
        ?: throw IllegalStateException("Cannot convert this string $this to PutRemove")
}

fun String.toPutRemoveOrNull(): PutRemove? = PutRemove.values().find { it.name == this.toUpperCase() }

fun checkPut(shipName: String?, action: String){
    if (shipName == null && action.toPutRemoveOrNull() == null) throw SemanticErrorException("Ship name is null or cannot convert $action to PutRemove")
}

fun Register.toUsernameAndPass()=UsernameAndPass(this.userName, this.pass)

fun badRequest(msg:String = "bad request") = ResponseEntity.badRequest()
    .contentType(MediaType.APPLICATION_JSON)
    .body(msg)

fun userTokenCheck(
    request: HttpServletRequest,
    userId: Int
): Boolean {
    val userByToken = request.getAttribute("UserArgumentResolver")
    val user = userByToken as User
    if (userId != user.userId) return true
    return false
}