package pt.isec.swipe_maths.utils

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.json.JSONObject
import pt.isec.swipe_maths.ConnectionStates
import pt.isec.swipe_maths.model.Player
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream
import java.io.Serializable
import java.net.*
import kotlin.concurrent.thread

class Server : Serializable {

    val state: MutableLiveData<ConnectionStates> = MutableLiveData()

    val clients: MutableList<Socket> = mutableListOf()

    val players: MutableLiveData<MutableList<Player>> = MutableLiveData(mutableListOf())

    val SERVER_PORT = 9999

    private var socket: Socket? = null
    private val socketI: InputStream?
        get() = socket?.getInputStream()
    private val socketO: OutputStream?
        get() = socket?.getOutputStream()

    private var serverSocket: ServerSocket? = null

    private var multicastThread: Thread? = null

    private var multiSocket: MulticastSocket = MulticastSocket(9996)

    private var running = true

    fun startServer(strIpAddress: String) {
        if (serverSocket != null || socket != null)
            return

        state.value = ConnectionStates.SERVER_CONNECTING
        val newPlayers = players.value!!
        newPlayers.add(
            Player(
                Firebase.auth.currentUser?.displayName!!,
                Firebase.auth.currentUser?.photoUrl!!
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
                        println("HERE!!!")
                        try {
                            val socketClient = serverSocket!!.accept()
                            clients.add(socketClient)
                            clientThread(socketClient)
                            println("Connection received")
                        } catch (e: Exception) {
                            // TODO deal with this
                            println(e.message)
                            break;
                        }
                    }
                }
            } catch (_: Exception) {
                state.postValue(ConnectionStates.SERVER_ERROR)
            }
        }
    }

    private fun startMulticast(strIpAddress: String) {
        thread {
            //multiSocket = MulticastSocket(9996)
            val group = InetAddress.getByName("224.0.0.251")
            multiSocket.joinGroup(group)
            multiSocket.broadcast = true
            val buffer = ByteArray(2048)
            while (running) {
                try {
                    println("waiting for packet!")
                    val packet = DatagramPacket(buffer, buffer.size)
                    multiSocket.receive(packet)
                    val strMsg = "$strIpAddress ${serverSocket?.localPort}"
                    packet.data = strMsg.toByteArray()
                    multiSocket.send(packet)

                } catch (e: Exception) {
                    println("Exception!!!")
                    break;
                }
            }
            println("closing multicast")
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


                socketO?.run {
                    thread {
                        try {
                            val printStream = PrintStream(this)
                            val json = JSONObject()
                            json.put("state", ConnectionStates.CONNECTION_ESTABLISHED)
                            printStream.println(json)
                            printStream.flush()
                        } catch (e: Exception) {
                            println("${e.message}")
                        }
                    }
                }

                while (true) {
                    val bufI = socketI!!.bufferedReader()
                    val message = bufI.readLine()
                    val json = JSONObject(message)
                    val rState = json.getString("state")
                    if (rState == ConnectionStates.CONNECTION_ENDED.toString()) {
                        val json = JSONObject()
                        json.put("state", ConnectionStates.CONNECTION_ENDED)
                        sendToClient(json.toString(), thisClient)
                        removeClient(thisClient)
                        break
                    } else if (rState == ConnectionStates.RETRIEVING_CLIENT_INFO.toString()) {
                        addPlayer(json, thisClient)
                    }
                }
            } catch (e: Exception) {
                println("${e.message}")
            } finally {
                // TODO exception here?
                println("closing client socket!")
                thisClient.close()
            }
        }
    }

    fun addPlayer(json: JSONObject, socket: Socket) {
        try {
            val name = json.getString("name")
            val photo = Uri.parse(json.getString("photo"))

            val newPlayers = players.value!!
            newPlayers.add(Player(name, photo, socket))
            players.postValue(newPlayers)
        } catch (_: Exception) {
            // TODO Send message to client
        }
    }

    fun removeClient(clientSocket: Socket) {
        clients.remove(clientSocket)
        val newPlayers = players.value!!
        val playerToRemove = newPlayers.find { it.socket == clientSocket }
        newPlayers.remove(playerToRemove)
        players.postValue(newPlayers)
    }

    fun sendToClients(json: JSONObject) {
        thread {
            for (i in clients.indices) {
                socket = clients[i]
                socketO!!.run {
                    try {
                        val printStream = PrintStream(this)
                        printStream.println(json)
                        printStream.flush()
                    } catch (_: Exception) {
                        // TODO Exception here
                    }
                }
            }
        }
    }

    fun sendToClient(json: String, clientSocket: Socket) {
        thread {
            socket = clientSocket
            socketO!!.run {
                try {
                    val printStream = PrintStream(this)
                    printStream.println(json)
                    printStream.flush()
                } catch (e: Exception) {
                    // TODO Exception here
                    println("${e.message}")
                }
            }
        }
    }

    fun closeServer() {
        multiSocket.close()
        serverSocket?.close()
        for (client in clients) {
            try {
                client.close()
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }
}