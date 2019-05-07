package com.example.madgwick;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class Dialogs extends DialogFragment
{
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Bundle b = getArguments();
        String type = b.getString("type");
        String desc = b.getString("desc");
        if (type != null)
        {
            switch (type)
            {
                case "info":
                    builder.setTitle(R.string.categories)
                            .setItems(R.array.info_options, new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    Dialog dlg = NextDialog(which);
                                    dlg.show();
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int id)
                                {

                                }
                            });
                    break;
                case "speed":
                    CharSequence[] options = getResources().getStringArray(R.array.speed_options);
                    int index = 0;
                    switch (Sensors.sensor_speed)
                    {
                        case 200000:
                            index = 0;
                            break;
                        case 60000:
                            index = 1;
                            break;
                        case 20000:
                            index = 2;
                            break;
                        case 10000:
                            index = 3;
                            break;
                    }

                    for (int i = 0; i < options.length; i++)
                    {
                        if (i == index)
                            options[i] = String.format(options[i].toString(), getString(R.string.current));
                        else
                            options[i] = String.format(options[i].toString(), "");
                    }

                    builder.setTitle(R.string.rates)
                            .setItems(options, new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    switch (which)
                                    {
                                        case 0:
                                            Sensors.sensor_speed = 200000;
                                            break;
                                        case 1:
                                            Sensors.sensor_speed = 60000;
                                            break;
                                        case 2:
                                            Sensors.sensor_speed = 20000;
                                            break;
                                        case 3:
                                            Sensors.sensor_speed = 10000;
                                            break;
                                    }

                                    Sensors.isSpeedChanged = true;
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int id)
                                {

                                }
                            });
                    break;
                case "error":
                    builder.setTitle(R.string.error)
                            .setMessage("Something went wrong while executing.\nError description: " + desc)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int id)
                                {

                                }
                            });
                    break;
            }
        }
        return builder.create();
    }

    Dialog NextDialog(int which)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        switch (which)
        {
            case 0:
                builder.setTitle(R.string.specs)
                        .setMessage("Accelerometer:\n\n" + Sensors.accSpec + "\n\nGyroscope:\n\n" + Sensors.gyrSpec + "\n\nMagnetometer:\n\n" + Sensors.magSpec)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int id)
                            {

                            }
                        });
                break;
            case 1:
                builder.setTitle(R.string.help)
                        .setMessage(R.string.help_content)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int id)
                            {

                            }
                        });
                break;
        }
        return builder.create();
    }
}
