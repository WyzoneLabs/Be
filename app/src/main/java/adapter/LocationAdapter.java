package adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckedTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.brimbay.be.R;

import java.util.List;

import model.Locations;
import utils.LocationsFilter;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {
    public List<Locations> locationsList,filterlist;
    private OnItemClickListener onItemClickListener;
    private LocationsFilter filter;

    public interface OnItemClickListener{
        void itemClickListener(Locations location, int i);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public LocationAdapter(@NonNull List<Locations> list) {
        this.locationsList = list;
        this.filterlist = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_list_item_single_choice,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Locations location = locationsList.get(position);

        holder._text.setText(location.name);

        holder._text.setOnClickListener(view -> {
            if (onItemClickListener != null){
                onItemClickListener.itemClickListener(location, position);
            }
        });
    }

    public Filter getFilter() {
        if (filter == null) {
            filter = new LocationsFilter(filterlist, this);
        }
        return filter;
    }

    @Override
    public int getItemCount() {
        return locationsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        AppCompatCheckedTextView _text;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            _text = (AppCompatCheckedTextView)itemView;
        }
    }

}