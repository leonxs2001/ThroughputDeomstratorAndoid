package de.thb.demonstrator.activity;


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.thb.demonstrator.enums.CommunicationType
import de.thb.demonstrator.enums.DataUnit
import de.thb.demonstrator.enums.SendingType
import de.thb.demonstrator.ui.theme.ThroughputDemonstratorTheme
import de.thb.throughputdeomstrator.R

class CreateConnectionActivity : ComponentActivity() {
    private val viewModel: ConnectorViewModel; get() = ViewModelProvider(this).get(ConnectorViewModel::class.java)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel =
            ViewModelProvider(this)[ConnectorViewModel::class.java]

        val ipAddress = intent.getStringExtra(MainActivity.IP_ADDRESS_IDENTIFIER)
        val port = intent.getIntExtra(MainActivity.PORT_IDENTIFIER, 65432)
        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_CANCELED) {
                viewModel.setShowError(true)
            } else {
                viewModel.setShowError(false)
            }
        }
        setContent {
            ThroughputDemonstratorTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Connector(viewModel) { communicationType, sendingType, bufferSize, dataSize, fileUri ->
                        val intent = Intent(this, LoadingActivity::class.java).apply {
                            putExtra(MainActivity.IP_ADDRESS_IDENTIFIER, ipAddress)
                            putExtra(MainActivity.PORT_IDENTIFIER, port)
                            putExtra(MainActivity.BUFFER_SIZE, bufferSize)
                            putExtra(MainActivity.SENDING_TYPE, sendingType.toString())
                            putExtra(MainActivity.COMMUNICATION_TYPE, communicationType.toString())
                            putExtra(MainActivity.DATA_SIZE, dataSize)
                            putExtra(MainActivity.FILE_URI, fileUri)

                        }
                        resultLauncher.launch(intent)
                    }
                }
            }
        }
    }

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            // Hier können Sie den ausgewählten Datei-URI verwenden
            viewModel.setFileUri(it)
        }
    }

    fun pickFile() {
        filePickerLauncher.launch(arrayOf("*/*"))
    }
}

class ConnectorViewModel : ViewModel() {
    private var _sendingType: MutableState<SendingType> = mutableStateOf(SendingType.FILE)
    private var _communicationType: MutableState<CommunicationType> = mutableStateOf(CommunicationType.UPLOAD)
    private var _dataUnit: MutableState<DataUnit> = mutableStateOf(DataUnit.GB)
    private var _bufferSize: MutableState<String> = mutableStateOf("1024");
    private var _dataSize: MutableState<String> = mutableStateOf("1")
    private var _fileUri: MutableState<Uri> = mutableStateOf(Uri.EMPTY)
    private var _showError: MutableState<Boolean> = mutableStateOf(false)

    fun getCommunicationType(): MutableState<CommunicationType> {
        return _communicationType
    }

    fun setCommunicationType(communicationType: CommunicationType) {
        _communicationType.value = communicationType
    }

    fun isShowError(): MutableState<Boolean> {
        return _showError
    }

    fun setShowError(showError: Boolean) {
        _showError.value = showError
    }

    fun getBufferSize(): MutableState<String> {
        return _bufferSize
    }

    fun setBufferSize(bufferSize: String) {
        _bufferSize.value = bufferSize
    }

    fun setDataSize(dataSize: String) {
        _dataSize.value = dataSize
    }

    fun getDataSize(): MutableState<String> {
        return _dataSize
    }

    fun setDataUnit(dataUnit: DataUnit) {
        _dataUnit.value = dataUnit
    }

    fun getDataUnit(): MutableState<DataUnit> {
        return _dataUnit
    }

    fun getSendingType(): MutableState<SendingType> {
        return _sendingType
    }

    fun setSendingType(sendingType: SendingType) {
        _sendingType.value = sendingType
    }

    fun getFileUri(): MutableState<Uri> {
        return _fileUri
    }

    fun setFileUri(fileUri: Uri) {
        _fileUri.value = fileUri
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Connector(
    viewModel: ConnectorViewModel,
    modifier: Modifier = Modifier,
    onSubmit: (CommunicationType, SendingType, Int, Int, Uri) -> Unit
) {
    val sendingTypeState = viewModel.getSendingType()
    val communicationTypeState = viewModel.getCommunicationType()
    val fileUriState = viewModel.getFileUri()
    val bufferSizeState = viewModel.getBufferSize()
    val dataSizeState = viewModel.getDataSize()
    val dataUnitState = viewModel.getDataUnit()
    val sendingTypes = SendingType.entries.toTypedArray()
    val dataUnits = DataUnit.entries.toTypedArray()
    val communicationTypes = CommunicationType.entries.toTypedArray()
    val context = LocalContext.current

    Box(
        modifier = modifier.padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {

        IconButton(
            modifier = Modifier.align(Alignment.TopStart),
            onClick = {
                (context as? Activity)?.let { goBack(it) }
            }

        ) {
            Icon(painter = painterResource(id = R.drawable.back_icon), contentDescription = "Back")
        }
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(top = 125.dp)
        ) {
            var expandedSendingType by remember { mutableStateOf(false) }
            var expandedDataUnit by remember { mutableStateOf(false) }
            var expandedCommunicationType by remember { mutableStateOf(false) }
            val sendingType = sendingTypeState.value
            val communicationType = communicationTypeState.value
            val bufferSize = bufferSizeState.value
            val dataSize = dataSizeState.value
            val fileUri = fileUriState.value;
            val dataUnit = dataUnitState.value
            val showError = viewModel.isShowError().value

            if (showError) {
                Text(
                    text = "Es ist ein Fehler aufgetreten beim Laden",
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 8.dp).align(Alignment.CenterHorizontally)
                )
            }

            Text(
                text = "Was willst du tun?",
                modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally),
                fontSize = 24.sp
            )

            ExposedDropdownMenuBox(
                expanded = expandedCommunicationType,
                onExpandedChange = {
                    expandedCommunicationType = !expandedCommunicationType
                },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                TextField(
                    value = communicationType.toString(),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCommunicationType) },
                    modifier = Modifier.menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expandedCommunicationType,
                    onDismissRequest = { expandedCommunicationType = false }
                ) {
                    communicationTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.toString()) },
                            onClick = {
                                viewModel.setCommunicationType(type)
                                expandedCommunicationType = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = expandedSendingType,
                onExpandedChange = {
                    expandedSendingType = !expandedSendingType
                },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                TextField(
                    value = sendingType.toString(),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSendingType) },
                    modifier = Modifier.menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expandedSendingType,
                    onDismissRequest = { expandedSendingType = false }
                ) {
                    sendingTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.toString()) },
                            onClick = {
                                viewModel.setSendingType(type)
                                expandedSendingType = false
                            }
                        )
                    }
                }
            }

            if (sendingType == SendingType.FILE && communicationType == CommunicationType.UPLOAD) {
                Button(
                    onClick = { (context as? CreateConnectionActivity)?.pickFile() },
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    var suffix = "";
                    if (!Uri.EMPTY.equals(fileUri)) {
                        suffix = "(ausgewählt)"
                    }
                    Text("Datei auswählen $suffix")
                }
            }

            if (sendingType == SendingType.DUMMY) {
                ExposedDropdownMenuBox(
                    expanded = expandedDataUnit,
                    onExpandedChange = {
                        expandedDataUnit = !expandedDataUnit
                    },
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    TextField(
                        value = dataUnit.toString(),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDataUnit) },
                        modifier = Modifier.menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedDataUnit,
                        onDismissRequest = { expandedDataUnit = false }
                    ) {
                        dataUnits.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.toString()) },
                                onClick = {
                                    viewModel.setDataUnit(type)
                                    expandedDataUnit = false
                                }
                            )
                        }
                    }
                }
                if (!validateBufferOrDataSize(dataSize)) {
                    Text(
                        text = "Das ist keine valide Menge an Daten",
                        color = Color.Red,
                        modifier = Modifier.padding(bottom = 8.dp).align(Alignment.Start)
                    )
                }
                OutlinedTextField(
                    value = dataSize,
                    onValueChange = {
                        viewModel.setDataSize(it);
                    },
                    label = { Text("Gib die Menge an Daten ein") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (!validateBufferOrDataSize(bufferSize)) {
                Text(
                    text = "Das ist keine valide Puffergröße",
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 8.dp).align(Alignment.Start)
                )
            }

            OutlinedTextField(
                value = bufferSize,
                onValueChange = {
                    viewModel.setBufferSize(it);
                },
                label = { Text("Gib die größe des Puffers ein in Byte") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Button(
                modifier = Modifier.align(Alignment.End),
                onClick = {
                    var dataSizeRes = 0;
                    if (sendingType == SendingType.DUMMY) {
                        dataSizeRes = dataSize.toInt() * dataUnit.multiplier
                    }
                    onSubmit(communicationType, sendingType, bufferSize.toInt(), dataSizeRes, fileUri)
                },
                enabled = isButtonEnabled(bufferSize, sendingType, dataSize, communicationType, fileUri)
            ) {
                Text("Starten")
            }
        }

    }
}

fun goBack(context: Activity) {
    context.setResult(ComponentActivity.RESULT_OK)
    context.finish()
}

fun goBackWithError(context: Activity) {
    context.setResult(ComponentActivity.RESULT_CANCELED)
    context.finish()
}

fun validateBufferOrDataSize(bufferSize: String): Boolean {
    if (bufferSize != "" && bufferSize.isDigitsOnly()) {
        val bufferSizeInt = bufferSize.toInt()
        return bufferSizeInt >= 1
    } else {
        return false
    }
}

fun isButtonEnabled(bufferSize: String, sendingType: SendingType, dataSize: String, communicationType: CommunicationType, fileUri: Uri): Boolean{
    return validateBufferOrDataSize(bufferSize) && (sendingType == SendingType.FILE || validateBufferOrDataSize(dataSize)) && (sendingType == SendingType.DUMMY || communicationType == CommunicationType.DOWNLOAD || !Uri.EMPTY.equals(
        fileUri
    ))
}
