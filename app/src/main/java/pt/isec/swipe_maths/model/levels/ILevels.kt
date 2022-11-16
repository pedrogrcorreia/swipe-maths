package pt.isec.swipe_maths.model.levels

interface ILevels {

    val validOperations : Array<String>

    val min : Int
    val max : Int

    val timer : Int
    val correctAnswers : Int
    val bonusTime : Int
    val nextLevel : Levels
}