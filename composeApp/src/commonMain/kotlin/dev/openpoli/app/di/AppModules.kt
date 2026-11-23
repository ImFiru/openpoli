package dev.openpoli.app.di

import dev.openpoli.app.data.api.ItemApi
import dev.openpoli.app.data.api.KtorItemApi
import dev.openpoli.app.data.api.createHttpClient
import dev.openpoli.app.data.database.AppDatabase
import dev.openpoli.app.data.database.ItemDao
import dev.openpoli.app.data.database.createDatabase
import dev.openpoli.app.data.mapper.DefaultItemMapper
import dev.openpoli.app.data.mapper.ItemMapper
import dev.openpoli.app.data.repository.ItemRepositoryImpl
import dev.openpoli.app.domain.repository.ItemRepository
import dev.openpoli.app.domain.usecase.ObserveItemsUseCase
import dev.openpoli.app.domain.usecase.RefreshItemsUseCase
import dev.openpoli.app.presentation.home.HomeViewModel
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val networkModule: Module = module {
    single<HttpClient> { createHttpClient() }
    single<ItemApi> { KtorItemApi(get()) }
}

val databaseModule: Module = module {
    single<AppDatabase> { createDatabase() }
    single<ItemDao> { get<AppDatabase>().itemDao() }
}

val repositoryModule: Module = module {
    single<ItemMapper> { DefaultItemMapper() }
    single<ItemRepository> { ItemRepositoryImpl(get(), get(), get()) }
}

val featureModule: Module = module {
    factoryOf(::ObserveItemsUseCase)
    factoryOf(::RefreshItemsUseCase)
    viewModelOf(::HomeViewModel)
}

val appModules: List<Module> = listOf(
    networkModule,
    databaseModule,
    repositoryModule,
    featureModule,
)
