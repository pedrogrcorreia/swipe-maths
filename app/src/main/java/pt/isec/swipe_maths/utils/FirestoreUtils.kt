package pt.isec.swipe_maths.utils

import android.os.Parcel
import android.os.Parcelable
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

data class SinglePlayerGame(val username: String?, val score: Int, val time: Int) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(username)
        parcel.writeInt(score)
        parcel.writeInt(time)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SinglePlayerGame> {
        override fun createFromParcel(parcel: Parcel): SinglePlayerGame {
            return SinglePlayerGame(parcel)
        }

        override fun newArray(size: Int): Array<SinglePlayerGame?> {
            return arrayOfNulls(size)
        }
    }
}