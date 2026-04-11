package com.utilityhub.mfexplorer.data.local.dao

import androidx.room.*
import com.utilityhub.mfexplorer.data.local.entities.ExploreCacheEntity
import com.utilityhub.mfexplorer.data.local.entities.WatchlistFolderEntity
import com.utilityhub.mfexplorer.data.local.entities.WatchlistFundEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {

    @Query("SELECT * FROM watchlist_folders ORDER BY createdAt DESC")
    fun getAllFolders(): Flow<List<WatchlistFolderEntity>>

    @Query("SELECT * FROM watchlist_folders WHERE id = :id")
    suspend fun getFolderById(id: Long): WatchlistFolderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: WatchlistFolderEntity): Long

    @Delete
    suspend fun deleteFolder(folder: WatchlistFolderEntity)

    @Query("DELETE FROM watchlist_folders WHERE id = :id")
    suspend fun deleteFolderById(id: Long)

    @Query("SELECT * FROM watchlist_funds WHERE folderId = :folderId ORDER BY addedAt DESC")
    fun getFundsInFolder(folderId: Long): Flow<List<WatchlistFundEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFund(fund: WatchlistFundEntity)

    @Query("DELETE FROM watchlist_funds WHERE folderId = :folderId AND schemeCode = :schemeCode")
    suspend fun removeFund(folderId: Long, schemeCode: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist_funds WHERE schemeCode = :schemeCode LIMIT 1)")
    fun isFundInAnyWatchlist(schemeCode: Int): Flow<Boolean>

    @Query("SELECT folderId FROM watchlist_funds WHERE schemeCode = :schemeCode")
    fun getFolderIdsForFund(schemeCode: Int): Flow<List<Long>>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist_funds WHERE folderId = :folderId AND schemeCode = :schemeCode LIMIT 1)")
    suspend fun isFundInFolder(folderId: Long, schemeCode: Int): Boolean
}

@Dao
interface ExploreCacheDao {

    @Query("SELECT * FROM explore_cache WHERE category = :category")
    suspend fun getCacheForCategory(category: String): ExploreCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache: ExploreCacheEntity)

    @Query("DELETE FROM explore_cache WHERE category = :category")
    suspend fun deleteCache(category: String)
}

