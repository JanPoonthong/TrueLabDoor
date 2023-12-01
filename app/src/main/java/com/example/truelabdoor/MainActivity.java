package com.example.truelabdoor;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.cardlan.twoshowinonescreen.CardLanStandardBus;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    CardLanStandardBus mCardLanDevCtrl = new CardLanStandardBus();

}