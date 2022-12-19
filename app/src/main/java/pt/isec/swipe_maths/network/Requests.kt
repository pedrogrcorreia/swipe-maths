package pt.isec.swipe_maths.network

enum class Requests {
    // Server Request
    UPDATE_PLAYERS_LIST,
    START_GAME,
    ROW_PLAYED,
    COL_PLAYED,
    UPDATE_TIMER,


    NONE,



    // Client Requests
    NEW_PLAYER,
    ROW_PLAY,
    COL_PLAY
}