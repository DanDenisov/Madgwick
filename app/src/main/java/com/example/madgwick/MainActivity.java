package com.example.madgwick;

import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity
{
    private Sensors sensors_inst;
    private TextView path;

    Button button_start, button_info, button_speed;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensors_inst = new Sensors(this, this);

        sensors_inst.X_val = findViewById(R.id.X);
        sensors_inst.Y_val = findViewById(R.id.Y);
        sensors_inst.Z_val = findViewById(R.id.Z);

        path = findViewById(R.id.saved_to);

        button_info = findViewById(R.id.Info);
        button_info.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Bundle b = new Bundle();
                b.putString("type", "info");

                Dialogs msg = new Dialogs();
                msg.setArguments(b);
                msg.show(getFragmentManager(), "dlg1");
            }
        });

        button_start = findViewById(R.id.Start);
        button_start.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!Sensors.isBeingMonitored)
                {
                    sensors_inst.RegisterSensors();
                    Sensors.isBeingMonitored = true;

                    try
                    {
                        String filepath = Environment.getExternalStorageDirectory().getPath();
                        File folder = new File(filepath, getString(R.string.directory));
                        if (!folder.exists())
                        {
                            if (!folder.mkdirs())
                            {
                                Bundle b = new Bundle();
                                b.putString("type", "error");
                                b.putString("desc", "Could not create a folder.");

                                Dialogs msg = new Dialogs();
                                msg.setArguments(b);
                                msg.show(getFragmentManager(), "dlg2");
                            }
                        }

                        Calendar time = Calendar.getInstance();
                        String filename = time.get(Calendar.HOUR) + ":" + time.get(Calendar.MINUTE) + ":" + time.get(Calendar.SECOND) +
                                "_" + time.get(Calendar.DAY_OF_MONTH) + "-" + time.get(Calendar.MONTH) + "-" + time.get(Calendar.YEAR) + ".txt";

                        File file = new File(folder, "/" + filename);
                        Sensors.writer = new FileWriter(file);

                        path.setText(getString(R.string.saved_to, "Files are saved to:\n" + folder.getAbsolutePath()));
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }

                    button_start.setText(R.string.btn_stop);
                }
                else
                {
                    sensors_inst.UnregisterSensors();
                    Sensors.isBeingMonitored = false;

                    try
                    {
                        Sensors.writer.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }

                    button_start.setText(R.string.btn_start);
                }
            }
        });

        button_speed = findViewById(R.id.Speed);
        button_speed.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Bundle b = new Bundle();
                b.putString("type", "speed");

                Dialogs msg = new Dialogs();
                msg.setArguments(b);
                msg.show(getFragmentManager(), "dlg3");
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (Sensors.isBeingMonitored)
            sensors_inst.RegisterSensors();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        sensors_inst.UnregisterSensors();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (requestCode == R.integer.REQUEST_WRITE_EXTERNAL && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            sensors_inst.WriteToFile(sensors_inst.content);
    }
}