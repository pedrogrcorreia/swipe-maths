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
import java.lang.NullPointerException
import java.net.*
import kotlin.concurrent.thread

object Client : Serializable {
    val state: MutableLiveData<ConnectionStates> = MutableLiveData(ConnectionStates.NO_CONNECTION)

    val players: MutableLiveData<MutableList<Player>> = MutableLiveData(mutableListOf())

    val SERVER_PORT = 9999

    private var socket: Socket? = null
    private val socketI: InputStream?
        get() = socket?.getInputStream()
    private val socketO: OutputStream?
        get() = socket?.getOutputStream()

    var isConnected: Boolean = false
        get() = socket != null


    fun startClient(serverIP: String, serverPort: Int = SERVER_PORT) {

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
                    println("Waiting for server requests...")
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
            } catch (e: NullPointerException) {
                // TODO Exception here meaning server was closing
                println("Thread: " + e)
                state.postValue(ConnectionStates.SERVER_ERROR)
            } catch (e: SocketException) {
                // TODO Exception here, client closed
                state.postValue(ConnectionStates.NO_CONNECTION)

                println("Thread " + e)
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
}