package com.example.motomeet.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class ServiceBookModel {
    private String serviceName, serviceDescription, serviceCost, doneServices, id, uid;
    @ServerTimestamp
    private Date timestamp;

    public ServiceBookModel() {}

    public ServiceBookModel(String serviceName, String serviceDescription, String serviceCost, String doneServices, String id, String uid){
        this.serviceName = serviceName;
        this.serviceDescription = serviceDescription;
        this.serviceCost = serviceCost;
        this.doneServices = doneServices;
        this.id = id;
        this.uid = uid;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    public String getServiceDescription() {
        return serviceDescription;
    }

    public void setServiceDescription(String serviceDescription) { this.serviceDescription = serviceDescription;}
    public String getServiceCost() {
        return serviceCost;
    }

    public void setServiceCost(String serviceCost) {
        this.serviceCost = serviceCost;
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
    public String getDoneServices() {
        return doneServices;
    }

    public void setDoneServices(String doneServices) {
        this.doneServices = doneServices;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
