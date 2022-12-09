package pt.isec.swipe_maths.utils

import androidx.lifecycle.MutableLiveData
import org.json.JSONObject
import pt.isec.swipe_maths.ConnectionStates
import pt.isec.swipe_maths.model.Player
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream
import java.net.*
import kotlin.concurrent.thread

class Server {
    companion object{
        val state : MutableLiveData<ConnectionStates> = MutableLiveData()

        val clients : MutableList<Socket> = mutableListOf()

        val players : MutableLiveData<MutableList<Player>> = MutableLiveData(mutableListOf())

        const val SERVER_PORT = 9999

        private var socket: Socket? = null
        private val socketI: InputStream?
            get() = socket?.getInputStream()
        private val socketO: OutputStream?
            get() = socket?.getOutputStream()

        private var serverSocket: ServerSocket? = null

        fun startServer(strIpAddress: String){
            if (serverSocket != null || socket != null)
                return

            state.value = ConnectionStates.SERVER_CONNECTING

            startMulticast(strIpAddress)

            thread {
                try {
                    serverSocket = ServerSocket(SERVER_PORT)
                    state.postValue(ConnectionStates.SERVER_CONNECTED)
                    serverSocket.run {
                        while(true){
                            println("HERE!!!")
                            try {
                                val socketClient = serverSocket!!.accept()
                                clients.add(socketClient)
                                clientThread(socketClient)
                                println("Connection received")
                            } catch(_: Exception){
                                // TODO deal with this
                            }
                        }
                    }
                } catch(_: Exception){
                    state.postValue(ConnectionStates.SERVER_ERROR)
                }
            }
        }

        private fun startMulticast(strIpAddress: String){
            thread {
                val multiSocket = MulticastSocket(9996)
                val group = InetAddress.getByName("224.0.0.251")
                multiSocket.joinGroup(group)
                multiSocket.broadcast = true
                val buffer = ByteArray(2048)
                while(true) {
                    try {
                        val packet = DatagramPacket(buffer, buffer.size)
                        multiSocket.receive(packet)
                        val strMsg = "$strIpAddress ${serverSocket?.localPort}"
                        packet.data = strMsg.toByteArray()
                        multiSocket.send(packet)
                    } catch (_: Exception) {
                        println("Exception!!!")
                    }
                }
            }
        }

        private fun clientThread(clientSocket: Socket){
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
                }catch (e: Exception){
                    println("${e.message}")
                }finally {
                    // TODO exception here?
                    println("closing client socket!")
                    thisClient.close()
                }
            }
        }

        fun addPlayer(json: JSONObject, socket: Socket){
            try {
                val name = json.getString("name")
                val photo = URL(json.getString("photo")) // TODO load default image

                val newPlayers = players.value!!
                newPlayers.add(Player(name, photo, socket))
                players.postValue(newPlayers)
            } catch(_: Exception){
                // TODO Send message to client
            }
        }

        fun removeClient(clientSocket: Socket){
            clients.remove(clientSocket)
            val newPlayers = players.value!!
            val playerToRemove = newPlayers.find {it.socket == clientSocket}
            newPlayers.remove(playerToRemove)
            players.postValue(newPlayers)
        }

        fun sendToClients(json: String){
            thread{
                for(i in clients.indices){
                    socket = clients[i]
                    socketO!!.run{
                        try{
                            val printStream = PrintStream(this)
                            printStream.println(json)
                            printStream.flush()
                        } catch(_: Exception){
                            // TODO Exception here
                        }
                    }
                }
            }
        }

        fun sendToClient(json: String, clientSocket: Socket){
            thread {
                socket = clientSocket
                socketO!!.run{
                    try{
                        val printStream = PrintStream(this)
                        printStream.println(json)
                        printStream.flush()
                    } catch(_: Exception){
                        // TODO Exception here
                    }
                }
            }
        }
    }
}