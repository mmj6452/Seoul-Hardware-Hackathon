package com.example.aiot;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import org.java_websocket.server.WebSocketServer;
import org.opencv.android.OpenCVLoader;
import org.opencv.objdetect.CascadeClassifier;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (OpenCVLoader.initDebug()) {
            Log.d("OpenCV Log", "OpenCV initialized");
        }


        String host = "192.168.43.108";
        int port = 3000;

        ImageView imageView = findViewById(R.id.imageView);

        new Thread(() -> {
            WebSocketServer server = new SimpleServer(new InetSocketAddress(host, port), this);
            server.run();
        }).start();
    }
}