package com.example.oqutoqu.view.fragment

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.domain.model.ChatMessage
import com.example.oqutoqu.viewmodel.ChatViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ChatScreen(viewModel: ChatViewModel = koinViewModel()) {
    val messages by viewModel.messages.collectAsState()
    var input by remember { mutableStateOf("") }
    var fileUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                Log.d("ChatScreen", "File selected: $uri")
                fileUri = uri
                fileName = getFileName(context, uri)
            } else {
                Log.e("ChatScreen", "File selection cancelled or failed")
            }
        }
    )


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(5.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(messages) { msg ->
                MessageBubble(msg)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (fileName != null) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = fileName ?: "",
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                    IconButton(onClick = {
                        fileUri = null
                        fileName = null
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Remove File")
                    }
                }
            } else {
                TextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Type a message...") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.width(8.dp))
            IconButton(onClick = {
                try {
                    filePickerLauncher.launch(
                        arrayOf("application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                    )
                } catch (e: Exception) {
                    Log.e("ChatScreen", "Error launching file picker", e)
                }
            }) {
                Icon(Icons.Default.AttachFile, contentDescription = "Attach File")
            }
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                try {
                    viewModel.onSend(input, fileUri?.toString())
                    input = ""
                    fileUri = null
                    fileName = null
                } catch (e: Exception) {
                    Log.e("ChatScreen", "Error sending message", e)
                }
            }) {
                Text("Send")
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val bg = if (message.isUser)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)

    val tc = if (message.isUser)
        MaterialTheme.colorScheme.onPrimary
    else
        MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .padding(vertical = 2.dp),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            backgroundColor = bg,
            elevation = 2.dp
        ) {
            Text(
                text = message.text.orEmpty(),
                color = tc,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}
private fun getFileName(context: Context, uri: Uri): String {
    val returnCursor = context.contentResolver.query(uri, null, null, null, null)
    return returnCursor?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && nameIndex >= 0) {
            cursor.getString(nameIndex) ?: uri.lastPathSegment ?: "file"
        } else {
            uri.lastPathSegment ?: "file"
        }
    } ?: (uri.lastPathSegment ?: "file")
}

