package com.example.kukielko.stockwatch;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by ola on 2/28/18.
 */

public class StockViewHolder extends RecyclerView.ViewHolder{
    public TextView symbol;
    public TextView company;
    public TextView last_trade_price;
    public TextView price_change_amount;
    public TextView price_change_percentage;

    public StockViewHolder(View itemView) {
        super(itemView);
        symbol = (TextView) itemView.findViewById(R.id.symbol_id);
        company = (TextView) itemView.findViewById(R.id.company_id);
        last_trade_price = (TextView) itemView.findViewById(R.id.last_price_id);
        price_change_amount = (TextView) itemView.findViewById(R.id.price_change_id);
        price_change_percentage = (TextView) itemView.findViewById(R.id.percent_inc_id);
    }
}
