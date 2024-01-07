package com.example.urbancycle.Community;

import android.graphics.Bitmap;

import java.io.Serializable;

public class Event implements Serializable {
    private String eventName;
    private String eventOrganizer;
    private String eventLocation;
    private String eventDate;
    private String eventTime;


    public Event(String s, String s1, String s2, String s3) {
    }

    public Event(String eventName, String eventOrganizer, String eventLocation, String eventDate, String eventTime) {
        this.eventName = eventName;
        this.eventOrganizer = eventOrganizer;
        this.eventLocation = eventLocation;
        this.eventDate = eventDate;
        this.eventTime = eventTime;

    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventOrganizer() {
        return eventOrganizer;
    }

    public void setEventOrganizer(String eventOrganizer) {
        this.eventOrganizer = eventOrganizer;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }


}
