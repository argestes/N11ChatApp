package tr.yigitunlu.n11chatapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tr.yigitunlu.n11chatapp.AppState
import tr.yigitunlu.n11chatapp.data.local.dao.ChatMessageDao
import tr.yigitunlu.n11chatapp.data.remote.websocket.ChatWebSocketClient
import tr.yigitunlu.n11chatapp.data.repository.ChatRepositoryImpl
import tr.yigitunlu.n11chatapp.domain.repository.ChatRepository
import tr.yigitunlu.n11chatapp.domain.usecase.ChatInteractionUseCase
import tr.yigitunlu.n11chatapp.domain.usecase.GetSampleChatComponentsUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGetSampleChatComponentsUseCase(): GetSampleChatComponentsUseCase {
        return GetSampleChatComponentsUseCase()
    }

    @Provides
    @Singleton
    fun provideChatRepository(
        chatMessageDao: ChatMessageDao,
        webSocketClient: ChatWebSocketClient,
    ): ChatRepository {
        return ChatRepositoryImpl(
            webSocketClient,
            chatMessageDao,
        )
    }

    @Provides
    @Singleton
    fun provideAppState(): AppState {
        return AppState()
    }

    @Provides
    @Singleton
    fun provideChatInteractionUseCase(
        chatRepository: ChatRepository,
        appState: AppState
    ): ChatInteractionUseCase {
        return ChatInteractionUseCase(chatRepository, appState)
    }
}
