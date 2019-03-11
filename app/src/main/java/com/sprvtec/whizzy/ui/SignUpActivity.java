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
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sprvtec.whizzy.R;
import com.sprvtec.whizzy.util.Constants;
import com.sprvtec.whizzy.util.GlobalApplication;
import com.sprvtec.whizzy.util.PermissionUtil;
import com.sprvtec.whizzy.util.Utils;
import com.sprvtec.whizzy.util.Webservice;
import com.sprvtec.whizzy.vo.CustomerRegistrationResponse;

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


public class SignUpActivity extends Activity {

    private EditText name, number, email;

    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private String android_id;
    private static final String TAG = "SignUpActivity";
    private View mLayout;
    private final int WRITE_EXTERNAL_STORAGE_PERMISSION = 130;
    private Uri outPutfileUri;
    private String path = "", encodedImage = "";
    private ImageView image;
    private Dialog otpDialog;
    private Dialog cont_dailog;
    private Dialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        name = findViewById(R.id.name);
        number = findViewById(R.id.number);
        email = findViewById(R.id.email);
        mLayout = findViewById(R.id.main);
        TextView register = findViewById(R.id.register);
        image = findViewById(R.id.image);
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(SignUpActivity.this, instanceIdResult -> {
            String newToken = instanceIdResult.getToken();
            Log.e("newToken", newToken);
            GlobalApplication.fcmToken = newToken;

        });
        register.setOnClickListener(v -> registerCheck());
        android_id = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);

        android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        findViewById(R.id.back).setOnClickListener(v -> finish());
        image.setOnClickListener(view -> {
            if (ActivityCompat.checkSelfPermission(SignUpActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestExternalStoragePermission();
            } else
                showDialogForEditPicture();
        });
        cont_dailog = new Dialog(SignUpActivity.this);
        cont_dailog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        cont_dailog.setContentView(R.layout.calllayout);
        cont_dailog.setCanceledOnTouchOutside(true);

        Button call = cont_dailog.findViewById(R.id.call);
        Button dimsiss = cont_dailog.findViewById(R.id.dismiss_call);

        call.setOnClickListener(v -> {
            Intent callWhizzy = new Intent(Intent.ACTION_DIAL);
            callWhizzy.setData(Uri.parse("tel:" + Constants.KEY_SUPPORT_NUMBER));

//            if (ContextCompat.checkSelfPermission(SignUpActivity.this,
//                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//                cont_dailog.dismiss();
//                requestReadPhoneStatePermission();
//                // TODO: Consider calling
//                //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for Activity#requestPermissions for more details.
//                return;
//            }
            cont_dailog.dismiss();
            startActivity(callWhizzy);
        });

        dimsiss.setOnClickListener(view -> cont_dailog.dismiss());

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
                    .setAction(R.string.ok, view -> ActivityCompat.requestPermissions(SignUpActivity.this,
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
        final Dialog dialog1 = new Dialog(SignUpActivity.this);
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
                Utils.showDialogFailure(SignUpActivity.this, "Please check SD card! Image shot is impossible!");
                return;
            }
//            outPutfileUri = Uri.fromFile(photo);
//            Log.e("URIis:", outPutfileUri + "");

            ///////
            outPutfileUri = FileProvider.getUriForFile(
                    SignUpActivity.this,
                    SignUpActivity.this.getApplicationContext()
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

    void registerCheck() {
//        if (isValidMobile(number.getText().toString()))
        if (!name.getText().toString().trim().equals("") && name.getText().toString().trim().length() > 2)
            if (email.getText().toString().trim().equals("") || isValidEmaillId(email.getText().toString().trim())) {

                new SendOtpTask(SignUpActivity.this, name.getText().toString().trim(), number.getText().toString().trim(), email.getText().toString()).execute();

            } else {
                Utils.showDialogFailure(SignUpActivity.this, "InValid Email Address.");
            }
        else
            Utils.showDialogFailure(SignUpActivity.this, "Please enter valid User Name");
    }

    private boolean isValidEmaillId(String email) {
        return email.matches(emailPattern);

    }


    @SuppressLint("StaticFieldLeak")
    class SendOtpTask extends AsyncTask<String, Void, String> {
        private Context context;
        //used registration response pattern to avoid classes creation
        private CustomerRegistrationResponse validateOTPResponse;
        private ProgressDialog progressDialog;
        private SharedPreferences sp;
        private String consumerMobile, email, consumerFirstName;

        SendOtpTask(Context context, String consumerFirstName, String consumerMobile, String email) {
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
            if (!SignUpActivity.this.isFinishing())
                progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> nameValuePairs = new ArrayList<>();


            nameValuePairs.add(new BasicNameValuePair(Webservice.USER_MOBILE, consumerMobile));
            nameValuePairs.add(new BasicNameValuePair(Webservice.USER_ID, sp.getString(Constants.PreferenceKey.KEY_USER_ID, "0")));


            Log.e("Name", consumerFirstName);
            Log.e("Mobile", consumerMobile);
            Log.e("Email", email);
            String json = Webservice.callPostService1(Webservice.SEND_OTP, nameValuePairs);
            String status = "";
            Log.e("INTEREST", json);
            Log.i("INTEREST", json);


            try {
                // Convert String to json object
                JSONObject jsonObject = new JSONObject(json);
                status = jsonObject.getString("status");

            } catch (JSONException e) {
                e.printStackTrace();
                return e.getMessage();
            }

            if (status.equalsIgnoreCase("success")) {
                try {
                    validateOTPResponse = new Gson().fromJson(json, CustomerRegistrationResponse.class);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    validateOTPResponse = new Gson().fromJson(json, CustomerRegistrationResponse.class);

                    // Convert String to json object
                    JSONObject jsonObject = new JSONObject(json);

                    return jsonObject.getJSONObject("data").getString("message");

                } catch (JSONException e) {
                    e.printStackTrace();
                    return e.getMessage();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String resp) {
            super.onPostExecute(resp);
            if (progressDialog != null && progressDialog.isShowing() && !SignUpActivity.this.isFinishing())
                progressDialog.dismiss();
            if (resp == null) {
                if (validateOTPResponse != null) {
                    if (!validateOTPResponse.data.OTP.equals("")) {
                        //success response
                        showOTPDialog(consumerMobile, validateOTPResponse.data.OTP);

                    } else {
                        //fail
                    }
                }
            } else if (!Utils.isNetConnected(context))
                showDialog1(context, "No internet connection", "Please check your wifi/mobile data signal");
            else {
                if (validateOTPResponse != null && !validateOTPResponse.data.message.equals(""))
                    Utils.showDialogFailure(context, resp);
                else
                    showDialog1(context, "Oops!!!", "Something went wrong Please try again after sometime!" + "\n" + resp);
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
            new SendOtpTask(SignUpActivity.this, name.getText().toString().trim(), number.getText().toString().trim(), email.getText().toString()).execute();
        });

        // Showing Alert Message
        try {
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }
    }

    @SuppressLint("StaticFieldLeak")
    class RegisteUserTask extends AsyncTask<String, Void, String> {
        private Context context;
        //used registration response pattern to avoid classes creation
        private CustomerRegistrationResponse validateOTPResponse;
        private ProgressDialog progressDialog;
        private SharedPreferences sp;
        private String consumerMobile, email, consumerFirstName;

        RegisteUserTask(Context context, String consumerFirstName, String consumerMobile, String email) {
            this.context = context;
            sp = PreferenceManager.getDefaultSharedPreferences(context);
            this.consumerMobile = consumerMobile;
            this.email = email;
            this.consumerFirstName = consumerFirstName;
            Log.e("Name", consumerFirstName);
            Log.e("Mobile", consumerMobile);
            Log.e("Email", email);
            Log.e("FCM TOKEN", GlobalApplication.fcmToken + "");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("loading...");
//            progressDialog.setCanceledOnTouchOutside(false);
            if (!SignUpActivity.this.isFinishing())
                progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> nameValuePairs = new ArrayList<>();

            nameValuePairs.add(new BasicNameValuePair(Webservice.FULL_NAME, consumerFirstName));
            nameValuePairs.add(new BasicNameValuePair(Webservice.USER_MOBILE, consumerMobile));
            nameValuePairs.add(new BasicNameValuePair(Webservice.USER_EMAIL, email));
            nameValuePairs.add(new BasicNameValuePair(Webservice.DEVICE_ID, android_id));
            nameValuePairs.add(new BasicNameValuePair(Webservice.USER_ID, sp.getString(Constants.PreferenceKey.KEY_USER_ID, "0")));
            nameValuePairs.add(new BasicNameValuePair(Webservice.OPERATING_SYSTEM, "Android"));
            if (GlobalApplication.fcmToken != null)
                nameValuePairs.add(new BasicNameValuePair(Webservice.FCM_TOKEN, GlobalApplication.fcmToken));
            else
                nameValuePairs.add(new BasicNameValuePair(Webservice.FCM_TOKEN, ""));
            if (!encodedImage.equals(""))
                nameValuePairs.add(new BasicNameValuePair(Webservice.USER_IMAGE_URL, encodedImage));
            nameValuePairs.add(new BasicNameValuePair(Webservice.APP_VERSION, Constants.APP_VERSION));

            Log.e("Name", consumerFirstName);
            Log.e("Mobile", consumerMobile);
            Log.e("Email", email);
            String json = Webservice.callPostService1(Webservice.REGISTER_USER, nameValuePairs);
//            String json = Webservice.postData(Webservice.REGISTER_USER, nameValuePairs);
            String status = "";
            Log.e("INTEREST", json);
            Log.i("INTEREST", json);


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
                    validateOTPResponse = new Gson().fromJson(json, CustomerRegistrationResponse.class);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    validateOTPResponse = new Gson().fromJson(json, CustomerRegistrationResponse.class);

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
            if (progressDialog != null && progressDialog.isShowing() && !SignUpActivity.this.isFinishing())
                progressDialog.dismiss();
            if (resp == null) {
                if (validateOTPResponse != null) {
                    sp.edit().putBoolean(Constants.PreferenceKey.KEY_VALID_FLAG, true).apply();
                    sp.edit().putString(Constants.PreferenceKey.KEY_USER_ID, validateOTPResponse.data.user_id).apply();
                    sp.edit().putString(Constants.PreferenceKey.KEY_CONSUMER_MOBILE, consumerMobile).apply();
                    sp.edit().putString(Constants.PreferenceKey.KEY_EMAIL, email).apply();
                    sp.edit().putString(Constants.PreferenceKey.KEY_FULL_NAME, consumerFirstName).apply();


                    successDialog(validateOTPResponse.data.message);
                }
            } else if (!Utils.isNetConnected(context))
                showDialog2(context, "No internet connection", "Please check your wifi/mobile data signal");
            else {
                if (validateOTPResponse != null && !validateOTPResponse.data.message.equals(""))
                    Utils.showDialogFailure(context, resp);
                else
                    showDialog2(context, "Oops!!!", "Something went wrong Please try again after sometime!" + "\n" + resp);
            }
        }
    }

    public void successDialog(String message) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        } else {
            dialog = new Dialog(SignUpActivity.this);

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setContentView(R.layout.dialog_interest_settings);
            dialog.setCancelable(false);
            TextView title = dialog.findViewById(R.id.text);

            title.setText(message);
            Button ok = dialog.findViewById(R.id.continuee);
            ok.setText("OK");
            ok.setOnClickListener(v -> {
                dialog.dismiss();
                Intent intent = getIntent();
                setResult(RESULT_OK, intent);
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
            new RegisteUserTask(SignUpActivity.this, name.getText().toString().trim(), number.getText().toString().trim(), email.getText().toString()).execute();
        });

        // Showing Alert Message
        try {
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }
    }

    void showOTPDialog(String mobileNumber, final String OTP) {
        if (otpDialog != null)
            if (otpDialog.isShowing())
                otpDialog.dismiss();

        otpDialog = new Dialog(SignUpActivity.this);

        otpDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        otpDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        otpDialog.setContentView(R.layout.dialog_otp);
        otpDialog.setCanceledOnTouchOutside(false);
        final EditText otp = otpDialog.findViewById(R.id.verification_code);
        TextView phoneNum = otpDialog.findViewById(R.id.phone);
        phoneNum.setText(getApplicationContext().getResources().getString(R.string.enter_otp_sent) + " " + mobileNumber);
        final Button ok = otpDialog.findViewById(R.id.ok);
        LinearLayout resend = otpDialog.findViewById(R.id.resend);
//        ok.setBackgroundResource(R.drawable.button_disable);
//        TextWatcher mTextWatcher = new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//                // check Fields For Empty Values
//                if ((otp.getText().toString()).equals(OTP)) {
//                    ok.setEnabled(true);
//                    ok.setBackgroundResource(R.drawable.button_blue);
//                } else {
//                    ok.setBackgroundResource(R.drawable.button_disable);
//                    ok.setEnabled(false);
//                }
//            }
//        };
//        otp.addTextChangedListener(mTextWatcher);
        ok.setOnClickListener(v -> {
            if ((otp.getText().toString()).equals(OTP)) {
                otpDialog.dismiss();
                new RegisteUserTask(SignUpActivity.this, name.getText().toString().trim(), number.getText().toString().trim(), email.getText().toString()).execute();
            } else Utils.showDialogFailure(SignUpActivity.this, "Please enter valid OTP");

        });
        otpDialog.findViewById(R.id.call_otp).setOnClickListener(v -> cont_dailog.show());
        resend.setOnClickListener(v -> {
//                new ResendOTPTask(context, userId).execute();
            new SendOtpTask(SignUpActivity.this, name.getText().toString().trim(), number.getText().toString().trim(), email.getText().toString()).execute();

        });
        otpDialog.show();
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
                        bitmap = utils.decodeUri(outPutfileUri, SignUpActivity.this);
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
                        bitmap = utils.decodeUri(selectedImage, SignUpActivity.this);
                        Log.e(TAG, bitmap.getByteCount() + " SIZE");//23970816
                        image.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.i(TAG, convertBase64(bitmap));
                }
                break;

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

        }else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
