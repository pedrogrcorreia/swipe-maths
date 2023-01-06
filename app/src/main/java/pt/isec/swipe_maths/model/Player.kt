package pt.isec.swipe_maths.model

import android.net.Uri
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.json.JSONArray
import org.json.JSONObject
import java.net.Socket
import java.net.URL

data class Player(val name: String, val photoUrl: String, val uid: String, val socket: Socket? = null) {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("name", name)
        json.put("photoUrl", photoUrl)
        json.put("uid", uid)
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
            val uid = json.getString("uid")
            return Player(name, photoUrl, uid)
        }

        val mySelf: Player by lazy {
            Player(
                Firebase.auth.currentUser!!.displayName!!,
                Firebase.auth.currentUser!!.photoUrl.toString(),
                Firebase.auth.currentUser!!.uid,
            )
        }
    }
}
