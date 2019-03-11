package com.sprvtec.whizzy.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sprvtec.whizzy.R;
import com.sprvtec.whizzy.util.Constants;
import com.sprvtec.whizzy.util.PermissionUtil;
import com.sprvtec.whizzy.util.Utils;
import com.sprvtec.whizzy.util.Webservice;
import com.sprvtec.whizzy.vo.CustomerRegistrationResponse;
import com.sprvtec.whizzy.vo.GetNearByVehiclesResponse;
import com.sprvtec.whizzy.vo.UserDetails;
import com.squareup.picasso.Picasso;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

//import android.support.design.widget.Snackbar;

public class ProfileActivity extends Activity {

    private EditText name, number, email;

    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private SharedPreferences sp;
    private static final String TAG = "SignUpActivity";
    private View mLayout;
    private final int WRITE_EXTERNAL_STORAGE_PERMISSION = 130;
    private Uri outPutfileUri;
    private String path = "", encodedImage = "";
    private ImageView image;
    Dialog dialog1;
    private Dialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        name = findViewById(R.id.name);
        number = findViewById(R.id.number);
        mLayout = findViewById(R.id.main);
        email = findViewById(R.id.email);
        image = findViewById(R.id.image);
        TextView register = findViewById(R.id.register);
        TextView title = findViewById(R.id.title);
        register.setText("Update");
        title.setText("Profile");
        number.setEnabled(false);
        number.setClickable(false);
        register.setOnClickListener(v -> registerCheck());
        new GetProfileDetailsTask(this).execute();
        findViewById(R.id.back).setOnClickListener(v -> finish());
        image.setOnClickListener(view -> {
            if (ActivityCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestExternalStoragePermission();
            } else
                showDialogForEditPicture();
        });


    }

    void registerCheck() {
//        if (isValidMobile(number.getText().toString()))
        if (!name.getText().toString().trim().equals("") && name.getText().toString().trim().length() > 2) {
            if (email.getText().toString().trim().equals("") || isValidEmaillId(email.getText().toString().trim())) {

                new UpdateUserProfileTask(ProfileActivity.this, name.getText().toString().trim(), number.getText().toString().trim(), email.getText().toString()).execute();

            } else {
                Utils.showDialogFailure(ProfileActivity.this, "Invalid Email Address.");
            }
        } else
            Utils.showDialogFailure(ProfileActivity.this, "Please enter valid User Name");
//        else
//            Utils.showDialogFailure(getApplicationContext(), "Please enter valid mobile number");
    }

    private void requestExternalStoragePermission() {
        Log.i(TAG, "read phone state permission has NOT been granted. Requesting permission.");

        // BEGIN_INCLUDE(Record_permission)
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Log.i(TAG,
                    "Displaying phone contacts permission rationale to provide additional context.");
            Snackbar.make(mLayout, R.string.permission_external_storage_rationale,
                    Snackbar.LENGTH_LONG)
                    .setAction(R.string.ok, view -> ActivityCompat.requestPermissions(ProfileActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            WRITE_EXTERNAL_STORAGE_PERMISSION))
                    .show();
        } else {

            // Record permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_PERMISSION);
        }
        // END_INCLUDE(phone_state_permission)
    }

    private void showDialogForEditPicture() {
        final Dialog dialog1 = new Dialog(ProfileActivity.this);
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog1.setContentView(R.layout.dialog_profile_pic);
        dialog1.setCanceledOnTouchOutside(false);
        TextView camera = dialog1.findViewById(R.id.take_photo);
        TextView gallery = dialog1.findViewById(R.id.gallery);
        TextView cancel = dialog1.findViewById(R.id.cancel);
        camera.setOnClickListener(v -> {
            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(takePicture, 0);
            File photo;
            try {
                photo = getOutputMediaFile();
                photo.delete();
            } catch (Exception e) {
                Log.v(TAG, "Can't create file to take picture!");
                Utils.showDialogFailure(ProfileActivity.this, "Please check SD card! Image shot is impossible!");
                return;
            }
//            outPutfileUri = Uri.fromFile(photo);
//            Log.e("URIis:", outPutfileUri + "");

            ///////
            outPutfileUri = FileProvider.getUriForFile(
                    ProfileActivity.this,
                    ProfileActivity.this.getApplicationContext()
                            .getPackageName() + ".provider", photo);
            Log.e("URIis:", outPutfileUri + "");
            takePicture.putExtra(MediaStore.EXTRA_OUTPUT, outPutfileUri);
            takePicture.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            ////////

//                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, outPutfileUri);
            startActivityForResult(takePicture, 0);
            dialog1.dismiss();
        });
        gallery.setOnClickListener(v -> {
            Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhoto, 1);//one can be replaced with any action code
            dialog1.dismiss();
        });
        cancel.setOnClickListener(v -> dialog1.dismiss());

        dialog1.show();
    }

    private boolean isValidEmaillId(String email) {
        return email.matches(emailPattern);

//        return Pattern.compile("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
//                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
//                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
//                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
//                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
//                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$").matcher(email).matches();
    }


    @SuppressLint("StaticFieldLeak")
    private class GetProfileDetailsTask extends AsyncTask<String, Void, String> {
        private Context context;
        //used registration response pattern to avoid classes creation
        private GetNearByVehiclesResponse getProfileDetailsResponse;
        private ProgressDialog progressDialog;

        GetProfileDetailsTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("loading...");
            progressDialog.setCanceledOnTouchOutside(false);
            if (!ProfileActivity.this.isFinishing())
                progressDialog.show();
//            loading.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {

            String url = Webservice.GET_PROFILE_DETAILS + "?" + Webservice.USER_ID + "=" + sp.getString(Constants.PreferenceKey.KEY_USER_ID, "") + "&" + Webservice.USER_MOBILE + "=" + sp.getString(Constants.PreferenceKey.KEY_CONSUMER_MOBILE, "");

            String json = Webservice.callGetService(url);
            String status = "";
            Log.e("Response send request", json);
            try {
                // Convert String to json object
                JSONObject jsonObject = new JSONObject(json);
                status = jsonObject.getString("status");

            } catch (JSONException e) {
                e.printStackTrace();
                return e.getMessage();
//                return context.getResources().getString(R.string.json_exception);
            }

            if (status.equalsIgnoreCase("success")) {
                try {
                    getProfileDetailsResponse = new Gson().fromJson(json, GetNearByVehiclesResponse.class);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    getProfileDetailsResponse = new Gson().fromJson(json, GetNearByVehiclesResponse.class);
                    // Convert String to json object
                    JSONObject jsonObject = new JSONObject(json);

                    return jsonObject.getJSONObject("data").getString("message");

                } catch (JSONException e) {
                    e.printStackTrace();
                    return e.getMessage();
//                    return context.getResources().getString(R.string.json_exception);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String resp) {
            super.onPostExecute(resp);
            if (progressDialog != null && progressDialog.isShowing() && !ProfileActivity.this.isFinishing())
                progressDialog.dismiss();
//            loading.setVisibility(View.GONE);
            if (resp == null) {
                if (getProfileDetailsResponse != null) {
                    updateUI(getProfileDetailsResponse.data.user_details);
                }
            } else if (!Utils.isNetConnected(context))
                showDialog1(context, "No internet connection", "Please check your wifi/mobile data signal");
            else {
                if (getProfileDetailsResponse != null && !getProfileDetailsResponse.data.message.equals(""))
                    Utils.showDialogFailure(context, resp);
                else
                    showDialog1(context, "Oops!!!", "Something went wrong Please try again after sometime!");
            }
        }
    }

    void showDialog1(final Context context, String title, String message) {

//        public void showAlertDialog(Context context, String title, String message, Boolean status) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);
        alertDialog.setCancelable(false);
        // Setting Dialog Message
        alertDialog.setMessage(message);
//        alertDialog.setMessage("Please check your wifi/mobile data to access the application");

        // Setting alert dialog icon
//            alertDialog.setIcon((status) ? R.drawable.success : R.drawable.fail);

        // Setting OK Button
        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Retry", (dialog, which) -> {

//                System.exit(0);
//                ((Activity) context).finish();
            alertDialog.dismiss();
            new GetProfileDetailsTask(ProfileActivity.this).execute();
        });

        // Showing Alert Message
        try {
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }
    }

    private void updateUI(UserDetails user_details) {
        number.setText(user_details.user_mobile);
        name.setText(user_details.full_name);
        email.setText(user_details.user_email);
//        imageLoader.DisplayImage(Webservice.PROFILE_IMAGE_PATH + user_details.user_image_url, image, R.drawable.male);
        if (user_details.user_image_url!=null&&!user_details.user_image_url.equals(""))
            Picasso.with(getApplicationContext()).load(Webservice.PROFILE_IMAGE_PATH + user_details.user_image_url).into(image);
    }


    @SuppressLint("StaticFieldLeak")
    class UpdateUserProfileTask extends AsyncTask<String, Void, String> {
        private Context context;
        //used registration response pattern to avoid classes creation
        private CustomerRegistrationResponse updateUserProfileResponse;
        private ProgressDialog progressDialog;
        private SharedPreferences sp;
        private String consumerMobile, email, consumerFirstName;

        UpdateUserProfileTask(Context context, String consumerFirstName, String consumerMobile, String email) {
            this.context = context;
            sp = PreferenceManager.getDefaultSharedPreferences(context);
            this.consumerMobile = consumerMobile;
            this.email = email;
            this.consumerFirstName = consumerFirstName;
            Log.e("Name", consumerFirstName);
            Log.e("Mobile", consumerMobile);
            Log.e("Email", email);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("loading...");
            if (!ProfileActivity.this.isFinishing())
                progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> nameValuePairs = new ArrayList<>();

            nameValuePairs.add(new BasicNameValuePair(Webservice.FULL_NAME, consumerFirstName));
            nameValuePairs.add(new BasicNameValuePair(Webservice.USER_MOBILE, consumerMobile));
            nameValuePairs.add(new BasicNameValuePair(Webservice.USER_EMAIL, email));
            nameValuePairs.add(new BasicNameValuePair(Webservice.USER_ID, sp.getString(Constants.PreferenceKey.KEY_USER_ID, "0")));
            if (!encodedImage.equals(""))
                nameValuePairs.add(new BasicNameValuePair(Webservice.USER_IMAGE_URL, encodedImage));
            String json = Webservice.callPostService1(Webservice.UPDATE_USER_PROFILE, nameValuePairs);
            String status = "";
            Log.e("INTEREST", json);


            try {
                // Convert String to json object
                JSONObject jsonObject = new JSONObject(json);
                status = jsonObject.getString("status");

            } catch (JSONException e) {
                e.printStackTrace();
                return e.getMessage();
//            return context.getResources().getString(R.string.json_exception);
            }

            if (status.equalsIgnoreCase("success")) {
                try {
                    updateUserProfileResponse = new Gson().fromJson(json, CustomerRegistrationResponse.class);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    updateUserProfileResponse = new Gson().fromJson(json, CustomerRegistrationResponse.class);
                    // Convert String to json object
                    JSONObject jsonObject = new JSONObject(json);

                    return jsonObject.getJSONObject("data").getString("message");

                } catch (JSONException e) {
                    e.printStackTrace();
                    return e.getMessage();
//                return context.getResources().getString(R.string.json_exception);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String resp) {
            super.onPostExecute(resp);
            if (progressDialog != null && progressDialog.isShowing() && !ProfileActivity.this.isFinishing())
                progressDialog.dismiss();
            if (resp == null) {
                if (updateUserProfileResponse != null) {
                    sp.edit().putString(Constants.PreferenceKey.KEY_FULL_NAME, consumerFirstName).apply();
                    successDialog(updateUserProfileResponse.data.message);
//                    Utils utils = new Utils();
//                    utils.showDialog(updateUserProfileResponse.data.message, ProfileActivity.this);
                }
            } else if (!Utils.isNetConnected(context))
                showDialog2(context, "No internet connection", "Please check your wifi/mobile data signal");
            else {
                if (updateUserProfileResponse != null && !updateUserProfileResponse.data.message.equals(""))
                    Utils.showDialogFailure(context, resp);
                else
                    showDialog2(context, "Oops!!!", "Something went wrong Please try again after sometime!");
            }
        }
    }

    public void successDialog(String message) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        } else {
            dialog = new Dialog(ProfileActivity.this);

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setContentView(R.layout.dialog_interest_settings);
            dialog.setCancelable(false);
            TextView title = dialog.findViewById(R.id.text);

            title.setText(message);
            Button ok = dialog.findViewById(R.id.continuee);
            ok.setText(getApplicationContext().getResources().getString(R.string.ok));
            ok.setOnClickListener(v -> {
                dialog.dismiss();
                finish();

            });

            dialog.show();
        }
    }

    void showDialog2(final Context context, String title, String message) {

//        public void showAlertDialog(Context context, String title, String message, Boolean status) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);
        alertDialog.setCancelable(false);
        // Setting Dialog Message
        alertDialog.setMessage(message);
//        alertDialog.setMessage("Please check your wifi/mobile data to access the application");

        // Setting alert dialog icon
//            alertDialog.setIcon((status) ? R.drawable.success : R.drawable.fail);

        // Setting OK Button
        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Retry", (dialog, which) -> {

//                System.exit(0);
//                ((Activity) context).finish();
            alertDialog.dismiss();
            new UpdateUserProfileTask(ProfileActivity.this, name.getText().toString().trim(), number.getText().toString().trim(), email.getText().toString()).execute();
        });

        // Showing Alert Message
        try {
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }
    }

    private File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Whizzy");

        /*Create the storage directory if it does not exist*/
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        /*Create a media file name*/
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
//        if (1 == 1) {
        path = mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".png";
        mediaFile = new File(path);
//        } else {
//            return null;
//        }

        return mediaFile;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outPutfileUri != null) {
            outState.putString("cameraImageUri", outPutfileUri.toString());
        }
        if (path != null)
            outState.putString("PATH", path);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("cameraImageUri")) {
            outPutfileUri = Uri.parse(savedInstanceState.getString("cameraImageUri"));
        }
        if (savedInstanceState.containsKey("PATH")) {
            path = savedInstanceState.getString("PATH");
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    Bitmap bitmap = null;
                    Utils utils = new Utils();
                    try {
                        bitmap = utils.decodeUri(outPutfileUri, ProfileActivity.this);
                        Log.e(TAG, bitmap.getByteCount() + " SIZE");//23970816
//                        image.setImageBitmap(bitmap);
                        image.setImageBitmap(rotateImageFun(bitmap));
//                        submit.setEnabled(true);
//                        submit.setBackgroundResource(R.drawable.button_brown_curve);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.i(TAG, convertBase64(bitmap));

                }

                break;
            case 1:
                if (resultCode == RESULT_OK) {
                    Log.i("ActivityResult", "true");
                    Uri selectedImage = imageReturnedIntent.getData();
                    Bitmap bitmap = null;
                    Utils utils = new Utils();
                    try {
                        bitmap = utils.decodeUri(selectedImage, ProfileActivity.this);
                        Log.e(TAG, bitmap.getByteCount() + " SIZE");//23970816
                        image.setImageBitmap(bitmap);
//                        submit.setEnabled(true);
//                        submit.setBackgroundResource(R.drawable.button_brown_curve);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.i(TAG, convertBase64(bitmap));
                }
                break;
//            case 111:
//                if (resultCode == RESULT_OK) {
//                    text.setVisibility(View.VISIBLE);
//                }
        }
    }

    private Bitmap rotateImageFun(Bitmap bitmap) {
        ExifInterface ei = null;
        try {
            ei = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap rotatedBitmap;
        switch (orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(bitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;
        }
        return rotatedBitmap;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    private String convertBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
        byte[] byteArrayImage = baos.toByteArray();
        encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
        return encodedImage;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {


        if (requestCode == WRITE_EXTERNAL_STORAGE_PERMISSION) {
            Log.i(TAG, "Received response for location permissions request.");

            // We have requested multiple permissions for contacts, so all of them need to be
            // checked.
            if (PermissionUtil.verifyPermissions(grantResults)) {
                // All required permissions have been granted, display contacts fragment.
                Snackbar.make(mLayout, R.string.permission_available_location,
                        Snackbar.LENGTH_SHORT)
                        .show();
                showDialogForEditPicture();
            } else {
                Log.i(TAG, "Location permissions were NOT granted.");
                Snackbar.make(mLayout, R.string.permissions_not_granted,
                        Snackbar.LENGTH_SHORT)
                        .show();
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
