package tr.yigitunlu.n11chatapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent

/**
 * Server text message component implementing ChatUiComponent
 */
class ServerMessageComponent(
    override val id: String,
    private val message: String,
    private val actions: List<ActionOption>? = null,
    private val imageUrl: String? = null,
    private val onActionSelected: ((ActionOption) -> Unit)? = null,
    private val isLatestAction: Boolean = false
) : ChatUiComponent {
    override val type = ChatComponentType.SERVER_MESSAGE

    @Composable
    override fun LazyItemScope.Render(modifier: Modifier) {
        Column(modifier = modifier.fillMaxWidth(0.8f)) {
            // Message bubble
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(12.dp)
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )


                // Image if provided
                if (imageUrl != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        SubcomposeAsyncImage(
                            model = imageUrl,
                            contentDescription = "Message image",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier.fillMaxWidth(),
                            loading = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.align(Alignment.Center),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            error = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .background(MaterialTheme.colorScheme.errorContainer)
                                ) {
                                    Text(
                                        text = "Image could not be loaded",
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            },
                            success = {
                                SubcomposeAsyncImageContent()
                            }
                        )
                    }
                }

                // Action buttons if provided
                if (!actions.isNullOrEmpty() && onActionSelected != null) {
                    ActionButtons(
                        options = actions,
                        onActionSelected = onActionSelected,
                        enabled = isLatestAction,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

        }
    }
}

