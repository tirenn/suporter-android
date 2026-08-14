package com.suporter.android.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.suporter.android.core.database.MonitoredAppDao
import com.suporter.android.core.database.MonitoredAppEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val isMonitored: Boolean
)

class AppRepository(
    private val context: Context,
    private val monitoredAppDao: MonitoredAppDao
) {

    val monitoredApps: Flow<List<MonitoredAppEntity>> = monitoredAppDao.getAllMonitoredApps()

    suspend fun getInstalledApps(): List<InstalledAppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        val activeMonitored = monitoredAppDao.getActiveMonitoredApps().map { it.packageName }.toSet()

        val list = resolveInfos.mapNotNull { ri ->
            val pkg = ri.activityInfo.packageName
            if (pkg == context.packageName) null
            else {
                val label = ri.loadLabel(pm).toString()
                InstalledAppInfo(
                    packageName = pkg,
                    appName = label,
                    isMonitored = activeMonitored.contains(pkg)
                )
            }
        }.distinctBy { it.packageName }.sortedBy { it.appName }

        list
    }

    suspend fun toggleAppMonitoring(packageName: String, appName: String, isEnabled: Boolean) {
        monitoredAppDao.insertOrUpdate(
            MonitoredAppEntity(
                packageName = packageName,
                appName = appName,
                isEnabled = isEnabled
            )
        )
    }
}
