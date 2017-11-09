/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.android.pets.data.PetsAdapter;
import com.example.android.pets.data.PetsContact;
import com.example.android.pets.data.PetsDBHelper;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor> {
    PetsDBHelper mDbHelper=new PetsDBHelper(this);
    private static int PET_LOADER=0;
    PetsAdapter mCursorAadapter;
    private static final String[] project=new String[]{PetsContact.PetsEntry._ID,
            PetsContact.PetsEntry.COLUMN_PET_NAME,
            PetsContact.PetsEntry.COLUMN_PET_BREED};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
        ListView lv=(ListView)findViewById(R.id.list);
        mCursorAadapter=new PetsAdapter(this,null);
        lv.setAdapter(mCursorAadapter);
        getLoaderManager().initLoader(PET_LOADER,null,this);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i=new Intent(getApplicationContext(),EditorActivity.class);
                Uri u= PetsContact.PetsEntry.CONTENT_URI;
                u= ContentUris.withAppendedId(u,id);
                i.setData(u);
                startActivity(i);
            }
        });
    }

    private void insertDummyData()
    {
        ContentValues values=new ContentValues();
        values.put(PetsContact.PetsEntry.COLUMN_PET_NAME,"Sankhadip");
        values.put(PetsContact.PetsEntry.COLUMN_PET_BREED,"raaste ka doggy");
        values.put(PetsContact.PetsEntry.COLUMN_PET_GENDER,1);
        values.put(PetsContact.PetsEntry.COLUMN_PET_WEIGHT,100);
        Uri uri=null;
        uri=getContentResolver().insert(PetsContact.PetsEntry.CONTENT_URI,values);
        if(uri==null)
            Toast.makeText(getApplicationContext(),getString(R.string.editor_save_pets_unsuccessful),Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getApplicationContext(),getString(R.string.editor_save_pets_successful),Toast.LENGTH_LONG).show();
    }
    private void deleteAllData()
    {
        showDeleteConfirmationDialog();
    }
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete All Pets?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deletePet();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deletePet() {
        // TODO: Implement this method
        int n=getContentResolver().delete(PetsContact.PetsEntry.CONTENT_URI,null,null);
        if(n==-1)
        {
            Toast.makeText(this,"Error in deleting all pets", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this,"Deletion of Pets Successful", Toast.LENGTH_SHORT).show();}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                // Do nothing for now
                insertDummyData();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Do nothing for now
                deleteAllData();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                PetsContact.PetsEntry.CONTENT_URI,
                project,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
        RelativeLayout rl=(RelativeLayout)findViewById(R.id.empty_view);
        if(data.getCount()!=0)
        {
            rl.setVisibility(View.GONE);
        }
        else
            rl.setVisibility(View.VISIBLE);
        mCursorAadapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {
        mCursorAadapter.swapCursor(null);
    }
}
