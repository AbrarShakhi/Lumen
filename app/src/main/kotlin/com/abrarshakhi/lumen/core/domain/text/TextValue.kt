package com.abrarshakhi.lumen.core.domain.text

sealed interface TextValue {

    data class Raw(val value: String) : TextValue

    data class Res(val id: Int, val args: List<Any> = emptyList()) : TextValue

    companion object {
        fun of(value: String): TextValue = Raw(value)
        fun res(id: Int, vararg args: Any): TextValue = Res(id, args.toList())
    }
}
