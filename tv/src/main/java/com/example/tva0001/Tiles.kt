package com.example.tva0001

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.tv.TvContract
import android.media.tv.TvInputInfo
import android.media.tv.TvInputManager
import android.provider.Settings
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import com.example.tva0001.ui.theme.TileCoral
import com.example.tva0001.ui.theme.TileCyan
import com.example.tva0001.ui.theme.TileGreen
import com.example.tva0001.ui.theme.TilePink
import com.example.tva0001.ui.theme.TilePurple
import com.example.tva0001.ui.theme.TileSlate
import com.example.tva0001.ui.theme.TileTeal
import com.example.tva0001.ui.theme.TileViolet

/** What happens when a tile is pressed. */
sealed interface TileAction {
    data class OpenApp(val packageName: String) : TileAction
    data class HdmiInput(val number: Int) : TileAction
    data object AllApps : TileAction
    data object Settings : TileAction
}

data class Tile(
    val label: String,
    val color: Color,
    val action: TileAction,
    // The home screen's own icon; without one, the app's logo is shown.
    val icon: SpaceIcon? = null,
    // The small second line. HDMI tiles show what's plugged in instead.
    val subtitle: String = "",
)

// The home screen layout. Change the wallpaper, labels, colors or order here.
val ActiveWallpaper = WallpaperScene.SpaceCowboy

val BigTile = Tile("HDMI 3", TileTeal, TileAction.HdmiInput(3), SpaceIcon.Rocket)

val TopRow = listOf(
    Tile("YouTube", TileCoral, TileAction.OpenApp("com.google.android.youtube.tv"), SpaceIcon.RetroTv, "Videos and music"),
    Tile("Prime Video", TilePurple, TileAction.OpenApp("com.amazon.amazonvideo.livingroom"), SpaceIcon.Comet, "Movies and shows"),
    Tile("Apple TV", TileGreen, TileAction.OpenApp("com.apple.atve.androidtv.appletv"), SpaceIcon.Sparkle, "Apple Originals"),
)

val BottomRow = listOf(
    Tile("HDMI 2", TileViolet, TileAction.HdmiInput(2), SpaceIcon.Planet),
    Tile("HDMI 1", TileCyan, TileAction.HdmiInput(1), SpaceIcon.Crescent),
    Tile("All apps", TilePink, TileAction.AllApps, SpaceIcon.Constellation),
    Tile("Settings", TileSlate, TileAction.Settings, SpaceIcon.GearOrbit, "Picture, sound, Wi-Fi"),
)

/** Opens an installed app, or explains why it can't. */
fun openApp(context: Context, packageName: String, label: String) {
    val pm = context.packageManager
    val intent = pm.getLeanbackLaunchIntentForPackage(packageName)
        ?: pm.getLaunchIntentForPackage(packageName)
    if (intent == null) {
        toast(context, "$label isn't installed on this TV")
        return
    }
    startSafely(context, intent, label)
}

fun openSettings(context: Context) {
    startSafely(context, Intent(Settings.ACTION_SETTINGS), "Settings")
}

/**
 * Switches the TV to an HDMI port. The emulator has no HDMI ports,
 * so this only does something on a real TV.
 */
fun openHdmi(context: Context, number: Int) {
    val ports = hdmiPorts(context)
    if (ports.isEmpty()) {
        toast(context, "No HDMI ports found on this TV")
        return
    }
    val port = findPort(context, ports, number)
    if (port == null) {
        toast(context, "HDMI $number wasn't found")
        return
    }
    val intent = Intent(Intent.ACTION_VIEW, TvContract.buildChannelUriForPassthroughInput(port.id))
    startSafely(context, intent, "HDMI $number")
}

/**
 * What's plugged into an HDMI port, e.g. "PlayStation 5", as the device
 * announces itself to the TV. Null if the TV has no such port.
 */
fun hdmiDeviceName(context: Context, number: Int): String? {
    val all = tvInputList(context)
    val port = findPort(context, hdmiPorts(context), number) ?: return null
    val device = all.firstOrNull { it.parentId == port.id }
    return device?.loadLabel(context)?.toString() ?: "Nothing plugged in"
}

private fun tvInputList(context: Context): List<TvInputInfo> =
    (context.getSystemService(Context.TV_INPUT_SERVICE) as? TvInputManager)?.tvInputList.orEmpty()

// Top-level HDMI ports only; devices plugged into a port show up as children.
private fun hdmiPorts(context: Context): List<TvInputInfo> =
    tvInputList(context).filter { it.type == TvInputInfo.TYPE_HDMI && it.parentId == null }

// Prefer the port whose name mentions the number (e.g. "HDMI 3"),
// otherwise fall back to the nth port.
private fun findPort(context: Context, ports: List<TvInputInfo>, number: Int): TvInputInfo? {
    val numberInName = Regex("""\b$number\b""")
    return ports.firstOrNull { numberInName.containsMatchIn(it.loadLabel(context).toString()) }
        ?: ports.sortedBy { it.id }.getOrNull(number - 1)
}

private fun startSafely(context: Context, intent: Intent, label: String) {
    try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: ActivityNotFoundException) {
        toast(context, "Couldn't open $label")
    } catch (e: SecurityException) {
        toast(context, "Couldn't open $label")
    }
}

private fun toast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
