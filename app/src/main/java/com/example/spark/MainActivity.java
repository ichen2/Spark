package com.example.spark;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Image;
import com.spotify.protocol.types.Track;

public class MainActivity extends AppCompatActivity {

    private static final String CLIENT_ID = "343c7425946a4130b0aed8bd56091458";
    private static final String REDIRECT_URI = "spark://callback";
    private SpotifyAppRemote mSpotifyAppRemote;

    private TextView mSongNameText;
    private ImageView mAlbumCoverImage;
    private TextView mArtistNameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSongNameText = (TextView) findViewById(R.id.tv_song_name);
        mAlbumCoverImage = (ImageView) findViewById(R.id.iv_album_cover);
        mArtistNameText = (TextView) findViewById(R.id.tv_artist_name);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Set the connection parameters
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();
        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("MainActivity", "Connected! Yay!");

                        // Now you can start interacting with App Remote
                        connected();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e("MainActivity", throwable.getMessage(), throwable);

                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });
    }

    private void connected() {
        mSpotifyAppRemote.getPlayerApi().play("spotify:playlist:37i9dQZF1E4zUdHmpg9jpG");
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track != null) {
                        Log.d("MainActivity", track.name + " by " + track.artist.name);
                        mSongNameText.setText(track.name);
                        mArtistNameText.setText(track.artist.name);
                        mSpotifyAppRemote
                                .getImagesApi()
                                .getImage(playerState.track.imageUri, Image.Dimension.LARGE)
                                .setResultCallback(
                                        bitmap -> {
                                            mAlbumCoverImage.setImageBitmap(bitmap);
                                            mAlbumCoverImage.setVisibility(View.VISIBLE);
                                        });
                        //Glide.with(this).load(track.imageUri).into(mAlbumCoverImage);
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }
}