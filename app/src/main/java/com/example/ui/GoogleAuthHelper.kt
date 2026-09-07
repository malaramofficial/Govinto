package com.example.ui

import android.content.Context
import android.util.Base64
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom

class GoogleAuthHelper(private val context: Context) {
    private val credentialManager = CredentialManager.create(context)
    private val auth = FirebaseAuth.getInstance()

    suspend fun signIn(): Result<com.google.firebase.auth.FirebaseUser> = runCatching {
        val clientId = context.getString(com.example.R.string.default_web_client_id)
        require(clientId.isNotBlank() && !clientId.startsWith("REPLACE_")) {
            "Google Sign-In is not configured. Enable Google provider in Firebase and update google-services.json."
        }
        val option = GetSignInWithGoogleOption.Builder(clientId)
            .setNonce(generateNonce())
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()
        val result = credentialManager.getCredential(context, request)
        val credential = result.credential
        if (credential !is CustomCredential || credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            error("Google account credential was not returned")
        }
        val googleCredential = try {
            GoogleIdTokenCredential.createFrom(credential.data)
        } catch (e: GoogleIdTokenParsingException) {
            throw IllegalStateException("Google ID token could not be parsed", e)
        }
        val firebaseCredential = GoogleAuthProvider.getCredential(googleCredential.idToken, null)
        auth.signInWithCredential(firebaseCredential).await().user
            ?: error("Firebase did not return a user")
    }

    private fun generateNonce(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING)
    }
}
