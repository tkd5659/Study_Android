package com.example.test;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

/*
* 메모 데이터 베이스
* */
public class NoteDatabase {
    private static final String TAG= "NoteDatabase";

    private static NoteDatabase database;
    public static String TABLE_NOTE = "NOTE";
    public static int DATABASE_VERSION= 1;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private Context context;

    private NoteDatabase(Context context){

        this.context=context;
    }

    public static NoteDatabase getInstance(Context context){
        if(database==null){
            database = new NoteDatabase(context);
        }
        return database;
    }
    public boolean open(){
        println("open database["+AppConstants.DATABASE_NAME+"]");

        dbHelper = new DatabaseHelper(context);
        db=dbHelper.getWritableDatabase();

        return true;
    }

    public void close(){
        println("close database["+AppConstants.DATABASE_NAME+"]");
        db.close();

        database = null;
    }
    public Cursor rawQuery(String SQL){
        println("\n executeQuery called \n");

        Cursor cursor = null;
        try {
                cursor=db.rawQuery(SQL,null);
                println("cursor count: " + cursor.getCount());
        }catch (Exception ex){
            Log.e(TAG,"Exception in executeQuery", ex);
        }return cursor;
    }

    public boolean execSQL(String SQL){
        println("\n execute called. \n");

        try {
            Log.d(TAG,"SQL : "+SQL);
            db.execSQL(SQL);
        }catch (Exception ex){
            Log.e(TAG,"Exception in executeQuery", ex);
            return false;
        }

        return true;
    }

    private class DatabaseHelper extends SQLiteOpenHelper{

        public DatabaseHelper(@Nullable Context context) {
            super(context, AppConstants.DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {	println("creating database [" + AppConstants.DATABASE_NAME + "].");

            // TABLE_NOTE
            println("creating table [" + TABLE_NOTE + "].");

            // drop existing table
            String DROP_SQL = "drop table if exists " + TABLE_NOTE;
            try {
                db.execSQL(DROP_SQL);
            } catch(Exception ex) {
                Log.e(TAG, "Exception in DROP_SQL", ex);
            }

            // create table
            String CREATE_SQL = "create table " + TABLE_NOTE + "("
                    + "  _id INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT, "
                    + "  WEATHER TEXT DEFAULT '', "
                    + "  ADDRESS TEXT DEFAULT '', "
                    + "  LOCATION_X TEXT DEFAULT '', "
                    + "  LOCATION_Y TEXT DEFAULT '', "
                    + "  CONTENTS TEXT DEFAULT '', "
                    + "  PICTURE TEXT DEFAULT '', "
                    + "  CREATE_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "  MODIFY_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP "
                    + ")";
            try {
                db.execSQL(CREATE_SQL);
            } catch(Exception ex) {
                Log.e(TAG, "Exception in CREATE_SQL", ex);
            }

            // create index
            String CREATE_INDEX_SQL = "create index " + TABLE_NOTE + "_IDX ON " + TABLE_NOTE + "("
                    + "CREATE_DATE"
                    + ")";
            try {
                db.execSQL(CREATE_INDEX_SQL);
            } catch(Exception ex) {
                Log.e(TAG, "Exception in CREATE_INDEX_SQL", ex);
            }
        }
        public void onOpen(SQLiteDatabase db){
            println("opened database["+AppConstants.DATABASE_NAME+"]");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            println("Upgrade database from version"+oldVersion+"to"+newVersion);
        }
    }
    private void println(String msg){
        Log.d(TAG,msg);
    }
}
