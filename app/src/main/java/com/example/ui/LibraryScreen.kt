package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DocumentChunk
import com.example.data.PdfDocument
import com.example.data.StudyMindDao
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun LibraryScreen(
    dao: StudyMindDao,
    onNavigateToPdf: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var pdfList by remember { mutableStateOf<List<PdfDocument>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    fun refreshPdfs() {
        coroutineScope.launch {
            pdfList = dao.getAllPdfs()
        }
    }

    LaunchedEffect(Unit) {
        refreshPdfs()
    }

    val filteredList = pdfList.filter {
        it.title.contains(searchQuery, ignoreCase = true)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Bar & Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Library",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Button(
                    onClick = { showAddDialog = true },
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Book")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Textbook", fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search textbooks...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "Library",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Your library is empty",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add standard pre-built textbooks or import your own notes/PDF contents below.",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 32.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList) { pdf ->
                        LibraryItemCard(
                            pdf = pdf,
                            onDelete = {
                                coroutineScope.launch {
                                    dao.deletePdf(pdf)
                                    refreshPdfs()
                                }
                            }
                        ) {
                            onNavigateToPdf(pdf.id)
                        }
                    }
                }
            }
        }

        // Add Educational Material Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Select educational study textbook") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Pick an interactive textbook chapter to import into your offline StudyMind AI environment:",
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )

                        TextbookOptionItem(
                            title = "Biochemical Pathways: Cellular Photosynthesis",
                            description = "Covers light-dependent reactions, Calvin cycle, thylakoid systems, and carbon fixation formulas.",
                            icon = Icons.Default.Eco,
                            color = Color(0xFF4CAF50)
                        ) {
                            coroutineScope.launch {
                                addEducationalBook(
                                    dao,
                                    title = "Biochemical Pathways: Photosynthesis",
                                    pages = 3,
                                    chunks = listOf(
                                        "Page 1: Photosynthesis is the vital biological pathway that converts solar energy into stable glucose chemical bonds. In eukaryotic phototrophs, this takes place in chloroplast organelles. The process is divided into light reactions occurring in the thylakoid membrane, and the Calvin Cycle which is hosted in the stroma fluid.",
                                        "Page 2: Light reactions rely on photosystem pigment complexes (PSI and PSII) to absorb photons. When chlorophyll a absorbs red and blue-violet light, its valence electrons are excited to high energy orbits. The electron vacancy in PSII is replenished via photolysis - the splitting of water molecules into hydrogen ions, electrons, and gaseous oxygen byproduct.",
                                        "Page 3: The Calvin Cycle (the light-independent reactions) fixes atmospheric carbon dioxide gas using the enzyme ribulose-1,5-bisphosphate carboxylase-oxygenase, commonly called RuBisCO. ATP and NADPH generated in light reactions drive the conversion of carbon intermediates into high-energy Glyceraldehyde 3-phosphate (G3P) sugar products."
                                    )
                                )
                                refreshPdfs()
                                showAddDialog = false
                            }
                        }

                        TextbookOptionItem(
                            title = "Foundations of Space Exploration",
                            description = "Covers flight dynamics, escape velocity, gravity staging, and history of Lunar Apollo missions.",
                            icon = Icons.Default.RocketLaunch,
                            color = Color(0xFF2196F3)
                        ) {
                            coroutineScope.launch {
                                addEducationalBook(
                                    dao,
                                    title = "Foundations of Space Exploration",
                                    pages = 3,
                                    chunks = listOf(
                                        "Page 1: Rocket flight is governed by Isaac Newton's third law of motion: for every action there is an equal and opposite reaction. Modern orbital spacecraft utilize liquid chemical propellants burned under extreme pressure inside combustion chambers and accelerated out supersonic De Laval nozzles.",
                                        "Page 2: Staging is the technology of dividing a single rocket vehicle into separate components. Once propellant in the heavy first stage (e.g. Saturn V S-IC) is depleted, it is jettisoned, discarding dead structural weight. This maximizes the acceleration of secondary stages attempting to exceed orbital velocity.",
                                        "Page 3: The historic NASA Project Apollo targeted landing astronauts on the lunar surface. Apollo 11 succeeded on July 20, 1969. The command module Columbia orbited the Moon while Lunar Module Eagle touched down in the Sea of Tranquility, driven by Neil Armstrong and Buzz Aldrin."
                                    )
                                )
                                refreshPdfs()
                                showAddDialog = false
                            }
                        }

                        TextbookOptionItem(
                            title = "Introduction to Machine Learning",
                            description = "Covers neural networks, regression formulas, supervised learning algorithms, and model optimization.",
                            icon = Icons.Default.Memory,
                            color = Color(0xFF9C27B0)
                        ) {
                            coroutineScope.launch {
                                addEducationalBook(
                                    dao,
                                    title = "Introduction to Machine Learning",
                                    pages = 3,
                                    chunks = listOf(
                                        "Page 1: Machine Learning (ML) is a core division of computer science that empowers software systems to discover patterns in datasets without procedural line-by-line coding. It is classified into Supervised Learning, which learns from explicit input-output tags, and Unsupervised Learning which discovers structures in unlabeled features.",
                                        "Page 2: Artificial Neural Networks (ANNs) are computing graphs inspired by biological brain neurons. Nodes (neurons) are arranged in layered architectures (Input, Hidden, Output) with numerical weights. Training updates weights using the backpropagation algorithm to minimize total classification error.",
                                        "Page 3: Overfitting is a primary danger in ML design. It occurs when a highly complex model learns the training dataset's noise instead of general patterns. While training accuracy becomes extremely high, the model's generalizability on fresh, unseen validation testing datasets is severely compromised."
                                    )
                                )
                                refreshPdfs()
                                showAddDialog = false
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

private suspend fun addEducationalBook(
    dao: StudyMindDao,
    title: String,
    pages: Int,
    chunks: List<String>
) {
    val pdfId = UUID.randomUUID().toString()
    val pdf = PdfDocument(
        id = pdfId,
        title = title,
        pageCount = pages,
        fileSizeBytes = chunks.sumOf { it.length.toLong() } * 2,
        processingStatus = "READY"
    )
    dao.insertPdf(pdf)

    val dbChunks = chunks.mapIndexed { idx, txt ->
        DocumentChunk(
            pdfId = pdfId,
            chunkIndex = idx,
            pageNumber = idx + 1,
            text = txt
        )
    }
    dao.insertChunks(dbChunks)
}

@Composable
fun TextbookOptionItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(color.copy(alpha = 0.15f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = color)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = description, fontSize = 11.sp, color = Color.Gray, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun LibraryItemCard(
    pdf: PdfDocument,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "Book",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = pdf.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${pdf.pageCount} Pages • ${(pdf.fileSizeBytes / 1024f).toInt()} KB",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete book",
                    tint = Color.Red.copy(alpha = 0.7f)
                )
            }
        }
    }
}
