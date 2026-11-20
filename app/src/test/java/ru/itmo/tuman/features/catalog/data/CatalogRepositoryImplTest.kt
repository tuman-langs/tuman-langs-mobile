package ru.itmo.tuman.features.catalog.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import ru.itmo.tuman.core.network.InternetAvailability
import ru.itmo.tuman.features.catalog.domain.CatalogFailure
import ru.itmo.tuman.features.catalog.domain.CatalogSetStorage
import ru.itmo.tuman.features.catalog.domain.CatalogResult
import ru.itmo.tuman.features.catalog.domain.model.CatalogCardPayload
import ru.itmo.tuman.features.catalog.domain.model.CatalogSetPayload
import ru.itmo.tuman.features.catalog.domain.model.CatalogSetSummary

class CatalogRepositoryImplTest {

    @Test
    fun loadSetsKeepsBashkirEntriesAndMapsSummaryFields() = runBlocking {
        val api = FakeCatalogApi().apply {
            sets = listOf(
                summary(language = "ba", id = "ba-set"),
                summary(language = "ru", id = "other-set"),
            )
        }
        val repository = repository(api = api)

        val result = repository.loadSets()

        assertEquals(
            CatalogResult.Success(
                listOf(
                    CatalogSetSummary(
                        remoteId = "ba-set",
                        title = "Demo",
                        description = "Test catalog set",
                        version = 2,
                        cardCount = 3,
                    ),
                ),
            ),
            result,
        )
    }

    @Test
    fun downloadImportsSetAndItsCardFields() = runBlocking {
        val api = FakeCatalogApi().apply {
            set = CatalogSetDto(
                id = "ba-set",
                version = 2,
                title = "Demo",
                description = "Test catalog set",
                cards = listOf(
                    CatalogCardDto(
                        id = "card-1",
                        front = "front",
                        back = "back",
                        example = "example",
                        exampleTranslation = "translation",
                        pronunciation = "pronunciation",
                        notes = "notes",
                    ),
                ),
            )
        }
        val storage = FakeCatalogSetStorage()
        val repository = repository(api = api, storage = storage)

        val result = repository.downloadSet("ba-set")

        assertEquals(CatalogResult.Success(Unit), result)
        assertEquals(
            listOf(
                CatalogSetPayload(
                    remoteId = "ba-set",
                    version = 2,
                    title = "Demo",
                    description = "Test catalog set",
                    cards = listOf(
                        CatalogCardPayload(
                            remoteId = "card-1",
                            front = "front",
                            back = "back",
                            example = "example",
                            exampleTranslation = "translation",
                            pronunciation = "pronunciation",
                            notes = "notes",
                        ),
                    ),
                ),
            ),
            storage.imported,
        )
    }

    @Test
    fun returnsOfflineWithoutRequestWhenInternetIsUnavailable() = runBlocking {
        val api = FakeCatalogApi()
        val internet = FakeInternetAvailability(available = false)
        val repository = repository(api = api, internet = internet)

        val result = repository.loadSets()

        assertEquals(CatalogResult.Failure(CatalogFailure.NoInternet), result)
        assertEquals(0, api.listRequests)
    }

    @Test
    fun distinguishesServerFailureFromMissingInternet() = runBlocking {
        val api = FakeCatalogApi().apply { listFailure = java.io.IOException("timeout") }
        val repository = repository(api = api)

        val result = repository.loadSets()

        assertEquals(CatalogResult.Failure(CatalogFailure.RequestFailed), result)
    }

    private fun repository(
        api: FakeCatalogApi = FakeCatalogApi(),
        storage: FakeCatalogSetStorage = FakeCatalogSetStorage(),
        internet: FakeInternetAvailability = FakeInternetAvailability(available = true),
    ) = CatalogRepositoryImpl(api, storage, internet)

    private class FakeCatalogApi : CatalogApi {
        var sets: List<CatalogSetSummaryDto> = emptyList()
        var set: CatalogSetDto? = null
        var listFailure: Exception? = null
        var listRequests = 0

        override suspend fun getSets(): List<CatalogSetSummaryDto> {
            listRequests++
            listFailure?.let { throw it }
            return sets
        }

        override suspend fun getSet(id: String): CatalogSetDto =
            requireNotNull(set).also { assertEquals(id, it.id) }
    }

    private class FakeCatalogSetStorage : CatalogSetStorage {
        val imported = mutableListOf<CatalogSetPayload>()
        private val versions = MutableStateFlow<Map<String, Int>>(emptyMap())

        override fun observeDownloadedVersions(): Flow<Map<String, Int>> = versions

        override suspend fun import(set: CatalogSetPayload) {
            imported += set
            versions.value = versions.value + (set.remoteId to set.version)
        }
    }

    private class FakeInternetAvailability(
        var available: Boolean,
    ) : InternetAvailability {
        override fun hasInternet(): Boolean = available
    }

    private fun summary(language: String, id: String) = CatalogSetSummaryDto(
        id = id,
        language = language,
        title = "Demo",
        description = "Test catalog set",
        version = 2,
        cardCount = 3,
    )
}
