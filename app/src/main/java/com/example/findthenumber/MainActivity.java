package com.example.findthenumber;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    public final String SHARED_PREFERENCES = "My_SHARED_PREFERENCES";
    public final String SHARED_PREFERENCES_HAS_CONTACTS_KEY = "HAS_CONTACTS_KEY";
    public final String SHARED_PREFERENCES_CALLER_NAME_KEY = "CALLER_NAME_KEY";
    ActivityResultCallback<Boolean> contactsPermissionCallBack = isGranted -> {
        if (isGranted) {
            getCallerName();
        } else {
            requestPermissionWithRationaleCheck();
        }
    };


    private final ActivityResultLauncher<Intent> manuallyPermissionResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    getCallerName();
                }
            });

    ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), contactsPermissionCallBack);


    private MaterialButton allow_contacts_button, search_contact_button, start_button;
    private LinearLayoutCompat allow_contacts_layout, search_layout, caller_name_layout;
    private SearchView my_search_view;
    private List<ContactRecord> contacts;
    private SharedPreferences sharedPreferences;
    private RecyclerView recycler_view;
    private ProgressBar progressBar;
    private EditText enter_name_edit_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        initViews();
        sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);


        boolean isGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
        if (isGranted)
            getCallerName();
    }

    private void getCallerName() {
        allow_contacts_layout.setVisibility(View.GONE);
        if (sharedPreferences.getString(SHARED_PREFERENCES_CALLER_NAME_KEY, null) == null) {
            caller_name_layout.setVisibility(View.VISIBLE);
        } else {
            getContacts();
        }
    }


    private void initViews() {
        allow_contacts_button.setOnClickListener(v -> requestContacts());
        search_contact_button.setOnClickListener(v -> searchContacts());
        start_button.setOnClickListener(v -> startAfterNameEntered());
        search_layout.setVisibility(View.GONE);
        recycler_view.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        caller_name_layout.setVisibility(View.GONE);

    }

    private void startAfterNameEntered() {
        if (enter_name_edit_text.getText().toString().isEmpty()) {
            enter_name_edit_text.setError("Must enter your name to use the App");
        } else {
            caller_name_layout.setVisibility(View.GONE);
            sharedPreferences.edit().putString(SHARED_PREFERENCES_CALLER_NAME_KEY, enter_name_edit_text.getText().toString()).apply();
            getContacts();
        }

    }

    private void searchContacts() {
        if (!my_search_view.getQuery().toString().isEmpty()) {
            progressBar.setVisibility(View.VISIBLE);
            String phone = my_search_view.getQuery().toString();
            try {
                searchContactByPhone(phone);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void searchContactByPhone(String phone) throws Exception {
        phone = AESUtils.encrypt(phone);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Numbers");
        myRef.child(phone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ContactRecord res = snapshot.getValue(ContactRecord.class);

                if (res != null) {

                    res.getNames().forEach(name -> {
                        try {
                            name.setContactName(AESUtils.decrypt(name.getContactName()));
                            name.setCallerName(AESUtils.decrypt(name.getCallerName()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    });
                    recycler_view.setVisibility(View.VISIBLE);
                    recycler_view.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    recycler_view.setAdapter(new ContactsAdapter(res.getNames()));
                    progressBar.setVisibility(View.GONE);
                    recycler_view.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(MainActivity.this, "Nothing found...", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void requestContacts() {
        requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
    }

    private void findViews() {
        my_search_view = findViewById(R.id.my_search_view);
        allow_contacts_layout = findViewById(R.id.allow_contacts_layout);
        allow_contacts_button = findViewById(R.id.allow_contacts_button);
        search_contact_button = findViewById(R.id.search_contact_button);
        enter_name_edit_text = findViewById(R.id.enter_name_edit_text);
        start_button = findViewById(R.id.start_button);
        caller_name_layout = findViewById(R.id.caller_name_layout);
        search_layout = findViewById(R.id.search_layout);
        recycler_view = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_circular);
    }

    @SuppressLint("Range")
    private void getContacts() {

        if (!sharedPreferences.getBoolean(SHARED_PREFERENCES_HAS_CONTACTS_KEY, false)) {
            getAllContacts();
        } else {
            search_layout.setVisibility(View.VISIBLE);
        }

    }


    private void uploadToServer(List<ContactRecord> contacts) {
        // upload to server and than sharedPreferences.edit().putBoolean(SHARED_PREFERENCES_HAS_CONTACTS_KEY,true).apply();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Numbers");
        contacts.forEach(contactRecord -> myRef.child(contactRecord.getPhone()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ContactRecord res = snapshot.getValue(ContactRecord.class);
                if (res != null) {
                    res.getNames().addAll(contactRecord.getNames());
                    res.setNames(res.getNames().stream().distinct().collect(Collectors.toList()));
                    myRef.child(res.getPhone()).setValue(res);
                } else {
                    myRef.child(contactRecord.getPhone()).setValue(contactRecord);
                }
                sharedPreferences.edit().putBoolean(SHARED_PREFERENCES_HAS_CONTACTS_KEY, true).apply();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }));
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            search_layout.setVisibility(View.VISIBLE);
        });
    }

    @SuppressLint("Range")
    private void getAllContacts() {
        progressBar.setVisibility(View.VISIBLE);
        Executors.newSingleThreadExecutor().execute(() -> {
            List<ContactRecord> nameList = new ArrayList<>();
            ContentResolver cr = getContentResolver();
            Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            if ((cur != null ? cur.getCount() : 0) > 0) {
                while (cur.moveToNext()) {
                    String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {

                        Cursor pCur = cr.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{id}, null);

                        String phoneNo = null;
                        if (pCur.moveToNext()) {
                            phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        }
                        if (phoneNo != null) {
                            ContactRecord contactRecord = null;
                            try {
                                contactRecord = new ContactRecord(getFixedPhoneNumber(phoneNo));
                                contactRecord.getNames().add(new Name(AESUtils.encrypt(sharedPreferences.getString(SHARED_PREFERENCES_CALLER_NAME_KEY, "")), AESUtils.encrypt(name)));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (contactRecord != null)
                                nameList.add(contactRecord);
                        }
                        pCur.close();
                    }
                }
            }
            if (cur != null) {
                cur.close();
            }
            contacts = nameList;
            uploadToServer(contacts);
        });


    }

    private String getFixedPhoneNumber(String phoneNo) throws Exception {
        phoneNo = phoneNo.replace("-", "");
        phoneNo = phoneNo.replace("+972", "0");
        phoneNo = phoneNo.replaceAll("\\s", "");
        phoneNo = phoneNo.replaceAll("[^a-zA-Z0-9]", "");
        return AESUtils.encrypt(phoneNo);
    }


    private void openSettingsManually() {

        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        manuallyPermissionResultLauncher.launch(intent);
    }


    private void requestPermissionWithRationaleCheck() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
            Log.d("pttt", "shouldShowRequestPermissionRationale = true");
            // Show user description for what we need the permission

            String message = "You must allow access to contacts in order to use the application";
            AlertDialog alertDialog =
                    new AlertDialog.Builder(this)
                            .setMessage(message)
                            .setPositiveButton(getString(android.R.string.ok),
                                    (dialog, which) -> {
                                        requestContacts();
                                        dialog.cancel();
                                    })
                            .setNegativeButton("No", (dialog, which) -> {
                                // disabled functions due to denied permissions
                            })
                            .show();
            alertDialog.setCanceledOnTouchOutside(true);
        } else {
            Log.d("pttt", "shouldShowRequestPermissionRationale = false");
            openPermissionSettingDialog();
        }
    }


    private void openPermissionSettingDialog() {
        String message = "You must allow access to contacts in order to use the application, clicking on OK will open the settings window where you can open this permission.";
        AlertDialog alertDialog =
                new AlertDialog.Builder(this)
                        .setMessage(message)
                        .setPositiveButton(getString(android.R.string.ok),
                                (dialog, which) -> {
                                    openSettingsManually();
                                    dialog.cancel();
                                }).show();
        alertDialog.setCanceledOnTouchOutside(true);
    }


}