package pt.isec.swipe_maths.network

import androidx.lifecycle.LiveData
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
    private val _state: MutableLiveData<ConnectionStates> = MutableLiveData(ConnectionStates.NO_CONNECTION)

    val state : LiveData<ConnectionStates>
        get() = _state

    val players: MutableLiveData<MutableList<Player>> = MutableLiveData(mutableListOf())

    private val _requestState: MutableLiveData<Requests> = MutableLiveData(Requests.NONE)

    val requestState : LiveData<Requests>
        get() = _requestState

    private val _onlineState: MutableLiveData<OnlineGameStates> = MutableLiveData()

    val onlineState: LiveData<OnlineGameStates>
        get() = _onlineState

    private const val SERVER_PORT = 9999
    const val SERVER_PORT_EMULATOR = 9998
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

        _state.postValue(ConnectionStates.CLIENT_CONNECTING)

        thread {
            try {
                println("Contacting server...")
                val serverSocket = Socket()
                serverSocket.connect(InetSocketAddress(serverIP, serverPort), 5000)
                serverThread(serverSocket)
                _state.postValue(ConnectionStates.CONNECTION_ESTABLISHED)
            } catch (e: Exception) {
                println("Start Client Thread: " + e.message)
                _state.postValue(ConnectionStates.CONNECTION_ERROR)
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
                    if(message != null && message.isNotEmpty()) {
                        val json = JSONObject(message)
                        parseRequest(json)
                    }
//                    if(message == null){
//                        _state.postValue(ConnectionStates.SERVER_ERROR)
//                        break
//                    }
                }
            } catch (e: NullPointerException) {
                // TODO Exception here meaning server was closing
                println("Thread: " + e.message)
                _state.postValue(ConnectionStates.SERVER_ERROR)
            } catch (e: SocketException) {
                // TODO Exception here, client closed
                _state.postValue(ConnectionStates.SERVER_ERROR)
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
            _state.postValue(ConnectionStates.NO_CONNECTION)
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
                    _state.postValue(ConnectionStates.SERVER_ERROR)
                }
            }
        }
    }

    private fun parseRequest(json: JSONObject) {
        println(json.getString("request"))
        when (json.getString("request")) {
            Requests.UPDATE_PLAYERS_LIST.toString() ->
                updatePlayersList(json)
            Requests.START_GAME.toString() -> {
                updateViews(json)
                _requestState.postValue(Requests.START_GAME)
                _onlineState.postValue(OnlineGameStates.START_GAME)
            }
            Requests.NEW_LEVEL_STARTING.toString() -> {
                _onlineState.postValue(OnlineGameStates.ALL_FINISHED_LEVEL)
            }
            Requests.UPDATE_VIEWS.toString(), Requests.NEW_LEVEL.toString() -> {
                updateViews(json)
                _requestState.postValue(Requests.UPDATE_VIEWS)
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
        }
        sendToServer(json)
    }

    fun colPlay(selectedCol: Int){
        val json = JSONObject().apply{
            put("request", Requests.COL_PLAY)
            put("colNumber", selectedCol)
            put("player", Player.mySelf.toJson())
        }
        sendToServer(json)
    }

    private fun updatePlayersList(json: JSONObject) {
        val games = json.getJSONArray("games")
        val newPlayers : MutableList<Player> = players.value!!
        newPlayers.clear()
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
            println(game)
            GameManagerClient.updatePlayer(game)
        }
    }
}