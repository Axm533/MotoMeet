package com.example.motomeet.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class MeetingModel {

    private String meetingName, meetingDescription, meetingDate, id, uid, imageUrl;
    @ServerTimestamp
    private Date timestamp;

    public MeetingModel(){}

    public MeetingModel(String meetingName, String meetingDescription, String meetingDate, String id, String uid, String imageUrl) {
        this.meetingName = meetingName;
        this.meetingDescription = meetingDescription;
        this.meetingDate = meetingDate;
        this.id = id;
        this.uid = uid;
        this.imageUrl = imageUrl;
    }

    public String getMeetingName() {
        return meetingName;
    }

    public void setMeetingName(String meetingName) {
        this.meetingName = meetingName;
    }

    public String getMeetingDate() {
        return meetingDate;
    }

    public void setMeetingDate(String meetingDate) {
        this.meetingDate = meetingDate;
    }
    public String getMeetingDescription() {
        return meetingDescription;
    }

    public void setMeetingDescription(String meetingDescription) {
        this.meetingDescription = meetingDescription;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
