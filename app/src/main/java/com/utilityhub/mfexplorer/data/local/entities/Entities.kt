package com.utilityhub.mfexplorer.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist_folders")
data class WatchlistFolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "watchlist_funds",
    foreignKeys = [
        ForeignKey(
            entity = WatchlistFolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["folderId"]), Index(value = ["folderId", "schemeCode"], unique = true)]
)
data class WatchlistFundEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val folderId: Long,
    val schemeCode: Int,
    val schemeName: String,
    val fundHouse: String,
    val latestNav: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "explore_cache")
data class ExploreCacheEntity(
    @PrimaryKey val category: String,
    val jsonData: String,
    val cachedAt: Long = System.currentTimeMillis()
)