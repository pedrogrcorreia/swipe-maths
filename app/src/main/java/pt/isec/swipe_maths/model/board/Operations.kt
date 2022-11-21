package pt.isec.swipe_maths.model.board

enum class Operators(var value: Int, var string: String) {
    Add(1, "+"), Sub(1, "-"), Mul(2, "*"), Div(2, "/");
}

class Operations {
    companion object {
        fun calculate(leftOperand: Int, operator: String, rightOperand: Int): Int {
            when (operator) {
                "+" -> return leftOperand + rightOperand
                "-" -> return leftOperand - rightOperand
                "*" -> return leftOperand * rightOperand
                "/" -> return leftOperand / rightOperand
            }
            return 0
        }

        fun calculateExpression(numbers: Array<Int>, operators: Array<String>): Int{
            val operatorsValue : List<Operators> = getOperators(operators)
            var result = 0
            if(operatorsValue[0].value < operatorsValue[1].value){
                result = calculate(numbers[1], operatorsValue[1].string, numbers[2])
                result = calculate(numbers[0], operatorsValue[0].string, result)
            } else{
                result = calculate(numbers[0], operatorsValue[0].string, numbers[1])
                result = calculate(result, operatorsValue[1].string, numbers[2])
            }
            return result
        }

        private fun getOperators(operators: Array<String>): List<Operators>{
            val operator : MutableList<Operators> = mutableListOf()
            for(i in operators.indices){
                when(operators[i]){
                    "+" -> operator.add(Operators.Add)
                    "-" -> operator.add(Operators.Sub)
                    "*" -> operator.add(Operators.Mul)
                    "/" -> operator.add(Operators.Div)
                }
            }
            return operator
        }
    }
}