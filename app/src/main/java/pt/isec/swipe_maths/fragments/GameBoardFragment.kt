package pt.isec.swipe_maths

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import pt.isec.swipe_maths.databinding.FragmentGameBoardBinding
import pt.isec.swipe_maths.model.Game

interface IGameBoardFragment{
    fun test() : Boolean
}

class GameBoardFragment : Fragment(), GestureDetector.OnGestureListener{
    var actBase : IGameBoardFragment? = null

    lateinit var binding: FragmentGameBoardBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.i("Debug", "onAttach2: ")
        actBase = context as? IGameBoardFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGameBoardBinding.inflate(inflater)
        val rootView : View = binding.frBoard.rootView

        rootView.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    gestureDetector.onTouchEvent(event)
                }
                if(event.action == MotionEvent.ACTION_MOVE){
                    gestureDetector.onTouchEvent(event)
                }
                v?.performClick()
                return true
            }
        })
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        for(i in 0 .. 4){
//            for(j in 0 .. 4){
//                val text = binding.root.findViewById<TextView>(gameBoard.textArray[i][j])
//                text.text = gameBoard.numbersArray[i][j].toString() + "  "
//            }
//        }
    }

    private val gestureDetector : GestureDetector by lazy {
        GestureDetector(context, this)
    }

    override fun onDown(e: MotionEvent?): Boolean {
        Log.i("Debug","Fragment touch working!!!!")
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
        Log.i("Debug", "${e1?.x}, ${e2?.x}")
        actBase!!.test()
        return true
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
        return true
    }
}
