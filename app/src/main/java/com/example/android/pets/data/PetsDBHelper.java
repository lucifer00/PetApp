package com.example.android.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Prashant on 6/24/2017.
 */

public class PetsDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME="petstore.db";
    private static final int DATABASE_VERSION=1;
    public PetsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlcomm="CREATE TABLE "+PetsContact.PetsEntry.TABLE_NAME+"("+PetsContact.PetsEntry._ID+" "+"INTEGER PRIMARY KEY AUTOINCREMENT, "+PetsContact.PetsEntry.COLUMN_PET_NAME+" TEXT NOT NULL, "+PetsContact.PetsEntry.COLUMN_PET_BREED+" TEXT NOT NULL, "+PetsContact.PetsEntry.COLUMN_PET_GENDER+" INTEGER NOT NULL, "+PetsContact.PetsEntry.COLUMN_PET_WEIGHT+" INTEGER NOT NULL DEFAULT 0)";
        System.out.println(sqlcomm);
        db.execSQL(sqlcomm);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
