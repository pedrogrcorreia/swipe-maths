package pt.isec.swipe_maths.model.board

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
    }
}