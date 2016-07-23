package io.github.leonawicz.inventory;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import io.github.leonawicz.inventory.invdb.InvCursorAdapter;
import io.github.leonawicz.inventory.invdb.InvDbHelper;

public class MainActivity extends AppCompatActivity {

    public InvCursorAdapter adapter;
    public ListView listView;
    public InvDbHelper dbHelper;
    public TextView instructView;

    static class ViewHolder{
        TextView rowId;
        TextView part;
        TextView quantity;
        TextView price;
        //ImageView thumbnail;
    }

    ViewHolder holder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DbApplication app = (DbApplication) getApplicationContext();
        listView = app.listView;
        listView = (ListView) findViewById(R.id.list);
        dbHelper = app.dbHelper;
        adapter = app.adapter;

        instructView = (TextView) findViewById(R.id.instructions);
        refreshInstructionsView(adapter, instructView);

        holder = new ViewHolder();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                holder.rowId = (TextView) view.findViewById(R.id.row_id);
                holder.part = (TextView) view.findViewById(R.id.part);
                holder.quantity = (TextView) view.findViewById(R.id.quantity);
                holder.price = (TextView) view.findViewById(R.id.saleprice);

                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("rowId", holder.rowId.getText().toString());
                intent.putExtra("part", holder.part.getText().toString());
                intent.putExtra("qty", holder.quantity.getText().toString());
                intent.putExtra("price", holder.price.getText().toString());
                startActivity(intent);
            }
        });

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                listView.setAdapter(adapter);
            }
        });

        Button btn = (Button) findViewById(R.id.add_product_btn);
        btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddProductActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onResume(){
        super.onResume();
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                DbApplication app = (DbApplication) getApplicationContext();
                app.refreshListView();
                refreshInstructionsView(adapter, instructView);
            }
        });
    }

    @Override
    public void onStop(){
        super.onStop();
        //cursor.close();
    }

    public void refreshInstructionsView(InvCursorAdapter adapter, TextView view) {
        boolean hasItems = adapter.getCount() > 0;
        if (hasItems) {
            view.setVisibility(View.INVISIBLE);
            view.setHeight(0);
        } else {
            view.setVisibility(View.VISIBLE);
            view.setText(R.string.instructions);
        }
    }

}