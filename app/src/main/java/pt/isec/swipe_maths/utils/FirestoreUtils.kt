package pt.isec.swipe_maths.utils

import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

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

        suspend fun highscoresSinglePlayer() : List<SinglePlayerGame>{
            val db = Firebase.firestore
            val gamesList = mutableListOf<SinglePlayerGame>()
            val querySnapshot = db.collection("highscores-singleplayer")
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(5)
                .get().await()
//                .addOnSuccessListener { docs ->
//
//                for(doc in docs){
//                    gamesList.add(SinglePlayerGame(doc.get("username").toString(), doc.get("score").toString().toInt(), doc.get("time").toString().toInt()))
//                }
//            }
            for(doc in querySnapshot) {
                gamesList.add(
                    SinglePlayerGame(
                        doc.get("username").toString(),
                        doc.get("score").toString().toInt(),
                        doc.get("time").toString().toInt()
                    )
                )
            }
            return gamesList
        }
    }
}

data class SinglePlayerGame(val username: String, val score: Int, val time: Int)