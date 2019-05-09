package com.example.madgwick;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
{
    private Sensors sensors_inst;
    private TextView path, betta_val, zeta_val;
    Button button_start, button_info, button_speed;
    SeekBar betta_seek, zeta_seek;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensors_inst = new Sensors(this);

        sensors_inst.wX_val = findViewById(R.id.wX_val);
        sensors_inst.wY_val = findViewById(R.id.wY_val);
        sensors_inst.wZ_val = findViewById(R.id.wZ_val);

        sensors_inst.aX_val = findViewById(R.id.aX_val);
        sensors_inst.aY_val = findViewById(R.id.aY_val);
        sensors_inst.aZ_val = findViewById(R.id.aZ_val);

        sensors_inst.mX_val = findViewById(R.id.mX_val);
        sensors_inst.mY_val = findViewById(R.id.mY_val);
        sensors_inst.mZ_val = findViewById(R.id.mZ_val);

        sensors_inst.X_val = findViewById(R.id.X_val);
        sensors_inst.Y_val = findViewById(R.id.Y_val);
        sensors_inst.Z_val = findViewById(R.id.Z_val);

        path = findViewById(R.id.saved_to);
        betta_val = findViewById(R.id.betta_val);
        betta_val.setText(String.format(Locale.ENGLISH,"%.2f", MadgwickFilter.betta));
        zeta_val = findViewById(R.id.zeta_val);
        zeta_val.setText(String.format(Locale.ENGLISH,"%.2f", MadgwickFilter.zeta));

        betta_seek = findViewById(R.id.betta_seek);
        betta_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                MadgwickFilter.betta = progress + 1;
                betta_val.setText(String.format(Locale.ENGLISH,"%.2f", MadgwickFilter.betta));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        zeta_seek = findViewById(R.id.zeta_seek);
        zeta_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                MadgwickFilter.zeta = progress + 1;
                zeta_val.setText(String.format(Locale.ENGLISH,"%.2f", MadgwickFilter.zeta));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

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
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                    {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                                MainActivity.this.getResources().getInteger(R.integer.REQUEST_WRITE_EXTERNAL));
                    }
                    else
                    {
                        sensors_inst.RegisterSensors();
                        Sensors.isBeingMonitored = true;
                        MadgwickFilter.init_asmp = true;

                        GetFile();

                        button_start.setText(R.string.btn_stop);
                    }
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

    void GetFile()
    {
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
            String filename = DateFormat.format("HH:mm:ss_dd-MM-yyyy", time).toString() + ".csv";

            File file = new File(folder, "/" + filename);
            Sensors.writer = new FileWriter(file);
            Sensors.writer.append("Timestamp,X,Y,Z\n");
            Sensors.writer.flush();

            path.setText(getString(R.string.saved_to, "Files are saved to:\n" + folder.getAbsolutePath()));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
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
        if (requestCode == getResources().getInteger(R.integer.REQUEST_WRITE_EXTERNAL) && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            sensors_inst.RegisterSensors();
            Sensors.isBeingMonitored = true;
            MadgwickFilter.init_asmp = true;

            GetFile();

            button_start.setText(R.string.btn_stop);
        }
    }
}