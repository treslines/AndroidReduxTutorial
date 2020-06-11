package com.softsuit.redux.oo

interface Ema
interface Macd
interface Points

interface Selector {
    fun use(a: Points, b: Points): WithIndex
    fun use(a: Macd, b: Macd): WithIndex
    fun use(a: Ema, b: Ema): WithIndex
    fun use(a: Macd, b: Ema): WithIndex
    fun use(a: Points, b: Macd): WithIndex
    fun use(a: Points, b: Ema): WithIndex
}

interface WithIndex {
    fun wi(ia: Int, ib: Int): WithOp
}

interface WithOp {
    fun op(op: Op): WithNext
}

interface WithNext {
    fun and(): Selector
    fun or(): Selector
    fun isTrue(): Boolean
}

data class Term(
    var a: Double = 0.0,
    var b: Double = 0.0,
    var op: Op = Op.EQ
)

data class Expression(
    var ta: Term = Term(),
    var tb: Term = Term(),
    var exp: Exp = Exp.NONE
)

enum class Op { GT, GTE, SM, SME, EQ }
enum class Exp { AND, OR, NONE }

object Operand : Selector, WithIndex, WithNext {

    var exp = Expression()
    val expressions = mutableListOf<Expression>()


    override fun wi(ia: Int, ib: Int): WithOp {
        TODO("Not yet implemented")
    }

    override fun and(): Selector {
        TODO("Not yet implemented")
    }

    override fun or(): Selector {
        TODO("Not yet implemented")
    }

    override fun isTrue(): Boolean {
        TODO("Not yet implemented")
    }

    override fun use(a: Points, b: Points): WithIndex {
        TODO("Not yet implemented")
    }

    override fun use(a: Macd, b: Macd): WithIndex {
        TODO("Not yet implemented")
    }

    override fun use(a: Ema, b: Ema): WithIndex {
        TODO("Not yet implemented")
    }

    override fun use(a: Macd, b: Ema): WithIndex {
        TODO("Not yet implemented")
    }

    override fun use(a: Points, b: Macd): WithIndex {
        TODO("Not yet implemented")
    }

    override fun use(a: Points, b: Ema): WithIndex {
        TODO("Not yet implemented")
    }

}