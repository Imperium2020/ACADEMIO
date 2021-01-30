package com.imperium.academio

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.util.Util
import com.imperium.academio.databinding.ActivityMaterialVideoViewBinding

class MaterialVideoView : AppCompatActivity() {
    private lateinit var binding: ActivityMaterialVideoViewBinding
    private lateinit var btnFullEnter: ImageButton
    private lateinit var btnFullExit: ImageButton
    private lateinit var params: ViewGroup.LayoutParams

    private var currentWindow = 0
    private var playWhenReady = true
    private var playbackPosition: Long = 0
    private var player: SimpleExoPlayer? = null
    private var uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get data from intent
        val args = intent.extras
        uri = Uri.parse(args!!.getString("uriString"))
        val title = args.getString("title")
        val text = args.getString("text", "No description provided")
        if (uri == null || title == null) return

        binding = ActivityMaterialVideoViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        params = binding.playerView.layoutParams

        // Set title and description
        binding.videoTitle.text = title
        binding.videoText.text = text

        // Fullscreen button
        btnFullEnter = binding.playerView.findViewById(R.id.exo_fullscreen_enter)
        btnFullEnter.setOnClickListener { fullScreenEnter() }
        btnFullEnter.visibility = View.VISIBLE

        // Release Fullscreen button
        btnFullExit = binding.playerView.findViewById(R.id.exo_fullscreen_exit)
        btnFullExit.setOnClickListener { fullScreenExit() }
        btnFullExit.visibility = View.GONE
    }

    public override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer()
        }
    }

    public override fun onResume() {
        super.onResume()
        setSystemUI(btnFullEnter.visibility == View.VISIBLE)
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer()
        }
    }

    public override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    public override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    private fun initializePlayer() {
        val exoPlayer = SimpleExoPlayer.Builder(this).build()
        player = exoPlayer
        binding.playerView.player = player


        // Prepare media item
        val mediaItem = MediaItem.fromUri(uri!!)
        exoPlayer.setMediaItem(mediaItem)

        // Prepare player state
        exoPlayer.playWhenReady = playWhenReady
        exoPlayer.seekTo(currentWindow, playbackPosition)
        exoPlayer.prepare()
    }

    // Remove player when paused or stopped
    private fun releasePlayer() {
        if (player == null) return
        playbackPosition = player!!.currentPosition
        currentWindow = player!!.currentWindowIndex
        playWhenReady = player!!.playWhenReady
        player!!.release()
        player = null
    }

    // Set Fullscreen
    private fun fullScreenEnter() {
        // Set background black
        binding.videoConstraint.setBackgroundColor(
                ResourcesCompat.getColor(resources, R.color.black, null))

        // Hide other views and set exit-fullscreen button as visible
        btnFullExit.visibility = View.VISIBLE
        btnFullEnter.visibility = View.GONE
        binding.scrollView.visibility = View.GONE

        // Request system ui for visibility changes and orientation
        setSystemUI(false)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        // Maximize layout parameters of player
        val p = binding.playerView.layoutParams
        p.width = ViewGroup.LayoutParams.MATCH_PARENT
        p.height = ViewGroup.LayoutParams.MATCH_PARENT
        binding.playerView.layoutParams = p
    }

    // Release Fullscreen
    private fun fullScreenExit() {
        // Set background white
        binding.videoConstraint.setBackgroundColor(
                ResourcesCompat.getColor(resources, R.color.white, null))

        // Show views and set exit-fullscreen button as gone
        btnFullExit.visibility = View.GONE
        btnFullEnter.visibility = View.VISIBLE
        binding.scrollView.visibility = View.VISIBLE

        // Request system ui for visibility changes and orientation
        setSystemUI(true)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Maximize layout parameters of player
        binding.playerView.layoutParams = params
    }

    @Suppress("DEPRECATION")
    private fun setSystemUI(boolean: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(boolean)
        } else {
            if (boolean) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            } else {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
            }
        }

    }
}