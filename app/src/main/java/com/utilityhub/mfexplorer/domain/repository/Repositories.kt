package com.utilityhub.mfexplorer.domain.repository

import com.utilityhub.mfexplorer.domain.model.*
import kotlinx.coroutines.flow.Flow

interface MfRepository {
    suspend fun searchFunds(query: String): Result<List<Fund>>
    suspend fun getFundsByCategory(category: FundCategory): Result<List<Fund>>
    suspend fun getAllFunds(): Result<List<Fund>>
    suspend fun getFundDetail(schemeCode: Int): Result<FundDetail>
}

interface WatchlistRepository {
    fun getAllFolders(): Flow<List<WatchlistFolder>>
    fun getFundsInFolder(folderId: Long): Flow<List<WatchlistFund>>
    fun isFundInAnyWatchlist(schemeCode: Int): Flow<Boolean>
    fun getFolderIdsForFund(schemeCode: Int): Flow<List<Long>>
    suspend fun createFolder(name: String): Long
    suspend fun updateFolderName(folderId: Long, newName: String)
    suspend fun deleteFolder(folderId: Long)
    suspend fun addFundToFolder(folderId: Long, fund: Fund)
    suspend fun removeFundFromFolder(folderId: Long, schemeCode: Int)
    suspend fun isFundInFolder(folderId: Long, schemeCode: Int): Boolean
}