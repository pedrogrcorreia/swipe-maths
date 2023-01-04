package pt.isec.swipe_maths.utils

import android.content.res.Resources
import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import pt.isec.swipe_maths.R
import pt.isec.swipe_maths.model.Game
import com.google.firebase.Timestamp
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class FirestoreUtils {
    companion object {
        fun addGame(game: Game) {

            val db = Firebase.firestore
            val highscore = hashMapOf(
                "score" to game.pointsData,
                "time" to game.totalTime,
                "username" to game.player.name,
                "photoUrl" to game.player.photoUrl
            )

            db.collection("highscores-singleplayer").document().set(highscore)
        }

        fun addGames(games: List<Game>) {
            val db = Firebase.firestore

            val highscores: MutableList<HashMap<String, Any?>> = mutableListOf()

            var allPoints = 0
            var allTime = 0
            for (game in games) {
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
                "date" to FieldValue.serverTimestamp(),
                "time" to allTime,
                "score" to allPoints
            )

            db.collection("highscores-multiplayer").document().set(onlineGame)
        }

        suspend fun highscoresSinglePlayer(): List<SinglePlayerGame> {
            val db = Firebase.firestore
            val gamesList = mutableListOf<SinglePlayerGame>()
            val querySnapshot = db.collection("highscores-singleplayer")
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(5)
                .get().await()
            for (doc in querySnapshot) {
                gamesList.add(
                    SinglePlayerGame(
                        doc.get("username").toString(),
                        doc.get("score").toString().toInt(),
                        doc.get("time").toString().toInt(),
                        doc.get("photoUrl").toString()
                    )
                )
            }
            return gamesList
        }

        suspend fun highscoresSinglePlayerTimeOrder(): List<SinglePlayerGame> {
            val db = Firebase.firestore
            val gamesList = mutableListOf<SinglePlayerGame>()
            val querySnapshot = db.collection("highscores-singleplayer")
                .orderBy("time", Query.Direction.DESCENDING)
                .limit(5)
                .get().await()
            for (doc in querySnapshot) {
                gamesList.add(
                    SinglePlayerGame(
                        doc.get("username").toString(),
                        doc.get("score").toString().toInt(),
                        doc.get("time").toString().toInt(),
                        doc.get("photoUrl").toString()
                    )
                )
            }
            return gamesList
        }

        suspend fun highscoresMultiPlayer(): List<OnlineGame> {
            val db = Firebase.firestore

            val gamesList = mutableListOf<OnlineGame>()

            val querySnapshot = db.collection("highscores-multiplayer")
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(5)
                .get().await()

            for (doc in querySnapshot) {
                gamesList.add(
                    OnlineGame(
                        doc.get("date") as Timestamp,
                        doc.get("time").toString().toInt(),
                        doc.get("score").toString().toInt(),
                        doc.id
                    )
                )
            }

            return gamesList
        }

        suspend fun highscoresMultiPlayerTimeOrder(): List<OnlineGame> {
            val db = Firebase.firestore

            val gamesList = mutableListOf<OnlineGame>()

            val querySnapshot = db.collection("highscores-multiplayer")
                .orderBy("time", Query.Direction.DESCENDING)
                .limit(5)
                .get().await()

            for (doc in querySnapshot) {
                gamesList.add(
                    OnlineGame(
                        doc.get("date") as Timestamp,
                        doc.get("time").toString().toInt(),
                        doc.get("score").toString().toInt(),
                        doc.id
                    )
                )
            }

            return gamesList
        }

        suspend fun getMultiplayerGame(gameId: String): List<SinglePlayerGame> {
            val db = Firebase.firestore

            val playersList = mutableListOf<SinglePlayerGame>()

            val docRef = db.collection("highscores-multiplayer")
                .document(gameId)

            val doc = docRef.get().await()

            val players = doc.get("players") as ArrayList<Map<*, *>>

            for (player in players) {
                playersList.add(
                    SinglePlayerGame(
                        player["username"].toString(),
                        player["score"].toString().toInt(),
                        player["time"].toString().toInt(),
                        player["photoUrl"].toString()
                    )
                )
            }

            return playersList
        }
    }


}

data class OnlineGame(val gameDate: Timestamp?, val totalTime: Int, val totalScore: Int, val gameId: String?) : Parcelable {
    constructor(parcel: Parcel) : this(
        Timestamp(Date(parcel.readString())),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(gameDate.toString())
        parcel.writeInt(totalTime)
        parcel.writeInt(totalScore)
        parcel.writeString(gameId)
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

data class SinglePlayerGame(
    val username: String?,
    val score: Int,
    val time: Int,
    val photoUrl: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(username)
        parcel.writeInt(score)
        parcel.writeInt(time)
        parcel.writeString(photoUrl)
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