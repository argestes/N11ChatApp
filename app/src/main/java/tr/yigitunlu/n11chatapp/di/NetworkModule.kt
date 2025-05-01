package tr.yigitunlu.n11chatapp.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import tr.yigitunlu.n11chatapp.data.remote.dto.ChatStepDto
import tr.yigitunlu.n11chatapp.data.remote.dto.ChatStepDtoDeserializer
import tr.yigitunlu.n11chatapp.data.remote.websocket.ChatWebSocketClient
import tr.yigitunlu.n11chatapp.data.remote.websocket.ChatWebSocketServiceImpl
import java.io.BufferedReader
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(ChatStepDto::class.java, ChatStepDtoDeserializer())
            .create()
    }

    @Provides
    @Singleton
    @Named("websocket_url")
    fun provideWebSocketUrl(): String {
        return "wss://echo.websocket.org"
    }

    @Provides
    @Singleton
    fun provideWebSocketClient(
        okHttpClient: OkHttpClient,
        @Named("websocket_url") websocketUrl: String,
        @ApplicationContext context: Context,
        gson: Gson
    ): ChatWebSocketClient {

        val jsonText = context.assets.open("live_support_flow.json").bufferedReader().use(
            BufferedReader::readText
        )

        return ChatWebSocketServiceImpl(okHttpClient, websocketUrl, jsonText, gson)
    }
}
