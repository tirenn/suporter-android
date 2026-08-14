package com.suporter.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.suporter.android.data.repository.AppRepository
import com.suporter.android.data.repository.AuthRepository
import com.suporter.android.data.repository.KeywordRepository
import com.suporter.android.data.repository.LogRepository
import com.suporter.android.ui.navigation.NavGraph
import com.suporter.android.ui.theme.BgDark
import com.suporter.android.ui.theme.SuporterTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as SuporterApp
        val authRepository = AuthRepository(app.preferences)
        val keywordRepository = KeywordRepository(app.database.keywordDao())
        val appRepository = AppRepository(this, app.database.monitoredAppDao())
        val logRepository = LogRepository(app.database.webhookLogDao())

        setContent {
            SuporterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BgDark
                ) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        app = app,
                        authRepository = authRepository,
                        keywordRepository = keywordRepository,
                        appRepository = appRepository,
                        logRepository = logRepository
                    )
                }
            }
        }
    }
}
