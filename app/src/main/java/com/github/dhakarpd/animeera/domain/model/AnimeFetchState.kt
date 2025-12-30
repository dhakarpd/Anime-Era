package com.github.dhakarpd.animeera.domain.model

sealed class AnimeFetchState {
    data object Loading : AnimeFetchState()
    data class StaleDataFetched(val animeWithDetail: AnimeWithDetail) : AnimeFetchState()
    data class StaleDataFetchFailure(val message: String? = null) : AnimeFetchState()
    data object Syncing : AnimeFetchState()
    data class SyncSuccess(val animeWithDetail: AnimeWithDetail) : AnimeFetchState()
    data class SyncFailure(val message: String? = null) : AnimeFetchState()
    data object NoInternetAvailable : AnimeFetchState()
}
