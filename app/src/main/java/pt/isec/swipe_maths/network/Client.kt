package pt.isec.swipe_maths.network

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONObject
import pt.isec.swipe_maths.ConnectionStates
import pt.isec.swipe_maths.model.Game
import pt.isec.swipe_maths.model.GameManager
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

    var game : Game? = null

    val gson = GsonBuilder()
            .registerTypeAdapter(Game::class.java, Game())
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
                    val json = JSONObject(message)
                    parseRequest(json)
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
                updatePlayersList(json.getJSONArray("players"))
            Requests.START_GAME.toString(), Requests.ROW_PLAY.toString() -> {
                GameManager.game = gson.fromJson(json.getString("game"), Game::class.java).apply{
                    board.postValue(boardData)
                    gameState.postValue(gameStateData)
                    level.postValue(levelData)
                    remainingTime.postValue(remainingTimeData)
                    nextLevelProgress.postValue(nextLevelProgressData)
                    points.postValue(pointsData)
                }
                state.postValue(ConnectionStates.START_GAME)
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
            put("game", gson.toJson(GameManager.game, Game::class.java))
        }
        sendToServer(json)
    }

    private fun updatePlayersList(jsonArray: JSONArray) {
        try {
            val newPlayers = players.value!!
            for (i in 0 until jsonArray.length()) {
                newPlayers.add(Player.fromJson(jsonArray.getJSONObject(i)))
            }
            players.postValue(newPlayers)
        } catch (e: Exception) {
            println(e.message)
        }
    }
}