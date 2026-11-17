package io.github.jqssun.airplay.ui

// Momo's TV redesign (sub-stage 3c): "Ajustes de AirPlay", built for the remote.
// No text field on the page itself: the name is edited in a small dialog that closes with the keyboard's ✓.

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import io.github.jqssun.airplay.Prefs
import io.github.jqssun.airplay.viewmodel.MainViewModel

private val RESOLUTIONS = listOf(Prefs.AUTO to "Automática", "1920x1080" to "1080p", "1280x720" to "720p")
private val FPS = listOf(24, 30, 60)

@Composable
fun TvSettingsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    BackHandler { onBack() }
    val name by viewModel.pref(Prefs.SERVER_NAME).collectAsState()
    val requirePin by viewModel.pref(Prefs.REQUIRE_PIN).collectAsState()
    val allowNew by viewModel.pref(Prefs.ALLOW_NEW_CONN).collectAsState()
    val autoStart by viewModel.pref(Prefs.AUTO_START).collectAsState()
    val bootStart by viewModel.pref(Prefs.BOOT_AUTO_START).collectAsState()
    val launchOnConnect by viewModel.pref(Prefs.LAUNCH_ON_CONNECT).collectAsState()
    val fullscreen by viewModel.pref(Prefs.AUTO_FULLSCREEN).collectAsState()
    val resolution by viewModel.pref(Prefs.RESOLUTION).collectAsState()
    val maxFps by viewModel.pref(Prefs.MAX_FPS).collectAsState()
    var editingName by remember { mutableStateOf(false) }
    val first = remember { FocusRequester() }

    LaunchedEffect(Unit) { first.requestFocusUntilLanded(attempts = 30) }

    Column(modifier = Modifier.fillMaxSize().background(TvStyle.GroundBrush).padding(start = 60.dp, end = 60.dp, top = 36.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(TvStyle.Tile),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Settings, contentDescription = null, tint = TvStyle.Label, modifier = Modifier.size(20.dp))
            }
            Text("Ajustes de AirPlay", color = TvStyle.Label, fontSize = 27.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.02).em)
        }
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(36.dp), modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(bottom = 30.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                TvGroup("Receptor") {
                    TvValueRow("Nombre", name, Modifier.focusRequester(first)) { editingName = true }
                    TvToggleRow("Pedir código", requirePin) { viewModel.set(Prefs.REQUIRE_PIN, it) }
                    TvToggleRow("Permitir nuevas conexiones", allowNew) { viewModel.set(Prefs.ALLOW_NEW_CONN, it) }
                }
                TvGroup("Iniciar AirPlay") {
                    TvToggleRow("Al abrir la app", autoStart) { viewModel.set(Prefs.AUTO_START, it) }
                    TvToggleRow("Al encender la TV", bootStart) { viewModel.set(Prefs.BOOT_AUTO_START, it) }
                }
            }
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(bottom = 30.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                TvGroup("Al conectarse un dispositivo") {
                    TvToggleRow("Abrir la app", launchOnConnect) { viewModel.set(Prefs.LAUNCH_ON_CONNECT, it) }
                    TvToggleRow("Pantalla completa", fullscreen) { viewModel.set(Prefs.AUTO_FULLSCREEN, it) }
                }
                TvGroup("Imagen") {
                    val resLabel = RESOLUTIONS.firstOrNull { it.first == resolution }?.second ?: resolution
                    TvValueRow("Resolución", resLabel) {
                        val i = RESOLUTIONS.indexOfFirst { it.first == resolution }
                        viewModel.set(Prefs.RESOLUTION, RESOLUTIONS[(i + 1) % RESOLUTIONS.size].first)
                    }
                    TvValueRow("Fotogramas por segundo", maxFps.toString()) {
                        val i = FPS.indexOf(maxFps)
                        viewModel.set(Prefs.MAX_FPS, FPS[(i + 1) % FPS.size])
                    }
                }
            }
        }
    }

    if (editingName) {
        var draft by remember { mutableStateOf(name) }
        val save = {
            val clean = draft.trim()
            if (clean.isNotEmpty()) viewModel.set(Prefs.SERVER_NAME, clean)
            editingName = false
        }
        MaterialTheme(colorScheme = darkColorScheme()) { AlertDialog(
            onDismissRequest = { editingName = false },
            title = { Text("Nombre del receptor") },
            text = {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { save() })
                )
            },
            confirmButton = { TextButton(onClick = { save() }) { Text("Guardar") } },
            dismissButton = { TextButton(onClick = { editingName = false }) { Text("Cancelar") } }
        ) }
    }
}

@Composable
private fun TvGroup(title: String, rows: @Composable () -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth().tvSurface(TvStyle.Card, 24).padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
    ) {
        Text(
            title.uppercase(),
            color = TvStyle.Label3,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.06.em,
            modifier = Modifier.padding(start = 16.dp, bottom = 2.dp)
        )
        rows()
    }
}

/** One settings row: glass at rest, white and slightly larger when focused (critically damped spring). */
@Composable
private fun TvRow(
    label: String,
    modifier: Modifier,
    onClick: () -> Unit,
    trailing: @Composable (focused: Boolean) -> Unit
) {
    val source = remember { MutableInteractionSource() }
    val focused by source.collectIsFocusedAsState()
    val p = focusSpring(focused)
    val scale = 1f + 0.04f * p   // full-width rows: 1.04, not 1.08, or they would spill out of their card
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(if (focused) (TV_LIFT_DP * p).dp else 0.dp, shape)   // not while unfocused: it would show through the see-through fill
            .clip(shape)
            .background(if (focused) Color.White else TvStyle.Tile)
            .clickable(interactionSource = source, indication = null, onClick = onClick)
            .height(42.dp)
            .padding(start = 16.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = if (focused) TvStyle.Dark else TvStyle.Label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        trailing(focused)
    }
}

@Composable
private fun TvToggleRow(label: String, on: Boolean, onChange: (Boolean) -> Unit) {
    TvRow(label, Modifier, onClick = { onChange(!on) }) { focused ->
        val knob by animateDpAsState(
            targetValue = if (on) 16.dp else 2.dp,
            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
            label = "knob"
        )
        val track by animateColorAsState(
            if (on) TvStyle.Green else if (focused) Color(0x3D787880) else Color(0x5C787880),
            tween(220), label = "track"
        )
        Box(
            Modifier
                .width(38.dp).height(23.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(track)
        ) {
            Box(
                Modifier
                    .offset(x = knob, y = 2.dp)
                    .size(19.dp)
                    .shadow(2.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}

@Composable
private fun TvValueRow(label: String, value: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    TvRow(label, modifier, onClick) { focused ->
        Crossfade(targetState = value, animationSpec = tween(220), label = "value") { v ->
            Text(
                "$v  ›",
                color = if (focused) Color(0xBF3C3C43) else Color(0x99EBEBF5),
                fontSize = 13.sp
            )
        }
    }
}
