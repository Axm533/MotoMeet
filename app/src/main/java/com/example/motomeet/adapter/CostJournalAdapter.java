package com.example.motomeet.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.motomeet.R;
import com.example.motomeet.model.CostJournalModel;

import java.util.List;

public class CostJournalAdapter extends RecyclerView.Adapter<CostJournalAdapter.CostJournalHolder>{

    private final List<CostJournalModel> list;

    public CostJournalAdapter(List<CostJournalModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public CostJournalHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cost_journal_items, parent, false);
        return new CostJournalAdapter.CostJournalHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CostJournalAdapter.CostJournalHolder holder, int position) {
        holder.fuelCostTV.setText(list.get(position).getFuelCost());
        holder.entryDateTV.setText(list.get(position).getEntryDate());
        holder.highwayCostTV.setText(list.get(position).getHighwayCost());
        holder.additionalCostTV.setText(list.get(position).getAdditionalCost());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class CostJournalHolder extends RecyclerView.ViewHolder{

        private final TextView fuelCostTV, entryDateTV, highwayCostTV, additionalCostTV;

        public CostJournalHolder(@NonNull View itemView) {
            super(itemView);

            fuelCostTV = itemView.findViewById(R.id.fuelCostTV);
            entryDateTV = itemView.findViewById(R.id.entryDateTV);
            highwayCostTV = itemView.findViewById(R.id.highwayCostTV);
            additionalCostTV = itemView.findViewById(R.id.additionalCostTV);

        }
    }
}
