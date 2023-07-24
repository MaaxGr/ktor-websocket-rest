package net.grossmax.ktorwebsocketrest.server

interface ClientRepository {

    fun findById(id: Int): Client?

    data class Client(
        val clientId: Int,
        val clientAuthToken: String
    )

}