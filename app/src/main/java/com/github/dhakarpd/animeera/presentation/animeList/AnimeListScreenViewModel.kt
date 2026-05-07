package com.github.dhakarpd.animeera.presentation.animeList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.github.dhakarpd.animeera.R
import com.github.dhakarpd.animeera.data.local.entity.AnimeEntity
import com.github.dhakarpd.animeera.domain.model.Anime
import com.github.dhakarpd.animeera.domain.model.SyncStatus
import com.github.dhakarpd.animeera.domain.repo.AnimeDataRepository
import com.github.dhakarpd.animeera.domain.usecase.EnsureAnimeSyncUseCase
import com.github.dhakarpd.animeera.presentation.common.SnackbarController
import com.github.dhakarpd.animeera.presentation.common.SnackbarEvent
import com.github.dhakarpd.animeera.presentation.common.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnimeListScreenViewModel @Inject constructor(
    val repository: AnimeDataRepository,
    /**
     * ensureAnimeSyncUseCase: EnsureAnimeSyncUseCase: By declaring a parameter without val or var,
     * you are telling Kotlin that this is just a constructor parameter.
     * Its scope is limited only to the init block. You can use it there, but it will not be accessible anywhere else in the class.
     * In your code, you only use ensureAnimeSyncUseCase inside the init block
     * **/
    ensureAnimeSyncUseCase: EnsureAnimeSyncUseCase,
): ViewModel() {

    /*val animeList: StateFlow<List<Anime>> = animeDao.getAllAnime().map { animeList ->
        animeList.map { animeEntity ->
            Anime(
                id = animeEntity.id,
                title = animeEntity.title,
                numberOfEpisodes = animeEntity.numberOfEpisodes,
                rating = animeEntity.rating,
                posterImageUrl = animeEntity.posterImageUrl,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )*/


    val animePager: StateFlow<PagingData<Anime>> = repository.getAnimePager()
        .map { pagingData: PagingData<AnimeEntity> ->
            pagingData.map { animeEntity ->
                Anime(
                    id = animeEntity.id,
                    title = animeEntity.title,
                    numberOfEpisodes = animeEntity.numberOfEpisodes,
                    rating = animeEntity.rating,
                    posterImageUrl = animeEntity.posterImageUrl,
                )
            }
        }
        .cachedIn(viewModelScope) // Cache the paging in viewmodel scope
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PagingData.empty(
                sourceLoadStates = LoadStates(
                    refresh = LoadState.Loading,
                    prepend = LoadState.Loading,
                    append = LoadState.Loading
                )
            )
        )

    /*
    *
    *
    * // Removed because when navigated from anime list -> anime detail and stayed for
    * // 5 sec or more and came back this sharedFlow was getting recollected leading to
    * // repository.fetchPopularAnimeList() call getting triggered again
    * // could have had SharingStarted.Lazily as well to tackle that
   val syncStatus: SharedFlow<SyncStatus> = repository.fetchPopularAnimeList()
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            replay = 0
        )
    * */

    // 1. Create a backing MutableStateFlow
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.IDLE)
    // 2. Expose as immutable StateFlow (StateFlow implies replay=1 automatically)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private var isDeviceOnline: Boolean = false

    init {
        //fetchAnime()
        isDeviceOnline = ensureAnimeSyncUseCase.execute()
        if (!isDeviceOnline) {
            viewModelScope.launch {
                SnackbarController.sendEvent(
                    SnackbarEvent(
                        message = UiText.StringResource(R.string.error_no_internet)
                    )
                )
            }
        }
    }

    fun getDeviceOnlineStatus(): Boolean {
        return isDeviceOnline
    }

    private fun fetchAnime() {
        viewModelScope.launch {
            repository.fetchPopularAnimeList()
                .collect { status ->
                    _syncStatus.value = status
                    when(status) {
                        SyncStatus.NO_INTERNET_CONNECTION -> {
                            SnackbarController.sendEvent(
                                SnackbarEvent(
                                    message = UiText.StringResource(R.string.error_no_internet)
                                )
                            )
                        }

                        SyncStatus.ERROR -> {
                            SnackbarController.sendEvent(
                                SnackbarEvent(
                                    message = UiText.StringResource(R.string.error_something_went_wrong)
                                )
                            )
                        }

                        else -> Unit
                    }
                }
        }
    }
}
