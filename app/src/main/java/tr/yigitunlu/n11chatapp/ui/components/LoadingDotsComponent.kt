package tr.yigitunlu.n11chatapp.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * A loading component that displays animated dots (e.g., "processing.", "processing..", "processing...")
 * Implements ChatUiComponent for use in chat flows
 */
class LoadingDotsComponent(
    override val id: String,
    private val text: String = "processing"
) : ChatUiComponent {
    override val type = ChatComponentType.SYSTEM_MESSAGE
    
    @Composable
    override fun LazyItemScope.Render(modifier: Modifier) {
        var dotCount by remember { mutableIntStateOf(0) }
        val dots = ".".repeat((dotCount % 4))
        
        // Animation cycle
        LaunchedEffect(key1 = true) {
            while (true) {
                delay(500) // Change dots every 500ms
                dotCount++
            }
        }
        
        Box(
            modifier = modifier.padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$text$dots",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
