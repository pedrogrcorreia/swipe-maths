package pt.isec.swipe_maths.utils

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import org.json.JSONObject
import pt.isec.swipe_maths.ConnectionStates
import pt.isec.swipe_maths.model.Player
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream
import java.net.*
import kotlin.concurrent.thread

interface NetworkFragment{
    fun cancel()
}

class NetUtils {

}