package com.jerryjin.swipeexitlayout;

import android.os.Bundle;

import com.jerryjin.lib.SwipeExitActivity;
import com.jerryjin.lib.ui.SwipeExitLayout;

public class MainActivity extends SwipeExitActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        root.setOnExitListener(new SwipeExitLayout.OnExitListenerImpl() {
            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onExit(int backgroundColor) {
                super.onExit(backgroundColor);
            }

            @Override
            public void onPreFinish() {
                super.onPreFinish();
            }

            @Override
            public void onRestore() {
                super.onRestore();
            }
        });
    }
}
