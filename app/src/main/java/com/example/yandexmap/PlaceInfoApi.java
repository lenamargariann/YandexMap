package com.example.yandexmap;

import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.yandex.mapkit.geometry.Point;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PlaceInfoApi {
    private final String TAG = "MyMap";
    private final Point point;
    private OkHttpClient client;
    public PlaceInfo place;

    public PlaceInfo getPlace() {
        return place;
    }

    public PlaceInfoApi(Point point) {
        this.point = point;
        setPlace();
    }

    public PlaceInfo webService() {
        place = new PlaceInfo();
        client = new OkHttpClient();
        final Request request = new Request.Builder().url("https://search-maps.yandex.ru/v1/" +
                "?apikey=996c7261-91de-4ceb-b8a0-a0c2a4ceb6ac" +
                "&text=" + point.getLatitude() + "," + point.getLongitude() +
                "&lang=en_US" +
                "&results=1").build();
        client.newCall(request).enqueue(new Callback() {

            PlaceInfo tmp;

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed to get response! " + e.getMessage());
            }


            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                this.tmp = null;

                try {
                    JSONObject object = new JSONObject(Objects.requireNonNull(Objects.requireNonNull(response.body()).string()));
                    Log.e(TAG, "hhh: " + object);
                    JSONObject obj = object.getJSONArray("features").getJSONObject(0).getJSONObject("properties");
                    String name = obj.getString("name");
                    String location = obj.getString("description");
                    String kind = obj.getJSONObject("GeocoderMetaData").getString("kind");
                    Log.e(TAG, "hhh: " + name);
                    Log.e(TAG, "hhh: " + location);
                    Log.e(TAG, "hhh: " + kind);
                    tmp = new PlaceInfo(name, location, "", kind);


                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
                PlaceInfoApi.this.place.setPlace(tmp);
                Log.e(TAG, PlaceInfoApi.this.place.getPlaceName() + "kx kx kx");
            }

        });
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, PlaceInfoApi.this.place.getPlaceName() + "jjj");
            }
        };
        handler.postDelayed(runnable,1000);

        return PlaceInfoApi.this.place;
    }


    public void setPlace() {
        Log.e(TAG, "From setting place!");
        this.place = new PlaceInfo(webService());
    }
}

