package com.advice.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.advice.core.local.Conference
import com.advice.core.ui.HomeState
import com.advice.ui.components.home.*
import com.advice.ui.preview.LightDarkPreview
import com.advice.ui.theme.ScheduleTheme
import com.advice.ui.theme.roundedCornerShape
import com.advice.ui.components.ProgressSpinner
import java.util.*

@Composable
fun HomeScreen(
    state: HomeState?,
    onConferenceClick: (Conference) -> Unit,
    onNavigationClick: (String) -> Unit,
) {
    Scaffold(
        topBar = { ConferenceSelector(state as? HomeState.Loaded, onConferenceClick) },
        modifier = Modifier.clip(roundedCornerShape)
    ) { contentPadding ->
        HomeScreenContent(
            state,
            onNavigationClick,
            modifier = Modifier
                .padding(contentPadding)
        )
    }
}

@Composable
fun HomeScreenContent(
    state: HomeState?,
    onNavigationClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        when (state) {
            is HomeState.Error -> {
            }

            is HomeState.Loaded -> {
                HomeScreen(state, onNavigationClick)
            }

            HomeState.Loading -> {
                ProgressSpinner()
            }

            null -> {
            }
        }

    }
}

@Composable
private fun HomeScreen(state: HomeState.Loaded, onNavigationClick: (String) -> Unit) {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        ConferenceView(state.conference)
        if (state.hasWifi) {
            WiFiCard(onConnectClicked = {})
        }

        if (state.hasProducts) {
            ProductCard(media = state.productExample) {
                onNavigationClick("merch")
            }
        }

        val remainder = state.countdown
        if (remainder > 0L) {
            CountdownView(remainder)
        }

        // Latest news
        if (state.news.isNotEmpty()) {
            state.news.first().let {
                ArticleView(text = it.text, date = it.date)
            }
            if (state.news.size > 1) {
                HomeCard {
                    Text("News",
                        Modifier
                            .clickable {
                                onNavigationClick("news")
                            }
                            .padding(16.dp))
                }
            }
        }


        if (state.documents.isNotEmpty()) {
            Text(
                "Documents",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
        }

        state.documents.forEach {
            HomeCard {
                Text(it.title,
                    Modifier
                        .clickable {
                            onNavigationClick("document/${it.id}")
                        }
                        .padding(16.dp))
            }
        }

        Text(
            "Other",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )

        HomeCard {
            Text("Speakers",
                Modifier
                    .clickable {
                        onNavigationClick("speakers")
                    }
                    .padding(16.dp))
        }
        HomeCard {
            Text("Vendors",
                Modifier
                    .clickable {
                        onNavigationClick("vendors")
                    }
                    .padding(16.dp))
        }
        HomeCard {
            Text("Villages",
                Modifier
                    .clickable {
                        onNavigationClick("villages")
                    }
                    .padding(16.dp))
        }
        HomeCard {
            Text("Locations",
                Modifier
                    .clickable {
                        onNavigationClick("locations")
                    }
                    .padding(16.dp))
        }
        HomeCard {
            Text("FAQ",
                Modifier
                    .clickable {
                        onNavigationClick("faq")
                    }
                    .padding(16.dp))
        }


        // Required spacer to push content above the bottom bar
        Spacer(Modifier.height(64.dp))
    }
}

@LightDarkPreview
@Composable
fun HomeScreenViewPreview() {
    ScheduleTheme {
        HomeScreen(
            state = HomeState.Loaded(
                conferences = listOf(Conference.Zero),
                conference = Conference.Zero,
                documents = emptyList(),
                news = emptyList(),
                countdown = Date().time / 1000L,
                forceTimeZone = false
            ), {}, {})
    }
}
