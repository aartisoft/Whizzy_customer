package com.sprvtec.whizzy.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.sprvtec.whizzy.R;
import com.sprvtec.whizzy.adapter.PhoneContactsListAdapter;
import com.sprvtec.whizzy.util.Constants;
import com.sprvtec.whizzy.util.GlobalApplication;
import com.sprvtec.whizzy.util.Utils;
import com.sprvtec.whizzy.vo.PhoneContact;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

//import android.support.design.widget.Snackbar;


/**
 * Created by SPRV on 5/16/2017.
 */
public class PhoneContactsActivity extends Activity {
    private ListView myContactsList;
    private EditText search;
    private PhoneContactsListAdapter requestsAdapter;
    private List<PhoneContact> contacts = new ArrayList<>();
    //    private ProgressBar progress;
    private final String TAG = "PhoneContactsActivity";
    private View mLayout;
    private int READ_PHONE_CONTACTS = 110;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_phone_contacts);
        mLayout = findViewById(R.id.main);
        myContactsList = findViewById(R.id.contacts_list);
        search = findViewById(R.id.search);
        TextView title = findViewById(R.id.title);
        title.setText(getApplicationContext().getResources().getString(R.string.phone_contacts));

        if (GlobalApplication.getInstance().phoneContacts.size() == 0) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {

                requestReadPhoneContactsPermission();

                Log.e("READ_PHONE_CONTACTS_", " NOT GRANTED");
            } else {
                Log.e("READ_PHONE_CONTACTS_", "GRANTED");
                new GetPhoneContacts(this).execute();
            }

        } else {
            contacts = GlobalApplication.getInstance().phoneContacts;
            requestsAdapter = new PhoneContactsListAdapter(PhoneContactsActivity.this, contacts);
            myContactsList.setAdapter(requestsAdapter);
        }
        findViewById(R.id.close).setOnClickListener(v -> search.setText(""));
        myContactsList.setOnItemClickListener((parent, view, position, id) -> {
            PhoneContact contact = (PhoneContact) parent.getItemAtPosition(position);
            Intent intent = getIntent();
            intent.putExtra(Constants.IntentKey.KEY_CONTACT, contact);
            setResult(RESULT_OK, intent);
            finish();


        });
        search.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                System.out.println("Text [" + s + "]");
                if (requestsAdapter != null)
                    requestsAdapter.getFilter().filter(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        findViewById(R.id.back).setOnClickListener(v -> finish());
    }

    @SuppressLint("StaticFieldLeak")
    private class GetPhoneContacts extends AsyncTask<String, Void, String> {
        private Context context;
        //used registration response pattern to avoid classes creation
//        private UserConnectsResponse connectedContactsResponse;
        private ProgressDialog progressDialog;

        GetPhoneContacts(Context context) {
            this.context = context;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            progress.setVisibility(View.VISIBLE);
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("loading...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            readContacts();
            return null;
        }

        @Override
        protected void onPostExecute(String resp) {
            super.onPostExecute(resp);
            progressDialog.dismiss();
            if(contacts.size()>0) {
                Collections.sort(contacts);
                GlobalApplication.getInstance().phoneContacts = contacts;
                requestsAdapter = new PhoneContactsListAdapter(PhoneContactsActivity.this, contacts);
                myContactsList.setAdapter(requestsAdapter);
            }else Utils.showDialogOpps(PhoneContactsActivity.this, "No Contacts Found");
        }
    }

    public void readContacts() {
        StringBuffer sb = new StringBuffer();
        sb.append("......Contact Details.....");
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
                null, null, null);
        String phone;
        String emailContact;
        String emailType;
        String image_uri;
        Bitmap bitmap;
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur
                        .getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur
                        .getString(cur
                                .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                image_uri = cur
                        .getString(cur
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                if (Integer
                        .parseInt(cur.getString(cur
                                .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
//                    System.out.println("name : " + name + ", ID : " + id);
                    sb.append("\n Contact Name:" + name);
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                    + " = ?", new String[]{id}, null);
                    while (pCur.moveToNext()) {

                        phone = pCur
                                .getString(pCur
                                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        String phone1 = phone.trim().replace("-", "").replace(" ", "");
                        PhoneContact contact = new PhoneContact(name, phone1);
//                        if (!contacts.contains(contact)) {
                        contacts.add(contact);
//                        }

                    }

                    /* Printing original list **/
//                    System.out.println(newList);
                    pCur.close();

                    Cursor emailCur = cr.query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID
                                    + " = ?", new String[]{id}, null);
                    while (emailCur.moveToNext()) {
                        emailContact = emailCur
                                .getString(emailCur
                                        .getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                        emailType = emailCur
                                .getString(emailCur
                                        .getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
                        sb.append("\nEmail:" + emailContact + "Email type:" + emailType);
//                        System.out.println("Email " + emailContact
//                                + " Email Type : " + emailType);

                    }

                    emailCur.close();
                }

                if (image_uri != null) {
//                    System.out.println(Uri.parse(image_uri));
                    try {
                        bitmap = MediaStore.Images.Media
                                .getBitmap(this.getContentResolver(),
                                        Uri.parse(image_uri));
                        sb.append("\n Image in Bitmap:" + bitmap);
//                        System.out.println(bitmap);

                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }


                sb.append("\n........................................");
            }


//            textDetail.setText(sb);
        }
//        else Utils.showDialogOpps(PhoneContactsActivity.this, "No Contacts Found");
        Set set = new TreeSet((o1, o2) -> {

            PhoneNumberUtil pnu = PhoneNumberUtil.getInstance();
            PhoneNumberUtil.MatchType mt = pnu.isNumberMatch(((PhoneContact) o1).MobileNumber, ((PhoneContact) o2).MobileNumber);
            if (mt == PhoneNumberUtil.MatchType.NSN_MATCH || mt == PhoneNumberUtil.MatchType.EXACT_MATCH) {
                return 0;
            }
            return 1;
        });
        set.addAll(contacts);
//        Log.e("FALSE CON", contacts.size() + "before");
        contacts = new ArrayList<>(set);
//        Log.e("FALSE CON", contacts.size() + "after");

    }


    private void requestReadPhoneContactsPermission() {
        Log.i(TAG, "read phone state permission has NOT been granted. Requesting permission.");

        // BEGIN_INCLUDE(phone_state_permission)
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_CONTACTS)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Log.i(TAG,
                    "Displaying phone contacts permission rationale to provide additional context.");
            Snackbar.make(mLayout, R.string.permission_phone_contacts_rationale,
                    Snackbar.LENGTH_LONG)
                    .setAction(R.string.ok, view -> ActivityCompat.requestPermissions(PhoneContactsActivity.this,
                            new String[]{Manifest.permission.READ_CONTACTS},
                            READ_PHONE_CONTACTS))
                    .show();
        } else {

            // phone state permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS},
                    READ_PHONE_CONTACTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == READ_PHONE_CONTACTS) {
            Log.i(TAG, "Received response for contacts permissions request.");
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // do your work here
                Snackbar.make(mLayout, R.string.permission_available_contacs,
                        Snackbar.LENGTH_SHORT)
                        .show();
                new GetPhoneContacts(this).execute();
            } else if (Build.VERSION.SDK_INT >= 23 && !shouldShowRequestPermissionRationale(permissions[0])) {

                Utils.showDialogPermission(PhoneContactsActivity.this, "Go to App Settings and Grant the Contacts  permission to use this feature.");
                // User selected the Never Ask Again Option
            } else {
                Snackbar.make(mLayout, R.string.permissions_not_granted,
                        Snackbar.LENGTH_SHORT)
                        .show();
                this.finish();
            }


        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
