package com.learninghorizon.mytube.util;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.services.youtube.model.Video;
import com.learninghorizon.mytube.model.VideoDetails;

import java.util.ArrayList;
import java.util.HashMap;

public class DataHolder {

    private static GoogleApiClient mGoogleApiClient;
    private static HashMap<String, VideoDetails> playListElements = new HashMap<String, VideoDetails>();
    private final static ArrayList<VideoDetails> playList = new ArrayList<VideoDetails>();

    public static void setmGoogleApiClient(final GoogleApiClient googleApiClient){
        mGoogleApiClient = googleApiClient;
    }

    public static GoogleApiClient getmGoogleApiClient(){
        return mGoogleApiClient;
    }

    public void setPlayListElements(VideoDetails videoDetails){
        playListElements.put(videoDetails.getId(), videoDetails);
    }

    public VideoDetails getPlayListElement(String videoId){
        return playListElements.get(videoId);
    }

    public void removeFromPlayListElements(String videoId) {
        playListElements.remove(videoId);
    }

    public ArrayList<VideoDetails> getAllPlayListElements(){
        playList.clear();
        for(VideoDetails videoDetails : playListElements.values()){
            playList.add(videoDetails);
        }
        return playList;
    }

    public ArrayList<VideoDetails> getPlayList(){
        return playList;
    }

    public void clearList() {
        playList.clear();
    }
}
