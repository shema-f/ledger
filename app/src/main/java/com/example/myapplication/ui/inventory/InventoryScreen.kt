package com.example.myapplication.ui.inventory

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PointOfSale
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.model.Product
import com.example.myapplication.util.LocalStrings
import com.example.myapplication.util.localizedString
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showLowStockOnly by viewModel.showLowStockOnly.collectAsState()
    val products by viewModel.products.collectAsState()
    val accounts by viewModel.accounts.collectAsState()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }

    var showPosDialog by remember { mutableStateOf(false) }
    var posProduct by remember { mutableStateOf<Product?>(null) }

    var showBarcodeScanner by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingProduct = null
                    showAddEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add Product")
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Header Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = localizedString("Inventory & POS"),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = localizedString("Product Catalog"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar & Camera Barcode Scanner Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(localizedString("search_products")) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Rounded.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(imageVector = Icons.Rounded.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { showBarcodeScanner = true },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.QrCodeScanner,
                        contentDescription = "Scan Barcode",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Low Stock Alerts Filter Chip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                FilterChip(
                    selected = showLowStockOnly,
                    onClick = { viewModel.toggleLowStockFilter(!showLowStockOnly) },
                    label = { Text(localizedString("Low Stock Alert")) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = "Low Stock Alert",
                            tint = if (showLowStockOnly) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Product List
            if (products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (showLowStockOnly) localizedString("Low Stock Alert") else localizedString("No products found."),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            onPosClick = {
                                posProduct = product
                                showPosDialog = true
                            },
                            onEditClick = {
                                editingProduct = product
                                showAddEditDialog = true
                            },
                            onDeleteClick = {
                                viewModel.deleteProduct(product.id)
                                Toast.makeText(context, "Deleted ${product.name}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // Barcode Scanner Modal Dialog
    if (showBarcodeScanner) {
        BarcodeScannerModal(
            onDismiss = { showBarcodeScanner = false },
            onBarcodeScanned = { scannedBarcode ->
                showBarcodeScanner = false
                coroutineScope.launch {
                    val foundProduct = viewModel.findProductByBarcode(scannedBarcode)
                    if (foundProduct != null) {
                        posProduct = foundProduct
                        showPosDialog = true
                    } else {
                        editingProduct = Product(
                            id = 0,
                            name = "",
                            barcode = scannedBarcode,
                            buyingPrice = 0.0,
                            sellingPrice = 0.0,
                            currentStock = 0,
                            minAlertStock = 5
                        )
                        showAddEditDialog = true
                    }
                }
            }
        )
    }

    // Add / Edit Product Modal Dialog
    if (showAddEditDialog) {
        AddEditProductDialog(
            product = editingProduct,
            onDismiss = { showAddEditDialog = false },
            onSave = { product ->
                if (editingProduct == null || editingProduct?.id == 0L) {
                    viewModel.addProduct(product)
                    Toast.makeText(context, "Added product ${product.name}", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.updateProduct(product)
                    Toast.makeText(context, "Updated product ${product.name}", Toast.LENGTH_SHORT).show()
                }
                showAddEditDialog = false
            }
        )
    }

    // Quick POS Checkout Modal Dialog
    if (showPosDialog && posProduct != null) {
        PosCheckoutDialog(
            product = posProduct!!,
            accounts = accounts,
            onDismiss = { showPosDialog = false },
            onConfirm = { quantity, accountId ->
                viewModel.conductPosSale(
                    product = posProduct!!,
                    quantity = quantity,
                    accountId = accountId,
                    onSuccess = {
                        Toast.makeText(context, "POS Checkout Successful!", Toast.LENGTH_SHORT).show()
                        showPosDialog = false
                    },
                    onError = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }
}

@Composable
fun ProductCard(
    product: Product,
    onPosClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLowStock = product.currentStock <= product.minAlertStock
    val margin = product.sellingPrice - product.buyingPrice
    val currencyFormat = NumberFormat.getNumberInstance(Locale.US)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    product.barcode?.let {
                        if (it.isNotBlank()) {
                            Text(
                                text = "Code: $it",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Stock Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isLowStock) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (isLowStock) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isLowStock) {
                            Icon(
                                imageVector = Icons.Rounded.Warning,
                                contentDescription = "Low Stock",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = "${product.currentStock} units",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pricing Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = localizedString("Buying Price"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${currencyFormat.format(product.buyingPrice)} ${localizedString("RWF")}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                Column {
                    Text(
                        text = localizedString("Selling Price"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${currencyFormat.format(product.sellingPrice)} ${localizedString("RWF")}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column {
                    Text(
                        text = localizedString("Profit Margin"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "+${currencyFormat.format(margin)} ${localizedString("RWF")}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF2E7D32)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Card Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit Product",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Delete Product",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Button(
                    onClick = onPosClick,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PointOfSale,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(localizedString("POS Checkout"))
                }
            }
        }
    }
}

@Composable
fun AddEditProductDialog(
    product: Product?,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var barcode by remember { mutableStateOf(product?.barcode ?: "") }
    var buyingPriceText by remember { mutableStateOf(product?.buyingPrice?.let { if (it > 0) it.toInt().toString() else "" } ?: "") }
    var sellingPriceText by remember { mutableStateOf(product?.sellingPrice?.let { if (it > 0) it.toInt().toString() else "" } ?: "") }
    var stockText by remember { mutableStateOf(product?.currentStock?.toString() ?: "") }
    var minAlertStockText by remember { mutableStateOf(product?.minAlertStock?.toString() ?: "5") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = if (product == null || product.id == 0L) "Add New Product" else "Edit Product")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = { Text("Barcode (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = buyingPriceText,
                        onValueChange = { buyingPriceText = it },
                        label = { Text("Buying Price *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = sellingPriceText,
                        onValueChange = { sellingPriceText = it },
                        label = { Text("Selling Price *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stockText,
                        onValueChange = { stockText = it },
                        label = { Text("Current Stock *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minAlertStockText,
                        onValueChange = { minAlertStockText = it },
                        label = { Text("Alert Threshold") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val buyPrice = buyingPriceText.toDoubleOrNull()
                    val sellPrice = sellingPriceText.toDoubleOrNull()
                    val stock = stockText.toIntOrNull()
                    val minAlert = minAlertStockText.toIntOrNull() ?: 5

                    if (name.isBlank()) {
                        errorMessage = "Product name is required"
                        return@Button
                    }
                    if (buyPrice == null || buyPrice < 0) {
                        errorMessage = "Valid buying price is required"
                        return@Button
                    }
                    if (sellPrice == null || sellPrice < 0) {
                        errorMessage = "Valid selling price is required"
                        return@Button
                    }
                    if (stock == null || stock < 0) {
                        errorMessage = "Valid stock quantity is required"
                        return@Button
                    }

                    val updatedProduct = Product(
                        id = product?.id ?: 0,
                        name = name.trim(),
                        barcode = barcode.trim().ifBlank { null },
                        buyingPrice = buyPrice,
                        sellingPrice = sellPrice,
                        currentStock = stock,
                        minAlertStock = minAlert
                    )
                    onSave(updatedProduct)
                }
            ) {
                Text(localizedString("Save"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localizedString("Cancel"))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosCheckoutDialog(
    product: Product,
    accounts: List<Account>,
    onDismiss: () -> Unit,
    onConfirm: (quantity: Int, accountId: Long) -> Unit
) {
    var quantity by remember { mutableIntStateOf(1) }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull { it.isDefault }?.id ?: accounts.firstOrNull()?.id ?: 0L) }
    var expandedAccountDropdown by remember { mutableStateOf(false) }

    val totalAmount = product.sellingPrice * quantity
    val unitProfit = product.sellingPrice - product.buyingPrice
    val totalProfit = unitProfit * quantity
    val currencyFormat = NumberFormat.getNumberInstance(Locale.US)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.PointOfSale,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(localizedString("POS Checkout"))
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Text(
                    text = "${localizedString("Stock Quantity")}: ${product.currentStock}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (product.currentStock <= product.minAlertStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Quantity Picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Quantity:", style = MaterialTheme.typography.bodyMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = { if (quantity > 1) quantity-- },
                            enabled = quantity > 1,
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "$quantity",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        OutlinedButton(
                            onClick = { if (quantity < product.currentStock) quantity++ },
                            enabled = quantity < product.currentStock,
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Account Selector Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedAccountDropdown,
                    onExpandedChange = { expandedAccountDropdown = !expandedAccountDropdown }
                ) {
                    val selectedAccount = accounts.find { it.id == selectedAccountId }
                    OutlinedTextField(
                        value = selectedAccount?.name ?: localizedString("Select Account"),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(localizedString("Select Account")) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAccountDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedAccountDropdown,
                        onDismissRequest = { expandedAccountDropdown = false }
                    ) {
                        accounts.forEach { account ->
                            DropdownMenuItem(
                                text = { Text("${account.name} (${currencyFormat.format(account.currentBalance)} RWF)") },
                                onClick = {
                                    selectedAccountId = account.id
                                    expandedAccountDropdown = false
                                }
                            )
                        }
                    }
                }

                // Summary / Profit Preview Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Total Sale:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "${currencyFormat.format(totalAmount)} ${localizedString("RWF")}",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "${localizedString("Profit Margin")}:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "+${currencyFormat.format(totalProfit)} ${localizedString("RWF")}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedAccountId == 0L && accounts.isNotEmpty()) {
                        selectedAccountId = accounts.first().id
                    }
                    onConfirm(quantity, selectedAccountId)
                },
                enabled = product.currentStock >= quantity && selectedAccountId != 0L
            ) {
                Text(localizedString("POS Checkout"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localizedString("Cancel"))
            }
        }
    )
}
