package com.semuju.gemini_image_describer

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semuju.gemini_image_describer.ui.theme.GeminiImageDescriberTheme
import dev.jeziellago.compose.markdowntext.MarkdownText

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.P)
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vm by viewModels<MainViewModel>()
        setContent {
            GeminiImageDescriberTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(title = { Text(text = "Gemini Image Describer") })
                    }
                ) { padding ->

                    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
                    val context = LocalContext.current
                    val uiState by vm.uiState.collectAsState()
                    val photoPicker =
                        rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
                            uris.forEach { uri ->
                                bitmap = MediaStore.Images.Media.getBitmap(
                                    context.contentResolver,
                                    uri
                                )
                            }
                        }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        bitmap?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(16.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Button(
                                onClick = {
                                    if (uiState.explanation.isEmpty())
                                        vm.generateDescription(it)
                                    else {
                                        bitmap = null
                                        vm.clearExplanation()
                                    }
                                },
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .fillMaxWidth()
                            ) {
                                if (uiState.isLoading)
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                else if (uiState.explanation.isEmpty())
                                    Text(text = "Explain Image")
                                else {
                                    Text(text = "Clear Everything")
                                }
                            }
                        } ?: run {
                            IconButton(
                                modifier = Modifier
                                    .width(300.dp)
                                    .height(150.dp),
                                onClick = {
                                    photoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .width(300.dp)
                                        .height(150.dp),
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = null
                                )

                            }
                            Text(text = "Pick an image", fontSize = 20.sp)
                        }

                        if (uiState.explanation.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(32.dp))
                            MarkdownText(
                                markdown = uiState.explanation,
                                modifier = Modifier
                                    .padding(16.dp)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(16.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
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
    GeminiImageDescriberTheme {
        Greeting("Android")
    }
}