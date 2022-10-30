package com.sandipbhattacharya.registerlogindemo.mlkit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.sandipbhattacharya.registerlogindemo.R;
import com.sandipbhattacharya.registerlogindemo.Rank;
import com.sandipbhattacharya.registerlogindemo.ShowScore;

public class GameModeSelect extends AppCompatActivity {

    Button gamemode1,gamemode2;
    String gamemode="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_mode_select);

        gamemode1 = findViewById(R.id.button_mode1);
        gamemode2 = findViewById(R.id.button_mode2);


    }

    public void Score_onClick(View v){
        switch (v.getId()) {
            case R.id.button_mode1://遊玩拍照模式
                gamemode="mode1";
                Intent intent1 = new Intent(this, ClientConnectandWait.class);
                intent1.putExtra("Gamemode",gamemode);
                startActivity(intent1);
                finish();
                break;
            case R.id.button_mode2://遊玩即時偵測模式
                gamemode="mode2";
                Intent intent2 = new Intent(this, ClientConnectandWait.class);
                intent2.putExtra("Gamemode",gamemode);
                startActivity(intent2);
                finish();
                break;
        }
    }
}