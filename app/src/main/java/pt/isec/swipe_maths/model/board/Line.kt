package pt.isec.swipe_maths.model.board

import com.google.gson.*
import pt.isec.swipe_maths.model.levels.Levels
import java.lang.reflect.Type
import kotlin.random.Random

class Line(level: Levels = Levels.Easy) : JsonSerializer<Line>, JsonDeserializer<Line> {
    companion object{
        private val random : Random = Random(System.currentTimeMillis())
    }
    var numbers : Array<Int> = arrayOf()
    var operators : Array<String>
    val lineValue : Int

    init{
        numbers = arrayOf(random.nextInt(level.min, level.max),
            random.nextInt(level.min, level.max),
            random.nextInt(level.min, level.max))
        operators = arrayOf(level.validOperations[random.nextInt(level.validOperations.size)],
            level.validOperations[random.nextInt(level.validOperations.size)]
            )
        lineValue = lineValue()
    }

    constructor(numbers: Array<Int>, operators: Array<String>) : this(){
        this.numbers = numbers
        this.operators = operators
    }

    fun printLine(): String {
        return "${numbers[0]} ${operators[0]} " +
                "${numbers[1]} ${operators[1]} ${numbers[2]} = $lineValue"
    }

    private fun lineValue() : Int {
//        var result = numbers[0]
//        for(i in operators.indices){
//            result = Operations.calculate(result, operators[i], numbers[i+1])
//        }
        return Operations.calculateExpression(numbers, operators)
    }

    override fun serialize(
        src: Line?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonObject {
        val jsonObject = JsonObject()

        try {

            jsonObject.add("numbers", context?.serialize(src?.numbers))
            jsonObject.add("operators", context?.serialize(src?.operators))

        }catch (e: Exception){
            println(e.message)
        }

        return jsonObject
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext?
    ): Line? {
        val json = json.asJsonObject

        val n = json.getAsJsonArray("numbers")
        val numbers : Array<Int> = Array(n.size()) { 0 }

        for(i in 0 until n.size()){
            numbers[i] = n.get(i).asInt
        }

        val o = json.getAsJsonArray("operators")
        val operators : Array<String> = Array(o.size()) { "" }

        for(i in 0 until o.size()){
            operators[i] = o.get(i).asString
        }
        return Line(numbers, operators)
    }
}