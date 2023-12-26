package com.semuju.gemini_image_describer

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainViewModel(): ViewModel() {

    private val mMainUIState = MutableStateFlow(MainUIState())
    val uiState = mMainUIState.asStateFlow()


    private val geminiModel by lazy {
        GenerativeModel(
            modelName = "gemini-pro-vision",
            apiKey = "YOUR_API_KEY_HERE",
        )
    }


    fun generateDescription(image: Bitmap) {
        viewModelScope.launch {
            mMainUIState.value = mMainUIState.value.copy(isLoading = true)
            val chunks = mutableListOf<GenerateContentResponse>()
            var chunksCount = 0
            launch{
                geminiModel.generateContentStream(
                    content {
                        image(image)
                        text("You are to describe this image in painstaking detail. If the image contains some long string of text, summarize it and provide and explanation for it, also make sure to give your observation about what you think the image means or how the image came about. make sure to put two headings Explanation and Observation for the relevant parts and you MUST answer in markdown")
                    }
                ).collect{ chunk ->
                    mMainUIState.value = mMainUIState.value.copy(
                        isLoading = false
                    )
                    chunks.add(chunk)
                    chunksCount++
                }
            }
            launch {
                while (mMainUIState.value.isLoading) {
                    delay(200)
                }
                var sentChunks = 0
                while (sentChunks < chunksCount) {
                    chunks[sentChunks].text?.forEach {
                        mMainUIState.value = mMainUIState.value.copy(
                            explanation = mMainUIState.value.explanation + it
                        )
                        delay(2)
                    }
                    sentChunks++
                }
            }
        }
    }

    fun clearExplanation() {
        mMainUIState.value = mMainUIState.value.copy(explanation = "")
    }


}

data class MainUIState(
    val isLoading: Boolean = false,
    val explanation: String = "",
    val error: String = ""
)