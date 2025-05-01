package tr.yigitunlu.n11chatapp.data.remote.dto

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type

/**
 * Custom deserializer for ChatStepDto to handle different types
 */
class ChatStepDtoDeserializer : JsonDeserializer<ChatStepDto> {
    
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ChatStepDto {
        val jsonObject = json.asJsonObject
        
        return when (val type = jsonObject.get("type").asString) {
            "button" -> deserializeButtonStep(jsonObject, context)
            "text" -> deserializeTextStep(jsonObject)
            "image" -> deserializeImageStep(jsonObject)
            else -> throw IllegalArgumentException("Unknown step type: $type")
        }
    }
    
    private fun deserializeButtonStep(
        jsonObject: JsonObject,
        context: JsonDeserializationContext
    ): ChatStepDto.ButtonStep {
        val step = jsonObject.get("step").asString
        val action = jsonObject.get("action").asString
        val type = jsonObject.get("type").asString
        val content = context.deserialize<ButtonContent>(
            jsonObject.get("content"),
            ButtonContent::class.java
        )
        
        return ChatStepDto.ButtonStep(step, action, type, content)
    }
    
    private fun deserializeTextStep(
        jsonObject: JsonObject
    ): ChatStepDto.TextStep {
        val step = jsonObject.get("step").asString
        val action = jsonObject.get("action").asString
        val type = jsonObject.get("type").asString
        val content = jsonObject.get("content").asString
        
        return ChatStepDto.TextStep(step, action, type, content)
    }
    
    private fun deserializeImageStep(
        jsonObject: JsonObject
    ): ChatStepDto.ImageStep {
        val step = jsonObject.get("step").asString
        val action = jsonObject.get("action").asString
        val type = jsonObject.get("type").asString
        val content = jsonObject.get("content").asString
        
        return ChatStepDto.ImageStep(step, action, type, content)
    }
}
