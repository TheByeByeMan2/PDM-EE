package pdm.battleshipApp.exceptions

import okhttp3.Response

abstract class AppException(msg: String) : Exception(msg)

abstract class ServerException(msg: String) : Exception(msg)

abstract class NetworkException(msg: String) : Exception(msg)

class JacksonException(
    val response: Response,
    msg: String = "Unexpected ${response.code} response from the API."
) : AppException(msg)

class PasswordsNotEqual() : AppException("The Password and Confirm Password are different")

class InvalidPositionException() : AppException("Invalid Position")

class PutAllShipException(msg: String = "Missing ships"): AppException(msg)

class InvalidPutException(msg: String = "Invalid Put") : AppException(msg)

class ServerUnknownException(msg: String = "") : ServerException("Panic!!!!, more info: $msg")

data class ServerWaitingRoomException(val theProblem: String = "") : ServerException(theProblem)

data class ServerGameException(val theProblem: String = "") : ServerException(theProblem)

data class AppGameException(val theProblem: String = "") : AppException(theProblem)

data class ServerProblem(val theProblem: String = "") : ServerException(theProblem)

class ListenerException(msg: String) : ServerException(msg)