package com.abrarshakhi.lumen.feature.about

import com.abrarshakhi.lumen.BuildConfig

object ProjectLinks {
    val repository: String = BuildConfig.REPOSITORY_URL.trimEnd('/')
    val star: String = repository
    val newIssue: String = "$repository/issues/new"
}
