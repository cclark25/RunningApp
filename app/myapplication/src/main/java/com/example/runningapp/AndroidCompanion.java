package com.example.runningapp;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.runningapp.databinding.ActivityAndroidCompanionBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;

import java.util.Timer;
import java.util.TimerTask;

public class AndroidCompanion extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityAndroidCompanionBinding binding;

    private ActivityTableManager tableManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAndroidCompanionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_android_companion);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.fab)
                        .setAction("Action", null).show();
            }
        });

        TableLayout o = (TableLayout) findViewById(R.id.tableData);

        this.tableManager = new ActivityTableManager(this, o);
        AndroidCompanion self = this;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println(self.tableManager.count);
            }
        }, 0, 1000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_android_companion, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_android_companion);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 123) {
            if(resultCode == android.app.Activity.RESULT_OK){
                System.out.println("Intent Succeeded: " + data.toUri(0));
                String result=data.getStringExtra("result");
            }
            if (resultCode == android.app.Activity.RESULT_CANCELED) {
                System.out.println("Intent failed");
                // Write your code if there's no result
            }
        }
    }
}