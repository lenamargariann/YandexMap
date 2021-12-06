package com.example.yandexmap;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

public class PlaceInfo extends BaseObservable {
    private String placeName;
    private String Location;
    private String phoneNumber;
    private String kind;

    public PlaceInfo() {
        this.placeName = "";
        this.Location = "";
        this.phoneNumber = "Oops! Phone number not found!";
        this.kind = "Oops! No kind.";
    }

    public PlaceInfo(PlaceInfo pl) {
        this.placeName = pl.placeName;
        this.kind = pl.getKind();
        this.Location = pl.getLocation();
        this.phoneNumber = pl.getPhoneNumber();
    }

    public PlaceInfo(String name, String location, String o, String kind) {
        this.placeName = name;
        this.kind = kind;
        this.Location = location;
        this.phoneNumber = o;
    }

    @Bindable
    public String getKind() {
        return kind;
    }

    @Bindable
    public String getLocation() {
        return Location;
    }

    @Bindable
    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Bindable
    public String getPlaceName() {
        return placeName;
    }

    @Bindable
    public void setPlace(PlaceInfo place) {
        this.placeName = place.placeName;
        this.phoneNumber = place.phoneNumber;
        this.kind = place.kind;
        this.Location = place.Location;

    }
}

