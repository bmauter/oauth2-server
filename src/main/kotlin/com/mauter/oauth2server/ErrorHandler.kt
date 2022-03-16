package com.mauter.oauth2server

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.ModelAndView
import java.time.Instant
import java.util.*

@ControllerAdvice
class ErrorHandler {
    private val log: Logger = LoggerFactory.getLogger(ErrorHandler::class.java)

    @ExceptionHandler(value = [(UnauthorizedException::class)])
    fun handle(exception: UnauthorizedException) = handle(HttpStatus.UNAUTHORIZED, exception)

    @ExceptionHandler(value = [(BadInputException::class)])
    fun handle(exception: BadInputException) = handle(HttpStatus.BAD_REQUEST, exception)

    @ExceptionHandler(value = [(MissingServletRequestParameterException::class)])
    fun handle(exception: MissingServletRequestParameterException) = handle(HttpStatus.BAD_REQUEST, exception)

    @ExceptionHandler(value = [(NotFoundException::class)])
    fun handle(exception: NotFoundException) = handle(HttpStatus.NOT_FOUND, exception)

    @ExceptionHandler(value = [(Throwable::class)])
    fun handle(exception: Throwable) = handle(HttpStatus.INTERNAL_SERVER_ERROR, exception)

    fun handle(status: HttpStatus, throwable: Throwable, message: String? = throwable.message): ModelAndView {
        val eventId = UUID.randomUUID()
        log.error("Exception with eventId={}", eventId, throwable)

        val mav = ModelAndView("error", status)
        mav.addObject("eventId", eventId)
        mav.addObject("occuredAt", Instant.now())
        mav.addObject("message", message)
        return mav
    }
}
