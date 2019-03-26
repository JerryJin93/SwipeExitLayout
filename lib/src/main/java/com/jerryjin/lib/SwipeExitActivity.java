package com.jerryjin.lib;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import com.jerryjin.lib.ui.SwipeExitLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Author: Jerry
 * Generated at: 2019/3/18 18:18
 * GitHub: https://github.com/JerryJin93
 * Blog:
 * WeChat: enGrave93
 * Version: 1.0.2
 * Description:
 */
@SuppressLint("Registered")
public abstract class SwipeExitActivity extends AppCompatActivity {

    protected SwipeExitLayout root;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = (SwipeExitLayout) LayoutInflater.from(this).inflate(R.layout.swipe_exit_container, null);
        root.attachTo(this);
    }

}
