package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.LexendZettaFont
import com.example.ui.theme.WorkSansFont
import com.example.ui.viewmodel.BoloDeweViewModel

@Composable
fun GlobalErrorPopUpNotifier(viewModel: BoloDeweViewModel) {
    val notification by viewModel.appNotification.collectAsState()

    AnimatedVisibility(
        visible = notification != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(), // Slide from top
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(Alignment.Top)
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    if (kotlin.math.abs(dragAmount) > 10f) {
                        viewModel.clearNotification()
                    }
                }
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, dragAmount ->
                    if (kotlin.math.abs(dragAmount) > 10f) {
                        viewModel.clearNotification()
                    }
                }
            }
    ) {
        if (notification != null) {
            val notif = notification!!
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F7FA)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .border(1.dp, Color.White, RoundedCornerShape(32.dp))
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Background pattern
                    Image(
                        painter = painterResource(id = R.drawable.img_cloud_background),
                        contentDescription = null,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.40f
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // App Logo Icon inside Notification
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "BD",
                                color = Color.White,
                                fontFamily = LexendZettaFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                letterSpacing = 2.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = notif.title,
                                fontFamily = LexendZettaFont,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333),
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = notif.message,
                                fontFamily = WorkSansFont,
                                color = Color(0xFF555555),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Time
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Baru saja",
                                color = Color.Gray,
                                fontFamily = WorkSansFont,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
