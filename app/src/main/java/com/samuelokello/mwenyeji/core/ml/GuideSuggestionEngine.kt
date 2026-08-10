package com.samuelokello.mwenyeji.core.ml

import android.content.Context
import android.util.Log
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.channels.FileChannel

interface GuideSuggestionEngine {
    fun suggestGuide(
        stopId: String,
        arrivalMinutes: Float,
        stopSequence: Float,
    ): String?
}

class TfLiteGuideSuggestionEngine(
    private val context: Context,
) : GuideSuggestionEngine {
    private var interpreter: Interpreter? = null
    private val outputLabels = mutableListOf<String>()
    private val stopMappings = mutableMapOf<String, Int>()

    init {
        try {
            Log.d("SuggestionEngine", "Initializing TfLiteGuideSuggestionEngine")
            val manifestContent =
                context.assets
                    .open("model_manifest.json")
                    .bufferedReader()
                    .use { it.readText() }
            val manifest = Json.parseToJsonElement(manifestContent).jsonObject

            val modelFile = manifest["model_file"]?.jsonPrimitive?.content ?: "mwenyeji_guide_model.tflite"
            val labels = manifest["output_labels"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
            outputLabels.addAll(labels)
            Log.d("SuggestionEngine", "Loaded ${outputLabels.size} labels from manifest")

            val stopMap = manifest["stop_mapping"]?.jsonObject?.get("stop_id_to_index")?.jsonObject ?: JsonObject(emptyMap())
            stopMap.forEach { (id, index) ->
                stopMappings[id] = index.jsonPrimitive.int
            }
            Log.d("SuggestionEngine", "Loaded ${stopMappings.size} stop mappings")

            val assetFileDescriptor = context.assets.openFd(modelFile)
            val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = assetFileDescriptor.startOffset
            val declaredLength = assetFileDescriptor.declaredLength

            val mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
            interpreter = Interpreter(mappedByteBuffer)
            Log.d("SuggestionEngine", "TFLite Interpreter initialized successfully with $modelFile")
        } catch (e: Exception) {
            Log.e("SuggestionEngine", "Initialization failed", e)
            e.printStackTrace()
        }
    }

    /**
     * Executes localized evaluation predictions across spatial-temporal contexts.
     * Returns a predicted guide string or null.
     */
    override fun suggestGuide(
        stopId: String,
        arrivalMinutes: Float,
        stopSequence: Float,
    ): String? {
        Log.d("SuggestionEngine", "Suggesting guide for stop: $stopId, arrivalMins: $arrivalMinutes")
        val stopIndex = stopMappings[stopId]?.toFloat()
        if (stopIndex == null) {
            Log.w("SuggestionEngine", "Stop ID $stopId not found in mappings")
            return null
        }

        val interp = interpreter
        if (interp == null) {
            Log.e("SuggestionEngine", "Interpreter is null")
            return null
        }

        try {
            val inputs = arrayOf(floatArrayOf(stopIndex, arrivalMinutes, stopSequence))
            val outputDistributionMatrix = Array(1) { FloatArray(outputLabels.size) }

            interp.run(inputs, outputDistributionMatrix)

            val predictedIndex = outputDistributionMatrix[0].indices.maxByOrNull { outputDistributionMatrix[0][it] } ?: -1
            val result = if (predictedIndex != -1 && predictedIndex < outputLabels.size) outputLabels[predictedIndex] else null
            Log.d("SuggestionEngine", "Prediction result: $result (index: $predictedIndex)")
            return result
        } catch (e: Exception) {
            Log.e("SuggestionEngine", "Prediction failed", e)
            return null
        }
    }
}
