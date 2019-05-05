package com.example.madgwick;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
{
    protected Sensors sensors_inst;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Sensors.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensors_inst = new Sensors();

        final Button button_info = findViewById(R.id.Info);
        button_info.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Dialogs msg = new Dialogs();
                msg.show(getFragmentManager(), "dlg");
            }
        });

        final Button button_start = findViewById(R.id.Start);
        button_start.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (button_start.getText() == "Start")
                {
                    Sensors.sensorManager.registerListener(sensors_inst, Sensors.acc, SensorManager.SENSOR_DELAY_NORMAL);
                    Sensors.sensorManager.registerListener(sensors_inst, Sensors.gyr, SensorManager.SENSOR_DELAY_NORMAL);
                    Sensors.sensorManager.registerListener(sensors_inst, Sensors.mag, SensorManager.SENSOR_DELAY_NORMAL);

                    button_start.setText(R.string.btn_stop);
                }
                else
                {
                    Sensors.sensorManager.unregisterListener(sensors_inst);

                    button_start.setText(R.string.btn_start);
                }
            }
        });
    }
}