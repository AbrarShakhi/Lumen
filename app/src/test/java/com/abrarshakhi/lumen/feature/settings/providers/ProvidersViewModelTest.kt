package com.abrarshakhi.lumen.feature.settings.providers

import com.abrarshakhi.lumen.core.domain.permission.AppPermission
import com.abrarshakhi.lumen.core.domain.permission.PermissionChecker
import com.abrarshakhi.lumen.core.domain.preferences.UserPreferences
import com.abrarshakhi.lumen.core.domain.preferences.UserPreferencesRepository
import com.abrarshakhi.lumen.core.domain.search.DefaultSearchProviderRegistry
import com.abrarshakhi.lumen.core.domain.search.ProviderId
import com.abrarshakhi.lumen.core.domain.search.ProviderMetadata
import com.abrarshakhi.lumen.core.domain.search.ProviderResults
import com.abrarshakhi.lumen.core.domain.search.ResultCategory
import com.abrarshakhi.lumen.core.domain.search.SearchProvider
import com.abrarshakhi.lumen.core.domain.search.SearchProviderRegistry
import com.abrarshakhi.lumen.core.domain.search.SearchQuery
import com.abrarshakhi.lumen.core.domain.text.TextValue
import com.abrarshakhi.lumen.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The providers screen's job is to say, accurately, why a source is not working — and the
 * distinction between "not yet granted" and "permanently denied" matters, because only one
 * of them can be fixed with a dialog.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProvidersViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private class FakeProvider(
        name: String,
        order: Int,
        permissions: List<AppPermission> = emptyList(),
    ) : SearchProvider {
        override val id = ProviderId(name)
        override val metadata = ProviderMetadata(
            displayName = TextValue.Raw(name),
            category = ResultCategory.App,
            order = order,
            requiredPermissions = permissions,
        )
        override fun search(query: SearchQuery): Flow<ProviderResults> =
            flowOf(ProviderResults.empty(id))
    }

    private class FakePermissions(
        override val sdkInt: Int = 31,
        private val granted: Set<AppPermission> = emptySet(),
        private val blocked: Set<AppPermission> = emptySet(),
    ) : PermissionChecker {
        override fun isGranted(permission: AppPermission) = permission in granted
        override fun isPermanentlyDenied(permission: AppPermission) = permission in blocked
    }

    private class FakePreferences(
        initial: UserPreferences = UserPreferences.Default,
    ) : UserPreferencesRepository {
        private val state = MutableStateFlow(initial)
        override val preferences: StateFlow<UserPreferences> = state
        override suspend fun update(transform: (UserPreferences) -> UserPreferences) {
            state.value = transform(state.value)
        }
    }

    private fun registry(vararg providers: SearchProvider) = object : SearchProviderRegistry {
        override val providers = providers.toList()
    }

    @Test
    fun `a provider needing nothing reports no permission requirement`() = runTest {
        val viewModel = ProvidersViewModel(
            registry(FakeProvider("apps", 10)),
            FakePermissions(),
            FakePreferences(),
        )
        advanceUntilIdle()

        assertEquals(
            ProviderPermissionState.NotRequired,
            viewModel.state.value.providers.single().permission,
        )
    }

    @Test
    fun `a granted permission reports ready`() = runTest {
        val viewModel = ProvidersViewModel(
            registry(FakeProvider("contacts", 10, listOf(AppPermission.ReadContacts))),
            FakePermissions(granted = setOf(AppPermission.ReadContacts)),
            FakePreferences(),
        )
        advanceUntilIdle()

        assertEquals(
            ProviderPermissionState.Granted,
            viewModel.state.value.providers.single().permission,
        )
    }

    @Test
    fun `an ungranted permission is requestable`() = runTest {
        val viewModel = ProvidersViewModel(
            registry(FakeProvider("contacts", 10, listOf(AppPermission.ReadContacts))),
            FakePermissions(),
            FakePreferences(),
        )
        advanceUntilIdle()

        val permission = viewModel.state.value.providers.single().permission
        assertTrue(permission is ProviderPermissionState.Missing)
        assertEquals(listOf(AppPermission.ReadContacts), permission.permissions)
    }

    @Test
    fun `a permanently denied permission is reported as blocked, not missing`() = runTest {
        // Showing a request prompt here would do nothing at all, so the UI has to send the
        // user to system settings instead.
        val viewModel = ProvidersViewModel(
            registry(FakeProvider("contacts", 10, listOf(AppPermission.ReadContacts))),
            FakePermissions(blocked = setOf(AppPermission.ReadContacts)),
            FakePreferences(),
        )
        advanceUntilIdle()

        assertTrue(viewModel.state.value.providers.single().permission is ProviderPermissionState.Blocked)
    }

    @Test
    fun `permissions outside this device's sdk range are ignored`() = runTest {
        // On API 31 the granular media permissions do not exist, so a file provider that
        // declares all of them must not be reported as missing the 33+ ones.
        val viewModel = ProvidersViewModel(
            registry(
                FakeProvider(
                    "files",
                    10,
                    listOf(AppPermission.ReadExternalStorage, AppPermission.ReadMediaImages),
                ),
            ),
            FakePermissions(sdkInt = 31, granted = setOf(AppPermission.ReadExternalStorage)),
            FakePreferences(),
        )
        advanceUntilIdle()

        assertEquals(
            ProviderPermissionState.Granted,
            viewModel.state.value.providers.single().permission,
            "only the permission that applies on API 31 should be considered",
        )
    }

    @Test
    fun `toggling a provider persists and is reflected back`() = runTest {
        val preferences = FakePreferences()
        val viewModel = ProvidersViewModel(
            registry(FakeProvider("apps", 10)),
            FakePermissions(),
            preferences,
        )
        advanceUntilIdle()
        assertTrue(viewModel.state.value.providers.single().enabled, "defaults to enabled")

        viewModel.dispatch(ProvidersIntent.EnabledSet(ProviderId("apps"), enabled = false))
        advanceUntilIdle()

        assertEquals(false, preferences.preferences.value.enabledProviders["apps"])
        assertEquals(false, viewModel.state.value.providers.single().enabled)
    }

    @Test
    fun `providers appear in metadata order, not registration order`() = runTest {
        // Uses the real registry, because ordering is its responsibility — the fake above
        // deliberately preserves insertion order and would make this assertion vacuous.
        val viewModel = ProvidersViewModel(
            DefaultSearchProviderRegistry(
                listOf(FakeProvider("web", order = 90), FakeProvider("apps", order = 10)),
            ),
            FakePermissions(),
            FakePreferences(),
        )
        advanceUntilIdle()

        assertEquals(listOf("apps", "web"), viewModel.state.value.providers.map { it.id.value })
    }
}
