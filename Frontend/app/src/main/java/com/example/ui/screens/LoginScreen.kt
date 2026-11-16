package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.UncialAntiquaFont
import com.example.ui.theme.WorkSansFont
import com.example.ui.viewmodel.BoloDeweViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: BoloDeweViewModel,
    onLoginSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Smooth button press animation states
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "ButtonScale"
    )

    // Smooth entry slide-up and fade-in animations for a premium app feel
    val entranceAlpha = remember { Animatable(0f) }
    val entranceOffsetY = remember { Animatable(50f) }

    LaunchedEffect(Unit) {
        entranceAlpha.animateTo(1f, animationSpec = tween(durationMillis = 800))
    }
    LaunchedEffect(Unit) {
        entranceOffsetY.animateTo(0f, animationSpec = tween(durationMillis = 800, easing = EaseOutCubic))
    }

    // Observe login success signals
    LaunchedEffect(key1 = true) {
        viewModel.isLoginSuccess.collect { success ->
            if (success) {
                onLoginSuccess()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Exquisite background image with full cloud patterns
        Image(
            painter = painterResource(id = R.drawable.img_cloud_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.35f // Slightly translucent for beautiful rich lines on white
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    alpha = entranceAlpha.value,
                    translationY = entranceOffsetY.value
                )
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            // Unicial Antiqua LOGIN header exactly matching screenshot using local asset font
            val loginText = buildAnnotatedString {
                withStyle(SpanStyle(fontSize = 58.sp)) {
                    append("L")
                }
                withStyle(SpanStyle(fontSize = 52.sp)) {
                    append("OGIN")
                }
            }

            Text(
                text = loginText,
                fontFamily = UncialAntiquaFont,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F4E2D), // Gorgeous Chinese imperial green text
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.35f),
                        offset = Offset(3f, 4f),
                        blurRadius = 4f
                    )
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 48.dp)
                    .testTag("login_title")
            )

            // Email/Username Input Card Style
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                placeholder = { 
                    Text(
                        "Email atau Username", 
                        color = Color.LightGray, 
                        fontFamily = WorkSansFont
                    ) 
                },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0xFFF7F7F7),
                    unfocusedContainerColor = Color(0xFFF7F7F7),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
                    .testTag("username_input"),
                textStyle = TextStyle(fontFamily = WorkSansFont, fontSize = 16.sp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Password Input Card Style with "Show" toggler
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { 
                    Text(
                        "Password", 
                        color = Color.LightGray, 
                        fontFamily = WorkSansFont
                    ) 
                },
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                shape = RoundedCornerShape(20.dp),
                trailingIcon = {
                    Text(
                        text = if (showPassword) "Hide" else "Show",
                        fontFamily = WorkSansFont,
                        color = Color(0xFF5DAF77),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable { showPassword = !showPassword }
                            .padding(end = 12.dp)
                            .testTag("show_password_toggle")
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0xFFF7F7F7),
                    unfocusedContainerColor = Color(0xFFF7F7F7),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
                    .testTag("password_input"),
                textStyle = TextStyle(fontFamily = WorkSansFont, fontSize = 16.sp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Validation Error Alert
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    fontFamily = WorkSansFont,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // Green Masuk Pill Button
            Button(
                onClick = {
                    viewModel.login(username, password)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5DAF77) // Foliage Green from the image
                ),
                shape = RoundedCornerShape(25.dp),
                interactionSource = interactionSource,
                modifier = Modifier
                    .graphicsLayer(
                        scaleX = buttonScale,
                        scaleY = buttonScale
                    )
                    .fillMaxWidth()
                    .height(52.dp)
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(25.dp))
                    .testTag("login_button"),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "Masuk",
                        fontFamily = WorkSansFont,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
