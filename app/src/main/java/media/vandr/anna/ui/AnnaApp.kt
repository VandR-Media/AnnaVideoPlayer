package media.vandr.anna.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import media.vandr.anna.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import media.vandr.anna.data.*
import java.util.Locale

private val Ink = Color(0xFF080610)
private val Panel = Color(0xFF171020)
private val Lilac = Color(0xFFD6A1FF)
private val Magenta = Color(0xFFFF4EDB)
private val DeepViolet = Color(0xFF24103D)
private val Muted = Color(0xFFA5A5B8)

@Composable
fun AnnaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Lilac, onPrimary = Color(0xFF24153E),
            background = Ink, onBackground = Color(0xFFF7F3FF),
            surface = Panel, onSurface = Color(0xFFF7F3FF),
            surfaceVariant = Color(0xFF292839), onSurfaceVariant = Muted,
            secondary = Magenta,
            tertiary = Color(0xFF9B5CFF),
            surfaceTint = Lilac
        ),
        typography = Typography(),
        shapes = Shapes(
            extraSmall = RoundedCornerShape(10.dp),
            small = RoundedCornerShape(14.dp),
            medium = RoundedCornerShape(20.dp),
            large = RoundedCornerShape(26.dp),
            extraLarge = RoundedCornerShape(30.dp)
        ),
        content = content
    )
}

private enum class Tab(val label: String) {
    VIDEOS("Видео"), FOLDERS("Папки"), FAVORITES("Избранное"), HISTORY("История"), STREAM("Сеть")
}

@Composable
fun AnnaApp(
    library: VideoLibrary,
    history: PlaybackHistory,
    canReadVideos: Boolean,
    refreshToken: Int,
    onRequestPermission: () -> Unit,
    onRefresh: () -> Unit,
    onPlay: (String, String) -> Unit
) {
    var current by rememberSaveable { mutableStateOf(Tab.VIDEOS) }
    var query by rememberSaveable { mutableStateOf("") }
    var order by rememberSaveable { mutableStateOf(VideoOrder.NEWEST) }
    var items by remember { mutableStateOf<List<VideoItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var changeVersion by remember { mutableIntStateOf(0) }

    LaunchedEffect(canReadVideos, refreshToken) {
        loading = true
        items = if (canReadVideos) withContext(Dispatchers.IO) {
            runCatching { library.scan() }.getOrDefault(emptyList())
        } else emptyList()
        loading = false
    }

    val filtered = remember(items, query, order) {
        LibrarySorting.sort(
            items.filter { it.title.contains(query, ignoreCase = true) || it.folder.contains(query, ignoreCase = true) },
            order
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Column(
                Modifier.statusBarsPadding().background(
                    Brush.verticalGradient(listOf(Color(0xFF28103B), Color(0xFF130C20), Ink))
                ).padding(start = 20.dp, end = 14.dp, top = 15.dp, bottom = 11.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.annavue_brand_logo),
                        contentDescription = "Логотип Annavue",
                        modifier = Modifier.size(50.dp).clip(RoundedCornerShape(13.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("ANNAVUE", fontWeight = FontWeight.ExtraBold, fontSize = 21.sp, letterSpacing = 2.1.sp, color = Color(0xFFF5E9FF))
                        Text("See more. Feel more.", fontSize = 11.sp, color = Lilac, letterSpacing = 0.7.sp)
                    }
                    if (current != Tab.STREAM) {
                        IconButton(onClick = onRefresh) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Обновить медиатеку", tint = Lilac)
                        }
                    }
                }
                if (current != Tab.STREAM) {
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = query, onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        trailingIcon = if (query.isNotEmpty()) ({
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "Очистить поиск")
                            }
                        }) else null,
                        placeholder = { Text("Найти видео или папку") },
                        shape = RoundedCornerShape(18.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Magenta, unfocusedBorderColor = Color(0xFF514064), cursorColor = Magenta
                        )
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF110B1B), tonalElevation = 0.dp) {
                for (tab in Tab.entries) {
                    NavigationBarItem(
                        selected = tab == current,
                        onClick = { current = tab; query = "" },
                        icon = {
                            Icon(when (tab) {
                                Tab.VIDEOS -> Icons.Filled.SmartDisplay
                                Tab.FOLDERS -> Icons.Filled.Folder
                                Tab.FAVORITES -> Icons.Filled.Favorite
                                Tab.HISTORY -> Icons.Filled.History
                                Tab.STREAM -> Icons.Filled.WifiTethering
                            }, contentDescription = null)
                        },
                        label = { Text(tab.label, fontSize = 10.sp, maxLines = 1) },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF24153E), selectedTextColor = Lilac,
                            indicatorColor = Lilac, unselectedIconColor = Muted, unselectedTextColor = Muted
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF130C20), Ink, Color(0xFF0C0814)))).padding(innerPadding)) {
            if (current == Tab.STREAM) {
                StreamTab(onPlay = onPlay, history = history, refreshToken = refreshToken)
            } else if (!canReadVideos) {
                EmptyScreen(
                    title = "Разрешить доступ к видео",
                    subtitle = "Анна найдёт видео на телефоне и разложит их по папкам. Фотографии и другие файлы не нужны.",
                    button = "Выбрать видео или разрешить доступ",
                    action = onRequestPermission
                )
            } else if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Lilac)
                }
            } else {
                when (current) {
                    Tab.VIDEOS -> {
                        SectionHeading("Все видео", "${filtered.size} файлов", order) { order = it }
                        VideoList(filtered, history, changeVersion, onPlay) { changeVersion++ }
                    }
                    Tab.FOLDERS -> FolderTab(filtered, history, changeVersion, onPlay) { changeVersion++ }
                    Tab.FAVORITES -> {
                        val favorites = filtered.filter { history.isFavorite(it.uri.toString()) }
                        SectionHeading("Избранное", "${favorites.size} видео")
                        VideoList(favorites, history, changeVersion, onPlay) { changeVersion++ }
                    }
                    Tab.HISTORY -> {
                        val visited = history.visits()
                        val byUri = filtered.associateBy { it.uri.toString() }
                        val recent = visited.mapNotNull { byUri[it] }
                        SectionHeading("Продолжить просмотр", "${recent.size} видео")
                        VideoList(recent, history, changeVersion, onPlay) { changeVersion++ }
                    }
                    Tab.STREAM -> Unit
                }
            }
        }
    }
}

@Composable
private fun SectionHeading(title: String, subtitle: String, selected: VideoOrder? = null, onSort: (VideoOrder) -> Unit = {}) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 17.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Muted, fontSize = 12.sp)
        }
        if (selected != null) {
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Filled.Sort, contentDescription = "Сортировка")
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    VideoOrder.entries.forEach { mode ->
                        DropdownMenuItem(text = { Text(mode.label) }, onClick = { onSort(mode); menuOpen = false })
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderTab(
    items: List<VideoItem>, history: PlaybackHistory, version: Int,
    onPlay: (String, String) -> Unit, onFavoriteChanged: () -> Unit
) {
    var folder by remember { mutableStateOf<String?>(null) }
    val groups = remember(items) { items.groupBy { it.folder }.toSortedMap(String.CASE_INSENSITIVE_ORDER) }
    if (folder == null) {
        SectionHeading("Папки", "${groups.size} папок")
        if (groups.isEmpty()) {
            EmptyScreen("Видео не найдены", "Снимите видео камерой или скопируйте фильм в память телефона.")
        } else {
            LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                items(groups.entries.toList(), key = { it.key }) { entry ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(19.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF251633), Panel))).clickable { folder = entry.key }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VideoCover(entry.value.first().uri, Modifier.size(width = 110.dp, height = 70.dp).clip(RoundedCornerShape(12.dp)))
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(entry.key.substringAfterLast('/'), fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(entry.key, color = Muted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${entry.value.size} видео", fontSize = 12.sp, color = Lilac)
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Muted)
                    }
                }
            }
        }
    } else {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { folder = null }) { Icon(Icons.Filled.ArrowBack, contentDescription = "К папкам") }
            Text(folder.orEmpty(), maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
        }
        VideoList(groups[folder].orEmpty(), history, version, onPlay, onFavoriteChanged)
    }
}

@Composable
private fun VideoList(
    items: List<VideoItem>, history: PlaybackHistory, version: Int,
    onPlay: (String, String) -> Unit, onFavoriteChanged: () -> Unit
) {
    if (items.isEmpty()) {
        EmptyScreen("Пока пусто", "Здесь появятся видео, когда вы добавите их или измените поиск.")
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 22.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items, key = { it.uri.toString() }) { video ->
            val uri = video.uri.toString()
            val isFavorite = remember(version, uri) { history.isFavorite(uri) }
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF251633), Panel))).clickable { onPlay(uri, video.title) }.padding(9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    VideoCover(video.uri, Modifier.size(132.dp, 82.dp).clip(RoundedCornerShape(13.dp)))
                    Box(Modifier.align(Alignment.BottomEnd).padding(5.dp)
                        .background(Color.Black.copy(alpha = 0.76f), RoundedCornerShape(5.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp)) {
                        Text(formatDuration(video.durationMs), fontSize = 10.sp, color = Color.White)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(video.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, lineHeight = 17.sp, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(5.dp))
                    Text(video.folder.substringAfterLast('/'), fontSize = 11.sp, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(formatSize(video.sizeBytes), fontSize = 11.sp, color = Lilac)
                }
                IconButton(onClick = { history.toggleFavorite(uri); onFavoriteChanged() }, modifier = Modifier.size(40.dp)) {
                    Icon(
                        if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isFavorite) "Удалить из избранного" else "В избранное",
                        tint = if (isFavorite) Lilac else Muted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StreamTab(onPlay: (String, String) -> Unit, history: PlaybackHistory, refreshToken: Int) {
    var url by rememberSaveable { mutableStateOf("") }
    val valid = StreamLinks.supported(url)
    val recentlyWatched = remember(refreshToken) {
        history.visits().filter(StreamLinks::supported).take(8)
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Смотреть по ссылке", fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Text("Откройте прямую ссылку на видео или прямой эфир. Работают HTTP(S), HLS (.m3u8), DASH (.mpd), RTSP и совместимые потоки.", color = Muted, fontSize = 14.sp)
        OutlinedTextField(
            value = url, onValueChange = { url = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Адрес видео") },
            placeholder = { Text("https://example.org/movie.m3u8") },
            minLines = 2, maxLines = 4,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            shape = RoundedCornerShape(16.dp)
        )
        Button(
            onClick = { onPlay(url.trim(), "Видео из интернета") },
            enabled = valid,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Воспроизвести", fontWeight = FontWeight.Bold)
        }
        HorizontalDivider(color = Color(0xFF4A315F))
        Text("Поддерживаются открытые ссылки и потоки, к которым у вас есть доступ. Это не обход подписок или защиты DRM.", color = Muted, fontSize = 12.sp)
        Text("Онлайн-каталог фильмов будет отдельным модулем после выбора доступного и легального источника.", color = Muted, fontSize = 12.sp)
        if (recentlyWatched.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text("Недавние потоки", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            recentlyWatched.forEach { link ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF251633), Panel))).clickable { onPlay(link, "Видео из интернета") }
                        .padding(13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.PlayCircle, contentDescription = null, tint = Lilac)
                    Spacer(Modifier.width(12.dp))
                    Text(link, maxLines = 2, overflow = TextOverflow.Ellipsis, fontSize = 12.sp, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun EmptyScreen(title: String, subtitle: String, button: String? = null, action: () -> Unit = {}) {
    Box(Modifier.fillMaxSize().padding(28.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Icon(Icons.Filled.VideoLibrary, contentDescription = null, modifier = Modifier.size(62.dp), tint = Lilac)
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Muted, fontSize = 14.sp, lineHeight = 20.sp)
            if (button != null) {
                Button(onClick = action, shape = RoundedCornerShape(14.dp)) { Text(button) }
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    val seconds = ms.coerceAtLeast(0) / 1000
    return if (seconds >= 3600) "%d:%02d:%02d".format(Locale.ROOT, seconds / 3600, seconds / 60 % 60, seconds % 60)
    else "%02d:%02d".format(Locale.ROOT, seconds / 60, seconds % 60)
}

private fun formatSize(bytes: Long): String = if (bytes >= 1024L * 1024 * 1024) {
    "%.1f ГБ".format(Locale.getDefault(), bytes.toDouble() / (1024L * 1024 * 1024))
} else {
    "%.0f МБ".format(Locale.getDefault(), bytes.toDouble() / (1024L * 1024))
}
