package com.learninghorizon.mytube.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.PlaylistSnippet;
import com.google.api.services.youtube.model.PlaylistStatus;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.learninghorizon.mytube.R;
import com.learninghorizon.mytube.YoutubePlayer;
import com.learninghorizon.mytube.constants.Constants;
import com.learninghorizon.mytube.fragment.Tab1;
import com.learninghorizon.mytube.model.VideoDetails;
import com.learninghorizon.mytube.util.DataHolder;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class SearchListAdapter extends ArrayAdapter {

    private static final String TAG = "SearchListAdapter";
    private static final String YOUTUBE_API_KEY = "AIzaSyCQKtQS8TvVPYHkEB8Q2ewNzZOWyCU0myc";
    private static Context mContext;
    private List<VideoDetails> searchResults;
    private DataHolder dataHolder = new DataHolder();

    private static String playListId= null;
    private static String mChosenAccountName = null;
    public SearchListAdapter(Context mContext, int resource, List<VideoDetails> searchResults, final String playListId, final YouTube youtube, final String mChosenAccountName){
        super(mContext, 0, searchResults);
        this.mContext = mContext;
        this.searchResults = searchResults;
        this.playListId = playListId;
        this.mChosenAccountName = mChosenAccountName;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (searchResults.size() >= position) {
            final VideoDetails searchResult = searchResults.get(position);
            if (null == convertView) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.search_list_item, parent, false);
            }

            ImageView thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
            TextView title = (TextView) convertView.findViewById(R.id.video_title);
            TextView datePublished = (TextView) convertView.findViewById(R.id.published_date);
            TextView numberOfViews = (TextView) convertView.findViewById(R.id.number_of_views);
            final ImageButton addToFavorites = (ImageButton) convertView.findViewById(R.id.add_to_favorite);
            if (null != dataHolder.getPlayListElement(searchResult.getId())) {
                addToFavorites.setImageDrawable(mContext.getApplicationContext().getResources().getDrawable(R.drawable.ic_grade_black_24dp));
            } else {
                addToFavorites.setImageDrawable(mContext.getApplicationContext().getResources().getDrawable(R.drawable.ic_grade_white_24dp));
            }
            String photo_url_str = searchResult.getImageURL();
            String newURL = photo_url_str;
            try {
                Picasso.with(mContext)
                        .load(newURL)
                        .placeholder(R.drawable.ic_launcher)
                        .into(thumbnail);
            } catch (Exception exception) {
                Log.e(TAG, "unable to download image of video", exception);
                exception.printStackTrace();
            }
            title.setText(searchResult.getTitle());
            DateTime videoPublishedDate = searchResult.getDatePublished();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM dd, yyyy");
            datePublished.setText(simpleDateFormat.format(videoPublishedDate.getValue()));
            numberOfViews.setText(String.valueOf(searchResult.getNumberOfViews()).concat(" Views"));
            LinearLayout linearLayout = (LinearLayout) title.getParent();
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent youTubeStandalonePlayer = new Intent(mContext, YoutubePlayer.class);
                    youTubeStandalonePlayer.putExtra("videoId", searchResult.getId());
                    if (null != youTubeStandalonePlayer) {
                        mContext.startActivity(youTubeStandalonePlayer);
                    }
                }
            });
            thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Intent youTubeStandalonePlayer = YouTubeStandalonePlayer.createPlaylistIntent((Activity)mContext, YOUTUBE_API_KEY, searchResult.getId());
                    Intent youTubeStandalonePlayer = new Intent(mContext, YoutubePlayer.class);
                    youTubeStandalonePlayer.putExtra("videoId", searchResult.getId());
                    if (null != youTubeStandalonePlayer) {
                        mContext.startActivity(youTubeStandalonePlayer);
                    }
                }
            });
            addToFavorites.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        if (null == playListId) {
                            //getPlayListId();
                            Toast.makeText(getContext(), "Unable to get your playlist", Toast.LENGTH_LONG).show();
                        }
                        if (null != dataHolder.getPlayListElement(searchResult.getId())) {
                            Toast.makeText(getContext(), "Already present in the playlist", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (null != playListId) {
                            AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected Void doInBackground(Void... params) {
                                    try {
                                        Tab1.insertPlaylistItem(playListId, searchResult.getId());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    return null;
                                }
                            };
                            task.execute();
                            // if(null!=id){
                            Toast.makeText(mContext, "Video added to playList", Toast.LENGTH_LONG).show();
                            addToFavorites.setImageDrawable(mContext.getApplicationContext().getResources().getDrawable(R.drawable.ic_grade_black_24dp));
                            //}
                        } else {
                            Toast.makeText(mContext, "Unable to create playlist", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception exception) {
                        Log.e(TAG, "Unable to insert into playlist");
                        exception.printStackTrace();
                    }
                }
            });
        }
            return convertView;

    }
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    private static void insertPlaylistItem(final String playlistId, final String videoId) throws IOException, ExecutionException, InterruptedException {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
        // Define a resourceId that identifies the video being added to the
        // playlist.
                final HttpTransport transport = AndroidHttp.newCompatibleTransport();
                final GsonFactory jsonFactory = new GsonFactory();
                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(mContext, YouTubeScopes.all());

                credential.setSelectedAccountName(mChosenAccountName);
                YouTube youtube = new YouTube.Builder(transport, jsonFactory,
                        credential).setApplicationName(Constants.APP_NAME)
                        .build();
        ResourceId resourceId = new ResourceId();
        resourceId.setKind("youtube#video");
        resourceId.setVideoId(videoId);

        // Set fields included in the playlistItem resource's "snippet" part.
        PlaylistItemSnippet playlistItemSnippet = new PlaylistItemSnippet();
        playlistItemSnippet.setTitle("First video in the test playlist");
        playlistItemSnippet.setPlaylistId(playlistId);
        playlistItemSnippet.setResourceId(resourceId);

        // Create the playlistItem resource and set its snippet to the
        // object created above.
        PlaylistItem playlistItem = new PlaylistItem();
        playlistItem.setSnippet(playlistItemSnippet);

        // Call the API to add the playlist item to the specified playlist.
        // In the API call, the first argument identifies the resource parts
        // that the API response should contain, and the second argument is
        // the playlist item being inserted.
                YouTube.PlaylistItems.Insert playlistItemsInsertCommand =
                        null;
                try {
                    playlistItemsInsertCommand = youtube.playlistItems().insert("snippet,contentDetails", playlistItem);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                PlaylistItem returnedPlaylistItem = null;
                try {
                    returnedPlaylistItem = playlistItemsInsertCommand.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Print data from the API response and return the new playlist
        // item's unique playlistItem ID.

        System.out.println("New PlaylistItem name: " + returnedPlaylistItem.getSnippet().getTitle());
        System.out.println(" - Video id: " + returnedPlaylistItem.getSnippet().getResourceId().getVideoId());
        System.out.println(" - Posted: " + returnedPlaylistItem.getSnippet().getPublishedAt());
        System.out.println(" - Channel: " + returnedPlaylistItem.getSnippet().getChannelId());
        return returnedPlaylistItem.getId();
            }

        }.execute((Void) null).get();
    }

    private void getPlayListId() throws ExecutionException, InterruptedException {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {

                try {
                   GoogleAccountCredential credential;
                    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
                    final GsonFactory jsonFactory = new GsonFactory();
                    credential = GoogleAccountCredential.usingOAuth2(mContext, YouTubeScopes.all());
                    credential.setSelectedAccountName(mChosenAccountName);
                    YouTube youtube = new YouTube.Builder(transport, jsonFactory,
                            credential).setApplicationName(Constants.APP_NAME)
                            .build();

                    PlaylistListResponse playlistListResponse = youtube.playlists().list("id,snippet").setMine(true).execute();

                    // Get the user's uploads playlist's id from channel list
                    // response
                    List<Playlist> playlist = playlistListResponse.getItems();

                    List<VideoDetails> videos = new ArrayList<VideoDetails>();

                    // Get videos from user's upload playlist with a playlist
                    // items list request
                    Playlist requiredPlayList = null;
                    for(Playlist playListItem : playlist){
                        if(playListItem.getSnippet().getTitle().equals(mContext.getResources().getString(R.string.playlist_name))){
                            requiredPlayList = playListItem;
                            playListId = playListItem.getId();
                            break;
                        }
                    }

                    if(null == requiredPlayList){
                        //youtube.playlists().update()
                        playListId = createPlaylist(youtube);
                        //TODO: create playlist here
                    }
                } catch (IOException e) {
                    // Utils.logAndShow(getActivity(), Constants.APP_NAME, e);
                    Log.e(TAG, e.getLocalizedMessage());
                    e.printStackTrace();
                }
                return playListId;
            }

        }.execute((Void) null).get();
    }

    private String createPlaylist(YouTube youtube) throws IOException{
        PlaylistSnippet playlistSnippet = new PlaylistSnippet();
        playlistSnippet.setTitle(mContext.getString(R.string.playlist_name));
        playlistSnippet.setDescription("A private playlist created for CMPE 277");
        PlaylistStatus playlistStatus = new PlaylistStatus();
        playlistStatus.setPrivacyStatus("public");

        Playlist youTubePlaylist = new Playlist();
        youTubePlaylist.setSnippet(playlistSnippet);
        youTubePlaylist.setStatus(playlistStatus);

        // Call the API to insert the new playlist. In the API call, the first
        // argument identifies the resource parts that the API response should
        // contain, and the second argument is the playlist being inserted.
        YouTube.Playlists.Insert playlistInsertCommand =
                youtube.playlists().insert("snippet,status", youTubePlaylist);
        Playlist playlistInserted = playlistInsertCommand.execute();

        // Print data from the API response and return the new playlist's
        // unique playlist ID.
        System.out.println("New Playlist name: " + playlistInserted.getSnippet().getTitle());
        System.out.println(" - Privacy: " + playlistInserted.getStatus().getPrivacyStatus());
        System.out.println(" - Description: " + playlistInserted.getSnippet().getDescription());
        System.out.println(" - Posted: " + playlistInserted.getSnippet().getPublishedAt());
        System.out.println(" - Channel: " + playlistInserted.getSnippet().getChannelId() + "\n");
        return playlistInserted.getId();

    }
}
