package tr.yigitunlu.n11chatapp.ui.preview

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import tr.yigitunlu.n11chatapp.domain.usecase.GetSampleChatComponentsUseCase
import tr.yigitunlu.n11chatapp.ui.viewmodel.ChatViewModel

/**
 * Provides a preview version of ChatViewModel for use in Compose previews
 */
@Composable
fun previewChatViewModel(): ChatViewModel {
    return viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
//                    return ChatViewModel(GetSampleChatComponentsUseCase()) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    )
}
