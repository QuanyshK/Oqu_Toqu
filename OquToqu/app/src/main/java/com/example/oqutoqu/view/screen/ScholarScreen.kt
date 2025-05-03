package com.example.oqutoqu.view.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oqutoqu.R
import com.example.oqutoqu.viewmodel.ScholarViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
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
    val systemUiController = rememberSystemUiController()
    val backgroundColor = colorResource(id = R.color.softBlueBackground)

    SideEffect {
        systemUiController.setStatusBarColor(
            color = backgroundColor,
            darkIcons = true // или false, если у вас светлая иконка на тёмном фоне
        )
    }
    LaunchedEffect(initialQuery) {
        initialQuery?.let {
            viewModel.search(it.trim(), page = 0)
            hasSearched = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.softBlueBackground))
    ) {
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
                    placeholder = { Text("Search articles...") },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = colorResource(id = R.color.primary_blue),
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = colorResource(id = R.color.primary_blue),
                        focusedLabelColor = Color.Gray,
                        unfocusedLabelColor = Color.Gray
                    ),
                    singleLine = true
                )

                Button(
                    onClick = {
                        if (query.isNotBlank()) {
                            viewModel.search(query.trim(), page = 0)
                            hasSearched = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.primary_blue)
                    ),
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
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
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
                        val clipboardManager = LocalClipboardManager.current
                        val uriHandler = LocalUriHandler.current
                        val context = LocalContext.current

                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = item.title ?: "Untitled",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            item.authors?.let {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray)
                                )
                            }

                            item.snippet?.let {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Black)
                                )
                            }

                            item.doi?.let { doi ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "DOI: $doi",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.Blue,
                                        fontSize = 12.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.clickable {
                                        val uri = Uri.parse("oqutoqu://scihub?doi=$doi")
                                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                    }
                                )
                            }

                            item.link?.let { link ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Open in browser",
                                    color = colorResource(id = R.color.primary_blue),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    modifier = Modifier.clickable { uriHandler.openUri(link) }
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
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.primary_blue)
                            )
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
