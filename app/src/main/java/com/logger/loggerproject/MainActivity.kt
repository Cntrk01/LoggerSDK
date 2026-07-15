package com.logger.loggerproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.logger.logger_sdk.core.LogDestination
import com.logger.logger_sdk.core.SdkConfig
import com.logger.logger_sdk.init.Logger
import com.logger.logger_sdk.storage.LogSerializer
import com.logger.loggerproject.ui.theme.LoggerProjectTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LoggerProjectTheme {
                Logger.initialize(
                    application = application,
                    config = SdkConfig(
                        logDestination = ConsoleLogDestination(),
                        channelCapacity = 5,
                        timeoutMs = 80_000,
                        batchSize = 5,
                    )
                )

                scope.launch {
                    repeat(5) { index ->
                        Logger.sendLog("MainActivity Opened ${index + 1}")
                        delay(1_000)
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

class ConsoleLogDestination : LogDestination<String> {

    override val serializer = StringSerializer()

    override suspend fun log(
        logs: List<String>
    ) {
        println("Gönderilen loglar:")
        logs.forEach {
            println(it)
        }
    }
}

class StringSerializer : LogSerializer<String> {

    override fun serialize(value: String): String = value

    override fun deserialize(value: String): String = value
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LoggerProjectTheme {
        Greeting("Android")
    }
}