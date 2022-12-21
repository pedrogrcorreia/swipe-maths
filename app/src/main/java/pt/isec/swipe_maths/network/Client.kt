package pt.isec.swipe_maths.network

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONObject
import pt.isec.swipe_maths.ConnectionStates
import pt.isec.swipe_maths.model.Game
import pt.isec.swipe_maths.model.GameManager
import pt.isec.swipe_maths.model.GameManagerClient
import pt.isec.swipe_maths.model.Player
import pt.isec.swipe_maths.views.GameViewModel
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream
import java.io.Serializable
import java.lang.NullPointerException
import java.net.*
import kotlin.concurrent.thread

object Client : Serializable {
    val state: MutableLiveData<ConnectionStates> = MutableLiveData(ConnectionStates.NO_CONNECTION)

    val players: MutableLiveData<MutableList<Player>> = MutableLiveData(mutableListOf())

    val requestState: MutableLiveData<Requests> = MutableLiveData(Requests.NONE)

    val SERVER_PORT = 9999
    val SERVER_PORT_EMULATOR = 9998
    val IP_ADDRESS = "10.0.2.2"

    private var socket: Socket? = null
    private val socketI: InputStream?
        get() = socket?.getInputStream()
    private val socketO: OutputStream?
        get() = socket?.getOutputStream()

    var isConnected: Boolean = false
        get() = socket != null

    var game : Game = GameManager.game

    val gson = GsonBuilder()
//            .registerTypeAdapter(Game::class.java, GameManager.game)
        .create()

    fun startClient(serverIP: String = IP_ADDRESS, serverPort: Int = SERVER_PORT) {

        if (socket != null) {
            return
        }

        players.value!!.clear()

        state.postValue(ConnectionStates.CLIENT_CONNECTING)

        thread {
            try {
                println("Contacting server...")
                val serverSocket = Socket()
                serverSocket.connect(InetSocketAddress(serverIP, serverPort), 5000)
                serverThread(serverSocket)
                state.postValue(ConnectionStates.CONNECTION_ESTABLISHED)
            } catch (e: Exception) {
                println("Start Client Thread: " + e.message)
                state.postValue(ConnectionStates.CONNECTION_ERROR)
            }
        }
    }

    private fun serverThread(serverSocket: Socket) {
        socket = serverSocket

        println("Successfully connected to server!")

        thread {
            try {
                if (socketI == null) {
                    return@thread
                }

                val bufI = socketI!!.bufferedReader()
                while (state.value != ConnectionStates.CONNECTION_ENDED) {
                    val message = bufI.readLine()
                    if(message != null) {
                        val json = JSONObject(message)
                        parseRequest(json)
                    }
                    if(message == null){
                        break
                    }
                }
            } catch (e: NullPointerException) {
                // TODO Exception here meaning server was closing
                println("Thread: " + e.message)
                state.postValue(ConnectionStates.SERVER_ERROR)
            } catch (e: SocketException) {
                // TODO Exception here, client closed
                state.postValue(ConnectionStates.NO_CONNECTION)

                println("Thread " + e.message)
            } finally {
                println("Closing from server socket!")
                closeClient()
            }
        }
    }

    fun contactMulticast(): String? {
        if (socket != null) {
            return null
        }
        val buffer = ByteArray(2056)
        val messageStr = "Connect"
        val socket = DatagramSocket()
        socket.broadcast = true

        val sendRequest = messageStr.toByteArray()

        val packet =
            DatagramPacket(
                sendRequest, sendRequest.size,
                InetAddress.getByName("224.0.0.251"),
                9996
            )
        socket.soTimeout = 5000

        return try {
            socket.send(packet)
            packet.data = buffer
            packet.length = buffer.size
            socket.receive(packet)
            val received = ByteArray(packet.length)
            packet.data.copyInto(received, 0, 0, packet.length)
            val str = String(received)
            str
        } catch (e: SocketTimeoutException) {
            e.message!!
            state.postValue(ConnectionStates.NO_CONNECTION)
            return null
        }
    }

    fun closeClient() {
        socket?.close()
        players.value!!.clear()
        socket = null
    }

    fun sendToServer(json: JSONObject) {
        thread {
            socketO!!.run {
                try {
                    val printStream = PrintStream(this)
                    printStream.println(json)
                    printStream.flush()
                } catch (_: Exception) {
                    // TODO this exception
                }
            }
        }
    }

    private fun parseRequest(json: JSONObject) {
        when (json.getString("request")) {
            Requests.UPDATE_PLAYERS_LIST.toString() ->
                updatePlayersList(json)
            Requests.START_GAME.toString() -> {
                updateViews(json)
//                GameManager.game = gson.fromJson(json.getString("game"), Game::class.java).apply{
//                    board.postValue(boardData)
//                    gameState.postValue(gameStateData)
//                    level.postValue(levelData)
//                    remainingTime.postValue(remainingTimeData)
//                    nextLevelProgress.postValue(nextLevelProgressData)
//                    points.postValue(pointsData)
//                }
                requestState.postValue(Requests.START_GAME)
                state.postValue(ConnectionStates.START_GAME)
            }
            Requests.UPDATE_VIEWS.toString() -> {
                updateViews(json)
                requestState.postValue(Requests.UPDATE_VIEWS)
            }
            Requests.ROW_PLAYED.toString() -> {
                GameManager.game = gson.fromJson(json.getString("game"), Game::class.java).apply{
                    board.postValue(boardData)
                    gameState.postValue(gameStateData)
                    level.postValue(levelData)
                    remainingTime.postValue(remainingTimeData)
                    nextLevelProgress.postValue(nextLevelProgressData)
                    points.postValue(pointsData)
                    correctAnswers.postValue(correctAnswersData)
                }
                println("Game received on row played: " + GameManager.game)
                requestState.postValue(Requests.ROW_PLAYED)
            }
            Requests.COL_PLAYED.toString() -> {
                GameManager.game = gson.fromJson(json.getString("game"), Game::class.java).apply{
                    board.postValue(boardData)
                    gameState.postValue(gameStateData)
                    level.postValue(levelData)
                    remainingTime.postValue(remainingTimeData)
                    nextLevelProgress.postValue(nextLevelProgressData)
                    points.postValue(pointsData)
                    correctAnswers.postValue(correctAnswersData)
                }
                requestState.postValue(Requests.COL_PLAYED)
            }
            Requests.UPDATE_TIMER.toString() -> {
                val timeLeft = json.getInt("time")
                GameManager.game.apply {
                    remainingTimeData = timeLeft
                }
                requestState.postValue(Requests.UPDATE_TIMER)
            }
        }
    }

    fun newPlayer(player: Player) {
        val json = JSONObject().apply {
            put("request", Requests.NEW_PLAYER)
            put("player", player.toJson())
        }
        sendToServer(json)
    }

    fun rowPlay(selectedRow: Int){
        val json = JSONObject().apply{
            put("request", Requests.ROW_PLAY)
            put("rowNumber", selectedRow)
            put("player", Player.mySelf.toJson())
            //put("game", gson.toJson(GameManager.game, Game::class.java))
        }
        sendToServer(json)
    }

    fun colPlay(selectedCol: Int){
        val json = JSONObject().apply{
            put("request", Requests.COL_PLAY)
            put("colNumber", selectedCol)
            put("player", Player.mySelf.toJson())
            //put("game", gson.toJson(GameManager.game, Game::class.java))
        }
        sendToServer(json)
    }

    private fun updatePlayersList(json: JSONObject) {
        val games = json.getJSONArray("games")
        val newPlayers : MutableList<Player> = players.value!!
        for(i in 0 until games.length()){
            val game = gson.fromJson(games.get(i).toString(), Game::class.java)
            GameManagerClient.newPlayer(game)
            newPlayers.add(game.player)
        }
        players.postValue(newPlayers)
    }

    private fun updateViews(json: JSONObject){
        val games = json.getJSONArray("games")
        for(i in 0 until games.length()){
            val game = gson.fromJson(games.get(i).toString(), Game::class.java)
            GameManagerClient.updatePlayer(game)
            println("Game $i $game")
        }
        println("GameManagerClient ${GameManager.game}")
    }
}