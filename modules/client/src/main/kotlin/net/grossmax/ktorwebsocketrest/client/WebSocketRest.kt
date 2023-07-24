package net.grossmax.ktorwebsocketrest.client


import net.grossmax.ktorwebsocketrest.shared.WebsocketMessage
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.*

suspend fun HttpClient.webSocketRPC(
    urlString: String,
    store: WebsocketHandlerStore,
    token: String,
    json: Json
) {

    webSocket(
        urlString = urlString
    ) {

        try {
            sendSerialized(
                WebsocketMessage(
                    direction = "REQUEST",
                    uuid = UUID.randomUUID().toString(),
                    type = "init",
                    code = 200,
                    payload = buildJsonObject {
                        put("authToken", token)
                    }
                )
            )

            val initMessage = incoming.receive() as? Frame.Text
            println("Init: ${initMessage?.readText() ?: "null"}")

            while (true) {
                val othersMessage = incoming.receive() as? Frame.Text ?: continue
                val text = othersMessage.readText()
                val message = json.decodeFromString<WebsocketMessage>(text)

                println("Receive! ${text}")

                if (message.direction == "REQUEST") {

                    val handler = store.handlers[message.type]
                        ?: continue
                    try {
                        val scope = WebsocketHandlerStore.HandlerScope(
                            message = message,
                            json = json
                        )
                        handler(scope)

                        val responseCode = scope.call.responseCode
                        println("Send: ${responseCode}")


                        if (responseCode == null) {


                            sendSerialized(
                                WebsocketMessage(
                                    direction = "RESPONSE",
                                    uuid = message.uuid,
                                    type = message.type,
                                    code = 404,
                                    payload = buildJsonObject { }
                                )
                            )
                        } else {
                            val json = scope.call.responsePayload

                            println("Send: ${Json.encodeToString(json)}")

                            sendSerialized(
                                WebsocketMessage(
                                    direction = "RESPONSE",
                                    uuid = message.uuid,
                                    type = message.type,
                                    code = responseCode.value,
                                    payload = scope.call.responsePayload ?: buildJsonObject { }
                                )
                            )
                        }
                    } catch (e: Exception) {
                        sendSerialized(
                            WebsocketMessage(
                                direction = "RESPONSE",
                                uuid = message.uuid,
                                type = message.type,
                                code = 500,
                                payload = buildJsonObject {
                                    put("message", e.message)
                                }
                            )
                        )

                    }
                }
            }
        } catch (e: Exception) {
            println("Exception: $e")
        }

    }

}