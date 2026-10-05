package com.abrarshakhi.lumen.core.domain.search

@JvmInline
value class ProviderId(val value: String) {
    override fun toString(): String = value
}

@JvmInline
value class ResultId(val value: String) {
    override fun toString(): String = value
}
