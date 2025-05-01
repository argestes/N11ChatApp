package tr.yigitunlu.n11chatapp.domain.usecase

import tr.yigitunlu.n11chatapp.ui.components.ActionOption
import tr.yigitunlu.n11chatapp.ui.components.ChatUiComponent
import tr.yigitunlu.n11chatapp.ui.components.LoadingDotsComponent
import tr.yigitunlu.n11chatapp.ui.components.ServerMessageComponent
import tr.yigitunlu.n11chatapp.ui.components.UserAnswerComponent
import javax.inject.Inject

/**
 * Use case for generating sample chat components for testing and preview
 */
class GetSampleChatComponentsUseCase @Inject constructor() {
    
    /**
     * Generates a list of sample chat components
     * @param onActionSelected Callback for when an action is selected
     * @return List of chat components
     */
    operator fun invoke(onActionSelected: (ActionOption) -> Unit): List<ChatUiComponent> {
        return listOf(
            // Welcome message with action buttons
            ServerMessageComponent(
                id = "1",
                message = "Merhaba, canlı destek hattına hoş geldiniz! Hangi konuda yardım almak istersiniz?",
                actions = listOf(
                    ActionOption("İade işlemi", "step_2"),
                    ActionOption("Sipariş durumu", "step_3"),
                    ActionOption("Ürün rehberi", "step_4"),
                    ActionOption("Sohbeti bitir", "end_conversation", isDestructive = true)
                ),
                onActionSelected = onActionSelected
            ),
            
            // User's selected action
            UserAnswerComponent(
                id = "2",
                option = ActionOption("İade işlemi", "step_2")
            ),
            
            // Return process question with action buttons
            ServerMessageComponent(
                id = "3",
                message = "İade işlemleri için ürününüzü kargoya verdiniz mi?",
                actions = listOf(
                    ActionOption("Evet, kargoya verdim", "step_5"),
                    ActionOption("Hayır, henüz vermedim", "step_6"),
                    ActionOption("Sohbeti bitir", "end_conversation", isDestructive = true)
                ),
                onActionSelected = onActionSelected
            ),
            
            // User's selected action
            UserAnswerComponent(
                id = "4",
                option = ActionOption("Evet, kargoya verdim", "step_5")
            ),
            
            // Product guide with image
            ServerMessageComponent(
                id = "5",
                message = "Ürün rehberi için aşağıdaki görsele bakabilirsiniz:",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/7/70/Example.png"
            ),
            
            // Thank you message with next steps
            ServerMessageComponent(
                id = "6",
                message = "Teşekkür ederiz! İade işleminiz kargoya ulaştığında işleme alınacaktır.",
                actions = listOf(
                    ActionOption("Başka bir sorum var", "step_1"),
                    ActionOption("Sohbeti bitir", "end_conversation", isDestructive = true)
                ),
                onActionSelected = onActionSelected,
                isLatestAction = false
            ),
            
            // Loading indicator as a chat component
            LoadingDotsComponent(
                id = "loading_thinking",
                text = "Düşünüyor"
            )
        )
    }
}
