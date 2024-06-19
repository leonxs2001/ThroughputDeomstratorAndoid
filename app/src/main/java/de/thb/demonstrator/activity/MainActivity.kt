package de.thb.demonstrator.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.thb.demonstrator.ui.theme.ThroughputDemonstratorTheme

class MainActivity : ComponentActivity() {
    companion object {
        const val IP_ADDRESS_IDENTIFIER = "ipAddress"
        const val PORT_IDENTIFIER = "port"
        const val BUFFER_SIZE = "bufferSize"
        const val SENDING_TYPE = "sendingType"
        const val DATA_SIZE = "dataSize"
        const val COMMUNICATION_TYPE = "communicationType"
        const val FILE_URI = "fileUri"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ThroughputDemonstratorTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
                    Home(viewModel) { ipAddress, port ->
                        val intent = Intent(this, CreateConnectionActivity::class.java).apply {
                            putExtra(IP_ADDRESS_IDENTIFIER, ipAddress)
                            putExtra(PORT_IDENTIFIER, port)
                        }
                        startActivity(intent)
                    }
                }
            }
        }
    }
}

class HomeViewModel : ViewModel() {
    private var _ipAddress: MutableState<String> = mutableStateOf("192.168.178.41")
    private var _port: MutableState<String> = mutableStateOf("65432");

    fun getIpAddress(): MutableState<String> {
        return _ipAddress
    }

    fun setIpAddress(newIpAddress: String) {
        _ipAddress.value = newIpAddress
    }

    fun getPort(): MutableState<String> {
        return _port
    }

    fun setPort(port: String) {
        _port.value = port
    }
}

@Composable
fun Home(viewModel: HomeViewModel, modifier: Modifier = Modifier, onSubmit: (String, Int) -> Unit) {
    val ipAddressState = viewModel.getIpAddress()
    val portState = viewModel.getPort()

    Box(
        modifier = modifier.padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(top = 190.dp)
        ) {
            val ipAddress = ipAddressState.value
            val port = portState.value
            if (!validateIpAddress(ipAddress)) {
                Text(
                    text = "Das ist keine valide IP-Adresse",
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 8.dp).align(Alignment.Start)
                )
            }
            OutlinedTextField(
                value = ipAddress,
                onValueChange = {
                    viewModel.setIpAddress(it)
                },
                label = { Text("Gib die IP-Adresse des Servers ein") },
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (!validatePort(port)) {
                Text(
                    text = "Das ist kein valider Port",
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 8.dp).align(Alignment.Start)
                )
            }
            OutlinedTextField(
                value = port,
                onValueChange = {
                    viewModel.setPort(it);
                },
                label = { Text("Gib die den Port des Servers ein") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Button(
                onClick = {
                    onSubmit(ipAddress, port.toInt())
                },
                enabled = validatePort(port) && validateIpAddress(ipAddress),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Bestätigen")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    ThroughputDemonstratorTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            val viewModel = HomeViewModel()
            Home(viewModel) { _, _ ->
            }
        }
    }
}

fun validateIpAddress(ipAddress: String): Boolean {
    val ipv4Regex =
        """^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$""".toRegex()
    val ipv6Regex =
        """^([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)$""".toRegex()
    return ipv4Regex.matches(ipAddress) || ipv6Regex.matches(ipAddress)
}

fun validatePort(port: String): Boolean {
    if (port != "" && port.isDigitsOnly()) {
        val portInt = port.toInt()
        return portInt in 49152..65535;
    } else {
        return false
    }

}
