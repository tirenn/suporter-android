package com.suporter.android.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [KeywordEntity::class, MonitoredAppEntity::class, WebhookLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun keywordDao(): KeywordDao
    abstract fun monitoredAppDao(): MonitoredAppDao
    abstract fun webhookLogDao(): WebhookLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val DEFAULT_KEYWORDS = listOf(
            "berhasil diterima",
            "cr",
            "credit",
            "dana masuk",
            "diterima",
            "incoming",
            "masuk",
            "payment received",
            "pembayaran diterima",
            "pembayaran masuk",
            "received",
            "saldo bertambah",
            "terima",
            "transfer masuk"
        )

        val DEFAULT_SUGGESTED_APPS = listOf(
            MonitoredAppEntity("id.dana", "DANA", isEnabled = true, isSuggested = true),
            MonitoredAppEntity("com.shopee.id", "Shopee / ShopeePay", isEnabled = true, isSuggested = true),
            MonitoredAppEntity("com.gojek.app", "GoPay / Gojek", isEnabled = true, isSuggested = true),
            MonitoredAppEntity("omo.medea.ovo", "OVO", isEnabled = true, isSuggested = true),
            MonitoredAppEntity("com.bca", "BCA Mobile", isEnabled = true, isSuggested = true),
            MonitoredAppEntity("id.bmri.livin", "Livin by Mandiri", isEnabled = true, isSuggested = true),
            MonitoredAppEntity("id.co.bri.brimo", "BRImo", isEnabled = true, isSuggested = true),
            MonitoredAppEntity("com.bankbni.bni", "BNI Mobile", isEnabled = true, isSuggested = true)
        )

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "suporter_listener.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed default keywords & suggested apps on database creation
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            val keywords = DEFAULT_KEYWORDS.map {
                                KeywordEntity(keyword = it, isDefault = true, isEnabled = true)
                            }
                            database.keywordDao().insertAll(keywords)
                            database.monitoredAppDao().insertAll(DEFAULT_SUGGESTED_APPS)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
