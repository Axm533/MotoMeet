package com.example.motomeet.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class CostJournalModel {
    private String fuelCost, highwayCost, additionalCost, id, uid;

    @ServerTimestamp
    private Timestamp entryDate, timestamp;

    public CostJournalModel() {}

    public CostJournalModel(String fuelCost, Timestamp entryDate, String highwayCost, String additionalCost, String id, String uid, Timestamp timestamp) {
        this.fuelCost = fuelCost;
        this.entryDate = entryDate;
        this.highwayCost = highwayCost;
        this.additionalCost = additionalCost;
        this.id = id;
        this.uid = uid;
        this.timestamp = timestamp;
    }

    public String getFuelCost() {
        return fuelCost;
    }

    public void setFuelCost(String fuelCost) {
        this.fuelCost = fuelCost;
    }

    public Timestamp getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(Timestamp entryDate) {
        this.entryDate = entryDate;
    }

    public String getHighwayCost() {
        return highwayCost;
    }

    public void setHighwayCost(String highwayCost) {
        this.highwayCost = highwayCost;
    }

    public String getAdditionalCost() {
        return additionalCost;
    }

    public void setAdditionalCost(String additionalCost) {
        this.additionalCost = additionalCost;
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

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
