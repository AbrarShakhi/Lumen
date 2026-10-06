package com.abrarshakhi.lumen.feature.about.di

import com.abrarshakhi.lumen.core.domain.document.ProjectDocument
import com.abrarshakhi.lumen.feature.about.DocumentViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val aboutFeatureModule = module {
    viewModel { (document: ProjectDocument) ->
        DocumentViewModel(document = document, repository = get())
    }
}
