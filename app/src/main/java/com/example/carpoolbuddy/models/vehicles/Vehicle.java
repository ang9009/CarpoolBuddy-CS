package com.example.carpoolbuddy.models.vehicles;

import java.io.Serializable;
import java.util.*;

public class Vehicle implements Serializable {
    private String owner;
    private String ownerID;
    private String model;
    private int capacity;
    private String vehicleID;
    private ArrayList<String> ridersUIDs;
    private ArrayList<String> ridersNames;
    private boolean open;
    private String vehicleType;
    private double basePrice;
    private double rideCost;
    private int distance;

    public Vehicle() {
    }

    public Vehicle(String owner, String ownerID, String model, int capacity, String vehicleType, double rideCost, int distance) {
        this.owner = owner;
        this.ownerID = ownerID;
        this.model = model;
        this.capacity = capacity;
        this.vehicleID = UUID.randomUUID().toString();
        this.ridersUIDs = new ArrayList<String>();
        this.ridersNames = new ArrayList<String>();
        this.open = true;
        this.vehicleType = vehicleType;
        this.basePrice = 15;
        this.rideCost = rideCost;
        this.distance = distance;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getVehicleID() {
        return vehicleID;
    }

    public void setVehicleID(String vehicleID) {
        this.vehicleID = vehicleID;
    }

    public ArrayList<String> getRidersUIDs() {
        return ridersUIDs;
    }

    public void addRiderUID(String riderUID) {
        ridersUIDs.add(riderUID);
    }

    public void removeRiderUID(String riderUID) {
        ridersUIDs.remove(riderUID);
    }

    public ArrayList<String> getRidersNames() {
        return ridersNames;
    }

    public void addRiderName(String riderName) {
        ridersNames.add(riderName);
    }

    public void removeRiderName(String riderName) {
        ridersNames.remove(riderName);
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    public void setRideCost(double rideCost) {
        this.rideCost = rideCost;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public double getRideCost() {
        return rideCost;
    }

    public int getDistance() {
        return distance;
    }
}
