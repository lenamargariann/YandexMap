package com.example.yandexmap;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.example.yandexmap.databinding.FragmentInfoBottomSheetLayoutBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.yandex.mapkit.RequestPoint;
import com.yandex.mapkit.RequestPointType;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.geometry.Polyline;
import com.yandex.mapkit.geometry.SubpolylineHelper;
import com.yandex.mapkit.map.ClusterizedPlacemarkCollection;
import com.yandex.mapkit.map.PolylineMapObject;
import com.yandex.mapkit.transport.TransportFactory;
import com.yandex.mapkit.transport.masstransit.MasstransitOptions;
import com.yandex.mapkit.transport.masstransit.MasstransitRouter;
import com.yandex.mapkit.transport.masstransit.Route;
import com.yandex.mapkit.transport.masstransit.Section;
import com.yandex.mapkit.transport.masstransit.SectionMetadata;
import com.yandex.mapkit.transport.masstransit.TimeOptions;
import com.yandex.mapkit.transport.masstransit.Transport;
import com.yandex.runtime.Error;

import java.util.ArrayList;
import java.util.List;

public class InfoBottomSheetLayout extends BottomSheetDialogFragment {

    private final String TAG = "MyMap";
    private FragmentInfoBottomSheetLayoutBinding fragmentInfoBottomSheetLayoutBinding;
    private final Point point;
    private final ClusterizedPlacemarkCollection mapObjects;
    private final Point myLoc;
    public PolylineMapObject currentObj;
    public final PolylineMapObject recentObj;
    private RecentPolylineMapObjectViewModel viewModel;
    public CloseBottomSheet closeBottomSheet;
    private TextView placeName;
    private TextView locationName;
    private TextView phoneNumber;
    private TextView kind;
    public PlaceInfo placeInfo;

    public InfoBottomSheetLayout(Point point,
                                 ClusterizedPlacemarkCollection mapObjects,
                                 Point myLoc, PolylineMapObject recentObj, PlaceInfo placeInfo) {
        this.point = point;
        this.mapObjects = mapObjects;
        this.myLoc = myLoc;
        this.recentObj = recentObj;
        this.placeInfo = placeInfo;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentInfoBottomSheetLayoutBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_info_bottom_sheet_layout, container, false);
        Log.e(TAG, "OnCreate view");
        fragmentInfoBottomSheetLayoutBinding.setPlace(placeInfo);
        Log.e(TAG, "Setting place :" + placeInfo.getPlaceName());
//        Log.e(TAG,placeInfo.getKind());
        fragmentInfoBottomSheetLayoutBinding.directionsButton.setOnClickListener(view -> {
            requestRoute(this.point);
            closeBottomSheet.closeBottomSheet();
        });
        viewModel = new ViewModelProvider(getActivity()).get(RecentPolylineMapObjectViewModel.class);


        return fragmentInfoBottomSheetLayoutBinding.getRoot();
    }


    public void requestRoute(Point point) {
        MasstransitOptions options = new MasstransitOptions(
                new ArrayList<>(),
                new ArrayList<>(),
                new TimeOptions());
        List<RequestPoint> points = new ArrayList<>();
        points.add(new RequestPoint(myLoc, RequestPointType.WAYPOINT, null));
        points.add(new RequestPoint(point, RequestPointType.WAYPOINT, null));
        MasstransitRouter mtRouter = TransportFactory.getInstance().createMasstransitRouter();
        com.yandex.mapkit.transport.masstransit.Session.RouteListener listener = new com.yandex.mapkit.transport.masstransit.Session.RouteListener() {
            @Override
            public void onMasstransitRoutes(@NonNull List<Route> routes) {

                if (routes.size() > 0) {
                    for (Section section : routes.get(0).getSections()) {

                        drawSection(
                                section.getMetadata().getData(),
                                SubpolylineHelper.subpolyline(
                                        routes.get(0).getGeometry(), section.getGeometry()));


                    }
                }

            }

            @Override
            public void onMasstransitRoutesError(@NonNull Error error) {

            }
        };
        mtRouter.requestRoutes(points, options, listener);

    }

    public void drawSection(SectionMetadata.SectionData data, Polyline geometry) {
        this.currentObj = this.mapObjects.getParent().addPolyline(geometry);
        viewModel.selectItem(this.currentObj);
        //  Log.e(TAG, this.currentObj + "Hello");
        if (recentObj != null) deleteRecentObj();
        currentObj.setStrokeColor(R.color.black);
        if (data.getTransports() != null) {
            for (Transport transport : data.getTransports()) {
                if (transport.getLine().getStyle() != null) {
                    currentObj.setStrokeColor(transport.getLine().getStyle().getColor() | R.color.black);
                    if (recentObj != null) deleteRecentObj();
                }
            }
        }
    }

    public void deleteRecentObj() {
        if (this.recentObj != null) {
            this.recentObj.setStrokeColor(R.color.transparent);
        }

    }

    interface CloseBottomSheet {
        void closeBottomSheet();
    }
}