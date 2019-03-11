package com.sprvtec.whizzy.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.sprvtec.whizzy.R;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;


public class Utils {
    private static AlertDialog internetAlertDialog;
    private Dialog dialog;

    /**
     * This method checks weather the internet was connected or not.
     *
     * @param context - application context
     * @return <i>true</i> if internet is connected, <i>false</i> if internet was not connected.
     */
    public static boolean isNetConnected(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {

            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (NetworkInfo anInfo : info)
                    if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
        }
        return false;
    }

    /**
     * Function to display simple Alert Dialog
     *
     * @param context - application context
     */
    public static void showDialog(final Context context) {
        if (internetAlertDialog != null)
            if (internetAlertDialog.isShowing() && !((Activity) context).isFinishing())
                internetAlertDialog.dismiss();
        internetAlertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        internetAlertDialog.setTitle("No internet connection");
        internetAlertDialog.setCancelable(false);
        // Setting Dialog Message
        internetAlertDialog.setMessage("Please check your wifi/mobile data signal");

        // Setting alert dialog icon
//            alertDialog.setIcon((status) ? R.drawable.success : R.drawable.fail);

        // Setting OK Button
        internetAlertDialog.setButton(Dialog.BUTTON_POSITIVE, "Ok", (dialog, which) -> {

//                System.exit(0);
            ((Activity) context).finish();
            internetAlertDialog.dismiss();
        });

        // Showing Alert Message
        try {
            if (!((Activity) context).isFinishing())
                internetAlertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }
    }

    public static void showDialogPermission(final Context context, String message) {
        if (internetAlertDialog != null)
            if (internetAlertDialog.isShowing() && !((Activity) context).isFinishing())
                internetAlertDialog.dismiss();
//        public void showAlertDialog(Context context, String title, String message, Boolean status) {
        internetAlertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        internetAlertDialog.setTitle("Permission Required");
        internetAlertDialog.setCancelable(false);
        // Setting Dialog Message
        internetAlertDialog.setMessage(message);
//        alertDialog.setMessage("Please check your wifi/mobile data to access the application");

        // Setting alert dialog icon
//            alertDialog.setIcon((status) ? R.drawable.success : R.drawable.fail);

        // Setting OK Button
        internetAlertDialog.setButton(Dialog.BUTTON_POSITIVE, "Ok", (dialog, which) -> {

//                System.exit(0);
            ((Activity) context).finish();
            internetAlertDialog.dismiss();
        });

        // Showing Alert Message
        try {
            if (!((Activity) context).isFinishing())
                internetAlertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }
    }

    public static void showDialogFailure(final Context context, String message) {
        if (internetAlertDialog != null && internetAlertDialog.isShowing() && !((Activity) context).isFinishing())
            internetAlertDialog.dismiss();
//        public void showAlertDialog(Context context, String title, String message, Boolean status) {
        internetAlertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
//        internetAlertDialog.setTitle("Permission Required");
        internetAlertDialog.setCancelable(false);
        // Setting Dialog Message
        internetAlertDialog.setMessage(message);

        internetAlertDialog.setButton(Dialog.BUTTON_POSITIVE, "Ok", (dialog, which) -> internetAlertDialog.dismiss());

        // Showing Alert Message
        try {
            if (!((Activity) context).isFinishing())
                internetAlertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }
    }

    public static void showDialogOpps(final Context context, String text) {
        if (internetAlertDialog != null)
            if (internetAlertDialog.isShowing() && !((Activity) context).isFinishing())
                internetAlertDialog.dismiss();
//        public void showAlertDialog(Context context, String title, String message, Boolean status) {
        internetAlertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        internetAlertDialog.setTitle("Oops!!!");
        internetAlertDialog.setCancelable(false);
        // Setting Dialog Message
        internetAlertDialog.setMessage("Something went wrong Please try again after sometime!" + "\n" + text);
//        alertDialog.setMessage("Please check your wifi/mobile data to access the application");

        // Setting alert dialog icon
//            alertDialog.setIcon((status) ? R.drawable.success : R.drawable.fail);

        // Setting OK Button
        internetAlertDialog.setButton(Dialog.BUTTON_POSITIVE, "Ok", (dialog, which) -> {

//                System.exit(0);
            internetAlertDialog.dismiss();
        });

        // Showing Alert Message
        try {
            if (!((Activity) context).isFinishing())
                internetAlertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }
    }


    /**
     * This method copies the InputStream to OutputStream
     *
     * @param is InputStream instance
     * @param os OutputStream instance
     */
    public static void CopyStream(InputStream is, OutputStream os) {
        final int buffer_size = 1024;
        try {

            byte[] bytes = new byte[buffer_size];
            for (; ; ) {
                //Read byte from input stream

                int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                    break;

                //Write byte from output stream
                os.write(bytes, 0, count);
            }
        } catch (Exception ignored) {
        }
    }

    public void showDialog(String titleText, Context context) {
        if (dialog != null && dialog.isShowing() && !((Activity) context).isFinishing()) {
            dialog.dismiss();
        } else {
            dialog = new Dialog(context);

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setContentView(R.layout.dialog_interest_settings);
            dialog.setCanceledOnTouchOutside(true);
            TextView title = dialog.findViewById(R.id.text);

            title.setText(titleText);
            Button ok = dialog.findViewById(R.id.continuee);
            ok.setText(context.getResources().getString(R.string.ok));
            ok.setOnClickListener(v -> dialog.dismiss());
            if (!((Activity) context).isFinishing())
                dialog.show();
        }
    }


    public Bitmap decodeUri(Uri selectedImage, Context context) throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(
                context.getContentResolver().openInputStream(selectedImage), null, o);

        final int REQUIRED_SIZE = 400; // default 100

        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (width_tmp / 2 >= REQUIRED_SIZE && height_tmp / 2 >= REQUIRED_SIZE) {
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(
                context.getContentResolver().openInputStream(selectedImage), null, o2);
    }


}
