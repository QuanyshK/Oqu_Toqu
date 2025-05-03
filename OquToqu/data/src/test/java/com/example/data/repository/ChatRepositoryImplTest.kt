package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.ChatListResponse
import com.example.data.model.ChatResponse
import com.example.data.source.remote.AuthApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.Response

class ChatRepositoryImplTest {

    private lateinit var context: Context
    private lateinit var authApi: AuthApi
    private lateinit var repository: ChatRepositoryImpl

    @Before
    fun setup() {
        context = mock()
        authApi = mock()
        val sharedPrefs = mock<SharedPreferences>()
        whenever(context.getSharedPreferences(any(), any())).thenReturn(sharedPrefs)
        repository = ChatRepositoryImpl(context, authApi)
    }

    @Test
    fun `getMessages returns converted list of ChatMessage`() = runTest {
        val mockList = listOf(
            ChatResponse(1, 1, "Hello", "Hi!", "file1.pdf"),
            ChatResponse(2, 1, "What?", "Nothing", null)
        )
        whenever(authApi.getMessages()).thenReturn(Response.success(ChatListResponse(mockList)))

        val result = repository.getMessages()

        assert(result.size == 4)
        assert(result[0].isUser)
        assert(!result[1].isUser)
    }
}
