package pt.isec.swipe_maths

enum class ConnectionStates {
    SERVER_CONNECTING, CLIENT_CONNECTING, CONNECTION_ESTABLISHED, WAITING_FOR_PLAYERS, CONNECTION_ERROR,
    CONNECTION_ENDED,
    START_GAME
}