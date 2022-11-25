package pt.isec.swipe_maths.utils

import android.content.Context
import android.net.wifi.WifiManager
import android.provider.Settings.Global.getString
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.MutableLiveData
import com.google.android.material.snackbar.Snackbar
import pt.isec.swipe_maths.ConnectionStates
import pt.isec.swipe_maths.R
import java.io.InputStream
import java.io.OutputStream
import java.net.*
import java.util.concurrent.TimeoutException
import kotlin.concurrent.thread

class NetUtils {
    companion object {
        val state : MutableLiveData<ConnectionStates> = MutableLiveData()

        const val SERVER_PORT = 9999

        private var socket: Socket? = null
        private val socketI: InputStream?
            get() = socket?.getInputStream()
        private val socketO: OutputStream?
            get() = socket?.getOutputStream()

        private var serverSocket: ServerSocket? = null

        private var threadComm: Thread? = null

        fun startServer(strIpAddress: String){
            if (serverSocket != null || socket != null)
                return
            state.value = ConnectionStates.SERVER_CONNECTING
            thread {
                serverSocket = ServerSocket(SERVER_PORT)
                serverSocket?.run {
                    while(true) {
                        try {
                            val socketClient = serverSocket!!.accept()
                            println("Received connection request!")
                            startComm(socketClient)
                        } catch (_: Exception) {
                        }
                    }
                }
            }

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

        fun startClient(serverIP: String, serverPort: Int = SERVER_PORT){
            if (socket != null)
                return

            thread {
                try {
                    //val newsocket = Socket(serverIP, serverPort)
                    val newsocket = Socket()
                    newsocket.connect(InetSocketAddress(serverIP,serverPort),5000)
                    startComm(newsocket)
                } catch (_: Exception) {

                }
            }
        }

        private fun startComm(newSocket: Socket){
//            if (threadComm != null)
//                return

            socket = newSocket

            threadComm = thread {
                try {
                    if (socketI == null)
                        return@thread

                    val bufI = socketI!!.bufferedReader()
                    println("Connected successfully!")
                    while (true) {
                        val message = bufI.readLine()
                        val move = message.toIntOrNull()
                    }
                } catch (_: Exception) {
                } finally {

                }
            }
        }

        fun contactMulticast() : String? {
            val buffer = ByteArray(2056)
            val messageStr = "Connect"
            val socket = DatagramSocket()
            socket.broadcast = true
            val sendRequest = messageStr.toByteArray()
            val packet =
                DatagramPacket(sendRequest, sendRequest.size, InetAddress.getByName("224.0.0.251"), 9996)

            socket.soTimeout = 5000

            try {
                socket.send(packet)
                packet.data = buffer
                packet.length = buffer.size
                socket.receive(packet)
                val received = ByteArray(packet.length)
                packet.data.copyInto(received, 0, 0, packet.length)
                val str = String(received)
                val ipAndPort = str.split(" ")
                startClient(ipAndPort[0], ipAndPort[1].toInt())
            } catch (e : SocketTimeoutException){
                state.postValue(ConnectionStates.CONNECTION_ERROR)
                return e.message!!
            }
            return null
        }
    }
}