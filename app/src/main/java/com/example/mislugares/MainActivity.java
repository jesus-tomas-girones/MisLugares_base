package com.example.mislugares;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity implements LocationListener {

    public static LugaresBD lugares;
    private LocationManager manejador;
    private Location mejorLocaliz;
    private static final int SOLICITUD_PERMISO_LOCALIZACION = 0;
    static final int RESULTADO_PREFERENCIAS = 0;
    private VistaLugarFragment fragmentVista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lugares = new LugaresBD(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fragmentVista = (VistaLugarFragment) getSupportFragmentManager()
                .findFragmentById(R.id.vista_lugar_fragment);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long _id = lugares.nuevo();
                Intent i = new Intent(MainActivity.this, EdicionLugarActivity.class);
                i.putExtra("_id", _id);
                startActivity(i);
            }
        });
        manejador = (LocationManager) getSystemService(LOCATION_SERVICE);
        ultimaLocalizazion();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void muestraLugar(long id) {
        if (fragmentVista != null) {
            fragmentVista.actualizarVistas(id);
        } else {
            Intent intent = new Intent(this, VistaLugarActivity.class);
            intent.putExtra("id", id);
            startActivityForResult(intent, 0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if  (id == R.id.action_settings) {
            lanzarPreferencias(null);
            return true;
        }
        if (id == R.id. acercaDe) {
            lanzarAcercaDe(null);
            return true;
        }
        if(id == R.id.menu_buscar) {
            lanzarVistaLugar(null);
            return true;
        }
        if (id==R.id.menu_mapa) {
            Intent intent = new Intent(this, MapaActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void lanzarAcercaDe(View view){
        Intent i = new Intent(this, AcercaDeActivity.class);
        startActivity(i);
    }

    public void lanzarVistaLugar(View view){
        final EditText entrada = new EditText(this);
        entrada.setText("0");
        new AlertDialog.Builder(this)
                .setTitle("Selección de lugar")
                .setMessage("indica su id:")
                .setView(entrada)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        long id = Long.parseLong(entrada.getText().toString());
                        Intent i = new Intent(MainActivity.this,
                                VistaLugarActivity.class);
                        i.putExtra("id", id);
                        startActivity(i);
                    }})
                .setNegativeButton("Cancelar", null)
                .show();
    }

    void ultimaLocalizazion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.
            ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (manejador.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                actualizaMejorLocaliz(manejador.getLastKnownLocation(
                        LocationManager.GPS_PROVIDER));
            }
            if (manejador.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                actualizaMejorLocaliz(manejador.getLastKnownLocation(
                        LocationManager.NETWORK_PROVIDER));
            }
        } else {
            PermisosUtilidades.solicitarPermiso(Manifest.permission.ACCESS_FINE_LOCATION,
                "Sin permiso de localización no es posible mostrar la distancia" +
                " a los lugares.", SOLICITUD_PERMISO_LOCALIZACION, this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        if (requestCode == SOLICITUD_PERMISO_LOCALIZACION) {
            if (grantResults.length== 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ultimaLocalizazion();
                activarProveedores();
                //adaptador.notifyDataSetChanged();
                SelectorFragment.adaptador.notifyDataSetChanged();
            }
        }
    }

    @Override protected void onResume() {
        super.onResume();
        activarProveedores();
        if (fragmentVista!=null && SelectorFragment.adaptador.getItemCount()>0) {
            fragmentVista.actualizarVistas(0);
        }
    }
    private void activarProveedores() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.
                ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (manejador.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                manejador.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        20 * 1000, 5, this);
            }
            if (manejador.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                manejador.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        10 * 1000, 10, this);
            }
        } else {
            PermisosUtilidades.solicitarPermiso(Manifest.permission.ACCESS_FINE_LOCATION,
                    "Sin el permiso localización no puedo mostrar la distancia"+
                    " a los lugares.", SOLICITUD_PERMISO_LOCALIZACION, this);
        }
    }

    @Override protected void onPause() {
        super.onPause();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.
                ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            manejador.removeUpdates(this);
        }
    }

    @Override public void onLocationChanged(Location location) {
        Log.d(Lugares.TAG, "Nueva localización: "+location);
        actualizaMejorLocaliz(location);
        //adaptador.notifyDataSetChanged();
        SelectorFragment.adaptador.notifyDataSetChanged();
    }

    @Override public void onProviderDisabled(String proveedor) {
        Log.d(Lugares.TAG, "Se deshabilita: "+proveedor);
        activarProveedores();
    }
    @Override   public void onProviderEnabled(String proveedor) {
        Log.d(Lugares.TAG, "Se habilita: "+proveedor);
        activarProveedores();
    }
    @Override
    public void onStatusChanged(String proveedor, int estado, Bundle extras) {
        Log.d(Lugares.TAG, "Cambia estado: "+proveedor);
        activarProveedores();
    }

    private static final long DOS_MINUTOS = 2 * 60 * 1000;

    private void actualizaMejorLocaliz(Location localiz) {
        if (localiz != null && (mejorLocaliz == null
                || localiz.getAccuracy() < 2*mejorLocaliz.getAccuracy()
                || localiz.getTime() - mejorLocaliz.getTime() > DOS_MINUTOS)) {
            Log.d(Lugares.TAG, "Nueva mejor localización");
            mejorLocaliz = localiz;
            Lugares.posicionActual.setLatitud(localiz.getLatitude());
            Lugares.posicionActual.setLongitud(localiz.getLongitude());
        }
    }


    public void lanzarPreferencias(View view) {
        Intent i = new Intent(this, PreferenciasActivity.class);
        startActivityForResult(i, RESULTADO_PREFERENCIAS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == RESULTADO_PREFERENCIAS) {
            SelectorFragment.adaptador.setCursor(MainActivity.lugares.extraeCursor());
            SelectorFragment.adaptador.notifyDataSetChanged();
        }
    }
}
