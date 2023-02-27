package com.example.saveimage;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText textInputTextFullname,textInputTextUsername, textInputTextEmail, textInputTextPassword;
    private TextView txtSignUp;
    private Button btnSignUp;
    private String fullname, username, email, password;
    private String URL ="http://192.168.2.67/LoginRegister/register.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        textInputTextFullname = findViewById(R.id.filename);
        textInputTextUsername = findViewById(R.id.username);
        textInputTextEmail = findViewById(R.id.email);
        textInputTextPassword = findViewById(R.id.password);
        txtSignUp = findViewById(R.id.textSignUp);
        btnSignUp = findViewById(R.id.buttonSignUp);
        fullname = username = email = password = "";
    }

    public void save(View view) {
        fullname = textInputTextFullname.getText().toString().trim();
        username = textInputTextUsername.getText().toString().trim();
        email = textInputTextEmail.getText().toString().trim();
        password = textInputTextPassword.getText().toString().trim();
        if(!fullname.equals("") && !username.equals("") && !email.equals("") && !password.equals("")) {
            StringRequest stringRequest1 = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response1) {
                    if (response1.equals("success")) {
                        txtSignUp.setText("Зареєстровано");
                        btnSignUp.setClickable(false);
                    } else if (response1.equals("failure")) {
                        Toast.makeText(SignUpActivity.this, "Invalid Username/Password", Toast.LENGTH_SHORT).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error1) {
                    Toast.makeText(getApplicationContext(), error1.toString().trim(), Toast.LENGTH_SHORT).show();
                }
            }){
                @Nullable
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> data = new HashMap<>();
                    data.put("fullname", fullname);
                    data.put("username", username);
                    data.put("email", email);
                    data.put("password", password);
                    return data;
                }
            };
            RequestQueue requestQueue1 = Volley.newRequestQueue(getApplicationContext());
            requestQueue1.add(stringRequest1);
        }
    }

    public void login(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}