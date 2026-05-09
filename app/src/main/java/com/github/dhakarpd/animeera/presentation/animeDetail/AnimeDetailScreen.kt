package com.github.dhakarpd.animeera.presentation.animeDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.github.dhakarpd.animeera.R
import com.github.dhakarpd.animeera.presentation.common.shimmer

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnimeDetailScreen(animeId: Int, viewModel: AnimeDetailScreenViewModel = hiltViewModel()) {
    val animeDetailState by viewModel.animeDetailState.collectAsState()

    LaunchedEffect(key1 = Unit) {
        // Check added to avoid triggering API call on configuration changes
        if (animeDetailState.animeId != animeId) {
            viewModel.getAnimeDetails(animeId)
        }
    }

    /**
     * 1. Capture the Scroll State
     * We already have val scrollState = rememberScrollState() in your AnimeDetailScreen.kt. This
     * state tracks exactly how many pixels the user has scrolled down.
     * 2. Calculate the "Parallax Offset"
     * As the user scrolls, we want the banner to move down slightly (relative to its container) so
     * it stays visible longer and appears "further away."
     * A common formula is: parallaxOffset = scrollState.value * 0.5f.
     * •
     * If the user scrolls 100px down, the banner only moves 50px up, creating the illusion of depth.
     * 3. Apply the Transformation
     * We would apply this offset to the Banner Header Box using the graphicsLayer modifier. This
     * is more performant than using Modifier.offset because it happens during the "Draw" phase and
     * doesn't trigger a full "Recomposition."
     * **/

    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = Color(0xFF121212)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (animeDetailState.animeId == animeId) {
                // Banner Header - Placed first to be in the background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .graphicsLayer {
                            // Parallax effect: move slower than the scroll
                            // Moves rendering position without affecting layout position.
                            translationY = scrollState.value * 0.5f
                            // Subtle fade out as we scroll
                            alpha = 1f - (scrollState.value / 800f).coerceIn(0f, 1f)
                        }
                ) {
                    if (!animeDetailState.trailerUrl.isNullOrBlank()) {
                        LifecycleAwareVideoPlayer(videoUrl = animeDetailState.trailerUrl!!)
                    } else {
                        AsyncImage(
                            model = animeDetailState.posterImageUrl,
                            contentDescription = animeDetailState.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Bottom Gradient Overlay for Cinematic Transition
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xFF121212).copy(alpha = 0.5f),
                                        Color(0xFF121212)
                                    )
                                )
                            )
                    )
                }

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    // Spacer to push content below the banner initially
                    Spacer(modifier = Modifier.height(300.dp))

                    Column(
                        modifier = Modifier
                            .background(Color(0xFF121212)) // Solid background to cover banner
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        // Title
                        Text(
                            text = animeDetailState.title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Stats Row (Rating & Episodes)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            val ratingValue = animeDetailState.rating?.toString()
                                ?: stringResource(R.string.not_available)
                            val episodesCount = animeDetailState.numberOfEpisodes?.toString()
                                ?: stringResource(R.string.not_available)

                            // Rating
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = ratingValue,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Episodes Badge
                            Surface(
                                color = Color(0xFFE50914),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.episodes_label, episodesCount),
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Synopsis
                        if (!animeDetailState.synopsis.isNullOrBlank()) {
                            Text(
                                text = stringResource(R.string.label_synopsis_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = animeDetailState.synopsis!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f),
                                lineHeight = 22.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Genres as Chips
                        if (animeDetailState.genres.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.label_genres),
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                animeDetailState.genres.forEach { genre ->
                                    AssistChip(
                                        onClick = { },
                                        label = { Text(genre, color = Color.White) },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = Color(0xFF333333)
                                        ),
                                        border = null
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Cast
                        if (animeDetailState.cast.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.label_main_cast),
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = animeDetailState.cast.joinToString(", "),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            } else if (animeDetailState.animeId == 0) {
                AnimeDetailShimmer()
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF121212))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.1f),
                            modifier = Modifier
                                .size(80.dp)
                                .rotate(-15f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.error_loading_details),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.error_connection_issue),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnimeDetailShimmer() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        // Banner Shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .shimmer()
        )

        Column(modifier = Modifier.padding(16.dp)) {
            // Title Shimmer
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(32.dp)
                    .shimmer()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Row Shimmer
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier
                        .size(width = 80.dp, height = 24.dp)
                        .shimmer()
                )
                Box(
                    modifier = Modifier
                        .size(width = 100.dp, height = 24.dp)
                        .shimmer()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Synopsis Title Shimmer
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .height(20.dp)
                    .shimmer()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Synopsis Content Shimmer
            repeat(4) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .padding(vertical = 2.dp)
                        .shimmer()
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(14.dp)
                    .shimmer()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Genres Title Shimmer
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.2f)
                    .height(18.dp)
                    .shimmer()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Chips Shimmer
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(width = 70.dp, height = 32.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .shimmer()
                    )
                }
            }
        }
    }
}
