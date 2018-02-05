package me.pushkaranand.simplebudget;


import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;

class MenuFunctions {
    static void openSettingsActivity(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }

    static void openAboutActivity(Context context) {
        new LibsBuilder()
                .withActivityStyle(Libs.ActivityStyle.DARK)
                .start(context);
    }

    static void openBackupActivity(Context context) {
        Intent b = new Intent(context, BackupActivity.class);
        context.startActivity(b);
    }

    static void resetSpendsDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Reset Spends")
                .setMessage("Do you want to reset this months spends??")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        Intent x = new Intent(context, ResetSpendService.class);
                        context.startService(x);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    static void getFeedback(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setCancelable(false)
                .setTitle("Feedback")
                .setMessage("Are you satisfied with this app??")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        Toast.makeText(context, "Please rate this application with 5 stars",
                                Toast.LENGTH_LONG).show();
                        try {
                            context.startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("market://details?id=" + context.getResources().getString(R.string.appID))));
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();
                            context.startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse(context.getResources().getString(R.string.app_playstore_link))));
                        }

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        Intent Email = new Intent(Intent.ACTION_SEND);
                        Email.setType("text/email");
                        Email.putExtra(Intent.EXTRA_EMAIL, new String[]{"anandpushkar088@gmail.com"});
                        Email.putExtra(Intent.EXTRA_SUBJECT, "Feedback for app Simple Budget");
                        context.startActivity(Intent.createChooser(Email, "Send Feedback:"));
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
