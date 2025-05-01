package tr.yigitunlu.n11chatapp.data.remote.websocket

import com.google.gson.GsonBuilder
import io.mockk.Call
import io.mockk.MockKAnswerScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import tr.yigitunlu.n11chatapp.data.remote.dto.ChatStepDto
import tr.yigitunlu.n11chatapp.data.remote.dto.ChatStepDtoDeserializer
import java.util.concurrent.TimeUnit

suspend fun main(args: Array<String>) {
    val clinet = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    val json = "[\n" +
            "    {\n" +
            "        \"step\": \"step_1\",\n" +
            "        \"type\": \"button\",\n" +
            "        \"content\": {\n" +
            "            \"text\": \"Merhaba, canlı destek hattına hoş geldiniz! Hangi konuda yardım almak istersiniz?\",\n" +
            "            \"buttons\": [\n" +
            "                {\n" +
            "                    \"label\": \"İade işlemi\",\n" +
            "                    \"action\": \"step_2\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"label\": \"Sipariş durumu\",\n" +
            "                    \"action\": \"step_3\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"label\": \"Ürün rehberi\",\n" +
            "                    \"action\": \"step_4\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"label\": \"Sohbeti bitir\",\n" +
            "                    \"action\": \"end_conversation\"\n" +
            "                }\n" +
            "            ]\n" +
            "        },\n" +
            "        \"action\": \"await_user_choice\"\n" +
            "    },\n" +
            "    {\n" +
            "        \"step\": \"step_2\",\n" +
            "        \"type\": \"button\",\n" +
            "        \"content\": {\n" +
            "            \"text\": \"İade işlemleri için ürününüzü kargoya verdiniz mi?\",\n" +
            "            \"buttons\": [\n" +
            "                {\n" +
            "                    \"label\": \"Evet, kargoya verdim\",\n" +
            "                    \"action\": \"step_5\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"label\": \"Hayır, henüz vermedim\",\n" +
            "                    \"action\": \"step_6\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"label\": \"Sohbeti bitir\",\n" +
            "                    \"action\": \"end_conversation\"\n" +
            "                }\n" +
            "            ]\n" +
            "        },\n" +
            "        \"action\": \"await_user_choice\"\n" +
            "    },\n" +
            "    {\n" +
            "        \"step\": \"step_3\",\n" +
            "        \"type\": \"text\",\n" +
            "        \"content\": \"Siparişiniz şu anda kargoda. Takip numaranız: TR123456789\",\n" +
            "        \"action\": \"end_conversation\"\n" +
            "    },\n" +
            "    {\n" +
            "        \"step\": \"step_4\",\n" +
            "        \"type\": \"image\",\n" +
            "        \"content\": \"https://example.com/urun-rehberi.png\",\n" +
            "        \"action\": \"show_guide\"\n" +
            "    },\n" +
            "    {\n" +
            "        \"step\": \"step_5\",\n" +
            "        \"type\": \"text\",\n" +
            "        \"content\": \"Teşekkür ederiz! İade işleminiz kargoya ulaştığında işleme alınacaktır.\",\n" +
            "        \"action\": \"end_conversation\"\n" +
            "    },\n" +
            "    {\n" +
            "        \"step\": \"step_6\",\n" +
            "        \"type\": \"text\",\n" +
            "        \"content\": \"İade işlemi için ürünü kargoya verdikten sonra işlemler başlatılacaktır. Yardıma ihtiyacınız olursa bizimle iletişime geçebilirsiniz.\",\n" +
            "        \"action\": \"end_conversation\"\n" +
            "    },\n" +
            "    {\n" +
            "        \"step\": \"step_7\",\n" +
            "        \"type\": \"button\",\n" +
            "        \"content\": {\n" +
            "            \"text\": \"Başka nasıl yardımcı olabilirim?\",\n" +
            "            \"buttons\": [\n" +
            "                {\n" +
            "                    \"label\": \"Yeni bir işlem başlat\",\n" +
            "                    \"action\": \"step_1\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"label\": \"Sohbeti bitir\",\n" +
            "                    \"action\": \"end_conversation\"\n" +
            "                }\n" +
            "            ]\n" +
            "        },\n" +
            "        \"action\": \"await_user_choice\"\n" +
            "    }\n" +
            "]\n"


    val gson = GsonBuilder()
        .registerTypeAdapter(ChatStepDto::class.java, ChatStepDtoDeserializer())
        .create()


    val socketClient = ChatWebSocketServiceImpl(
        clinet,
        "wss://echo.websocket.org",
        json,
        gson
    )

    withContext(Dispatchers.Default) {
        async {
            socketClient.connect().flowOn(Dispatchers.Default).collectLatest {
                println(it)
            }
        }
        while (true) {
            println("Reading line")
            val message = readln()
            socketClient.sendStep(message)
        }
    }
}

private fun mockLogger(isError: Boolean = false): MockKAnswerScope<Int, Int>.(Call) -> Int =
    { c ->
        val tag: String = c.invocation.args[0] as String
        val message: String = c.invocation.args[1] as String
        val throwable: Throwable? = c.invocation.args[2] as? Throwable?

        val stream = if (isError) {
            System.err
        } else {
            System.out
        }

        stream.println("$tag $message")
        throwable?.printStackTrace(stream)
        0
    }
