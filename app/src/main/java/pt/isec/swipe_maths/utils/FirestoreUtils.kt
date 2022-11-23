package pt.isec.swipe_maths.utils

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FirestoreUtils {
    companion object {
        fun addGame(score: Int, time: Int, username: String){
            val db = Firebase.firestore

            val game = hashMapOf(
                "score" to score,
                "time" to time,
                "username" to username
            )

            db.collection("highscores-singleplayer").document().set(game)
        }
    }
}