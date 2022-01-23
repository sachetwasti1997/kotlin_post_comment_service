package com.sachet.post_comment_microservice.custom_exception

import org.springframework.http.HttpStatus

data class ErrorResponse(
    val message: String ?,
    val errorCode: HttpStatus
)