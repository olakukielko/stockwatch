package com.example.kukielko.stockwatch;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ola on 3/4/18.
 */

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String TAG = "DatabaseHandler";
    private static final String DATABASE_NAME = "StockDB";
    private static final String TABLE_NAME = "StockWatchTable";
    private static final String SYMBOL = "StockSymbol";
    private static final String COMPANY  = "CompanyName";


    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    SYMBOL + " TEXT not null," +
                    COMPANY + " TEXT not null)";

    private SQLiteDatabase database;

    public DatabaseHandler(Context context){
        super(context, DATABASE_NAME, null,DATABASE_VERSION);
        database = getWritableDatabase();
        Log.d(TAG, "DatabaseHandler: ");
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        //only called if db does not exist
        Log.d(TAG, "onCreate: Making New DB");
        db.execSQL(SQL_CREATE_TABLE);
    }
    public void addStock(Stock stock) {
        //Log.d(TAG, "addStock: " + stock.getName() + stock.getCompany());
        ContentValues values = new ContentValues();

        values.put(SYMBOL, stock.getName());
        values.put(COMPANY, stock.getCompany());
        long key = database.insert(TABLE_NAME, null, values);
        Log.d(TAG, "addStock: " + key);
    }
    public void deleteStock(String symbol) {
        Log.d(TAG, "deleteStock: Deleting Stock " + symbol);

        int cnt = database.delete(
                TABLE_NAME, SYMBOL + " = ?", new String[] { symbol });
        Log.d(TAG, "deleteStock: " + cnt);
    }

    public List<String> loadStocks() {
        Log.d(TAG, " loadStocks: Load all symbol-company entries from DB");
        ArrayList<String> stocks = new ArrayList<>();
        Cursor cursor = database.query(
                TABLE_NAME, // The table to query
                new String[]{ SYMBOL, COMPANY }, // The columns to return
                null, // The columns for the WHERE clause, null means “*”
                null, // The values for the WHERE clause, null means “*”
                null, // don't group the rows
                null, // don't filter by row groups
                null); // The sort order
        if (cursor != null) { // Only proceed if cursor is not null
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                String symbol = cursor.getString(0); // 1st returned column
                String company = cursor.getString(1); // 2nd returned column
                stocks.add(symbol);
                cursor.moveToNext();
            }
            cursor.close();
        }
        return stocks;
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
    public void shutDown() {
        database.close();
    }
}
