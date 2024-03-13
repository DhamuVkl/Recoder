package com.example.recoder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private MediaRecorder mediaRecorder;
    private boolean isRecording = false;

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnRecord = findViewById(R.id.btnRecord);
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecordClick();
            }
        });

        // Request audio recording permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    private void onRecordClick() {
        if (!isRecording) {
            startMediaRecorder();
        } else {
            stopMediaRecorder();
        }
    }

    private void startMediaRecorder() {
        if (mediaRecorder == null) {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            try {
                // Create a new file descriptor
                ParcelFileDescriptor parcelFileDescriptor = createAudioFileDescriptor();

                // Set the output file using the file descriptor
                if (parcelFileDescriptor != null) {
                    mediaRecorder.setOutputFile(parcelFileDescriptor.getFileDescriptor(), 0, 0);
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                    isRecording = true;
                    Button btnRecord = findViewById(R.id.btnRecord);
                    btnRecord.setText("Stop Recording");
                    Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Recording failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private ParcelFileDescriptor createAudioFileDescriptor() {
        ContentResolver contentResolver = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.DISPLAY_NAME, "audio_record_" + System.currentTimeMillis() + ".3gp");
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/3gpp");
        values.put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/Recordings");

        Uri audioUri = contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
        ParcelFileDescriptor parcelFileDescriptor = null;

        try {
            if (audioUri != null) {
                parcelFileDescriptor = contentResolver.openFileDescriptor(audioUri, "w");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return parcelFileDescriptor;
    }

    private void stopMediaRecorder() {
        if (isRecording) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;
            Button btnRecord = findViewById(R.id.btnRecord);
            btnRecord.setText("Start Recording");
            Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
