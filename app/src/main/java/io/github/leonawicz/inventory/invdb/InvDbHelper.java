package io.github.leonawicz.inventory.invdb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

import io.github.leonawicz.inventory.InvContract;

public class InvDbHelper extends SQLiteOpenHelper {

    SQLiteDatabase db;

    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String DBL_TYPE = " REAL";
    private static final String BITMAP_TYPE = " BLOB";
    private static final String SEP = ",";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + InvContract.InvEntry.TBLNAME + " (" +
                    InvContract.InvEntry._ID + " INTEGER PRIMARY KEY," +
                    InvContract.InvEntry.COLNAME_ITEM + TEXT_TYPE + SEP +
                    InvContract.InvEntry.COLNAME_SUP + TEXT_TYPE + SEP +
                    InvContract.InvEntry.COLNAME_QTY + INT_TYPE + SEP +
                    InvContract.InvEntry.COLNAME_COST + DBL_TYPE + SEP +
                    InvContract.InvEntry.COLNAME_PRICE + DBL_TYPE + SEP +
                    InvContract.InvEntry.COLNAME_BITMAP + BITMAP_TYPE + " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + InvContract.InvEntry.TBLNAME;

    public InvDbHelper(Context context) {
        super(context, InvContract.DATABASE_NAME, null, InvContract.DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public long insertTableRow(String part, String supplier, int quantity, double cost, double price, Bitmap image) {
        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(InvContract.InvEntry.COLNAME_ITEM, part);
        values.put(InvContract.InvEntry.COLNAME_SUP, supplier);
        values.put(InvContract.InvEntry.COLNAME_QTY, quantity);
        values.put(InvContract.InvEntry.COLNAME_COST, cost);
        values.put(InvContract.InvEntry.COLNAME_PRICE, price);
        values.put(InvContract.InvEntry.COLNAME_BITMAP, DbBitmapUtility.getBytes(image));

        long newRowId;
        newRowId = db.insert(
                InvContract.InvEntry.TBLNAME,
                null,
                values);
        return newRowId;
    }

    public Cursor allRows() {
        db = this.getReadableDatabase();
        String[] proj = {
                InvContract.InvEntry._ID,
                InvContract.InvEntry.COLNAME_ITEM,
                InvContract.InvEntry.COLNAME_SUP,
                InvContract.InvEntry.COLNAME_QTY,
                InvContract.InvEntry.COLNAME_COST,
                InvContract.InvEntry.COLNAME_PRICE,
                InvContract.InvEntry.COLNAME_BITMAP
        };

        Cursor c = db.query(
                InvContract.InvEntry.TBLNAME,
                proj, null, null, null, null, null);
        c.moveToFirst();
        return c;
    }

    public Cursor rowById(int id) {
        db = this.getReadableDatabase();
        String[] proj = {
                InvContract.InvEntry._ID,
                InvContract.InvEntry.COLNAME_ITEM,
                InvContract.InvEntry.COLNAME_SUP,
                InvContract.InvEntry.COLNAME_QTY,
                InvContract.InvEntry.COLNAME_COST,
                InvContract.InvEntry.COLNAME_PRICE,
                InvContract.InvEntry.COLNAME_BITMAP
        };

        Cursor c = db.query(
                InvContract.InvEntry.TBLNAME,
                proj, InvContract.InvEntry._ID + "=" + id, null, null, null, null);
        c.moveToFirst();
        return c;
    }

    public void deleteRowById(int id) {
        db = this.getWritableDatabase();
        db.delete(InvContract.InvEntry.TBLNAME, "_ID=?", new String[]{Integer.toString(id)});
        db.close();
    }

    public int updateTableRowById(String part, String supplier, Integer quantity, Double cost, Double price, long id) {
        db = this.getReadableDatabase();
        ContentValues values = new ContentValues();
        if (part != null)
            values.put(InvContract.InvEntry.COLNAME_ITEM, part);
        if (supplier != null)
            values.put(InvContract.InvEntry.COLNAME_SUP, supplier);
        if (quantity != null)
            values.put(InvContract.InvEntry.COLNAME_QTY, quantity);
        if (cost != null)
            values.put(InvContract.InvEntry.COLNAME_COST, cost);
        if (price != null)
            values.put(InvContract.InvEntry.COLNAME_PRICE, price);

        String selection = InvContract.InvEntry._ID + "=?";
        String[] selectionArgs = {id + ""};

        int count = db.update(
                InvContract.InvEntry.TBLNAME,
                values,
                selection,
                selectionArgs);
        db.close();
        return count;
    }

    public void deleteTableEntries(){
        db = this.getWritableDatabase();
        db.delete(InvContract.InvEntry.TBLNAME, null, null);
        db.close();
    }

    public void deleteDatabase(Context context) {
        context.deleteDatabase(InvContract.InvEntry.TBLNAME);
    }
}
