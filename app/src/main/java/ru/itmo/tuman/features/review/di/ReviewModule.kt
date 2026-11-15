package ru.itmo.tuman.features.review.di

import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.ParametersHolder
import ru.itmo.tuman.features.review.ui.ReviewViewModel
import ru.itmo.tuman.features.review.data.ReviewRepositoryImpl
import ru.itmo.tuman.features.review.domain.ReviewRepository

val reviewModule = module {
    single<ReviewRepository> { ReviewRepositoryImpl(get()) }
    viewModel { parameters: ParametersHolder ->
        ReviewViewModel(repository = get(), deckId = parameters.getOrNull<Long>())
    }
}
