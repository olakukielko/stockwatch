package com.example.kukielko.stockwatch;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ola on 3/1/18.
 */

public class AsyncLoad extends AsyncTask<String, Void, String> {
    private static final String TAG = "AsyncLoad";
    private MainActivity mainActivity;
    String symbol, searchInput;
    //for search query
    public String URL1 = "http://d.yimg.com/aq/autoc?region=US&lang=en-US&";
    //for download
    public String URL2 = "https://api.iextrading.com/1.0/stock/";
    public String flag;

    public AsyncLoad(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    protected String doInBackground(String... strings) {
        flag = strings[0];
        searchInput = "";
        for(char ch: strings[1].toCharArray()){
            if(ch ==(' ')){
                break;
            } else{searchInput+=ch; }
        }
        searchInput = searchInput.toString();
        Log.d(TAG, "doInBackground: " + flag +" " + searchInput);
        return getJSON(buildUrl(searchInput));
    }


    //From user provided query build a URL object and cast it to string
    public String buildUrl(String query){
        Log.d(TAG, "buildUrl: " );
        Log.d(TAG, "buildUrl: building URL from input=" + query);
        Uri.Builder buildURL;
        String urlToUse;
        if (flag=="0"){
            buildURL = Uri.parse(URL1).buildUpon();
            buildURL.appendQueryParameter("query", query);
            urlToUse = buildURL.build().toString();
        } else{
            urlToUse = URL2 + query + "/quote";
        }
        return urlToUse;
    }
    //input built url string and retrieve online data
    public String getJSON(String tryurl){
        Log.d(TAG, "getJSON: url=" + tryurl);
        StringBuilder sb = new StringBuilder();
        try{
            URL url = new URL(tryurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) !=null){
                sb.append(line).append('\n');
            }
            Log.d(TAG, "getJSON: results="+ sb.toString());

        } catch (Exception e){
            Log.e(TAG,"retrieveJson", e);
        }
        return sb.toString();
    }
    @Override
    protected void onPostExecute(String s) {
        Log.d(TAG, "onPostExecute: string to parse ="+ s);
        Log.d(TAG, "onPostExecute: flag=" + flag);
        if (flag =="0"){
            Log.d(TAG, "onPostExecute: " + "flag is 0");
            List<String> searchResults = getSearchResults(s);
            mainActivity.getDataFromAsync(searchResults);
        }
        else if (flag=="1"){
            Log.d(TAG, "onPostExecute: " + "flag is 1");
            List<String> downloadResults = parseDownload(s);
            try {
                mainActivity.getDownloadData(downloadResults);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else{
            Log.d(TAG, "onPostExecute: " + "flag is 2");
            List<String> downloadResults = parseDownload(s);
            mainActivity.getDummyData(downloadResults);
        }

    }
    public List<String> parseDownload(String json){
        List<String> searchResults = new ArrayList<>();
            try {
                JSONObject jobjMain = new JSONObject(json);
                searchResults.add(jobjMain.getString("symbol"));
                searchResults.add(jobjMain.getString("companyName"));
                searchResults.add(jobjMain.getString("latestPrice"));
                searchResults.add(jobjMain.getString("change"));
                searchResults.add(jobjMain.getString("changePercent"));
                Log.d(TAG, "parseDownload: string=" + searchResults);

            } catch (Exception e) {
                //mainActivity.displayError("Error: No JSON response.");
                Log.e(TAG, "retrieveJson", e);
            }
        return searchResults;
    }
    //Parses json string and retrieves symbol and name
    public List<String> getSearchResults(String json){
        List<String> searchResults = new ArrayList<>();
        try{
            JSONObject jobjMain = new JSONObject(json);

            JSONObject resultSet = jobjMain.getJSONObject("ResultSet");
            JSONArray resultArray = resultSet.getJSONArray("Result");
            for (int i = 0; i < resultArray.length(); i++) {
                JSONObject item = resultArray.getJSONObject(i);
                String symbol = item.getString("symbol");
                if (!symbol.contains(".")){
                    if (symbol.length()<8){
                    searchResults.add(item.getString("symbol") + " " +  item.getString("name"));
                }}
            }
            Log.d(TAG, "getSearchResults: "+searchResults);
        } catch (Exception e){
            Log.e(TAG,"retrieveJson", e);

        }
        return searchResults;
    }
}
