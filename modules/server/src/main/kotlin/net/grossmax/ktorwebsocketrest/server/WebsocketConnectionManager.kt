package net.grossmax.ktorwebsocketrest.server

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import net.grossmax.ktorwebsocketrest.shared.WebsocketMessage
import java.util.*

class WebsocketConnectionManager(
    private val clientRepository: ClientRepository,
    val json: Json = Json
) {

    val connectedSockets = mutableMapOf<String, Channel<String>>()
    val answers = mutableMapOf<String, Channel<WebsocketMessage>>()

    suspend fun sendToSocket(deviceId: Int, message: String, payload: JsonElement = buildJsonObject {  }): WebsocketMessage? {
        val client = clientRepository.findById(deviceId)
            ?: return null

        val requestUUID = UUID.randomUUID().toString()
        val requestMessage = WebsocketMessage(
            direction = "REQUEST",
            uuid = requestUUID,
            type = message,
            code = 200,
            payload = payload
        )

        val final = json.encodeToString(requestMessage)

        println("SEND: $final")

        val socket = connectedSockets[client.clientAuthToken]
            ?: return null

        socket.send(final)
        val channel = Channel<WebsocketMessage>()
        answers[requestUUID] = channel

        val result = withTimeoutOrNull(5000) {
            channel.receive()
        }
        channel.close()
        answers.remove(requestUUID)
        return result
    }

}