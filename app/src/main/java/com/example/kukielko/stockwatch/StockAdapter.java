package com.example.kukielko.stockwatch;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by ola on 2/28/18.
 */

public class StockAdapter extends RecyclerView.Adapter<StockViewHolder> {
    private static final String TAG = "SavedNotesAdapter";
    private MainActivity mainActivity;
    private ArrayList<Stock> stock_list;
    private RecyclerView recyclerView;


    public StockAdapter(ArrayList<Stock> stocklist, MainActivity ma){
        this.stock_list = stocklist;
        mainActivity = ma;
    }
    @Override
    public StockViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //inflate stock_item layout into itemView
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_item, parent, false);
        final StockViewHolder holder = new StockViewHolder(itemView);

        itemView.setOnClickListener(mainActivity);
        itemView.setOnLongClickListener(mainActivity);   //send onlongclick to main activity to implement
        return new StockViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(StockViewHolder holder, int position) {
        final Stock curr_stock = stock_list.get(position);
        holder.symbol.setText(curr_stock.getName());
        holder.company.setText(curr_stock.getCompany());
        holder.price_change_percentage.setText(curr_stock.getPrice_change_percentage()+"%");
        holder.last_trade_price.setText(curr_stock.getLast_trade_price());
        if (curr_stock.getPrice_change_amount().charAt(0)=='-'){
            holder.price_change_amount.setTextColor(Color.RED);
            holder.symbol.setTextColor(Color.RED);
            holder.company.setTextColor(Color.RED);
            holder.price_change_percentage.setTextColor(Color.RED);
            holder.last_trade_price.setTextColor(Color.RED);
            holder.price_change_amount.setText("\u25bc   "+ curr_stock.getPrice_change_amount());

        }
        else{
            holder.price_change_amount.setTextColor(Color.GREEN);
            holder.symbol.setTextColor(Color.GREEN);
            holder.company.setTextColor(Color.GREEN);
            holder.price_change_percentage.setTextColor(Color.GREEN);
            holder.last_trade_price.setTextColor(Color.GREEN);
            holder.price_change_amount.setText("\u25b2   "+ curr_stock.getPrice_change_amount());

        }

    }

    @Override
    public int getItemCount() {
        return stock_list.size();
    }
}
