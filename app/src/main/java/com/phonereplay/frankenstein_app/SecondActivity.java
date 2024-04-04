package com.phonereplay.frankenstein_app;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.phonereplay.tasklogger.PhoneReplayApi;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        Button button = findViewById(R.id.button_start_two);
        Button buttons = findViewById(R.id.button_first_two);
        button.setOnClickListener(v -> PhoneReplayApi.startRecording());

        buttons.setOnClickListener(v -> PhoneReplayApi.stopRecording());
    }
}