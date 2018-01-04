package com.wlu.android.khan_fark_project;

import java.util.ArrayList;
import java.util.UUID;

public class Card {

    private UUID mId;
    private String mImage;

    private String mQuestion;
    private String mAnswer;
    private String mTitle;
    private double mLongitude;
    private double mLatitude;
    private String mUniversity;
    private String link;
    private String mDescription;

    public Card() {
        this(UUID.randomUUID());
    }

    public Card(UUID id) {
        mId = id;
    }

    public String getImage(){
        return mImage;
    }

    public void setImage(String givenimage) {
        mImage = givenimage;
    }

    public String getQuestion(){return mQuestion; }

    public void setQuestion(String givenQuestion) {
        mQuestion = givenQuestion;
    }

    public String getAnswer(){return mAnswer; }
    public void setAnswer(String givenAnswer) {
        mAnswer = givenAnswer;
    }

    public UUID getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }


    public void setTitle(String title) {
        mTitle = title;
    }

    public double getLongitude(){
        return mLongitude;
    }
    public void setLongitude(double longitude){
        mLongitude=longitude;
    }

    public double getLatitude(){
        return mLatitude;
    }
    public void setLatitude(double latitude){
        mLatitude=latitude;
    }



    public String getDescription(){
        return mDescription;
    }



    public void setDescription(String description){
        mDescription=description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String l) {
        link = l;
    }


}
