package com.sandipbhattacharya.registerlogindemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sandipbhattacharya.registerlogindemo.loginsystem.MainActivity;
import com.sandipbhattacharya.registerlogindemo.mlkit.CameraXLivePreviewActivity;
import com.sandipbhattacharya.registerlogindemo.opencv.FaceDetect;

public class ShowScore extends AppCompatActivity {
    TextView BattleResult, UserScore, OppsiteScore, UserName, OppsiteName;
    Button BtnSeeRank, BtnRetry, BtnLeave;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_score);

        BattleResult = findViewById(R.id.tv_battle_result);
        UserScore = findViewById(R.id.tv_user_score);
        OppsiteScore = findViewById(R.id.tv_oppsite_score);
        UserName = findViewById(R.id.tv_user_name);
        OppsiteName = findViewById(R.id.tv_oppsite_name);
        BtnSeeRank = findViewById(R.id.btn_see_rank);
        BtnRetry = findViewById(R.id.btn_retry);
        BtnLeave = findViewById(R.id.btn_leave);

        Intent intent = getIntent();
        int opposite_score = intent.getIntExtra("OppositeScore", 0);
        int user_score = intent.getIntExtra("UserScore",0);
        String opposite_name = intent.getStringExtra("OppositeName");

        if(user_score>opposite_score){
            BattleResult.setText("勝利!");
            BattleResult.setTextColor(Color.GREEN);
        }else if(user_score<opposite_score){
            BattleResult.setText("戰敗...");
            BattleResult.setTextColor(Color.RED);
        }
        UserScore.setText(String.valueOf(user_score));
        OppsiteScore.setText(String.valueOf(opposite_score));
        UserName.setText(MainActivity.Main_name);
        OppsiteName.setText(opposite_name);
    }

    public void Score_onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_see_rank://點擊查詢排行榜
                Intent intent1 = new Intent(ShowScore.this, Rank.class);
                startActivity(intent1);
                break;
            case R.id.btn_retry:
                Intent intent2 = new Intent(ShowScore.this, CameraXLivePreviewActivity.class);
                startActivity(intent2);
                finish();
                break;
            case R.id.btn_leave:
                finishAffinity();
                break;
        }
    }
}