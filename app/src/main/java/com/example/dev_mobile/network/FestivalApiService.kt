
package com.example.dev_mobile.network

import retrofit2.Response
import retrofit2.http.GET

data class FestivalCourantDto(
    val id: Int,
    val nom: String,
    val est_courant: Boolean = true,
    val date_debut: String? = null,
    val date_fin: String? = null
)

interface FestivalApiService {
    @GET("api/festivals/courant")
    suspend fun getFestivalCourant(): Response<FestivalCourantDto>
}