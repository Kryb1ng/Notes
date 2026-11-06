package com.example.myapplication.utils

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class TextRecognitionAnalyzer(
    private val onTextDetected: (String) -> Unit,
    private val onNumberFound: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    // Регулярка теперь принимает и буквы, и цифры в середине (чтобы найти совпадение, даже если OCR ошибся)
    private val lenientRegex = Regex("[A-Z]{2}[A-Z0-9]{3}[A-Z]")

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val fullText = visionText.text
                    if (fullText.isNotBlank()) {
                        onTextDetected(fullText)

                        // Убираем все пробелы и переносы строк, оставляем только буквы и цифры
                        val cleanText = fullText.uppercase().replace("[^A-Z0-9]".toRegex(), "")

                        // Ищем номер
                        val match = lenientRegex.find(cleanText)
                        if (match != null) {
                            val rawNumber = match.value
                            Log.d("OCR_TEST", "Сырой номер: $rawNumber")

                            // Разбиваем номер на части: 2 буквы, 3 символа, 1 буква
                            val prefix = rawNumber.take(2)
                            var digits = rawNumber.substring(2, 5)
                            val suffix = rawNumber.takeLast(1)

                            // УМНАЯ ЗАМЕНА: исправляем ошибки OCR в блоке цифр
                            digits = digits
                                .replace("O", "0")
                                .replace("I", "1")
                                .replace("L", "1")
                                .replace("Z", "2")
                                .replace("S", "5")
                                .replace("B", "8")
                                .replace("G", "6")

                            val correctedNumber = "$prefix$digits$suffix"
                            Log.d("OCR_TEST", "Исправленный номер: $correctedNumber")

                            onNumberFound(correctedNumber)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("OCR_TEST", "Ошибка распознавания", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}