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
import com.example.tva0001.ui.theme.TileDark
import com.example.tva0001.ui.theme.TileGreen
import com.example.tva0001.ui.theme.TilePurple
import com.example.tva0001.ui.theme.TileTeal

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
    // Shown when there's no app logo to use.
    val symbol: Symbol = Symbol.Play,
)

// The home screen layout. Change labels, colors or order here.
val BigTile = Tile("HDMI 3", TileTeal, TileAction.HdmiInput(3), Symbol.Hdmi)

val TopRow = listOf(
    Tile("YouTube", TileCoral, TileAction.OpenApp("com.google.android.youtube.tv")),
    Tile("Prime Video", TilePurple, TileAction.OpenApp("com.amazon.amazonvideo.livingroom")),
    Tile("Apple TV", TileGreen, TileAction.OpenApp("com.apple.atve.androidtv.appletv")),
)

val BottomRow = listOf(
    Tile("HDMI 2", TileDark, TileAction.HdmiInput(2), Symbol.Hdmi),
    Tile("HDMI 1", TileDark, TileAction.HdmiInput(1), Symbol.Hdmi),
    Tile("All apps", TileDark, TileAction.AllApps, Symbol.Apps),
    Tile("Settings", TileDark, TileAction.Settings, Symbol.Settings),
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
    val tvInputs = context.getSystemService(Context.TV_INPUT_SERVICE) as? TvInputManager
    // Top-level HDMI ports only; devices plugged into a port show up as children.
    val ports = tvInputs?.tvInputList
        ?.filter { it.type == TvInputInfo.TYPE_HDMI && it.parentId == null }
        .orEmpty()
    if (ports.isEmpty()) {
        toast(context, "No HDMI ports found on this TV")
        return
    }
    // Prefer the port whose name mentions the number (e.g. "HDMI 3"),
    // otherwise fall back to the nth port.
    val numberInName = Regex("""\b$number\b""")
    val port = ports.firstOrNull { numberInName.containsMatchIn(it.loadLabel(context).toString()) }
        ?: ports.sortedBy { it.id }.getOrNull(number - 1)
    if (port == null) {
        toast(context, "HDMI $number wasn't found")
        return
    }
    val intent = Intent(Intent.ACTION_VIEW, TvContract.buildChannelUriForPassthroughInput(port.id))
    startSafely(context, intent, "HDMI $number")
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
