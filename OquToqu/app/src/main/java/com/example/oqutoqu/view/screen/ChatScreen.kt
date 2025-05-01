package com.example.oqutoqu.view.screen

import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.ChatMessage
import com.example.oqutoqu.viewmodel.ChatViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import com.example.oqutoqu.R
import com.example.oqutoqu.receiver.NetworkReceiver

@Composable
fun ChatScreen(viewModel: ChatViewModel = koinViewModel()) {
    val context = LocalContext.current
    var isConnected by remember { mutableStateOf(true) }
    DisposableEffect(Unit) {
        val receiver = NetworkReceiver { isConnected ->
            viewModel.onConnectionChanged(isConnected)
        }
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        context.registerReceiver(receiver, filter)

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    DisposableEffect(Unit) {
        val receiver = NetworkReceiver { connected ->
            isConnected = connected
        }

        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        context.registerReceiver(receiver, filter)

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    val messages by viewModel.messages.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.setSelectedFile(it) }
    }

    Column(Modifier.fillMaxSize().background(colorResource(id = R.color.background))) {

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(messages) { message ->
                MessageBubble(message)
            }
        }

        LaunchedEffect(messages.size) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size)
            }
        }

        Divider()

        Surface(
            elevation = 10.dp,
            modifier = Modifier.fillMaxWidth(),
            color = Color.White
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (viewModel.selectedFileName != null) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFFDEE8FF), shape = MaterialTheme.shapes.small)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = viewModel.selectedFileName ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearSelectedFile() }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove file")
                        }
                    }
                } else {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Type your message...") },
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp),
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }

                IconButton(onClick = {
                    launcher.launch(arrayOf(
                        "application/pdf",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                    ))
                }) {
                    Icon(Icons.Default.AttachFile, contentDescription = "Attach File")
                }

                Button(
                    onClick = {
                        viewModel.onSend(inputText)
                        inputText = ""
                        viewModel.clearSelectedFile()
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.primary_blue)),
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text("Send", color = Color.White)
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

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = alignment
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = backgroundColor,
            elevation = 4.dp,
            modifier = Modifier
                .padding(vertical = 4.dp)
                .widthIn(max = 320.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                SelectionContainer {
                    val content = if (showText) message.text else message.botResponse
                    Text(
                        text = formatMessageText(content ?: ""),
                        color = textColor,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                message.fileName?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\uD83D\uDCCE $it",
                        color = textColor.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

private fun formatMessageText(raw: String): String {
    return raw
        .replace("*", "")
        .replace(Regex("\\*\\s+"), "\u2022 ")
        .replace("\n", "\n\n")
}


