package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;


/**
 * Created by JBurgos on 10/2/16.
 */

public class InventoryCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link InventoryCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // The newView method is used to inflate a new view and return it,
        // you don't bind any data to the view at this point.
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current product can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        // Find fields to populate in inflated template
        TextView tvName = (TextView) view.findViewById(R.id.pName);
        TextView tvPrice = (TextView) view.findViewById(R.id.pPrice);
        final TextView tvQuantity = (TextView) view.findViewById(R.id.number_quantity_view);
        RelativeLayout parentView = (RelativeLayout) view.findViewById(R.id.list_product_view);

        // find the columns of product attributes that we are interested in
        int idColumnIndex = cursor.getColumnIndex(InventoryEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);

        // read the attributes from the cursor for the current product
        final int rowId = cursor.getInt(idColumnIndex);
        String productName = cursor.getString(nameColumnIndex);
        String productPrice = cursor.getString(priceColumnIndex);
        String productQuantity = cursor.getString(quantityColumnIndex);

        // update the Textview with the attributes for the current product
        tvName.setText(productName);
        tvPrice.setText(productPrice);
        tvQuantity.setText(productQuantity);

        parentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open Editor activity
                Intent intent = new Intent(context, DetailActivity.class);

                // Form the content URI that represents clicked product
                Uri currentProductUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI,
                        rowId);

                // Set the URI on the data field of the intent
                intent.setData(currentProductUri);

                context.startActivity(intent);
            }
        });

        Button sellButton = (Button) view.findViewById(R.id.sellButton);
        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int quantity = Integer.parseInt(tvQuantity.getText().toString());

                if (quantity == 0) {
                    Toast.makeText(context, R.string.no_more_stock,
                            Toast.LENGTH_SHORT).show();
                } else if (quantity > 0) {
                    quantity = quantity - 1;

                    String quantityString = Integer.toString(quantity);

                    ContentValues values = new ContentValues();
                    values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantityString);

                    Uri currentProductUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, rowId);

                    Log.v("Button", currentProductUri.toString());

                    int rowsAffected = context.getContentResolver().update(currentProductUri, values,
                            null, null);

                    if (rowsAffected != 0) {
                        // update text view if db update is successful
                        tvQuantity.setText(quantityString);

                    } else {
                        Toast.makeText(context, "Failed to update", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

}

