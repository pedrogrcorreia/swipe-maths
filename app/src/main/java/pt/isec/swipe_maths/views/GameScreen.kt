package pt.isec.swipe_maths.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import pt.isec.swipe_maths.model.GameBoard

class GameScreen @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes), GestureDetector.OnGestureListener {

    companion object{
        var colWidth : Int = 0
        var rowHeight : Int = 0

        fun setValues(width : Int, height : Int){
            colWidth = width
            rowHeight = height
        }
    }

    private lateinit var gameBoard : GameBoard

    constructor(context : Context, gameBoard: GameBoard) : this(context){
        this.gameBoard = gameBoard
    }

    var selectedPlay : Boolean = false

    private val gestureDetector : GestureDetector by lazy {
        GestureDetector(context, this)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(gestureDetector.onTouchEvent(event)){
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun onDown(e: MotionEvent?): Boolean {
        Log.i("Debug", "${e?.x}")
        return true
    }

    override fun onShowPress(e: MotionEvent?) {
        return
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        return true
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        if(e1!!.y + e2!!.y > rowHeight * 4 /*&& !selectedPlay*/) {
            Log.i("Debug", "${getColumnScrolled(e1!!.x.toInt())}")
            Log.i("Debug", "${e1.x}")
            selectedPlay = !selectedPlay
            return true
        }
        return false
    }

    override fun onLongPress(e: MotionEvent?) {
        return
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        Log.i("Debug", "onFling ${e1?.x} ${e2?.x}")
        return true
    }

    fun getColumnScrolled(x : Int) : Int {
        if(x in 1 until colWidth){
            return 0
        }

        when(x){
            in 1 until colWidth -> return 0
            in colWidth + 1 until colWidth * 2 -> return 1
            in (colWidth * 2) + 1 until colWidth * 3 -> return 2
            in (colWidth * 3) + 1 until colWidth * 4 -> return 3
            in (colWidth * 4) + 1 until colWidth * 5 -> return 4
        }
        return 100
    }
}