package ru.itmo.tuman.features.review.di

import org.koin.dsl.module
import ru.itmo.tuman.features.review.data.ReviewRepositoryImpl
import ru.itmo.tuman.features.review.domain.ReviewRepository

/** Что Koin создаёт для повторения. ViewModel экрана добавит Дворак М. */
val reviewModule = module {
    single<ReviewRepository> { ReviewRepositoryImpl(get()) }
}
