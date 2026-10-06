package com.abrarshakhi.lumen.core.data.di

import com.abrarshakhi.lumen.core.data.db.AppIndexDao
import com.abrarshakhi.lumen.core.data.db.LumenDatabase
import com.abrarshakhi.lumen.core.data.document.AssetDocumentRepository
import com.abrarshakhi.lumen.core.domain.document.DocumentRepository
import com.abrarshakhi.lumen.core.data.db.UsageStatDao
import com.abrarshakhi.lumen.core.data.preferences.DataStoreUserPreferencesRepository
import com.abrarshakhi.lumen.core.data.network.HttpClientFactory
import com.abrarshakhi.lumen.core.data.repository.RoomUsageRepository
import com.abrarshakhi.lumen.core.data.secret.DataStoreSecretStore
import com.abrarshakhi.lumen.core.domain.secret.SecretStore
import com.abrarshakhi.lumen.core.domain.preferences.UserPreferencesRepository
import com.abrarshakhi.lumen.core.domain.repository.UsageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val ApplicationScopeQualifier = named("applicationScope")

val dataModule = module {

    single<CoroutineScope>(ApplicationScopeQualifier) {
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    single { LumenDatabase.create(androidContext()) }
    single<UsageStatDao> { get<LumenDatabase>().usageStatDao() }
    single<AppIndexDao> { get<LumenDatabase>().appIndexDao() }

    single<UserPreferencesRepository> {
        DataStoreUserPreferencesRepository(
            context = androidContext(),
            scope = get(ApplicationScopeQualifier),
        )
    }

    single { HttpClientFactory.create() }

    single<DocumentRepository> { AssetDocumentRepository(androidContext().assets) }

    single<SecretStore> {
        DataStoreSecretStore(
            context = androidContext(),
            scope = get(ApplicationScopeQualifier),
        )
    }

    single<UsageRepository> {
        RoomUsageRepository(dao = get(), scope = get(ApplicationScopeQualifier))
    }
}
