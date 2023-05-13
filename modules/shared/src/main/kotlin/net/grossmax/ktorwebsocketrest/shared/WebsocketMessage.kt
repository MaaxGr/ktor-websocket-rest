package net.grossmax.ktorwebsocketrest.shared

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
    data class WebsocketMessage(
    val direction: String,
    val uuid: String,
    val type: String,
    val payload: JsonElement,
    val code: Int
    )