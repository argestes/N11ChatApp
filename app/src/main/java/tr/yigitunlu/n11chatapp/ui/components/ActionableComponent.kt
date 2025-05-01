package tr.yigitunlu.n11chatapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Component for rendering action buttons with selection animation
 */
@Composable
fun ActionButtons(
    options: List<ActionOption>,
    onActionSelected: (ActionOption) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (options.isEmpty()) return

    // State to track if an option has been selected
    var selectedOptionId by remember { mutableStateOf<String?>(null) }
    // State to track if we should show loading indicator

    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            options.forEachIndexed { index, option ->
                // Only show buttons if no option is selected or this is the selected option
                if (option.isDestructive) {
                    OutlinedButton(
                        onClick = {
                            selectedOptionId = option.actionId
                            // Delay the actual action to show the loading state
                            onActionSelected(option)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        enabled = enabled && selectedOptionId == null
                    ) {
                        Text(text = option.label)
                    }
                } else {
                    Button(
                        onClick = {
                            selectedOptionId = option.actionId
                            // Delay the actual action to show the loading state
                            onActionSelected(option)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (index == 0) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.secondary
                        ),
                        enabled = enabled && selectedOptionId == null
                    ) {
                        Text(text = option.label)
                    }
                }
            }
        }
    }
}

/**
 * Data class representing an action option
 */
data class ActionOption(
    val label: String,
    val actionId: String,
    val isDestructive: Boolean = false
)
