package tr.yigitunlu.n11chatapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tr.yigitunlu.n11chatapp.FeedbackViewModel
import tr.yigitunlu.n11chatapp.MainViewModel
import tr.yigitunlu.n11chatapp.ui.components.ChatUiComponent
import tr.yigitunlu.n11chatapp.ui.viewmodel.ChatViewModel

@Preview
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val chatComponents by viewModel.chatComponents.collectAsState()
    val conversationTerminated by viewModel.conversationTerminated.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(0.6f)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                ChatComponentList(
                    components = chatComponents,
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (conversationTerminated) {
                Box(
                    modifier = Modifier
                        .weight(0.4f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Sohbet sonlandırıldı. Bize ulaştığınız için teşekkür ederiz",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Button(
                            onClick = {
                                viewModel.onClickReconnect()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            ),
                        ) {
                            Text(text = "Yeni sohbet")
                        }

                        Button(
                            onClick = {
                                viewModel.onClickFeedback()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                        ) {
                            Text(
                                text = "Geri bildirim"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeedbackScreen(
    modifier: Modifier = Modifier,
    viewModel: FeedbackViewModel = viewModel()
) {
    var feedbackText by remember { mutableStateOf("") }
    var showThankYouMessage by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Geri bildiriminizi duymak isteriz!",
            style = MaterialTheme.typography.headlineLarge
        )

        OutlinedTextField(
            value = feedbackText,
            onValueChange = { feedbackText = it },
            label = { Text("Geri bildiriminiz") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp) // Adjust height as needed
        )

        Button(
            onClick = {

                // TODO: Implement logic to send feedback (e.g., to a backend service)
                // For now, we'll just show a thank you message
                showThankYouMessage = true

                coroutineScope.launch {
                    delay(1000)
                    viewModel.navigateToChat()
                }
            },
            enabled = feedbackText.isNotBlank() // Enable button only when there's text
        ) {
            Text("Geri Bildirim Gönder")
        }

        if (showThankYouMessage) {
            Text(
                text = "Geri bildiriminiz için teşekkür ederiz!",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun ChatComponentList(
    components: List<ChatUiComponent>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = modifier.padding(16.dp)
    ) {
        items(components) { component ->
            with(this) {
                with(component) {
                    Render(Modifier)
                }
            }
        }
    }

    LaunchedEffect(components.size) {
        if (components.isNotEmpty()) {
            listState.animateScrollToItem(components.size - 1)
        }
    }
}


