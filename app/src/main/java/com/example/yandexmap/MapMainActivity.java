package com.example.yandexmap;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yandexmap.databinding.FragmentInfoBottomSheetLayoutBinding;
import com.example.yandexmap.databinding.MapMainActivityBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.GeoObjectCollection;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.Cluster;
import com.yandex.mapkit.map.ClusterListener;
import com.yandex.mapkit.map.ClusterizedPlacemarkCollection;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.PolylineMapObject;
import com.yandex.mapkit.map.VisibleRegionUtils;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.search.Response;
import com.yandex.mapkit.search.SearchFactory;
import com.yandex.mapkit.search.SearchManager;
import com.yandex.mapkit.search.SearchManagerType;
import com.yandex.mapkit.search.SearchOptions;
import com.yandex.mapkit.search.Session;
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;

import java.util.ArrayList;
import java.util.List;

public class MapMainActivity extends AppCompatActivity implements SearchViewAdapter.MoveCallback, ClusterListener, InfoBottomSheetLayout.CloseBottomSheet {
    private final String TAG = "MyMap";
    private MapView mapView;
    private Point myLoc;
    private FragmentInfoBottomSheetLayoutBinding fragmentInfoBottomSheetLayoutBinding;
    private SearchManager searchManager;
    private Context context;
    private InfoBottomSheetLayout dialog;
    private ClusterizedPlacemarkCollection mapObjects;
    private List<PlacemarkMapObject> placemarkMapObjects;
    private PlacemarkMapObject myLocationPlacemark = null;
    private PolylineMapObject recentObj;
    private RecentPolylineMapObjectViewModel viewModel;
    private PlaceInfoApi placeInfoApi;
    private InputMethodManager manager;
    private View view;
    public PlaceInfo placeInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "Hey");
        MapKitFactory.setApiKey("849b9e16-b36f-4a9c-8bd9-6a6535183a9d");
        MapKitFactory.initialize(this);
        SearchViewAdapter.moveCallback = this;
        MapMainActivityBinding mapMainActivityBinding = DataBindingUtil.setContentView(this, R.layout.map_main_activity);
        mapView = mapMainActivityBinding.mapView;
        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED);
        LocationManager myLocation = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        context = this;
        placemarkMapObjects = new ArrayList<>();
        myLocation.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5, 2, location -> Log.e(TAG, "Location changed!"));
        GPSTracker gpsTracker = new GPSTracker(this);
        Location location = gpsTracker.location;
        double myLongitude = location.getLongitude();
        double myLatitude = location.getLatitude();
        myLoc = new Point(myLatitude, myLongitude);
        FloatingActionButton myPlace = mapMainActivityBinding.showMyGeolocation;
        mapObjects = mapView.getMap().getMapObjects().addClusterizedPlacemarkCollection(this);
        ImageProvider image = ImageProvider.fromResource(this, R.mipmap.address);
        IconStyle style = new IconStyle().setScale(0.09f);
        viewModel = new ViewModelProvider(this).get(RecentPolylineMapObjectViewModel.class);
        manager = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        myPlace.setOnClickListener(v -> {
            if (myLocationPlacemark == null) {
                myLocationPlacemark = mapObjects.addPlacemark(myLoc, image, style);
                addPlacemarkListeners(myLocationPlacemark);
                mapView.getMap().move(new CameraPosition(myLoc, 20.0f, 0.0f, 0.0f), new Animation(Animation.Type.SMOOTH, 3.0F), null);
                mapObjects.clusterPlacemarks(60, 15);
            } else {
                myLocationPlacemark.setGeometry(myLoc);
            }

        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        MenuItem searchItem = menu.getItem(0);
        RecyclerView recyclerView = findViewById(R.id.search_recycler);
        SearchView view = (SearchView) searchItem.getActionView();
        view.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.e(TAG, "Hello from set on query");
                return false;
            }


            @Override
            public boolean onQueryTextChange(String newText) {
                Log.e(TAG, "Hello");
                recyclerView.setVisibility(View.VISIBLE);
                submitQuery(String.valueOf(view.getQuery()),
                        new Session.SearchListener() {
                            @Override
                            public void onSearchResponse(@NonNull Response response) {
                                List<GeoObjectCollection.Item> tempList = response.getCollection().getChildren();
                                if (tempList.size() > 10) {
                                    tempList = tempList.subList(0, 9);
                                }
                                SearchViewAdapter adapter = new SearchViewAdapter(tempList, MapMainActivity.this);
                                recyclerView.setLayoutManager(new LinearLayoutManager(MapMainActivity.this));
//                                DividerItemDecoration divider = new DividerItemDecoration(MapMainActivity.this, DividerItemDecoration.VERTICAL);
//                                recyclerView.addItemDecoration(divider);
                                recyclerView.setAdapter(adapter);

                            }

                            //response.getCollection().getChildren().get(0))
                            @Override
                            public void onSearchError(@NonNull Error error) {
                                Log.e(TAG, "Error while getting response");
                            }
                        });
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }


    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return super.getLifecycle();
    }

    @Override
    protected void onStart() {
        Log.e(TAG, "onStart");
        super.onStart();
        mapView.onStart();
        MapKitFactory.getInstance().onStart();
    }


    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
    }

    private void submitQuery(String query, Session.SearchListener searchListener) {
        searchManager.submit(query, VisibleRegionUtils.toPolygon(mapView.getMap().getVisibleRegion()), new SearchOptions(), searchListener);
    }

    @Override
    public void moveToPoint(Point point) {
        view = MapMainActivity.this.getCurrentFocus();
        manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        Log.e(TAG, "From move to a point");
        mapView.getMap().move(new CameraPosition(point, 15.0f, 0.0f, 0.0f), new Animation(Animation.Type.SMOOTH, 3.0F), null);
        RecyclerView recyclerView = findViewById(R.id.search_recycler);
        ImageProvider image = ImageProvider.fromResource(context, R.mipmap.address);
        IconStyle style = new IconStyle().setScale(0.09f);
        PlacemarkMapObject placemarkMapObject = mapObjects.addPlacemark(point, image, style);

        addPlacemarkListeners(placemarkMapObject);
        placemarkMapObjects.add(placemarkMapObject);
        mapObjects.clusterPlacemarks(60, 15);
        recyclerView.setVisibility(View.GONE);
    }

    private void addPlacemarkListeners(PlacemarkMapObject placemarkMapObject) {
        placemarkMapObject.addTapListener((mapObject, point) -> {
            Log.e(TAG, "Object was tapped!");
            placeInfoApi = new PlaceInfoApi(point);
            Runnable runnable = () -> {
                placeInfo = new PlaceInfo(placeInfoApi.getPlace());
                dialog = new InfoBottomSheetLayout(point, mapObjects, myLoc, MapMainActivity.this.recentObj, placeInfo);
                dialog.show(getSupportFragmentManager(), TAG);
                dialog.closeBottomSheet = MapMainActivity.this;
            };
            Handler handler = new Handler();
            handler.postDelayed(runnable, 700);

            viewModel.getItem().observe(this, polylineMapObject -> MapMainActivity.this.recentObj = polylineMapObject);
            if (this.recentObj != null) {
                Log.e(TAG, this.recentObj.getGeometry() + "Helloy");
            }

            return false;
        });
    }


    @Override
    public void onClusterAdded(@NonNull Cluster cluster) {
        cluster.getAppearance().setIcon(new TextImageProvider(Integer.toString(cluster.getSize())));
        cluster.addClusterTapListener(cluster1 -> {
            Toast.makeText(
                    getApplicationContext(),
                    cluster1.getSize(),
                    Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    @Override
    public void closeBottomSheet() {
        dialog.dismiss();
        view = MapMainActivity.this.getCurrentFocus();
        manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public class TextImageProvider extends ImageProvider {

        private final String text;

        public TextImageProvider(String text) {
            this.text = text;
        }

        @Override
        public String getId() {
            return "text_" + text;
        }

        @Override
        public Bitmap getImage() {
            DisplayMetrics metrics = new DisplayMetrics();
            WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            manager.getDefaultDisplay().getMetrics(metrics);
            Paint textPaint = new Paint();
            Paint.FontMetrics textMetrics = textPaint.getFontMetrics();
            textPaint.setTextSize(16 * metrics.density);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setStyle(Paint.Style.FILL);
            textPaint.setAntiAlias(true);
            float widthF = textPaint.measureText(text);
            Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
            float heightF = Math.abs(fontMetrics.bottom) + Math.abs(fontMetrics.top);
            float textRadius = (float) Math.sqrt((widthF * heightF + widthF * heightF) / 2);
            float internalRadius = textRadius + 10 * metrics.density;
            float externalRadius = internalRadius + 2 * metrics.density;
            int width = (int) (2 * externalRadius + 0.5);
            Bitmap bitmap = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Paint backgroundPaint = new Paint();
            backgroundPaint.setAntiAlias(true);
            backgroundPaint.setColor(Color.RED);
            canvas.drawCircle(width / 2, width / 2, externalRadius, backgroundPaint);

            backgroundPaint.setColor(Color.WHITE);
            canvas.drawCircle(width / 2, width / 2, internalRadius, backgroundPaint);

            canvas.drawText(
                    text,
                    width / 2,
                    width / 2 - (textMetrics.ascent + textMetrics.descent) / 2,
                    textPaint);

            return bitmap;
        }


    }


}
