package com.example.kukielko.stockwatch;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.StrictMode;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {
    private static final String TAG = "MainActivity";
    private RecyclerView recyclerView;
    private StockAdapter mAdapter;
    public Stock curr_item;
    public List<String> listofstocks = new ArrayList<>();
    public List<String> stocklist = new ArrayList<>();
    public ArrayList<Stock> allStocks = new ArrayList<>();
    public String searchInput, symbol;
    private DatabaseHandler databaseHandler;
    private SwipeRefreshLayout swiper;
    public String networkError = "Error: No Network Detected";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //for internet connection
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        if (checkNetwork()){
            //set up adapter and recycler view
            recyclerView = (RecyclerView) findViewById(R.id.recycler);
            mAdapter = new StockAdapter(allStocks, this);
            mAdapter.notifyDataSetChanged();
            recyclerView.setAdapter(mAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            swiper = (SwipeRefreshLayout) findViewById(R.id.swiper);

            swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    doRefresh();
                }
            });
            databaseHandler = new DatabaseHandler(this);
            List<String> list = databaseHandler.loadStocks();
            for (String item: list){
                doAsync("1", item);
                listofstocks.add(item);
            }
            if (listofstocks.isEmpty()){
                populateDummyData();
            }
            Log.d(TAG, "onCreate: done");
        } else {displayError(networkError);}
    }
    private void doRefresh(){
        if(checkNetwork()){
            //re-download all items in allStocks list
            for (Stock item: allStocks){
                doAsync("1", item.getName());
                mAdapter.notifyDataSetChanged();
                swiper.setRefreshing(false);
            }
            Toast.makeText(this, "Stock Data Refreshed", Toast.LENGTH_SHORT).show();
        } else{
            displayError(networkError);
        }
    }

    @Override
    protected void onDestroy() {
        databaseHandler.shutDown();
        super.onDestroy();
    }
    public void populateDummyData(){
        stocklist.add("AAPL");
        stocklist.add("GOOG");
        try{
            doAsync("2","AAPL");
            doAsync("2","GOOG");
        }
        catch (Exception e){
            Log.d(TAG, "populateDummyData: " + e);
        }
    }

    //for every item in allStocks, read in symbol and download data
    //string i is "0" if doing json search, "1" if downloading stock data
    public void doAsync(String i, String s){
        AsyncLoad asyncLoad = new AsyncLoad(this);
        asyncLoad.execute(i,s);
    }
    //returns true if device is connected to internet, false if not
    public boolean checkNetwork(){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //after returning from async task,
    public void getDataFromAsync(List<String> s) {
        selectionDialog(s);
    }

    //receive download results; check if this stock already exists, if not, create stock object
    public void getDummyData(List<String >s){
        curr_item = new Stock();
        curr_item.setName(s.get(0));
        curr_item.setCompany(s.get(1));
        curr_item.setLast_trade_price(s.get(2));
        curr_item.setPrice_change_amount(s.get(3));
        curr_item.setPrice_change_percentage(s.get(4));
        allStocks.add(curr_item);
        mAdapter.notifyDataSetChanged();
        stocklist.add(s.get(0));
        databaseHandler.addStock(curr_item);
    }
    //sends back data from the second JSON request
    public void getDownloadData(List<String> s) throws ClassNotFoundException {
        if (s.isEmpty()){
            displayError("Error: No symbol data found.");
        }
        else{
        if (!stocklist.contains(s.get(0))){
            curr_item = new Stock();                               
            curr_item.setName(s.get(0));                           
            curr_item.setCompany(s.get(1));                        
            curr_item.setLast_trade_price(s.get(2));               
            curr_item.setPrice_change_amount(s.get(3));            
            curr_item.setPrice_change_percentage(s.get(4));        
            allStocks.add(curr_item);                              
            databaseHandler.addStock(curr_item);                   
            mAdapter.notifyDataSetChanged();
            stocklist.add(s.get(0));
        }
        }  }
    @Override
    ///FOR TOP BAR MENU TO SHOW UP
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    ///ACTION FOR SELECTING MENU ITEM
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item1: //new stock
                Toast.makeText(this, "New Stock Selected", Toast.LENGTH_SHORT).show();
                newStock();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    //happens when new stock menu button is pressed; dialog box with search pops up new stock item is made and added to allStocks
    public void newStock(){
        if (checkNetwork()){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Stock Selection");
            builder.setMessage("Please enter a stock symbol:");
            final EditText input = new EditText(this);
            input.setFilters(new InputFilter[] {new InputFilter.AllCaps()});

            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);
            builder.setPositiveButton("Search", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d(TAG, "newStock: pressed");
                    searchInput = input.getText().toString();
                    //i=0 means we are searching for the query
                    doAsync("0",searchInput);
                    Log.d(TAG, "onClick:newStock(): "+searchInput);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        }
        else{
            displayError(networkError);
        }}

    //Displays error dialog to user
    public void displayError(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(msg);
        builder.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        builder.create().show();
    }
    //Displays search results into alert dialog, lets user pick one and returns symbol name
    public void selectionDialog(List<String> results) {
        Log.d(TAG, "selectionDialog: " + results.size());
        if (results.size()==1){
            symbol = results.get(0);
            processNewStock();
        }
        else if (results.size()>0){
            final String[] symbol = new String[1];
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
            builderSingle.setTitle("Make a selection");

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_singlechoice);
            arrayAdapter.addAll(results);

            builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d(TAG, "onClick: " + arrayAdapter.getItem(which));
                    symbol[0] = arrayAdapter.getItem(which);
                    returnSymbol(arrayAdapter.getItem(which));
                }
            });
            builderSingle.show();
        }
        else {
            displayError("Symbol not found: " + searchInput);
        }
    }
    //helps get the string out of the anonymous function in selectionDialog
    //sets global variable symbol to the selected result
    public void returnSymbol(String h){
        symbol = h;
        processNewStock();

    }

    //i ==1 - look up symbol
    public void processNewStock(){
        doAsync("1",symbol);
    }
    @Override
    //Receives longclick listener from stockadapter
    public boolean onLongClick(View v) {  // long click listener called by ViewHolder long clicks
        int pos = recyclerView.getChildLayoutPosition(v);
        confirmDelete(pos);
        Log.d(TAG, "onLongClick: " + pos);
        return false;
    }
    //TODO
    @Override
    public void onClick(View view) {
        int pos = recyclerView.getChildLayoutPosition(view);
        String url = "https://www.marketwatch.com/investing/stock/" + allStocks.get(pos).getName();
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setPackage("com.android.chrome");
        try {
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            i.setPackage(null);
            startActivity(i);
        }
    }

    public void confirmDelete(int position){
        final int curr_pos = position;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                databaseHandler.deleteStock(allStocks.get(curr_pos).getName());
                allStocks.remove(curr_pos);
                stocklist.remove(curr_pos);
                mAdapter.notifyItemRemoved(curr_pos);
                mAdapter.notifyItemRangeChanged(curr_pos,allStocks.size());

            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //do nothing
            }

        });

        builder.setMessage("Would you like to delete this entry?");
        builder.setTitle("Delete");
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
