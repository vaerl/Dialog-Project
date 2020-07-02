package com.example.multimediapizzaorder;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class OverviewActivity extends AppCompatActivity {

    public static String ORDER = "order";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        TextView overview = findViewById(R.id.overview);
        if(getIntent().hasExtra(ORDER)){
            Order order = (Order) getIntent().getSerializableExtra(ORDER);
            overview.setText(order.toString());
        } else {
            overview.setText(getResources().getText(R.string.error));
        }
    }
}