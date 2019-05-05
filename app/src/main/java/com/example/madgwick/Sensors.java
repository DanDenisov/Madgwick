package com.example.madgwick;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

public class Sensors extends MainActivity implements SensorEventListener
{
    static protected SensorManager sensorManager;
    static protected Sensor acc, gyr, mag;

    static public String accSpec, gyrSpec;

    static public Boolean InvokeFiltration;
    static public int sensors_count;
    static public int response_count;
    static private long t_prev;

    protected TextView X, Y, Z;
    private static final double RadToDeg = 180 / Math.PI;
    private static String content;

    public Sensors()
    {
        acc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (acc != null)
            sensors_count++;
        gyr = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (gyr != null)
            sensors_count++;
        mag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (mag != null)
            sensors_count++;

        accSpec = "Name: " + acc.getName() + "\nVendor: " + acc.getVendor() +
                "\nVersion: " + acc.getVersion() + "\nResolution: " + acc.getResolution();
        gyrSpec = "Name: " + gyr.getName() + "\nVendor: " + gyr.getVendor() +
                "\nVersion: " + gyr.getVersion() + "\nResolution: " + gyr.getResolution();

        X = findViewById(R.id.X);
        Y = findViewById(R.id.Y);
        Z = findViewById(R.id.Z);
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event)
    {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE)
        {
            MadgwickFilter.w = new double[] {0, event.values[0], event.values[1], event.values[2]};
            if (InvokeFiltration)
            {
                MadgwickFilter.dt = (event.timestamp - t_prev) * Math.pow(10, -9);
                t_prev = event.timestamp;
            }
            else
                t_prev = event.timestamp;
            response_count++;
        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            MadgwickFilter.a = new double[] {0, event.values[0], event.values[1], event.values[2]};
            response_count++;
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
        {
            MadgwickFilter.m = new double[] {0, event.values[0], event.values[1], event.values[2]};
            response_count++;
        }

        if (InvokeFiltration && response_count == sensors_count)
        {
            double[] filtrated = MadgwickFilter.Filtrate();

            //outputting results on the screen
            X.setText(getString(R.string.x, filtrated[0] * RadToDeg));
            Y.setText(getString(R.string.y, filtrated[1] * RadToDeg));
            Z.setText(getString(R.string.z, filtrated[2] * RadToDeg));

            //writing results to a file
            content = event.timestamp / 1000000 + "," + filtrated[0] * RadToDeg + "," + filtrated[1] * RadToDeg + "," + filtrated[2] * RadToDeg + "\n";
            WriteToFile(content);

            response_count = 0;
        }

        InvokeFiltration = true;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        sensorManager.registerListener(this, acc, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyr, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, mag, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    protected void WriteToFile(String data)
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        }
        else
        {
            try
            {
                String filepath = Environment.getExternalStorageDirectory().getPath();
                File folder = new File(filepath, getString(R.string.directory));
                if (!folder.exists())
                    folder.mkdirs();

                Calendar time = Calendar.getInstance();
                File file = new File(folder, "/" + time.get(Calendar.HOUR) + ":" + time.get(Calendar.MINUTE) + ":" + time.get(Calendar.SECOND) +
                        "_" + time.get(Calendar.DAY_OF_MONTH) + "-" + time.get(Calendar.MONTH) + "-" + time.get(Calendar.YEAR) + ".txt");

                FileWriter writer = new FileWriter(file);
                writer.append(data);
                writer.flush();
                writer.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        switch (requestCode)
        {
            case R.integer.REQUEST_WRITE_EXTERNAL:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    WriteToFile(content);
                }
                break;
        }
    }
}