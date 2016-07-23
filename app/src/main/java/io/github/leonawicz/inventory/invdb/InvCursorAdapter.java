package io.github.leonawicz.inventory.invdb;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.Locale;

import io.github.leonawicz.inventory.DbApplication;
import io.github.leonawicz.inventory.InvContract;
import io.github.leonawicz.inventory.MainActivity;
import io.github.leonawicz.inventory.R;

public class InvCursorAdapter extends CursorAdapter {
    private Context mContext;
    private LayoutInflater cursorInflater;
    private Button btn;
    InvDbHelper dbHelper;

    public InvCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
        cursorInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        this.mContext=context;
    }

    @Override
    public void bindView(final View view, Context context, final Cursor cursor) {
        TextView idView = (TextView) view.findViewById(R.id.row_id);
        String id = cursor.getString(cursor.getColumnIndex(InvContract.InvEntry._ID));
        idView.setText(id);

        TextView partView = (TextView) view.findViewById(R.id.part);
        String part = cursor.getString(cursor.getColumnIndex(InvContract.InvEntry.COLNAME_ITEM));
        partView.setText(part);

        TextView qtyView = (TextView) view.findViewById(R.id.quantity);
        String qty = cursor.getString(cursor.getColumnIndex(InvContract.InvEntry.COLNAME_QTY));
        qtyView.setText(qty);
        final int newQuantity = Integer.valueOf(qty) - 1;

        TextView priceView = (TextView) view.findViewById(R.id.saleprice);
        String price = cursor.getString(cursor.getColumnIndex(InvContract.InvEntry.COLNAME_PRICE));
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.getDefault());
        priceView.setText(format.format(Double.valueOf(price)));

        btn = (Button) view.findViewById(R.id.list_item_sale_btn);
        final int pos = cursor.getPosition();
        final int rowId = Integer.valueOf(id);
        btn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                cursor.moveToPosition(pos);
                Log.v("InvCursorAdapter", "onClick pos: " + pos);
                Log.v("InvCursorAdapter", "onClick new qty: " + newQuantity);
                if (newQuantity >= 0) {
                    DbApplication app = ((DbApplication) view.getContext().getApplicationContext());
                    dbHelper = app.dbHelper;
                    dbHelper.updateTableRowById(null, null, newQuantity, null, null, rowId);
                    app.refreshListView();
                } else {
                    Toast.makeText(view.getContext(), R.string.decrease_quantity_failure,
                            Toast.LENGTH_SHORT).show();
                }
            }});

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return cursorInflater.inflate(R.layout.list_item, parent, false);
    }
}
