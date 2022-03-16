package com.mauter.oauth2server

class BadInputException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
class NotFoundException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
class UnauthorizedException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
