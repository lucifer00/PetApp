package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

import static com.example.android.pets.data.PetsContact.PetsEntry.TABLE_NAME;

/**
 * Created by Prashant on 6/30/2017.
 */

public class PetProvider extends ContentProvider {
    private static final UriMatcher sUriMatcher=new UriMatcher(UriMatcher.NO_MATCH);
    private static final int PETS=100;
    private static final int PET_ID=101;
    static
    {

        sUriMatcher.addURI(PetsContact.CONTENT_AUTHORITY, PetsContact.PATH_PETS,PETS);
        sUriMatcher.addURI(PetsContact.CONTENT_AUTHORITY, PetsContact.PATH_PETS + "/#",PET_ID);
    }
    private PetsDBHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper=new PetsDBHelper(getContext());
        if(mDbHelper==null)System.out.println("null");
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor=null;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                // For the PETS code, query the pets table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.
                // TODO: Perform database query on pets table
                cursor=database.query(TABLE_NAME,projection,null,null,null,null,null);
                break;
            case PET_ID:
                // For the PET_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.pets/pets/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = PetsContact.PetsEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        //database.close();
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return PetsContact.PetsEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetsContact.PetsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        //return null;
    }}

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return insertPet(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
        //return null;
    }
    private Uri insertPet(Uri uri, ContentValues values) {

        // TODO: Insert a new pet into the pets database table with the given ContentValues

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        SQLiteDatabase datbase=mDbHelper.getWritableDatabase();
        long id=datbase.insert(TABLE_NAME,null,values);
        if(id==-1)
            System.out.println("Values not inserted int he database");
        getContext().getContentResolver().notifyChange(uri,null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        int rowsDel=0;
        switch (match) {
            case PETS:
                // Delete all rows that match the selection and selection args
                rowsDel=database.delete(PetsContact.PetsEntry.TABLE_NAME, selection, selectionArgs);
                database.delete("sqlite_sequence","name=?", new String[]{PetsContact.PetsEntry.TABLE_NAME});
                if(rowsDel!=0)
                    getContext().getContentResolver().notifyChange(uri,null);
                return rowsDel;
            case PET_ID:
                // Delete a single row given by the ID in the URI
                selection = PetsContact.PetsEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDel=database.delete(PetsContact.PetsEntry.TABLE_NAME, selection, selectionArgs);
                if(rowsDel!=0)
                    getContext().getContentResolver().notifyChange(uri,null);
                return rowsDel;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);

        //return 0;
    }}

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match=sUriMatcher.match(uri);
        switch (match)
        {
            case PETS:
                return updatePet(uri,values,selection,selectionArgs);
            case PET_ID:
                selection = PetsContact.PetsEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updatePet(uri,values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Deletion not supported for"+uri);
        }
        //return 0;
    }
    private int updatePet(Uri uri,ContentValues values,String selection,String[] selectionArgs)
    {
        int num=0;
        SQLiteDatabase db=mDbHelper.getWritableDatabase();
        num=db.update(TABLE_NAME,values,selection,selectionArgs);
        if(num!=0)
            getContext().getContentResolver().notifyChange(uri,null);
        return num;
    }
}
