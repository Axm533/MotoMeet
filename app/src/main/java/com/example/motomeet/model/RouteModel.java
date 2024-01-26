package com.example.motomeet.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class RouteModel {
    private String routeName, routeDescription, routeLength, id, uid, imageUrl;

    private Double startLat, startLong, endLat, endLong;

    @ServerTimestamp
    private Date timestamp;

    public RouteModel(){}

    public RouteModel(String routeName, String routeDescription, String routeLength, Double startLat, Double startLong, Double endLat, Double endLong, String id, String uid, String imageUrl) {
        this.routeName = routeName;
        this.routeDescription = routeDescription;
        this.routeLength = routeLength;
        this.id = id;
        this.uid = uid;
        this.imageUrl = imageUrl;
        this.startLat = startLat;
        this.startLong = startLong;
        this.endLat = endLat;
        this.endLong = endLong;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }
    public String getRouteDescription() {
        return routeDescription;
    }

    public void setRouteDescription(String routeDescription) {
        this.routeDescription = routeDescription;
    }
    public String getRouteLength() {
        return routeLength;
    }

    public void setRouteLength(String routeLength) {
        this.routeLength = routeLength;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Double getStartLat() {
        return startLat;
    }

    public void setStartLat(Double startLat) {
        this.startLat = startLat;
    }

    public Double getStartLong() {
        return startLong;
    }

    public void setStartLong(Double startLong) {
        this.startLong = startLong;
    }

    public Double getEndLat() {
        return endLat;
    }

    public void setEndLat(Double endLat) {
        this.endLat = endLat;
    }

    public Double getEndLong() {
        return endLong;
    }

    public void setEndLong(Double endLong) {
        this.endLong = endLong;
    }
}
