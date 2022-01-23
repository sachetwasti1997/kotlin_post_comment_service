package com.sachet.post_comment_microservice.exception_handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.sachet.post_comment_microservice.custom_exception.ErrorResponse
import com.sachet.post_comment_microservice.custom_exception.PostCommentDataException
import com.sachet.post_comment_microservice.custom_exception.PostCommentNotFoundException
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class GlobalErrorHandler: ErrorWebExceptionHandler {

    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
        val dataFactory = exchange.response.bufferFactory()
        val errorResponse: ErrorResponse
        if (ex is PostCommentDataException){
            errorResponse = ErrorResponse(ex.message, HttpStatus.BAD_REQUEST)
            exchange.response.statusCode = HttpStatus.BAD_REQUEST
        }else if (ex is PostCommentNotFoundException){
            errorResponse = ErrorResponse(ex.message, HttpStatus.NOT_FOUND)
            exchange.response.statusCode = HttpStatus.NOT_FOUND
        } else {
            errorResponse = ErrorResponse(ex.message, HttpStatus.INTERNAL_SERVER_ERROR)
            exchange.response.statusCode = HttpStatus.INTERNAL_SERVER_ERROR
        }
        val mapper = ObjectMapper()
        val jsonString = mapper.writeValueAsString(errorResponse)
        val errorMessage = dataFactory.wrap(jsonString.toByteArray())
        return exchange.response.writeWith(Mono.just(errorMessage))
    }
}