package com.example.madgwick;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import java.io.FileWriter;
import java.io.IOException;

public class Sensors implements SensorEventListener
{
    private Context context;

    static Boolean isSpeedChanged = false;
    static Boolean isBeingMonitored = false;

    static int sensor_speed = 200000;
    private SensorManager sensorManager;
    static Sensor gyr, acc, mag;
    static String gyrSpec, accSpec, magSpec;

    private Boolean InvokeFiltration = false;
    private Boolean gyrQueried = false, accQueried = false, magQueried = false;
    private long t_prev;

    TextView wX_val, wY_val, wZ_val;
    TextView aX_val, aY_val, aZ_val;
    TextView mX_val, mY_val, mZ_val;
    TextView X_val, Y_val, Z_val;

    static private final double RadToDeg = 180 / Math.PI;
    static FileWriter writer;

    Sensors(Context c)
    {
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
        {
            magSpec = "Not presented.";
            magQueried = true;
        }
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event)
    {
        if (isSpeedChanged)
        {
            UnregisterSensors();
            RegisterSensors();

            isSpeedChanged = false;
        }

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

            wX_val.setText(context.getString(R.string.value, event.values[0]));
            wY_val.setText(context.getString(R.string.value, event.values[1]));
            wZ_val.setText(context.getString(R.string.value, event.values[2]));

            gyrQueried = true;
        }
        if (!accQueried && event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            MadgwickFilter.a = new double[] {0, event.values[0], event.values[1], event.values[2]};

            aX_val.setText(context.getString(R.string.value, event.values[0]));
            aY_val.setText(context.getString(R.string.value, event.values[1]));
            aZ_val.setText(context.getString(R.string.value, event.values[2]));

            accQueried = true;
        }
        if (!magQueried && event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
        {
            MadgwickFilter.m = new double[] {0, event.values[0], event.values[1], event.values[2]};

            mX_val.setText(context.getString(R.string.value, event.values[0]));
            mY_val.setText(context.getString(R.string.value, event.values[1]));
            mZ_val.setText(context.getString(R.string.value, event.values[2]));

            magQueried = true;
        }

        if (gyrQueried && accQueried && magQueried)
        {
            if (InvokeFiltration)
            {
                double[] filtrated = MadgwickFilter.Filtrate();

                //outputting results on the screen
                X_val.setText(context.getString(R.string.value, filtrated[0] * RadToDeg));
                Y_val.setText(context.getString(R.string.value, filtrated[1] * RadToDeg));
                Z_val.setText(context.getString(R.string.value, filtrated[2] * RadToDeg));

                //writing results to a file
                String content = event.timestamp / 1000000 + "," + filtrated[0] * RadToDeg + "," + filtrated[1] * RadToDeg + "," + filtrated[2] * RadToDeg + "\n";
                WriteToFile(content);
            }
            else
            {
                InvokeFiltration = true;
            }

            //drop state
            gyrQueried = accQueried = false;
            if (mag != null)
                magQueried = false;
        }
    }

    void RegisterSensors()
    {
        sensorManager.registerListener(this, acc, sensor_speed);
        sensorManager.registerListener(this, gyr, sensor_speed);
        sensorManager.registerListener(this, mag, sensor_speed);
    }

    void UnregisterSensors()
    {
        sensorManager.unregisterListener(this);
    }

    private void WriteToFile(String data)
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