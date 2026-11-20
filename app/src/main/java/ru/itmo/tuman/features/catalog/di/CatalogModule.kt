package ru.itmo.tuman.features.catalog.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import ru.itmo.tuman.features.catalog.data.CatalogApi
import ru.itmo.tuman.features.catalog.data.CatalogRepositoryImpl
import ru.itmo.tuman.features.catalog.domain.CatalogRepository
import ru.itmo.tuman.features.catalog.ui.CatalogViewModel

val catalogModule = module {
    single<CatalogApi> { get<Retrofit>().create(CatalogApi::class.java) }
    single<CatalogRepository> { CatalogRepositoryImpl(get(), get(), get()) }
    viewModel { CatalogViewModel(get()) }
}
