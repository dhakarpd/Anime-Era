package com.github.dhakarpd.animeera.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.github.dhakarpd.animeera.presentation.common.shimmer
import com.github.dhakarpd.animeera.presentation.navigation.Screen
import com.github.dhakarpd.animeera.presentation.navigation.animeNavGraph
import com.github.dhakarpd.animeera.ui.theme.AnimeEraTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            AnimeEraTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    NavHost(
                        navController = navController,
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        startDestination = Screen.AnimeList.route
                    ) {
                        animeNavGraph(navController)
                    }
                }
            }
        }
    }
}

@Composable
fun MovieShimmerItem() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp, 120.dp)
                .clip(RoundedCornerShape(8.dp))
                .shimmer()
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .fillMaxWidth(0.7f)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .height(14.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .height(14.dp)
                    .fillMaxWidth(0.5f)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer()
            )
        }
    }
}