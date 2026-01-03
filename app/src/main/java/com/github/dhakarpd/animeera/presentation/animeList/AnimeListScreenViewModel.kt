package com.github.dhakarpd.animeera.presentation.animeList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.dhakarpd.animeera.data.local.dao.AnimeDao
import com.github.dhakarpd.animeera.domain.model.Anime
import com.github.dhakarpd.animeera.domain.model.SyncStatus
import com.github.dhakarpd.animeera.domain.repo.AnimeDataRepository
import com.github.dhakarpd.animeera.presentation.common.SnackbarController
import com.github.dhakarpd.animeera.presentation.common.SnackbarEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnimeListScreenViewModel @Inject constructor(
    val repository: AnimeDataRepository,
    val animeDao: AnimeDao
): ViewModel() {

    val animeList: StateFlow<List<Anime>> = animeDao.getAllAnime().map { animeList ->
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

    init {
        fetchAnime()
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
                                    message = "Please check your internet connection"
                                )
                            )
                        }

                        SyncStatus.ERROR -> {
                            SnackbarController.sendEvent(
                                SnackbarEvent(
                                    message = "Something went wrong"
                                )
                            )
                        }

                        else -> Unit
                    }
                }
        }
    }
}
