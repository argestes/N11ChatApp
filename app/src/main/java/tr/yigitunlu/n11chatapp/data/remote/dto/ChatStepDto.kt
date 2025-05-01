package tr.yigitunlu.n11chatapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Base DTO for all chat steps received from WebSocket
 */
public sealed class ChatStepDto {
    abstract val step: String
    abstract val action: String
    abstract val type: String
    
    /**
     * Button type step with text and buttons
     */
    data class ButtonStep(
        @SerializedName("step") override val step: String,
        @SerializedName("action") override val action: String,
        @SerializedName("type") override val type: String, // Will always be "button"
        @SerializedName("content") val content: ButtonContent
    ) : ChatStepDto()
    
    /**
     * Text type step with simple text content
     */
    data class TextStep(
        @SerializedName("step") override val step: String,
        @SerializedName("action") override val action: String,
        @SerializedName("type") override val type: String, // Will always be "text"
        @SerializedName("content") val content: String
    ) : ChatStepDto()
    
    /**
     * Image type step with image URL
     */
    data class ImageStep(
        @SerializedName("step") override val step: String,
        @SerializedName("action") override val action: String,
        @SerializedName("type") override val  type: String, // Will always be "image"
        @SerializedName("content") val content: String
    ) : ChatStepDto()
}

/**
 * Content structure for button type steps
 */
data class ButtonContent(
    @SerializedName("text") val text: String,
    @SerializedName("buttons") val buttons: List<Button>
)

/**
 * Button structure for button content
 */
data class Button(
    @SerializedName("label") val label: String,
    @SerializedName("action") val action: String
)
