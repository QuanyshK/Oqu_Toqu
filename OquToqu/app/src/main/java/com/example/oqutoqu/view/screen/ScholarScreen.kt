package com.example.oqutoqu.view.screen

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.oqutoqu.R
import com.example.oqutoqu.viewmodel.ScholarViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ScholarScreen(
    viewModel: ScholarViewModel = koinViewModel(),
    initialQuery: String? = null
) {
    val items by viewModel.items.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var query by remember { mutableStateOf(initialQuery ?: "") }
    var hasSearched by remember { mutableStateOf(false) }

    LaunchedEffect(initialQuery) {
        initialQuery?.let {
            viewModel.search(it.trim(), page = 0)
            hasSearched = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEAF1FF))
    ) {
        Divider()

        Surface(
            shadowElevation = 6.dp,
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search...") },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .background(
                            color = Color.White,
                            shape = MaterialTheme.shapes.medium
                        ),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = Color.Gray,
                        unfocusedLabelColor = Color.Gray
                    )
                )

                Button(
                    onClick = {
                        if (query.isNotBlank()) {
                            viewModel.search(query.trim(), page = 0)
                            hasSearched = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.primary_blue)),
                    modifier = Modifier
                        .height(56.dp)
                        .padding(start = 8.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Search", color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading && items.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
            ) {
                items(items) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        elevation = CardDefaults.cardElevation(4.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val clipboardManager = LocalClipboardManager.current
                            val uriHandler = LocalUriHandler.current
                            val context = LocalContext.current

                            Text(
                                text = item.title ?: "Without Title",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            )
                            item.authors?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall.copy(color = Color.Black))
                            }
                            item.snippet?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall.copy(color = Color.Black))
                            }

                            item.doi?.let { doi ->
                                Text(
                                    text = "DOI: $doi",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.Black,
                                        fontSize = 12.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.clickable {
                                        val uri = Uri.parse("oqutoqu://scihub?doi=$doi")
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        context.startActivity(intent)
                                    }
                                )
                            }

                            item.link?.let { link ->
                                Text(
                                    text = "Open in Web",
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .clickable {
                                            uriHandler.openUri(link)
                                        }
                                )
                            }
                        }
                    }
                }

                if (items.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.loadNextPage() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.primary_blue))
                        ) {
                            Text("Load More", color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}
