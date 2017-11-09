package com.example.android.pets.data;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.pets.R;

/**
 * Created by Prashant on 7/3/2017.
 */

public class PetsAdapter extends CursorAdapter {
    public PetsAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.custom_layout_catalog,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView name=(TextView)view.findViewById(R.id.name_text_view);
        TextView breed=(TextView)view.findViewById(R.id.breed_text_view);
        int namecolumn=cursor.getColumnIndexOrThrow(PetsContact.PetsEntry.COLUMN_PET_NAME);
        int breedcolumn=cursor.getColumnIndexOrThrow(PetsContact.PetsEntry.COLUMN_PET_BREED);
        name.setText(cursor.getString(namecolumn));
        breed.setText(cursor.getString(breedcolumn));
    }
}
