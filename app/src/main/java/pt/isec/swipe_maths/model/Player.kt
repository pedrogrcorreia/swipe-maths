package pt.isec.swipe_maths.model

import org.json.JSONArray
import org.json.JSONObject
import java.net.Socket
import java.net.URL

data class Player(val name: String, val photoUrl: URL, val socket: Socket? = null){
    fun toJson() : JSONObject {
        val json = JSONObject()
        json.put("name", name)
        json.put("photoUrl", photoUrl)
        return json
    }

    companion object {
        fun playersToJson(players: List<Player>) : JSONArray {
            val jsonArray = JSONArray()
            for (player in players) {
                jsonArray.put(player.toJson())
            }
            return jsonArray
        }

        fun fromJson(json: JSONObject) : Player {
            val name = json.getString("name")
            val photoUrl = json.getString("photoUrl")
            return Player(name, URL(photoUrl))
        }
    }
}
