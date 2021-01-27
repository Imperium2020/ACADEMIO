package com.imperium.academio;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.util.Util;
import com.imperium.academio.databinding.ActivityMaterialVideoViewBinding;

import static android.view.ViewGroup.LayoutParams;

public class MaterialVideoView extends AppCompatActivity {
    private ActivityMaterialVideoViewBinding binding;
    private SimpleExoPlayer player;
    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;
    private Uri uri;
    private ImageButton btnFullEnter;
    private ImageButton btnFullExit;
    private LayoutParams params;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil
                .setContentView(this, R.layout.activity_material_video_view);

        // Get data from intent
        Bundle args = getIntent().getExtras();
        uri = Uri.parse(args.getString("uriString"));
        String title = args.getString("title");
        String text = args.getString("text", "No description provided");
        if (uri == null || title == null) return;

        // Set title and description
        binding.videoTitle.setText(title);
        binding.videoText.setText(text);

        // Fullscreen button
        btnFullEnter = binding.playerView.findViewById(R.id.exo_fullscreen_enter);
        btnFullEnter.setOnClickListener(view -> fullScreenEnter());
        btnFullEnter.setVisibility(View.VISIBLE);

        // Release Fullscreen button
        btnFullExit = binding.playerView.findViewById(R.id.exo_fullscreen_exit);
        btnFullExit.setOnClickListener(view -> fullScreenExit());
        btnFullExit.setVisibility(View.GONE);
    }


    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Hide system ui
        binding.playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE |
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    private void initializePlayer() {
        player = new SimpleExoPlayer.Builder(this).build();
        params = binding.playerView.getLayoutParams();
        binding.playerView.setPlayer(player);

        // Prepare media item
        MediaItem mediaItem = MediaItem.fromUri(uri);
        player.setMediaItem(mediaItem);

        // Prepare player state
        player.setPlayWhenReady(playWhenReady);
        player.seekTo(currentWindow, playbackPosition);
        player.prepare();
    }

    // Remove player when paused or stopped
    private void releasePlayer() {
        if (player == null) return;
        playbackPosition = player.getCurrentPosition();
        currentWindow = player.getCurrentWindowIndex();
        playWhenReady = player.getPlayWhenReady();
        player.release();
        player = null;
    }

    // Set Fullscreen
    private void fullScreenEnter() {
        // Set background black
        binding.videoConstraint.setBackgroundColor(
                ResourcesCompat.getColor(getResources(), R.color.black, null));

        // Hide other views and set exit-fullscreen button as visible
        btnFullExit.setVisibility(View.VISIBLE);
        btnFullEnter.setVisibility(View.GONE);
        binding.scrollView.setVisibility(View.GONE);

        // Request system ui for visibility changes and orientation
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Maximize layout parameters of player
        ViewGroup.LayoutParams p = binding.playerView.getLayoutParams();
        p.width = LayoutParams.MATCH_PARENT;
        p.height = LayoutParams.MATCH_PARENT;
        binding.playerView.setLayoutParams(p);
    }

    // Release Fullscreen
    private void fullScreenExit() {
        // Set background white
        binding.videoConstraint.setBackgroundColor(
                ResourcesCompat.getColor(getResources(), R.color.white, null));

        // Show views and set exit-fullscreen button as gone
        btnFullExit.setVisibility(View.GONE);
        btnFullEnter.setVisibility(View.VISIBLE);
        binding.scrollView.setVisibility(View.VISIBLE);

        // Request system ui for visibility changes and orientation
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Maximize layout parameters of player
        binding.playerView.setLayoutParams(params);
    }
}
