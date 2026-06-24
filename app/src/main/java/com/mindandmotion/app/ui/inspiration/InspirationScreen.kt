package com.mindandmotion.app.ui.inspiration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mindandmotion.app.data.quotes.QuoteDto
import com.mindandmotion.app.ui.components.AppTopBar
import com.mindandmotion.app.ui.components.EmptyState
import com.mindandmotion.app.ui.components.SectionCard

@Composable
fun InspirationScreen(viewModel: QuotesViewModel) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Inspirație",
                actions = {
                    IconButton(onClick = viewModel::onRefresh) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Reîncarcă")
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                state.errorMessage != null -> {
                    Column(
                        Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.errorMessage.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = viewModel::onRefresh,
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Încearcă din nou")
                        }
                    }
                }

                state.quotes.isEmpty() -> {
                    EmptyState(
                        title = "Niciun citat momentan",
                        subtitle = "Apasă butonul de reîncărcare din header.",
                        icon = Icons.Filled.FormatQuote,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        state.featuredQuote?.let { featured ->
                            item(key = "featured-${featured.id}") {
                                FeaturedQuoteCard(featured)
                            }
                        }
                        items(state.quotes, key = { it.id }) { quote ->
                            QuoteCard(quote)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedQuoteCard(quote: QuoteDto) {
    SectionCard(title = "Citatul zilei") {
        QuoteContent(quote)
    }
}

@Composable
private fun QuoteCard(quote: QuoteDto) {
    SectionCard {
        QuoteContent(quote)
    }
}

@Composable
private fun QuoteContent(quote: QuoteDto) {
    Text(
        text = "\u201C${quote.quote}\u201D",
        style = MaterialTheme.typography.bodyLarge
    )
    Text(
        text = "\u2014 ${quote.author}",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
    )
}
