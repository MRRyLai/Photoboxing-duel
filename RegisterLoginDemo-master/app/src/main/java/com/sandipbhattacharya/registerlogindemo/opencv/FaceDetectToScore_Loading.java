package com.sandipbhattacharya.registerlogindemo.opencv;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.sandipbhattacharya.registerlogindemo.R;

public class FaceDetectToScore_Loading extends AppCompatActivity {
    private int count=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detect_to_score_loading);
        ImageView load_iv = findViewById(R.id.loading_iv);
        Intent intent = getIntent();
        count = intent.getIntExtra("opencount", 0);
        Log.d("Loading","count:"+count);
        if(count>=1){
            finish();
        }
    }
}