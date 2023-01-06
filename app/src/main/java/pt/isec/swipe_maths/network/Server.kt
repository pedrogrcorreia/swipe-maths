package pt.isec.swipe_maths.network

import android.net.Uri
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONObject
import pt.isec.swipe_maths.ConnectionStates
import pt.isec.swipe_maths.GameStates
import pt.isec.swipe_maths.model.Game
import pt.isec.swipe_maths.model.GameManager
import pt.isec.swipe_maths.model.GameManagerServer
import pt.isec.swipe_maths.model.Player
import pt.isec.swipe_maths.model.board.Board
import pt.isec.swipe_maths.views.GameViewModel
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream
import java.net.*
import kotlin.concurrent.thread

object Server {

    private val _state: MutableLiveData<ConnectionStates> = MutableLiveData()

    val state: LiveData<ConnectionStates>
        get() = _state

    private val _onlineState: MutableLiveData<OnlineGameStates> = MutableLiveData()

    val onlineState: LiveData<OnlineGameStates>
        get() = _onlineState

    val clients: MutableList<Socket> = mutableListOf()

    val players: MutableLiveData<MutableList<Player>> = MutableLiveData(mutableListOf())

    val SERVER_PORT = 9999

    val gson = GsonBuilder()
//            .registerTypeAdapter(Game::class.java, GameSerializer())
        .create()

    private var socket: Socket? = null
    private val socketI: InputStream?
        get() = socket?.getInputStream()
    private val socketO: OutputStream?
        get() = socket?.getOutputStream()

    private var serverSocket: ServerSocket? = null

    private var multiSocket: MulticastSocket = MulticastSocket(9996)

    private var running = true

    fun startServer(strIpAddress: String) {
        if (serverSocket != null || socket != null)
            return

        println("Server is starting...")

        println(GameManagerServer.games)

        GameManagerServer.newBoard(GameManager.game.boardData)

        players.value!!.clear()

        _state.value = ConnectionStates.SERVER_CONNECTING
        val newPlayers = players.value!!
        newPlayers.add(
            Player(
                Firebase.auth.currentUser?.displayName!!,
                Firebase.auth.currentUser?.photoUrl!!.toString()
            )
        )
        players.postValue(newPlayers)

        startMulticast(strIpAddress)

        thread {
            try {
                serverSocket = ServerSocket(SERVER_PORT)
                _state.postValue(ConnectionStates.SERVER_CONNECTED)
                serverSocket.run {
                    while (true) {
                        println("Waiting for clients...")
                        try {
                            val socketClient = serverSocket!!.accept()
                            clients.add(socketClient)
                            clientThread(socketClient)
                            println("Connection received!")
                        } catch (e: Exception) {
                            println("ServerSocket: " + e.message)
                            break
                        }
                    }
                }
            } catch (e: Exception) {
                println("Thread: " + e.message)
                _state.postValue(ConnectionStates.SERVER_ERROR)
            }
        }
    }

    private fun startMulticast(strIpAddress: String) {
        thread {
            println("Multicast Server is starting...")
            val group = InetAddress.getByName("224.0.0.251")
            multiSocket.joinGroup(group)
            multiSocket.broadcast = true
            val buffer = ByteArray(2048)
            while (running) {
                try {
                    println("Waiting for packets...")
                    val packet = DatagramPacket(buffer, buffer.size)
                    multiSocket.receive(packet)
                    val strMsg = "$strIpAddress ${serverSocket?.localPort}"
                    packet.data = strMsg.toByteArray()
                    multiSocket.send(packet)

                } catch (e: Exception) {
                    println("MulticastSocket: " + e.message)
                    break
                }
            }
            println("Closing multicast thread!")
        }
    }

    private fun clientThread(clientSocket: Socket) {
        val thisClient = clientSocket
        socket = clientSocket

        thread {
            try {
                if (socketI == null) {
                    return@thread
                }

                while (true) {
                    println("Waiting for messages from client...")
                    val bufI = thisClient.getInputStream()!!.bufferedReader()
                        val message = bufI.readLine()
                        if (message != null && message.isNotEmpty()) {
                            val json = JSONObject(message)
                            parseRequest(json, thisClient)
                        }
                    if(message == null){
                        break
                    }
                }
            } catch (e: Exception) {
                println("Client thread: " + e.message)
            } finally {
                println("Closing client socket!")
                removeClient(thisClient)
//                thisClient.close()
            }
        }
    }

    private fun addPlayer(json: JSONObject, socket: Socket) {
        try {
            val playerJSON = json.getJSONObject("player")
            println(playerJSON)
            val name = playerJSON.getString("name")
            val photo = playerJSON.getString("photoUrl")
            val newPlayers = players.value!!
            newPlayers.add(Player(name, photo, socket))
            GameManagerServer.addNewPlayer(Player(name, photo))
            players.postValue(newPlayers)
        } catch (e: Exception) {
            println(e.message)
        }
    }

    private fun removeClient(clientSocket: Socket) {
        if(onlineState.value == OnlineGameStates.PLAYING){
            _state.postValue(ConnectionStates.CONNECTION_ERROR)
            closeServer()
            return
        }
        clients.remove(clientSocket)
        val newPlayers = players.value!!
        val playerToRemove = newPlayers.find { it.socket == clientSocket }
        newPlayers.remove(playerToRemove)
        GameManagerServer.removePlayer(playerToRemove?.name)
        players.postValue(newPlayers)
    }

    private fun sendToClients(json: JSONObject) {
        thread {
            for (i in clients.indices) {
                socket = clients[i]

                    try {
                        socketO!!.run {
                            val printStream = PrintStream(this)
                            printStream.println(json)
                            printStream.flush()
                        }
                    } catch (e: Exception) {
                        closeServer()
                        println(e.message)
                    }
            }
        }
    }

    fun closeServer() {
        multiSocket.close()
        serverSocket?.close()
        multiSocket = MulticastSocket(9996)
        serverSocket = null
        socket = null
        for (client in clients) {
            try {
                client.close()
            } catch (e: Exception) {
                println("Exception closing client: ${e.message}")
            }
        }
        clients.clear()
//        GameManager.newGame()
    }

    fun updateViews(json: JSONObject){
        try {
            json.apply {
                put("games", JSONArray().apply {
                    for (game in GameManagerServer.games) {
                        put(gson.toJson(game, Game::class.java))
                    }
                })
            }
            sendToClients(json)
        } catch(e: Exception){
            print("${e.message}")
        }
    }

    private fun parseRequest(json: JSONObject, socket: Socket) {
        when (json.getString("request")) {
            Requests.NEW_PLAYER.toString() -> {
                addPlayer(json, socket)
                broadcastPlayers()
            }
            Requests.ROW_PLAY.toString() -> {
                val selectedRow = json.getInt("rowNumber")
                val player = Player.fromJson(json.getJSONObject("player"))
                GameManagerServer.rowPlay(selectedRow, player)
            }
            Requests.COL_PLAY.toString() -> {
                val selectedCol = json.getInt("colNumber")
                val player = Player.fromJson(json.getJSONObject("player"))
                GameManagerServer.colPlay(selectedCol, player)
            }
        }
    }

    private fun broadcastPlayers() {
        val json = JSONObject().apply {
            put("request", Requests.UPDATE_PLAYERS_LIST)
        }
        updateViews(json)
    }

    fun startGame() {
        _onlineState.postValue(OnlineGameStates.PLAYING)
        val json = JSONObject().apply {
            put("request", Requests.START_GAME)
        }
        updateViews(json)

        GameManagerServer.watchTimers()

//        GameManager.game.startTime()
        for(game in GameManagerServer.games){
            game.startTime()
        }
    }

    fun startNewLevelTimers(){
        val json = JSONObject().apply {
            put("request", Requests.NEW_LEVEL_STARTING)
        }
        updateViews(json)
    }

    fun startNewLevel(){
        GameManagerServer.boardsList.run {
            clear()
            add(Board(GameManagerServer.currentLevel.nextLevel))
        }
        for(game in GameManagerServer.games){
            if(game.gameStateData != GameStates.GAME_OVER) {
                game.newLevel()
                game.apply {
                    plays = 0
                }
            }
        }
        GameManagerServer.resetNewLevelBoards()

        val json = JSONObject().apply{
            put("request", Requests.NEW_LEVEL)
        }
        updateViews(json)
    }

    fun updateTime(){
        val json = JSONObject().apply {
            put("request", Requests.UPDATE_VIEWS)
        }
        updateViews(json)
    }

    fun levelFinished(){
        _onlineState.postValue(OnlineGameStates.ALL_FINISHED_LEVEL)
        val json = JSONObject().apply {
            put("request", Requests.UPDATE_VIEWS)
        }
        updateViews(json)
    }

    fun gameOver(){
        _onlineState.postValue(OnlineGameStates.ALL_GAME_OVER)
        val json = JSONObject().apply {
            put("request", Requests.GAME_OVER
            )
        }
        updateViews(json)
    }

    // GAME FUNCTIONS

    fun rowPlay(row: Int){
        GameManagerServer.rowPlayServer(row, Player.mySelf)
    }

    fun colPlay(col: Int){
        GameManagerServer.colPlayServer(col, Player.mySelf)
    }
}