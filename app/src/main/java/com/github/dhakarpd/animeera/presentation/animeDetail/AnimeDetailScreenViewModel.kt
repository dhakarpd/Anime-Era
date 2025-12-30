package com.github.dhakarpd.animeera.presentation.animeDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.dhakarpd.animeera.domain.model.AnimeFetchState
import com.github.dhakarpd.animeera.domain.model.AnimeWithDetail
import com.github.dhakarpd.animeera.domain.repo.AnimeDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class AnimeDetailScreenViewModel @Inject constructor(private val repository: AnimeDataRepository) : ViewModel() {

    private val _animeDetailState = MutableStateFlow(AnimeWithDetail(0, "", 0, 0.0, "", "", emptyList(), emptyList(), ""))
    val animeDetailState: StateFlow<AnimeWithDetail> = _animeDetailState.asStateFlow()

    private val _fetchState = MutableStateFlow<AnimeFetchState>(AnimeFetchState.Loading)
    val fetchState: StateFlow<AnimeFetchState> = _fetchState.asStateFlow()

    fun getAnimeDetails(id: Int) {
        repository.fetchAnimeByID(id).onEach { state ->
            _fetchState.value = state
            when (state) {
                is AnimeFetchState.StaleDataFetched -> {
                    _animeDetailState.value = state.animeWithDetail
                }
                is AnimeFetchState.SyncSuccess -> {
                    _animeDetailState.value = state.animeWithDetail
                }
                else -> Unit
            }
        }.launchIn(viewModelScope)
    }
}
