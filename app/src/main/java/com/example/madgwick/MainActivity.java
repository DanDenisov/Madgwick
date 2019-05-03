package com.example.madgwick;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
{
    static protected TextView X, Y, Z;
    static protected Sensors sensors_inst;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Sensors.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensors_inst = new Sensors();

        X = findViewById(R.id.X);
        Y = findViewById(R.id.Y);
        Z = findViewById(R.id.Z);

        final Button button = findViewById(R.id.Info);
        button.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Dialogs msg = new Dialogs();
                msg.show(getFragmentManager(), "dlg");
            }
        });


    }
}