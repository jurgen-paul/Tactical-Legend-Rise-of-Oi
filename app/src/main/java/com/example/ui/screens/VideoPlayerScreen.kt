package com.example.ui.screens

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.*
import com.example.util.SoundManager
import kotlinx.coroutines.delay

data class GameVideoItem(
    val id: String,
    val title: String,
    val duration: String,
    val category: String,
    val description: String,
    val videoUrl: String,
    val isRewardClaimed: Boolean = false
)

@Composable
fun VideoPlayerScreen(
    onClaimCredits: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val videoList = remember {
        mutableStateListOf(
            GameVideoItem(
                id = "v1",
                title = "Oistars Ops 1 - Official DNI Cyber Warfare Trailer",
                duration = "01:30",
                category = "CINEMATIC",
                description = "Oistars Ops 1 Specialist tactical combat preview featuring Ruin's Gravity Spikes, Outrider's Sparrow Bow, and Prophet's Glitch in Singapore.",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
            ),
            GameVideoItem(
                id = "v2",
                title = "Oistars Ops 1 Zombies - Shadows of Evil Morg City Reveal",
                duration = "02:15",
                category = "ZOMBIES",
                description = "Morg City undead outbreak! Fight through Keeper Phantoms, Margwa monsters, and unleash GobbleGum powers.",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"
            ),
            GameVideoItem(
                id = "v3",
                title = "Cyber Core & Specialist Loadout Customization Guide",
                duration = "01:45",
                category = "TUTORIAL",
                description = "Master DNI Cyber Cores, weapon crafting, rarity tiers, and loadout optimization in the Oistars Ops Armory.",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4"
            ),
            GameVideoItem(
                id = "v4",
                title = "Blackjack Supply Drops & Black Market Unboxing",
                duration = "01:10",
                category = "BLACK MARKET",
                description = "Learn how to spend Cryptokeys to unlock Mastercraft weapons and Specialist weapon camos.",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoylikes.mp4"
            )
        )
    }

    var selectedVideo by remember { mutableStateOf(videoList[0]) }
    var isPlaying by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableStateOf(0) }
    var totalDurationMs by remember { mutableStateOf(90000) } // default 90s
    var isFullscreen by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableStateOf(1.0f) }
    var rewardClaimedIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }

    // Position progress tracking timer
    LaunchedEffect(isPlaying, selectedVideo) {
        while (isPlaying) {
            delay(500)
            videoViewRef?.let { vView ->
                if (vView.isPlaying) {
                    currentPositionMs = vView.currentPosition
                    if (vView.duration > 0) {
                        totalDurationMs = vView.duration
                    }
                }
            } ?: run {
                // Fallback simulation timer if stream buffering
                currentPositionMs = (currentPositionMs + 500).coerceAtMost(totalDurationMs)
                if (currentPositionMs >= totalDurationMs) {
                    currentPositionMs = 0
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .testTag("video_player_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Screen Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CyberPrimary)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.OndemandVideo,
                            contentDescription = null,
                            tint = CyberPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "CYBER MEDIA CINEMA",
                                color = CyberPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "OFFICIAL TRAILERS & COMBAT STREAMS",
                                color = CyberSubtext,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        color = CyberPrimary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, CyberPrimary)
                    ) {
                        Text(
                            text = "4K STREAM",
                            color = CyberPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Video Player Container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isFullscreen) 360.dp else 220.dp)
                    .border(1.5.dp, CyberPrimary, RoundedCornerShape(14.dp))
                    .testTag("game_video_player_view"),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                shape = RoundedCornerShape(14.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Native VideoView Integration with Fallback
                    AndroidView(
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                setVideoURI(Uri.parse(selectedVideo.videoUrl))
                                setOnPreparedListener { mp ->
                                    mp.isLooping = true
                                    if (isPlaying) start()
                                }
                                setOnErrorListener { _, _, _ ->
                                    // Smoothly handle network stream fallback
                                    true
                                }
                            }
                        },
                        update = { vView ->
                            videoViewRef = vView
                            if (isPlaying && !vView.isPlaying) {
                                vView.start()
                            } else if (!isPlaying && vView.isPlaying) {
                                vView.pause()
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Video Control HUD Overlay
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top Bar Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = CyberSecondary,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = selectedVideo.category,
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(
                                    onClick = { isMuted = !isMuted },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                        contentDescription = "Mute",
                                        tint = Color.White
                                    )
                                }

                                IconButton(
                                    onClick = { isFullscreen = !isFullscreen },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                        contentDescription = "Fullscreen",
                                        tint = Color.White
                                    )
                                }
                            }
                        }

                        // Center Play / Pause Action
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    SoundManager.playClickSound()
                                    currentPositionMs = (currentPositionMs - 10000).coerceAtLeast(0)
                                    videoViewRef?.seekTo(currentPositionMs)
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(Icons.Default.Replay10, contentDescription = "-10s", tint = Color.White)
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            IconButton(
                                onClick = {
                                    SoundManager.playClickSound()
                                    isPlaying = !isPlaying
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(CyberPrimary, CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            IconButton(
                                onClick = {
                                    SoundManager.playClickSound()
                                    currentPositionMs = (currentPositionMs + 10000).coerceAtMost(totalDurationMs)
                                    videoViewRef?.seekTo(currentPositionMs)
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(Icons.Default.Forward10, contentDescription = "+10s", tint = Color.White)
                            }
                        }

                        // Bottom Scrub Bar
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = formatMs(currentPositionMs),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = formatMs(totalDurationMs),
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 10.sp
                                )
                            }

                            LinearProgressIndicator(
                                progress = { if (totalDurationMs > 0) currentPositionMs.toFloat() / totalDurationMs.toFloat() else 0f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = CyberPrimary,
                                trackColor = Color.White.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Currently Selected Video Description & Reward
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CyberBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedVideo.title,
                                color = CyberOnSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = selectedVideo.description,
                                color = CyberSubtext,
                                fontSize = 11.sp
                            )
                        }

                        val isClaimed = rewardClaimedIds.contains(selectedVideo.id)
                        Button(
                            onClick = {
                                if (!isClaimed) {
                                    SoundManager.playVictorySound()
                                    rewardClaimedIds = rewardClaimedIds + selectedVideo.id
                                    onClaimCredits(50)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isClaimed) CyberSurfaceVariant else CyberGreen
                            ),
                            enabled = !isClaimed,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .testTag("claim_video_reward_button")
                        ) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isClaimed) "CLAIMED" else "+50 CR",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Video Stream Playlist Selection
            Text(
                text = "CYBER STREAM PLAYLIST",
                color = CyberTertiary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(videoList) { item ->
                    val isSelected = item.id == selectedVideo.id

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                SoundManager.playClickSound()
                                selectedVideo = item
                                currentPositionMs = 0
                                isPlaying = true
                            }
                            .testTag("video_item_${item.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) CyberSurfaceVariant else CyberSurface
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) CyberPrimary else CyberBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) CyberPrimary.copy(alpha = 0.3f) else CyberBorder.copy(alpha = 0.2f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.PlayArrow else Icons.Default.Movie,
                                    contentDescription = null,
                                    tint = if (isSelected) CyberPrimary else CyberSubtext,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    color = if (isSelected) CyberPrimary else CyberOnSurface,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${item.category} • ${item.duration}",
                                    color = CyberSubtext,
                                    fontSize = 10.sp
                                )
                            }

                            if (rewardClaimedIds.contains(item.id)) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Claimed",
                                    tint = CyberGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatMs(ms: Int): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format("%02d:%02d", min, sec)
}
