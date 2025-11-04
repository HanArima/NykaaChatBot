package com.garima.nykaachatbot

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object GeminiService {

    private const val TAG = "GeminiService"
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private const val GEMINI_API_KEY = "AIzaSyCfQF1oFmlqAsdN_R3rqY2n2dPYhpy0KZ8"

    // Correct v1 endpoint and a working model
    private const val GEMINI_MODEL = "gemini-2.5-flash"

    suspend fun getGeminiResponse(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            val url =
                "https://generativelanguage.googleapis.com/v1/models/$GEMINI_MODEL:generateContent?key=$GEMINI_API_KEY"

            // Correct JSON payload
            val jsonBody = """
                {
                  "contents": [
                    {
                      "parts": [
                        {"text": "$prompt"}
                      ]
                    }
                  ]
                }
            """.trimIndent()

            val requestBody = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()

            return@withContext if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: return@withContext "Empty response"
                Log.d(TAG, "Gemini response: $responseBody")

                // Parse response
                val jsonResponse = JSONObject(responseBody)
                val text = jsonResponse
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                text
            } else {
                val errorBody = response.body?.string()
                Log.e(TAG, "Gemini API Error: ${response.code} - ${response.message}\n$errorBody")
                "Error ${response.code}: ${errorBody ?: response.message}"
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception calling Gemini API", e)
            "Exception: ${e.message}"
        }
    }
}
