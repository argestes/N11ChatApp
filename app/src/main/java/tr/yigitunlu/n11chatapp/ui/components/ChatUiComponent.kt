package tr.yigitunlu.n11chatapp.ui.components

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Interface for all chat UI components
 * This will be implemented by server message components and user choice components
 */
interface ChatUiComponent {
    /**
     * Renders the component in the UI
     * @param modifier Modifier for styling and layout
     */
    @Composable
    fun LazyItemScope.Render(modifier: Modifier)
    
    /**
     * Unique identifier for the component
     */
    val id: String
    
    /**
     * Component type
     */
    val type: ChatComponentType
}

/**
 * Enum defining the types of chat components
 */
enum class ChatComponentType {
    SERVER_MESSAGE,
    USER_ACTION_PICKER,
    USER_PICKED_ACTION,
    SYSTEM_MESSAGE
}
