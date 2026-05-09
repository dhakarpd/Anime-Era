package com.github.dhakarpd.animeera.presentation.animeList

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.github.dhakarpd.animeera.R
import com.github.dhakarpd.animeera.domain.model.SyncStatus
import com.github.dhakarpd.animeera.presentation.common.shimmer
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeListScreen(
    animeListScreenViewModel: AnimeListScreenViewModel = hiltViewModel(),
    onAnimeClick: (Int) -> Unit
) {
    val animePagingItems = animeListScreenViewModel.animePager.collectAsLazyPagingItems()
    val syncStatus = animeListScreenViewModel.syncStatus.collectAsState()
    /**
     * the combination of nestedScroll and scrollBehavior is used to create a dynamic interaction
     * between the Top Bar and the scrolling grid. This is a standard Material 3 pattern that makes
     * the UI feel more integrated and "alive."
     * Here is the breakdown of how they work together:
     * 1. val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
     * This variable acts as a state controller.
     * •
     * The Job: It tracks the current scroll offset (how far the user has scrolled) and determines
     * how the Top Bar should react.
     * •
     * Pinned Behavior: Since we used pinnedScrollBehavior(), the Top Bar will stay at the top
     * (pinned), but its appearance will change. For example, when you start scrolling, the Top Bar
     * typically changes its container color or elevation to show that content is moving underneath it.
     * 2. Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
     * This modifier is applied to the Scaffold. It acts as a bridge/listener.
     * •
     * The Job: By default, a scrollable list (like LazyVerticalGrid) "consumes" all scroll events.
     * Without nestedScroll, the Top Bar would never know that the user is scrolling.
     * •
     * Nested Scroll Connection: This connects the scroll events happening
     * inside the LazyVerticalGrid to the scrollBehavior we defined. It says: "Hey Top Bar, the
     * grid just scrolled 10 pixels; you might want to update your transparency or elevation."
     * 3. scrollBehavior = scrollBehavior (Inside TopAppBar)
     * Finally, we pass that state back into the TopAppBar composable.
     * •
     * The Job: This allows the TopAppBar to actually apply the visual changes (like the background
     * color shift) based on the data it receives from the nested scroll bridge.
     * What functionality is it offering the user?
     * 1. Visual Hierarchy: When the user is at the very top of the list, the Top Bar is often
     * transparent or matches the background exactly. As soon as they scroll down, the Top Bar
     * becomes slightly more opaque (using the scrolledContainerColor we set: Color(0xFF121212)).
     * This clearly separates the "System UI/Navigation" from the "Content."
     * 2. Continuity: It provides a smooth transition. Instead of the Top Bar suddenly snapping to a
     * different color, it fades in/out based on the scroll position.
     * 3. Space Efficiency: While you are using a pinned behavior, other types
     * (like enterAlwaysScrollBehavior) would actually hide the Top Bar when you scroll
     * down to give more room for posters, and show it again the moment you scroll up.
     * In short: nestedScroll is the "sensor" that detects movement, and scrollBehavior
     * is the "brain" that tells the Top Bar how to look based on that movement.
     * **/
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color(0xFF121212), // Cinematic Dark Background
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.anime_list_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                actions = {
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
                            contentDescription = stringResource(R.string.syncing_desc),
                            tint = Color.White,
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .rotate(angle)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF121212).copy(alpha = 0.95f),
                    scrolledContainerColor = Color(0xFF121212),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        val loadState = animePagingItems.loadState
        val isInitialLoad = loadState.refresh is LoadState.Loading
        val isError = loadState.refresh is LoadState.Error && animePagingItems.itemCount == 0

        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            if (isInitialLoad) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(8) {
                        AnimeListItemShimmer()
                    }
                }

            } else if (isError) {
                FullScreenError(message = stringResource(R.string.error_fetch_data)) {
                    animePagingItems.refresh()
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(animePagingItems.itemCount) { index ->
                        val anime = animePagingItems[index]
                        if (anime != null) {
                            AnimeListItem(
                                anime = anime,
                                isOnline = animeListScreenViewModel.getDeviceOnlineStatus(),
                                onClick = {
                                    onAnimeClick(anime.id)
                                })
                        }
                    }
                    item(span = { GridItemSpan(2) }) {
                        when (val appendLoadState = animePagingItems.loadState.append) {
                            is LoadState.Loading -> {
                                AnimeListItemShimmer()
                            }
                            is LoadState.Error -> {
                                Text(
                                    text = stringResource(R.string.error_generic),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(16.dp).fillMaxWidth()
                                )
                            }
                            is LoadState.NotLoading -> {
                                if (appendLoadState.endOfPaginationReached && animePagingItems.itemCount > 0) {
                                    Text(
                                        text = stringResource(R.string.end_of_list),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(16.dp).fillMaxWidth()
                                    )
                                }
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
    isOnline: Boolean,
    onClick: () -> Unit
) {
    val episodes = anime.numberOfEpisodes?.toString() ?: stringResource(R.string.not_available)
    val rating = anime.rating?.toString() ?: stringResource(R.string.not_available)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E), // Slightly lighter than background
            contentColor = Color.White
        )
    ) {
        Column {
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(anime.posterImageUrl)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        // When Online -> Network requests allowed; Cache can be updated
                        // When Offline -> No network call; Also disables reading from network layer cache
                        // But still reads from disk cache
                        .networkCachePolicy(
                            if (isOnline) CachePolicy.ENABLED
                            else CachePolicy.DISABLED
                        )
                        .crossfade(true)
                        .build(),
                    contentDescription = anime.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f / 3f)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                )

                // Rating Badge
                if (anime.rating != null) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(bottomStart = 8.dp),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(
                                text = rating,
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Episode Badge
                Surface(
                    color = Color(0xFFE50914).copy(alpha = 0.85f), // Netflix Red
                    shape = RoundedCornerShape(topEnd = 8.dp),
                    modifier = Modifier.align(Alignment.BottomStart)
                ) {
                    Text(
                        text = stringResource(R.string.episodes_label, episodes),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp
                    )
                }
            }

            Text(
                text = anime.title,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(8.dp),
                fontWeight = FontWeight.SemiBold,
                minLines = 2
            )
        }
    }
}

@Composable
fun AnimeListItemShimmer() {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .shimmer()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(16.dp)
                    .padding(horizontal = 8.dp)
                    .shimmer()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(16.dp)
                    .padding(start = 8.dp, bottom = 8.dp)
                    .shimmer()
            )
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
                Text(stringResource(R.string.retry_button))
            }
        }
    }
}
