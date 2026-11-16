package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.AttributeOption
import com.example.data.model.Product
import com.example.data.model.resolveProductImageUrl
import com.example.ui.theme.WorkSansFont
import com.example.ui.viewmodel.BoloDeweViewModel

@Composable
fun <T> AttributeDropdown(
    label: String,
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T) -> Unit,
    nameSelector: (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontFamily = WorkSansFont, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedOption?.let { nameSelector(it) } ?: "Select $label",
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { expanded = true }) {
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
                    .clickable { expanded = true }
                    .padding(top = 4.dp)
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(nameSelector(option), fontFamily = WorkSansFont) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProductDetailScreenNew(
    productId: Int?,
    viewModel: BoloDeweViewModel,
    onNavigateBack: () -> Unit
) {
    val activeProducts by viewModel.allProducts.collectAsState()
    val attributesResp by viewModel.attributes.collectAsState()
    val isEditMode = productId != null
    val targetProduct = remember(productId, activeProducts) {
        if (isEditMode) {
            activeProducts.find { it.id == productId }
        } else null
    }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isImageCleared by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { selectedImageUri = it } }

    var selCategory by remember { mutableStateOf<AttributeOption?>(null) }
    var selBrand by remember { mutableStateOf<AttributeOption?>(null) }
    var selGender by remember { mutableStateOf<AttributeOption?>(null) }
    var selAgeGroup by remember { mutableStateOf<AttributeOption?>(null) }
    var selMaterial by remember { mutableStateOf<AttributeOption?>(null) }
    var selCutting by remember { mutableStateOf<AttributeOption?>(null) }
    var selFit by remember { mutableStateOf<AttributeOption?>(null) }
    var selColor by remember { mutableStateOf<AttributeOption?>(null) }
    var selSizeGroup by remember { mutableStateOf<AttributeOption?>(null) }

    LaunchedEffect(targetProduct, attributesResp) {
        if (targetProduct != null && attributesResp != null) {
            selCategory = attributesResp?.categories?.find { it.id == targetProduct.category_id }
            selBrand = attributesResp?.brands?.find { it.id == targetProduct.brand_id }
            selGender = attributesResp?.genders?.find { it.id == targetProduct.gender_id }
            selAgeGroup = attributesResp?.age_groups?.find { it.id == targetProduct.age_group_id }
            selMaterial = attributesResp?.materials?.find { it.id == targetProduct.material_id }
            selCutting = attributesResp?.cuttings?.find { it.id == targetProduct.cutting_id }
            selFit = attributesResp?.fits?.find { it.id == targetProduct.fit_id }
            selColor = attributesResp?.colors?.find { it.id == targetProduct.color_id }
            selSizeGroup = attributesResp?.size_groups?.find { it.id == targetProduct.size_group_id }
        }
    }

    val generatedName = remember(selBrand, selCutting, selSizeGroup, selGender) {
        listOfNotNull(selBrand?.name, selCutting?.name, selSizeGroup?.name, selGender?.name)
            .joinToString(" ")
            .ifEmpty { "Pilih atribut di bawah untuk membuat nama" }
    }

    val generatedSku = remember(selCutting, selBrand) {
        if (isEditMode) {
            targetProduct?.kode_sku ?: ""
        } else {
            val base = listOfNotNull(selCutting?.name, selBrand?.name)
                .joinToString("-")
                .uppercase()
                .replace(" ", "-")
            if (base.isNotEmpty()) "$base-${(100..999).random()}" else "Pilih atribut di bawah"
        }
    }

    val entranceAlpha = remember { Animatable(0f) }
    val entranceOffsetY = remember { Animatable(35f) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        entranceAlpha.animateTo(1f, animationSpec = tween(450))
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
                .graphicsLayer(alpha = entranceAlpha.value, translationY = entranceOffsetY.value)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF0F4E2D))
                    }
                    Text(
                        text = if (isEditMode) "Product Details" else "Add New Product",
                        fontFamily = WorkSansFont, fontWeight = FontWeight.Bold, fontSize = 18.sp,
                        color = Color(0xFF0F4E2D), modifier = Modifier.weight(1f), textAlign = TextAlign.Center
                    )
                    if (isEditMode) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(22.dp))
                        }
                    } else {
                        Spacer(modifier = Modifier.size(48.dp))
                    }
                }
            }

            item {
                Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFFF1F1F1)).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(140.dp).clip(RoundedCornerShape(12.dp)).background(Color.LightGray).clickable { launcher.launch("image/*") }, contentAlignment = Alignment.Center) {
                        if (selectedImageUri != null) {
                            AsyncImage(model = selectedImageUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        } else if (!isImageCleared && targetProduct?.gambar != null) {
                            AsyncImage(model = resolveProductImageUrl(targetProduct.gambar), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        } else {
                            Text("Upload\nImage", fontFamily = WorkSansFont, color = Color.Gray, textAlign = TextAlign.Center, fontSize = 12.sp)
                        }
                    }
                    if (selectedImageUri != null || (!isImageCleared && targetProduct?.gambar != null)) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Clear Image", color = Color.Red, fontFamily = WorkSansFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.clickable { selectedImageUri = null; isImageCleared = true })
                    }
                }
            }

            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Product Name", fontFamily = WorkSansFont, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    OutlinedTextField(
                        value = generatedName,
                        onValueChange = {},
                        readOnly = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF9F9F9), unfocusedContainerColor = Color(0xFFF9F9F9)
                        )
                    )
                }
            }

            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Product SKU (Kode SKU)", fontFamily = WorkSansFont, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    OutlinedTextField(
                        value = generatedSku,
                        onValueChange = {},
                        readOnly = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF9F9F9), unfocusedContainerColor = Color(0xFFF9F9F9)
                        )
                    )
                }
            }

            val opts = attributesResp
            if (opts != null) {
                item { AttributeDropdown("Kategori (Category)", opts.categories, selCategory, { selCategory = it }, { it.name }) }
                item { AttributeDropdown("Merk (Brand)", opts.brands, selBrand, { selBrand = it }, { it.name }) }
                item { AttributeDropdown("Gender", opts.genders, selGender, { selGender = it }, { it.name }) }
                item { AttributeDropdown("Umur (Age Group)", opts.age_groups, selAgeGroup, { selAgeGroup = it }, { it.name }) }
                item { AttributeDropdown("Bahan (Material)", opts.materials, selMaterial, { selMaterial = it }, { it.name }) }
                item { AttributeDropdown("Model / Cutting", opts.cuttings, selCutting, { selCutting = it }, { it.name }) }
                item { AttributeDropdown("Fit", opts.fits, selFit, { selFit = it }, { it.name }) }
                item { AttributeDropdown("Warna (Color)", opts.colors, selColor, { selColor = it }, { it.name }) }
                item { AttributeDropdown("Ukuran (Size Group)", opts.size_groups, selSizeGroup, { selSizeGroup = it }, { it.name }) }
            } else {
                item { Text("Loading attributes...", color = Color.Gray, fontFamily = WorkSansFont) }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
            
            item {
                Button(
                    onClick = {
                        val newProduct = Product(
                            id = targetProduct?.id ?: 0,
                            kode_sku = generatedSku,
                            nama_produk = generatedName,
                            gambar = if (isImageCleared && selectedImageUri == null) null else targetProduct?.gambar,
                            category_id = selCategory?.id ?: 1,
                            brand_id = selBrand?.id ?: 1,
                            gender_id = selGender?.id ?: 1,
                            age_group_id = selAgeGroup?.id ?: 1,
                            material_id = selMaterial?.id ?: 1,
                            cutting_id = selCutting?.id ?: 1,
                            fit_id = selFit?.id ?: 1,
                            color_id = selColor?.id ?: 1,
                            size_group_id = selSizeGroup?.id ?: 1
                        )
                        
                        if (isEditMode) {
                            viewModel.updateProduct(newProduct, newGambarUri = selectedImageUri?.toString()) {
                                onNavigateBack()
                            }
                        } else {
                            viewModel.addProduct(newProduct, gambarUri = selectedImageUri?.toString()) {
                                onNavigateBack()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F4E2D)),
                    shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = generatedName.isNotEmpty() && !generatedName.startsWith("Pilih") &&
                              generatedSku.isNotEmpty() && !generatedSku.startsWith("Pilih")
                ) {
                    Text("Save Product", fontFamily = WorkSansFont, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Hapus Produk?", fontFamily = WorkSansFont, fontWeight = FontWeight.Bold, color = Color.Red) },
                text = { Text("Apakah Anda yakin ingin menghapus produk ini secara permanen dari server?", fontFamily = WorkSansFont) },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        viewModel.deleteProduct(productId!!, generatedSku)
                        onNavigateBack()
                    }) { Text("Hapus", color = Color.Red, fontFamily = WorkSansFont, fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("Batal", color = Color.Gray, fontFamily = WorkSansFont) }
                }
            )
        }

        // Popup layer
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
        ) {
            com.example.ui.components.GlobalErrorPopUpNotifier(viewModel)
        }
    }
}
