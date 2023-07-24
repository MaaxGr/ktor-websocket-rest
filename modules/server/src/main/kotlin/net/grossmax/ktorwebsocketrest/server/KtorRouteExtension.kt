package net.grossmax.ktorwebsocketrest.server

import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import net.grossmax.ktorwebsocketrest.shared.WebsocketMessage

fun Route.webSocketRPC(websocketConnectionManager: WebsocketConnectionManager, path: String) {
    webSocket(path) {
        try {
            val firstMessage = receiveDeserialized<WebsocketMessage?>() ?: return@webSocket close()

            val authToken = firstMessage.payload.jsonObject["authToken"]?.jsonPrimitive?.content ?: return@webSocket close()
            println("Auth Token: $authToken")

            sendSerialized(
                WebsocketMessage(
                    direction = "RESPONSE",
                    uuid = firstMessage.uuid,
                    type = "init",
                    code = 200,
                    payload = buildJsonObject {
                        put("message", "You are connected!")
                    }
                )
            )

            if (websocketConnectionManager.connectedSockets.containsKey(authToken)) {
                websocketConnectionManager.connectedSockets[authToken]?.close()
            }

            websocketConnectionManager.connectedSockets[authToken] = Channel()

            launch {
                while(true) {
                    val message = websocketConnectionManager.connectedSockets[authToken]?.receive() ?: continue
                    send(message)
                }
            }

            for (frame in incoming) {
                frame as? Frame.Text ?: continue
                val receivedText = frame.readText()
                val receivedObject = websocketConnectionManager.json.decodeFromString<WebsocketMessage>(receivedText)

                println("Received: $receivedObject")

                if (receivedObject.direction == "RESPONSE") {
                    websocketConnectionManager.answers[receivedObject.uuid]?.send(receivedObject)
                }
            }
        } catch (e: ClosedReceiveChannelException) {
            e.printStackTrace()
        }
    }

}