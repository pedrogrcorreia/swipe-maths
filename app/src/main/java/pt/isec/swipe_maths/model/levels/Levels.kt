package pt.isec.swipe_maths.model.levels

enum class Levels : ILevels {
    Easy {
        override val validOperations: Array<String>
            get() = arrayOf("+")
        override val min: Int
            get() = 1
        override val max: Int
            get() = 9
        override val timer: Int
            get() = 60
        override val correctAnswers: Int
            get() = 1
        override val bonusTime: Int
            get() = 5
        override val nextLevel: Levels
            get() = Medium
    },
    Medium {
        override val validOperations: Array<String>
            get() = arrayOf("+", "-")
        override val min: Int
            get() = 1
        override val max: Int
            get() = 99
        override val timer: Int
            get() = 50
        override val correctAnswers: Int
            get() = 7
        override val bonusTime: Int
            get() = 5
        override val nextLevel: Levels
            get() = Hard
    },
    Hard {
        override val validOperations: Array<String>
            get() = arrayOf("+", "-", "*")
        override val min: Int
            get() = 1
        override val max: Int
            get() = 999
        override val timer: Int
            get() = 45
        override val correctAnswers: Int
            get() = 7
        override val bonusTime: Int
            get() = 3
        override val nextLevel: Levels
            get() = Expert
    },
    Expert {
        override val validOperations: Array<String>
            get() = arrayOf("+", "-", "*", "/")
        override val min: Int
            get() = 99
        override val max: Int
            get() = 999
        override val timer: Int
            get() = 30
        override val correctAnswers: Int
            get() = 10
        override val bonusTime: Int
            get() = 5
        override val nextLevel: Levels
            get() = Expert
    },
}