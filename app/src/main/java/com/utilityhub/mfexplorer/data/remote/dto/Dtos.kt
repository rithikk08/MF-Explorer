package com.utilityhub.mfexplorer.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FundSearchItemDto(
    @SerializedName("schemeCode") val schemeCode: Int,
    @SerializedName("schemeName") val schemeName: String,
    @SerializedName("fundHouse") val fundHouse: String? = null,
    @SerializedName("schemeType") val schemeType: String? = null,
    @SerializedName("schemeCategory") val schemeCategory: String? = null,
    @SerializedName("isin") val isin: String? = null
)

data class FundDetailDto(
    @SerializedName("meta") val meta: FundMetaDto,
    @SerializedName("data") val navHistory: List<NavDataDto>,
    @SerializedName("status") val status: String
)

data class FundMetaDto(
    @SerializedName("fund_house") val fundHouse: String,
    @SerializedName("scheme_type") val schemeType: String,
    @SerializedName("scheme_category") val schemeCategory: String,
    @SerializedName("scheme_code") val schemeCode: Int,
    @SerializedName("scheme_name") val schemeName: String
)

data class NavDataDto(
    @SerializedName("date") val date: String,
    @SerializedName("nav") val nav: String
)