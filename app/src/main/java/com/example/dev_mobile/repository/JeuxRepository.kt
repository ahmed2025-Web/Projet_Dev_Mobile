package com.example.dev_mobile.repository

import com.example.dev_mobile.network.JeuDto
import com.example.dev_mobile.network.JeuxApiService
import com.example.dev_mobile.network.RetrofitClient

class JeuxRepository {
    private val api = RetrofitClient.retrofit.create(JeuxApiService::class.java)

    suspend fun getAll(): ApiResult<List<JeuDto>> = try {
        val response = api.getAll()
        if (response.isSuccessful) {
            ApiResult.Success(response.body() ?: emptyList())
        } else {
            ApiResult.Error("Erreur ${response.code()}")
        }
    } catch (e: Exception) {
        ApiResult.Error("Serveur injoignable : ${e.message}")
    }
}
