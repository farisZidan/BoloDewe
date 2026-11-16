package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Image
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Product
import com.example.data.model.resolveProductImageUrl
import com.example.data.model.SalesTransaction
import com.example.ui.theme.WorkSansFont
import com.example.ui.viewmodel.BoloDeweViewModel
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import java.text.SimpleDateFormat
import java.util.*

// ---------------------- TRANSAKSI TERKINI ("LIHAT SEMUA") SCREEN ----------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransaksiTerkiniScreen(
    viewModel: BoloDeweViewModel,
    onNavigateBack: () -> Unit
) {
    val products by viewModel.allProducts.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()
    var searchDateQuery by remember { mutableStateOf("") }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showEditDatePickerDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<SalesTransaction?>(null) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    val editDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    var selectedFilterPill by remember { mutableStateOf(0) } // 0: Hari Ini, 1: Kemarin, 2: Kemarin Lusa

    val entranceAlpha = remember { Animatable(0f) }
    val entranceOffsetY = remember { Animatable(30f) }
    LaunchedEffect(Unit) {
        entranceAlpha.animateTo(1f, animationSpec = tween(450))
    }
    LaunchedEffect(Unit) {
        entranceOffsetY.animateTo(0f, animationSpec = tween(450))
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // App background 10%
        Image(
            painter = painterResource(id = R.drawable.img_cloud_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.10f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    alpha = entranceAlpha.value,
                    translationY = entranceOffsetY.value
                )
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with back navigation matches Page 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_button_trans_terkini")) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF0F4E2D))
                }
                Text(
                    text = "Transaksi Terkini",
                    fontFamily = WorkSansFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF0F4E2D),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                // Balance space
                Spacer(modifier = Modifier.width(48.dp))
            }

            // Date picker fake input row matches Page 2
            OutlinedTextField(
                value = searchDateQuery,
                onValueChange = { searchDateQuery = it },
                readOnly = true,
                placeholder = { Text("Cari berdasarkan tanggal...", color = Color.Gray, fontFamily = WorkSansFont) },
                leadingIcon = {
                    Icon(
                        Icons.Filled.DateRange,
                        contentDescription = "Date",
                        modifier = Modifier.clickable { showDatePickerDialog = true }
                    )
                },
                trailingIcon = {
                    if (searchDateQuery.isNotEmpty()) {
                        IconButton(onClick = { searchDateQuery = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear", tint = Color.Gray)
                        }
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
                    .clickable { showDatePickerDialog = true }
                    .testTag("date_picker_filter_input")
            )

            // List of detailed transaction cards
            val filteredList = remember(transactions, products, searchDateQuery) {
                val baseFiltered = when {
                    searchDateQuery.isNotEmpty() -> transactions.filter { it.tanggal == searchDateQuery }
                    else -> transactions
                }
                
                val currentProductIds = products.map { it.id }.toSet()
                baseFiltered.filter { it.product_id in currentProductIds }.sortedByDescending { it.tanggal }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Tidak ada transaksi untuk tanggal ini.",
                                fontFamily = WorkSansFont,
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    items(filteredList) { item ->
                        val prod = products.find { it.id == item.product_id }
                        DetailedTransactionCard(
                            transaction = item,
                            productName = prod?.nama_produk ?: "ID ${item.product_id}",
                            productImage = prod?.gambar,
                            onEdit = { 
                                editingTransaction = item
                                showEditDatePickerDialog = true
                            },
                            onDelete = { viewModel.deleteTransaction(item.id_transaksi) }
                        )
                    }
                }
            }
        }

        // Beautiful real interactive calendar dialog automatically adapting to system locale and phone date
        if (showDatePickerDialog) {
            DatePickerDialog(
                onDismissRequest = { showDatePickerDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        val selectedTimestamp = datePickerState.selectedDateMillis
                        if (selectedTimestamp != null) {
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                            searchDateQuery = sdf.format(Date(selectedTimestamp))
                        }
                        showDatePickerDialog = false
                    }) {
                        Text("Pilih", color = Color(0xFF0F4E2D), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    Row {
                        TextButton(onClick = {
                            searchDateQuery = ""
                            showDatePickerDialog = false
                        }) {
                            Text("Reset", color = Color.Red)
                        }
                        TextButton(onClick = {
                            showDatePickerDialog = false
                        }) {
                            Text("Batal", color = Color.Gray)
                        }
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

        if (showEditDatePickerDialog) {
            DatePickerDialog(
                onDismissRequest = { 
                    showEditDatePickerDialog = false 
                    editingTransaction = null
                },
                confirmButton = {
                    TextButton(onClick = {
                        val selectedTimestamp = editDatePickerState.selectedDateMillis
                        if (selectedTimestamp != null && editingTransaction != null) {
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                            val newDate = sdf.format(Date(selectedTimestamp))
                            viewModel.updateSaleTransaction(
                                transactionId = editingTransaction!!.id_transaksi,
                                productId = editingTransaction!!.product_id,
                                dateString = newDate
                            )
                        }
                        showEditDatePickerDialog = false
                        editingTransaction = null
                    }) {
                        Text("Simpan", color = Color(0xFF0F4E2D), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showEditDatePickerDialog = false
                        editingTransaction = null
                    }) {
                        Text("Batal", color = Color.Gray)
                    }
                }
            ) {
                DatePicker(
                    state = editDatePickerState,
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

        // Global Error Alert pop-up system
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
        ) {
            com.example.ui.components.GlobalErrorPopUpNotifier(viewModel)
        }
    }
}

@Composable
fun DetailedTransactionCard(transaction: SalesTransaction, productName: String, productImage: String? = null, onEdit: (() -> Unit)? = null, onDelete: (() -> Unit)? = null) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteTransactionDialog by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        label = "ArrowRotation"
    )

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
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Product photo or placeholder thumbnail box
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(12.dp))
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
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = productName,
                            fontFamily = WorkSansFont,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        
                    }

                    // Quantity sold text green
                    Text(
                        text = "1 pcs",
                        fontFamily = WorkSansFont,
                        color = Color(0xFF0F4E2D),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Chevron icon rotation dynamically animated
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = Color(0xFF0F4E2D),
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer(rotationZ = rotationState)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .padding(bottom = 14.dp)
                ) {
                    Divider(color = Color(0xFFECECEC), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tanggal:", fontFamily = WorkSansFont, color = Color.Gray, fontSize = 12.sp)
                        Text(transaction.tanggal, fontFamily = WorkSansFont, color = Color(0xFF333333), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                    
                    if (onEdit != null || onDelete != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (onEdit != null) {
                                Button(
                                    onClick = { onEdit.invoke() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F4E8)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    elevation = ButtonDefaults.buttonElevation(0.dp)
                                ) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = Color(0xFF0F4E2D), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Edit Tanggal", color = Color(0xFF0F4E2D), fontFamily = WorkSansFont, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                            if (onDelete != null) {
                                Button(
                                    onClick = { showDeleteTransactionDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF0F0)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    elevation = ButtonDefaults.buttonElevation(0.dp)
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Hapus", tint = Color.Red, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Hapus", color = Color.Red, fontFamily = WorkSansFont, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ---------------------- PRODUCT DETAILS / EDIT SCREEN ----------------------
@Composable
fun ProductDetailScreen(
    productId: Int?, // Pass null for creation mode!
    viewModel: BoloDeweViewModel,
    onNavigateBack: () -> Unit
) {
    val products by viewModel.allProducts.collectAsState()
    val isEditMode = productId != null
    val targetProduct = remember(productId, products) {
        if (isEditMode) products.find { it.id == productId } else null
    }

    var name by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }

    // System image picker launcher and local state
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var isImageCleared by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    // Dropdown selectors states matches Page 5 detail design with dynamic additions
    var cuttingsList by remember { mutableStateOf(listOf("Slim Fit", "Regular Fit", "Loose Fit")) }
    var selectedCutting by remember { mutableStateOf("Slim Fit") }

    var selectedSizingType by remember { mutableStateOf("Letter Sizing") }
    var selectedSpecificSize by remember { mutableStateOf("M") }
    var sizingTypesMap by remember { mutableStateOf(mapOf(
        "Numerical Sizing" to listOf("28", "29", "30", "31", "32", "33", "34", "35", "36"),
        "Waist & Inseam" to listOf("28x30", "29x30", "30x30", "31x30", "32x30", "33x30", "34x30"),
        "Letter Sizing" to listOf("XS", "S", "M", "L", "XL", "XXL", "XXXL")
    )) }

    var brandsList by remember { mutableStateOf(listOf("Levis", "Cardinal", "Wrangler")) }
    var selectedBrand by remember { mutableStateOf("Levis") }
    var selectedGender by remember { mutableStateOf("Pria") }

    // Dialog state controllers
    var showAddCuttingDialog by remember { mutableStateOf(false) }
    var newCuttingName by remember { mutableStateOf("") }

    var showAddBrandDialog by remember { mutableStateOf(false) }
    var newBrandName by remember { mutableStateOf("") }

    var showAddSizingTypeDialog by remember { mutableStateOf(false) }
    var newSizingTypeName by remember { mutableStateOf("") }
    var newSizingTypeAvailableSizes by remember { mutableStateOf("") }

    var showEditSizingTypeDialog by remember { mutableStateOf<String?>(null) }
    var newUkuranToAppend by remember { mutableStateOf("") }

    var showSizeChoiceDialog by remember { mutableStateOf<String?>(null) }

    // Init values
    LaunchedEffect(targetProduct) {
        isImageCleared = false
        if (targetProduct != null) {
            name = targetProduct.nama_produk
            sku = targetProduct.kode_sku
            
            val cut = if (targetProduct.cutting_id == 1) "Slim Fit" else "Regular Fit"
            if (cut !in cuttingsList) {
                cuttingsList = cuttingsList + cut
            }
            selectedCutting = cut

            val brand = when (targetProduct.brand_id) {
                1 -> "Levis"
                2 -> "Cardinal"
                else -> "Wrangler"
            }
            if (brand !in brandsList) {
                brandsList = brandsList + brand
            }
            selectedBrand = brand

            selectedGender = when (targetProduct.gender_id) {
                1 -> "Pria"
                2 -> "Wanita"
                else -> "Unisex"
            }
        } else if (!isEditMode) {
            // Default random SKU mapping for quick testing creation
            sku = "SKU-" + (1000..9999).random().toString()
        }
    }

    val entranceAlpha = remember { Animatable(0f) }
    val entranceOffsetY = remember { Animatable(35f) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        entranceAlpha.animateTo(1f, animationSpec = tween(450))
    }
    LaunchedEffect(Unit) {
        entranceOffsetY.animateTo(0f, animationSpec = tween(450))
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Image(
            painter = painterResource(id = R.drawable.img_cloud_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.10f
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    alpha = entranceAlpha.value,
                    translationY = entranceOffsetY.value
                )
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Back item layout
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("detail_back_btn")) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF0F4E2D))
                    }
                    Text(
                        text = if (isEditMode) "Product Details" else "Add New Product",
                        fontFamily = WorkSansFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF0F4E2D),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    
                    if (isEditMode) {
                        // Deletion icon button inside secondary views
                        IconButton(onClick = {
                            showDeleteDialog = true
                        }, modifier = Modifier.testTag("delete_product_btn")) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                }
            }

            // Products photos row grid with "Upload" button matches Page 5
            item {
                Text(
                    "Product Photos",
                    fontFamily = WorkSansFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.Black
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Upload pill button matches Page 5 visual
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F1F1)),
                        modifier = Modifier
                            .size(76.dp)
                            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                            .clickable { launcher.launch("image/*") }
                            .testTag("upload_image_card")
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Filled.AddAPhoto, contentDescription = "Upload", tint = Color.Gray, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Upload", fontFamily = WorkSansFont, fontSize = 10.sp, color = Color.Gray)
                        }
                    }

                    // Display product image preview using AsyncImage!
                    if (selectedImageUri != null) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFFCEE5D0), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Selected Image Preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Cancel image select overlay
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .clickable {
                                        selectedImageUri = null
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Cancel Image",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    } else if (targetProduct?.gambar != null && !isImageCleared) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = resolveProductImageUrl(targetProduct.gambar),
                                contentDescription = "Product Image Preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Remove existing product image overlay
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .clickable {
                                        isImageCleared = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Remove Image",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    } else {
                        // Placeholder grid boxes representing photos matching design
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF7F7F7))
                                    .border(1.dp, Color(0xFFE4E4E4), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Image, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(22.dp))
                            }
                        }
                    }
                }
            }

            // Editable attributes list
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Product Name", fontFamily = WorkSansFont, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("e.g. Slim Fit Denim Chinos", color = Color.LightGray, fontFamily = WorkSansFont) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF9F9F9),
                            unfocusedContainerColor = Color(0xFFF9F9F9),
                            focusedBorderColor = Color(0xFFCEE5D0),
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        ),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp).testTag("product_name_input")
                    )
                }
            }

            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Product SKU (Kode SKU)", fontFamily = WorkSansFont, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        placeholder = { Text("e.g. TSHIRT-BLK-L", color = Color.LightGray, fontFamily = WorkSansFont) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF9F9F9),
                            unfocusedContainerColor = Color(0xFFF9F9F9),
                            focusedBorderColor = Color(0xFFCEE5D0),
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        ),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp).testTag("product_sku_input")
                    )
                }
            }



            // Model Cutting dropdown container with Dynamic Additions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Model Cutting", fontFamily = WorkSansFont, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("+ Add New", fontFamily = WorkSansFont, fontWeight = FontWeight.Bold, color = Color(0xFF5DAF77), fontSize = 12.sp, modifier = Modifier.clickable {
                        showAddCuttingDialog = true
                    })
                }
                
                var expandedCutting by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCutting,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expandedCutting = true }) {
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF9F9F9),
                            unfocusedContainerColor = Color(0xFFF9F9F9),
                            focusedBorderColor = Color(0xFFCEE5D0),
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedCutting = true }
                            .padding(top = 4.dp)
                    )

                    DropdownMenu(
                        expanded = expandedCutting,
                        onDismissRequest = { expandedCutting = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        cuttingsList.forEach { cut ->
                            DropdownMenuItem(
                                text = { Text(cut, fontFamily = WorkSansFont) },
                                onClick = {
                                    selectedCutting = cut
                                    expandedCutting = false
                                }
                            )
                        }
                    }
                }
            }

            // Size Dropdown selector with choice pop-ups and custom categories
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Size", fontFamily = WorkSansFont, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("+ Add New", fontFamily = WorkSansFont, fontWeight = FontWeight.Bold, color = Color(0xFF5DAF77), fontSize = 12.sp, modifier = Modifier.clickable {
                        showAddSizingTypeDialog = true
                    })
                }
                
                var expandedSize by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = "$selectedSizingType ($selectedSpecificSize)",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { 
                                    showSizeChoiceDialog = selectedSizingType
                                }) {
                                    Icon(Icons.Filled.Check, contentDescription = "Pilih Ukuran", tint = Color(0xFF0F4E2D))
                                }
                                IconButton(onClick = { expandedSize = true }) {
                                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF9F9F9),
                            unfocusedContainerColor = Color(0xFFF9F9F9),
                            focusedBorderColor = Color(0xFFCEE5D0),
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedSize = true }
                            .padding(top = 4.dp)
                    )

                    DropdownMenu(
                        expanded = expandedSize,
                        onDismissRequest = { expandedSize = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        sizingTypesMap.keys.forEach { sizingTypeName ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(sizingTypeName, fontFamily = WorkSansFont)
                                        IconButton(
                                            onClick = {
                                                showEditSizingTypeDialog = sizingTypeName
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.Edit,
                                                contentDescription = "Edit Sizes List",
                                                modifier = Modifier.size(16.dp),
                                                tint = Color(0xFF0F4E2D)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    selectedSizingType = sizingTypeName
                                    expandedSize = false
                                    showSizeChoiceDialog = sizingTypeName
                                }
                            )
                        }
                    }
                }
            }

            // Brand selectors with dynamic Merks additions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Merk", fontFamily = WorkSansFont, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("+ Add New", fontFamily = WorkSansFont, fontWeight = FontWeight.Bold, color = Color(0xFF5DAF77), fontSize = 12.sp, modifier = Modifier.clickable {
                        showAddBrandDialog = true
                    })
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    brandsList.forEach { brand ->
                        val isSel = selectedBrand == brand
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) Color(0xFFE8F5E9) else Color.White)
                                .border(
                                    1.dp, 
                                    if (isSel) Color(0xFF0F4E2D) else Color.LightGray, 
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedBrand = brand }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = brand,
                                fontFamily = WorkSansFont,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) Color(0xFF0F4E2D) else Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Gender grid filters: "Pria", "Wanita", "Unisex" Matches Page 5 layout
            item {
                Text(
                    "Gender",
                    fontFamily = WorkSansFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.Black
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("Pria", "Wanita", "Unisex").forEach { sex ->
                        val isSexSel = selectedGender == sex
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSexSel) Color(0xFFE8F5E9) else Color.White)
                                .border(
                                    1.dp, 
                                    if (isSexSel) Color(0xFF0F4E2D) else Color.LightGray, 
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedGender = sex }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = sex,
                                fontFamily = WorkSansFont,
                                fontWeight = if (isSexSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSexSel) Color(0xFF0F4E2D) else Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Save Product green pill button matches Page 5
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        val brandId = when (selectedBrand) {
                            "Levis" -> 1
                            "Cardinal" -> 2
                            else -> 3
                        }
                        val genderId = when (selectedGender) {
                            "Pria" -> 1
                            "Wanita" -> 2
                            else -> 3
                        }

                        if (isEditMode) {
                            val finalGambar = if (isImageCleared && selectedImageUri == null) null else targetProduct?.gambar
                            viewModel.updateProduct(
                                product = targetProduct!!.copy(
                                    nama_produk = name,
                                    kode_sku = sku,
                                    gambar = finalGambar,
                                    brand_id = brandId,
                                    gender_id = genderId,
                                    cutting_id = if (selectedCutting == "Slim Fit") 1 else 2
                                ),
                                newGambarUri = selectedImageUri?.toString()
                            ) {
                                onNavigateBack()
                            }
                        } else {
                            viewModel.addProduct(
                                product = com.example.data.model.Product(
                                    kode_sku = sku,
                                    nama_produk = name,
                                    gambar = null
                                ),
                                gambarUri = selectedImageUri?.toString()
                            ) {
                                onNavigateBack()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F4E2D)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_product_btn"),
                    enabled = name.trim().isNotEmpty() && sku.trim().isNotEmpty()
                ) {
                    Text(
                        "Save Product",
                        fontFamily = WorkSansFont,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                }
            }
        }

        // 1. Add Cutting Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Hapus Produk?", fontFamily = WorkSansFont, fontWeight = FontWeight.Bold, color = Color.Red) },
                text = { Text("Apakah Anda yakin ingin menghapus produk ini secara permanen dari server?", fontFamily = WorkSansFont) },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        viewModel.deleteProduct(productId!!, targetProduct?.kode_sku ?: sku)
                        onNavigateBack()
                    }) {
                        Text("Hapus", color = Color.Red, fontFamily = WorkSansFont, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Batal", color = Color.Gray, fontFamily = WorkSansFont)
                    }
                }
            )
        }

        if (showAddCuttingDialog) {
            AlertDialog(
                onDismissRequest = { showAddCuttingDialog = false },
                title = { Text("Tambah Model Cutting", fontFamily = WorkSansFont, fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = newCuttingName,
                        onValueChange = { newCuttingName = it },
                        placeholder = { Text("e.g. Regular Crop / Oversized Fit", fontFamily = WorkSansFont) },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val nameTrimmed = newCuttingName.trim()
                            if (nameTrimmed.isNotEmpty()) {
                                cuttingsList = cuttingsList + nameTrimmed
                                selectedCutting = nameTrimmed
                            }
                            newCuttingName = ""
                            showAddCuttingDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F4E2D))
                    ) {
                        Text("Tambah", fontFamily = WorkSansFont, color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddCuttingDialog = false }) {
                        Text("Batal", fontFamily = WorkSansFont)
                    }
                }
            )
        }

        // 2. Add Brand Dialog
        if (showAddBrandDialog) {
            AlertDialog(
                onDismissRequest = { showAddBrandDialog = false },
                title = { Text("Tambah Merk Baru", fontFamily = WorkSansFont, fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = newBrandName,
                        onValueChange = { newBrandName = it },
                        placeholder = { Text("e.g. Erigo / Uniqlo", fontFamily = WorkSansFont) },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val nameTrimmed = newBrandName.trim()
                            if (nameTrimmed.isNotEmpty()) {
                                brandsList = brandsList + nameTrimmed
                                selectedBrand = nameTrimmed
                            }
                            newBrandName = ""
                            showAddBrandDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F4E2D))
                    ) {
                        Text("Tambah", fontFamily = WorkSansFont, color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddBrandDialog = false }) {
                        Text("Batal", fontFamily = WorkSansFont)
                    }
                }
            )
        }

        // 3. Add Sizing Type Dialog
        if (showAddSizingTypeDialog) {
            AlertDialog(
                onDismissRequest = { showAddSizingTypeDialog = false },
                title = { Text("Tambah Jenis Ukuran Baru", fontFamily = WorkSansFont, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Nama Jenis Ukuran:", fontFamily = WorkSansFont, fontSize = 12.sp, color = Color.Gray)
                        OutlinedTextField(
                            value = newSizingTypeName,
                            onValueChange = { newSizingTypeName = it },
                            placeholder = { Text("e.g. European Sizing", fontFamily = WorkSansFont) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Ukuran yang Tersedia (pisahkan dengan koma):", fontFamily = WorkSansFont, fontSize = 12.sp, color = Color.Gray)
                        OutlinedTextField(
                            value = newSizingTypeAvailableSizes,
                            onValueChange = { newSizingTypeAvailableSizes = it },
                            placeholder = { Text("e.g. 39, 40, 41, 42", fontFamily = WorkSansFont) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val typeName = newSizingTypeName.trim()
                            val sizesStr = newSizingTypeAvailableSizes.trim()
                            if (typeName.isNotEmpty()) {
                                val listSizes = sizesStr.split(",")
                                    .map { it.trim() }
                                    .filter { it.isNotEmpty() }
                                if (listSizes.isNotEmpty()) {
                                    sizingTypesMap = sizingTypesMap + (typeName to listSizes)
                                    selectedSizingType = typeName
                                    selectedSpecificSize = listSizes.first()
                                    showSizeChoiceDialog = typeName
                                }
                            }
                            newSizingTypeName = ""
                            newSizingTypeAvailableSizes = ""
                            showAddSizingTypeDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F4E2D))
                    ) {
                        Text("Tambah", fontFamily = WorkSansFont, color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddSizingTypeDialog = false }) {
                        Text("Batal", fontFamily = WorkSansFont)
                    }
                }
            )
        }

        // 4. Edit Sizing Type Dialog (to add new size values to an existing type)
        showEditSizingTypeDialog?.let { typeName ->
            val sizes = sizingTypesMap[typeName] ?: emptyList()
            AlertDialog(
                onDismissRequest = { showEditSizingTypeDialog = null },
                title = { Text("Edit Ukuran: $typeName", fontFamily = WorkSansFont, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Ukuran saat ini:", fontFamily = WorkSansFont, fontSize = 12.sp, color = Color.Gray)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            sizes.forEach { sz ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFE8F5E9))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(sz, fontFamily = WorkSansFont, fontSize = 11.sp, color = Color(0xFF0F4E2D))
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text("Tambah Ukuran Baru:", fontFamily = WorkSansFont, fontSize = 12.sp, color = Color.Gray)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newUkuranToAppend,
                                onValueChange = { newUkuranToAppend = it },
                                placeholder = { Text("e.g. XLG", fontFamily = WorkSansFont) },
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    val newSz = newUkuranToAppend.trim()
                                    if (newSz.isNotEmpty()) {
                                        val newList = sizes + newSz
                                        sizingTypesMap = sizingTypesMap + (typeName to newList)
                                        newUkuranToAppend = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F4E2D)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(52.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Add Size", tint = Color.White)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showEditSizingTypeDialog = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F4E2D))
                    ) {
                        Text("Selesai", fontFamily = WorkSansFont, color = Color.White)
                    }
                }
            )
        }

        // 5. Pop UP Select Size values Dialog (Specific size selector list grid)
        showSizeChoiceDialog?.let { typeName ->
            val sizeOptions = sizingTypesMap[typeName] ?: emptyList()
            AlertDialog(
                onDismissRequest = { showSizeChoiceDialog = null },
                title = { Text("Pilih Ukuran ($typeName)", fontFamily = WorkSansFont, fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Silahkan pilih salah satu ukuran yang tersedia:", fontFamily = WorkSansFont, fontSize = 12.sp, color = Color.Gray)
                        
                        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                            columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.heightIn(max = 220.dp)
                        ) {
                            items(sizeOptions.size) { index ->
                                val sizeChoice = sizeOptions[index]
                                val isSelected = selectedSizingType == typeName && selectedSpecificSize == sizeChoice
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFFCEE5D0) else Color(0xFFF1F1F1))
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) Color(0xFF0F4E2D) else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            selectedSizingType = typeName
                                            selectedSpecificSize = sizeChoice
                                            showSizeChoiceDialog = null
                                        }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = sizeChoice,
                                        fontFamily = WorkSansFont,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color(0xFF0F4E2D) else Color.DarkGray,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSizeChoiceDialog = null }) {
                        Text("Tutup", fontFamily = WorkSansFont)
                    }
                }
            )
        }

        // Global Pop-Up Database/Network Error notifier overlay
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
        ) {
            com.example.ui.components.GlobalErrorPopUpNotifier(viewModel)
        }
    }
}
