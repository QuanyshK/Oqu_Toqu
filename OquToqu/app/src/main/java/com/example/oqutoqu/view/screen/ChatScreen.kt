package com.example.oqutoqu.view.screen

import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ChatMessage
import com.example.oqutoqu.viewmodel.ChatViewModel
import com.example.oqutoqu.R
import com.example.oqutoqu.receiver.NetworkReceiver
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun ChatScreen(viewModel: ChatViewModel = koinViewModel()) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val messages by viewModel.messages.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.setSelectedFile(it) }
    }

    var isConnected by remember { mutableStateOf(true) }
    DisposableEffect(Unit) {
        val receiver = NetworkReceiver { isConnected = it; viewModel.onConnectionChanged(it) }
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        context.registerReceiver(receiver, filter)
        onDispose { context.unregisterReceiver(receiver) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.softBlueBackground))
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                Text(
                    text = "💬 Welcome to Oqu Toqu chat",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 16.dp),
                    color = Color.DarkGray,
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Center
                )
            }
            items(messages) { message ->
                MessageBubble(message)
            }
        }

        LaunchedEffect(messages.lastOrNull()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size)
            }
        }

        Divider()

        Surface(elevation = 8.dp, color = Color.White) {
            Column(Modifier.padding(8.dp)) {
                viewModel.selectedFileName?.let { fileName ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(color = Color(0xFFEAF1FF), shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = null, tint = colorResource(id = R.color.primary_blue))
                        Text(
                            text = fileName,
                            modifier = Modifier.weight(1f).padding(start = 8.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 14.sp
                        )
                        IconButton(onClick = { viewModel.clearSelectedFile() }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove file")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Type your message...") },
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.Transparent, shape = RoundedCornerShape(12.dp)),
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color(0xFFF2F5F9),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = colorResource(id = R.color.primary_blue)
                        ),
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp)
                    )

                    IconButton(onClick = {
                        launcher.launch(arrayOf(
                            "application/pdf",
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                        ))
                    }) {
                        Icon(Icons.Default.AttachFile, contentDescription = "Attach File", tint = colorResource(id = R.color.primary_blue))
                    }

                    IconButton(onClick = {
                        if (inputText.isNotBlank() || viewModel.selectedFileName != null) {
                            viewModel.onSend(inputText)
                            inputText = ""
                            viewModel.clearSelectedFile()
                        }
                    }) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = colorResource(id = R.color.primary_blue))
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val showText = !message.text.isNullOrBlank()
    val showBotResponse = !message.botResponse.isNullOrBlank()
    if (!showText && !showBotResponse) return

    val backgroundColor = if (message.isUser) colorResource(id = R.color.primary_blue) else Color.White
    val textColor = if (message.isUser) Color.White else Color.Black
    val alignment = if (message.isUser) Arrangement.End else Arrangement.Start
    val attachmentColor = if (message.isUser) Color.White.copy(alpha = 0.2f) else Color(0xFFEAF1FF)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = alignment
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = backgroundColor,
            elevation = 4.dp,
            modifier = Modifier
                .padding(vertical = 6.dp, horizontal = 4.dp)
                .widthIn(max = 320.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                SelectionContainer {
                    val content = if (showText) message.text else message.botResponse
                    Text(
                        text = formatMessageText(content ?: ""),
                        color = textColor,
                        style = MaterialTheme.typography.body1,
                        lineHeight = 22.sp
                    )
                }

                if (message.isUser) {
                    message.fileName?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = attachmentColor,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "\uD83D\uDCCE $it",
                                    color = textColor.copy(alpha = 0.9f),
                                    style = MaterialTheme.typography.caption
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatMessageText(raw: String): String {
    return raw
        .replace(Regex("(?<=\\w)\\n(?=\\w)"), " ")
        .replace(Regex("\\n{2,}"), "\n\n")
        .replace(Regex("(?<=[^\\n])\\*\\s"), "\n• ")
        .replace(Regex("\\*{1,}"), "")
        .trim()
}
