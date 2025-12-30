package com.github.dhakarpd.animeera.presentation.animeList

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.github.dhakarpd.animeera.domain.model.SyncStatus

@Composable
fun AnimeListScreen(
    animeListScreenViewModel: AnimeListScreenViewModel = hiltViewModel(),
    onAnimeClick: (Int) -> Unit
) {
    val animeList = animeListScreenViewModel.animeList.collectAsState()
    val syncStatus = animeListScreenViewModel.syncStatus.collectAsState(initial = SyncStatus.SYNCING)

//    println("Anime List: ${animeList.value}")
//    println("Sync Status: ${syncStatus.value}")

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
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "Sync"
                )
            }
        }
        LazyColumn {
            items(animeList.value) { anime ->
                AnimeListItem(anime = anime, onClick = { onAnimeClick(anime.id) })
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
