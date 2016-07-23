package io.github.leonawicz.inventory;

import android.app.Application;
import android.database.Cursor;
import android.widget.ListView;

import io.github.leonawicz.inventory.invdb.InvCursorAdapter;
import io.github.leonawicz.inventory.invdb.InvDbHelper;

public class DbApplication extends Application {
    public InvDbHelper dbHelper;
    public Cursor cursor;
    public InvCursorAdapter adapter;
    public ListView listView;

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new InvDbHelper(this);
        cursor = dbHelper.allRows();
        adapter = new InvCursorAdapter(this, cursor, 0);
        listView = new ListView(this);
    }

    public void refreshListView() {
        cursor = dbHelper.allRows();
        adapter.changeCursor(cursor);
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
    }
}
