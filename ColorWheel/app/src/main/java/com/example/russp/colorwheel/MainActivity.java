package com.example.russp.colorwheel;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements ColorWheelDialog.OnColorChangedListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button)findViewById(R.id.color_wheel_button) ;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ColorWheelDialog(MainActivity.this, MainActivity.this, Color.WHITE).show();
            }
        });

    }

    @Override
    public void colorChanged(int color) {

    }
}
