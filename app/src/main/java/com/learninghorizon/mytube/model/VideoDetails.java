package com.learninghorizon.mytube.model;

import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.ResourceId;

import java.math.BigInteger;


public class VideoDetails {
    private String id;
    private String title;
    private DateTime datePublished;
    private BigInteger numberOfViews;
    private String imageURL;
    private String resourceId;

    public VideoDetails(String id, final String title, final DateTime datePublished, final BigInteger numberOfViews,
                        final String imageURL, final String resourceId){
        this.id = id;
        this.title = title;
        this.datePublished = datePublished;
        this.imageURL = imageURL;
        this.numberOfViews = numberOfViews;
        this.resourceId = resourceId;
    }

    public VideoDetails(){

    }

    public VideoDetails(String id, final String title, final DateTime datePublished, final BigInteger numberOfViews,
    final String imageURL){
        this.id = id;
        this.title = title;
        this.datePublished = datePublished;
        this.numberOfViews = numberOfViews;
        this.imageURL = imageURL;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDatePublished(DateTime datePublished) {
        this.datePublished = datePublished;
    }

    public void setNumberOfViews(BigInteger numberOfViews) {
        this.numberOfViews = numberOfViews;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public DateTime getDatePublished() {
        return datePublished;
    }

    public BigInteger getNumberOfViews() {
        return numberOfViews;
    }

    public void setImageURL(String imageURL){
        this.imageURL = imageURL;
    }

    public String getImageURL(){
        return imageURL;
    }

    public String getResourceId(){
        return resourceId;
    }
}
