package com.utilityhub.mfexplorer.presentation.common

sealed class Screen(val route: String) {
    object Explore : Screen("explore")
    object Watchlist : Screen("watchlist")
    object Search : Screen("search")
    
    object FundDetail : Screen("fund_detail/{schemeCode}") {
        fun createRoute(code: Int) = "fund_detail/$code"
    }
    
    object ViewAll : Screen("view_all/{category}") {
        fun createRoute(cat: String) = "view_all/$cat"
    }
    
    object WatchlistDetail : Screen("watchlist_detail/{folderId}") {
        fun createRoute(id: Long) = "watchlist_detail/$id"
    }
}
