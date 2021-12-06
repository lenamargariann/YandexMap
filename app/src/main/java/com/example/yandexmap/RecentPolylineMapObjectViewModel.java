package com.example.yandexmap;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.yandex.mapkit.map.PolylineMapObject;

public class RecentPolylineMapObjectViewModel extends ViewModel {
    private final MutableLiveData<PolylineMapObject> mapObjectMutableLiveData = new MutableLiveData<PolylineMapObject>();

    public void selectItem(PolylineMapObject polylineMapObject) {
        mapObjectMutableLiveData.setValue(polylineMapObject);
    }


    public MutableLiveData<PolylineMapObject> getItem() {
        return this.mapObjectMutableLiveData;
    }
}
