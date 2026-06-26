package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Quote
import com.example.ui.QuoteViewModel
import com.example.ui.theme.Amber500
import com.example.ui.theme.Amber600
import com.example.ui.theme.Amber700
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Indigo400
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Rose400
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Teal400
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: QuoteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Support manual toggle or system dark theme
            var manualDarkTheme by remember { mutableStateOf<Boolean?>(null) }
            val systemDark = isSystemInDarkTheme()
            val isDark = manualDarkTheme ?: systemDark

            MyApplicationTheme(darkTheme = isDark) {
                QuoteAppScreen(
                    viewModel = viewModel,
                    isDark = isDark,
                    onToggleTheme = { manualDarkTheme = !isDark }
                )
            }
        }
    }
}

@Composable
fun QuoteAppScreen(
    viewModel: QuoteViewModel,
    isDark: Boolean,
    onToggleTheme: () -> Unit
) {
    val context = LocalContext.current
    val currentQuote by viewModel.currentQuote.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val hasPrevious = viewModel.hasPrevious()
    val isLoading by viewModel.isLoading.collectAsState()
    val networkError by viewModel.networkError.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(networkError) {
        networkError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    val allQuotes by viewModel.allQuotes.collectAsState()

    // Categories list for chips filtering dynamically generated from all loaded quotes
    val categories = remember(allQuotes) {
        val base = listOf("All", "Wisdom", "Motivation", "Science", "Art")
        val customCats = allQuotes.map { it.category }
            .filter { it.isNotBlank() && it !in base && it != "Favorites" && it != "My Quotes" && it != "All" }
            .distinct()
            .sorted()
        base + customCats + listOf("Favorites", "My Quotes")
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("main_scaffold")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Bar / App Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Inspired",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 28.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Slate900
                        )
                    )
                    Text(
                        text = "Your daily dose of wisdom",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isDark) Slate300 else Slate700
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Manual theme switch button
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Slate800 else Slate100)
                            .testTag("theme_toggle")
                    ) {
                        Text(
                            text = if (isDark) "☀️" else "🌙",
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Add Custom Quote Button
                    IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .testTag("add_quote_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Custom Quote",
                            tint = if (isDark) Slate900 else Color.White
                        )
                    }
                }
            }

            // 2. Scrollable Category Chips Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .testTag("category_row"),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = category == selectedCategory
                    val chipBgColor = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        if (isDark) Slate800 else Slate100
                    }
                    val chipTextColor = if (isSelected) {
                        if (isDark) Slate900 else Color.White
                    } else {
                        if (isDark) Slate100 else Slate900
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(chipBgColor)
                            .clickable { viewModel.selectCategory(category) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("category_chip_$category"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = chipTextColor
                            )
                        )
                    }
                }
            }

            // 3. Central Quote Box / Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp).testTag("quote_loader")
                    )
                } else if (currentQuote != null) {
                    val quote = currentQuote!!
                    
                    // Category-based smooth colors for aesthetic pairings
                    val accentColor by animateColorAsState(
                        targetValue = when (quote.category) {
                            "Wisdom" -> Indigo400
                            "Motivation" -> Amber500
                            "Science" -> Teal400
                            "Art" -> Rose400
                            else -> Emerald400
                        },
                        animationSpec = spring(),
                        label = "AccentColor"
                    )

                    val cardBgColor by animateColorAsState(
                        targetValue = if (isDark) {
                            when (quote.category) {
                                "Wisdom" -> Color(0xFF131524)
                                "Motivation" -> Color(0xFF1C1810)
                                "Science" -> Color(0xFF0F1B1A)
                                "Art" -> Color(0xFF1D1216)
                                else -> Color(0xFF0E1A14)
                            }
                        } else {
                            when (quote.category) {
                                "Wisdom" -> Color(0xFFEEF2F6)
                                "Motivation" -> Color(0xFFFFFBEB)
                                "Science" -> Color(0xFFF0FDF4)
                                "Art" -> Color(0xFFFFF1F2)
                                else -> Color(0xFFF0FDF4)
                            }
                        },
                        animationSpec = spring(),
                        label = "CardBgColor"
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(28.dp),
                                clip = false,
                                spotColor = accentColor
                            )
                            .testTag("quote_card"),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBgColor),
                        border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.5f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp)
                        ) {
                            // Semi-transparent Quote Icon overlay in background (Top-Left)
                            Icon(
                                painter = painterResource(id = R.drawable.ic_quote_decor),
                                contentDescription = null,
                                tint = accentColor.copy(alpha = 0.15f),
                                modifier = Modifier
                                    .size(72.dp)
                                    .align(Alignment.TopStart)
                                    .offset(x = (-12).dp, y = (-12).dp)
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Category Tag
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(accentColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = quote.category.uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = accentColor,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                AnimatedContent(
                                    targetState = quote,
                                    transitionSpec = {
                                        fadeIn(animationSpec = tween(durationMillis = 500)) togetherWith
                                                fadeOut(animationSpec = tween(durationMillis = 300))
                                    },
                                    label = "QuoteTransition"
                                ) { targetQuote ->
                                    SelectionContainer {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            // Quote Text
                                            Text(
                                                text = "“${targetQuote.text}”",
                                                style = MaterialTheme.typography.headlineSmall.copy(
                                                    fontSize = 22.sp,
                                                    fontFamily = FontFamily.Serif,
                                                    fontStyle = FontStyle.Italic,
                                                    fontWeight = FontWeight.SemiBold,
                                                    lineHeight = 32.sp,
                                                    color = if (isDark) Color.White else Slate900,
                                                    textAlign = TextAlign.Center
                                                ),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 8.dp)
                                                    .testTag("quote_text")
                                            )

                                            Spacer(modifier = Modifier.height(20.dp))

                                            // Quote Author
                                            Text(
                                                text = "— ${targetQuote.author}",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontSize = 16.sp,
                                                    fontFamily = FontFamily.SansSerif,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (isDark) Slate300 else Slate700,
                                                    textAlign = TextAlign.Center
                                                ),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("quote_author")
                                            )
                                        }
                                    }
                                }

                                // Delete custom quote if applicable
                                if (quote.isCustom) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background((if (isDark) Color(0xFF3B1D20) else Color(0xFFFFEBEE)))
                                            .clickable { viewModel.deleteCurrentCustomQuote() }
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Quote",
                                            tint = Rose400,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Delete My Quote",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Rose400,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Empty state (e.g. no favorites saved)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_quote_decor),
                            contentDescription = null,
                            tint = if (isDark) Slate800 else Slate200,
                            modifier = Modifier
                                .size(96.dp)
                                .alpha(0.6f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (selectedCategory == "Favorites") "No favorites saved yet" else "No quotes found",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Slate300 else Slate700
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (selectedCategory == "Favorites") {
                                "Find a quote you love and click the heart icon to save it here!"
                            } else {
                                "Add a custom quote or select another category."
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (isDark) Slate300.copy(alpha = 0.6f) else Slate700.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }

            // 4. Interactive Navigation & Action controls
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back/Undo Button
                    IconButton(
                        onClick = { viewModel.goBack() },
                        enabled = hasPrevious,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                if (hasPrevious) {
                                    if (isDark) Slate800 else Slate100
                                } else {
                                    if (isDark) Slate900 else Color.White
                                }
                            )
                            .testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Previous Quote",
                            tint = if (hasPrevious) {
                                if (isDark) Color.White else Slate900
                            } else {
                                if (isDark) Slate700 else Slate300
                            }
                        )
                    }

                    // MAIN PRIMARY Action: "New Quote" button with golden gradient background and elegant floating shadow
                    Button(
                        onClick = { viewModel.showRandomQuote() },
                        modifier = Modifier
                            .height(56.dp)
                            .width(180.dp)
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(28.dp),
                                spotColor = Amber500
                            )
                            .testTag("new_quote_button"),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Amber500, Amber600, Amber700),
                                        start = Offset(0f, 0f),
                                        end = Offset(250f, 150f)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_refresh),
                                    contentDescription = null,
                                    tint = Slate900,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "New Quote",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Slate900,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                )
                            }
                        }
                    }

                    // Favorite Button
                    IconButton(
                        onClick = { viewModel.toggleFavoriteCurrent() },
                        enabled = currentQuote != null,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                if (currentQuote != null) {
                                    if (isDark) Slate800 else Slate100
                                } else {
                                    if (isDark) Slate900 else Color.White
                                }
                            )
                            .testTag("favorite_button")
                    ) {
                        val favColor = if (isFavorite) Rose400 else (if (isDark) Slate300 else Slate700)
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Toggle Favorite",
                            tint = if (currentQuote != null) favColor else (if (isDark) Slate700 else Slate300)
                        )
                    }
                }

                // 5. Utility Copy and Share Controls
                if (currentQuote != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Share Button
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isDark) Slate800 else Slate100)
                                .clickable {
                                    val current = currentQuote ?: return@clickable
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "“${current.text}”\n— ${current.author}\n\nShared via Random Quote Generator"
                                        )
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "Share quote using")
                                    context.startActivity(shareIntent)
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("share_quote"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Quote",
                                tint = if (isDark) Slate300 else Slate700,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Share",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (isDark) Slate300 else Slate700,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    // Custom Quote Add Dialog
    if (showAddDialog) {
        AddQuoteDialog(
            isDark = isDark,
            existingCategories = categories.filter { it != "All" && it != "Favorites" && it != "My Quotes" },
            onDismiss = { showAddDialog = false },
            onSave = { text, author, category ->
                viewModel.addCustomQuote(text, author, category)
                showAddDialog = false
                Toast.makeText(context, "Successfully saved custom quote!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddQuoteDialog(
    isDark: Boolean,
    existingCategories: List<String>,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(existingCategories.firstOrNull() ?: "Wisdom") }
    var expanded by remember { mutableStateOf(false) }

    // State for custom category
    var isNewCategory by remember { mutableStateOf(false) }
    var newCategoryText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .testTag("add_quote_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = if (isDark) Slate800 else Color.White,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Add Custom Quote",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Slate900
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quote Text field
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Quote Text") },
                    placeholder = { Text("Type or paste your inspiring quote here...") },
                    modifier = Modifier.fillMaxWidth().testTag("add_quote_text_input"),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (isDark) Slate700 else Slate300,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Author field
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Author (Optional)") },
                    placeholder = { Text("Unknown") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_quote_author_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (isDark) Slate700 else Slate300,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category dropdown picker
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth().testTag("add_quote_category_picker")
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = if (isNewCategory) "Create New..." else category,
                        onValueChange = {},
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (isDark) Slate700 else Slate300,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(if (isDark) Slate800 else Color.White)
                    ) {
                        existingCategories.forEach { selection ->
                            DropdownMenuItem(
                                text = { Text(selection) },
                                onClick = {
                                    category = selection
                                    isNewCategory = false
                                    expanded = false
                                },
                                modifier = Modifier.testTag("dropdown_item_$selection")
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(if (isDark) Slate700 else Slate200)
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "+ New Category...",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            },
                            onClick = {
                                isNewCategory = true
                                expanded = false
                            },
                            modifier = Modifier.testTag("dropdown_item_new_category")
                        )
                    }
                }

                if (isNewCategory) {
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newCategoryText,
                        onValueChange = { newCategoryText = it },
                        label = { Text("New Category Name") },
                        placeholder = { Text("e.g. Leadership, Faith, Tech") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_quote_custom_category_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (isDark) Slate700 else Slate300,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, if (isDark) Slate700 else Slate300),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isDark) Slate200 else Slate700
                        ),
                        modifier = Modifier.testTag("dialog_cancel_button")
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            val finalCategory = if (isNewCategory) {
                                val trimmed = newCategoryText.trim()
                                if (trimmed.isNotEmpty()) {
                                    trimmed.split(" ")
                                        .filter { it.isNotBlank() }
                                        .joinToString(" ") { word ->
                                            word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                                        }
                                } else {
                                    "Custom"
                                }
                            } else {
                                category
                            }
                            onSave(text, author, finalCategory)
                        },
                        enabled = text.trim().isNotEmpty() && (!isNewCategory || newCategoryText.trim().isNotEmpty()),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = if (isDark) Slate900 else Color.White
                        ),
                        modifier = Modifier.testTag("dialog_save_button")
                    ) {
                        Text("Save Quote")
                    }
                }
            }
        }
    }
}
