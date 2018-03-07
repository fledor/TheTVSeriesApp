package com.thetvguys.thetvseriesnotifier.app;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;

import org.json.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.thetvguys.thetvseriesnotifier.app.auth.AuthUiActivity;

@SuppressWarnings({"unused", "unchecked"})
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    boolean test = false;

    /*
    *   die MenuItems und der ActionButton werden beim erstellen der App-Instanz erstellt und werden
    *   dann später weiterverwendet, um z.B. die Sichtbarkeit zu verändern. Deswegen müssen diese
    *   Variablen in der gesamten Klasse als Attribute aufrufbar sein.
    */
    MenuItem menuItemSearch;
    MenuItem menuItemOptions;
    FloatingActionButton fabWatchlist;
    FloatingActionButton fabWatchlistR;


    /* TODO: remove this and its usages */
    int important = 0;

    /*
    *   Die Suche ist anfangs nicht aktiv, sodass die Suchansicht auch nicht gezeigt werden soll. Dafür
    *   wird hier searchActive auf false gesetzt, was dann in  onCreateOptionsMenu abgefragt wird,
    *   sobald die App startet und das obere Menü erscheint.
    */
    boolean searchActive = false;


    String watchlist;
    String currentSeriesId = "";

    
/*
*   Dieser String beeinflusst durch die Verwendung in anderen Methoden die Anfragen an die API.
*   In diesem Fall wird die Sprache auf Englisch gesetzt, was in der App in einem Menüpunkt geändert
*   werden kann.
*/    

    String lang = "en";

    String localSeriesName = "Service temporarily unavailable";
    String localOverview = "We apologize for the inconvenience";

    int control = 0;


/*
*   Diese Methode wird aufgerufen, sobald die App startet und die MainActivity (welche die einzige
*   Activity in der App ist) aufgerufen wird. Dort werden die Menüs erstellt und die Layoutelemente
*   initialisiert.
* */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        important = 0;

        Log.d("FCM", "Instance ID: " + FirebaseInstanceId.getInstance().getToken());

        //Seitenmenü
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //Navigationsmenü im Seitenmenü
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);


//        TextView textViewWatchlist = findViewById(R.id.textViewWatchlist);
//        textViewWatchlist.setVisibility(View.VISIBLE);

        try {
            postToken(getApplicationContext());
            Log.d("POST", "Token posted");
        } catch (JSONException e) {
            Log.e("POST", "Unable to post Token: " + e.toString());
        }


        try {
            getWatchlist(getApplicationContext());
        } catch (JSONException e) {
            Log.e("GET","Unable to get Watchlist: "+e.toString());
        }


    }



/*
*   In dieser Methode wird festgelegt, was bei einem Druck auf die Zurücktaste des Smartphones
*   passiert. Normalerweise wird das mithilfe eines sogenannten "BackStacks" gelöst. Das ist ein
*   Stapel, auf den die vorherigen Ansichten abgelegt werden und dann beim Druck auf Zurück "von
*   oben heruntergenommen" werden. Diese Implementierung ist sinnvoller, wurde jedoch aus
*   Zeitgründen nicht mehr umgesetzt.
* */

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (important == 1) {
            //Zeige die Suchergebnisse, verstecke die Detailansicht, wenn die Detailansicht gerade
            //sichtbar ist. Schließe die Seitenleiste, falls sie offen ist.

            ListView lvD = findViewById(R.id.listViewDetailedS); //Detailansicht
            ListView lvR = findViewById(R.id.listViewResultsS); //Suchergebnisse-Ansicht
            drawer = findViewById(R.id.drawer_layout); //Seitenmenü


            if (lvD.getVisibility() == View.VISIBLE) {
                lvD.setVisibility(View.INVISIBLE);
                lvR.setVisibility(View.VISIBLE);
                fabWatchlist.setVisibility(View.INVISIBLE);
            } else if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else if (lvR.getVisibility() == View.VISIBLE) {
                this.startActivity(new Intent(this, MainActivity.class));
                fabWatchlist.setVisibility(View.INVISIBLE);
            } else {
                super.onBackPressed();
            }
        } else if (important == 0) {
            ListView lvD = findViewById(R.id.listViewDetailed);
            ListView lvR = findViewById(R.id.listViewResults);

            //show the result list if the detailed view is currently active
            if (lvD.getVisibility() == View.VISIBLE) {
                lvD.setVisibility(View.INVISIBLE);
                lvR.setVisibility(View.VISIBLE);
                fabWatchlistR.setVisibility(View.INVISIBLE);
            } else if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else if (lvR.getVisibility() == View.VISIBLE) {
                this.startActivity(new Intent(this, MainActivity.class));
                fabWatchlistR.setVisibility(View.INVISIBLE);
            } else {
                super.onBackPressed();
            }
        }
    }


    /*
    *   Diese Methode wird aufgerufen, sobald das Menü in der oberen Leiste erstellt wird. Hier wird
    *   durch Abfragen von searchActive sichergestellt, dass die Suche und das Suchmenü nur im Menüpunkt
    *   Suche angezeigt wird. Auch der QueryTextListener wird an dieser Stelle gesetzt. Dieser Listener
    *   ist für die Weitergabe der Texteingaben im Suchfeld zu den API-Endpoints (siehe
    *   API-Dokumentation) zuständig.
    *
    *
    * */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        menuItemSearch = menu.findItem(R.id.menuSearch);
        menuItemOptions = menu.findItem(R.id.action_settings);

        if (!searchActive) {
            menuItemSearch.setVisible(false);
            menuItemSearch.getActionView().setVisibility(View.INVISIBLE);
            menuItemOptions.setVisible(false);
        } else {
            menuItemSearch.setVisible(true);
            menuItemSearch.getActionView().setVisibility(View.VISIBLE);
            menuItemOptions.setVisible(true);
        }


        final SearchView searchView = (SearchView) menuItemSearch.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show(); //Gib den übermittelten Text als Toast-Nachricht aus

                if (important == 0) {
                    ListView lvR = findViewById(R.id.listViewResults);
                    lvR.setAdapter(null);
                    ListView lvD = findViewById(R.id.listViewDetailed);
                    lvD.setAdapter(null);
                    fabWatchlist.setVisibility(View.INVISIBLE);
                } else {
                    Log.d("SEARCH", "didn't select search tab ");
                    ListView lvR = findViewById(R.id.listViewResultsS);
                    lvR.setAdapter(null);
                    ListView lvD = findViewById(R.id.listViewDetailedS);
                    lvD.setAdapter(null);
                    fabWatchlist.setVisibility(View.INVISIBLE);
                }

                try {
                    postNewSeriesByName(getApplicationContext(), s); //Rufe die Suche nach Seriennamen auf mit dem übermittelten Text
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Fehler mit der API.", Toast.LENGTH_LONG).show();
                }


                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                //Versteckt den Hinweistext, wenn Text eingegeben wird.
                try {
                    TextView textViewWatchlist = findViewById(R.id.textViewWatchlist);
                    textViewWatchlist.setVisibility(View.GONE);
                } catch (NullPointerException e) {

                }
                try {
                    TextView textview2 = findViewById(R.id.textView2);
                    textview2.setVisibility(View.GONE);
                } catch (NullPointerException e) {
                }
                return false;
            }
        });


        return super.onCreateOptionsMenu(menu);
    }


    /*
    *   In dieser Methode wird das Auswählen von Menüpunkten in der linken Navigationsleiste verarbeitet
    *   . Dabei wird die ID, welche von der Navigationsleiste übergeben wird mit der ID des Menüpunktes
    *   verglichen und bei Überstimmung wird eine Abfolge an Methodenaufrufen ausgeführt, die das neue
    *   Layout (bzw. die neue Ansicht) laden und sichtbar machen.
    *
    * */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here
        int id = item.getItemId();

        if (id == R.id.nav_search) {
            searchActive = true;

            important = 1;
//                Toast.makeText(getApplicationContext(), "Debug: Search Navigation Item Selected", Toast.LENGTH_LONG).show();
            setContentView(R.layout.activity_content);
            Log.d("FCM", "Instance ID: " + FirebaseInstanceId.getInstance().getToken());

            Toolbar toolbar = findViewById(R.id.toolbarS);
            setSupportActionBar(toolbar);
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            navigationView.getMenu().getItem(1).setChecked(true);
            menuItemSearch.setVisible(true);
            menuItemSearch.getActionView().setVisibility(View.VISIBLE);
            menuItemOptions.setVisible(true);

            MenuInflater inflater = getMenuInflater();
            fabWatchlist = findViewById(R.id.fabAddToWatchlist);
            fabWatchlist.setVisibility(View.INVISIBLE);
            fabWatchlist.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        postWatchlist(getApplicationContext(), currentSeriesId);
                        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (currentFirebaseUser != null) {
                            Toast.makeText(getApplicationContext(), "Successfully added to Watchlist", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Please log in to be able to save series to your personal list.", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Log.e("POST", "Unable to post Watchlist: " + e.toString());
                    }
                }
            });


        } else if (id == R.id.nav_watchlist) {
            searchActive = false;
            important = 0;
            Context context = getApplicationContext();
            setContentView(R.layout.activity_main);
//            Toast.makeText(context, "Debug: Watchlist Navigation Item Selected", Toast.LENGTH_LONG).show();
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            navigationView.getMenu().getItem(0).setChecked(true);

            menuItemSearch.setVisible(false);
            menuItemSearch.getActionView().setVisibility(View.INVISIBLE);
            menuItemOptions.setVisible(false);


            try {
                getWatchlist(getApplicationContext());
            } catch (JSONException e) {
                Log.e("GET","Unable to get Watchlist: "+e.toString());
            }


        } else if (id == R.id.nav_share) {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = "Use TheTVSeriesApp to keep track of your watching habits and get notifications for new episodes! More at: http://tvseriesapp.tk";
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "TheTVSeriesApp");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "Share via"));


        } else if (id == R.id.nav_login) {
            this.startActivity(new Intent(this, AuthUiActivity.class));

            try {
                postToken(getApplicationContext());
            } catch (JSONException e) {
                Log.e("POST", "Unable to post Token: " + e.toString());
            }
            FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            try {
                if (currentFirebaseUser != null) {
                    Log.d("AUTH", currentFirebaseUser.getUid());
                    Log.d("AUTH", currentFirebaseUser.getDisplayName());
                    Log.d("AUTH", currentFirebaseUser.getEmail());
                    Log.d("AUTH", currentFirebaseUser.getPhoneNumber());
                    Log.d("AUTH", currentFirebaseUser.getProviderId());
                }
            } catch (NullPointerException e) {
                Log.d("AUTH", "At least one Firebase parameter is undefined: " + e.toString());
            }
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    /*
    *   Diese Methode ist zuständig für das Ändern der Sprache der Ansteuerung der API (und damit auch
    *   der Suche). Diese wird zwischen Deutsch und Englisch geändert, wenn der_die Nutzer_in auf den
    *   Menüeintrag "Change Language" im oberen Menü tippt.
    *
    * */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                switch (lang) {
                    case "en":
                        lang = "de";
                        Toast.makeText(getApplicationContext(), "Sprache wurde auf Deutsch geändert", Toast.LENGTH_LONG).show();
                        break;
                    case "de":
                        lang = "es";
                        Toast.makeText(getApplicationContext(), "Changed Language to English", Toast.LENGTH_LONG).show();
                        break;
                    case "es":
                        lang = "en";
                        Toast.makeText(getApplicationContext(), "Cambió el idioma al español.", Toast.LENGTH_LONG).show();
                        break;
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
    * Inzwischen befindet sich die Methode postNewCustomFCM nicht mehr in aktueller Benutzung, sondern ist nur
    * noch für Debug-Zwecke relevant. Theoretisch ermöglicht sie das Senden angepasster Push-Nachrichten über
    * den Umweg über den Server, jedoch wurde sie bisher nur mit einer Testnachricht genutzt.
    * */

    public void postNewCustomFCM(Context context, final String content) {
        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest sr = new StringRequest(Request.Method.POST, "https://tvdb-rest.herokuapp.com/fcm", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Response", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("token", FirebaseInstanceId.getInstance().getToken());
                params.put("title", "T E S T");
                params.put("body", content);
                params.put("priority", "high");
                params.put("timetolive", "345600");

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        queue.add(sr);
    }


    /*
    *   Diese Methode steuert einen API-Endpunkt (siehe API-Dokumentation) an, welcher bei Übergabe
    *   der Serien-ID (eindeutige Nummer zur Identifikation einer Serie in der Datenbank) Informationen
    *   über diese Serie liefert. In dieser Methode wird außerdem eine detaillierte Listenansicht zur
    *   Anzeige einzelner Suchergebnisse nach Antippen eines Eintrags in der Suchergebnisliste (weiter
    *   unten erklärt) erstellt und angezeigt, was aus Zeitgründen nicht in einer eigenen Methode
    *   realisiert wurde.
    *
    *   */
    public void getSeriesById(Context context, final String seriesID) throws org.json.JSONException {

        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest sr = new StringRequest(Request.Method.GET, "https://tvdb-rest.herokuapp.com/getSeriesById?series_id=" + seriesID + "&lang=" + lang, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("POST (byId)", response);

                try {

                    JSONObject responseObject = new JSONObject(response);
                    Log.d("attr", responseObject.getString("id"));
                    Log.d("attr", responseObject.getString("seriesName"));
                    Log.d("attr", responseObject.getString("aliases"));
                    Log.d("attr", responseObject.getString("banner"));
                    Log.d("attr", responseObject.getString("seriesId"));
                    Log.d("attr", responseObject.getString("status"));
                    Log.d("attr", responseObject.getString("firstAired"));
                    Log.d("attr", responseObject.getString("network"));
                    Log.d("attr", responseObject.getString("networkId"));
                    Log.d("attr", responseObject.getString("runtime"));
                    Log.d("attr", responseObject.getString("genre"));
                    Log.d("attr", responseObject.getString("overview"));
                    Log.d("attr", responseObject.getString("siteRating"));
                    Log.d("attr", responseObject.getString("lastUpdated"));
                    /*
                    Log.d("attr", responseObject.getString("airsDayofWeek"));
                    Log.d("attr", responseObject.getString("airsTime"));
                    Log.d("attr", responseObject.getString("rating"));
                    Log.d("attr", responseObject.getString("imdbId"));
                    Log.d("attr", responseObject.getString("zap2itId"));
                    Log.d("attr", responseObject.getString("added"));
                    Log.d("attr", responseObject.getString("addedBy"));
                    Log.d("attr", responseObject.getString("siteRatingCount"));*/


                    if (important == 1 && notNull(responseObject.getString("overview"))) {

                        final ListView lvD = findViewById(R.id.listViewDetailedS);

                        String aliases = responseObject.get("aliases").toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\"", "").replaceAll("\\,", ", ");
                        String genre = responseObject.get("genre").toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\"", "").replaceAll("\\,", ", ");

                        currentSeriesId = responseObject.get("id").toString();

                        ArrayList<String> arrayDetails = new ArrayList<>();
                        arrayDetails.add((notNull((String) responseObject.get("seriesName")) ? responseObject.get("seriesName").toString() : "not provided"));
                        arrayDetails.add("Alias: " + (aliases));
                        arrayDetails.add("Overview: " + (notNull((String) responseObject.get("overview")) ? responseObject.get("overview").toString() : "not provided"));
                        arrayDetails.add("Genre: " + (genre));
                        arrayDetails.add("Id: " + (notNull(responseObject.get("id").toString()) ? responseObject.get("id").toString() : "not provided"));
                        arrayDetails.add("Network: " + (notNull((String) responseObject.get("network")) ? responseObject.get("network").toString() : "not provided"));
                        arrayDetails.add("First Aired: " + (notNull((String) responseObject.get("firstAired")) ? responseObject.get("firstAired").toString() : "not provided"));
                        arrayDetails.add("Status: " + (notNull((String) responseObject.get("status")) ? responseObject.get("status").toString() : "not provided"));
                        arrayDetails.add("Score: " + (notNull(responseObject.get("siteRating").toString()) ? responseObject.get("siteRating").toString() : "not provided"));

//                        ArrayList<Button> arrayDetailsBBB = new ArrayList<>();
//                        arrayDetailsBBB.add(new Button(MainActivity.this));

                        ArrayAdapter<String> adapterDetails = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, arrayDetails);
//                        ArrayAdapter<Button> adapterDetailsBBB = new ArrayAdapter<Button>(MainActivity.this, android.R.layout.simple_list_item_1, arrayDetailsBBB);
                        adapterDetails.notifyDataSetChanged();
                        lvD.setAdapter(adapterDetails);
                    } else if (important == 0 && notNull(responseObject.getString("overview"))) {

                        final ListView lvD = findViewById(R.id.listViewDetailed);

                        String aliases = responseObject.get("aliases").toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\"", "").replaceAll("\\,", ", ");
                        String genre = responseObject.get("genre").toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\"", "").replaceAll("\\,", ", ");

                        currentSeriesId = responseObject.get("id").toString();

                        ArrayList<String> arrayDetails = new ArrayList<>();
                        arrayDetails.add((notNull((String) responseObject.get("seriesName")) ? responseObject.get("seriesName").toString() : "not provided"));
                        arrayDetails.add("Alias: " + (aliases));
                        arrayDetails.add("Overview: " + (notNull((String) responseObject.get("overview")) ? responseObject.get("overview").toString() : "not provided"));
                        arrayDetails.add("Genre: " + (genre));
                        arrayDetails.add("Id: " + (notNull(responseObject.get("id").toString()) ? responseObject.get("id").toString() : "not provided"));
                        arrayDetails.add("Network: " + (notNull((String) responseObject.get("network")) ? responseObject.get("network").toString() : "not provided"));
                        arrayDetails.add("First Aired: " + (notNull((String) responseObject.get("firstAired")) ? responseObject.get("firstAired").toString() : "not provided"));
                        arrayDetails.add("Status: " + (notNull((String) responseObject.get("status")) ? responseObject.get("status").toString() : "not provided"));
                        arrayDetails.add("Score: " + (notNull(responseObject.get("siteRating").toString()) ? responseObject.get("siteRating").toString() : "not provided"));

                        ArrayAdapter<String> adapterDetails = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, arrayDetails);
                        adapterDetails.notifyDataSetChanged();
                        lvD.setAdapter(adapterDetails);
                    } else {
                        Toast.makeText(getApplicationContext(), "No further description available", Toast.LENGTH_LONG).show();
                    }


                } catch (JSONException e) {
                    Log.e("POST / Search", e.toString());
                }

            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("POST", error.toString());

                if (tolerance < 4) {
                    try {
                        getSeriesById(getApplicationContext(), seriesID);
                    } catch (JSONException e) {
                        Log.e("POST", e.toString());
                    }
                    tolerance++;
                } else {
                    Log.e("Too many Volley Errors, stopped trying", error.toString());
                }

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("series_id", seriesID);

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        queue.add(sr);

    }

    /*TODO: explain this */
    int tolerance = 0;


    /*
    *   Diese Methode steuert einen API-Endpunkt (siehe API-Dokumentation) an, welcher bei Übergabe
    *   eines Such-Strings Serien mit zur Suchphrase passendem Titel liefert. In dieser Methode wird
    *   außerdem Listenansicht zur Anzeige aller Suchergebnisse mit Titel und Handlungsübersicht
    *   erstellt und angezeigt, was aus Zeitgründen nicht in einer eigenen Methode realisiert wurde.
    */
    public void postNewSeriesByName(Context context, final String seriesNameKonrad) throws org.json.JSONException {

        final String seriesNameKonrad2 = seriesNameKonrad.replaceAll("\\s", "%20");
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest sr = new StringRequest(Request.Method.GET, "https://tvdb-rest.herokuapp.com/getSeriesByName?series_name=" + seriesNameKonrad2 + "&lang=" + lang, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("POST", response);

                try {
                    JSONArray arr = new JSONArray(response);
                    for (int i = 0; i < arr.length(); i++) {
                        //Attribute
                        String aliases = arr.getJSONObject(i).getString("aliases");
                        String banner = arr.getJSONObject(i).getString("banner");
                        String firstAired = arr.getJSONObject(i).getString("firstAired");
                        String id = arr.getJSONObject(i).getString("id");
                        String network = arr.getJSONObject(i).getString("network");
                        String overview = arr.getJSONObject(i).getString("overview");
                        String seriesName = arr.getJSONObject(i).getString("seriesName");
                        String status = arr.getJSONObject(i).getString("status");

                        //Test
                        Log.d("aliases", aliases);
                        Log.d("banner", banner);
                        Log.d("firstAired", firstAired);
                        Log.d("id", id);
                        Log.d("network", network);
                        Log.d("overview", overview);
                        Log.d("seriesName", seriesName);
                        Log.d("status", status);


                        if (important == 1) {
                            final ListView lvR = findViewById(R.id.listViewResultsS);
                            lvR.setVisibility(View.VISIBLE);
                            final ListView lvD = findViewById(R.id.listViewDetailedS);

                            //ArrayList<String> arrayResults = new ArrayList<>();

                            List<Map<String, String>> data = new ArrayList<>();
                            for (int j = 0; j < arr.length(); ) {

                                Map<String, String> datum = new HashMap<>(7);

                                datum.put("seriesName", arr.getJSONObject(j).getString("seriesName"));
                                if (arr.getJSONObject(j).getString("overview").length() > 140) {
                                    datum.put("overview", arr.getJSONObject(j).getString("overview").substring(0,140)+"..."+"\n");
                                } else {
                                    datum.put("overview", arr.getJSONObject(j).getString("overview"));
                                }
                                datum.put("id", arr.getJSONObject(j).getString("id"));
                                datum.put("network", arr.getJSONObject(j).getString("network"));
                                datum.put("firstAired", arr.getJSONObject(j).getString("firstAired"));
                                datum.put("status", arr.getJSONObject(j).getString("status"));
                                data.add(datum);


                                j++;
                            }

                            SimpleAdapter sAdapter = new SimpleAdapter(MainActivity.this, data,
                                    android.R.layout.simple_list_item_2,
                                    new String[]{"seriesName", "overview"},
                                    new int[]{android.R.id.text1,
                                            android.R.id.text2});



                            lvR.setAdapter(sAdapter);

                            //method for handling clicks in the result list
                            lvR.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapter, View v, int position,
                                                        long arg3) {
                                    HashMap values = (HashMap) adapter.getItemAtPosition(position);
                                    String valueTitle = "";//values.get("title").toString();
                                    String valueId = "";//values.get("id").toString();

//                                        Toast.makeText(getApplicationContext(), "You clicked item \"" + valueTitle + "\" with id \"" + valueId + "\"" , Toast.LENGTH_LONG).show();

                                    lvR.setVisibility(View.INVISIBLE);
                                    lvD.setVisibility(View.VISIBLE);
                                    fabWatchlist.setVisibility(View.VISIBLE);

                                    try {
                                        getSeriesById(getApplicationContext(), values.get("id").toString());
                                        Log.d("DETAILVIEW", "success");
                                    } catch (JSONException e) {
                                        Log.e("error", e.toString());
                                    }


                                }
                            });
                        } else if (important == 0) {
                            Log.d("SEARCH", "didnt select search view ");
                            Log.d("important", String.valueOf(important));
                            final ListView lvR = findViewById(R.id.listViewResults);
                            lvR.setVisibility(View.VISIBLE);
                            final ListView lvD = findViewById(R.id.listViewDetailed);

                            //ArrayList<String> arrayResults = new ArrayList<>();

                            List<Map<String, String>> data = new ArrayList<>();
                            for (int j = 0; j < arr.length(); ) {

                                Map<String, String> datum = new HashMap<>(7);

                                datum.put("seriesName", arr.getJSONObject(j).getString("seriesName"));
                                if (arr.getJSONObject(j).getString("overview").length() > 140) {
                                    datum.put("overview", arr.getJSONObject(j).getString("overview").substring(0,140)+"..."+"\n");
                                } else {
                                    datum.put("overview", arr.getJSONObject(j).getString("overview"));
                                }
                                datum.put("id", arr.getJSONObject(j).getString("id"));
                                datum.put("network", arr.getJSONObject(j).getString("network"));
                                datum.put("firstAired", arr.getJSONObject(j).getString("firstAired"));
                                datum.put("status", arr.getJSONObject(j).getString("status"));
                                data.add(datum);


                                j++;
                            }

                            SimpleAdapter sAdapter = new SimpleAdapter(MainActivity.this, data,
                                    android.R.layout.simple_list_item_2,
                                    new String[]{"seriesName", "overview"},
                                    new int[]{android.R.id.text1,
                                            android.R.id.text2});

                        /*adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, arrayResults);
                        adapter.notifyDataSetChanged();*/

                            lvR.setAdapter(sAdapter);

                            //method for handling clicks in the result list
                            lvR.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapter, View v, int position,
                                                        long arg3) {
                                    HashMap values = (HashMap) adapter.getItemAtPosition(position);
                                    String valueTitle = "";//values.get("title").toString();
                                    String valueId = "";//values.get("id").toString();

//                                        Toast.makeText(getApplicationContext(), "You clicked item \"" + valueTitle + "\" with id \"" + valueId + "\"" , Toast.LENGTH_LONG).show();

                                    lvR.setVisibility(View.INVISIBLE);
                                    lvD.setVisibility(View.VISIBLE);

                                    try {
                                        getSeriesById(getApplicationContext(), values.get("id").toString());
                                        Log.d("DETAILVIEW", "success");
                                    } catch (JSONException e) {
                                        Log.e("error", e.toString());
                                    }


                                }
                            });


                        }


                        test = true;


                    }

                } catch (JSONException e) {
                    Log.e("POST / Search", e.toString());
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("POST", error.toString());

                if (tolerance < 4) {
                    try {
                        postNewSeriesByName(MainActivity.this, seriesNameKonrad2);
                    } catch (JSONException e) {
                        Log.e("POST", e.toString());
                    }
                    tolerance++;
                } else {
                    Log.e("Too many Volley Errors, stopped trying", error.toString());
                }

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("series_name", seriesNameKonrad2);

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        queue.add(sr);

    }


    /*
    *   Diese Methode sendet ein Firebase-Device-Token, d.h. die Identifizierung einer Instanz der App auf einem Gerät.
    *   Dabei hat jedes Gerät sein eigenes Device-Token, und dient somit in Kombination mit der ID des Nutzers in der
    *   Datenbank (der UID) als eindeutige Identifizierung und folglich Verknüpfung von Benutzern und ihren Geräten.
    *   Benutzt wird die Methode um die erfolgreiche Herstellung einer weiteren Kommunikation zwischen Server und App
    *   zu gewährleisten.Wie auch bei anderen HTTP-Anfrage-Methoden wird auch hier die Methode bei Fehlschlag bis zu
    *   vier Male erneut aufgerufen, um sicherzustellen dass der Server sich nicht mehr im Sleep-Modus befindet, d.h. inaktiv ist.
    */
    public void postToken(Context context) throws org.json.JSONException {

        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest sr = new StringRequest(Request.Method.POST, "https://tvdb-rest.herokuapp.com/postToken", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("POST (token)", response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("POST", error.toString());

                if (tolerance < 4) {
                    try {
                        postToken(getApplicationContext());
                    } catch (JSONException e) {
                        Log.e("POST", e.toString());
                    }
                    tolerance++;
                } else {
                    Log.e("Too many Volley Errors, stopped trying", error.toString());
                }

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("token", FirebaseInstanceId.getInstance().getToken());
                try {
                    params.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                } catch (NullPointerException e) {
                    Log.e("postToken failed", e.toString());
                }

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        queue.add(sr);

    }


    /*
    * Die Methode postWatchlist ist für das Hinzufügen einer momentan in der Detailansicht der Suche dargestellten
    * Serie zuständig. Sie wird durch einen FloatingActionButton in der Detailansicht aufgerufen und stellt eine Anfrage
    * an den Endpoint /addWatchlistItem mithilfe von Nutzeridentifikations-ID, der momentan ausgewählten Sprache
    * und der gewünschten Serien-ID, im Anwendungsfall stets der ID der zurzeit betrachteten Serie.
    * */

    public void postWatchlist(Context context, final String seriesID) throws org.json.JSONException {

        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest sr = new StringRequest(Request.Method.POST, "https://tvdb-rest.herokuapp.com/addWatchListItem", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("POST (watchlist)", response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("POST", error.toString());

                if (tolerance < 4) {
                    try {
                        postWatchlist(MainActivity.this, seriesID);
                    } catch (JSONException e) {
                        Log.e("POST", e.toString());
                    }
                    tolerance++;
                } else {
                    Log.e("Too many Volley Errors, stopped trying", error.toString());
                }

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                //Festlegung der Parameter
                params.put("wl_item", seriesID);
                try {
                    params.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                } catch (NullPointerException e) {
                    Log.e("postWatchlist failed", e.toString());
                }
                params.put("lang", lang);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        queue.add(sr);
    }

    /*
    * removeWatchlistItem dient dem Entfernen einzelner Serien aus der persönlichen Watchlist.
    * An den Server übergeben werden hier nur die Nutzer-ID des Benutzers und die ID der momentan in
    * Detailansicht der eigenen Watchlist angezeigten Serie.
    * */

    public void removeWatchlistItem(Context context, final String seriesID) throws org.json.JSONException {

        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest sr = new StringRequest(Request.Method.DELETE, "https://tvdb-rest.herokuapp.com/removeWatchlistItem?uid="+FirebaseAuth.getInstance().getCurrentUser().getUid()+"&wl_item="+seriesID, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("REMOVE (watchlist)", response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("REMOVE", error.toString());

                if (tolerance < 4) {
                    try {
                        removeWatchlistItem(MainActivity.this, seriesID);
                    } catch (JSONException e) {
                        Log.e("REMOVE", e.toString());
                    }
                    tolerance++;
                } else {
                    Log.e("Too many Volley Errors, stopped trying", error.toString());
                }

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        queue.add(sr);
    }

    /*
    * Die Methode getWatchlist wird bei jedem Besuch des Watchlist-Tabs, also auch bei Appstart ausgeführt. Sie fragt
    * die nutzereigene, auf dem Server gespeicherte Watchlist ab, und lässt diese zugleich in einer ListView anzeigen.
    * Außerdem wird auch ein FloatingActionButton zum Entfernen von Serien von der Watchlist erstellt bzw. als nicht
    * sichtbar eingesetzt, je nachdem ob die Detailansicht ausgewählt ist oder nicht. Dabei fungiert sie im Rahmen des
    * Watchlist-Tabs ähnlich wie postNewSeriesByName
    * */

    public void getWatchlist(Context context) throws org.json.JSONException {

        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest sr = new StringRequest(Request.Method.POST, "https://tvdb-rest.herokuapp.com/getWatchlist", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("GET (watchlist)", response);

                TextView textViewWatchlist = findViewById(R.id.textViewWatchlist);
                textViewWatchlist.setVisibility(View.GONE);
                findViewById(R.id.textViewWatchlistLoadingInfo).setVisibility(View.VISIBLE);

                if (response.equals("[]")) {
                    findViewById(R.id.textViewWatchlist).setVisibility(View.VISIBLE);
                } else {

                    try {
                        JSONArray arr = new JSONArray(response);
                        for (int i = 0; i < arr.length(); i++) {

                            if (important == 0) {
                                Log.d("SEARCH", "didnt select search view ");
                                Log.d("important", String.valueOf(important));
                                final ListView lvR = findViewById(R.id.listViewResults);
                                lvR.setVisibility(View.VISIBLE);
                                final ListView lvD = findViewById(R.id.listViewDetailed);

                                //ArrayList<String> arrayResults = new ArrayList<>();

                                final String currentId = arr.getJSONObject(i).getString("id");

                                List<Map<String, String>> data = new ArrayList<>();
                                for (int j = 0; j < arr.length(); ) {

                                    Map<String, String> datum = new HashMap<>(2);

                                    datum.put("seriesName", arr.getJSONObject(j).getString("seriesName"));
                                    datum.put("overview", arr.getJSONObject(j).getString("overview").substring(0, 140) + "..." + "\n");
                                    data.add(datum);


                                    j++;
                                }

                                SimpleAdapter sAdapter = new SimpleAdapter(MainActivity.this, data,
                                        android.R.layout.simple_list_item_2,
                                        new String[]{"seriesName", "overview"},
                                        new int[]{android.R.id.text1,
                                                android.R.id.text2});

                        /*adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, arrayResults);
                        adapter.notifyDataSetChanged();*/

                                lvR.setAdapter(sAdapter);

                                //method for handling clicks in the result list
                                lvR.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> adapter, View v, int position,
                                                            long arg3) {
                                        HashMap values = (HashMap) adapter.getItemAtPosition(position);
                                        String valueTitle = "";//values.get("title").toString();
                                        String valueId = "";//values.get("id").toString();

//                                        Toast.makeText(getApplicationContext(), "You clicked item \"" + valueTitle + "\" with id \"" + valueId + "\"" , Toast.LENGTH_LONG).show();

                                        lvR.setVisibility(View.INVISIBLE);
                                        lvD.setVisibility(View.VISIBLE);
                                        fabWatchlistR = findViewById(R.id.fabremoveFromWatchlist);
                                        fabWatchlistR.setVisibility(View.VISIBLE);

                                        try {
                                            getSeriesById(getApplicationContext(), currentId);
                                            Log.d("DETAILVIEW", "success");
                                        } catch (JSONException e) {
                                            Log.e("error", e.toString());
                                        }


                                    }
                                });


                            }


                            test = true;


                        }

                    } catch (JSONException e) {
                        Log.e("POST / Search", e.toString());
                    }
                }

                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                findViewById(R.id.textViewWatchlistLoadingInfo).setVisibility(View.GONE);
                fabWatchlistR = findViewById(R.id.fabremoveFromWatchlist);
                fabWatchlistR.setVisibility(View.INVISIBLE);
                fabWatchlistR.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        try {
                            removeWatchlistItem(getApplicationContext(), currentSeriesId);
                            FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                            if (currentFirebaseUser != null) {
                                Toast.makeText(getApplicationContext(), "Successfully removed from Watchlist", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "An error occurred. Please try to log in again.", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Log.e("REMOVE", "Unable to remove WatchlistItem: " + e.toString());
                        }
                    }
                });

            }


        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("GET", error.toString());


                if (tolerance < 4) {
                    try {
                        getWatchlist(MainActivity.this);
                    } catch (JSONException e) {
                        Log.e("GET", e.toString());
                    }
                    tolerance++;
                } else {
                    FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    findViewById(R.id.textViewWatchlistLoadingInfo).setVisibility(View.INVISIBLE);
                    findViewById(R.id.loadingPanel).setVisibility(View.GONE);

                    if (currentFirebaseUser != null) {
                        findViewById(R.id.textViewWatchlist).setVisibility(View.VISIBLE);
                    } else {
                        findViewById(R.id.textViewWatchlistNotLoggedIn).setVisibility(View.VISIBLE);
                    }

                    Log.e("Too many Volley Errors, stopped trying", error.toString());
                }

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                try {
                    params.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                } catch (NullPointerException e) {
                    Log.e("removeWatchlist failed", e.toString());
                }
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        queue.add(sr);
    }

    /*
    * Die Methode notNull prüft, ob ein übergebener String den Wert "null" hat oder leer ist. In der Datenbank sind
    * nicht eingetragene Elemente teilweise leer, teilweise haben sie den Wert "null", und dies wird
    * beides zusammen in dieser Methode überprüft, um diese Strings dann in der Listenansicht durch
    * "not provided" (= "nicht zur Verfügung gestellt") ersetzen zu können.
    *
    * */
    public boolean notNull(String string) {
        return !string.equals("null") && !string.isEmpty();
    }

}
