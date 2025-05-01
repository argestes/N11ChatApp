package tr.yigitunlu.n11chatapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import tr.yigitunlu.n11chatapp.ui.screen.ChatScreen
import tr.yigitunlu.n11chatapp.ui.screen.FeedbackScreen
import tr.yigitunlu.n11chatapp.ui.theme.N11ChatTheme
import tr.yigitunlu.n11chatapp.ui.viewmodel.Screen
import javax.inject.Inject
import javax.inject.Singleton

@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            N11ChatTheme {
                MainScreen(viewModel())
            }
        }
    }
}

@Composable
private fun MainScreen(viewModel: MainViewModel) {
    val screen by viewModel.screen
    when (screen) {
        Screen.Chat -> ChatScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        )

        Screen.Feedback -> FeedbackScreen()
    }
}


@HiltViewModel
class MainViewModel @Inject constructor(
    private val appState: AppState
) : ViewModel() {
    val screen = appState.screen
}

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val appState: AppState
) : ViewModel() {

    fun navigateToChat() {
        appState.navigateTo(Screen.Chat)
    }
}

@Singleton
class AppState {
    val screen = mutableStateOf<Screen>(Screen.Chat)

    fun navigateTo(screen: Screen) {
        this.screen.value = screen
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    N11ChatTheme {
        ChatScreen(viewModel = tr.yigitunlu.n11chatapp.ui.preview.previewChatViewModel())
    }
}