package com.example.sampletwistontime;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void createTimer(View view) {
        String json = "";
        EditText name = findViewById(R.id.nameEdit);
        EditText duration = findViewById(R.id.durationEdit);
        try {
            final JSONObject object = new JSONObject();

            object.put("TimerName", name.getText().toString());
            object.put("Duration", Integer.parseInt(duration.getText().toString()));
            object.put("Repeat", "Never");
            object.put("Notification", "AtTimeOfEvent");
            object.put("Sound", "Chime");

            json = object.toString();
            try {
                FileOutputStream fileout=openFileOutput("timersettings.txt", MODE_PRIVATE);
                OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
                outputWriter.write(json);
                outputWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (JSONException e) {
            Log.e("tag", "Failed to create JSONObject",e);
        }
        Intent intent = new Intent(MainActivity.this, TimerActivity.class);
        startActivity(intent);
    }
}