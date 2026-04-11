package com.utilityhub.mfexplorer.data.repository

import com.utilityhub.mfexplorer.data.local.dao.WatchlistDao
import com.utilityhub.mfexplorer.data.local.entities.WatchlistFolderEntity
import com.utilityhub.mfexplorer.data.local.entities.WatchlistFundEntity
import com.utilityhub.mfexplorer.domain.model.Fund
import com.utilityhub.mfexplorer.domain.model.WatchlistFolder
import com.utilityhub.mfexplorer.domain.model.WatchlistFund
import com.utilityhub.mfexplorer.domain.repository.WatchlistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchlistRepositoryImpl @Inject constructor(
    private val watchlistDao: WatchlistDao
) : WatchlistRepository {

    override fun getAllFolders(): Flow<List<WatchlistFolder>> =
        watchlistDao.getAllFolders().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getFundsInFolder(folderId: Long): Flow<List<WatchlistFund>> =
        watchlistDao.getFundsInFolder(folderId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun isFundInAnyWatchlist(schemeCode: Int): Flow<Boolean> =
        watchlistDao.isFundInAnyWatchlist(schemeCode)

    override fun getFolderIdsForFund(schemeCode: Int): Flow<List<Long>> =
        watchlistDao.getFolderIdsForFund(schemeCode)

    override suspend fun createFolder(name: String): Long =
        watchlistDao.insertFolder(WatchlistFolderEntity(name = name))

    override suspend fun deleteFolder(folderId: Long) =
        watchlistDao.deleteFolderById(folderId)

    override suspend fun addFundToFolder(folderId: Long, fund: Fund) {
        watchlistDao.insertFund(
            WatchlistFundEntity(
                folderId = folderId,
                schemeCode = fund.schemeCode,
                schemeName = fund.schemeName,
                fundHouse = fund.fundHouse,
                latestNav = fund.latestNav
            )
        )
    }

    override suspend fun removeFundFromFolder(folderId: Long, schemeCode: Int) =
        watchlistDao.removeFund(folderId, schemeCode)

    override suspend fun isFundInFolder(folderId: Long, schemeCode: Int): Boolean =
        watchlistDao.isFundInFolder(folderId, schemeCode)

    private fun WatchlistFolderEntity.toDomain() = WatchlistFolder(
        id = id,
        name = name
    )

    private fun WatchlistFundEntity.toDomain() = WatchlistFund(
        schemeCode = schemeCode,
        schemeName = schemeName,
        fundHouse = fundHouse,
        latestNav = latestNav
    )
}