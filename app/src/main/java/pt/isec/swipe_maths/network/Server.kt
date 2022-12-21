package pt.isec.swipe_maths.network

import android.net.Uri
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONObject
import pt.isec.swipe_maths.ConnectionStates
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

    val state: MutableLiveData<ConnectionStates> = MutableLiveData()

    val clients: MutableList<Socket> = mutableListOf()

    val players: MutableLiveData<MutableList<Player>> = MutableLiveData(mutableListOf())

    val SERVER_PORT = 9999

    val game = GameManager.game

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

        state.value = ConnectionStates.SERVER_CONNECTING
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
                state.postValue(ConnectionStates.SERVER_CONNECTED)
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
                state.postValue(ConnectionStates.SERVER_ERROR)
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
                    val bufI = socketI!!.bufferedReader()
                    val message = bufI.readLine()
                    if (message != null) {
                        val json = JSONObject(message)
                        parseRequest(json, thisClient)
                    }
                    if(message == null){
                        break
                    }
                }
            } catch (e: Exception) {
                println("Client thread: " + e.printStackTrace())
            } finally {
                // TODO exception here?
                println("Closing client socket!")
                removeClient(thisClient)
                thisClient.close()
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
        clients.remove(clientSocket)
        val newPlayers = players.value!!
        val playerToRemove = newPlayers.find { it.socket == clientSocket }
        newPlayers.remove(playerToRemove)
        players.postValue(newPlayers)
    }

    private fun sendToClients(json: JSONObject) {
        thread {
            for (i in clients.indices) {
                socket = clients[i]
                socketO!!.run {
                    try {
                        val printStream = PrintStream(this)
                        printStream.println(json)
                        printStream.flush()
                    } catch (e: Exception) {
                        // TODO Exception here
                        println(e.message)
                    }
                }
            }
        }
    }

    fun sendToClient(json: JSONObject, clientSocket: Socket) {
        thread {
            socket = clientSocket
            socketO!!.run {
                try {
                    val printStream = PrintStream(this)
                    printStream.println(json)
                    printStream.flush()
                } catch (e: Exception) {
                    // TODO Exception here
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
        for (client in clients) {
            try {
                client.close()
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }

    private fun updateViews(json: JSONObject){
        json.apply {
            put("games", JSONArray().apply {
                for(game in GameManagerServer.games){
                    put(gson.toJson(game, Game::class.java))
                }
            })
        }
        println("Request: " + json.getString("request"))
//        println(GameManagerServer.games)
        sendToClients(json)
    }

    private fun parseRequest(json: JSONObject, socket: Socket) {
        when (json.getString("request")) {
            Requests.NEW_PLAYER.toString() -> {
                addPlayer(json, socket)
                broadcastPlayers()
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
        GameManagerServer.watchTimers()
        GameManager.game.startTime()
        for(game in GameManagerServer.games){
            game.startTime()
        }
        val json = JSONObject().apply {
            put("request", Requests.START_GAME)
        }
        updateViews(json)
    }

    fun updateTime(player: Player, game: Game){
        val json = JSONObject().apply {
            put("request", Requests.UPDATE_VIEWS)
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