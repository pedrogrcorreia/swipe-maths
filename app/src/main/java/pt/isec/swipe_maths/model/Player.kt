package pt.isec.swipe_maths.model

import android.net.Uri
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.json.JSONArray
import org.json.JSONObject
import java.net.Socket
import java.net.URL

data class Player(val name: String, val photoUrl: String, val socket: Socket? = null) {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("name", name)
        json.put("photoUrl", photoUrl)
        return json
    }

    companion object {
        fun playersToJson(players: List<Player>): JSONArray {
            val jsonArray = JSONArray()
            for (player in players) {
                jsonArray.put(player.toJson())
            }
            return jsonArray
        }

        fun fromJson(json: JSONObject): Player {
            val name = json.getString("name")
            val photoUrl = json.getString("photoUrl")
            return Player(name, photoUrl)
        }

        val mySelf: Player by lazy {
            Player(
                Firebase.auth.currentUser!!.displayName ?: "Pedro Correia",
                Firebase.auth.currentUser!!.photoUrl.toString()
                    ?: "https://firebasestorage.googleapis.com/v0/b/swipe-maths.appspot.com/o/file%2FIMG_20221212_132411.jpg?alt=media&token=83f07ff8-520f-4f26-8aa7-1b7b84120f57"
            )
        }
    }
}
