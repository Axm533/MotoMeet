package com.example.motomeet.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.motomeet.R;
import com.example.motomeet.model.ServiceBookModel;

import java.util.List;

public class ServiceBookAdapter extends RecyclerView.Adapter<ServiceBookAdapter.ServiceBookHolder> {

    private final List<ServiceBookModel> list;

    public ServiceBookAdapter(List<ServiceBookModel> list){
        this.list = list;
    }

    @NonNull
    @Override
    public ServiceBookHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.servicebook_items, parent, false);
        return new ServiceBookHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceBookHolder holder, int position) {

        holder.serviceNameTV.setText(list.get(position).getServiceName());
        holder.serviceDescriptionTV.setText(list.get(position).getServiceDescription());
        holder.serviceCostTV.setText(list.get(position).getServiceCost());
        holder.doneServicesTV.setText(list.get(position).getDoneServices());

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ServiceBookHolder extends RecyclerView.ViewHolder{

        private final TextView serviceNameTV, serviceDescriptionTV, serviceCostTV, doneServicesTV;

        public ServiceBookHolder(@NonNull View itemView) {
            super(itemView);

            serviceNameTV = itemView.findViewById(R.id.serviceNameTV);
            serviceDescriptionTV = itemView.findViewById(R.id.serviceDescriptionTV);
            serviceCostTV = itemView.findViewById(R.id.serviceCostTV);
            doneServicesTV = itemView.findViewById(R.id.doneServicesTV);
        }
    }
}
