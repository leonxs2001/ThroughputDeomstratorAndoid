package de.thb.demonstrator.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.thb.demonstrator.activity.MainActivity.Companion.BUFFER_SIZE
import de.thb.demonstrator.activity.MainActivity.Companion.COMMUNICATION_TYPE
import de.thb.demonstrator.activity.MainActivity.Companion.DATA_SIZE
import de.thb.demonstrator.activity.MainActivity.Companion.IP_ADDRESS_IDENTIFIER
import de.thb.demonstrator.activity.MainActivity.Companion.PORT_IDENTIFIER
import de.thb.demonstrator.activity.MainActivity.Companion.SENDING_TYPE
import de.thb.demonstrator.client.Client
import de.thb.demonstrator.client.ClientLoadingResult
import de.thb.demonstrator.enums.CommunicationType
import de.thb.demonstrator.enums.SendingType
import de.thb.demonstrator.ui.theme.ThroughputDemonstratorTheme
import de.thb.throughputdeomstrator.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.round

class LoadingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath

        val ipAddress = intent.getStringExtra(IP_ADDRESS_IDENTIFIER)
        val port = intent.getIntExtra(PORT_IDENTIFIER, 0)
        val bufferSize = intent.getIntExtra(BUFFER_SIZE, 0)
        val sendingTypeString = intent.getStringExtra(SENDING_TYPE)
        val communicationTypeString = intent.getStringExtra(COMMUNICATION_TYPE)
        val sendingType = SendingType.fromString(sendingTypeString)
        val communicationType = CommunicationType.fromString(communicationTypeString)
        val dataSize = intent.getIntExtra(DATA_SIZE, 0)

        val client = Client()

        setContent {
            ThroughputDemonstratorTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    LoadingScreen(
                        ipAddress = ipAddress ?: "",
                        port = port,
                        bufferSize = bufferSize,
                        communicationType = communicationType,
                        sendingType = sendingType,
                        dataSize = dataSize,
                        path = path,
                        client = client
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingScreen(
    modifier: Modifier = Modifier,
    ipAddress: String,
    port: Int,
    bufferSize: Int,
    communicationType: CommunicationType,
    sendingType: SendingType,
    dataSize: Int,
    path: String,
    client: Client
) {
    var isLoading by remember { mutableStateOf(true) }
    var clientLoadingResult: ClientLoadingResult? by remember { mutableStateOf(null) }

    var progressPercent by remember { mutableFloatStateOf(0f) }
    var progress by remember { mutableFloatStateOf(0f) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        try {
            val time = withContext(Dispatchers.IO) {
                client.startClient(
                    communicationType,
                    sendingType,
                    bufferSize,
                    dataSize,
                    ipAddress,
                    port,
                    path
                ) { loadPercentage ->
                    progressPercent = round(loadPercentage.toFloat() * 100) / 100
                    progress = (loadPercentage / 100).toFloat()
                }
            }

            clientLoadingResult = time
            isLoading = false
        } catch (e: Throwable) {
            e.printStackTrace()
            client.stopClient()
            (context as? Activity)?.let { goBackWithError(it) }
        }

    }
    Box(
        modifier = modifier.padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        IconButton(
            modifier = Modifier.align(Alignment.TopStart),
            onClick = {
                client.stopClient()
                (context as? Activity)?.let { goBack(it) }
            }

        ) {
            Icon(painter = painterResource(id = R.drawable.back_icon), contentDescription = "Back")
        }

        if (isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(progress = progress)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Lade Daten... ${progressPercent}%")
            }
        } else {

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Laden abgeschlossen!", fontSize = 24.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Ladezeit ist ${clientLoadingResult?.minutes} Minuten")
                Text("${clientLoadingResult?.seconds} Sekunden")
                Text("und ${clientLoadingResult?.milliseconds} Millisekunden")

                if (sendingType == SendingType.FILE) {
                    Button(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        onClick = {
                            (context as? Activity)?.let {
                                clientLoadingResult?.let { it1 ->
                                    openFile(
                                        context,
                                        it1.path
                                    )
                                }
                            }
                        },
                    ) {
                        Text("Datei öffnen")
                    }
                }
            }
        }
    }
}

fun openFile(context: Activity, path: String) {

    val intent = Intent(Intent.ACTION_VIEW)
    println(path)
    intent.setDataAndType(Uri.parse(path), "*/*")
    context.startActivity(intent)
}