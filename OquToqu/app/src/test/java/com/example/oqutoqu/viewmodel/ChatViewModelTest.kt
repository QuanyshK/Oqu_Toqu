package com.example.oqutoqu.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.source.local.ChatDao
import com.example.domain.model.ChatMessage
import com.example.domain.usecase.GetChatMessagesUseCase
import com.example.domain.usecase.SendChatMessageUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var contentResolver: ContentResolver
    private lateinit var cursor: Cursor
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkInfo: NetworkInfo
    private lateinit var getMessages: GetChatMessagesUseCase
    private lateinit var sendMessage: SendChatMessageUseCase
    private lateinit var dao: ChatDao
    private lateinit var viewModel: ChatViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        context = mock()
        sharedPreferences = mock()
        contentResolver = mock()
        cursor = mock()
        connectivityManager = mock()
        networkInfo = mock()
        getMessages = mock()
        sendMessage = mock()
        dao = mock()

        whenever(context.getSharedPreferences(any(), any())).thenReturn(sharedPreferences)
        whenever(context.contentResolver).thenReturn(contentResolver)
        whenever(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(connectivityManager)
        whenever(connectivityManager.activeNetworkInfo).thenReturn(networkInfo)
        whenever(networkInfo.isConnectedOrConnecting).thenReturn(true)
        runTest {
            whenever(getMessages()).thenReturn(emptyList())
        }
        viewModel = ChatViewModel(
            context = context,
            getMessages = getMessages,
            sendMessage = sendMessage,
            chatDao = dao,
            coroutineDispatcher = testDispatcher
        )
    }

    @Test
    fun `onSend does nothing when input is blank and no file`() = runTest {
        viewModel.onSend("")
        testScheduler.advanceUntilIdle()
        assert(viewModel.messages.value.isEmpty())
    }

    @Test
    fun `onSend adds message when input is valid`() = runTest {
        viewModel.onSend("Hello")
        testScheduler.advanceUntilIdle()
        val messages = viewModel.messages.value
        assert(messages.any { it.text == "Hello" && it.isUser })
    }

    @Test
    fun `setSelectedFile sets uri and filename from fallback`() {
        val uri = mock<Uri>()
        whenever(uri.lastPathSegment).thenReturn("fallback.txt")
        viewModel.setSelectedFile(uri)
        assert(viewModel.selectedFileName == "fallback.txt")
    }

    @Test
    fun `setSelectedFile sets filename from cursor`() {
        val uri = mock<Uri>()
        whenever(uri.lastPathSegment).thenReturn("fallback.txt")

        whenever(contentResolver.query(any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()))
            .thenReturn(cursor)
        whenever(cursor.moveToFirst()).thenReturn(true)
        whenever(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)).thenReturn(0)
        whenever(cursor.getString(0)).thenReturn("mock_file.txt")

        viewModel.setSelectedFile(uri)
        assert(viewModel.selectedFileName == "mock_file.txt")
    }
}
