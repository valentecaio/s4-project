package valentecaio.mapquestapp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static valentecaio.mapquestapp.R.id.camera_view;

public class CameraActivity extends AppCompatActivity implements LocationListener, SurfaceHolder.Callback, SensorEventListener {

    boolean DEBUG = true;

    TextView descriptionTextView;
    ImageView pointerIcon;

    private Camera mCamera;
    private SurfaceHolder mSurfaceHolder;
    private boolean isCameraviewOn = false;

    private float[] mGravity;
    private float[] mGeomagnetic;
    private Sensor magnetometer;
    private Sensor accelerometer;

    private SensorManager sensorManager;
    private Point target;
    private Location myLocation;
    private LocationManager locationManager;

    private static final double DISTANCE_SAFETY_MARGIN = 300;
    private static final double AZIMUTH_SAFETY_MARGIN = 30;

    private double currentAzimuth = 0;
    private double targetAzimuth = 0;
    private double distance = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        verify_permissions();

        target = new Point("yan", 48.3588, -4.5700);
        myLocation = new Location("hi");
        myLocation.setLatitude(48.356647);
        myLocation.setLongitude(-4.570205);

        descriptionTextView = (TextView) findViewById(R.id.cameraTextView);
        pointerIcon = (ImageView) findViewById(R.id.icon);

        // config camera
        SurfaceView surfaceView = (SurfaceView) findViewById(camera_view);
        mSurfaceHolder = surfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // config GPS
        // Getting LocationManager object
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 2000, 1, this);
        } catch (SecurityException ex){
            ex.printStackTrace();
        }

        // config compass
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    private void updateDescription() {
        String text = target.getName() + " location:"
                + "\n latitude: " + target.getLatitude() + "  longitude: " + target.getLongitude()
                + "\n Current location:"
                + "\n Latitude: " + myLocation.getLatitude() + "  Longitude: " + myLocation.getLongitude()
                + "\n "
                + "\n Target currentAzimuth: " + (int)targetAzimuth
                + "\n Current currentAzimuth: " + (int)currentAzimuth
                + "\n Distance: " + (int)distance;

        descriptionTextView.setText(text);
    }

    public void calculateTargetAzimuth() {
        double dX = target.getLatitude() - myLocation.getLatitude();
        double dY = target.getLongitude() - myLocation.getLongitude();

        double phiAngle;
        double tanPhi;

        tanPhi = Math.abs(dY / dX);
        phiAngle = Math.atan(tanPhi);
        phiAngle = Math.toDegrees(phiAngle);

        if (dX > 0 && dY > 0) { // I quater
            targetAzimuth = phiAngle;
        } else if (dX < 0 && dY > 0) { // II
            targetAzimuth = 180 - phiAngle;
        } else if (dX < 0 && dY < 0) { // III
            targetAzimuth = 180 + phiAngle;
        } else if (dX > 0 && dY < 0) { // IV
            targetAzimuth = 360 - phiAngle;
        } else {
            targetAzimuth = phiAngle;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mCamera = Camera.open();
        mCamera.setDisplayOrientation( 90);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        if (isCameraviewOn) {
            mCamera.stopPreview();
            isCameraviewOn = false ;
        }

        if ( mCamera != null ) {
            try {
                mCamera.setPreviewDisplay(mSurfaceHolder);
                mCamera.startPreview();
                isCameraviewOn = true ;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null ;
        isCameraviewOn = false ;
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mGravity = event.values;
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mGeomagnetic = event.values;
        }

        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];

            if (SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic)) {
                // orientation contains currentAzimuth, pitch and roll
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);

                double receivedAzimuth = (double)orientation[0];
                receivedAzimuth = (Math.toDegrees(receivedAzimuth)+360)%360;
                this.onAzimuthChanged(receivedAzimuth);
            }
        }
    }

    public void onAzimuthChanged(double newAzimuth) {
        this.currentAzimuth = newAzimuth;
        calculateTargetAzimuth();
        calculateDistance();

        List<Double> angles = calculateAzimuthRange(targetAzimuth);
        Log.e("debug", "angles: " + angles.toString() + " | distance: " + distance);

        if ((isBetween(angles.get(0), angles.get(1), currentAzimuth)) && distance <= DISTANCE_SAFETY_MARGIN) {
            pointerIcon.setVisibility(View.VISIBLE);
        } else {
            pointerIcon.setVisibility(View.INVISIBLE);
        }

        updateDescription();
    }

    private List<Double> calculateAzimuthRange(double azimuth) {
        double minAngle = azimuth - AZIMUTH_SAFETY_MARGIN;
        double maxAngle = azimuth + AZIMUTH_SAFETY_MARGIN;
        List<Double> minMax = new ArrayList<Double>();

        if (minAngle < 0)
            minAngle += 360;

        if (maxAngle >= 360)
            maxAngle -= 360;

        minMax.clear();
        minMax.add(minAngle);
        minMax.add(maxAngle);

        return minMax;
    }

    private boolean isBetween(double minAngle, double maxAngle, double azimuth) {
        if (minAngle > maxAngle) {
            // this MUST be and OR (and must NOT be an AND)
            return (isBetween(0, maxAngle, azimuth) || isBetween(minAngle, 360, azimuth));
        } else {
            return ((azimuth > minAngle) && (azimuth < maxAngle));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    public void calculateDistance() {
        double dX = target.getLatitude() - myLocation.getLatitude();
        double dY = target.getLongitude() - myLocation.getLongitude();

        this.distance = (Math. sqrt(Math.pow (dX, 2 ) + Math.pow(dY, 2 )) * 100000 );
    }

    @Override
    public void onLocationChanged(Location location) {
        if(!DEBUG) {
            myLocation = location;
            calculateDistance();
            calculateTargetAzimuth();
            updateDescription();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {    }

    @Override
    public void onProviderEnabled(String provider) {    }

    @Override
    public void onProviderDisabled(String provider) {    }
}

