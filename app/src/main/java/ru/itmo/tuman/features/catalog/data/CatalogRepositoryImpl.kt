package ru.itmo.tuman.features.catalog.data

import android.database.SQLException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import ru.itmo.tuman.core.network.InternetAvailability
import ru.itmo.tuman.features.catalog.domain.CatalogFailure
import ru.itmo.tuman.features.catalog.domain.CatalogRepository
import ru.itmo.tuman.features.catalog.domain.CatalogResult
import ru.itmo.tuman.features.catalog.domain.CatalogSetStorage
import ru.itmo.tuman.features.catalog.domain.model.CatalogSetSummary
import java.io.IOException

class CatalogRepositoryImpl(
    private val api: CatalogApi,
    private val catalogSetStorage: CatalogSetStorage,
    private val internetAvailability: InternetAvailability,
) : CatalogRepository {

    override fun observeDownloadedVersions(): Flow<Map<String, Int>> =
        catalogSetStorage.observeDownloadedVersions()

    override suspend fun loadSets(): CatalogResult<List<CatalogSetSummary>> =
        when (val result = request { api.getSets() }) {
            is CatalogResult.Success -> CatalogResult.Success(
                result.value
                    .filter { it.language == BASHKIR_LANGUAGE_CODE }
                    .map(CatalogSetSummaryDto::toDomain),
            )
            is CatalogResult.Failure -> result
        }

    override suspend fun downloadSet(remoteId: String): CatalogResult<Unit> =
        when (val result = request { api.getSet(remoteId) }) {
            is CatalogResult.Failure -> result
            is CatalogResult.Success -> {
                if (result.value.id != remoteId) {
                    CatalogResult.Failure(CatalogFailure.RequestFailed)
                } else {
                    try {
                        catalogSetStorage.import(result.value.toDomain())
                        CatalogResult.Success(Unit)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: SQLException) {
                        CatalogResult.Failure(CatalogFailure.SaveFailed)
                    }
                }
            }
        }

    private suspend fun <T> request(block: suspend () -> T): CatalogResult<T> {
        if (!internetAvailability.hasInternet()) {
            return CatalogResult.Failure(CatalogFailure.NoInternet)
        }
        return try {
            CatalogResult.Success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            val failure = if (e.code() == 404) {
                CatalogFailure.SetNotFound
            } else {
                CatalogFailure.RequestFailed
            }
            CatalogResult.Failure(failure)
        } catch (e: SerializationException) {
            CatalogResult.Failure(CatalogFailure.RequestFailed)
        } catch (e: IOException) {
            val failure = if (internetAvailability.hasInternet()) {
                CatalogFailure.RequestFailed
            } else {
                CatalogFailure.NoInternet
            }
            CatalogResult.Failure(failure)
        }
    }

    private companion object {
        const val BASHKIR_LANGUAGE_CODE = "ba"
    }
}
