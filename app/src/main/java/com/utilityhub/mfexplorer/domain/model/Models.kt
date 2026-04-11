package com.utilityhub.mfexplorer.domain.model

data class Fund(
    val schemeCode: Int,
    val schemeName: String,
    val fundHouse: String,
    val schemeType: String = "",
    val schemeCategory: String = "",
    val latestNav: String = ""
)

data class FundDetail(
    val schemeCode: Int,
    val schemeName: String,
    val fundHouse: String,
    val schemeType: String,
    val schemeCategory: String,
    val latestNav: String,
    val navChange: Double,
    val navHistory: List<NavPoint>
)

data class NavPoint(
    val date: String,
    val nav: Double
)

data class WatchlistFolder(
    val id: Long,
    val name: String,
    val fundCount: Int = 0
)

data class WatchlistFund(
    val schemeCode: Int,
    val schemeName: String,
    val fundHouse: String,
    val latestNav: String
)

enum class FundCategory(val displayName: String, val query: String) {
    INDEX("Index Funds", "index"),
    BLUECHIP("Bluechip Funds", "bluechip"),
    TAX_SAVER("Tax Saver (ELSS)", "tax saver elss"),
    LARGE_CAP("Large Cap Funds", "large cap")
}