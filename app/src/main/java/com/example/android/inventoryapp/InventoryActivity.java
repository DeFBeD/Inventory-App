package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import static android.R.attr.id;

public class InventoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // Identifies a particular Loader being used in this component
    private static final int PRODUCT_LOADER = 0;

    InventoryCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // Setup FAB to open DetailActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InventoryActivity.this, DetailActivity.class);
                startActivity(intent);
            }
        });

        //find a ListView that will be populated with the product data
        ListView inventoryListView = (ListView) findViewById(R.id.list);

        //find and set empty view on ListView, so it shows when the list has 0 items
        View emptyView = findViewById(R.id.empty_view);
        inventoryListView.setEmptyView(emptyView);

        //Setup an adapter to create a list for each row of product data in the cursor
        //There is no product data yet (until the loader finishes) so pass in null for the cursor
        mCursorAdapter = new InventoryCursorAdapter(this, null);
        inventoryListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        inventoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(InventoryActivity.this, DetailActivity.class);

                // Form the content URI that represents the specific product that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link InventoryEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.inventoryapp/inventory/2"
                // if the product with ID 2 was clicked on.
                Uri currentProductUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentProductUri);

                // Launch the {@link EditorActivity} to display the data for the current product.
                startActivity(intent);
            }
        });

        //kick off loader
        getSupportLoaderManager().initLoader(PRODUCT_LOADER, null, this);

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

            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllProducts();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Helper method to delete all products in the database.
     */
    private void deleteAllProducts() {
        int rowsDeleted = getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
        Log.v("InventoryActivity", rowsDeleted + " rows deleted from inventory database");
    }



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_DESCRIPTION,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_IMAGE,



        };

        // this method will execute the Content Providers query method on a background thread
        return new CursorLoader(
                this,   // Parent activity context
                InventoryEntry.CONTENT_URI,        // Table to query
                projection,     // Projection to return
                null,            // No selection clause
                null,            // No selection arguments
                null             // Default sort order
        );

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //update with this new cursor containing update product data
        mCursorAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // callback called when the data needs to be deleted
        mCursorAdapter.changeCursor(null);
    }
}
