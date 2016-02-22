package com.learninghorizon.mytube.fragment;


import android.app.Activity;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

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
import com.learninghorizon.mytube.HomeActivity;
import com.learninghorizon.mytube.MainActivity;
import com.learninghorizon.mytube.R;
import com.learninghorizon.mytube.adapter.SearchListAdapter;
import com.learninghorizon.mytube.constants.Constants;
import com.learninghorizon.mytube.model.VideoDetails;
import com.learninghorizon.mytube.util.DataHolder;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;



public class Tab1 extends Fragment implements SwipeRefreshLayout.OnRefreshListener{
    static View v;
    private static final String TAG = Tab1.class.getName();
    GoogleAccountCredential credential;
    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final GsonFactory jsonFactory = new GsonFactory();
    private static final int REQUEST_AUTHORIZATION = 3;
    String mChosenAccountName;
    String ACCOUNT_KEY = "accountName";
    static List<VideoDetails> videoItems = new ArrayList<VideoDetails>();
    static SearchListAdapter searchListAdapter;
    static  YouTube youtube;
    private static String playListId;
    private static DataHolder dataHolder = new DataHolder();
    private static ProgressDialog ringProgressDialog;
    private static String searchQuery = "android";
    SwipeRefreshLayout swipeLayout;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.tab_1, container, false);

        credential = GoogleAccountCredential.usingOAuth2(getContext(), YouTubeScopes.all());
        loadAccount();
        credential.setSelectedAccountName(mChosenAccountName);
       /* try {
            String token = credential.getToken();
        }catch(GoogleAuthException googleAuthException){
            Log.e(TAG, "Error getting token", googleAuthException);
            googleAuthException.printStackTrace();
        }catch(IOException ioException){
            Log.e(TAG,"IOError in getting token",ioException);
            ioException.printStackTrace();
        }*/
        //credential.setSelectedAccountName("ram.0737@gmail.com");
        loadUploadedVideos(searchQuery, false);
        ImageView addToFavorite = (ImageView) v.findViewById(R.id.add_to_favorite);
        ringProgressDialog = ProgressDialog.show(getActivity(), "Please wait ...", "Loading Videos ...", true);
        ringProgressDialog.show();
        swipeLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        dataHolder.clearList();
        return v;
    }

    private void loadAccount() {
        mChosenAccountName = getArguments().getString(ACCOUNT_KEY);
    }

    public void setSearchQuery(String query, Context context, final Activity activity){
        try {
           activity.runOnUiThread(new Runnable() {
               public void run() {
                   ringProgressDialog = ProgressDialog.show(activity, "Please wait ...", "Loading Videos ...", true);
                   ringProgressDialog.show();
               }
           });
        }catch(Exception e){
           e.printStackTrace();
        }
        searchQuery = query;
        videoItems.clear();
        credential = GoogleAccountCredential.usingOAuth2(context, YouTubeScopes.all());
        loadAccount();
        credential.setSelectedAccountName(mChosenAccountName);
        //ringProgressDialog = ProgressDialog.show((Activity)context, "Please wait ...", "Loading Results ...", true);
        loadUploadedVideos(query, true);
       // updateList();
    }


    private void loadUploadedVideos(final String searchQuery, final boolean notifyDataSetChange) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {


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

                        Playlist requiredPlayList = null;
                        for (Playlist playListItem : playlist) {
                            if (playListItem.getSnippet().getTitle().equals("SJSU-CMPE-277")) {
                                requiredPlayList = playListItem;
                                playListId = playListItem.getId();
                                break;
                            }
                        }

                        if (null == requiredPlayList) {
                            //youtube.playlists().update()
                            playListId = createPlaylist(youtube);
                            //TODO: create playlist here
                        }

                    if(videoItems.isEmpty()) {
                   // Get videos from user's upload playlist with a playlist
                    // items list request
                    PlaylistItemListResponse pilr = youtube.playlistItems()
                            .list("id,contentDetails,snippet")
                            .setPlaylistId(playlist.get(0).getId())
                            .setMaxResults(20l).execute();
                    List<String> videoIds = new ArrayList<String>();

                    // Iterate over playlist item list response to get uploaded
                    // videos' ids.
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

                        VideoDetails videoDetails = new VideoDetails(item.getId(),item.getSnippet().getTitle(), item.getSnippet().getPublishedAt(), youtube.videos().list("id, statistics").setId(item.getId()).setMaxResults(1l).execute().getItems().get(0).getStatistics().getViewCount(),
                                item.getSnippet().getThumbnails().getDefault().getUrl(),item.getId());
                        dataHolder.setPlayListElements(videoDetails);
                        Log.d(TAG, item.getSnippet().getTitle());
                    }


                        SearchListResponse searchResult = youtube.search().list("snippet").setQ(searchQuery).setType("video")
                                .setMaxResults(10l).execute();

                        List<SearchResult> items = searchResult.getItems();
                        for (SearchResult item : items) {
                            VideoDetails videoDetails = new VideoDetails(item.getId().getVideoId(), item.getSnippet().getTitle(), item.getSnippet().getPublishedAt(), youtube.videos().list("id, statistics").setId(item.getId().getVideoId()).setMaxResults(1l).execute().getItems().get(0).getStatistics().getViewCount(),
                                    item.getSnippet().getThumbnails().getDefault().getUrl());
                            videoItems.add(videoDetails);
                        }
                    }
                    return null;

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
                    //Toast.makeText(getContext(), "Error while performing the operation", Toast.LENGTH_LONG).show();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void returnvalue) {



                try {
                    if(!notifyDataSetChange) {
                        searchVideo();
                    }else{
                        ListView listView = (ListView) v.findViewById(R.id.search_list);
                        if(null == searchListAdapter){
                            searchListAdapter = (SearchListAdapter)listView.getAdapter();
                        }
                        searchListAdapter.notifyDataSetChanged();
                    }
                    if(null!=ringProgressDialog) {
                        ringProgressDialog.dismiss();
                    }
                }catch(IOException ioException){
                    Log.e(TAG, "error while printing search records", ioException);
                }
                if(null!=swipeLayout){
                    swipeLayout.setRefreshing(false);
                }

                //mUploadsListFragment.setVideos(videos);

                //set videos here
            }

        }.execute((Void) null);
    }

    private void searchVideo() throws IOException {
        ListView listView = (ListView) v.findViewById(R.id.search_list);
        searchListAdapter = new SearchListAdapter(getContext(), 0, videoItems,playListId, youtube,mChosenAccountName);
        listView.setAdapter(searchListAdapter);
       // searchListAdapter.notifyDataSetChanged();
       /* for (VideoDetails item : videoItems) {
            //Log.e(TAG, youtube.videos().list("id, snippet, contentDetails, statistics, status, topicDetails").setId(item.getId()).setMaxResults(10l).execute().getItems().toString());
        }*/

    }
    private void updateList(){
        searchListAdapter.notifyDataSetChanged();
    }


    public static String insertPlaylistItem(String playlistId, String videoId) throws IOException {

        // Define a resourceId that identifies the video being added to the
        // playlist.
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
                youtube.playlistItems().insert("snippet,contentDetails", playlistItem);
        PlaylistItem returnedPlaylistItem = playlistItemsInsertCommand.execute();
        // Print data from the API response and return the new playlist
        // item's unique playlistItem ID.
        VideoDetails videoDetails = new VideoDetails(videoId,returnedPlaylistItem.getSnippet().getTitle(),returnedPlaylistItem.getSnippet().getPublishedAt(),new BigInteger("0"),returnedPlaylistItem.getSnippet().getThumbnails().getDefault().getUrl(),returnedPlaylistItem.getSnippet().getResourceId().getVideoId());
        dataHolder.setPlayListElements(videoDetails);
           // Tab2 tab2 = (Tab2) HomeActivity.tabPagerAdapter.getItem(1);
            //tab2.refresh();

        System.out.println("New PlaylistItem name: " + returnedPlaylistItem.getSnippet().getTitle());
        System.out.println(" - Video id: " + returnedPlaylistItem.getSnippet().getResourceId().getVideoId());
        System.out.println(" - Posted: " + returnedPlaylistItem.getSnippet().getPublishedAt());
        System.out.println(" - Channel: " + returnedPlaylistItem.getSnippet().getChannelId());
        return returnedPlaylistItem.getId();

    }

    private String createPlaylist(YouTube youtube) throws IOException{
        PlaylistSnippet playlistSnippet = new PlaylistSnippet();
        playlistSnippet.setTitle(getActivity().getString(R.string.playlist_name));
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

    @Override
    public void onRefresh() {
        if(null == dataHolder) {
            dataHolder = new DataHolder();
        }
        dataHolder.clearList();
        setSearchQuery(searchQuery, getContext(), getActivity());

    }
}
