package com.example.android.inventoryapp;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.ParcelFileDescriptor;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import java.io.FileDescriptor;
import java.io.IOException;

import static com.example.android.inventoryapp.data.InventoryProvider.LOG_TAG;


/**
 * Created by JBurgos on 10/2/16.
 */

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the product data loader
     */
    private static final int EXISTING_PRODUCT_LOADER = 0;

    private static final int PICK_IMAGE_REQUEST = 0;

    /**
     * Content URI for the existing product (null if it's a new product)
     */
    private Uri mCurrentProductUri;

    /**
     * EditText field to enter the products name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the products description
     */
    private EditText mDescriptionEditText;

    /**
     * EditText field to enter the products price
     */
    private EditText mPriceEditText;

    /**
     * EditText field to enter the products quantity
     */
    private EditText mQuantityEditText;

    private TextView mFinalQuantityTextView;

    private Button mImagePathButton;

    private ImageView mImageView;

    private Uri mUri;


    /**
     * Boolean flag that keeps track of whether the product has been edited (true) or not (false)
     */
    private boolean mInventoryHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mInventoryHasChanged = true;
            return false;
        }
    };

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mDescriptionEditText = (EditText) findViewById(R.id.edit_product_description);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_stock_amount);
        mFinalQuantityTextView = (TextView) findViewById(R.id.quantity_text_view);
        mImagePathButton = (Button) findViewById(R.id.add_photo_button);
        mImageView = (ImageView) findViewById(R.id.edit_image);


        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mDescriptionEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mImagePathButton.setOnTouchListener(mTouchListener);

        FloatingActionButton fab_detail = (FloatingActionButton) findViewById(R.id.fab_detail);
        fab_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                orderMore();
            }
        });

        mImagePathButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;

                if (Build.VERSION.SDK_INT < 19) {
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                } else {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                }

                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new product or editing an existing one.
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();


        // If the intent DOES NOT contain a product content URI, then we know that we are
        // creating a new product in the inventory.
        if (mCurrentProductUri == null) {

            fab_detail.setVisibility(View.GONE);

            // This is a new product, so change the app bar to say "Add a Product"
            setTitle(getString(R.string.display_activity_title_new_product));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a product that hasn't been created yet.)
            invalidateOptionsMenu();

        } else {
            // Otherwise this is an existing product, so change app bar to say "Edit Product"
            setTitle(getString(R.string.display_activity_title_edit_product));

            // Initialize a loader to read the inventory data from the database
            // and display the current values in the editor
            getSupportLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);

        }

    }

    public void increment(View view) {

        String quantityString = mQuantityEditText.getText().toString().trim();

        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, R.string.addProductToAdjust, Toast.LENGTH_SHORT).show();
            return;
        }

        if (mCurrentProductUri == null && quantityString.length() > 0) {
            Toast.makeText(this, R.string.quanity_buttons_toast, Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantityString);

        int quantity = 0;

        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }

        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity);

        int newQ = quantity + 1;

        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, newQ);

        getContentResolver().update(mCurrentProductUri, values, null, null);

    }

    public void decrement(View view) {
        String quantityString = mQuantityEditText.getText().toString().trim();

        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, R.string.addProductToAdjust, Toast.LENGTH_SHORT).show();
            return;
        }

        if (mCurrentProductUri == null && quantityString.length() > 0) {
            Toast.makeText(this, R.string.quanity_buttons_toast, Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantityString);

        int quantity = 0;

        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }

        if (quantity == 0) {
            Toast.makeText(this, "Cant have nothing of something, Silly", Toast.LENGTH_SHORT).show();
            return;
        }

        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity);

        int newQ = quantity - 1;

        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, newQ);

        getContentResolver().update(mCurrentProductUri, values, null, null);

    }

    public void plusTwentyFive(View view) {

        String quantityString = mQuantityEditText.getText().toString().trim();

        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, R.string.addProductToAdjust, Toast.LENGTH_SHORT).show();
            return;
        }

        if (mCurrentProductUri == null && quantityString.length() > 0) {
            Toast.makeText(this, R.string.quanity_buttons_toast, Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantityString);

        int quantity = 0;

        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }

        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity);

        int newQ = quantity + 25;

        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, newQ);

        getContentResolver().update(mCurrentProductUri, values, null, null);

    }

    public void plusFifty(View view) {

        String quantityString = mQuantityEditText.getText().toString().trim();

        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, R.string.addProductToAdjust, Toast.LENGTH_SHORT).show();
            return;
        }

        if (mCurrentProductUri == null && quantityString.length() > 0) {
            Toast.makeText(this, R.string.quanity_buttons_toast, Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantityString);

        int quantity = 0;

        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }

        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity);

        int newQ = quantity + 50;

        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, newQ);

        getContentResolver().update(mCurrentProductUri, values, null, null);

    }

    public void plusSeventyFive(View view) {

        String quantityString = mQuantityEditText.getText().toString().trim();

        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, R.string.addProductToAdjust, Toast.LENGTH_SHORT).show();
            return;
        }

        if (mCurrentProductUri == null && quantityString.length() > 0) {
            Toast.makeText(this, R.string.quanity_buttons_toast, Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantityString);

        int quantity = 0;

        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }

        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity);

        int newQ = quantity + 75;

        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, newQ);


        getContentResolver().update(mCurrentProductUri, values, null, null);

    }

    public void plusOneHundred(View view) {

        String quantityString = mQuantityEditText.getText().toString().trim();

        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, R.string.addProductToAdjust, Toast.LENGTH_SHORT).show();
            return;
        }

        if (mCurrentProductUri == null && quantityString.length() > 0) {
            Toast.makeText(this, R.string.quanity_buttons_toast, Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantityString);

        int quantity = 0;

        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }

        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity);

        int newQ = quantity + 100;

        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, newQ);

        getContentResolver().update(mCurrentProductUri, values, null, null);

    }

    private int calculateStockNeeded(boolean addTwentyFive, boolean addFifty, boolean addSeventyFive,
                                     boolean addOneHundred) {
        int baseUnitNeded = 0;

        if (addTwentyFive) {
            baseUnitNeded = baseUnitNeded + 25;
        }

        if (addFifty) {
            baseUnitNeded = baseUnitNeded + 50;
        }

        if (addSeventyFive) {
            baseUnitNeded = baseUnitNeded + 75;
        }

        if (addOneHundred) {
            baseUnitNeded = baseUnitNeded + 100;
        }
        return baseUnitNeded;
    }


    public void orderMore() {

        String nameString = mNameEditText.getText().toString().trim();
        String descriptionString = mDescriptionEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();

        CheckBox addTwentyFiveCheckbox = (CheckBox) findViewById(R.id.plus_25_check);
        Boolean addTwentyFive = addTwentyFiveCheckbox.isChecked();

        CheckBox addFiftyCheckbox = (CheckBox) findViewById(R.id.plus_50_check);
        Boolean addFifty = addFiftyCheckbox.isChecked();

        CheckBox addSeventyFiveCheckbox = (CheckBox) findViewById(R.id.plus_75_check);
        Boolean addSeventyFive = addSeventyFiveCheckbox.isChecked();

        CheckBox addOneHundredCheckbox = (CheckBox) findViewById(R.id.plus_100_check);
        Boolean addOneHundred = addOneHundredCheckbox.isChecked();

        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(descriptionString) && TextUtils.isEmpty(priceString) && TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, R.string.addProductToAdjust, Toast.LENGTH_SHORT).show();
            return;
        }

        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_DESCRIPTION,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,};

        Cursor cursor = getContentResolver().query(
                mCurrentProductUri,
                projection,
                null,
                null,
                null);

        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
            int descriptionColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_DESCRIPTION);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);

            String name = cursor.getString(nameColumnIndex);
            String description = cursor.getString(descriptionColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);

            mNameEditText.setText(name);
            mDescriptionEditText.setText(description);
            mPriceEditText.setText(Double.toString(price));
            mQuantityEditText.setText(Integer.toString(quantity));
            mFinalQuantityTextView.setText(Integer.toString(quantity));

        }

        String priceMessage = "Name of Product: " + nameString;
        priceMessage += "\n";
        priceMessage += "\nCost of Product: " + priceString;
        priceMessage += "\n";
        priceMessage += "\nDescription of Product: " + descriptionString;
        priceMessage += "\n";
        priceMessage += "\nCurrent Amount in Stock: " + quantityString;
        priceMessage += "\n";
        priceMessage += "\nCurrent amount of stock asking for: " + calculateStockNeeded(addTwentyFive, addFifty,
                addSeventyFive, addOneHundred);
        priceMessage += "\n";
        priceMessage += "\nThank You for your continued support as our vendor!";

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_SUBJECT, "Inventory order for " + "Establishment x");
        intent.putExtra(Intent.EXTRA_TEXT, priceMessage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }


    }

    private void saveProduct() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String descriptionString = mDescriptionEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();

        if (nameString.length() == 0) {
            mNameEditText.setError("Name can\'t be empty");
            return;
        } else if (priceString.length() == 0) {
            mPriceEditText.setError("Invalid Price");
            return;
        } else if (descriptionString.length() == 0) {
            mDescriptionEditText.setError("Need Description");
            return;
        } else if (quantityString.length() == 0) {
            mQuantityEditText.setError("Invalid Quantity");
            return;
        } else if (mUri == null) {
            Toast.makeText(this, "Please add an Image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(InventoryEntry.COLUMN_PRODUCT_DESCRIPTION, descriptionString);
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, priceString);
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantityString);
        values.put(InventoryEntry.COLUMN_IMAGE, mUri.toString());

        double price = 0;
        double quantity = 0;

        if (!TextUtils.isEmpty(priceString)) {
            price = Double.parseDouble(priceString);
        }
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, price);

        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Double.parseDouble(quantityString);
        }
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity);

        // Determine if this is a new or existing product by checking if mCurrentProductUri is null or not
        if (mCurrentProductUri == null) {

            // This is a NEW product, so insert a new product into the provider,
            // returning the content URI for the new product.
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.detail_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.detail_insert_product_successful),
                        Toast.LENGTH_SHORT).show();

            }
        } else {
            // Otherwise this is an EXISTING product, so update the product with content URI: mCurrentProductUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentProductUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.detail_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.detail_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                mUri = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + mUri.toString());
                mImageView.setImageBitmap(getBitmapFromUri(mUri));
            }
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return image;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Error closing ParcelFile Descriptor");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save product to database
                saveProduct();

                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link InventoryActivity}.
                if (!mInventoryHasChanged) {
                    NavUtils.navigateUpFromSameTask(DetailActivity.this);
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
                                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mInventoryHasChanged) {
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
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all product attributes, define a projection that contains
        // all columns from the inventory table
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_DESCRIPTION,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_IMAGE,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,         // Query the content URI for the current product
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of product attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
            int descriptionColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_DESCRIPTION);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            int imageColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_IMAGE);


            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String description = cursor.getString(descriptionColumnIndex);
            mUri = Uri.parse(cursor.getString(imageColumnIndex));
            double price = cursor.getDouble(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);


            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mDescriptionEditText.setText(description);
            mPriceEditText.setText(Double.toString(price));
            mQuantityEditText.setText(Integer.toString(quantity));
            mFinalQuantityTextView.setText(Integer.toString(quantity));
            mImageView.setImageBitmap(getBitmapFromUri(mUri));

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mDescriptionEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mFinalQuantityTextView.setText("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product.
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
     * Prompt the user to confirm that they want to delete this product.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
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
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentProductUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.detail_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.detail_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

}

