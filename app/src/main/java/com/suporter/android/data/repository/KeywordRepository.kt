package com.suporter.android.data.repository

import com.suporter.android.core.database.KeywordDao
import com.suporter.android.core.database.KeywordEntity
import kotlinx.coroutines.flow.Flow

class KeywordRepository(private val keywordDao: KeywordDao) {

    val allKeywords: Flow<List<KeywordEntity>> = keywordDao.getAllKeywords()

    suspend fun addKeyword(keywordText: String): Long {
        val trimmed = keywordText.trim().lowercase()
        return keywordDao.insert(KeywordEntity(keyword = trimmed, isDefault = false, isEnabled = true))
    }

    suspend fun toggleKeyword(keyword: KeywordEntity) {
        keywordDao.update(keyword.copy(isEnabled = !keyword.isEnabled))
    }

    suspend fun deleteKeyword(keyword: KeywordEntity) {
        keywordDao.delete(keyword)
    }
}
