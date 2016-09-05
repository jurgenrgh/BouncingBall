package com.seabird.jvr.bouncingball;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity implements SensorEventListener
{
    private static final int ACCELCOUNT = 10;
    BallSurfaceView ballSurfaceView;
    public SensorManager sensorManager;
    public Sensor accelSensor;
    D3 accelValues = new D3();
    int nCount = 0;
    int nStep = 0;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        setContentView( R.layout.activity_main );
        Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );

        ballSurfaceView = (BallSurfaceView) findViewById( R.id.bouncingview );
        // ballSurfaceView.setBackgroundColor(Color.rgb( 0, 80, 00 ));

        sensorManager = (SensorManager) (this.getSystemService( SENSOR_SERVICE ));
        accelSensor = sensorManager.getDefaultSensor( Sensor.TYPE_ACCELEROMETER );

        ArrayAdapter adapter = ArrayAdapter.createFromResource( this, R.array.parameters, R.layout.spinner_item );
        ((Spinner) findViewById( R.id.spinner )).setAdapter( adapter );
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.menu_main, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if( id == R.id.action_settings )
        {
            return true;
        }
        return super.onOptionsItemSelected( item );
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        // register this class as a listener for the orientation and
        // accelerometer sensors
        sensorManager.registerListener( this, accelSensor, SensorManager.SENSOR_DELAY_GAME );
    }

    @Override
    protected void onPause()
    {
        // unregister listener
        super.onPause();
        sensorManager.unregisterListener( this );
    }

    @Override
    public void onSensorChanged( SensorEvent event )
    {
        // Observed rate on S5 for SENSOR_DELAY_GAME is ~50Hz
        if( event.sensor.getType() == Sensor.TYPE_ACCELEROMETER )
        {
            getAccelerometer( event );
        }
    }

    private void getAccelerometer( SensorEvent event )
    {
        synchronized(accelValues)
        {
            float[] values = event.values;
            // Movement
            accelValues.x = - values[0];
            accelValues.y =   values[1];
            accelValues.z = - values[2];
        }
    }

    @Override
    public void onAccuracyChanged( Sensor sensor, int i )
    {
        //nothing to do
    }
}
