// ui/feature/finance/FinanceScreen.kt
package com.example.moflow.ui.feature.finance

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.moflow.domain.model.Transaction
import com.example.moflow.domain.model.TransactionCategory
import com.example.moflow.domain.model.TransactionType
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    viewModel: FinanceViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showFilterMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manajemen Keuangan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter"
                        )
                    }
                    IconButton(onClick = { exportToPdf(context, state.filteredTransactions) }) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Ekspor ke PDF"
                        )
                    }
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        Text(
                            text = "Filter berdasarkan Bulan",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        DropdownMenuItem(
                            text = { Text("Semua Bulan") },
                            onClick = {
                                viewModel.onMonthFilterChange(-1)
                                showFilterMenu = false
                            }
                        )
                        val months = listOf(
                            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
                        )
                        months.forEachIndexed { index, month ->
                            DropdownMenuItem(
                                text = { Text(month) },
                                onClick = {
                                    viewModel.onMonthFilterChange(index)
                                    showFilterMenu = false
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Filter berdasarkan Tipe",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        DropdownMenuItem(
                            text = { Text("Semua Tipe") },
                            onClick = {
                                viewModel.onTypeFilterChange(null)
                                showFilterMenu = false
                            }
                        )
                        TransactionType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    viewModel.onTypeFilterChange(type)
                                    showFilterMenu = false
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Filter berdasarkan Kategori",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        DropdownMenuItem(
                            text = { Text("Semua Kategori") },
                            onClick = {
                                viewModel.onCategoryFilterChange(null)
                                showFilterMenu = false
                            }
                        )
                        TransactionCategory.values().forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    viewModel.onCategoryFilterChange(category)
                                    showFilterMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Transaksi")
            }
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Kartu ringkasan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SummaryCard(
                        title = "Pemasukan",
                        amount = state.monthlyIncome,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.primary
                    )
                    SummaryCard(
                        title = "Pengeluaran",
                        amount = state.monthlyExpense,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Kartu saldo
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Saldo",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Rp${String.format("%,.0f", state.monthlyIncome - state.monthlyExpense)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (state.monthlyIncome >= state.monthlyExpense)
                                MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Indikator filter
                val filterText = buildString {
                    append("Menampilkan: ")
                    if (state.selectedMonth != -1) {
                        val months = listOf(
                            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
                        )
                        append(months[state.selectedMonth])
                    } else {
                        append("Semua Bulan")
                    }

                    // Safe call dan null check untuk selectedType
                    state.selectedType?.let {
                        append(" | ${it.name}")  // Safe access to selectedType.name
                    }

                    // Safe call dan null check untuk selectedCategory
                    state.selectedCategory?.let {
                        append(" | ${it.name}")  // Safe access to selectedCategory.name
                    }
                }

                Text(
                    text = filterText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Daftar transaksi
                if (state.filteredTransactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tidak ada transaksi ditemukan",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.filteredTransactions) { transaction ->
                            TransactionItem(
                                transaction = transaction,
                                onEdit = { viewModel.openEditDialog(transaction) },
                                onDelete = { viewModel.deleteTransaction(transaction) }
                            )
                        }
                    }
                }
            }
        }

        // Dialog Tambah Transaksi
        if (state.showAddDialog) {
            TransactionDialog(
                title = "Tambah Transaksi",
                confirmButtonText = "Tambah",
                uiState = uiState,
                onAmountChange = viewModel::onAmountChange,
                onTypeChange = viewModel::onTypeChange,
                onCategoryChange = viewModel::onCategoryChange,
                onDateChange = viewModel::onDateChange,
                onNotesChange = viewModel::onNotesChange,
                onDismiss = viewModel::closeAddDialog,
                onConfirm = viewModel::addTransaction
            )
        }

        // Dialog Edit Transaksi
        if (state.showEditDialog) {
            TransactionDialog(
                title = "Edit Transaksi",
                confirmButtonText = "Perbarui",
                uiState = uiState,
                onAmountChange = viewModel::onAmountChange,
                onTypeChange = viewModel::onTypeChange,
                onCategoryChange = viewModel::onCategoryChange,
                onDateChange = viewModel::onDateChange,
                onNotesChange = viewModel::onNotesChange,
                onDismiss = viewModel::closeEditDialog,
                onConfirm = viewModel::updateTransaction
            )
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    amount: Double,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Rp${String.format("%,.0f", amount)}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

// ui/feature/finance/FinanceScreen.kt (lanjutan)
@Composable
fun TransactionItem(
    transaction: Transaction,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateFormat.format(transaction.date),
                    style = MaterialTheme.typography.bodySmall
                )
                if (transaction.notes.isNotEmpty()) {
                    Text(
                        text = transaction.notes,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text = "Rp${String.format("%,.0f", transaction.amount)}",
                style = MaterialTheme.typography.titleMedium,
                color = if (transaction.type == TransactionType.INCOME)
                    MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 8.dp)
            )

            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDialog(
    title: String,
    confirmButtonText: String,
    uiState: TransactionUiState,
    onAmountChange: (String) -> Unit,
    onTypeChange: (TransactionType) -> Unit,
    onCategoryChange: (TransactionCategory) -> Unit,
    onDateChange: (Date) -> Unit,
    onNotesChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.date.time
    )

    var expandedType by remember { mutableStateOf(false) }
    var expandedCategory by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = uiState.amount.isNotEmpty() && uiState.amount.toDoubleOrNull() != null
            ) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Amount field
                OutlinedTextField(
                    value = uiState.amount,
                    onValueChange = onAmountChange,
                    label = { Text("Jumlah") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.amount.isNotEmpty() && uiState.amount.toDoubleOrNull() == null
                )

                // Transaction type dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedType,
                    onExpandedChange = { expandedType = it }
                ) {
                    OutlinedTextField(
                        value = uiState.type.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipe") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedType,
                        onDismissRequest = { expandedType = false }
                    ) {
                        TransactionType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    onTypeChange(type)
                                    expandedType = false
                                }
                            )
                        }
                    }
                }

                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = it }
                ) {
                    OutlinedTextField(
                        value = uiState.category.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategori") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        TransactionCategory.values().forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    onCategoryChange(category)
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }

                // Date picker
                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                OutlinedTextField(
                    value = dateFormat.format(uiState.date),
                    onValueChange = {},
                    label = { Text("Tanggal") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Pilih tanggal"
                            )
                        }
                    }
                )

                // Notes field
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = onNotesChange,
                    label = { Text("Catatan") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        onDateChange(Date(it))
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

fun exportToPdf(context: Context, transactions: List<Transaction>) {
    // Gunakan coroutine scope untuk operasi I/O
    kotlinx.coroutines.MainScope().launch(Dispatchers.IO) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            // Ubah val menjadi var karena akan di-reassign
            var page = pdfDocument.startPage(pageInfo)
            // Ubah val menjadi var karena akan di-reassign
            var canvas = page.canvas
            val paint = android.graphics.Paint()

            // Pengaturan style untuk teks
            paint.textSize = 14f
            val boldPaint = android.graphics.Paint().apply {
                textSize = 16f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            }
            val headerPaint = android.graphics.Paint().apply {
                textSize = 20f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            }

            // Header laporan
            canvas.drawText("MoFlow - Laporan Transaksi", 50f, 50f, headerPaint)

            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
            canvas.drawText("Dibuat pada: ${dateFormat.format(Date())}", 50f, 80f, paint)
            canvas.drawLine(50f, 90f, 545f, 90f, paint)

            // Judul kolom dengan style bold
            var y = 120f
            canvas.drawText("Tanggal", 50f, y, boldPaint)
            canvas.drawText("Kategori", 150f, y, boldPaint)
            canvas.drawText("Tipe", 280f, y, boldPaint)
            canvas.drawText("Jumlah", 350f, y, boldPaint)
            canvas.drawText("Catatan", 450f, y, boldPaint)

            y += 20f
            canvas.drawLine(50f, y, 545f, y, paint)
            y += 20f

            // Batasi jumlah transaksi per halaman
            val maxItemsPerPage = 25
            var itemsOnCurrentPage = 0

            for (transaction in transactions) {
                // Periksa apakah perlu halaman baru
                if (itemsOnCurrentPage >= maxItemsPerPage) {
                    pdfDocument.finishPage(page)
                    val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
                    // Sekarang dapat di-reassign karena dideklarasikan sebagai var
                    page = pdfDocument.startPage(newPageInfo)
                    // Sekarang dapat di-reassign karena dideklarasikan sebagai var
                    canvas = page.canvas
                    y = 50f
                    canvas.drawText("MoFlow - Laporan Transaksi (lanjutan)", 50f, y, boldPaint)
                    y += 30f
                    canvas.drawText("Tanggal", 50f, y, boldPaint)
                    canvas.drawText("Kategori", 150f, y, boldPaint)
                    canvas.drawText("Tipe", 280f, y, boldPaint)
                    canvas.drawText("Jumlah", 350f, y, boldPaint)
                    canvas.drawText("Catatan", 450f, y, boldPaint)
                    y += 20f
                    canvas.drawLine(50f, y, 545f, y, paint)
                    y += 20f
                    itemsOnCurrentPage = 0
                }

                val date = dateFormat.format(transaction.date)
                canvas.drawText(date, 50f, y, paint)
                canvas.drawText(transaction.category.name, 150f, y, paint)
                canvas.drawText(transaction.type.name, 280f, y, paint)

                // Format mata uang sebagai Rupiah
                val amount = "Rp${String.format("%,.0f", transaction.amount)}"
                canvas.drawText(amount, 350f, y, paint)

                // Batasi panjang catatan agar tidak keluar halaman
                val notes = if (transaction.notes.length > 15)
                    "${transaction.notes.take(15)}..."
                else
                    transaction.notes
                canvas.drawText(notes, 450f, y, paint)

                y += 25f
                itemsOnCurrentPage++
            }

            // Ringkasan total di bagian bawah
            canvas.drawLine(50f, y, 545f, y, paint)
            y += 25f

            val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

            canvas.drawText("Total Pemasukan: Rp${String.format("%,.0f", totalIncome)}", 50f, y, boldPaint)
            y += 25f
            canvas.drawText("Total Pengeluaran: Rp${String.format("%,.0f", totalExpense)}", 50f, y, boldPaint)
            y += 25f
            canvas.drawText("Saldo: Rp${String.format("%,.0f", totalIncome - totalExpense)}", 50f, y, boldPaint)

            pdfDocument.finishPage(page)

            // Menggunakan FileProvider untuk Android 10+ compatibility
            val pdfFolder = File(context.getExternalFilesDir(null), "MoFlow")
            if (!pdfFolder.exists()) {
                pdfFolder.mkdirs()
            }

            val fileName = "MoFlow_Transaksi_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
            val pdfFile = File(pdfFolder, fileName)

            // Tulis file dengan FileOutputStream
            val fos = FileOutputStream(pdfFile)
            pdfDocument.writeTo(fos)
            fos.close()
            pdfDocument.close()

            // Berikan hasil di thread utama
            withContext(Dispatchers.Main) {
                // Buat URI menggunakan FileProvider
                val fileUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    pdfFile
                )

                // Tampilkan notifikasi sukses
                Toast.makeText(
                    context,
                    "PDF berhasil disimpan: ${pdfFile.name}",
                    Toast.LENGTH_LONG
                ).show()

                // Buka PDF dengan intent
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(fileUri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(
                        context,
                        "Tidak ada aplikasi PDF viewer. File disimpan di: ${pdfFile.absolutePath}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Gagal membuat PDF: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}