package pt.isec.swipe_maths.model.levels

enum class Levels : ILevels {
    Easy {
        override val validOperations: Array<String>
            get() = arrayOf("+")
        override val min: Int
            get() = 1
        override val max: Int
            get() = 9
    },
    Medium {
        override val validOperations: Array<String>
            get() = arrayOf("+", "-")
        override val min: Int
            get() = 1
        override val max: Int
            get() = 99
    },
    Hard {
        override val validOperations: Array<String>
            get() = arrayOf("+", "-", "*")
        override val min: Int
            get() = 1
        override val max: Int
            get() = 999
    },
    Expert {
        override val validOperations: Array<String>
            get() = arrayOf("+", "-", "*", "/")
        override val min: Int
            get() = 99
        override val max: Int
            get() = 999
    },
}