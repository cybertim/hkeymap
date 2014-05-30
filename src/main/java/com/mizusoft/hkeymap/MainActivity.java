package com.mizusoft.hkeymap;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;

/**
 *
 * @author Tim
 */
public class MainActivity extends Activity {

    private final String DEFAULT_LOC = "/system/usr/keylayout/sec_keypad.kl";
    private final static int PICK_KEY_CODE = 111;
    private final static String PICK_KEY_ID = "ListID";
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ArrayList<String> values = new ArrayList<String>();
        values.add("Loading...");
        this.sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        this.listView = (ListView) findViewById(R.id.list);
        this.adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, values);
        this.listView.setAdapter(adapter);
        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> av, View view, int i, long l) {
                startActivityForResult(new Intent(view.getContext(), KeySelectActivity.class).putExtra(PICK_KEY_ID, i), PICK_KEY_CODE);
            }
        });
        reload();
    }

    private void reload() {
        KeyParser.asyncLoadKeymap(this, this.sharedPref.getString("prefLoc", DEFAULT_LOC));
    }

    private void save() {
        ArrayList<String> values = new ArrayList<String>();
        for (int i = 0; i < this.adapter.getCount(); i++) {
            values.add(this.adapter.getItem(i));
        }
        KeyParser.asyncSaveKeymap(this, this.sharedPref.getString("prefLoc", DEFAULT_LOC), values);
    }

    public void updateListAdapter(ArrayList<String> items) {
        adapter.clear();
        adapter.addAll(items);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i(MainActivity.class.getName(), "*** KeyCode: " + keyCode + ", ScanCode: " + event.getScanCode() + ", KeyEvent: " + KeyEvent.keyCodeToString(keyCode));
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                startActivity(new Intent(this, PrefActivity.class));
                return true;
            case R.id.reload:
                reload();
                return true;
            case R.id.save:
                save();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_KEY_CODE) {
            if (resultCode == RESULT_OK) {
                String keyCode = data.getExtras().getString(KeySelectActivity.KEY_CODE);
                int listId = data.getExtras().getInt(PICK_KEY_ID);
                String oldEntry = this.adapter.getItem(listId);
                this.adapter.insert(this.adapter.getItem(listId).split(":")[0] + ":" + keyCode, listId);
                this.adapter.remove(oldEntry);
                this.adapter.notifyDataSetChanged();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
