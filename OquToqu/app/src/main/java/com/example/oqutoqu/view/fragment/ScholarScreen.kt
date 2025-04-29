package com.example.oqutoqu.view.fragment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.oqutoqu.viewmodel.ScholarViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ScholarScreen(viewModel: ScholarViewModel = koinViewModel()) {
    val items by viewModel.items.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var query by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search..") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (query.isNotBlank()) {
                    viewModel.search(query.trim(), page = 0)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Search")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading && items.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(items) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(item.title ?: "Without Title", style = MaterialTheme.typography.titleMedium)
                            item.authors?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall)
                            }
                            item.snippet?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall)
                            }
                            val clipboardManager = LocalClipboardManager.current

                            item.doi?.let { doi ->
                                Text(
                                    text = "DOI: $doi",
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .clickable {
                                            clipboardManager.setText(AnnotatedString(doi))
                                        }
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.loadNextPage() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Load More")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

