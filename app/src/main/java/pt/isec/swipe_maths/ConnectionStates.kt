package pt.isec.swipe_maths

enum class ConnectionStates {
    SERVER_CONNECTING,
    SERVER_CONNECTED,
    SERVER_ERROR,
    NO_CONNECTION,
    CLIENT_CONNECTING,
    CONNECTION_ESTABLISHED,
    CONNECTION_ERROR,
    CONNECTION_ENDED,
}