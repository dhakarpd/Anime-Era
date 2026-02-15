package com.github.dhakarpd.animeera.presentation.animeList

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.github.dhakarpd.animeera.domain.model.SyncStatus
import com.github.dhakarpd.animeera.presentation.common.shimmer
@Composable
fun AnimeListScreen(
    animeListScreenViewModel: AnimeListScreenViewModel = hiltViewModel(),
    onAnimeClick: (Int) -> Unit
) {
    val animePagingItems = animeListScreenViewModel.animePager.collectAsLazyPagingItems()
    val syncStatus = animeListScreenViewModel.syncStatus.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .height(24.dp)
        ) {
            Text("Anime List")
            if (syncStatus.value == SyncStatus.SYNCING) {
                val infiniteTransition = rememberInfiniteTransition(label = "sync-rotation")
                val angle by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(animation = tween(1000)),
                    label = "sync-angle"
                )
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "Syncing",
                    modifier = Modifier.rotate(angle)
                )
            }
        }

        val isInitialLoad = animePagingItems.loadState.refresh is LoadState.Loading
        val isListEmptyAfterLoad = animePagingItems.loadState.refresh is LoadState.NotLoading && animePagingItems.itemCount == 0

        if (isInitialLoad) {
            LazyColumn {
                items(8) {
                    AnimeListItemShimmer()
                }
            }

        } else if (isListEmptyAfterLoad) {
            FullScreenError(message = "Could not fetch data. Please check your internet connection and try again.") {
                animePagingItems.refresh()
            }
        } else {
            LazyColumn {
                items(animePagingItems.itemCount) { index ->
                    val anime = animePagingItems[index]
                    if (anime != null) {
                        AnimeListItem(anime = anime, onClick = {
                            onAnimeClick(anime.id)
                        })
                    }
                }
                item {
                    when (val appendLoadState = animePagingItems.loadState.append) {
                        is LoadState.Loading -> {
                            AnimeListItemShimmer()
                        }
                        is LoadState.Error -> {
                            // This state is less likely with your mediator, but good to handle.
                            // Could show a small retry button at the bottom of the list.
                            Text(
                                text = "Unable to fetch data.",
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        is LoadState.NotLoading -> {
                            if (appendLoadState.endOfPaginationReached && animePagingItems.itemCount > 0) {
                                Text(
                                    text = "You've reached the end!",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun AnimeListItem(
    anime: com.github.dhakarpd.animeera.domain.model.Anime,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            AsyncImage(
                model = anime.posterImageUrl,
                contentDescription = anime.title,
                modifier = Modifier
                    .size(100.dp)
                    .padding(end = 16.dp)
            )
            Column {
                Text(
                    text = anime.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Episodes: ${anime.numberOfEpisodes ?: "N/A"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Rating: ${anime.rating ?: "N/A"}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun AnimeListItemShimmer() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .padding(end = 16.dp)
                    .shimmer()
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .shimmer()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(16.dp)
                        .shimmer()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(16.dp)
                        .shimmer()
                )
            }
        }
    }
}

@Composable
fun FullScreenError(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(text = message, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}
