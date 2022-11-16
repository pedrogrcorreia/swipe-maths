package pt.isec.swipe_maths

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import pt.isec.swipe_maths.databinding.FragmentGameBoardBinding
import pt.isec.swipe_maths.model.Game
import pt.isec.swipe_maths.views.GameScreen
import pt.isec.swipe_maths.views.GameViewModel

const val SQUARES_TO_SCROLL = 2

interface IGameBoardFragment{
    fun swipeVertical(selectedColumn: Int) : Boolean

    fun swipeHorizontal(selectedRow: Int) : Boolean
}

class GameBoardFragment : Fragment(), GestureDetector.OnGestureListener{
    var actBase : IGameBoardFragment? = null

    lateinit var binding: FragmentGameBoardBinding

    private val viewModel : GameViewModel by activityViewModels()

    var colWidth : Int = 0
    var rowHeight : Int = 0

    var selectedPlay : Boolean = false

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

//        viewModel.gameBoard.observe(viewLifecycleOwner){
//            binding.sq00.text = viewModel.gameBoard.value?.getNumber(0, 0)
//            binding.sq02.text = viewModel.gameBoard.value?.getNumber(0, 1)
//            binding.sq04.text = viewModel.gameBoard.value?.getNumber(0, 2)
//
//            binding.sq20.text = viewModel.gameBoard.value?.getNumber(1, 0)
//            binding.sq22.text = viewModel.gameBoard.value?.getNumber(1, 1)
//            binding.sq24.text = viewModel.gameBoard.value?.getNumber(1, 2)
//
//            binding.sq40.text = viewModel.gameBoard.value?.getNumber(2, 0)
//            binding.sq42.text = viewModel.gameBoard.value?.getNumber(2, 1)
//            binding.sq44.text = viewModel.gameBoard.value?.getNumber(2, 2)
//
//            binding.sq01.text = viewModel.gameBoard.value?.getHorizontalOperation(0, 0)
//            binding.sq03.text = viewModel.gameBoard.value?.getHorizontalOperation(0, 1)
//
//            binding.sq21.text = viewModel.gameBoard.value?.getHorizontalOperation(1, 0)
//            binding.sq23.text = viewModel.gameBoard.value?.getHorizontalOperation(1, 1)
//
//            binding.sq41.text = viewModel.gameBoard.value?.getHorizontalOperation(2, 0)
//            binding.sq43.text = viewModel.gameBoard.value?.getHorizontalOperation(2, 1)
//
//            binding.sq10.text = viewModel.gameBoard.value?.getVerticalOperation(0, 0)
//            binding.sq30.text = viewModel.gameBoard.value?.getVerticalOperation(0, 1)
//
//            binding.sq12.text = viewModel.gameBoard.value?.getVerticalOperation(1, 0)
//            binding.sq32.text = viewModel.gameBoard.value?.getVerticalOperation(1, 1)
//
//            binding.sq14.text = viewModel.gameBoard.value?.getVerticalOperation(2, 0)
//            binding.sq34.text = viewModel.gameBoard.value?.getVerticalOperation(2, 1)
//        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i("Debug", "$colWidth")
    }

    private val gestureDetector : GestureDetector by lazy {
        GestureDetector(context, this)
    }

    override fun onDown(e: MotionEvent?): Boolean {
        colWidth = binding.sq00.width
        rowHeight = binding.sq00.height
        Log.i("Debug","x: ${e!!.x} y: ${e.y}")
        Log.i("Debug", "width: $colWidth")
        Log.i("Debug", "height: $rowHeight")
//        viewModel.changeValue()
        selectedPlay = false
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
        if(!selectedPlay) {
            if (e2!!.y > e1!!.y + rowHeight * SQUARES_TO_SCROLL
                || e2!!.y < e1!!.y - rowHeight * SQUARES_TO_SCROLL
            ) {
                actBase!!.swipeVertical(getColumnScrolled(e1.x.toInt()))
                selectedPlay = true
                return true
            } else if (e2!!.x > e1!!.x + colWidth * SQUARES_TO_SCROLL
                || e2!!.x < e1!!.x - colWidth * SQUARES_TO_SCROLL
            ) {
                actBase!!.swipeHorizontal(getRowScrolled(e1.y.toInt()))
                selectedPlay = true
                return true
            }
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
        return false
    }

    fun getColumnScrolled(x : Int) : Int {
        when(x){
            in 1 until colWidth -> return 0
            in colWidth + 1 until colWidth * 2 -> return 1
            in (colWidth * 2) + 1 until colWidth * 3 -> return 2
            in (colWidth * 3) + 1 until colWidth * 4 -> return 3
            in (colWidth * 4) + 1 until colWidth * 5 -> return 4
        }
        return 100
    }

    fun getRowScrolled(x: Int) : Int {
        when(x){
            in 1 until rowHeight -> return 0
            in rowHeight + 1 until rowHeight * 2 -> return 1
            in (rowHeight * 2) + 1 until rowHeight * 3 -> return 2
            in (rowHeight * 3) + 1 until rowHeight * 4 -> return 3
            in (rowHeight * 4) + 1 until rowHeight * 5 -> return 4
        }
        return 100
    }
}
