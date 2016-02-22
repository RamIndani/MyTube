package com.learninghorizon.mytube.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.learninghorizon.mytube.R;
import com.learninghorizon.mytube.YoutubePlayer;
import com.learninghorizon.mytube.constants.Constants;
import com.learninghorizon.mytube.fragment.Tab1;
import com.learninghorizon.mytube.fragment.Tab2;
import com.learninghorizon.mytube.model.VideoDetails;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class PlayListAdapter extends ArrayAdapter {

    private static final String TAG = "SearchListAdapter";
    private Context mContext;
    private List<VideoDetails> searchResults;
    private static String playListId= null;
    private static String mChosenAccountName = null;
    private static ArrayList<String> videoId = new ArrayList<String>();
    public PlayListAdapter(Context mContext, int resource, List<VideoDetails> searchResults, String playListId, String mChosenAccountName){
        super(mContext, 0, searchResults);
        this.mContext = mContext;
        this.searchResults = searchResults;
        this.playListId = playListId;
        this.mChosenAccountName = mChosenAccountName;
    }

    public ArrayList<String> getVideoId() {
        return videoId;
    }

    public String takePlayListId(){
        return playListId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        final VideoDetails searchResult = searchResults.get(position);
        if(null==convertView){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.play_list_item, parent, false);
        }

        ImageView thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
        TextView title = (TextView) convertView.findViewById(R.id.video_title);
        TextView datePublished = (TextView) convertView.findViewById(R.id.published_date);
        TextView numberOfViews = (TextView) convertView.findViewById(R.id.number_of_views);
        final CheckBox removeFromFavorites = (CheckBox) convertView.findViewById(R.id.remove_from_favorite);
        String photo_url_str = searchResult.getImageURL();
        String newURL = photo_url_str;
        Picasso.with(mContext)
                .load(newURL)
                .into(thumbnail);
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
                youTubeStandalonePlayer.putExtra("videoId",searchResult.getId());
                if(null!=youTubeStandalonePlayer){
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

        removeFromFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!removeFromFavorites.isChecked()){
                    int counter = 0;
                    boolean found = false;
                    for(String id : videoId){
                        if(id.equals(searchResult.getId())){
                            found = true;
                            break;
                        }
                        counter++;
                    }
                    if(found) {
                        videoId.remove(counter);
                        removeFromFavorites.setChecked(false);
                    }
                }else {
                    videoId.add(searchResult.getId());
                    removeFromFavorites.setChecked(true);
                }
                   /* if (null == playListId) {
                        getPlayListId();
                    }
                    if (null != playListId) {
                        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                try {
                                    Tab2.removePlaylistItem(playListId, searchResult.getResourceId());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }
                        };
                        task.execute();*/
                        // if(null!=id){
                        //Toast.makeText(mContext, "Video removed from playList", Toast.LENGTH_LONG).show();
                        //removeFromFavorites.setImageDrawable(mContext.getApplicationContext().getResources().getDrawable(R.drawable.ic_grade_white_24dp));
                        //}


            }
        });

        return convertView;
    }

    public void setPlaylistId(String playlistId) {
        this.playListId = playlistId;
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

    public void getPlayListId() throws ExecutionException, InterruptedException {
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
                } catch (IOException e) {
                    // Utils.logAndShow(getActivity(), Constants.APP_NAME, e);
                    Log.e(TAG, e.getLocalizedMessage());
                    e.printStackTrace();
                }
                return playListId;
            }

        }.execute((Void) null).get();
    }
}
