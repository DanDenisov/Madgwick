package com.example.madgwick;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import java.io.FileWriter;
import java.io.IOException;

public class Sensors implements SensorEventListener
{
    private Activity activity;
    private Context context;

    static SensorManager sensorManager;
    static Sensor gyr, acc, mag;
    static String gyrSpec, accSpec, magSpec;

    private Boolean InvokeFiltration = false;
    private Boolean gyrQueried = false, accQueried = false, magQueried = false;
    private long t_prev;

    TextView X, Y, Z;
    static private final double RadToDeg = 180 / Math.PI;
    static FileWriter writer;
    String content;

    Sensors(Context c, Activity a)
    {
        activity = a;
        context = c;

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        gyr = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (gyr != null)
        {
            gyrSpec = "Name: " + gyr.getName() + "\nVendor: " + gyr.getVendor() +
                    "\nVersion: " + gyr.getVersion() + "\nResolution: " + gyr.getResolution();
        }
        else
            gyrSpec = "Not presented.";

        acc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (acc != null)
        {
            accSpec = "Name: " + acc.getName() + "\nVendor: " + acc.getVendor() +
                    "\nVersion: " + acc.getVersion() + "\nResolution: " + acc.getResolution();
        }
        else
            accSpec = "Not presented.";

        mag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (mag != null)
        {
            magSpec = "Name: " + mag.getName() + "\nVendor: " + mag.getVendor() +
                    "\nVersion: " + mag.getVersion() + "\nResolution: " + mag.getResolution();
        }
        else
            magSpec = "Not presented.";
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event)
    {
        if (!gyrQueried && event.sensor.getType() == Sensor.TYPE_GYROSCOPE)
        {
            MadgwickFilter.w = new double[] {0, event.values[0], event.values[1], event.values[2]};
            if (InvokeFiltration)
            {
                MadgwickFilter.dt = (event.timestamp - t_prev) * Math.pow(10, -9);
                t_prev = event.timestamp;
            }
            else
                t_prev = event.timestamp;
            gyrQueried = true;
        }
        if (!accQueried && event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            MadgwickFilter.a = new double[] {0, event.values[0], event.values[1], event.values[2]};
            accQueried = true;
        }
        if (!magQueried && event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
        {
            MadgwickFilter.m = new double[] {0, event.values[0], event.values[1], event.values[2]};
            magQueried = true;
        }

        if (InvokeFiltration && gyrQueried && accQueried && magQueried)
        {
            double[] filtrated = MadgwickFilter.Filtrate();

            //outputting results on the screen
            X.setText(context.getString(R.string.x, filtrated[0] * RadToDeg));
            Y.setText(context.getString(R.string.y, filtrated[1] * RadToDeg));
            Z.setText(context.getString(R.string.z, filtrated[2] * RadToDeg));

            //writing results to a file
            content = event.timestamp / 1000000 + "," + filtrated[0] * RadToDeg + "," + filtrated[1] * RadToDeg + "," + filtrated[2] * RadToDeg + "\n";
            WriteToFile(content);

            //drop state
            gyrQueried = accQueried = magQueried = false;
        }

        InvokeFiltration = true;
    }

    void WriteToFile(String data)
    {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                    context.getResources().getInteger(R.integer.REQUEST_WRITE_EXTERNAL));
        }
        else
        {
            try
            {
                writer.append(data);
                writer.flush();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}