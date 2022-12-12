package pt.isec.swipe_maths.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class ServerService : Service() {

    inner class ServerBinder : Binder(){
        fun getService(): ServerService = this@ServerService
    }

    private var _counter = 0
    var counter = 0
        get() = _counter

    fun increment(){
        _counter++
    }

    private val binder: IBinder = ServerBinder()

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }
}