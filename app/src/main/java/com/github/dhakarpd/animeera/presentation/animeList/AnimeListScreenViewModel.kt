package com.github.dhakarpd.animeera.presentation.animeList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.dhakarpd.animeera.data.local.dao.AnimeDao
import com.github.dhakarpd.animeera.domain.model.Anime
import com.github.dhakarpd.animeera.domain.model.SyncStatus
import com.github.dhakarpd.animeera.domain.repo.AnimeDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
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

    val syncStatus: SharedFlow<SyncStatus> = repository.fetchPopularAnimeList()
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            replay = 0
        )
}
