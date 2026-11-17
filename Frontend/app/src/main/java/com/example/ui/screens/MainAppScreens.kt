package com.example.ui.screens

import coil.compose.AsyncImage
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.outlined.LocalGroceryStore
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.rotate
import com.example.R
import com.example.data.model.Product
import com.example.data.model.resolveProductImageUrl
import com.example.data.model.SalesTransaction
import com.example.data.model.ForecastItem
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import com.example.ui.theme.WorkSansFont
import com.example.ui.theme.LexendZettaFont
import com.example.ui.viewmodel.BoloDeweViewModel
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MainAppContainer(
    viewModel: BoloDeweViewModel,
    onNavigateToTransactionsDetail: () -> Unit,
    onNavigateToProductDetail: (Int) -> Unit,
    onNavigateToProductCreate: () -> Unit,
    onLogout: () -> Unit
) {
    val selectedTab by viewModel.selectedTab.collectAsState()



    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Laporan Tab (Tab item 0)
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { viewModel.setSelectedTab(0) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Filled.Assessment else Icons.Outlined.Assessment,
                            contentDescription = "Laporan"
                        )
                    },
                    label = { Text("Laporan", fontFamily = WorkSansFont, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0F4E2D),
                        selectedTextColor = Color(0xFF0F4E2D),
                        indicatorColor = Color(0xFFE8F5E9),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )

                // Transaksi Tab (Tab item 1 - Middle, main default)
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { viewModel.setSelectedTab(1) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Filled.LocalGroceryStore else Icons.Outlined.LocalGroceryStore,
                            contentDescription = "Transaksi"
                        )
                    },
                    label = { Text("Transaksi", fontFamily = WorkSansFont, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0F4E2D),
                        selectedTextColor = Color(0xFF0F4E2D),
                        indicatorColor = Color(0xFFE8F5E9),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )

                // Produk Tab (Tab item 2)
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { viewModel.setSelectedTab(2) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) Icons.Filled.Inventory2 else Icons.Outlined.Inventory2,
                            contentDescription = "Produk"
                        )
                    },
                    label = { Text("Produk", fontFamily = WorkSansFont, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0F4E2D),
                        selectedTextColor = Color(0xFF0F4E2D),
                        indicatorColor = Color(0xFFE8F5E9),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            // Background cloud image behind with 10% opacity exactly as instructed!
            Image(
                painter = painterResource(id = R.drawable.img_cloud_background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.10f // Opacity background apps : 10%
            )

            // Animatable key to trigger beautiful entrance slide-up and fade transition upon tab change
            val tabEntranceProgress = remember(selectedTab) { Animatable(0f) }
            LaunchedEffect(selectedTab) {
                tabEntranceProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 400)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        alpha = tabEntranceProgress.value,
                        translationY = 24f * (1f - tabEntranceProgress.value)
                    )
            ) {
                // Dynamic view based on tab selection
                when (selectedTab) {
                    0 -> LaporanView(viewModel)
                    1 -> TransaksiView(
                        viewModel = viewModel,
                        onNavigateToAllTransactions = onNavigateToTransactionsDetail,
                        onNavigateToProductCreate = onNavigateToProductCreate
                    )
                    2 -> ProdukView(
                        viewModel = viewModel,
                        onNavigateToDetail = onNavigateToProductDetail,
                        onNavigateToCreate = onNavigateToProductCreate,
                        onLogout = onLogout
                    )
                }
            }

            // Global pop-up error notification system
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
            ) {
                com.example.ui.components.GlobalErrorPopUpNotifier(viewModel)
            }
        }
    }
}

// ---------------------- LAPORAN TAB VIEW ----------------------
@Composable
fun LaporanView(viewModel: BoloDeweViewModel) {
    val transactions by viewModel.allTransactions.collectAsState()
    val products by viewModel.completeCatalog.collectAsState()
    val forecastList by viewModel.forecastList.collectAsState()

    var isMonthlySelected by remember { mutableStateOf(false) }
    var activePopUpProduct by remember { mutableStateOf<Product?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val filteredTx = getFilteredTransactions(transactions, isMonthlySelected)


    // Filter and group to compute top selling items
    val groupedSales = filteredTx
        .groupBy { tx -> products.find { it.id == tx.product_id }?.nama_produk ?: "ID ${tx.product_id}" }
        .mapValues { entry -> entry.value.size }
        .toList()
        .sortedByDescending { it.second }

    val top5 = groupedSales.take(5)
    val top5Names = top5.map { it.first }.toSet()

    // Determine products below Top 5
    val productsBelowTop5 = products.filter { it.nama_produk !in top5Names }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "LAPORAN",
                fontFamily = WorkSansFont,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF0F4E2D),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .testTag("laporan_title")
            )
        }


//        // Detailed graphic horizontal progress bar comparing dynamic sales of the Top 5 products
//        item {
//            Card(
//                shape = RoundedCornerShape(20.dp),
//                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7)),
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .shadow(1.dp, RoundedCornerShape(20.dp))
//                    .border(1.dp, Color(0xFFEDEDED), RoundedCornerShape(20.dp))
//            ) {
//                Column(modifier = Modifier.padding(18.dp)) {
//                    Text(
//                        text = "Top 5 Trend Produk Paling Laku",
//                        fontFamily = WorkSansFont,
//                        fontWeight = FontWeight.Bold,
//                        color = Color.Black,
//                        fontSize = 15.sp
//                    )
//                    Text(
//                        text = if (isMonthlySelected) "Periode Hari Ini s/d 30 Hari Lalu" else "Periode Hari Ini s/d 7 Hari Lalu",
//                        fontFamily = WorkSansFont,
//                        color = Color.Gray,
//                        fontSize = 11.sp,
//                        modifier = Modifier.padding(bottom = 16.dp)
//                    )
//
//                    if (top5.isEmpty()) {
//                        Box(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(100.dp),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Text(
//                                text = "Belum ada penjualan di periode ini",
//                                fontFamily = WorkSansFont,
//                                color = Color.Gray,
//                                fontSize = 12.sp
//                            )
//                        }
//                    } else {
//                        val maxQtyVal = top5.maxOfOrNull { it.second } ?: 1
//                        top5.forEachIndexed { idx, item ->
//                            val currentRatio = item.second.toFloat() / maxQtyVal.toFloat()
//                            Row(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(vertical = 8.dp),
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                // Dynamic Rank container
//                                Box(
//                                    modifier = Modifier
//                                        .size(24.dp)
//                                        .clip(CircleShape)
//                                        .background(if (idx == 0) Color(0xFF0F4E2D) else Color(0xFFEFEFEF)),
//                                    contentAlignment = Alignment.Center
//                                ) {
//                                    Text(
//                                        text = "${idx + 1}",
//                                        fontFamily = LexendZettaFont,
//                                        fontWeight = FontWeight.Bold,
//                                        fontSize = 10.sp,
//                                        color = if (idx == 0) Color.White else Color.Black
//                                    )
//                                }
//
//                                Spacer(modifier = Modifier.width(12.dp))
//
//                                Column(modifier = Modifier.weight(1f)) {
//                                    Row(
//                                        modifier = Modifier.fillMaxWidth(),
//                                        horizontalArrangement = Arrangement.SpaceBetween,
//                                        verticalAlignment = Alignment.CenterVertically
//                                    ) {
//                                        Text(
//                                            text = item.first,
//                                            fontFamily = WorkSansFont,
//                                            fontWeight = FontWeight.Medium,
//                                            fontSize = 13.sp,
//                                            color = Color.Black,
//                                            maxLines = 1,
//                                            overflow = TextOverflow.Ellipsis,
//                                            modifier = Modifier.weight(1f, fill = false)
//                                        )
//                                        Text(
//                                            text = "${item.second} Pcs",
//                                            fontFamily = WorkSansFont,
//                                            fontWeight = FontWeight.Bold,
//                                            fontSize = 13.sp,
//                                            color = Color(0xFF0F4E2D)
//                                        )
//                                    }
//                                    Spacer(modifier = Modifier.height(6.dp))
//                                    Box(
//                                        modifier = Modifier
//                                            .fillMaxWidth()
//                                            .height(8.dp)
//                                            .clip(RoundedCornerShape(4.dp))
//                                            .background(Color(0xFFEBEBEB))
//                                    ) {
//                                        Box(
//                                            modifier = Modifier
//                                                .fillMaxWidth(currentRatio)
//                                                .fillMaxHeight()
//                                                .clip(RoundedCornerShape(4.dp))
//                                                .background(if (idx == 0) Color(0xFF0F4E2D) else Color(0xFF5DAF77))
//                                        )
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }

        // Line Chart component to visualize product sales trends
        item {
            ProductSalesTrendLineChart(transactions = transactions)
        }

        // Card: Prediksi Trend Moment (AI Forecast)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F9F5)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color(0xFFD0E6D5), RoundedCornerShape(16.dp))
                    .testTag("forecast_trend_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Prediksi Trend Moment",
                                fontFamily = WorkSansFont,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F4E2D),
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Prakiraan stock kebutuhan berdasarkan tren moment server",
                                fontFamily = WorkSansFont,
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                        var isRefreshing by remember { mutableStateOf(false) }
                        val rotationAngle by animateFloatAsState(
                            targetValue = if (isRefreshing) 360f else 0f,
                            animationSpec = tween(durationMillis = 800, easing = LinearEasing),
                            finishedListener = { isRefreshing = false }
                        )

                        IconButton(
                            onClick = { 
                                isRefreshing = true
                                viewModel.fetchForecast() 
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Refresh Ramalan",
                                tint = Color(0xFF0F4E2D),
                                modifier = Modifier
                                    .size(18.dp)
                                    .rotate(rotationAngle)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (forecastList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF0F4E2D), strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            forecastList.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White, RoundedCornerShape(8.dp))
                                        .border(1.dp, Color(0xFFECECEC), RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.nama_produk ?: "",
                                            fontFamily = WorkSansFont,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "SKU: ${item.sku ?: item.kode_sku ?: ""}",
                                            fontFamily = WorkSansFont,
                                            color = Color.Gray,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column(horizontalAlignment = Alignment.End) {
                                        val statusString = item.status ?: ""
                                        if (statusString.contains("Sukses", ignoreCase = true) || statusString.contains("Online", ignoreCase = true)) {
                                            Text(
                                                text = "Saran: +${item.prediksi_bulan_depan ?: item.ramalan_final ?: 0} Pcs",
                                                fontFamily = WorkSansFont,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F4E2D),
                                                fontSize = 13.sp
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .padding(top = 2.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFFE8F5E9))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "REKOMENDASI",
                                                    fontFamily = WorkSansFont,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF2E7D32),
                                                    fontSize = 8.sp
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = "N/A",
                                                fontFamily = WorkSansFont,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Gray,
                                                fontSize = 13.sp
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .padding(top = 2.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFFFFEBEE))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "DATA KURANG",
                                                    fontFamily = WorkSansFont,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFC62828),
                                                    fontSize = 8.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Subheader: Trend Products Below Top 5
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "Trend Produk Lainnya",
                    fontFamily = WorkSansFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.Black
                )
            }
        }

        if (productsBelowTop5.isEmpty()) {
            item {
                Text(
                    text = "Semua produk masuk dalam Top 5",
                    fontFamily = WorkSansFont,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        } else {
            items(productsBelowTop5) { prod ->
                BelowTop5ProductRowCard(
                    product = prod,
                    isMonthly = isMonthlySelected,
                    transactions = transactions,
                    onOpenTrend = { activePopUpProduct = it }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    // Interactive chart Dialog pop-up
    activePopUpProduct?.let { prod ->
        ProductTrendPopUpDialog(
            product = prod,
            transactions = transactions,
            onDismiss = { activePopUpProduct = null }
        )
    }
}

@Composable
fun BelowTop5ProductRowCard(
    product: Product,
    isMonthly: Boolean,
    transactions: List<SalesTransaction>,
    onOpenTrend: (Product) -> Unit
) {
    val filteredTx = getFilteredTransactions(transactions, isMonthly)
    val qtySold = filteredTx.filter { it.product_id == product.id }.size

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFEDEDED), RoundedCornerShape(16.dp))
            .clickable { onOpenTrend(product) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product photo circular container
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                if (product.gambar != null) {
                    AsyncImage(
                        model = resolveProductImageUrl(product.gambar),
                        contentDescription = "Product Image preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Inventory2,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.nama_produk,
                    fontFamily = WorkSansFont,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Jumlah Stock terjual: $qtySold pcs",
                    fontFamily = WorkSansFont,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Extended icon for pop up chart trend penjualan
            IconButton(
                onClick = { onOpenTrend(product) },
                modifier = Modifier
                    .size(36.dp)
                    .testTag("trend_icon_${product.id}")
            ) {
                Icon(
                    imageVector = Icons.Filled.TrendingUp,
                    contentDescription = "Pop Up Trend Chart",
                    tint = Color(0xFF0F4E2D)
                )
            }
        }
    }
}

// ---------------------- INTERACTIVE POP-UP CHART DIALOG ----------------------
@Composable
fun ProductTrendPopUpDialog(
    product: Product,
    transactions: List<SalesTransaction>,
    onDismiss: () -> Unit
) {
    var popUpIsMonthly by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Tutup",
                    color = Color(0xFF0F4E2D),
                    fontWeight = FontWeight.Bold,
                    fontFamily = WorkSansFont
                )
            }
        },
        title = {
            Text(
                text = "Tren Penjualan Produk",
                fontFamily = WorkSansFont,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = product.nama_produk,
                    fontFamily = WorkSansFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF0F4E2D)
                )

                // Inline selector for dialogue timeframe: Last 7 Days vs Last 30 Days
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5F5F5))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("1 Minggu", "1 Bulan").forEachIndexed { index, label ->
                        val isSel = (index == 1) == popUpIsMonthly
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) Color(0xFF0F4E2D) else Color.Transparent)
                                .clickable { popUpIsMonthly = (index == 1) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) Color.White else Color.Gray,
                                fontFamily = WorkSansFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Dynamic chart computation matching sold quantities
                val filteredTx = getFilteredTransactions(transactions, popUpIsMonthly)
                val prodTx = filteredTx.filter { it.product_id == product.id }
                val dailyQuantitySum = prodTx.groupBy { it.tanggal }.mapValues { entry -> entry.value.size }

                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val targetDayLabels = mutableListOf<String>()
                val calendar = Calendar.getInstance()

                val latestTxDate = transactions.mapNotNull {
                    try { sdf.parse(it.tanggal) } catch (e: Exception) { null }
                }.maxByOrNull { it.time }

                if (latestTxDate != null) {
                    calendar.time = latestTxDate
                }

                val daysCount = if (popUpIsMonthly) 30 else 7
                for (i in (daysCount - 1) downTo 0) {
                    val c = Calendar.getInstance().apply {
                        time = calendar.time
                        add(Calendar.DAY_OF_YEAR, -i)
                    }
                    targetDayLabels.add(sdf.format(c.time))
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Color(0xFFF7F7F7), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    if (dailyQuantitySum.isEmpty()) {
                        Text(
                            text = "Tidak ada penjualan dalam jangka waktu ini.",
                            fontFamily = WorkSansFont,
                            color = Color.Gray,
                            fontSize = 11.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val visualDataPoints = if (popUpIsMonthly) {
                                // Group 30 days into 5 bars representing 6 days groups to prevent overcrowding!
                                List(5) { groupIdx ->
                                    val daysInGroup = targetDayLabels.subList(groupIdx * 6, (groupIdx + 1) * 6)
                                    val groupedSoldVal = daysInGroup.sumOf { dailyQuantitySum[it] ?: 0 }
                                    "P${groupIdx + 1}" to groupedSoldVal
                                }
                            } else {
                                targetDayLabels.map { d ->
                                    val dayName = d.substringAfterLast("-") // e.g. "04"
                                    dayName to (dailyQuantitySum[d] ?: 0)
                                }
                            }

                            val maxSalesCount = visualDataPoints.maxOf { it.second }.coerceAtLeast(1)
                            visualDataPoints.forEach { pt ->
                                val barRatio = pt.second.toFloat() / maxSalesCount.toFloat()
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = if (pt.second > 0) "${pt.second}" else "",
                                        fontFamily = WorkSansFont,
                                        fontSize = 9.sp,
                                        color = Color(0xFF0F4E2D),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(16.dp)
                                            .height((70 * barRatio).dp)
                                            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                            .background(if (pt.second > 0) Color(0xFF0F4E2D) else Color(0xFFEFEFEF))
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = pt.first,
                                        fontFamily = WorkSansFont,
                                        fontSize = 9.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}


// ---------------------- TRANSAKSI TAB VIEW ----------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransaksiView(
    viewModel: BoloDeweViewModel,
    onNavigateToAllTransactions: () -> Unit,
    onNavigateToProductCreate: () -> Unit
) {
    val activeProducts by viewModel.allProducts.collectAsState()
    val completeCatalog by viewModel.completeCatalog.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    val todayDateString = remember {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.format(Date())
    }
    var dateString by remember { mutableStateOf(todayDateString) }
    var priceOverride by remember { mutableStateOf("") }
    var quantityValue by remember { mutableStateOf("1") }

    var showDatePickerDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    val filteredProducts = remember(searchQuery, activeProducts) {
        if (searchQuery.trim().isEmpty()) {
            emptyList()
        } else {
            activeProducts.filter { it.nama_produk.contains(searchQuery, ignoreCase = true) || it.kode_sku.contains(searchQuery, ignoreCase = true) }
        }
    }

    var selectedRecFilter by remember { mutableStateOf(0) } // 0: Hari Ini, 1: Kemarin, 2: Kemarin Lusa, 3: Seminggu

    // Mengelompokkan transaksi yang sudah difilter berdasarkan product_id
    val groupedTransactions = remember(transactions, completeCatalog, selectedRecFilter) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val refDate = getReferenceDate(transactions)
        val baseCal = Calendar.getInstance().apply { time = refDate }
        val today = sdf.format(baseCal.time)

        val calYesterday = Calendar.getInstance().apply {
            time = baseCal.time
            add(Calendar.DATE, -1)
        }
        val yesterday = sdf.format(calYesterday.time)

        val calLusa = Calendar.getInstance().apply {
            time = baseCal.time
            add(Calendar.DATE, -2)
        }
        val yesterdayLusa = sdf.format(calLusa.time)

        val baseFiltered = when (selectedRecFilter) {
            0 -> transactions.filter { it.tanggal == today }
            1 -> transactions.filter { it.tanggal == yesterday }
            2 -> transactions.filter { it.tanggal == yesterdayLusa }
            3 -> { // Seminggu: 7 hari ke belakang dari referensi sekarang
                val cutoff = Calendar.getInstance().apply {
                    time = baseCal.time
                    add(Calendar.DAY_OF_YEAR, -7)
                }
                transactions.filter { tx ->
                    val d = try { sdf.parse(tx.tanggal) } catch (e: Exception) { null }
                    d != null && !d.before(cutoff.time) && !d.after(baseCal.time)
                }
            }
            else -> transactions
        }

        val activeProductIds = completeCatalog.map { it.id }.toSet()
        baseFiltered
            .filter { it.product_id in activeProductIds }
            .groupBy { it.product_id }
            .values
            .map { txGroup ->
                val latestTx = txGroup.maxByOrNull { it.tanggal } ?: txGroup.first()
                // Mengembalikan Pair berisi SalesTransaction representatif dan total jumlah item terjual
                Pair(latestTx, txGroup.size)
            }
            .sortedByDescending { it.second }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "TRANSAKSI",
                fontFamily = WorkSansFont,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF0F4E2D),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .testTag("transaksi_title")
            )
        }

        // Search Bar item
        item {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = if (selectedProduct != null) selectedProduct!!.nama_produk else searchQuery,
                    onValueChange = {
                        selectedProduct = null
                        searchQuery = it
                    },
                    placeholder = { Text("Cari Produk...", color = Color.Gray, fontFamily = WorkSansFont) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color.Gray) },
                    trailingIcon = {
                        if (selectedProduct != null || searchQuery.isNotEmpty()) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Clear",
                                modifier = Modifier.clickable {
                                    selectedProduct = null
                                    searchQuery = ""
                                }
                            )
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFCEE5D0),
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedContainerColor = Color(0xFFF9F9F9),
                        unfocusedContainerColor = Color(0xFFF9F9F9),
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_barang_input")
                )

                // Search Dropdown matches Screenshot 3, visual 3: Suggestion showing matched items
                if (filteredProducts.isNotEmpty() && selectedProduct == null) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Column {
                            filteredProducts.take(4).forEach { prod ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedProduct = prod
                                            searchQuery = prod.nama_produk
                                        }
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Inventory2, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = prod.nama_produk,
                                        fontFamily = WorkSansFont,
                                        fontSize = 14.sp,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                } else if (searchQuery.isNotEmpty() && filteredProducts.isEmpty() && selectedProduct == null) {
                    // Visual matches bottom of screen 3 (search not found): Show "+ Produk Baru"
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Filled.SearchOff, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Produk tidak ditemukan",
                                fontFamily = WorkSansFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                "Coba kata kunci lain atau tambah produk baru di bawah.",
                                fontFamily = WorkSansFont,
                                color = Color.Gray,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onNavigateToProductCreate,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F4E2D)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("+ Produk Baru", fontFamily = WorkSansFont, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Transaction Details Input Fields form layout
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFEDEDED), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Date input
                    Text(
                        "Tanggal (Wajib/Hari Ini)",
                        fontFamily = WorkSansFont,
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = dateString,
                        onValueChange = { dateString = it },
                        readOnly = true,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.DateRange,
                                contentDescription = "Pilih Tanggal",
                                tint = Color(0xFF0F4E2D),
                                modifier = Modifier.clickable { showDatePickerDialog = true }
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color(0xFFF0F0F0),
                            unfocusedContainerColor = Color(0xFFF0F0F0),
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePickerDialog = true }
                            .padding(top = 4.dp)
                            .testTag("date_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quantity input
                    Text(
                        "Jumlah (Wajib)",
                        fontFamily = WorkSansFont,
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = quantityValue,
                        onValueChange = { quantityValue = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color(0xFFF0F0F0),
                            unfocusedContainerColor = Color(0xFFF0F0F0),
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .testTag("quantity_input")
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // "Tambah" Transaction Button
                    Button(
                        onClick = {
                            val finalProd = selectedProduct
                            if (finalProd != null) {
                                viewModel.addSaleTransaction(
                                    productId = finalProd.id,
                                    dateString = dateString,
                                    quantity = quantityValue.toIntOrNull() ?: 1
                                )

                                // Clear forms
                                selectedProduct = null
                                searchQuery = ""
                                priceOverride = ""
                                quantityValue = "1"
                                dateString = todayDateString
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5DAF77)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("add_transaction_button"),
                        enabled = selectedProduct != null
                    ) {
                        Text(
                            text = "Tambah",
                            fontFamily = WorkSansFont,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Header: "Transaksi Terkini" matches Page 3 layout
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transaksi Terkini",
                    fontFamily = WorkSansFont,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontSize = 14.sp
                )
                Text(
                    text = "Lihat Semua",
                    fontFamily = WorkSansFont,
                    color = Color(0xFF0F4E2D),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .clickable { onNavigateToAllTransactions() }
                        .testTag("lihat_semua_btn")
                )
            }
        }

        // Filtes pills row: "Hari Ini", "Kemarin", "Kemarin Lusa", "Seminggu"
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filterLabels = listOf("Hari Ini", "Kemarin", "Kemarin Lusa", "Seminggu")
                filterLabels.forEachIndexed { idx, label ->
                    val isActive = selectedRecFilter == idx
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isActive) Color(0xFF0F4E2D) else Color(0xFFF0F0F0))
                            .clickable { selectedRecFilter = idx }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("filter_pill_$idx")
                    ) {
                        Text(
                            text = label,
                            fontFamily = WorkSansFont,
                            color = if (isActive) Color.White else Color.Gray,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        if (groupedTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Belum ada transaksi di rentang waktu ini.",
                        fontFamily = WorkSansFont,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Menggunakan items dengan Pair (Transaction & Total Qty)
            items(groupedTransactions) { (item, totalQty) ->
                val prod = completeCatalog.find { it.id == item.product_id }
                TransactionRowCard(
                    transaction = item,
                    productName = prod?.nama_produk ?: "ID ${item.product_id}",
                    productImage = prod?.gambar,
                    totalQuantity = totalQty, // Kirim total kuantitas
                    onDelete = { viewModel.deleteTransaction(item.id_transaksi) }
                )
            }
        }
    }

    if (showDatePickerDialog) {
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedTimestamp = datePickerState.selectedDateMillis
                    if (selectedTimestamp != null) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        dateString = sdf.format(Date(selectedTimestamp))
                    }
                    showDatePickerDialog = false
                }) {
                    Text("Pilih", color = Color(0xFF0F4E2D), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Batal", color = Color.Gray)
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = Color(0xFF0F4E2D),
                    selectedDayContentColor = Color.White,
                    todayContentColor = Color(0xFF0F4E2D),
                    todayDateBorderColor = Color(0xFF0F4E2D)
                )
            )
        }
    }
}

@Composable
fun TransactionRowCard(
    transaction: SalesTransaction,
    productName: String,
    productImage: String? = null,
    totalQuantity: Int = 1, // Parameter penampung total kuantitas
    onDelete: (() -> Unit)? = null
) {
    var showDeleteTransactionDialog by remember { mutableStateOf(false) }

    if (showDeleteTransactionDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteTransactionDialog = false },
            title = { Text("Hapus Transaksi?", fontFamily = WorkSansFont, fontWeight = FontWeight.Bold, color = Color.Red) },
            text = { Text("Apakah Anda yakin ingin menghapus transaksi penjualan ini?", fontFamily = WorkSansFont) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteTransactionDialog = false
                    onDelete?.invoke()
                }) {
                    Text("Hapus", color = Color.Red, fontFamily = WorkSansFont, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteTransactionDialog = false }) {
                    Text("Batal", color = Color.Gray, fontFamily = WorkSansFont)
                }
            }
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFECECEC), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFEFEFEF)),
                contentAlignment = Alignment.Center
            ) {
                if (productImage != null) {
                    AsyncImage(
                        model = resolveProductImageUrl(productImage),
                        contentDescription = "Product Image preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Inventory2,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = productName,
                    fontFamily = WorkSansFont,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = transaction.tanggal,
                    fontFamily = WorkSansFont,
                    color = Color.Gray,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Menampilkan jumlah total kuantitas produk yang terjual secara dinamis
            Text(
                text = "$totalQuantity Pcs",
                fontFamily = WorkSansFont,
                color = Color(0xFF0F4E2D),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            if (onDelete != null) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { showDeleteTransactionDialog = true }, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "Hapus Transaksi", tint = Color.Red, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}


// ---------------------- PRODUK TAB VIEW ----------------------
@Composable
fun ProdukView(
    viewModel: BoloDeweViewModel,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToCreate: () -> Unit,
    onLogout: () -> Unit
) {
    val activeProducts by viewModel.allProducts.collectAsState()
    val completeCatalog by viewModel.completeCatalog.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()
    
    var searchProdukQuery by remember { mutableStateOf("") }
    
    val filteredProducts = activeProducts.filter {
        it.nama_produk.contains(searchProdukQuery, ignoreCase = true) || it.kode_sku.contains(searchProdukQuery, ignoreCase = true)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Logout button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.size(48.dp))
                
                Text(
                    text = "PRODUK",
                    fontFamily = WorkSansFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF0F4E2D),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("barang_view_title")
                )

                IconButton(
                    onClick = onLogout,
                    modifier = Modifier.testTag("logout_btn")
                ) {
                    Icon(Icons.Filled.Logout, contentDescription = "Logout", tint = Color.Gray)
                }
            }

            // Search product box
            OutlinedTextField(
                value = searchProdukQuery,
                onValueChange = { searchProdukQuery = it },
                placeholder = { Text("Cari Produk...", color = Color.Gray, fontFamily = WorkSansFont) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color.Gray) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFCEE5D0),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedContainerColor = Color(0xFFF9F9F9),
                    unfocusedContainerColor = Color(0xFFF9F9F9),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("barang_search_input")
            )

            // Content list
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredProducts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 44.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Belum ada produk yang cocok.",
                                fontFamily = WorkSansFont,
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    items(filteredProducts) { item ->
                        // Calculate sold stock based on current timeframes filter
                        val soldQty = getSoldQuantityForProduct(item.id, transactions, timeframe = "")
                        ProductItemRowCard(item, soldQty, onClick = { onNavigateToDetail(item.id) })
                    }
                }
            }
        }

        // Floating Action Button matching bottom layout for additions
        FloatingActionButton(
            onClick = onNavigateToCreate,
            containerColor = Color(0xFF0F4E2D),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("fab_add_product")
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Product")
        }
    }
}

// --- Helper functions for dynamic Report Calculations ---
fun getReferenceDate(transactions: List<SalesTransaction>): Date {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return transactions.mapNotNull {
        try { sdf.parse(it.tanggal) } catch (e: Exception) { null }
    }.maxByOrNull { it.time } ?: Date()
}

fun getFilteredTransactions(transactions: List<SalesTransaction>, isMonthly: Boolean): List<SalesTransaction> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val latestTxDate = getReferenceDate(transactions)

    val cutoffCal = Calendar.getInstance().apply {
        time = latestTxDate
        if (isMonthly) {
            add(Calendar.DAY_OF_YEAR, -30)
        } else {
            add(Calendar.DAY_OF_YEAR, -7)
        }
    }

    return transactions.filter { tx ->
        try {
            val d = sdf.parse(tx.tanggal)
            d != null && !d.before(cutoffCal.time) && !d.after(latestTxDate)
        } catch (e: Exception) {
            true // fallback
        }
    }
}

// Helper function to calculate sold quantity for a product under timeframe filters
private fun getSoldQuantityForProduct(
    productId: Int,
    transactions: List<com.example.data.model.SalesTransaction>,
    timeframe: String
): Int {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    val refDate = getReferenceDate(transactions)

    val baseCal = java.util.Calendar.getInstance().apply { time = refDate }
    val todayStr = sdf.format(baseCal.time)

    val yesterdayCal = java.util.Calendar.getInstance().apply {
        time = refDate
        add(java.util.Calendar.DATE, -1)
    }
    val yesterdayStr = sdf.format(yesterdayCal.time)

    val kemarinLusaCal = java.util.Calendar.getInstance().apply {
        time = refDate
        add(java.util.Calendar.DATE, -2)
    }
    val kemarinLusaStr = sdf.format(kemarinLusaCal.time)

    val weekAgoCal = java.util.Calendar.getInstance().apply {
        time = refDate
        add(java.util.Calendar.DATE, -6)
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }

    val refMidnight = java.util.Calendar.getInstance().apply {
        time = refDate
        set(java.util.Calendar.HOUR_OF_DAY, 23)
        set(java.util.Calendar.MINUTE, 59)
        set(java.util.Calendar.SECOND, 59)
    }

    return transactions.filter { tx ->
        tx.product_id == productId && when (timeframe) {
            "Hari Ini" -> tx.tanggal == todayStr
            "Kemarin" -> tx.tanggal == yesterdayStr
            "Kemarin Lusa" -> tx.tanggal == kemarinLusaStr
            "Seminggu" -> {
                try {
                    val txDate = sdf.parse(tx.tanggal)
                    txDate != null && !txDate.before(weekAgoCal.time) && !txDate.after(refMidnight.time)
                } catch (e: Exception) {
                    false
                }
            }
            else -> true
        }
    }.size
}

@Composable
fun ProductItemRowCard(product: Product, soldQty: Int, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(1.dp, RoundedCornerShape(20.dp))
            .border(1.dp, Color(0xFFEDEDED), RoundedCornerShape(20.dp))
            .testTag("product_card_${product.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product photo container
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF1F1F1)),
                contentAlignment = Alignment.Center
            ) {
                if (product.gambar != null) {
                    AsyncImage(
                        model = resolveProductImageUrl(product.gambar),
                        contentDescription = "Product Image Thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Inventory2,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.nama_produk,
                    fontFamily = WorkSansFont,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Terjual: $soldQty pcs",
                        fontFamily = WorkSansFont,
                        color = Color(0xFF0F4E2D),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}


// --- Utility functions ---
fun formatIDR(amount: Double): String {
    return try {
        val formatter = NumberFormat.getInstance(Locale("in", "ID"))
        formatter.format(amount)
    } catch (e: Exception) {
        amount.toInt().toString()
    }
}


// --- TREND LINE CHART COMPONENT & DATA MODELS ---

data class ChartDataPoint(
    val xLabel: String,
    val value: Int,
    val tooltipLabel: String
)

@Composable
fun ProductSalesTrendLineChart(transactions: List<SalesTransaction>) {
    val filters = listOf("Harian", "Mingguan", "Bulanan", "Tahunan")
    var activeFilter by remember { mutableStateOf("Mingguan") }
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }

    val points = remember(transactions, activeFilter) {
        val list = mutableListOf<ChartDataPoint>()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        selectedPointIndex = null

        when (activeFilter) {
            "Harian" -> {
                val labelSdf = SimpleDateFormat("dd/MM", Locale.getDefault())
                for (i in 6 downTo 0) {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, -i)
                    val dateStr = sdf.format(cal.time)
                    val displayLabel = labelSdf.format(cal.time)
                    val totalQty = transactions.filter { it.tanggal == dateStr }.size
                    list.add(ChartDataPoint(displayLabel, totalQty, "Tanggal: $dateStr\nTerjual: $totalQty Pcs"))
                }
            }
            "Mingguan" -> {
                val labelSdf = SimpleDateFormat("dd MMM", Locale.getDefault())
                for (i in 3 downTo 0) {
                    val calStart = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -((i + 1) * 7 - 1)) }
                    val calEnd = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -(i * 7)) }
                    val startStr = labelSdf.format(calStart.time)
                    val endStr = labelSdf.format(calEnd.time)
                    
                    val totalQty = transactions.filter { tx ->
                        try {
                            val txDate = sdf.parse(tx.tanggal)
                            if (txDate != null) {
                                val txCal = Calendar.getInstance().apply { time = txDate }
                                val startCal = Calendar.getInstance().apply { 
                                    time = calStart.time
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                val endCal = Calendar.getInstance().apply {
                                    time = calEnd.time
                                    set(Calendar.HOUR_OF_DAY, 23)
                                    set(Calendar.MINUTE, 59)
                                    set(Calendar.SECOND, 59)
                                    set(Calendar.MILLISECOND, 999)
                                }
                                !txCal.before(startCal) && !txCal.after(endCal)
                            } else false
                        } catch (e: Exception) {
                            false
                        }
                    }.size
                    
                    list.add(ChartDataPoint("Mgg ${4 - i}", totalQty, "Minggu ke-${4 - i}\n($startStr - $endStr)\nTerjual: $totalQty Pcs"))
                }
            }
            "Bulanan" -> {
                val monthLabelSdf = SimpleDateFormat("MMM yy", Locale.getDefault())
                val monthKeySdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
                for (i in 5 downTo 0) {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.MONTH, -i)
                    val monthKey = monthKeySdf.format(cal.time)
                    val displayLabel = monthLabelSdf.format(cal.time)
                    val totalQty = transactions.filter { it.tanggal.startsWith(monthKey) }.size
                    list.add(ChartDataPoint(displayLabel, totalQty, "Bulan: $displayLabel\nTerjual: $totalQty Pcs"))
                }
            }
            "Tahunan" -> {
                for (i in 3 downTo 0) {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.YEAR, -i)
                    val yearStr = cal.get(Calendar.YEAR).toString()
                    val totalQty = transactions.filter { it.tanggal.startsWith(yearStr) }.size
                    list.add(ChartDataPoint(yearStr, totalQty, "Tahun: $yearStr\nTerjual: $totalQty Pcs"))
                }
            }
        }
        list
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(24.dp))
            .border(1.dp, Color(0xFFEDEDED), RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Tren Penjualan",
                        fontFamily = WorkSansFont,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Statistik stok terjual",
                        fontFamily = WorkSansFont,
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
                
                Icon(
                    imageVector = Icons.Filled.TrendingUp,
                    contentDescription = null,
                    tint = Color(0xFF0F4E2D),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                filters.forEach { filter ->
                    val isSelected = activeFilter == filter
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color.White else Color.Transparent)
                            .clickable { activeFilter = filter }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = filter,
                            fontFamily = WorkSansFont,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp,
                            color = if (isSelected) Color(0xFF0F4E2D) else Color.Gray
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(points) {
                            detectTapGestures { offset ->
                                if (points.isNotEmpty()) {
                                    val paddingLeft = 32.dp.toPx()
                                    val paddingRight = 16.dp.toPx()
                                    val chartWidth = size.width - paddingLeft - paddingRight
                                    val segmentWidth = chartWidth / (points.size - 1).coerceAtLeast(1)
                                    var closestIndex = 0
                                    var minDistance = Float.MAX_VALUE
                                    for (index in points.indices) {
                                        val xPos = paddingLeft + index * segmentWidth
                                        val dist = kotlin.math.abs(offset.x - xPos)
                                        if (dist < minDistance) {
                                            minDistance = dist
                                            closestIndex = index
                                        }
                                    }
                                    selectedPointIndex = closestIndex
                                }
                            }
                        }
                ) {
                    if (points.isNotEmpty()) {
                        val paddingLeft = 32.dp.toPx()
                        val paddingRight = 16.dp.toPx()
                        val paddingTop = 16.dp.toPx()
                        val paddingBottom = 24.dp.toPx()

                        val chartWidth = size.width - paddingLeft - paddingRight
                        val chartHeight = size.height - paddingTop - paddingBottom

                        val maxVal = points.maxOfOrNull { it.value } ?: 0
                        val gridMax = if (maxVal == 0) 10 else (((maxVal / 5) + 1) * 5)
                        val segmentWidth = chartWidth / (points.size - 1).coerceAtLeast(1)

                        val gridLinesCount = 4
                        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        for (i in 0..gridLinesCount) {
                            val y = paddingTop + i * (chartHeight / gridLinesCount)
                            val gridValue = gridMax - (i * (gridMax / gridLinesCount))
                            
                            drawLine(
                                color = Color(0xFFECECEC),
                                start = Offset(paddingLeft, y),
                                end = Offset(size.width - paddingRight, y),
                                pathEffect = dashEffect,
                                strokeWidth = 1.dp.toPx()
                            )
                            
                            drawContext.canvas.nativeCanvas.drawText(
                                "$gridValue",
                                paddingLeft - 8.dp.toPx(),
                                y + 4.dp.toPx(),
                                android.graphics.Paint().apply {
                                    color = android.graphics.Color.GRAY
                                    textSize = 10.sp.toPx()
                                    textAlign = android.graphics.Paint.Align.RIGHT
                                    typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
                                }
                            )
                        }

                        val coords = points.mapIndexed { index, pt ->
                            val x = paddingLeft + index * segmentWidth
                            val ratio = if (gridMax == 0) 0f else pt.value.toFloat() / gridMax.toFloat()
                            val y = paddingTop + chartHeight - (ratio * chartHeight)
                            Offset(x, y)
                        }

                        if (coords.isNotEmpty()) {
                            val areaPath = Path().apply {
                                moveTo(coords.first().x, paddingTop + chartHeight)
                                coords.forEach { lineTo(it.x, it.y) }
                                lineTo(coords.last().x, paddingTop + chartHeight)
                                close()
                            }
                            drawPath(
                                path = areaPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF5DAF77).copy(alpha = 0.3f),
                                        Color(0xFF5DAF77).copy(alpha = 0.0f)
                                    ),
                                    startY = paddingTop,
                                    endY = paddingTop + chartHeight
                                )
                            )
                        }

                        if (coords.size > 1) {
                            val strokePath = Path().apply {
                                moveTo(coords.first().x, coords.first().y)
                                for (i in 1 until coords.size) {
                                    val prev = coords[i - 1]
                                    val curr = coords[i]
                                    val controlX1 = prev.x + (curr.x - prev.x) / 3f
                                    val controlY1 = prev.y
                                    val controlX2 = prev.x + 2f * (curr.x - prev.x) / 3f
                                    val controlY2 = curr.y
                                    cubicTo(controlX1, controlY1, controlX2, controlY2, curr.x, curr.y)
                                }
                            }
                            drawPath(
                                path = strokePath,
                                color = Color(0xFF0F4E2D),
                                style = Stroke(
                                    width = 3.dp.toPx(),
                                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                                )
                            )
                        }

                        coords.forEachIndexed { index, offset ->
                            drawContext.canvas.nativeCanvas.drawText(
                                points[index].xLabel,
                                offset.x,
                                size.height - 4.dp.toPx(),
                                android.graphics.Paint().apply {
                                    color = android.graphics.Color.DKGRAY
                                    textSize = 9.sp.toPx()
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
                                }
                            )

                            val isSelected = index == selectedPointIndex
                            if (isSelected) {
                                drawCircle(
                                    color = Color(0xFF0F4E2D).copy(alpha = 0.2f),
                                    radius = 11.dp.toPx(),
                                    center = offset
                                )
                            }
                            
                            drawCircle(
                                color = Color(0xFF0F4E2D),
                                radius = 5.dp.toPx(),
                                center = offset
                            )
                            
                            drawCircle(
                                color = Color.White,
                                radius = 2.5.dp.toPx(),
                                center = offset
                            )
                        }
                    }
                }
            }

            val activeIndex = selectedPointIndex
            if (activeIndex != null && activeIndex in points.indices) {
                val pt = points[activeIndex]
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .border(1.dp, Color(0xFFCEE5D0), RoundedCornerShape(14.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF0F4E2D), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.TrendingUp,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        
                        Column {
                            Text(
                                text = pt.tooltipLabel.substringBefore("\n"),
                                fontFamily = WorkSansFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                            Text(
                                text = pt.tooltipLabel.substringAfter("\n"),
                                fontFamily = WorkSansFont,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = Color(0xFF0F4E2D)
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Ketuk titik grafik untuk detail transaksi spesifik",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = WorkSansFont,
                        color = Color.Gray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private fun generateCsvReportString(transactions: List<SalesTransaction>): String {
    val sb = StringBuilder()
    sb.append("ID Transaksi,Tanggal,Product ID\n")
    transactions.forEach { tx ->
        sb.append("${tx.id_transaksi},${tx.tanggal},${tx.product_id}\n")
    }
    return sb.toString()
}

private fun generatePdfReportBytes(transactions: List<SalesTransaction>): ByteArray {
    val pdfDocument = android.graphics.pdf.PdfDocument()
    val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = android.graphics.Paint()
    
    // Header Title
    paint.textSize = 20f
    paint.isFakeBoldText = true
    paint.color = android.graphics.Color.rgb(15, 78, 45) // Chinese imperial green: 0xFF0F4E2D
    canvas.drawText("Laporan Transaksi - Bolo Dewe", 40f, 60f, paint)
    
    // Meta descriptors
    paint.textSize = 10f
    paint.isFakeBoldText = false
    paint.color = android.graphics.Color.GRAY
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    canvas.drawText("Waktu Cetak: ${sdf.format(Date())}", 40f, 85f, paint)
    canvas.drawText("Jumlah Transaksi: ${transactions.size}", 40f, 100f, paint)
    
    // Divider
    paint.strokeWidth = 2f
    paint.color = android.graphics.Color.rgb(15, 78, 45)
    canvas.drawLine(40f, 115f, 555f, 115f, paint)
    
    // Table Header attributes
    var y = 140f
    paint.textSize = 11f
    paint.isFakeBoldText = true
    paint.color = android.graphics.Color.BLACK
    canvas.drawText("ID Transaksi", 40f, y, paint)
    canvas.drawText("Tanggal", 180f, y, paint)
    canvas.drawText("Product ID", 340f, y, paint)
    
    paint.strokeWidth = 1f
    paint.color = android.graphics.Color.LTGRAY
    canvas.drawLine(40f, y + 8f, 555f, y + 8f, paint)
    y += 28f
    
    // Draw cells
    paint.isFakeBoldText = false
    transactions.forEach { tx ->
        if (y < 780f) {
            canvas.drawText(tx.id_transaksi.toString(), 40f, y, paint)
            canvas.drawText(tx.tanggal, 180f, y, paint)
            canvas.drawText(tx.product_id.toString(), 340f, y, paint)
            y += 22f
        }
    }
    
    // Footer notice
    paint.textSize = 9f
    paint.color = android.graphics.Color.GRAY
    canvas.drawLine(40f, 805f, 555f, 805f, paint)
    canvas.drawText("Laporan ini diunduh secara resmi melalui aplikasi POS Bolo Dewe.", 40f, 822f, paint)
    
    pdfDocument.finishPage(page)
    val out = java.io.ByteArrayOutputStream()
    pdfDocument.writeTo(out)
    pdfDocument.close()
    return out.toByteArray()
}
