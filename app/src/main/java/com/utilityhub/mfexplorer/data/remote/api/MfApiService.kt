package com.utilityhub.mfexplorer.data.remote.api

import com.utilityhub.mfexplorer.data.remote.dto.FundDetailDto
import com.utilityhub.mfexplorer.data.remote.dto.FundSearchItemDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MfApiService {

    @GET("mf")
    suspend fun getAllFunds(
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0
    ): List<FundSearchItemDto>

    @GET("mf/search")
    suspend fun searchFunds(
        @Query("q") query: String
    ): List<FundSearchItemDto>

    @GET("mf/{schemeCode}")
    suspend fun getFundDetail(
        @Path("schemeCode") schemeCode: Int
    ): FundDetailDto

    @GET("mf/{schemeCode}/latest")
    suspend fun getLatestNav(
        @Path("schemeCode") schemeCode: Int
    ): FundDetailDto
}