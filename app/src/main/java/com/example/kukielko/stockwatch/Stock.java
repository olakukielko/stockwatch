package com.example.kukielko.stockwatch;

import android.widget.TextView;

/**
 * Created by ola on 2/28/18.
 */

public class Stock {
    private String symbol;
    private String company;
    private String last_trade_price;
    private String price_change_amount;
    private String price_change_percentage;

    public Stock(){
        symbol = "symbol";
        company = "company";
        last_trade_price = "$50";
        price_change_amount = "1.5";
        price_change_percentage = "1%";
    }

    public String getName(){
        return symbol;
    }
    public void setName(String symbol) {
        this.symbol = symbol;
    }
    public String getCompany(){
        return company;
    }
    public void setCompany(String company) {
        this.company = company;
    }
    public String getLast_trade_price(){
        return last_trade_price;
    }
    public void setLast_trade_price(String price) {
        this.last_trade_price = price;
    }
    public String getPrice_change_amount(){
        return price_change_amount;
    }
    public void setPrice_change_amount(String amount) {
        this.price_change_amount = amount;
    }
    public String getPrice_change_percentage(){
        return price_change_percentage;
    }
    public void setPrice_change_percentage(String percent) {
        this.price_change_percentage = percent;
    }

}
