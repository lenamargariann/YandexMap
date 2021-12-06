package com.example.yandexmap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yandex.mapkit.GeoObjectCollection;
import com.yandex.mapkit.geometry.Point;

import java.util.List;

public class SearchViewAdapter extends RecyclerView.Adapter<SearchViewAdapter.LocationHolder> {
    private static final String TAG = "MyMap";
    public static MoveCallback moveCallback;
    private final List<GeoObjectCollection.Item> list;
    private final Context context;

    public SearchViewAdapter(List<GeoObjectCollection.Item> temp, Context cont) {
        this.list = temp;
        this.context = cont;
    }

    @NonNull
    @Override
    public SearchViewAdapter.LocationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(context).inflate(R.layout.search_item_view, parent, false);
        return new LocationHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewAdapter.LocationHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.bind(list.get(position).getObj().getName());
        holder.itemView.setOnClickListener(v -> {
            Point myLoc = list.get(position).getObj().getGeometry().get(0).getPoint();
            moveCallback.moveToPoint(myLoc);

        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class LocationHolder extends RecyclerView.ViewHolder {
        private final TextView searchItem;

        public LocationHolder(@NonNull View itemView) {
            super(itemView);
            searchItem = itemView.findViewById(R.id.search_text);
        }

        public void bind(String text) {
            searchItem.setText(text);
        }
    }


    interface MoveCallback {
        void moveToPoint(Point point);
    }
}
