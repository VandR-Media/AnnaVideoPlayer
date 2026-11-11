package media.vandr.anna

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import media.vandr.anna.data.PlaybackHistory
import media.vandr.anna.data.VideoLibrary
import media.vandr.anna.player.PlayerActivity
import media.vandr.anna.ui.AnnaApp
import media.vandr.anna.ui.AnnaTheme

class MainActivity : ComponentActivity() {
    private val library by lazy { VideoLibrary(this) }
    private val history by lazy { PlaybackHistory(this) }
    private var refreshToken by mutableIntStateOf(0)

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { refreshToken++ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AnnaTheme {
                AnnaApp(
                    library = library,
                    history = history,
                    canReadVideos = hasVideoPermission(),
                    refreshToken = refreshToken,
                    onRequestPermission = { requestPermission.launch(videoPermissions()) },
                    onRefresh = { refreshToken++ },
                    onPlay = { uri, name ->
                        startActivity(
                            Intent(this, PlayerActivity::class.java)
                                .putExtra(PlayerActivity.EXTRA_VIDEO_URI, uri)
                                .putExtra(PlayerActivity.EXTRA_TITLE, name)
                        )
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshToken++
    }

    private fun hasVideoPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
        ) return true
        if (Build.VERSION.SDK_INT >= 34 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == PackageManager.PERMISSION_GRANTED
        ) return true
        return Build.VERSION.SDK_INT < 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun videoPermissions(): Array<String> = when {
        Build.VERSION.SDK_INT >= 34 -> arrayOf(
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
        )
        Build.VERSION.SDK_INT >= 33 -> arrayOf(Manifest.permission.READ_MEDIA_VIDEO)
        else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}
