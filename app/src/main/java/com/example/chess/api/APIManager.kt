package com.example.chess.api

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpCallValidator
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.CancellationException
import kotlinx.serialization.json.Json

class APIManager(id: String) {
    // no api key required
    var jsonHttpClient = HttpClient {
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        defaultRequest {
            url.host = "lichess.org"
            url.protocol = URLProtocol.HTTPS

            url.encodedPath = "/api/puzzle/${id}"
            Log.w("url", url.toString())
            contentType(ContentType.Application.Json)
        }
        HttpResponseValidator {
            getCustomResponseValidator(this)
        }
    }

    private fun getCustomResponseValidator(responseValidator: HttpCallValidator.Config):
            HttpCallValidator.Config {
        responseValidator.handleResponseExceptionWithRequest { exception, _ ->
            var exceptionResponseText =
                exception.message ?: "Unkown Error occurred. Please contact your administrator."
            if (exception is ClientRequestException) {
                val exceptionResponse = exception.response
                exceptionResponseText = exceptionResponse.bodyAsText()
            } else if (exception is ServerResponseException) {
                val exceptionResponse = exception.response
                exceptionResponseText = exceptionResponse.bodyAsText()
            }
            throw CancellationException(exceptionResponseText)
        }
        return responseValidator
    }
}