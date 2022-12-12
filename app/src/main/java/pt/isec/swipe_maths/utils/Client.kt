package pt.isec.swipe_maths.utils

import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import org.json.JSONObject
import pt.isec.swipe_maths.ConnectionStates
import pt.isec.swipe_maths.model.Player
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream
import java.io.Serializable
import java.net.*
import kotlin.concurrent.thread

class Client : Serializable {
    val state: MutableLiveData<ConnectionStates> = MutableLiveData(ConnectionStates.NO_CONNECTION)

    val players: MutableLiveData<MutableList<Player>> = MutableLiveData(mutableListOf())

    val SERVER_PORT = 9999

    private var socket: Socket? = null
    private val socketI: InputStream?
        get() = socket?.getInputStream()
    private val socketO: OutputStream?
        get() = socket?.getOutputStream()

    var isConnected : Boolean = false
        get() = socket != null


    fun startClient(serverIP: String, serverPort: Int = SERVER_PORT) {

        if (socket != null) {
            return
        }

        state.postValue(ConnectionStates.CLIENT_CONNECTING)

        thread {
            try {
                val serverSocket = Socket()
                serverSocket.connect(InetSocketAddress(serverIP, serverPort), 5000)
                serverThread(serverSocket)
            } catch (_: Exception) {
                state.postValue(ConnectionStates.CONNECTION_ERROR)
            }
        }
    }

    private fun serverThread(serverSocket: Socket) {
        socket = serverSocket

        println(state.value)

        thread {
            try {
                if (socketI == null) {
                    return@thread
                }

                val bufI = socketI!!.bufferedReader()
                while (state.value != ConnectionStates.CONNECTION_ENDED) {
                    val message = bufI.readLine()
                    val json = JSONObject(message)
                    val rState = json.getString("state")
                    println(json)
                    if (rState == ConnectionStates.UPDATE_PLAYERS_LIST.toString()) {
                        val rPlayers = json.getJSONArray("players")
                        val newPlayers = players.value!!
                        newPlayers.clear()
                        for (i in 0 until rPlayers.length()) {
                            val player = rPlayers.getJSONObject(i)
                            newPlayers.add(Player.fromJson(player))
                        }
                        players.postValue(newPlayers)
                    }
                    state.postValue(ConnectionStates.valueOf(rState))
                }
            } catch (e: Exception) {
                // TODO Exception here meaning server was closing
                println("${e.message}")
            } finally {
                println("Closing from server socket!")
                state.postValue(ConnectionStates.SERVER_ERROR)
                socket?.close()
                socket = null
            }
        }
    }

    fun contactMulticast(): String? {
        println(state.value)
        println(socket)
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

        try {
            socket.send(packet)
            packet.data = buffer
            packet.length = buffer.size
            socket.receive(packet)
            val received = ByteArray(packet.length)
            packet.data.copyInto(received, 0, 0, packet.length)
            val str = String(received)
            return str
        } catch (e: SocketTimeoutException) {
            state.postValue(ConnectionStates.CONNECTION_ERROR)
            return e.message!!
        }
    }

    fun closeClient() {
        val json = JSONObject()
        json.put("state", ConnectionStates.CONNECTION_ENDED)
        sendToServer(json)
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
}