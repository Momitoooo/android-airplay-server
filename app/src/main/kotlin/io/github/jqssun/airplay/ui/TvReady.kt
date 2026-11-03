package io.github.jqssun.airplay.ui

// Momo's TV redesign (sub-stage 3a): the screen shown while nobody is connected.
// Kept in its own file so upstream changes to MainScreen.kt stay easy to merge.

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import io.github.jqssun.airplay.Prefs
import io.github.jqssun.airplay.R
import io.github.jqssun.airplay.service.AirPlayService.ServerState
import io.github.jqssun.airplay.viewmodel.MainViewModel

/** Design tokens shared with Momo's other TV apps (Home Hub, Backlight Sync). */
object TvStyle {
    val Ground = Color(0xFF101218)
    val Label = Color(0xFFF5F5F7)
    val Label2 = Color(0xA3EBEBF5)   // 64%
    val Label3 = Color(0x99EBEBF5)   // 60%
    val Chip = Color(0x12FFFFFF)     // 7% white
    val Glass = Color(0x14FFFFFF)    // 8% white
    val Dark = Color(0xFF1C1C1E)
    val Green = Color(0xFF30D158)
    val Amber = Color(0xFFFFCB80)
    val Red = Color(0xFFFF453A)
}

@Composable
fun TvReadyScreen(viewModel: MainViewModel, onSettings: () -> Unit) {
    val state by viewModel.serverState.collectAsState()
    val name by viewModel.pref(Prefs.SERVER_NAME).collectAsState()
    val running = state == ServerState.RUNNING
    val settingsFocus = remember { FocusRequester() }
    val startFocus = remember { FocusRequester() }

    LaunchedEffect(running) {
        if (running) settingsFocus.requestFocusUntilLanded() else startFocus.requestFocusUntilLanded()
    }

    Box(modifier = Modifier.fillMaxSize().background(TvStyle.Ground)) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(105.dp).clip(CircleShape).background(Color(0x14EBEBF5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.tv_ic_airplay),
                    contentDescription = null,
                    tint = TvStyle.Label,
                    modifier = Modifier.size(54.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = when (state) {
                    ServerState.RUNNING -> "Listo para AirPlay"
                    ServerState.ERROR -> "No se pudo iniciar AirPlay"
                    else -> "AirPlay está parado"
                },
                color = TvStyle.Label,
                fontSize = 39.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.02).em
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = if (running)
                    "En tu iPhone o Mac, abre el Centro de control, toca Duplicar pantalla y elige «$name»."
                else
                    "Pulsa Iniciar para que tu iPhone o Mac encuentren la TV.",
                color = TvStyle.Label2,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 510.dp)
            )
            Spacer(Modifier.height(26.dp))
            if (running) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TvChip(painterResource(R.drawable.tv_ic_screen), "Duplicar pantalla")
                    TvChip(painterResource(R.drawable.tv_ic_music), "Música")
                    TvChip(painterResource(R.drawable.tv_ic_video), "Vídeo")
                }
            } else {
                TvPillButton(
                    text = if (state == ServerState.ERROR) "Iniciar de nuevo" else "Iniciar",
                    icon = rememberVectorPainter(Icons.Rounded.PlayArrow),
                    onClick = { viewModel.startServer() },
                    modifier = Modifier.focusRequester(startFocus)
                )
            }
        }

        Row(
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 60.dp, bottom = 36.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                Modifier.size(8.dp).clip(CircleShape).background(
                    when (state) {
                        ServerState.RUNNING -> TvStyle.Green
                        ServerState.ERROR -> TvStyle.Red
                        else -> TvStyle.Amber
                    }
                )
            )
            Text(
                text = when (state) {
                    ServerState.RUNNING -> "Visible como «$name»"
                    ServerState.ERROR -> "Error al iniciar el receptor"
                    else -> "No visible en la red"
                },
                color = TvStyle.Label3,
                fontSize = 12.sp
            )
        }

        TvPillButton(
            text = "Ajustes",
            icon = rememberVectorPainter(Icons.Rounded.Settings),
            onClick = onSettings,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 60.dp, bottom = 28.dp)
                .focusRequester(settingsFocus)
        )
    }
}

/** A quiet label with a glyph (not focusable). */
@Composable
fun TvChip(icon: Painter, text: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(TvStyle.Chip)
            .height(32.dp)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(painter = icon, contentDescription = null, tint = Color(0xCCEBEBF5), modifier = Modifier.size(14.dp))
        Text(text = text, color = Color(0xCCEBEBF5), fontSize = 13.sp)
    }
}

/**
 * tvOS-style button: glass at rest; when focused it turns white with dark text, grows (x1.06) and lifts,
 * on a critically damped spring (no bounce). Same behaviour as the buttons in Home Hub.
 */
@Composable
fun TvPillButton(text: String, icon: Painter?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val source = remember { MutableInteractionSource() }
    val focused by source.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        targetValue = if (focused) 1.06f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "lift"
    )
    val shape = RoundedCornerShape(18.dp)
    val fg = if (focused) TvStyle.Dark else TvStyle.Label
    Row(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(if (focused) 14.dp else 0.dp, shape)
            .clip(shape)
            .background(if (focused) Color.White else TvStyle.Glass)
            .clickable(interactionSource = source, indication = null, onClick = onClick)
            .height(36.dp)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (icon != null) Icon(painter = icon, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
        Text(text = text, color = fg, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
