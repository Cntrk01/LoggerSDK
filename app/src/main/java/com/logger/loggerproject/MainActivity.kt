package com.logger.loggerproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import com.logger.logger_sdk.init.Logger
import com.logger.loggerproject.model.ScreenLog
import com.logger.loggerproject.ui.theme.LoggerProjectTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LoggerProjectTheme {
                LaunchedEffect(Unit) {
                    repeat(10) { index ->
                        Logger.sendLog(
                            ScreenLog(
                                screenName = "Home",
                                openedAt = System.currentTimeMillis()
                            )
                        )
                        delay(1000)
                    }
                }
            }
        }
    }
}