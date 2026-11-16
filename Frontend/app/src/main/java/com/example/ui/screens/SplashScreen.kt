package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LexendZettaFont
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onAnimationFinished: () -> Unit
) {
    // 1. Background color using fully qualified Color Animatable function
    val backgroundColor = remember { androidx.compose.animation.Animatable(Color(0xFF1E1E1E)) }
    
    // 2. Monogram/active letters color
    val activeLetterColor = remember { androidx.compose.animation.Animatable(Color.White) }
    
    // 3. B un-mirrors (scaleX goes from -1f to 1f)
    val bScaleX = remember { Animatable(-1f) }
    
    // 4. "olo" spacer width progress (0f to 1f)
    val oloWidthProgress = remember { Animatable(0f) }
    
    // Symmetrical "ewe" spacer width progress (0f to 1f)
    val eweWidthProgress = remember { Animatable(0f) }
    
    // 5. "olo" entry values
    val oloAlpha = remember { Animatable(0f) }
    val oloScale = remember { Animatable(0f) }
    val oloTranslationY = remember { Animatable(50f) } // start lower for rising bounce effect
    
    // 6. "ewe" entry values
    val eweAlpha = remember { Animatable(0f) }
    val eweScale = remember { Animatable(0f) }
    val eweTranslationY = remember { Animatable(50f) } // start lower for rising bounce effect

    LaunchedEffect(Unit) {
        // Start background transition at 800ms, lasting 700ms
        delay(800)
        backgroundColor.animateTo(
            targetValue = Color.White,
            animationSpec = tween(durationMillis = 700)
        )
    }

    LaunchedEffect(Unit) {
        // Start active letters color transition at 800ms, lasting 700ms
        delay(800)
        activeLetterColor.animateTo(
            targetValue = Color(0xFF20B743),
            animationSpec = tween(durationMillis = 700)
        )
    }

    LaunchedEffect(Unit) {
        // B un-mirror (scaleX from -1 to 1) at 1500ms, lasting 600ms
        delay(1500)
        bScaleX.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600)
        )
    }

    LaunchedEffect(Unit) {
        // Both spacer widths progress (0 to 1) at 1500ms, lasting 700ms for perfect symetry
        delay(1500)
        launch {
            oloWidthProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 700,
                    easing = { t ->
                        // Easing implementation corresponding to easeOutBack
                        val f = t - 1f
                        1f + f * f * (2.70158f * f + 1.70158f)
                    }
                )
            )
        }
        launch {
            eweWidthProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 700,
                    easing = { t ->
                        // Easing implementation corresponding to easeOutBack
                        val f = t - 1f
                        1f + f * f * (2.70158f * f + 1.70158f)
                    }
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        // "olo" text animation triggering at 2100ms with a dynamic spring bounce
        delay(2100)
        launch {
            oloAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 350)
            )
        }
        launch {
            oloScale.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            oloTranslationY.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        // "ewe" text animation triggering with stagger at 2250ms with a dynamic spring bounce
        delay(2250)
        launch {
            eweAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 350)
            )
        }
        launch {
            eweScale.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            eweTranslationY.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        // Complete the splash sequence and proceed to main/login screen after 3500ms
        delay(3500)
        onAnimationFinished()
    }

    val currentOloWidth = (oloWidthProgress.value * 120f).dp
    val currentEweWidth = (eweWidthProgress.value * 120f).dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor.value)
            .testTag("splash_screen_container"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Group 1: "Bolo" in left-center (aligned to end of left container)
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                // "B" Letter - slides left as the "olo" width increases
                Text(
                    text = "B",
                    color = activeLetterColor.value,
                    fontFamily = LexendZettaFont,
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp,
                    modifier = Modifier
                        .graphicsLayer(
                            scaleX = bScaleX.value
                        )
                )

                // "olo" spacer container
                Box(
                    modifier = Modifier
                        .width(currentOloWidth)
                        .wrapContentHeight(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (oloAlpha.value > 0.01f) {
                        Text(
                            text = "olo",
                            color = Color(0xFF20B743),
                            fontFamily = LexendZettaFont,
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier
                                .graphicsLayer(
                                    scaleX = oloScale.value,
                                    scaleY = oloScale.value,
                                    translationY = oloTranslationY.value
                                )
                                .alpha(oloAlpha.value)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp)) // gap between Bolo and Dewe sections

            // Group 2: "Dewe" in right-center (aligned to start of right container)
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                // "D" Letter
                Text(
                    text = "D",
                    color = activeLetterColor.value,
                    fontFamily = LexendZettaFont,
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp
                )

                // "ewe" spacer container
                Box(
                    modifier = Modifier
                        .width(currentEweWidth)
                        .wrapContentHeight(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (eweAlpha.value > 0.01f) {
                        Text(
                            text = "ewe",
                            color = Color(0xFF20B743),
                            fontFamily = LexendZettaFont,
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier
                                .graphicsLayer(
                                    scaleX = eweScale.value,
                                    scaleY = eweScale.value,
                                    translationY = eweTranslationY.value
                                )
                                .alpha(eweAlpha.value)
                        )
                    }
                }
            }
        }
    }
}
