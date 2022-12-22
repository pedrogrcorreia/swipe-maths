package pt.isec.swipe_maths.network

enum class Requests {
    // Server Request
    UPDATE_PLAYERS_LIST,
    START_GAME,
    NEW_LEVEL,


    NONE,
    UPDATE_VIEWS,


    // Client Requests
    NEW_PLAYER,
    ROW_PLAY,
    COL_PLAY,
}