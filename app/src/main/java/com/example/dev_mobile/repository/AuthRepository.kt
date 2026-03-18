
package com.example.dev_mobile.repository

import com.example.dev_mobile.network.AuthApiService
import com.example.dev_mobile.network.LoginRequest
import com.example.dev_mobile.network.RegisterRequest
import com.example.dev_mobile.network.RetrofitClient

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository {

    private val api: AuthApiService =
        RetrofitClient.retrofit.create(AuthApiService::class.java)

    suspend fun register(nom: String, prenom: String, email: String, login: String, password: String): AuthResult {
        return try {
            val response = api.register(RegisterRequest(nom, prenom, email, login, password))
            if (response.isSuccessful) AuthResult.Success
            else AuthResult.Error(response.errorBody()?.string() ?: "Erreur ${response.code()}")
        } catch (e: Exception) {
            AuthResult.Error("Impossible de joindre le serveur : ${e.message}")
        }
    }

    suspend fun login(login: String, password: String): AuthResult {
        return try {
            val response = api.login(LoginRequest(login, password))
            if (response.isSuccessful) AuthResult.Success
            else AuthResult.Error(response.errorBody()?.string() ?: "Identifiants incorrects")
        } catch (e: Exception) {
            AuthResult.Error("Impossible de joindre le serveur : ${e.message}")
        }
    }

    suspend fun logout(): AuthResult {
        return try {
            api.logout()
            RetrofitClient.getCookieJar().clearAll()
            AuthResult.Success
        } catch (e: Exception) {
            RetrofitClient.getCookieJar().clearAll()
            AuthResult.Success
        }
    }
}