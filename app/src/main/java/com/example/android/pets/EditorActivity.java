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
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pets.data.PetsContact;

import static android.R.attr.id;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** EditText field to enter the pet's name */
    private EditText mNameEditText;

    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;

    /** EditText field to enter the pet's weight */
    private EditText mWeightEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private static int PET_LOADER=0;
    private int mGender = 0;
    private Uri u=null;
    long id_pet;
    public boolean hasPetChanged=false;
    private View.OnTouchListener touchListener= new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            hasPetChanged=true;
            return false;
        }
    };
    private static final String[] project=new String[]{PetsContact.PetsEntry._ID,
            PetsContact.PetsEntry.COLUMN_PET_NAME,
            PetsContact.PetsEntry.COLUMN_PET_BREED,
            PetsContact.PetsEntry.COLUMN_PET_GENDER,
            PetsContact.PetsEntry.COLUMN_PET_WEIGHT};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);
        setupSpinner();
        mNameEditText.setOnTouchListener(touchListener);
        mBreedEditText.setOnTouchListener(touchListener);
        mWeightEditText.setOnTouchListener(touchListener);
        mGenderSpinner.setOnTouchListener(touchListener);
        Intent i=getIntent();
        u=i.getData();
        if(u==null)
            setTitle("Add a Pet");
        else {
            setTitle("Edit Pet");
            id_pet=Long.valueOf(u.getLastPathSegment());
            System.out.println(id);
            getLoaderManager().initLoader(PET_LOADER,null,this);
            invalidateOptionsMenu();
        }

    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetsContact.PetsEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetsContact.PetsEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetsContact.PetsEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if(u==null)
        {
            MenuItem m=menu.findItem(R.id.action_delete);
            m.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Do nothing for now
                savePet();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                if (!hasPetChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                ShowUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
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
        int n=getContentResolver().delete(u,null,null);
        if(n==-1)
        {
            Toast.makeText(this,R.string.editor_delete_pet_failed, Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this,R.string.editor_delete_pet_successful, Toast.LENGTH_SHORT).show();
        Intent i=new Intent(this,CatalogActivity.class);
        startActivity(i);}
    }
    private void ShowUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
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
    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!hasPetChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        ShowUnsavedChangesDialog(discardButtonClickListener);
    }
    public void savePet()
    {
        EditText etname=(EditText)findViewById(R.id.edit_pet_name);
        EditText etbreed=(EditText)findViewById(R.id.edit_pet_breed);
        Spinner spgender=(Spinner)findViewById(R.id.spinner_gender);
        EditText etweight=(EditText)findViewById(R.id.edit_pet_weight);
        String gender=spgender.getSelectedItem().toString();
        ContentValues values=new ContentValues();
        int genderval;
        System.out.println(gender);
        if(TextUtils.isEmpty(etname.getText().toString())||TextUtils.isEmpty(etbreed.getText().toString())||TextUtils.isEmpty(etweight.getText().toString())||
        gender.equals("Unknown"))
        {
            Toast.makeText(getApplicationContext(), "There was a problem!! Check the values again", Toast.LENGTH_LONG).show();
            return;
        }
        switch (gender)
        {
            case "Male":
                genderval= PetsContact.PetsEntry.GENDER_MALE;
                values.put(PetsContact.PetsEntry.COLUMN_PET_GENDER,genderval);
                break;
            case "Female":
                genderval= PetsContact.PetsEntry.GENDER_FEMALE;
                values.put(PetsContact.PetsEntry.COLUMN_PET_GENDER,genderval);
                break;
        }
        values.put(PetsContact.PetsEntry.COLUMN_PET_NAME,etname.getText().toString());
        values.put(PetsContact.PetsEntry.COLUMN_PET_BREED,etbreed.getText().toString());
        values.put(PetsContact.PetsEntry.COLUMN_PET_WEIGHT,Integer.parseInt(etweight.getText().toString()));
        if(u!=null)
        {
            int n=getContentResolver().update(u,values,null,null);
            if(n==0)
                Toast.makeText(getApplicationContext(),"There was a problem with updation of the pet",Toast.LENGTH_LONG).show();
            else
                Toast.makeText(getApplicationContext(),"Pet updated successfully",Toast.LENGTH_LONG).show();
        }
        else{
        Uri uri=null;
        uri=getContentResolver().insert(PetsContact.PetsEntry.CONTENT_URI,values);
        if(uri==null)
            Toast.makeText(getApplicationContext(),getString(R.string.editor_save_pets_unsuccessful),Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getApplicationContext(),getString(R.string.editor_save_pets_successful),Toast.LENGTH_LONG).show();}
        Intent i=new Intent(this,CatalogActivity.class);
        startActivity(i);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,u,project,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data!=null&&data.moveToFirst()){
        int namecolumnindex=data.getColumnIndexOrThrow(PetsContact.PetsEntry.COLUMN_PET_NAME);
        int breedcolumnindex=data.getColumnIndexOrThrow(PetsContact.PetsEntry.COLUMN_PET_BREED);
        int gendercolumnindex=data.getColumnIndexOrThrow(PetsContact.PetsEntry.COLUMN_PET_GENDER);
        int weightcolumnindex=data.getColumnIndexOrThrow(PetsContact.PetsEntry.COLUMN_PET_WEIGHT);
        System.out.println(namecolumnindex+" "+breedcolumnindex+" "+gendercolumnindex+" "+weightcolumnindex);
        String name=data.getString(1);
        String breed=data.getString(2);
        int gender=data.getInt(3);
        int weight=data.getInt(4);
        mNameEditText.setText(name);
        mBreedEditText.setText(breed);
        switch (gender)
        {
            case 0:
                mGenderSpinner.setSelection(0);
                break;
            case 1:
                mGenderSpinner.setSelection(1);
                break;
            case 2:
                mGenderSpinner.setSelection(2);
                break;
        }
        mWeightEditText.setText(String.valueOf(weight));}
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}