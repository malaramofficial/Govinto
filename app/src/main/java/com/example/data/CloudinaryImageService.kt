package com.example.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.BufferedSink
import org.json.JSONObject

class CloudinaryImageService(
    private val context: Context = AppContextHolder.context
) {
    companion object {
        private const val CLOUD_NAME = "dsnetrfzy"
        private const val UPLOAD_PRESET = "govinto_upload"
        private const val UPLOAD_URL = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"
    }

    private val client = OkHttpClient()

    suspend fun uploadImage(uri: Uri, publicId: String): String = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val mimeType = resolver.getType(uri)?.takeIf { it.startsWith("image/") } ?: "image/jpeg"

        val imageBody = object : RequestBody() {
            override fun contentType() = mimeType.toMediaType()

            override fun writeTo(sink: BufferedSink) {
                resolver.openInputStream(uri)?.use { input ->
                    input.copyTo(sink.outputStream())
                } ?: throw IllegalStateException("Selected image could not be opened")
            }
        }

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", "listing_$publicId", imageBody)
            .addFormDataPart("upload_preset", UPLOAD_PRESET)
            .addFormDataPart("public_id", "govinto/listings/$publicId")
            .build()

        val request = Request.Builder()
            .url(UPLOAD_URL)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val message = runCatching { JSONObject(responseBody).optString("error") }.getOrNull()
                    ?.takeIf { it.isNotBlank() }
                    ?: response.message
                throw IllegalStateException("Cloudinary upload failed (${response.code}): $message")
            }

            val json = JSONObject(responseBody)
            json.optString("secure_url")
                .takeIf { it.isNotBlank() }
                ?: throw IllegalStateException("Cloudinary returned no image URL")
        }
    }
}
