package com.utilityhub.mfexplorer.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.utilityhub.mfexplorer.data.local.dao.ExploreCacheDao
import com.utilityhub.mfexplorer.data.local.entities.ExploreCacheEntity
import com.utilityhub.mfexplorer.data.remote.api.MfApiService
import com.utilityhub.mfexplorer.data.remote.dto.FundSearchItemDto
import com.utilityhub.mfexplorer.domain.model.*
import com.utilityhub.mfexplorer.domain.repository.MfRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

private const val CACHE_EXPIRY_MS = 6 * 60 * 60 * 1000L // 6 hours

@Singleton
class MfRepositoryImpl @Inject constructor(
    private val apiService: MfApiService,
    private val exploreCacheDao: ExploreCacheDao,
    private val gson: Gson
) : MfRepository {

    override suspend fun searchFunds(query: String): Result<List<Fund>> = runCatching {
        val dtos = apiService.searchFunds(query).take(15) // Limit results for fast loading & no rate limits
        val baseFunds = dtos.map { it.toDomain() }
        
        coroutineScope {
            baseFunds.map { fund ->
                async {
                    try {
                        val detailDto = apiService.getLatestNav(fund.schemeCode)
                        val latestNav = detailDto.navHistory.firstOrNull()?.nav ?: ""
                        fund.copy(latestNav = latestNav)
                    } catch (e: Exception) {
                        fund // Keep as empty string on fail
                    }
                }
            }.awaitAll()
        }
    }

    override suspend fun getFundsByCategory(category: FundCategory): Result<List<Fund>> {
        // Try cache first
        val cached = exploreCacheDao.getCacheForCategory(category.name)
        if (cached != null && !isCacheExpired(cached.cachedAt)) {
            return runCatching {
                val type = object : TypeToken<List<Fund>>() {}.type
                gson.fromJson(cached.jsonData, type)
            }
        }

        // Fetch from network
        return runCatching {
            val dtos = apiService.searchFunds(category.query).take(30) // Limit to top 30 to avoid API rate limits using ViewAll
            val baseFunds = dtos.map { it.toDomain() }

            // Concurrently fetch the latest NAV for these funds
            val fundsWithNav = coroutineScope {
                baseFunds.map { fund ->
                    async {
                        try {
                            val detailDto = apiService.getLatestNav(fund.schemeCode)
                            val latestNav = detailDto.navHistory.firstOrNull()?.nav ?: ""
                            fund.copy(latestNav = latestNav)
                        } catch (e: Exception) {
                            fund // Fallback to empty string if rate limited
                        }
                    }
                }.awaitAll()
            }

            // Save to cache
            exploreCacheDao.insertCache(
                ExploreCacheEntity(
                    category = category.name,
                    jsonData = gson.toJson(fundsWithNav)
                )
            )
            fundsWithNav
        }.recoverCatching { error ->
            // On network error, return stale cache if available
            if (cached != null) {
                val type = object : TypeToken<List<Fund>>() {}.type
                gson.fromJson(cached.jsonData, type)
            } else {
                throw error
            }
        }
    }

    override suspend fun getAllFunds(): Result<List<Fund>> {
        // Try cache first
        val cached = exploreCacheDao.getCacheForCategory("ALL_FUNDS")
        if (cached != null && !isCacheExpired(cached.cachedAt)) {
            return runCatching {
                val type = object : TypeToken<List<Fund>>() {}.type
                gson.fromJson(cached.jsonData, type)
            }
        }

        return runCatching {
            // we will fetch top 30 global funds to save rate limit, can adjust limit
            val dtos = apiService.getAllFunds(limit = 30)
            val baseFunds = dtos.map { it.toDomain() }

            val fundsWithNav = coroutineScope {
                baseFunds.map { fund ->
                    async {
                        try {
                            val detailDto = apiService.getLatestNav(fund.schemeCode)
                            val latestNav = detailDto.navHistory.firstOrNull()?.nav ?: ""
                            fund.copy(latestNav = latestNav)
                        } catch (e: Exception) {
                            fund
                        }
                    }
                }.awaitAll()
            }

            exploreCacheDao.insertCache(
                ExploreCacheEntity(
                    category = "ALL_FUNDS",
                    jsonData = gson.toJson(fundsWithNav)
                )
            )
            fundsWithNav
        }.recoverCatching { error ->
            if (cached != null) {
                val type = object : TypeToken<List<Fund>>() {}.type
                gson.fromJson(cached.jsonData, type)
            } else {
                throw error
            }
        }
    }

    override suspend fun getFundDetail(schemeCode: Int): Result<FundDetail> = runCatching {
        val dto = apiService.getFundDetail(schemeCode)

        // Sample NAV data for chart performance - take 1 point per week for last 3 years
        val allNavData = dto.navHistory
        val sampledNav = sampleNavData(allNavData.map { navDto ->
            NavPoint(navDto.date, navDto.nav.toDoubleOrNull() ?: 0.0)
        })

        val latestNav = dto.navHistory.firstOrNull()?.nav ?: "N/A"
        val previousNav = dto.navHistory.getOrNull(1)?.nav?.toDoubleOrNull() ?: 0.0
        val currentNavDouble = latestNav.toDoubleOrNull() ?: 0.0
        val change = if (previousNav != 0.0) {
            ((currentNavDouble - previousNav) / previousNav) * 100
        } else 0.0

        FundDetail(
            schemeCode = dto.meta.schemeCode,
            schemeName = dto.meta.schemeName,
            fundHouse = dto.meta.fundHouse,
            schemeType = dto.meta.schemeType,
            schemeCategory = dto.meta.schemeCategory,
            latestNav = latestNav,
            navChange = change,
            navHistory = sampledNav
        )
    }

    /**
     * Sample NAV data to reduce chart data points.
     * Takes 1 point per 7 days for performance, max 365 points (7 years weekly).
     */
    private fun sampleNavData(navPoints: List<NavPoint>): List<NavPoint> {
        if (navPoints.size <= 365) return navPoints
        val step = navPoints.size / 365
        return navPoints.filterIndexed { index, _ -> index % step == 0 }
            .take(365)
    }

    private fun isCacheExpired(cachedAt: Long): Boolean {
        return System.currentTimeMillis() - cachedAt > CACHE_EXPIRY_MS
    }

    private fun FundSearchItemDto.toDomain() = Fund(
        schemeCode = schemeCode,
        schemeName = schemeName,
        fundHouse = fundHouse ?: "",
        schemeType = schemeType ?: "",
        schemeCategory = schemeCategory ?: ""
    )
}