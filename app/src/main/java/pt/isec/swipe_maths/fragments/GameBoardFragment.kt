package pt.isec.swipe_maths.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import pt.isec.swipe_maths.R
import pt.isec.swipe_maths.databinding.FragmentGameBoardBinding
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


        viewModel.board.observe(viewLifecycleOwner){

            // Lines
            binding.sq00.text = viewModel.board.value?.lines?.get(0)?.numbers?.get(0).toString()
//            binding.sq00.text = viewModel.board.value?.lines?.get(0)?.numbers?.get(0).toString()
            binding.sq01.text = viewModel.board.value?.lines?.get(0)?.operators?.get(0)
            binding.sq02.text = viewModel.board.value?.lines?.get(0)?.numbers?.get(1).toString()
            binding.sq03.text = viewModel.board.value?.lines?.get(0)?.operators?.get(1)
            binding.sq04.text = viewModel.board.value?.lines?.get(0)?.numbers?.get(2).toString()

            binding.sq20.text = viewModel.board.value?.lines?.get(1)?.numbers?.get(0).toString()
            binding.sq21.text = viewModel.board.value?.lines?.get(1)?.operators?.get(0)
            binding.sq22.text = viewModel.board.value?.lines?.get(1)?.numbers?.get(1).toString()
            binding.sq23.text = viewModel.board.value?.lines?.get(1)?.operators?.get(1)
            binding.sq24.text = viewModel.board.value?.lines?.get(1)?.numbers?.get(2).toString()

            binding.sq40.text = viewModel.board.value?.lines?.get(2)?.numbers?.get(0).toString()
            binding.sq41.text = viewModel.board.value?.lines?.get(2)?.operators?.get(0)
            binding.sq42.text = viewModel.board.value?.lines?.get(2)?.numbers?.get(1).toString()
            binding.sq43.text = viewModel.board.value?.lines?.get(2)?.operators?.get(1)
            binding.sq44.text = viewModel.board.value?.lines?.get(2)?.numbers?.get(2).toString()


            // Column operators
            binding.sq10.text = viewModel.board.value?.cols?.get(0)?.operators?.get(0)
            binding.sq30.text = viewModel.board.value?.cols?.get(0)?.operators?.get(1)

            binding.sq12.text = viewModel.board.value?.cols?.get(1)?.operators?.get(0)
            binding.sq32.text = viewModel.board.value?.cols?.get(1)?.operators?.get(1)

            binding.sq14.text = viewModel.board.value?.cols?.get(2)?.operators?.get(0)
            binding.sq34.text = viewModel.board.value?.cols?.get(2)?.operators?.get(1)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private val gestureDetector : GestureDetector by lazy {
        GestureDetector(context, this)
    }

    override fun onDown(e: MotionEvent?): Boolean {
        colWidth = binding.sq00.width
        rowHeight = binding.sq00.height
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
                getColumnScrolled(e1.x.toInt())?.let { actBase!!.swipeVertical(it) }
                selectedPlay = true
                return true
            } else if (e2!!.x > e1!!.x + colWidth * SQUARES_TO_SCROLL
                || e2!!.x < e1!!.x - colWidth * SQUARES_TO_SCROLL
            ) {
                getRowScrolled(e1.y.toInt())?.let { actBase!!.swipeHorizontal(it) }
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

    private fun getColumnScrolled(x : Int) : Int? {
        when(x){
            in 1 until colWidth -> return 0
//            in colWidth + 1 until colWidth * 2 -> return 1
            in (colWidth * 2) + 1 until colWidth * 3 -> return 1
//            in (colWidth * 3) + 1 until colWidth * 4 -> return 3
            in (colWidth * 4) + 1 until colWidth * 5 -> return 2
        }
        return null
    }

    private fun getRowScrolled(x: Int) : Int? {
        when(x){
            in 1 until rowHeight -> return 0
//            in rowHeight + 1 until rowHeight * 2 -> return 1
            in (rowHeight * 2) + 1 until rowHeight * 3 -> return 1
//            in (rowHeight * 3) + 1 until rowHeight * 4 -> return 3
            in (rowHeight * 4) + 1 until rowHeight * 5 -> return 2
        }
        return null
    }
}
