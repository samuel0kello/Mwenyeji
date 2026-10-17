package com.samuelokello.mwenyeji.feature.planner.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/** Speech to text by the phone's own recogniser: offline where it has the language, online otherwise. */
class VoiceInput(
    /** False on phones with no recogniser; hide the mic there. */
    val available: Boolean,
    /** Opens the recogniser. The words come back through `onText`. */
    val listen: () -> Unit,
)

/**
 * @param prompt Shown by the recogniser while it listens.
 * @param onText The best guess at what was said.
 * @param onUnavailable The recogniser could not be opened.
 */
@Composable
fun rememberVoiceInput(
    prompt: String,
    onText: (String) -> Unit,
    onUnavailable: () -> Unit,
): VoiceInput {
    val context = LocalContext.current
    val currentOnText by rememberUpdatedState(onText)
    val currentOnUnavailable by rememberUpdatedState(onUnavailable)
    val available = remember { SpeechRecognizer.isRecognitionAvailable(context) }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
            result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
                ?.takeIf { it.isNotBlank() }
                ?.let(currentOnText)
        }

    return VoiceInput(available) {
        val intent =
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                // Kenyan English or Swahili, following the phone; place names are said the same either way.
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE, recognitionLanguage())
                .putExtra(RecognizerIntent.EXTRA_PROMPT, prompt)
                .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        try {
            launcher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            currentOnUnavailable()
        }
    }
}

private fun recognitionLanguage(): String = if (Locale.getDefault().language == "sw") "sw-KE" else "en-KE"
