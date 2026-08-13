package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.GearEntity
import com.example.data.db.PlayerProfileEntity
import com.example.data.model.GearType
import com.example.data.model.Rarity
import com.example.ui.theme.*
import com.example.util.SoundManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class StoreCategory {
    ALL, BUNDLES, WEAPONS, PASSES
}

sealed class StoreItem(
    val id: String,
    val title: String,
    val description: String,
    val price: String,
    val category: StoreCategory,
    val icon: ImageVector,
    val rarity: Rarity = Rarity.COMMON,
    val badgeTag: String? = null
) {
    class CreditBundle(
        id: String,
        title: String,
        description: String,
        price: String,
        val creditsAmount: Int,
        val dataAmount: Int,
        icon: ImageVector,
        badgeTag: String? = null
    ) : StoreItem(id, title, description, price, StoreCategory.BUNDLES, icon, Rarity.RARE, badgeTag)

    class DirectWeapon(
        id: String,
        title: String,
        description: String,
        price: String,
        val gearItem: GearEntity,
        icon: ImageVector,
        rarity: Rarity,
        badgeTag: String? = null
    ) : StoreItem(id, title, description, price, StoreCategory.WEAPONS, icon, rarity, badgeTag)

    class VipPass(
        id: String,
        title: String,
        description: String,
        price: String,
        val newBadgeRank: String,
        val bonusCredits: Int,
        val bonusData: Int,
        icon: ImageVector,
        badgeTag: String? = null
    ) : StoreItem(id, title, description, price, StoreCategory.PASSES, icon, Rarity.LEGENDARY, badgeTag)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(
    playerProfile: PlayerProfileEntity?,
    onBuyBundle: (Int, Int, () -> Unit) -> Unit,
    onBuyWeapon: (GearEntity, () -> Unit) -> Unit,
    onBuyVipPass: (String, Int, Int, () -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(StoreCategory.ALL) }
    var selectedItemForPurchase by remember { mutableStateOf<StoreItem?>(null) }
    var purchasedItemReceipt by remember { mutableStateOf<StoreItem?>(null) }

    val coroutineScope = rememberCoroutineScope()

    val catalogItems = remember {
        listOf(
            // Cryptokey & Credit Bundles
            StoreItem.CreditBundle(
                id = "pack_starter",
                title = "Cryptokey Starter Pack",
                description = "+1,200 Cryptokeys & +300 DNI Data",
                price = "$0.99",
                creditsAmount = 1200,
                dataAmount = 300,
                icon = Icons.Default.MonetizationOn,
                badgeTag = "BEST VALUE"
            ),
            StoreItem.CreditBundle(
                id = "pack_veteran",
                title = "Blackjack Supply Drop Cache",
                description = "+4,500 Cryptokeys & +1,200 DNI Data",
                price = "$2.99",
                creditsAmount = 4500,
                dataAmount = 1200,
                icon = Icons.Default.AccountBalance,
                badgeTag = "POPULAR"
            ),
            StoreItem.CreditBundle(
                id = "pack_sovereign",
                title = "Black Market Mega Vault",
                description = "+15,000 Cryptokeys & +4,500 DNI Data",
                price = "$9.99",
                creditsAmount = 15000,
                dataAmount = 4500,
                icon = Icons.Default.Paid,
                badgeTag = "MEGA DEAL"
            ),

            // Exclusive Weapons & Gear
            StoreItem.DirectWeapon(
                id = "weapon_scythe",
                title = "Mastercraft Scythe Minigun",
                description = "Legendary Reaper Weapon. +65 ATK, +25% Crit Damage",
                price = "$2.99",
                gearItem = GearEntity(
                    id = "weapon_scythe_${System.currentTimeMillis()}",
                    name = "Mastercraft Scythe Minigun",
                    type = GearType.WEAPON,
                    rarity = Rarity.LEGENDARY,
                    attackBonus = 65,
                    critBonus = 25
                ),
                icon = Icons.Default.FlashOn,
                rarity = Rarity.LEGENDARY,
                badgeTag = "LEGENDARY"
            ),
            StoreItem.DirectWeapon(
                id = "weapon_exosuit",
                title = "DNI Kinetic Overdrive Armor",
                description = "Legendary Battery Armor. +60 DEF, +220 HP",
                price = "$3.99",
                gearItem = GearEntity(
                    id = "armor_dreadnought_${System.currentTimeMillis()}",
                    name = "DNI Kinetic Overdrive Armor",
                    type = GearType.ARMOR,
                    rarity = Rarity.LEGENDARY,
                    defenseBonus = 60,
                    hpBonus = 220
                ),
                icon = Icons.Default.Shield,
                rarity = Rarity.LEGENDARY,
                badgeTag = "LEGENDARY"
            ),
            StoreItem.DirectWeapon(
                id = "weapon_chip",
                title = "Glitch DNI Core v5",
                description = "Epic Prophet Cyber Chip. +40 ATK, +120 HP, +15% Crit",
                price = "$1.99",
                gearItem = GearEntity(
                    id = "chip_quantum_${System.currentTimeMillis()}",
                    name = "Glitch DNI Core v5",
                    type = GearType.CIPHER_CHIP,
                    rarity = Rarity.EPIC,
                    attackBonus = 40,
                    hpBonus = 120,
                    critBonus = 15
                ),
                icon = Icons.Default.Memory,
                rarity = Rarity.EPIC,
                badgeTag = "HOT"
            ),

            // Passes & Upgrades
            StoreItem.VipPass(
                id = "pass_sovereign",
                title = "Blackjack Master Prestige Pass",
                description = "Unlocks 'Oistars Ops Master Prestige' Title, +6,000 Cryptokeys, +2,500 DNI Data & Permanent XP Boost",
                price = "$4.99",
                newBadgeRank = "Oistars Ops Master Prestige",
                bonusCredits = 6000,
                bonusData = 2500,
                icon = Icons.Default.WorkspacePremium,
                badgeTag = "VIP EXCLUSIVE"
            )
        )
    }

    val filteredItems = remember(selectedCategory) {
        if (selectedCategory == StoreCategory.ALL) catalogItems
        else catalogItems.filter { it.category == selectedCategory }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .testTag("cyber_store_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header HUD Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CyberPrimary)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = CyberPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "BLACK MARKET & CRYPTOKEYS",
                                    color = CyberPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "BLACKJACK SUPPLY DROPS & MASTERCRAFTS",
                                    color = CyberSubtext,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            color = CyberSecondary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, CyberSecondary)
                        ) {
                            Text(
                                text = playerProfile?.badgeRank ?: "SOVEREIGN OI",
                                color = CyberSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Balance Display Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ResourceChip(
                            icon = Icons.Default.MonetizationOn,
                            label = "Credits",
                            value = "${playerProfile?.credits ?: 0}",
                            color = CyberYellow,
                            modifier = Modifier.weight(1f)
                        )

                        ResourceChip(
                            icon = Icons.Default.Terminal,
                            label = "Tactical Data",
                            value = "${playerProfile?.tacticalData ?: 0}",
                            color = CyberPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CategoryTabButton(
                    title = "ALL",
                    isSelected = selectedCategory == StoreCategory.ALL,
                    onClick = {
                        SoundManager.playClickSound()
                        selectedCategory = StoreCategory.ALL
                    },
                    modifier = Modifier.weight(1f)
                )
                CategoryTabButton(
                    title = "VAULTS",
                    isSelected = selectedCategory == StoreCategory.BUNDLES,
                    onClick = {
                        SoundManager.playClickSound()
                        selectedCategory = StoreCategory.BUNDLES
                    },
                    modifier = Modifier.weight(1f)
                )
                CategoryTabButton(
                    title = "WEAPONS",
                    isSelected = selectedCategory == StoreCategory.WEAPONS,
                    onClick = {
                        SoundManager.playClickSound()
                        selectedCategory = StoreCategory.WEAPONS
                    },
                    modifier = Modifier.weight(1f)
                )
                CategoryTabButton(
                    title = "VIP PASSES",
                    isSelected = selectedCategory == StoreCategory.PASSES,
                    onClick = {
                        SoundManager.playClickSound()
                        selectedCategory = StoreCategory.PASSES
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Catalog Grid
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredItems) { item ->
                    StoreItemCard(
                        item = item,
                        onSelectPurchase = {
                            SoundManager.playClickSound()
                            selectedItemForPurchase = item
                        }
                    )
                }
            }
        }

        // Purchase Confirmation Terminal Dialog
        selectedItemForPurchase?.let { item ->
            PaymentTerminalDialog(
                item = item,
                onDismiss = { selectedItemForPurchase = null },
                onConfirmPayment = {
                    coroutineScope.launch {
                        when (item) {
                            is StoreItem.CreditBundle -> {
                                onBuyBundle(item.creditsAmount, item.dataAmount) {
                                    SoundManager.playVictorySound()
                                    purchasedItemReceipt = item
                                    selectedItemForPurchase = null
                                }
                            }
                            is StoreItem.DirectWeapon -> {
                                onBuyWeapon(item.gearItem) {
                                    SoundManager.playShieldHealSound()
                                    purchasedItemReceipt = item
                                    selectedItemForPurchase = null
                                }
                            }
                            is StoreItem.VipPass -> {
                                onBuyVipPass(item.newBadgeRank, item.bonusCredits, item.bonusData) {
                                    SoundManager.playVictorySound()
                                    purchasedItemReceipt = item
                                    selectedItemForPurchase = null
                                }
                            }
                        }
                    }
                }
            )
        }

        // Receipt Modal
        purchasedItemReceipt?.let { item ->
            PurchaseReceiptDialog(
                item = item,
                onDismiss = { purchasedItemReceipt = null }
            )
        }
    }
}

@Composable
private fun ResourceChip(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = CyberSurfaceVariant,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(text = label, color = CyberSubtext, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(text = value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun CategoryTabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(34.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) CyberPrimary else CyberSurface
        ),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (isSelected) CyberPrimary else CyberBorder),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
    ) {
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else CyberSubtext
        )
    }
}

@Composable
private fun StoreItemCard(
    item: StoreItem,
    onSelectPurchase: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("store_item_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, item.rarity.color.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(item.rarity.color.copy(alpha = 0.2f))
                    .border(1.dp, item.rarity.color, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.rarity.color,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.title,
                        color = CyberOnSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    item.badgeTag?.let { tag ->
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = item.rarity.color,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = tag,
                                color = Color.Black,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.description,
                    color = CyberSubtext,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onSelectPurchase,
                colors = ButtonDefaults.buttonColors(containerColor = CyberGreen),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("buy_button_${item.id}")
            ) {
                Text(
                    text = item.price,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun PaymentTerminalDialog(
    item: StoreItem,
    onDismiss: () -> Unit,
    onConfirmPayment: () -> Unit
) {
    var isProcessing by remember { mutableStateOf(false) }
    var selectedPaymentMethod by remember { mutableStateOf("Google Play In-App Billing") }

    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        confirmButton = {
            Button(
                onClick = {
                    isProcessing = true
                },
                enabled = !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = CyberGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("confirm_payment_button")
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AUTHORIZING TRANSACTION...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PAY ${item.price} NOW", fontSize = 13.sp, fontWeight = FontWeight.Black)
                }
            }

            LaunchedEffect(isProcessing) {
                if (isProcessing) {
                    delay(1200)
                    onConfirmPayment()
                }
            }
        },
        dismissButton = {
            if (!isProcessing) {
                TextButton(onClick = onDismiss) {
                    Text("CANCEL", color = CyberSubtext, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CreditCard, contentDescription = null, tint = CyberPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CYBER PAYMENT GATEWAY",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = CyberPrimary
                )
            }
        },
        text = {
            Column {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
                    border = BorderStroke(1.dp, CyberBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "ITEM ORDER", fontSize = 10.sp, color = CyberSubtext, fontWeight = FontWeight.Bold)
                        Text(text = item.title, fontSize = 14.sp, color = CyberOnSurface, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = item.description, fontSize = 11.sp, color = CyberSubtext)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "TOTAL PRICE:", fontSize = 11.sp, color = CyberOnSurface, fontWeight = FontWeight.Bold)
                            Text(text = item.price, fontSize = 14.sp, color = CyberGreen, fontWeight = FontWeight.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "SELECT PAYMENT METHOD", fontSize = 10.sp, color = CyberSubtext, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                listOf("Google Play In-App Billing", "Cyber-Crypto Wallet", "Oi Sovereign Black Card").forEach { method ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPaymentMethod = method }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPaymentMethod == method,
                            onClick = { selectedPaymentMethod = method },
                            colors = RadioButtonDefaults.colors(selectedColor = CyberPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = method, fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        },
        containerColor = CyberSurface,
        shape = RoundedCornerShape(14.dp)
    )
}

@Composable
private fun PurchaseReceiptDialog(
    item: StoreItem,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("close_receipt_button")
            ) {
                Text("EQUIP & RETURN TO HUB", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = CyberGreen,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "PURCHASE SUCCESSFUL!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = CyberGreen
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Ref: OI-TX-${(100000..999999).random()}",
                    fontSize = 11.sp,
                    color = CyberSubtext,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Your items have been delivered directly to 'The Oi' squad inventory and player profile matrix.",
                    fontSize = 12.sp,
                    color = CyberOnSurface,
                    textAlign = TextAlign.Center
                )
            }
        },
        containerColor = CyberSurface,
        shape = RoundedCornerShape(14.dp)
    )
}
