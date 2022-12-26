package pt.isec.swipe_maths.utils

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import pt.isec.swipe_maths.model.Game

class FirestoreUtils {
    companion object {
        fun addGame(game: Game){

            val db = Firebase.firestore
            val highscore = hashMapOf(
                "score" to game.pointsData,
                "time" to game.totalTime,
                "username" to game.player.name,
                "photoUrl" to game.player.photoUrl
            )

            db.collection("highscores-singleplayer").document().set(highscore)
        }

        fun addGames(games: List<Game>){
            val db = Firebase.firestore

            val highscores: MutableList<HashMap<String, Any?>> = mutableListOf()

            var allPoints = 0
            var allTime = 0
            for(game in games){
                val highscore = hashMapOf<String, Any?>(
                    "score" to game.pointsData,
                    "time" to game.totalTime,
                    "username" to game.player.name,
                    "photoUrl" to game.player.photoUrl
                )
                highscores.add(highscore)
                allPoints += game.pointsData
                allTime += game.totalTime
            }

            val onlineGame = hashMapOf(
                "players" to highscores,
                "time" to allTime,
                "score" to allPoints
            )

            db.collection("highscores-multiplayer").document().set(onlineGame)
        }

        suspend fun highscoresMultiPlayer() : List<SinglePlayerGame>{
            val db = Firebase.firestore

            val gamesList = mutableListOf<SinglePlayerGame>()

            val querySnapshot = db.collection("highscores-multiplayer")
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(5)
                .get().await()

            for(doc in querySnapshot){
                gamesList.add(
                    SinglePlayerGame(
                        "Multiplayer Game",
                        doc.get("time").toString().toInt(),
                        doc.get("score").toString().toInt()
                    )
                )
            }

            return gamesList
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

data class OnlineGame(val totalTime: Int, val totalScore: Int) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(totalTime)
        parcel.writeInt(totalScore)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<OnlineGame> {
        override fun createFromParcel(parcel: Parcel): OnlineGame {
            return OnlineGame(parcel)
        }

        override fun newArray(size: Int): Array<OnlineGame?> {
            return arrayOfNulls(size)
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