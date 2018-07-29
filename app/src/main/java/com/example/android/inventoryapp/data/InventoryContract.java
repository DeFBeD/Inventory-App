package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by JBurgos on 10/2/16.
 */

public class InventoryContract {


    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private InventoryContract() {
    }

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.inventoryapp/inventory/ is a valid path for
     * looking at product data. content://com.example.android.inventoryapp/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_INVENTORY = "inventory";

    /**
     * Inner class that defines constant values for the inventory database table.
     * Each entry in the table represents a single pet.
     */

    public static final class InventoryEntry implements BaseColumns {

        /**
         * The content URI to access the product data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of products.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single product.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        /**
         * Name of database table for products
         */
        public final static String TABLE_NAME = "inventory";

        /**
         * Unique ID number for the product (only for use in the database table).
         * <p>
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the product.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_NAME = "name";

        /**
         * description of the product.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_DESCRIPTION = "description";

        /**
         * price of the product.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_PRICE = "price";

        /**
         * stock of the product.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_QUANTITY = "quantity";

        public static final String COLUMN_IMAGE = "image";


        public static String getIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);


        }
}

}
