package pt.isec.swipe_maths.utils

import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class NetUtils {
    companion object {
        const val SERVER_PORT = 9999

        private var socket: Socket? = null
        private val socketI: InputStream?
            get() = socket?.getInputStream()
        private val socketO: OutputStream?
            get() = socket?.getOutputStream()

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