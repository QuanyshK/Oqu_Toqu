// domain/src/test/kotlin/com/example/domain/usecase/GetChatMessagesUseCaseTest.kt

package com.example.domain.usecase

import com.example.domain.model.ChatMessage
import com.example.domain.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetChatMessagesUseCaseTest {

    private lateinit var repository: ChatRepository
    private lateinit var useCase: GetChatMessagesUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetChatMessagesUseCase(repository)
    }

    @Test
    fun `invoke returns messages list from repository`() = runTest {
        // given
        val messages = listOf(
            ChatMessage(
                id = 1L,
                text = "Hello",
                fileName = null,
                botResponse = "Hi!",
                isUser = true,
                userToken = "token123"
            )
        )
        coEvery { repository.getMessages() } returns messages

        // when
        val result = useCase.invoke()

        // then
        assertEquals(messages, result)
    }

    @Test
    fun `invoke returns empty list when repository returns empty`() = runTest {
        // given
        coEvery { repository.getMessages() } returns emptyList()

        // when
        val result = useCase.invoke()

        // then
        assertEquals(emptyList<ChatMessage>(), result)
    }
}
