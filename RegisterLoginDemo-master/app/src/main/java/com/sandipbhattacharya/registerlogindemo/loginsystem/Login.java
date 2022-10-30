package com.sandipbhattacharya.registerlogindemo.loginsystem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
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

public class Login extends AppCompatActivity {
    private Button btnlogin;
    private EditText etEmail, etPassword;
    private String email, password;
    private String URL = "http://10.0.2.2/login/login.php";//!!!把這個ip改成你所在用的ip!!!
    String result;
    static public String log_username = "",msg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        email = password = "";
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnlogin = findViewById(R.id.btn_login);
//        btnlogin.setOnClickListener(new View.OnClickListener() {
//            @Override
//            // 按鈕事件
//            public void onClick(View view) {
//                // 按下之後會執行的程式碼
//                // 宣告執行緒
//                Thread thread = new Thread(mutiThread);
//                thread.start(); // 開始執行
//            }
//        });
    }

    public void login(View view) {
        email = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        if(!email.equals("") && !password.equals("")){
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (response.equals("failure")) {
                        Toast.makeText(Login.this, "無效的名稱或密碼", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        log_username = response.substring(12);
                        Log.d("res", response);
                        Log.d("res", log_username);
                        Intent intent = new Intent(Login.this, popcat_MainActivity.class);
                        Toast.makeText(Login.this, "登入成功", Toast.LENGTH_SHORT).show();
                        startActivity(intent);
                        finish();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(Login.this, error.toString().trim(), Toast.LENGTH_SHORT).show();
                }
            }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> data = new HashMap<>();
                    data.put("email", email);
                    data.put("password", password);
                    return data;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(stringRequest);
        }else{
            Toast.makeText(this, "Fields can not be empty!", Toast.LENGTH_SHORT).show();
        }
    }
    public void register(View view) {
        Intent intent = new Intent(this, Register.class);
        startActivity(intent);
        finish();
    }
}