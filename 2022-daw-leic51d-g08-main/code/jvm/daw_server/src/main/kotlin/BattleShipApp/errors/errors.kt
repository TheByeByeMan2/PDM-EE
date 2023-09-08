package BattleShipApp.errors


/**
 * Thrown to indicate that a method has not been passed an argument.
 */
class BadRequestException(message: String) : Exception(message)
/**
 * Thrown to indicate that a method has not been passed an argument.
 */
class SemanticErrorException(message: String) : Exception(message)
/**
 * Thrown to indicate that a method didn´t fount the resource needed
 */
class NotFoundException(message: String) : Exception(message)
/**
 * Thrown to indicate that user is unauthenticated
 */
class UnauthenticatedException(message: String) : Exception(message)
/**
 * Thrown to indicate that a resource with that name already exists.
 */
class AlreadyInUse(message: String) : Exception(message)
/**
 * Thrown to indicate that something failed.
 */
class FailException(message: String) : Exception(message)

class GameFinishException(message: String): Exception(message)