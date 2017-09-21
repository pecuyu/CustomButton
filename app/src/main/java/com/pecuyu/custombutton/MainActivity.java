package com.pecuyu.custombutton;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CustomButton customButton = (CustomButton) findViewById(R.id.id_cb);
        customButton.setOnClickStateChangeListener(new CustomButton.OnStateChangeListener() {
            @Override
            public void onStart() {
                Toast.makeText(getApplicationContext(), "onStart", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStop() {
                Toast.makeText(getApplicationContext(), "onStop", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
