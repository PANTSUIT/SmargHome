package com.example.smarthome;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


public class StartActivity extends AppCompatActivity {

    private static final String TAG = "StartActivity";
    private Button button_reco;
    private Button button_Start;
    private Button button_Stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Log.i(TAG, "onCreate: ");
        button_reco = findViewById(R.id.button_reconized);
        button_Start = findViewById(R.id.button_start);
        button_Stop = findViewById(R.id.button_stop);

        final Intent intent = new Intent(StartActivity.this , TestService.class);

        button_reco.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "button_reco clicked");
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        button_Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(intent);
            }
        });

        button_Stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(intent);
            }
        });

        //TestService.StartSe
    }
}