package io.github.leonawicz.inventory;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.Locale;

import io.github.leonawicz.inventory.invdb.DbBitmapUtility;
import io.github.leonawicz.inventory.invdb.InvDbHelper;

public class DetailActivity extends AppCompatActivity {

    final static private String PURCHASING_DEPT_PHONE = "2125551212";
    InvDbHelper dbHelper;

    static class ViewHolder {
        TextView part;
        TextView supplier;
        TextView quantity;
        TextView cost;
        TextView price;
        EditText adjQty;
        ImageView thumbnail;
    }

    ViewHolder holder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details);

        holder = new ViewHolder();
        holder.part = (TextView) findViewById(R.id.details_part);
        holder.supplier = (TextView) findViewById(R.id.details_supplier);
        holder.quantity = (TextView) findViewById(R.id.details_quantity);
        holder.cost = (TextView) findViewById(R.id.details_purchaseprice);
        holder.price = (TextView) findViewById(R.id.details_saleprice);
        holder.adjQty = (EditText) findViewById(R.id.adjust_quantity);
        holder.thumbnail = (ImageView) findViewById(R.id.details_thumbnail);

        int qty = Integer.valueOf(getIntent().getStringExtra("qty"));
        final String prefixQty = getString(R.string.prefix_quantity) + " ";
        String sQty = prefixQty + qty;
        String sPart = getString(R.string.prefix_part) + " " + getIntent().getStringExtra("part");
        String sPrice = getIntent().getStringExtra("price");

        holder.part.setText(sPart);
        holder.quantity.setText(sQty);
        holder.price.setText(sPrice);

        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.getDefault());
        final int rowId = Integer.valueOf(getIntent().getStringExtra("rowId"));

        final DbApplication app = (DbApplication) getApplicationContext();
        dbHelper = app.dbHelper;
        Cursor c = dbHelper.rowById(rowId);

        String sSupplier = getString(R.string.prefix_supplier) + " " + c.getString(
                c.getColumnIndexOrThrow(InvContract.InvEntry.COLNAME_SUP));
        String sCost = c.getString(c.getColumnIndexOrThrow(InvContract.InvEntry.COLNAME_COST));
        holder.thumbnail.setImageBitmap(DbBitmapUtility.getImage(
                c.getBlob(c.getColumnIndexOrThrow(InvContract.InvEntry.COLNAME_BITMAP))));
        holder.supplier.setText(sSupplier);
        holder.cost.setText(format.format(Double.valueOf(sCost)));
        c.close();

        Button btnQty = (Button) findViewById(R.id.adjust_quantity_btn);
        btnQty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String qtyChange = holder.adjQty.getText().toString();
                int qty = Integer.valueOf(holder.quantity.getText().toString().substring(prefixQty.length()));
                int newQuantity = -1;
                try {

                    newQuantity = qty + Integer.valueOf(qtyChange);
                    Log.v("DetailActivity", newQuantity + "");
                    if (newQuantity < 0) {
                        Toast.makeText(DetailActivity.this,
                                R.string.quantity_adjust_failure, Toast.LENGTH_LONG).show();
                    } else {
                        int count = dbHelper.updateTableRowById(
                                null, null, newQuantity, null, null, rowId);
                        String sQtyNew = prefixQty + newQuantity;
                        holder.quantity.setText(sQtyNew);
                        holder.adjQty.setText("");
                        Toast.makeText(DetailActivity.this,
                                R.string.quantity_adjust_success, Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(DetailActivity.this,
                            R.string.invalid_numbers2, Toast.LENGTH_SHORT).show();
                }

            }
        });

        Button btn1down = (Button) findViewById(R.id.adjust_1down_btn);
        btn1down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int qty = Integer.valueOf(holder.quantity.getText().toString().substring(prefixQty.length()));
                if (qty < 1) {
                    Toast.makeText(DetailActivity.this,
                            R.string.quantity_adjust_failure, Toast.LENGTH_LONG).show();
                } else {
                    int count = dbHelper.updateTableRowById(
                            null, null, qty - 1, null, null, rowId);
                    String sQtyNew = prefixQty + (qty - 1);
                    holder.quantity.setText(sQtyNew);
                    Toast.makeText(DetailActivity.this,
                            R.string.quantity_adjust_success, Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button btn1up = (Button) findViewById(R.id.adjust_1up_btn);
        btn1up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int qty = Integer.valueOf(holder.quantity.getText().toString().substring(prefixQty.length()));
                int count = dbHelper.updateTableRowById(
                        null, null, qty + 1, null, null, rowId);
                String sQtyNew = prefixQty + (qty + 1);
                holder.quantity.setText(sQtyNew);
                Toast.makeText(DetailActivity.this,
                        R.string.quantity_adjust_success, Toast.LENGTH_SHORT).show();
            }
        });

        Button btnDelete = (Button) findViewById(R.id.delete_btn);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(DetailActivity.this)
                        .setTitle("Delete Record")
                        .setMessage("Are you sure you want to permanently delete this part record?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                dbHelper.deleteRowById(rowId);
                                app.refreshListView();
                                Intent intent = new Intent(DetailActivity.this, MainActivity.class);
                                startActivity(intent);
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });

        Button btnBuy = (Button) findViewById(R.id.purchase_btn);
        btnBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialPhoneNumber(PURCHASING_DEPT_PHONE);
            }
        });
    }

    public void dialPhoneNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent intent = NavUtils.getParentActivityIntent(this);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this, intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
