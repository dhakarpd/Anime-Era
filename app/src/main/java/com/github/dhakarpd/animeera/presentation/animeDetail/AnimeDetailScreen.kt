package com.github.dhakarpd.animeera.presentation.animeDetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.github.dhakarpd.animeera.R

@Composable
fun AnimeDetailScreen(animeId: Int, viewModel: AnimeDetailScreenViewModel = hiltViewModel()) {
    val animeDetailState by viewModel.animeDetailState.collectAsState()

    LaunchedEffect(key1 = Unit) {
        // Check added to avoid triggering API call on configuration changes
        if (animeDetailState.animeId != animeId) {
            viewModel.getAnimeDetails(animeId)
        }
    }


    /***
     *
     *
     *
     * Need to handle scenario when db doesn't have data + api also failed
     *
     *
     *
     * */


    if (animeDetailState.animeId == animeId) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Video Player or Poster
            if (!animeDetailState.trailerUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                ) {
                    LifecycleAwareVideoPlayer(videoUrl = animeDetailState.trailerUrl!!)
                }
            } else {
                AsyncImage(
                    model = animeDetailState.posterImageUrl,
                    contentDescription = animeDetailState.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Title
                Text(
                    text = animeDetailState.title,
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Plot/Synopsis
                if (!animeDetailState.synopsis.isNullOrBlank()) {
                    Text(
                        text = animeDetailState.synopsis!!,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Genre(s)
                if (animeDetailState.genres.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.label_genres, animeDetailState.genres.joinToString(", ")),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Main Cast
                if (animeDetailState.cast.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.label_cast, animeDetailState.cast.joinToString(", ")),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Number of Episodes and Rating
                Row {
                    val episodesCount = animeDetailState.numberOfEpisodes?.toString()
                        ?: stringResource(R.string.not_available)

                    val ratingValue = animeDetailState.rating?.toString()
                        ?: stringResource(R.string.not_available)

                    Text(
                        text = stringResource(R.string.episodes_label, episodesCount),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.rating_label, ratingValue),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
