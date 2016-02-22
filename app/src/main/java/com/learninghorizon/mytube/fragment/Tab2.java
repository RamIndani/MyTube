package com.learninghorizon.mytube.fragment;



import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
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
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;

import com.google.api.services.youtube.model.VideoListResponse;
import com.learninghorizon.mytube.R;
import com.learninghorizon.mytube.adapter.PlayListAdapter;
import com.learninghorizon.mytube.adapter.SearchListAdapter;
import com.learninghorizon.mytube.constants.Constants;
import com.learninghorizon.mytube.model.VideoDetails;
import com.learninghorizon.mytube.util.DataHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;



public class Tab2 extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    View v;
    private static final String TAG = Tab2.class.getName();
    private static GoogleAccountCredential credential;
    private final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    private final GsonFactory jsonFactory = new GsonFactory();
    private static final int REQUEST_AUTHORIZATION = 3;
    String mChosenAccountName;
    private static String accountName;
    String ACCOUNT_KEY = "accountName";
    private static DataHolder dataHolder = new DataHolder();
    private List<VideoDetails> videoItems = new ArrayList<>();
    static YouTube youtube;
    private static String playListId;
    private static Context mContext;
    PlayListAdapter playListAdapter;


    private static ProgressDialog ringProgressDialog;
    SwipeRefreshLayout swipeLayout;


    @Override
    public void onCreate(Bundle savedInstaanceState) {
        super.onCreate(savedInstaanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mContext = getContext();
        v = inflater.inflate(R.layout.tab_2, container, false);
        credential = GoogleAccountCredential.usingOAuth2(getContext(), YouTubeScopes.all());
        loadAccount();
        credential.setSelectedAccountName(mChosenAccountName);
        //credential.setSelectedAccountName("ram.0737@gmail.com");

        loadUploadedVideos();

        ringProgressDialog = ProgressDialog.show(getActivity(), "Please wait ...", "Loading Videos ...", true);
        ringProgressDialog.show();
        swipeLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        dataHolder.clearList();
        return v;
    }

    private void loadAccount() {
        mChosenAccountName = getArguments().getString(ACCOUNT_KEY);
        accountName = mChosenAccountName;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(null!=item){
        switch (item.getItemId()) {
            case R.id.action_delete: {

                try {

                    AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            try {
                                if (null == playListAdapter) {
                                   // Toast.makeText(getContext(), "Unable to remove videos", Toast.LENGTH_LONG).show();
                                    return null;
                                }
                                if (null != playListAdapter) {
                                    if (playListAdapter.getVideoId().isEmpty()) {
                                       // Toast.makeText(getContext(), "Select Videos to remove", Toast.LENGTH_LONG).show();
                                        return null;
                                    }
                                }

                                final ArrayList<String> videoId = playListAdapter.getVideoId();
                                final String playListId = playListAdapter.takePlayListId();

                                removePlaylistItem(playListId, videoId);
                                videoId.clear();
                                loadUploadedVideos();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        public void onPostExecute(Void returnValue) {
                            playListAdapter.notifyDataSetChanged();
                        }
                    };
                    task.execute();
                    // if(null!=id){
                    Toast.makeText(mContext, "Videos removed from playList", Toast.LENGTH_LONG).show();

                    //removeFromFavorites.setImageDrawable(mContext.getApplicationContext().getResources().getDrawable(R.drawable.ic_grade_white_24dp));
                    //}

                } catch (Exception exception) {
                    Log.e(TAG, "Unable to remove from playlist");
                    exception.printStackTrace();
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
return false;
}
    private void loadUploadedVideos() {
        videoItems.clear();

        new AsyncTask<Void, Void, List<VideoDetails>>() {
            @Override
            protected List<VideoDetails> doInBackground(Void... voids) {
             playListId= null;

                 youtube = new YouTube.Builder(transport, jsonFactory,
                        credential).setApplicationName(Constants.APP_NAME)
                        .build();

                try {
                    /*
					 * Now that the user is authenticated, the app makes a
					 * channels list request to get the authenticated user's
					 * channel. Returned with that data is the playlist id for
					 * the uploaded videos.
					 * https://developers.google.com/youtube
					 * /v3/docs/channels/list
					 */
                    PlaylistListResponse playlistListResponse = youtube.playlists().list("id,contentDetails,snippet").setMine(true).execute();
                    // Get the user's uploads playlist's id from channel list
                    // response
                    List<Playlist> playlist = playlistListResponse.getItems();

                    List<VideoDetails> videos = new ArrayList<VideoDetails>();
                    // Get videos from user's upload playlist with a playlist
                    // items list request
                    Playlist requiredPlayList = null;
                    for(Playlist playListItem : playlist){
                        if(playListItem.getSnippet().getTitle().equals(getActivity().getResources().getString(R.string.playlist_name))){
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

                    // Get videos from user's upload playlist with a playlist
                    // items list request
                    PlaylistItemListResponse pilr = youtube.playlistItems()
                            .list("id,contentDetails,snippet")
                            .setPlaylistId(playListId)
                            .setMaxResults(20l).execute();
                    List<String> videoIds = new ArrayList<String>();

                    // Iterate over playlist item list response to get uploaded
                    // videos' ids.
                    ResourceId resourceId;
                    for (PlaylistItem item : pilr.getItems()) {
                        videoIds.add(item.getContentDetails().getVideoId());

                    }

                    // Get details of uploaded videos with a videos list
                    // request.
                    VideoListResponse vlr = youtube.videos()
                            .list("id,snippet,status")
                            .setId(TextUtils.join(",", videoIds)).execute();

                    // Add only the public videos to the local videos list.
                    for (Video item : vlr.getItems()) {
                        /*if ("public".equals(item.getStatus()
                                .getPrivacyStatus())) {*/

                                VideoDetails videoDetails = new VideoDetails(item.getId(),item.getSnippet().getTitle(), item.getSnippet().getPublishedAt(), youtube.videos().list("id, statistics").setId(item.getId()).setMaxResults(1l).execute().getItems().get(0).getStatistics().getViewCount(),
                                        item.getSnippet().getThumbnails().getDefault().getUrl(),item.getId());
                                videoItems.add(videoDetails);
                                dataHolder.setPlayListElements(videoDetails);
                            //insertPlaylistItem("PLNg5eDYOUfdlfEfGGz4SApE_p07060JiL","MBx-R2RsFdU",youtube);
                        //}
                        Log.d(TAG, item.getSnippet().getTitle());
                    }

                    return videos;

                } catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
                 /*   showGooglePlayServicesAvailabilityErrorDialog(availabilityException
                            .getConnectionStatusCode());*/
                } catch (UserRecoverableAuthIOException userRecoverableException) {
                    startActivityForResult(
                            userRecoverableException.getIntent(),
                            REQUEST_AUTHORIZATION);
                } catch (IOException e) {
                    // Utils.logAndShow(getActivity(), Constants.APP_NAME, e);
                    Log.e(TAG, e.getLocalizedMessage());
                    e.printStackTrace();
                }catch(Exception exception){
                    if(null!=ringProgressDialog){
                        ringProgressDialog.dismiss();
                    }
                    Toast.makeText(getContext(), "Error while performing the operation", Toast.LENGTH_LONG).show();
                }
                return null;
            }

            @Override
            protected void onPostExecute(List<VideoDetails> videos) {
                try {
                    displayPlayList();
                    if(null!=swipeLayout){
                        swipeLayout.setRefreshing(false);
                    }
                    if(null!=ringProgressDialog){
                        ringProgressDialog.dismiss();
                    }
                }catch(IOException ioException){
                    Log.e(TAG, "error while printing search records", ioException);
                }
                //mUploadsListFragment.setVideos(videos);

                //set videos here
            }

        }.execute((Void) null);
    }

    private void displayPlayList() throws IOException {
        ListView listView = (ListView) v.findViewById(R.id.play_list);

        playListAdapter = new PlayListAdapter(mContext, 0, videoItems, playListId, mChosenAccountName);
        listView.setAdapter(playListAdapter);
        listView.setAdapter(playListAdapter);
        if(null!=ringProgressDialog) {
            ringProgressDialog.dismiss();
        }
       /* for (VideoDetails item : videoItems) {
            //Log.e(TAG, youtube.videos().list("id, snippet, contentDetails, statistics, status, topicDetails").setId(item.getId()).setMaxResults(10l).execute().getItems().toString());
        }*/
    }


    private String createPlaylist(YouTube youtube) throws IOException{
        PlaylistSnippet playlistSnippet = new PlaylistSnippet();
        playlistSnippet.setTitle(getString(R.string.playlist_name));
        playlistSnippet.setDescription("A playlist created for CMPE 277");
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

    public void removePlaylistItem(String playlistId, ArrayList<String> videoIdList) throws IOException {
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(mContext, YouTubeScopes.all());
        credential.setSelectedAccountName(accountName);
        final HttpTransport transport = AndroidHttp.newCompatibleTransport();
        final GsonFactory jsonFactory = new GsonFactory();




        List<PlaylistItem> playlistItemList = new ArrayList<PlaylistItem>();

        // Retrieve the playlist of the channel's uploaded videos.
        YouTube.PlaylistItems.List playlistItemRequest =
                youtube.playlistItems().list("id,contentDetails,snippet");
        playlistItemRequest.setPlaylistId(playlistId);

        // Only retrieve data used in this application, thereby making
        // the application more efficient. See:
        // https://developers.google.com/youtube/v3/getting-started#partial
        String nextToken = "";

        // Call the API one or more times to retrieve all items in the
        // list. As long as the API response returns a nextPageToken,
        // there are still more items to retrieve.
        String playlistitemId = null;
        do {
            playlistItemRequest.setPageToken(nextToken);
            PlaylistItemListResponse playlistItemResult = playlistItemRequest.execute();

            playlistItemList.addAll(playlistItemResult.getItems());

            nextToken = playlistItemResult.getNextPageToken();
        } while (nextToken != null);



        for(String videoId : videoIdList) {
            for (PlaylistItem playlistItem : playlistItemList) {
                String playlistVideoId = playlistItem.getSnippet().getResourceId().getVideoId();
                Log.e(TAG, "PLAYLISTITEM ID : " + playlistItem.getId() + " PLAYLIST VIDEO ID : " + playlistVideoId);
                if (playlistVideoId.equals(videoId)) {
                    playlistitemId = playlistItem.getId();
                    Log.e(TAG, "playListItem matched :" + playlistitemId);
                    YouTube.PlaylistItems playlistItems = youtube.playlistItems();

                    YouTube.PlaylistItems.Delete playlistItemsDeleteCommand =
                            playlistItems.delete(playlistitemId);

                    playlistItemsDeleteCommand.execute();
                    dataHolder.removeFromPlayListElements(videoId);
                    videoItems.remove(videoId);
                   // playListAdapter.notifyDataSetChanged();
                    break;
                }
            }
        }
       /* YouTube youtube = new YouTube.Builder(transport, jsonFactory,
                credential).setApplicationName(Constants.APP_NAME)
                .build();*/

    }

    public void refresh() {
        if(dataHolder.getPlayList().size() != videoItems.size()) {
            loadUploadedVideos();
        }
    }

    @Override
    public void onRefresh() {
        try {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    ringProgressDialog = ProgressDialog.show(getActivity(), "Please wait ...", "Loading Videos ...", true);
                    ringProgressDialog.show();
                }
            });
        }catch(Exception e){
            Log.e("error loading reload icon", e.getLocalizedMessage());
            e.printStackTrace();
        }
        loadUploadedVideos();
    }
}
