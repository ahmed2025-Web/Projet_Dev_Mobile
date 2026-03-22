
package com.example.dev_mobile.repository

import com.example.dev_mobile.network.FestivalApiService
import com.example.dev_mobile.network.FestivalCourantDto
import com.example.dev_mobile.network.RetrofitClient

class FestivalRepository {
    private val api = RetrofitClient.retrofit.create(FestivalApiService::class.java)

    suspend fun getFestivalCourant(): FestivalCourantDto? {
        return try {
            val response = api.getFestivalCourant()
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }
}