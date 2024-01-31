package kea.dpang.auth.controller

import kea.dpang.auth.base.ErrorResponse
import kea.dpang.auth.exception.*

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import java.time.LocalDateTime

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(
        InvalidPasswordException::class,
        InvalidRefreshTokenException::class,
        InvalidVerificationCodeException::class
    )
    fun handleBadRequestException(ex: RuntimeException, request: WebRequest): ResponseEntity<ErrorResponse> {
        return generateErrorResponse(ex, request, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(
        TokenNotFoundException::class,
        UserNotFoundException::class,
        VerificationCodeNotFoundException::class
    )
    fun handleNotFoundException(ex: RuntimeException, request: WebRequest): ResponseEntity<ErrorResponse> {
        return generateErrorResponse(ex, request, HttpStatus.NOT_FOUND)
    }

    private fun generateErrorResponse(
        ex: RuntimeException,
        request: WebRequest,
        status: HttpStatus
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = status.value(),
            error = status.name,
            message = ex.message ?: "세부 정보가 제공되지 않았습니다",
            path = request.getDescription(false)
        )

        return ResponseEntity(errorResponse, status)
    }
}
