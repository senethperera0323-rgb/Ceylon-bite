package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.theme.*
import com.example.viewmodel.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: RestaurantViewModel = viewModel()
                    CeylonBiteMainApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun CeylonBiteMainApp(viewModel: RestaurantViewModel) {
    val currentRole by viewModel.currentRole.collectAsState()
    val lang by viewModel.selectedLanguage.collectAsState()

    // Manage general backstack or visual shifts
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        when (currentRole) {
            UserRole.SPLASH -> SplashView(viewModel, lang)
            UserRole.CUSTOMER -> CustomerView(viewModel, lang)
            UserRole.STAFF_LOGIN -> StaffLoginView(viewModel, lang)
            UserRole.STAFF_DASHBOARD -> StaffDashboardView(viewModel, lang)
        }
    }
}

@Composable
fun NeobrutalistBox(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.onBackground,
    borderWidth: androidx.compose.ui.unit.Dp = 2.dp,
    cornerRadius: androidx.compose.ui.unit.Dp = 16.dp,
    shadowOffset: androidx.compose.ui.unit.Dp = 4.dp,
    shadowColor: Color = MaterialTheme.colorScheme.onBackground,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier.padding(end = shadowOffset, bottom = shadowOffset)
    ) {
        // Shadow Layer (placed behind, offset slightly by layout parameters)
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .background(shadowColor, shape)
        )
        // Main Container Layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(containerColor, shape)
                .border(borderWidth, borderColor, shape)
                .clip(shape)
        ) {
            content()
        }
    }
}

// ==========================================
// 1. SPLASH / ROLE & TRILINGUAL GATEWAY VIEW
// ==========================================
@Composable
fun SplashView(viewModel: RestaurantViewModel, lang: String) {
    val activeKdsOrders by viewModel.activeKdsOrders.collectAsState()
    val listItems by viewModel.menuItemsList.collectAsState()
    
    val waitingCount = activeKdsOrders.count { it.orderStatus == "PENDING" || it.orderStatus == "PREPARING" }
    val readyCount = activeKdsOrders.count { it.orderStatus == "OUT_FOR_DELIVERY" }
    
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top AppBar block (thematic replica of design HTML header)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = viewModel.tr("app_title", lang).uppercase(),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "SRI LANKA • PRO EDITION",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // Trilingual Switcher Panel mapped directly to the beautiful HTML capsule bar shape
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(24.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    Triple("en", "EN", "lang_btn_en"),
                    Triple("si", "සිං", "lang_btn_si"),
                    Triple("ta", "த", "lang_btn_ta")
                ).forEach { (code, label, tag) ->
                    val isSelected = lang == code
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { viewModel.setLanguage(code) }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag(tag),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // 1. Aggregator Status Grid Mockups (dynamic offline/online check)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Uber Eats block
            NeobrutalistBox(
                modifier = Modifier.weight(1f),
                containerColor = Color(0xFFE8DEF8),
                borderWidth = 2.dp,
                cornerRadius = 24.dp,
                shadowOffset = 3.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                        .height(80.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "UBER EATS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF22C55E), RoundedCornerShape(4.dp))
                        )
                    }
                    Text(
                        text = "ONLINE",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // PickMe Eats block
            NeobrutalistBox(
                modifier = Modifier.weight(1f),
                containerColor = Color(0xFF1D1B20),
                borderWidth = 2.dp,
                borderColor = Color(0xFF1D1B20),
                cornerRadius = 24.dp,
                shadowOffset = 3.dp,
                shadowColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                        .height(80.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PICKME EATS",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF4ADE80), RoundedCornerShape(4.dp))
                        )
                    }
                    Text(
                        text = "ONLINE",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )
                }
            }
        }

        // 2. Statistics Hero Box
        NeobrutalistBox(
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color.White,
            cornerRadius = 28.dp,
            shadowOffset = 6.dp
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACTIVE ORDERS • LIVE KDS",
                        style = MaterialTheme.typography.titleMedium.copy(fontStyle = FontStyle.Italic),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "+12% TODAY",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Text(
                    text = String.format("%02d", activeKdsOrders.size),
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 68.sp),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.Black.copy(alpha = 0.1f))
                        .padding(vertical = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "KDS READY",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                        Text(
                            text = String.format("%02d", readyCount),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "WAITING",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                        Text(
                            text = String.format("%02d", waitingCount),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }

        // 3. AI Preview Box Mocker
        val featuredItem = listItems.firstOrNull { it.isAvailable }
        val featuredName = featuredItem?.nameEn ?: "Kottu Roti Special"
        val featuredImage = featuredItem?.imageUrl ?: ""
        
        NeobrutalistBox(
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            cornerRadius = 16.dp,
            shadowOffset = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (featuredImage.isNotEmpty()) {
                        AsyncImage(
                            model = featuredImage,
                            contentDescription = featuredName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFFFFFAF0), Color(0xFFF3EDF7))
                                    )
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = featuredName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "AI Image synthesis auto-synchronized",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "AI Spark",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Role Gateway Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Customer Entry Portal
            Button(
                onClick = { viewModel.navigateTo(UserRole.CUSTOMER) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .testTag("customer_portal_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2EC4B6),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, Color.Black)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = viewModel.tr("btn_customer", lang),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Staff Entry Portal
            Button(
                onClick = { viewModel.navigateTo(UserRole.STAFF_LOGIN) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("staff_portal_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, Color.Black)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = viewModel.tr("btn_staff", lang),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Fineprint Attribution Credits
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "CREATED BY SENETH PERERA",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f)
            )
            Text(
                text = "Seneth Perera Platform Dev © 2026",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 9.sp),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

// ==========================================
// 2. CUSTOMER PORTAL VIEW
// ==========================================
@Composable
fun CustomerView(viewModel: RestaurantViewModel, lang: String) {
    val selectedTown by viewModel.selectedTown.collectAsState()
    val isDeliveryAllowed by viewModel.isDeliveryAllowed.collectAsState()
    val deliveryMethod by viewModel.deliveryMethod.collectAsState()
    val cartList by viewModel.cartList.collectAsState()
    val customizingItem by viewModel.customizingItem.collectAsState()
    val menuItemsAll by viewModel.menuItemsList.collectAsState()
    val activeTrackedOrder by viewModel.activeTrackedOrder.collectAsState()
    val isStoreOpen by viewModel.isStoreOpen.collectAsState()

    var showLocationSelector by remember { mutableStateOf(false) }
    var showCheckoutScreen by remember { mutableStateOf(false) }

    // Filter available menu items
    val availableMenuItems = menuItemsAll.filter { it.isAvailable }

    if (activeTrackedOrder != null) {
        CustomerOrderTrackerView(viewModel, lang) {
            viewModel.clearTrackedOrder()
            viewModel.navigateTo(UserRole.SPLASH)
        }
        return
    }

    if (showCheckoutScreen) {
        CustomerCheckoutView(
            viewModel = viewModel,
            lang = lang,
            onClose = { showCheckoutScreen = false }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2023)),
            shape = RoundedCornerShape(0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.navigateTo(UserRole.SPLASH) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }

                    Text(
                        text = viewModel.tr("app_title", lang),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF2EC4B6)
                    )

                    Spacer(modifier = Modifier.width(36.dp))
                }

                // Customer localization summary & radial coverage button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(Color(0xFF2B2D31), RoundedCornerShape(12.dp))
                        .clickable { showLocationSelector = true }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Location Pin",
                            tint = Color(0xFFFF9F1C),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = selectedTown.name,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${viewModel.tr("lbl_distance_from_branch", lang)}: ${selectedTown.distanceKm} km",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                if (isDeliveryAllowed) Color(0xFF2EC4B6).copy(alpha = 0.15f)
                                else Color(0xFFE71D36).copy(alpha = 0.15f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isDeliveryAllowed) viewModel.tr("zone_delivery", lang)
                                   else viewModel.tr("zone_takeaway_only", lang),
                            color = if (isDeliveryAllowed) Color(0xFF2EC4B6) else Color(0xFFE71D36),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // If store is closed, display warnings
                if (!isStoreOpen) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE71D36).copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = "Closed", tint = Color(0xFFE71D36))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = viewModel.tr("closed", lang),
                                color = Color(0xFFE71D36),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Main Menu Body
        if (showLocationSelector) {
            // Location selection panel
            LocationGeofencingSelector(
                viewModel = viewModel,
                lang = lang,
                onClose = { showLocationSelector = false }
            )
        } else {
            Box(modifier = Modifier.weight(1f)) {
                if (availableMenuItems.isEmpty()) {
                    // Empty Catalog state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Empty",
                            tint = Color.DarkGray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Delicacies Available Online",
                            color = Color.LightGray,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Restaurant managers are cooking something fresh. Stand by!",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Text(
                                text = viewModel.tr("menu_categories", lang),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        items(availableMenuItems) { item ->
                            val localName = when (lang) {
                                "si" -> item.nameSi
                                "ta" -> item.nameTa
                                else -> item.nameEn
                            }
                            val localDesc = when (lang) {
                                "si" -> item.descriptionSi
                                "ta" -> item.descriptionTa
                                else -> item.descriptionEn
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("menu_card_${item.id}"),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2023)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(modifier = Modifier.padding(12.dp)) {
                                    // Food Image
                                    AsyncImage(
                                        model = item.imageUrl,
                                        contentDescription = item.nameEn,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(90.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.DarkGray)
                                    )

                                    Spacer(modifier = Modifier.width(14.dp))

                                    // Content details
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = localName,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )

                                        Text(
                                            text = localDesc,
                                            fontSize = 11.sp,
                                            color = Color.LightGray.copy(alpha = 0.7f),
                                            lineHeight = 15.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${viewModel.tr("price_lkr", lang)} ${item.basePrice}",
                                                color = Color(0xFFFF9F1C),
                                                fontWeight = FontWeight.Black,
                                                fontSize = 15.sp
                                            )

                                            Button(
                                                onClick = { viewModel.startCustomize(item) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2EC4B6)),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                modifier = Modifier.height(30.dp),
                                                enabled = isStoreOpen
                                            ) {
                                                Text(
                                                    text = viewModel.tr("add_to_cart", lang),
                                                    fontSize = 10.sp,
                                                    color = Color.Black,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // BOTTOM CART BAR OVERLAY
                if (cartList.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cart_panel"),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2EC4B6)),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${cartList.size} Items Selected",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Total: Rs. ${viewModel.getCartTotal()}",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp
                                    )
                                }

                                Button(
                                    onClick = { showCheckoutScreen = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = viewModel.tr("btn_checkout", lang),
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // MODIFIER CUSTOMIZATION OVERLAY DIALOG
    customizingItem?.let { item ->
        val selectedModifiers by viewModel.selectedModifiers.collectAsState()
        val allModifiers by viewModel.modifiersList.collectAsState()
        val itemModifiers = allModifiers.filter { it.menuItemId == item.id }

        var quantity by remember { mutableIntStateOf(1) }

        Dialog(onDismissRequest = { viewModel.confirmCustomizeAndAddCart(item, 0) }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2023)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = viewModel.tr("customize_item", lang),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    Text(
                        text = when (lang) {
                            "si" -> item.nameSi
                            "ta" -> item.nameTa
                            else -> item.nameEn
                        },
                        fontSize = 15.sp,
                        color = Color(0xFFFF9F1C),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Text(
                        text = "${viewModel.tr("base_price", lang)}: Rs. ${item.basePrice}",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (itemModifiers.isEmpty()) {
                        Text(
                            text = "No modifier additives registered for this dish.",
                            fontSize = 12.sp,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .heightIn(max = 180.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(itemModifiers) { modifier ->
                                val modifierLabel = when (lang) {
                                    "si" -> modifier.nameSi
                                    "ta" -> modifier.nameTa
                                    else -> modifier.nameEn
                                }
                                val isChecked = selectedModifiers.any { it.id == modifier.id }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF2B2D31), RoundedCornerShape(10.dp))
                                        .clickable { viewModel.toggleModifier(modifier) }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { viewModel.toggleModifier(modifier) },
                                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2EC4B6))
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(modifierLabel, color = Color.White, fontSize = 13.sp)
                                    }

                                    Text("+ Rs. ${modifier.price}", color = Color(0xFFFF9F1C), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quantity incrementer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Quantity / ප්‍රමාණය", color = Color.LightGray, fontSize = 13.sp)

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (quantity > 1) quantity-- }) {
                                Text("-", color = Color.LightGray, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = quantity.toString(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            IconButton(onClick = { quantity++ }) {
                                Icon(Icons.Default.Add, contentDescription = "Plus", tint = Color.LightGray)
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.DarkGray)

                    // Display aggregate calculations
                    val activeAddonsPrice = selectedModifiers.sumOf { it.price }
                    val calculatedRowPrice = (item.basePrice + activeAddonsPrice) * quantity

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(viewModel.tr("item_total", lang), color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("Rs. $calculatedRowPrice", color = Color(0xFFFF9F1C), fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.confirmCustomizeAndAddCart(item, 0) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                        ) {
                            Text("Cancel", color = Color.White)
                        }

                        Button(
                            onClick = { viewModel.confirmCustomizeAndAddCart(item, quantity) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2EC4B6))
                        ) {
                            Text(viewModel.tr("btn_confirm_item", lang), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// GEOFENCING LOCATIONS DRILL VIEW
@Composable
fun LocationGeofencingSelector(
    viewModel: RestaurantViewModel,
    lang: String,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("location_geofencing_selector"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2023)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = viewModel.tr("delivery_location", lang),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 15.sp
                )

                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.Gray)
                }
            }

            Text(
                text = "We check operational boundaries dynamically for Uber Eats (Max 5km) and PickMe Eats (Max 7km). Beyond this, system shifts to secure takeaway mode.",
                color = Color.Gray,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 240.dp)
            ) {
                items(viewModel.sriLankanTowns) { town ->
                    val isSelected = viewModel.selectedTown.value.name == town.name

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isSelected) Color(0xFFFF9F1C).copy(alpha = 0.1f) else Color(0xFF2B2D31),
                                RoundedCornerShape(10.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) Color(0xFFFF9F1C) else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                viewModel.selectTown(town)
                                onClose()
                            }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(town.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Radial Distance: ${town.distanceKm} km", color = Color.Gray, fontSize = 11.sp)
                        }

                        // Badges
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (town.isUberEatsCovered) {
                                Box(modifier = Modifier.background(Color(0xFF2EC4B6).copy(alpha = 0.2f), RoundedCornerShape(4.dp)).padding(4.dp)) {
                                    Text("Uber", color = Color(0xFF2EC4B6), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (town.isPickMeEatsCovered) {
                                Box(modifier = Modifier.background(Color(0xFFFF9F1C).copy(alpha = 0.2f), RoundedCornerShape(4.dp)).padding(4.dp)) {
                                    Text("PickMe", color = Color(0xFFFF9F1C), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (!town.isUberEatsCovered && !town.isPickMeEatsCovered) {
                                Box(modifier = Modifier.background(Color(0xFFE71D36).copy(alpha = 0.2f), RoundedCornerShape(4.dp)).padding(4.dp)) {
                                    Text("Takeaway Only", color = Color(0xFFE71D36), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// CHECKOUT & PAYMENT INVOICING POPUP
@Composable
fun CustomerCheckoutView(
    viewModel: RestaurantViewModel,
    lang: String,
    onClose: () -> Unit
) {
    val selectedTown by viewModel.selectedTown.collectAsState()
    val isDeliveryAllowed by viewModel.isDeliveryAllowed.collectAsState()
    val deliveryMethod by viewModel.deliveryMethod.collectAsState()
    val cartList by viewModel.cartList.collectAsState()

    var nameVal by remember { mutableStateOf("Sajith Rajapakse") }
    var phoneVal by remember { mutableStateOf("0772458931") }
    var addressVal by remember { mutableStateOf("House 35, Colombo Inner Road, " + selectedTown.name) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Secure Checkout Interface", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.White)
            Spacer(modifier = Modifier.width(48.dp))
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Address details
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2023)), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("1. Customer Profile Details", color = Color(0xFFFF9F1C), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = nameVal,
                            onValueChange = { nameVal = it },
                            label = { Text("Your Registered Name") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2EC4B6))
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = phoneVal,
                            onValueChange = { phoneVal = it },
                            label = { Text("Contact Phone (for Notify.lk SMS updates)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2EC4B6))
                        )
                    }
                }
            }

            // Delivery method & geofenced status
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2023)), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("2. Dispatch Logistics Mode", color = Color(0xFFFF9F1C), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            val pickupSelected = deliveryMethod == "TAKEAWAY"
                            Button(
                                onClick = { viewModel.setDeliveryMethod("TAKEAWAY") },
                                colors = ButtonDefaults.buttonColors(containerColor = if (pickupSelected) Color(0xFF2EC4B6) else Color(0xFF2B2D31)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Self-Takeaway/Pickup", color = if (pickupSelected) Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.setDeliveryMethod("DELIVERY") },
                                colors = ButtonDefaults.buttonColors(containerColor = if (!pickupSelected) Color(0xFF2EC4B6) else Color(0xFF2B2D31)),
                                enabled = isDeliveryAllowed,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Home Delivery", color = if (!pickupSelected) Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (!isDeliveryAllowed) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE71D36).copy(alpha = 0.12f)),
                                modifier = Modifier.padding(top = 10.dp)
                            ) {
                                Text(
                                    text = viewModel.tr("coverage_warning", lang),
                                    color = Color(0xFFE71D36),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.padding(10.dp),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            if (deliveryMethod == "DELIVERY") {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = addressVal,
                                    onValueChange = { addressVal = it },
                                    label = { Text("Delivery Destination Address") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2EC4B6))
                                )
                            }
                        }
                    }
                }
            }

            // Payment Gateways
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2023)), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(viewModel.tr("payment_title", lang), color = Color(0xFFFF9F1C), fontWeight = FontWeight.Bold)
                        Text(
                            text = viewModel.tr("payment_sim_desc", lang),
                            color = Color.LightGray.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // PayHere Gateway Credit Card Simulation button
                        Button(
                            onClick = {
                                viewModel.placeOrder(nameVal, phoneVal, addressVal, "Card / PayHere")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9F1C)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("payhere_card_button")
                        ) {
                            Text(viewModel.tr("btn_pay_card", lang), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // COD payment button
                        Button(
                            onClick = {
                                viewModel.placeOrder(nameVal, phoneVal, addressVal, "Cash on Delivery (COD)")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2EC4B6)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("payhere_cod_button")
                        ) {
                            Text(viewModel.tr("btn_pay_cod", lang), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// 3. SECURE CUSTOMER ORDER STATUS LIVE TRACKER VIEW
@Composable
fun CustomerOrderTrackerView(
    viewModel: RestaurantViewModel,
    lang: String,
    onBackToHome: () -> Unit
) {
    val order by viewModel.activeTrackedOrder.collectAsState()
    val orderLog = order ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
            Text(
                text = viewModel.tr("order_tracking", lang),
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                color = Color(0xFF2EC4B6)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Order Header Specifications
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2023)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "${viewModel.tr("order_id_lbl", lang)}: ${orderLog.orderId}",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text("Recipient Name: ${orderLog.customerName}", color = Color.LightGray, fontSize = 13.sp)
                    Text("Payment System: ${orderLog.paymentMethod} (${orderLog.paymentStatus})", color = Color.LightGray, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Items Summary:\n${orderLog.itemsSummary}", color = Color(0xFFFF9F1C), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Total Paid: Rs. ${orderLog.totalAmount}", color = Color.White, fontWeight = FontWeight.Black)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Visual Process Progress Steps mapping statuses: PENDING, PREPARING, OUT_FOR_DELIVERY, COMPLETED
            val orderStatus = orderLog.orderStatus
            val stepIndex = when (orderStatus) {
                "PENDING" -> 1
                "PREPARING" -> 2
                "OUT_FOR_DELIVERY" -> 3
                "COMPLETED" -> 4
                else -> 1
            }

            // Steps layout
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                listOf(
                    Pair(1, viewModel.tr("order_placed", lang)),
                    Pair(2, viewModel.tr("order_preparing", lang)),
                    Pair(3, viewModel.tr("order_dispatched", lang)),
                    Pair(4, viewModel.tr("order_completed", lang))
                ).forEach { (step, text) ->
                    val active = indexMatches(stepIndex, step)
                    val done = stepIndex > step

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (active) Color(0xFF2EC4B6).copy(alpha = 0.1f) else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    when {
                                        active -> Color(0xFFFF9F1C)
                                        done -> Color(0xFF2EC4B6)
                                        else -> Color.DarkGray
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (done) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                            } else {
                                Text(step.toString(), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = text,
                            color = if (active) Color.White else Color.Gray,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                            fontSize = if (active) 15.sp else 13.sp
                        )
                    }
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            // Brand watermark on active checkout completion
            Text(
                text = "Created by Seneth Perera — Secure Client Engine",
                color = Color.DarkGray,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )

            Button(
                onClick = onBackToHome,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B2D31)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(viewModel.tr("btn_back", lang), color = Color.White)
            }
        }
    }
}

fun indexMatches(currentIndex: Int, step: Int): Boolean {
    return currentIndex == step
}

// ==========================================
// 4. RESTAURANT STAFF LOGIN GATEWAY
// ==========================================
@Composable
fun StaffLoginView(viewModel: RestaurantViewModel, lang: String) {
    var username by remember { mutableStateOf("seneth") }
    var password by remember { mutableStateOf("1234") }
    var groupId by remember { mutableStateOf("CB-COLOMBO-1") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = { viewModel.navigateTo(UserRole.SPLASH) },
            modifier = Modifier.align(Alignment.Start)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = Color(0xFFFF9F1C),
                modifier = Modifier.size(56.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = viewModel.tr("login_title", lang),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Text(
                text = viewModel.tr("login_subtitle", lang),
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Text Outputs
            OutlinedTextField(
                value = groupId,
                onValueChange = { groupId = it },
                label = { Text(viewModel.tr("lbl_restaurant_id", lang)) },
                modifier = Modifier.fillMaxWidth()
                    .testTag("login_group_id"),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF9F1C))
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(viewModel.tr("lbl_username", lang)) },
                modifier = Modifier.fillMaxWidth()
                    .testTag("login_username"),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF9F1C))
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(viewModel.tr("lbl_password", lang)) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
                    .testTag("login_password"),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF9F1C))
            )

            Spacer(modifier = Modifier.height(20.dp))

            val context = LocalContext.current
            Button(
                onClick = {
                    if (username.lowercase() == "seneth" && password == "1234" && groupId.isNotEmpty()) {
                        viewModel.navigateTo(UserRole.STAFF_DASHBOARD)
                    } else {
                        Toast.makeText(context, viewModel.tr("err_login", lang), Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("login_submit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9F1C))
            ) {
                Text(viewModel.tr("btn_login", lang), color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        Text(
            text = "Attributed development portal for: Seneth Perera (Administrator)",
            color = Color.DarkGray,
            fontSize = 9.sp,
            textAlign = TextAlign.Center
        )
    }
}

// ==========================================
// 5. RESTAURANT MANAGEMENT DASHBOARD (KDS/POS/INVENTORY)
// ==========================================
@Composable
fun StaffDashboardView(viewModel: RestaurantViewModel, lang: String) {
    var activeSubTab by remember { mutableIntStateOf(0) } // 0: KDS TICKETS, 1: POS MENU, 2: INVENTORY STOCK CONTROL, 3: STATS & LOGS

    val isStoreOpen by viewModel.isStoreOpen.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Nav bar details
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2023)),
            shape = RoundedCornerShape(0.dp)
        ) {
            Column(modifier = Modifier.padding(top = 10.dp, start = 16.dp, end = 16.dp, bottom = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.navigateTo(UserRole.SPLASH) }) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Exit", tint = Color.LightGray)
                        }
                        Text(
                            text = "Owner Dashboard",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }

                    // Online Store open checkbox
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isStoreOpen) "Store OPEN" else "Store CLOSED",
                            color = if (isStoreOpen) Color(0xFF2EC4B6) else Color(0xFFE71D36),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Switch(
                            checked = isStoreOpen,
                            onCheckedChange = { viewModel.setStoreStatus(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF2EC4B6))
                        )
                    }
                }

                // Sub Category Tabs Row
                ScrollableTabRow(
                    selectedTabIndex = activeSubTab,
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFFFF9F1C),
                    edgePadding = 0.dp
                ) {
                    Tab(
                        selected = activeSubTab == 0,
                        onClick = { activeSubTab = 0 },
                        text = { Text("Kitchen KDS", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = activeSubTab == 1,
                        onClick = { activeSubTab = 1 },
                        text = { Text("POS Menu", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = activeSubTab == 2,
                        onClick = { activeSubTab = 2 },
                        text = { Text("Inventory", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = activeSubTab == 3,
                        onClick = { activeSubTab = 3 },
                        text = { Text("Thermal & SMS Logs", fontSize = 12.sp) }
                    )
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (activeSubTab) {
                0 -> StaffKdsTicketsPane(viewModel, lang)
                1 -> StaffPosMenuCreatorPane(viewModel, lang)
                2 -> StaffLiveInventoryStockPane(viewModel, lang)
                3 -> StaffThermalSmsLogsPane(viewModel, lang)
            }
        }
    }
}

// 5A. KITCHEN DISPLAY PANE (KDS)
@Composable
fun StaffKdsTicketsPane(viewModel: RestaurantViewModel, lang: String) {
    val activeKdsOrders by viewModel.activeKdsOrders.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = viewModel.tr("live_active_tickets", lang),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Webhook aggregations trigger
            Button(
                onClick = {
                    viewModel.injectSimulatedAggregatedOrder(listOf("Uber Eats", "PickMe Eats").random())
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B2D31)),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Text(viewModel.tr("sim_aggregator_order", lang), fontSize = 9.sp, color = Color.White)
            }
        }

        if (activeKdsOrders.isEmpty()) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text("All tables cleared! No pending orders in cooking token list.", color = Color.Gray, fontSize = 12.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(activeKdsOrders) { order ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (order.isAggregator) Color(0xFFFF9F1C).copy(alpha = 0.05f) else Color(0xFF1F2023)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (order.isAggregator) Color(0xFFFF9F1C).copy(alpha = 0.4f) else Color.Transparent,
                                RoundedCornerShape(14.dp)
                            )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Ticket: ${order.orderId}",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Cust: ${order.customerName} | Locality: ${order.deliveryAddress}",
                                        fontSize = 11.sp,
                                        color = Color.LightGray
                                    )
                                }

                                // Aggregation badge
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (order.isAggregator) Color(0xFFFF9F1C) else Color(0xFF2EC4B6),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = order.deliveryService,
                                        color = Color.Black,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color.DarkGray)

                            // Items description list
                            Text(
                                text = order.itemsSummary,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total LKR: Rs. ${order.totalAmount}",
                                    color = Color(0xFFFF9F1C),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Printer cooking tokens
                                    IconButton(
                                        onClick = { viewModel.triggerManualPrint(order, "KITCHEN") },
                                        modifier = Modifier.background(Color(0xFF2B2D31), RoundedCornerShape(8.dp))
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = "Print receipt", tint = Color.White, modifier = Modifier.size(16.dp))
                                    }

                                    // Next status tracker button
                                    Button(
                                        onClick = { viewModel.advanceOrderStatus(order.orderId, order.orderStatus) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2EC4B6)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text(
                                            text = when (order.orderStatus) {
                                                "PENDING" -> "Accept & Prepare"
                                                "PREPARING" -> "Cooked & Dispatch"
                                                "OUT_FOR_DELIVERY" -> "Complete Order"
                                                else -> "Finish"
                                            },
                                            color = Color.Black,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
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

// 5B. POS MENU CREATOR WITH AI FOOD IMAGERY (GEMINI DIRECT REST)
@Composable
fun StaffPosMenuCreatorPane(viewModel: RestaurantViewModel, lang: String) {
    val isAiGenerating by viewModel.isAiGenerating.collectAsState()
    val listItems by viewModel.menuItemsList.collectAsState()

    var customDishName by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = viewModel.tr("pos_manager", lang),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Text field entry
        OutlinedTextField(
            value = customDishName,
            onValueChange = { customDishName = it },
            label = { Text(viewModel.tr("dish_name_input", lang)) },
            modifier = Modifier.fillMaxWidth().testTag("pos_dish_name_input"),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF9F1C)),
            trailingIcon = {
                if (customDishName.isNotEmpty()) {
                    IconButton(onClick = { customDishName = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = null, tint = Color.Gray)
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (isAiGenerating) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9F1C).copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(color = Color(0xFFFF9F1C), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(viewModel.tr("ai_generating", lang), color = Color(0xFFFF9F1C), fontSize = 12.sp)
                }
            }
        } else {
            Button(
                onClick = {
                    if (customDishName.trim().isNotEmpty()) {
                        viewModel.submitMenuItemWithAiImage(customDishName)
                        customDishName = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9F1C)),
                modifier = Modifier.fillMaxWidth().height(48.dp)
                    .testTag("pos_submit_dish_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(viewModel.tr("ai_auto_complete", lang), color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Small List of Dishes added
        Text(
            text = "Total Active Dishes (${listItems.size})",
            fontSize = 13.sp,
            color = Color.LightGray,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(listItems) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1F2023), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        AsyncImage(
                            model = item.imageUrl,
                            contentDescription = item.nameEn,
                            modifier = Modifier.size(42.dp).clip(RoundedCornerShape(8.dp)).background(Color.DarkGray),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(item.nameEn, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(item.category, color = Color.Gray, fontSize = 11.sp)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Rs. ${item.basePrice}", color = Color(0xFFFF9F1C), fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp))
                        IconButton(onClick = { viewModel.deleteItem(item.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE71D36), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

// 5C. LIVE INVENTORY STOCK CONTEXT TOGGLE PANE (KDS <=> CUSTOMER <=> AGGREGATORS SYNCHRONIZED)
@Composable
fun StaffLiveInventoryStockPane(viewModel: RestaurantViewModel, lang: String) {
    val listItems by viewModel.menuItemsList.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Live Sync Stock levels",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Toggling active stock switches instantly updates Uber Eats, PickMe Eats, customer basket checkouts and POS menu options alike.",
            color = Color.Gray,
            fontSize = 11.sp,
            lineHeight = 15.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
            items(listItems) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1F2023), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        AsyncImage(
                            model = item.imageUrl,
                            contentDescription = item.nameEn,
                            modifier = Modifier.size(45.dp).clip(RoundedCornerShape(8.dp)).background(Color.DarkGray),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(item.nameEn, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (item.isAvailable) "Instock & Synced Online" else "OUT OF STOCK — Locked",
                                color = if (item.isAvailable) Color(0xFF2EC4B6) else Color(0xFFE71D36),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }

                    Switch(
                        checked = item.isAvailable,
                        onCheckedChange = { viewModel.toggleItemInStock(item.id, it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF2EC4B6))
                    )
                }
            }
        }
    }
}

// 5D. HARDWARE RECEIPTS & TRANSACTIONAL SMS SIMULATION LOG TERMINAL
@Composable
fun StaffThermalSmsLogsPane(viewModel: RestaurantViewModel, lang: String) {
    val printerLogs by viewModel.printerLogs.collectAsState()
    val smsLogs by viewModel.smsLogs.collectAsState()

    var activeSubLogsTab by remember { mutableStateOf("PRINTER") } // "PRINTER" or "SMS"

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Toggle logs channel
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { activeSubLogsTab = "PRINTER" },
                colors = ButtonDefaults.buttonColors(containerColor = if (activeSubLogsTab == "PRINTER") Color(0xFFFF9F1C) else Color(0xFF2B2D31)),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Epson Thermal Logs", color = if (activeSubLogsTab == "PRINTER") Color.Black else Color.White)
            }

            Button(
                onClick = { activeSubLogsTab = "SMS" },
                colors = ButtonDefaults.buttonColors(containerColor = if (activeSubLogsTab == "SMS") Color(0xFFFF9F1C) else Color(0xFF2B2D31)),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Notify.lk SMS Alerts", color = if (activeSubLogsTab == "SMS") Color.Black else Color.White)
            }
        }

        if (activeSubLogsTab == "PRINTER") {
            // Bluetooth receipts
            Text(
                text = "Simulated Bluetooth EPSON ESC/POS 80mm printer output streams",
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (printerLogs.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No receipts triggered yet.", color = Color.DarkGray, fontSize = 12.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(printerLogs) { log ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black, RoundedCornerShape(10.dp))
                                .border(1.dp, Color.DarkGray, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = log,
                                color = Color.Green,
                                fontSize = 10.sp,
                                style = LocalTextStyle.current.copy(lineHeight = 14.sp)
                            )
                        }
                    }
                }
            }
        } else {
            // Notify.lk alerts SMS
            Text(
                text = "Simulated Notify.lk Sri Lanka Transactional SMS dispatch streams",
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (smsLogs.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No transactional SMS logged yet.", color = Color.DarkGray, fontSize = 12.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(smsLogs) { log ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1F2023), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF2EC4B6))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(log, color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
