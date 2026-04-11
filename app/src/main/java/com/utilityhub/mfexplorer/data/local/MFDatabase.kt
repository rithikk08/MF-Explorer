package com.utilityhub.mfexplorer.data.local



import androidx.room.Database
import androidx.room.RoomDatabase
import com.utilityhub.mfexplorer.data.local.dao.ExploreCacheDao
import com.utilityhub.mfexplorer.data.local.dao.WatchlistDao
import com.utilityhub.mfexplorer.data.local.entities.ExploreCacheEntity
import com.utilityhub.mfexplorer.data.local.entities.WatchlistFolderEntity
import com.utilityhub.mfexplorer.data.local.entities.WatchlistFundEntity

@Database(
    entities = [
        WatchlistFolderEntity::class,
        WatchlistFundEntity::class,
        ExploreCacheEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MFDatabase : RoomDatabase() {
    abstract fun watchlistDao(): WatchlistDao
    abstract fun exploreCacheDao(): ExploreCacheDao
}