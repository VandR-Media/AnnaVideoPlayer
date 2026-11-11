package media.vandr.anna.player

import android.app.PictureInPictureParams
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.util.Rational
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import media.vandr.anna.data.PlaybackHistory

@OptIn(UnstableApi::class)
class PlayerActivity : ComponentActivity() {
    companion object {
        const val EXTRA_VIDEO_URI = "video_uri"
        const val EXTRA_TITLE = "video_title"
    }

    private val history by lazy { PlaybackHistory(this) }
    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var overlay: LinearLayout
    private var videoUri: String = ""
    private var finished = false
    private var aspectMode = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val source = intent.getStringExtra(EXTRA_VIDEO_URI)?.trim().orEmpty()
        if (source.isBlank()) { finish(); return }
        videoUri = source
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        window.setBackgroundDrawableResource(android.R.color.black)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemBars()

        playerView = PlayerView(this).apply {
            useController = true
            setShowSubtitleButton(true)
            controllerShowTimeoutMs = 3500
            setBackgroundColor(Color.BLACK)
        }

        val root = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }
        root.addView(playerView, FrameLayout.LayoutParams(-1, -1))
        overlay = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10), dp(4), dp(10), dp(4))
            setBackgroundColor(0x99000000.toInt())
        }
        overlay.addView(actionLabel("←") { finish() })
        overlay.addView(TextView(this).apply {
            text = intent.getStringExtra(EXTRA_TITLE) ?: "АННА"
            setTextColor(Color.WHITE)
            textSize = 14f
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        overlay.addView(actionLabel("⛶") { cycleAspectRatio() })
        overlay.addView(actionLabel("◱") { enterPip() })
        root.addView(overlay, FrameLayout.LayoutParams(-1, dp(48), Gravity.TOP))
        setContentView(root)

        val uri = android.net.Uri.parse(source)
        val mediaBuilder = MediaItem.Builder().setUri(uri)
        when {
            uri.path.orEmpty().endsWith(".m3u8", ignoreCase = true) -> mediaBuilder.setMimeType(MimeTypes.APPLICATION_M3U8)
            uri.path.orEmpty().endsWith(".mpd", ignoreCase = true) -> mediaBuilder.setMimeType(MimeTypes.APPLICATION_MPD)
        }
        player = ExoPlayer.Builder(this)
            .setSeekBackIncrementMs(10_000)
            .setSeekForwardIncrementMs(10_000)
            .build().apply {
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_ENDED) {
                            finished = true
                            history.clearPosition(videoUri)
                        }
                    }
                    override fun onPlayerError(error: PlaybackException) {
                        Toast.makeText(this@PlayerActivity,
                            "Не удалось воспроизвести: ${error.errorCodeName}", Toast.LENGTH_LONG).show()
                    }
                })
                setMediaItem(mediaBuilder.build())
                prepare()
                history.position(videoUri).takeIf { it > 0 }?.let { seekTo(it) }
                playWhenReady = true
            }
        playerView.player = player
        history.recordVisit(videoUri)
    }

    private fun actionLabel(label: String, click: () -> Unit): TextView = TextView(this).apply {
        text = label
        textSize = 24f
        setTextColor(Color.WHITE)
        gravity = Gravity.CENTER
        contentDescription = when (label) {
            "←" -> "Назад"
            "⛶" -> "Масштаб видео"
            else -> "Картинка в картинке"
        }
        layoutParams = LinearLayout.LayoutParams(dp(52), dp(44))
        setOnClickListener { click() }
    }

    private fun cycleAspectRatio() {
        aspectMode = (aspectMode + 1) % 3
        val mode = when (aspectMode) {
            1 -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            2 -> AspectRatioFrameLayout.RESIZE_MODE_FILL
            else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
        }
        playerView.resizeMode = mode
        val label = when (aspectMode) {
            1 -> "Заполнить без искажений"
            2 -> "Растянуть"
            else -> "Вписать в экран"
        }
        Toast.makeText(this, label, Toast.LENGTH_SHORT).show()
    }

    private fun enterPip() {
        if (android.content.pm.PackageManager.FEATURE_PICTURE_IN_PICTURE !in packageManager.systemAvailableFeatures.map { it.name }) {
            Toast.makeText(this, "Режим PiP не поддерживается", Toast.LENGTH_SHORT).show()
            return
        }
        val aspect = Rational(16, 9)
        runCatching {
            enterPictureInPictureMode(PictureInPictureParams.Builder().setAspectRatio(aspect).build())
        }.onFailure { Toast.makeText(this, "Не удалось включить PiP", Toast.LENGTH_SHORT).show() }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: android.content.res.Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        overlay.visibility = if (isInPictureInPictureMode) View.GONE else View.VISIBLE
        playerView.useController = !isInPictureInPictureMode
    }

    private fun savePlaybackPosition() {
        val p = player ?: return
        if (finished) { history.clearPosition(videoUri); return }
        val duration = p.duration
        val at = p.currentPosition
        if (duration == C.TIME_UNSET || duration <= 0 || at < 0) return
        if (at >= duration - 15_000) history.clearPosition(videoUri)
        else history.setPosition(videoUri, at)
    }

    override fun onPause() {
        savePlaybackPosition()
        super.onPause()
    }

    override fun onStop() {
        if (!isInPictureInPictureMode) player?.pause()
        savePlaybackPosition()
        super.onStop()
    }

    override fun onDestroy() {
        savePlaybackPosition()
        playerView.player = null
        player?.release()
        player = null
        super.onDestroy()
    }

    private fun hideSystemBars() {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
    }

    private fun dp(value: Int): Int = (resources.displayMetrics.density * value).toInt()
}
