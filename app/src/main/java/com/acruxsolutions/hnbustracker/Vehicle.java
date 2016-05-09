package com.acruxsolutions.hnbustracker;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by manhnv on 5/8/16.
 */
public class Vehicle {

    private String busNumber;
    private double busLat;
    private double busLong;
    private String timestamp;


    public Vehicle() {
    }

    public Vehicle(String busNumber, double busLat, double busLong, String timestamp) {
        this.busNumber = busNumber;
        this.busLat = busLat;
        this.busLong = busLong;
        this.timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
    }

    public String getBusNumber() {
        return busNumber;
    }

    public double getBusLat() {

        return busLat;
    }

    public double getBusLong() {
        return busLong;
    }

    public String getTimestamp() {

        return timestamp;
    }

}