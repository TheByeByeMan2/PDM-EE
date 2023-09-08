package pdm.battleshipApp.exceptions

class UnauthorizedException(): ServerException("Unauthorized User")

data class LoginException(val theProblem: String = ""): ServerException(theProblem)

data class RegisterServerException(val theProblem: String = ""): ServerException(theProblem)

class UserAlreadyExist():ServerException("The username is already exist")