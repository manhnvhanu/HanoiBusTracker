package com.acruxsolutions.hnbustracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class SelectBusActivity extends AppCompatActivity implements View.OnClickListener{

    Intent i;

    Button busNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_bus);

        busNumber = (Button) findViewById(R.id.busNo);
        assert busNumber != null;
        busNumber.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.busNo:
                i= new Intent(SelectBusActivity.this,ViewBusActivity.class);
                i.putExtra("busNumberId", "02");
                startActivity(i);

                break;

            default:
                break;
        }
    }
}
