package com.example.tva0001

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import com.example.tva0001.ui.theme.FocusRing
import com.example.tva0001.ui.theme.GlassEdge
import com.example.tva0001.ui.theme.GlassFill
import com.example.tva0001.ui.theme.TextBright
import com.example.tva0001.ui.theme.TextFaint
import com.example.tva0001.ui.theme.TextSoft
import com.example.tva0001.ui.theme.TileDark
import com.example.tva0001.ui.theme.TileTealText
import com.example.tva0001.ui.theme.WallpaperDeep
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
    if (LocalInspectionMode.current) {
        // Android Studio's Preview can't show the 4K layer.
        Box(Modifier.fillMaxSize().background(WallpaperDeep))
    } else {
        AndroidView(factory = ::SunsetWallpaperView, modifier = Modifier.fillMaxSize())
    }
    // Darkens the top and bottom so text and tiles stay readable on any picture.
    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                0f to Color.Black.copy(alpha = 0.35f),
                0.3f to Color.Transparent,
                0.5f to Color.Transparent,
                1f to Color.Black.copy(alpha = 0.75f),
            )
        )
    )
}

@Composable
private fun HomeContent(onAllApps: () -> Unit) {
    val context = LocalContext.current
    val now = rememberMinuteClock()
    val weather by rememberWeather()
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                BasicText(
                    now.format(clockFormat(context)),
                    style = TextStyle(color = TextSoft, fontSize = 20.sp),
                )
                weather?.let {
                    Spacer(Modifier.width(20.dp))
                    SymbolIcon(it.symbol, 24.dp, TextSoft)
                    Spacer(Modifier.width(6.dp))
                    BasicText("${it.degrees}°", style = TextStyle(color = TextSoft, fontSize = 20.sp))
                }
            }
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
            // Glass: a dark see-through tile that only shows its color when selected.
            .background(if (focused) tile.color.copy(alpha = 0.9f) else GlassFill, TileShape)
            .border(
                if (focused) 3.dp else 1.dp,
                if (focused) FocusRing else GlassEdge,
                TileShape,
            )
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(14.dp),
        contentAlignment = if (big) Alignment.BottomStart else Alignment.Center,
    ) {
        if (big) {
            Column {
                TileIcon(tile, 44.dp)
                Spacer(Modifier.height(12.dp))
                BasicText("Watch", style = TextStyle(color = TileTealText, fontSize = 13.sp))
                BasicText(tile.label, style = TextStyle(color = TextBright, fontSize = 24.sp))
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TileIcon(tile, 28.dp)
                Spacer(Modifier.height(6.dp))
                BasicText(tile.label, style = TextStyle(color = TextBright, fontSize = 16.sp))
            }
        }
    }
}

/** The app's own logo if it's installed, otherwise the tile's white symbol. */
@Composable
private fun TileIcon(tile: Tile, size: Dp) {
    val logo = (tile.action as? TileAction.OpenApp)?.let { rememberAppLogo(it.packageName) }
    if (logo != null) {
        Image(logo, contentDescription = null, modifier = Modifier.size(size))
    } else {
        SymbolIcon(tile.symbol, size, TextBright)
    }
}

@Composable
private fun rememberAppLogo(packageName: String): ImageBitmap? {
    val context = LocalContext.current
    return remember(packageName) {
        try {
            context.packageManager.getApplicationIcon(packageName).toBitmap(128, 128).asImageBitmap()
        } catch (e: PackageManager.NameNotFoundException) {
            null
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
                    modifier = Modifier.height(110.dp).then(if (first) Modifier.focusRequester(firstFocus) else Modifier),
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
