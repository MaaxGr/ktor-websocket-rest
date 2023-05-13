package net.grossmax.ktorwebsocketrest.client

import net.grossmax.ktorwebsocketrest.shared.WebsocketMessage
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

class WebsocketHandlerStore() {

    val handlers = mutableMapOf<String, suspend HandlerScope.() -> Unit>()

    fun register(route: String, handler: suspend HandlerScope.() -> Unit) {
        handlers[route] = handler
    }

    class HandlerScope(message: WebsocketMessage, json: Json) {
        val call = HandlerScopeCall(message, json)
    }

    class HandlerScopeCall(
        message: WebsocketMessage,
        val json: Json
    ) {

        var responseCode: HttpStatusCode? = null
        var responsePayload: JsonElement? = null

        val jsonPayload = message.payload

        inline fun <reified T : Any> receivePayload(): T {
            return json.decodeFromJsonElement(jsonPayload)
        }

        inline fun <reified T> respond(payload: T) {
            respond(HttpStatusCode.OK, payload)
        }

        inline fun <reified T> respond(code: HttpStatusCode, payload: T) {
            responseCode = code
            responsePayload = json.encodeToJsonElement(payload)
        }

    }

}