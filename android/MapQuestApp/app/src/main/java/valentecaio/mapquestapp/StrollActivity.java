package valentecaio.mapquestapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class StrollActivity extends AppCompatActivity {
    private ArrayList<Balade> serverBalades;
    private ArrayList<Balade> localBalades = new ArrayList<>();
    public DAO database = new DAO(this);
    public AppFileManager afm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stroll);

        verify_permissions();

        // load database to populate listView
        this.database.loadDatabase();

        // read all balades from internal database
        afm = new AppFileManager(getApplicationContext());
        localBalades = afm.listDownloadedBalades();

        configureListView();

        // uncomment following line to delete all data when loading application
        //afm.deleteAll();
    }

    public void configureListView(){
        final ListView balades_listView = (ListView)findViewById(R.id.scrolls_list_view);
        balades_listView.setItemsCanFocus(false);

        // if cant load serverBalades, show only localBalades
        final ArrayList<Balade> lv_source =  serverBalades!=null ? serverBalades : localBalades;

        balades_listView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Log.i("ONCLICK_CELL", "Click ListItem Number " + position);

                // stock clicked balade as global variables
                Balade chosen_balade = lv_source.get(position);

                // try/catch to avoid error when clicking in not downloaded balade
                try {
                    // load balade points/medias before performing intent
                    chosen_balade = afm.readBalade(chosen_balade.getId());
                    GlobalVariables.getInstance().balade = chosen_balade;

                    // go to mapActivity
                    Intent i = new Intent(StrollActivity.this, MapActivity.class);
                    startActivity(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // populate listView
        BaladesAdapter adapter = new BaladesAdapter(this, lv_source, this);
        balades_listView.setAdapter(adapter);
    }

    public void setBaladesArray(ArrayList<Balade> balades){
        // useful information for debug
        for(Balade b: balades){
            Log.i("STROLL_ACTIVITY", b.toString());
        }

        this.serverBalades = balades;
        configureListView();
    }

    public void enableButtons(boolean enabled) {
        Log.i("ENABLE_BUTTONS", "" + enabled);
    }

    private void verify_permissions(){
        String[] permissions = {
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.ACCESS_NETWORK_STATE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.ACCESS_WIFI_STATE,
                android.Manifest.permission.CAMERA};

        ArrayList<String> permissionsToAsk = new ArrayList<String>();
        for(String permission: permissions){
            if(ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                permissionsToAsk.add(permission);
            }
        }

        // ask permission
        if (permissionsToAsk.size() > 0) {
            String[] request = new String[permissionsToAsk.size()];
            request = permissionsToAsk.toArray(request);
            ActivityCompat.requestPermissions(this, request, 1);
        }
    }
}
