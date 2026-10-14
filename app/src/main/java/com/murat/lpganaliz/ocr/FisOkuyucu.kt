package com.murat.lpganaliz.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Fiş fotoğrafını ML Kit (Türkçe/Latin alfabesi) ile okur.
 */
object FisOkuyucu {

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    /** Uri'den görüntüyü okuyup ham metni döndürür. */
    suspend fun oku(context: Context, uri: Uri): Result<String> = runCatching {
        val image = InputImage.fromFilePath(context, uri)
        suspendCancellableCoroutine { cont ->
            recognizer.process(image)
                .addOnSuccessListener { visionText -> cont.resume(visionText.text) }
                .addOnFailureListener { e -> cont.resume("") }
        }
    }
}
