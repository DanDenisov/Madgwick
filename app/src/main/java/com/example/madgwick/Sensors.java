package com.example.madgwick;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Sensors extends MainActivity implements SensorEventListener
{
    static protected SensorManager sensorManager;
    static protected Sensor acc, gyr, mag;

    static public String accSpec, gyrSpec;

    static public int sensors_count;
    static public int response_count;

    static private long t_prev;

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

        sensorManager.registerListener(this, acc, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyr, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, mag, SensorManager.SENSOR_DELAY_NORMAL);
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
            if (MadgwickFilter.call_count == 0)
                t_prev = event.timestamp;
            else
            {
                MadgwickFilter.dt = (event.timestamp - t_prev) * Math.pow(10, -9);
                t_prev = event.timestamp;
            }
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

        if (response_count == sensors_count)
        {
            MadgwickFilter.Filtrate();
            response_count = 0;
        }
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
}