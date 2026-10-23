package com.example.tva0001

import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tva0001.ui.theme.FocusRing
import com.example.tva0001.ui.theme.TextBright
import com.example.tva0001.ui.theme.TextFaint
import com.example.tva0001.ui.theme.TextSoft
import com.example.tva0001.ui.theme.TileDark
import com.example.tva0001.ui.theme.TileTealText
import com.example.tva0001.ui.theme.WallpaperDeep
import com.example.tva0001.ui.theme.WallpaperLight
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val TileShape = RoundedCornerShape(12.dp)

@Composable
fun HomeScreen() {
    var showAllApps by remember { mutableStateOf(false) }

    // A home screen never closes on Back; it only closes the "All apps" panel.
    BackHandler { showAllApps = false }

    Box(Modifier.fillMaxSize()) {
        Wallpaper()
        if (showAllApps) {
            AllAppsPanel()
        } else {
            HomeContent(onAllApps = { showAllApps = true })
        }
    }
}

@Composable
private fun Wallpaper() {
    Canvas(Modifier.fillMaxSize()) {
        drawRect(WallpaperDeep)
        val slant = Path().apply {
            moveTo(size.width * 0.55f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height)
            lineTo(size.width * 0.30f, size.height)
            close()
        }
        drawPath(slant, WallpaperLight)
    }
}

@Composable
private fun HomeContent(onAllApps: () -> Unit) {
    val context = LocalContext.current
    val now = rememberMinuteClock()
    val bigTileFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { bigTileFocus.requestFocus() }

    fun press(tile: Tile) = when (val action = tile.action) {
        is TileAction.OpenApp -> openApp(context, action.packageName, tile.label)
        is TileAction.HdmiInput -> openHdmi(context, action.number)
        TileAction.AllApps -> onAllApps()
        TileAction.Settings -> openSettings(context)
    }

    // 48dp / 27dp keeps everything inside the TV's safe area.
    Column(Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 27.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                BasicText(greeting(now.hour), style = TextStyle(color = TextFaint, fontSize = 14.sp))
                BasicText(
                    now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")),
                    style = TextStyle(color = TextBright, fontSize = 32.sp),
                )
            }
            BasicText(
                now.format(clockFormat(context)),
                style = TextStyle(color = TextSoft, fontSize = 20.sp),
            )
        }

        Spacer(Modifier.weight(1f))

        Row(Modifier.fillMaxWidth().height(196.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TileButton(
                tile = BigTile,
                modifier = Modifier.weight(2f).fillMaxHeight().focusRequester(bigTileFocus),
                big = true,
                onClick = { press(BigTile) },
            )
            Column(Modifier.weight(4f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TopRow.forEach { tile ->
                        TileButton(tile, Modifier.weight(1f).fillMaxHeight(), onClick = { press(tile) })
                    }
                }
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BottomRow.forEach { tile ->
                        TileButton(tile, Modifier.weight(1f).fillMaxHeight(), onClick = { press(tile) })
                    }
                }
            }
        }
    }
}

/** A tile that grows and gets a white ring when the remote is on it. */
@Composable
private fun TileButton(
    tile: Tile,
    modifier: Modifier = Modifier,
    big: Boolean = false,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    Box(
        modifier
            .graphicsLayer { scaleX = if (focused) 1.05f else 1f; scaleY = scaleX }
            .border(3.dp, if (focused) FocusRing else Color.Transparent, TileShape)
            .background(tile.color, TileShape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(14.dp),
        contentAlignment = if (big) Alignment.BottomStart else Alignment.Center,
    ) {
        if (big) {
            Column {
                BasicText("Watch", style = TextStyle(color = TileTealText, fontSize = 13.sp))
                BasicText(tile.label, style = TextStyle(color = TextBright, fontSize = 24.sp))
            }
        } else {
            BasicText(tile.label, style = TextStyle(color = TextBright, fontSize = 16.sp))
        }
    }
}

private data class InstalledApp(val label: String, val packageName: String)

@Composable
private fun AllAppsPanel() {
    val context = LocalContext.current
    val apps = remember { installedTvApps(context) }
    val firstFocus = remember { FocusRequester() }
    LaunchedEffect(apps) { if (apps.isNotEmpty()) firstFocus.requestFocus() }

    Column(Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 27.dp)) {
        BasicText("All apps", style = TextStyle(color = TextBright, fontSize = 28.sp))
        BasicText("Press Back to return home", style = TextStyle(color = TextFaint, fontSize = 14.sp))
        Spacer(Modifier.height(16.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            // Room for the focus ring to grow without being cut off.
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(apps) { app ->
                val first = app == apps.first()
                TileButton(
                    tile = Tile(app.label, TileDark, TileAction.OpenApp(app.packageName)),
                    modifier = Modifier.height(80.dp).then(if (first) Modifier.focusRequester(firstFocus) else Modifier),
                    onClick = { openApp(context, app.packageName, app.label) },
                )
            }
        }
    }
}

private fun installedTvApps(context: Context): List<InstalledApp> {
    val pm = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
    return pm.queryIntentActivities(intent, 0)
        .map { InstalledApp(it.loadLabel(pm).toString(), it.activityInfo.packageName) }
        .filter { it.packageName != context.packageName }
        .distinctBy { it.packageName }
        .sortedBy { it.label.lowercase() }
}

/** The current time, refreshed at the start of every minute. */
@Composable
private fun rememberMinuteClock(): LocalDateTime {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay((60 - LocalDateTime.now().second) * 1000L)
            now = LocalDateTime.now()
        }
    }
    return now
}

private fun clockFormat(context: Context): DateTimeFormatter =
    DateTimeFormatter.ofPattern(if (DateFormat.is24HourFormat(context)) "HH:mm" else "h:mm a")

private fun greeting(hour: Int) = when (hour) {
    in 5..11 -> "Good morning"
    in 12..16 -> "Good afternoon"
    else -> "Good evening"
}

@Preview(device = Devices.TV_1080p)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}
