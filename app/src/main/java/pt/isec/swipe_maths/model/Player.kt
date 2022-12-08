package pt.isec.swipe_maths.model

import java.net.Socket
import java.net.URL

data class Player(val name: String, val photoUrl: URL, val socket: Socket? = null)
