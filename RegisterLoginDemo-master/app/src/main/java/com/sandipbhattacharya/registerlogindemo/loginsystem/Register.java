package com.sandipbhattacharya.registerlogindemo.loginsystem;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sandipbhattacharya.registerlogindemo.R;
import com.sandipbhattacharya.registerlogindemo.popcat_MainActivity;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    private EditText etName, etEmail, etPassword, etReenterPassword;
    private TextView tvStatus;
    private Button btnRegister;
    //資料庫
    // 預設網址 10.0.2.2
    //手機資料庫 192.168.43.241

    private String URL = "http://10.0.2.2/login/register.php";
    private String name, email, password, reenterPassword;
    static public String reg_username = "";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etReenterPassword = findViewById(R.id.etReenterPassword);
        tvStatus = findViewById(R.id.tvStatus);
        btnRegister = findViewById(R.id.btnRegister);
        name = email = password = reenterPassword = "";
    }

    public void save(View view) {
        name = etName.getText().toString().trim();
        email = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        reenterPassword = etReenterPassword.getText().toString().trim();
        if(!password.equals(reenterPassword)){
            Toast.makeText(this, "密碼不一致", Toast.LENGTH_SHORT).show();
        }
        else if(!name.equals("") && !email.equals("") && !password.equals("")){
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (response.equals("success")) {
                        reg_username = name+"\n";
                        Toast.makeText(Register.this, "註冊成功", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Register.this, popcat_MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else if (response.equals("failure")) {
                        tvStatus.setText("格式錯誤請重新輸入");                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), error.toString().trim(), Toast.LENGTH_SHORT).show();
                }
            }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> data = new HashMap<>();
                    data.put("name", name);
                    data.put("email", email);
                    data.put("password", password);
                    return data;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    10000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(stringRequest);
        } else{
            Toast.makeText(this, "輸入不可以有空白!", Toast.LENGTH_SHORT).show();
        }
    }

    public void login(View view) {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
        finish();
    }
}
