package pt.isec.swipe_maths.utils

import android.content.Context
import android.net.wifi.WifiManager
import androidx.core.content.ContextCompat.getSystemService
import java.io.InputStream
import java.io.OutputStream
import java.net.*
import kotlin.concurrent.thread

class NetUtils {
    companion object {
        const val SERVER_PORT = 9999

        private var socket: Socket? = null
        private val socketI: InputStream?
            get() = socket?.getInputStream()
        private val socketO: OutputStream?
            get() = socket?.getOutputStream()

        private var udpSocket : DatagramSocket? = null

        private var serverSocket: ServerSocket? = null

        private var threadComm: Thread? = null

        fun startServer(){
            if (serverSocket != null || socket != null)
                return

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
                udpSocket = DatagramSocket(9997)
                udpSocket?.run {
                    while(true){
                        udpSocket?.broadcast = true
                        val buffer = ByteArray(2048)
                        try {
                            println("UDP Server running!! ${udpSocket?.localPort} ${udpSocket?.localAddress}")
                            val packet = DatagramPacket(buffer, buffer.size)
                            udpSocket!!.receive(packet)
                            println("Received request")
                            val strMsg = "${serverSocket?.localPort}"
                            packet.data = strMsg.toByteArray()
                            udpSocket!!.send(packet)
                        } catch(_: Exception){
                            println("Exception!!!")
                        }

                    }
                }
            }

            thread {
                val multiSocket = MulticastSocket(3030)
                val group = InetAddress.getByName("230.30.30.30")
                multiSocket.joinGroup(group)
                multiSocket.broadcast = true
                val buffer = ByteArray(2048)
                while(true) {
                    try {
                        println("Multicast Server running!! ${multiSocket.localPort} ${multiSocket.localAddress}")
                        val packet = DatagramPacket(buffer, buffer.size)
                        multiSocket.receive(packet)
                        println("Received request")
                        val strMsg = "${serverSocket?.localPort}"
                        packet.data = strMsg.toByteArray()
                        multiSocket.send(packet)
                    } catch (_: Exception) {
                        println("Exception!!!")
                    }
                }
            }
        }

        fun startClient(serverIP: String,serverPort: Int = SERVER_PORT){
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
    }
}