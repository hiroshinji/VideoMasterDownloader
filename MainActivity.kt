package com.videomaster.downloader

import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.webkit.URLUtil
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF0B0D12)
private val Panel = Color(0xFF151923)
private val Muted = Color(0xFF9299AA)
private val Accent = Color(0xFF8B5CF6)
private val Cyan = Color(0xFF43D9D0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val incoming = when (intent?.action) {
            Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
            Intent.ACTION_VIEW -> intent.dataString.orEmpty()
            else -> ""
        }
        setContent { VideoMasterApp(incoming) }
    }
}

@Composable
private fun VideoMasterApp(incoming: String) {
    val context = LocalContext.current
    var link by remember { mutableStateOf(incoming) }
    var notice by remember { mutableStateOf("") }
    var showHistory by remember { mutableStateOf(false) }
    var history by remember { mutableStateOf(listOf<String>()) }
    val platform = detectPlatform(link)
    val isDirectFile = isDirectMediaUrl(link)

    MaterialTheme(colorScheme = darkColorScheme(
        background = Bg, surface = Panel, primary = Accent,
        onBackground = Color.White, onSurface = Color.White
    )) {
        Surface(Modifier.fillMaxSize(), color = Bg) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Spacer(Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(48.dp).background(Brush.linearGradient(listOf(Accent, Cyan)), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Text("VideoMaster", fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Text("SEM ANÚNCIOS  ·  SEM COMPLICAÇÃO", fontSize = 9.sp, letterSpacing = 1.1.sp, color = Muted)
                    }
                    IconButton(onClick = { showHistory = !showHistory }) {
                        Icon(Icons.Default.History, "Histórico", tint = Muted)
                    }
                }

                AnimatedVisibility(true, enter = fadeIn() + slideInVertically()) {
                    Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF262044), Color(0xFF14252D))), RoundedCornerShape(26.dp)).padding(22.dp)) {
                        Text("Seus vídeos.\nDo seu jeito.", fontSize = 29.sp, lineHeight = 34.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Spacer(Modifier.height(8.dp))
                        Text("Cole o link, confira a origem e baixe. Sem anúncios, sem cadastro e sem etapas desnecessárias.", fontSize = 14.sp, color = Color(0xFFD2D5E0), lineHeight = 20.sp)
                        Spacer(Modifier.height(20.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PlatformChip("YouTube", Color(0xFFFF5656))
                            PlatformChip("Instagram", Color(0xFFE879B9))
                            PlatformChip("TikTok", Color(0xFF4ADDD3))
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("LINK DO VÍDEO", fontSize = 11.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold, color = Muted)
                    OutlinedTextField(
                        value = link,
                        onValueChange = { link = it; notice = "" },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("https://…", color = Muted) },
                        leadingIcon = { Icon(Icons.Default.Link, null, tint = Accent) },
                        trailingIcon = {
                            IconButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                link = clipboard.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString().orEmpty()
                            }) { Icon(Icons.Default.ContentPaste, "Colar link", tint = Cyan) }
                        },
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Accent, unfocusedBorderColor = Color(0xFF303644),
                            focusedContainerColor = Panel, unfocusedContainerColor = Panel,
                            cursorColor = Cyan, focusedTextColor = Color.White, unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                    if (link.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.size(8.dp).background(if (platform == "Link desconhecido") Muted else Cyan, CircleShape))
                            Text("Plataforma: $platform", fontSize = 12.sp, color = Muted)
                        }
                    }
                    Button(
                        onClick = {
                            val normalized = link.trim()
                            if (!normalized.startsWith("https://") && !normalized.startsWith("http://")) {
                                notice = "Cole um link HTTPS válido para continuar."
                            } else if (isDirectMediaUrl(normalized)) {
                                startDownload(context, normalized, noticeSetter = { notice = it })
                                history = (listOf(normalized) + history).distinct().take(20)
                            } else if (detectPlatform(normalized) != "Link desconhecido") {
                                notice = if (detectPlatform(normalized) == "TikTok") "Link do TikTok reconhecido. Para salvar sem marca-d’água, é necessário que a plataforma ou o titular disponibilize uma versão limpa autorizada. Este link, sozinho, não fornece um arquivo limpo para baixar. Você pode abrir o vídeo no TikTok ou usar um link direto autorizado para a mídia." else "Link de plataforma reconhecido. O app não extrai arquivos diretamente de páginas protegidas. Use o recurso oficial de download quando disponível ou um link direto autorizado para o arquivo de mídia."
                                history = (listOf(normalized) + history).distinct().take(20)
                            } else {
                                notice = "Esse endereço não parece ser um arquivo de vídeo/áudio direto (.mp4, .m4v, .webm, .mp3 ou .m4a)."
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent)
                    ) {
                        Icon(Icons.Default.Download, null)
                        Spacer(Modifier.width(10.dp))
                        Text("Continuar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (notice.isNotBlank()) {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF202432)), shape = RoundedCornerShape(16.dp)) {
                        Text(notice, Modifier.padding(16.dp), color = Color(0xFFE0E3EE), fontSize = 13.sp, lineHeight = 19.sp)
                        if (detectPlatform(link) != "Link desconhecido" && link.startsWith("http")) {
                            TextButton(onClick = { openUrl(context, link) }, modifier = Modifier.padding(start = 6.dp, bottom = 6.dp)) {
                                Text("Abrir na plataforma", color = Cyan)
                            }
                        }
                    }
                }

                if (showHistory) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Links recentes", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        if (history.isEmpty()) Text("Seus links recentes aparecerão aqui.", color = Muted, fontSize = 13.sp)
                        history.forEach { item ->
                            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(14.dp), onClick = { link = item }) {
                                Text(item, Modifier.padding(13.dp), color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(20.dp)).padding(17.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Shield, null, tint = Cyan)
                        Text("Privacidade e compatibilidade", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Text("Sem anúncios e sem rastreadores publicitários integrados. Links diretos autorizados são salvos em Downloads. Links de YouTube, Instagram e TikTok são reconhecidos, mas só é possível baixar quando o serviço oferece essa opção ou existe uma fonte de mídia autorizada. No TikTok, uma versão sem marca-d’água precisa ser disponibilizada legitimamente pelo serviço ou pelo titular.", color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.MusicNote, null, tint = Accent, modifier = Modifier.size(18.dp))
                        Text("MP3: recurso futuro, para fontes de áudio autorizadas.", color = Muted, fontSize = 11.sp)
                    }
                }
                Text("VideoMaster Downloader · sem anúncios · v1.0.0", Modifier.fillMaxWidth().padding(bottom = 22.dp), color = Color(0xFF555D6F), fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun PlatformChip(name: String, tint: Color) {
    Row(Modifier.background(Color(0x332D3444), RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(6.dp).background(tint, CircleShape))
        Text(name, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

private fun detectPlatform(raw: String): String {
    val host = runCatching { Uri.parse(raw.trim()).host?.lowercase().orEmpty() }.getOrDefault("")
    return when {
        host.contains("youtube.com") || host == "youtu.be" || host.endsWith(".youtube.com") -> "YouTube"
        host.contains("instagram.com") || host.endsWith(".instagram.com") -> "Instagram"
        host.contains("tiktok.com") || host.endsWith(".tiktok.com") -> "TikTok"
        else -> "Link desconhecido"
    }
}

private fun isDirectMediaUrl(raw: String): Boolean {
    val path = runCatching { Uri.parse(raw).path?.lowercase().orEmpty() }.getOrDefault("")
    return listOf(".mp4", ".m4v", ".webm", ".mp3", ".m4a", ".aac", ".wav", ".ogg").any { path.endsWith(it) }
}

private fun startDownload(context: Context, url: String, noticeSetter: (String) -> Unit) {
    runCatching {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(URLUtil.guessFileName(url, null, null))
            .setDescription("VideoMaster Downloader")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(false)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, null, null))
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
        noticeSetter("Download iniciado. Acompanhe o progresso na notificação do Android; o arquivo será salvo em Downloads.")
    }.onFailure { noticeSetter("Não foi possível iniciar o download: ${it.localizedMessage ?: "erro desconhecido"}") }
}

private fun openUrl(context: Context, url: String) {
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
}
