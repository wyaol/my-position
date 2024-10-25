package com.example.myposition

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface LocationApiService {
    @POST("upload/location")
    fun uploadLocation(@Body location: LocationData): Call<Void>
}
