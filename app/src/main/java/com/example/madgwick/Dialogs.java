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
        builder.setTitle(R.string.categories)
                .setItems(R.array.options, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Dialog dlg = NextDialog(which);
                        dlg.show();
                    }
                });
        return builder.create();
    }

    public Dialog NextDialog(int which)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        switch (which)
        {
            case 0:
                builder.setTitle(R.string.specs)
                        .setMessage("Accelerometer:\n\n" + Sensors.accSpec + "\n\nGyroscope:\n\n" + Sensors.gyrSpec)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                        {
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
                            public void onClick(DialogInterface dialog, int id)
                            {

                            }
                        });
                break;
        }
        return builder.create();
    }
}
