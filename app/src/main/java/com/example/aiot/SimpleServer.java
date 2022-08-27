package com.example.aiot;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.opencv.android.Utils;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

public class SimpleServer extends WebSocketServer {
    private ImageView imageView;
    private Context context;

    private CascadeClassifier faceDetector;

    public SimpleServer(InetSocketAddress address, Context context) {
        super(address);
        this.context = context;
        imageView = ((Activity) context).findViewById(R.id.imageView);

        setCascadeClassifier();
    }

    private void setCascadeClassifier() {
        try {
            InputStream is = context.getResources().openRawResource(R.raw.haarcascade_frontalface_alt2);
            File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
            File caseFile = new File(cascadeDir, "caseFile.xml");

            FileOutputStream fos = new FileOutputStream(caseFile);

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            is.close();
            fos.close();

            faceDetector = new CascadeClassifier(caseFile.getAbsolutePath());
            if (faceDetector.empty()) {
                faceDetector = null;
            } else {
                cascadeDir.delete();
            }
        } catch (IOException e) {
            System.out.println("setCascadeClassifier failed.");
        }
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        conn.send("Welcome to the server!"); //This method sends a message to the new client
        broadcast("new connection: " + handshake.getResourceDescriptor()); //This method sends a message to all clients connected
        System.out.println("new connection to " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("received message from " + conn.getRemoteSocketAddress() + ": " + message);
    }

    private Bitmap detectFace(Bitmap bitmap) {
        Mat img = new Mat();
        Utils.bitmapToMat(bitmap, img);

        Mat gray = new Mat();
        Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);

        MatOfRect faces = new MatOfRect();
        faceDetector.detectMultiScale(gray, faces, 1.1, 2, 0, new Size(10, 10));

        for (Rect face: faces.toArray()) {
            Imgproc.rectangle(img, face, new Scalar(255, 0, 0));
        }

        Utils.matToBitmap(img, bitmap);

        return bitmap;
    }

    private Bitmap detectFace(ByteBuffer byteBuffer) {
        Mat img = new Mat(new Size(480, 320), CvType.CV_8UC4);
        img.put(0,0,byteBuffer.array());

//        Mat img = Imgcodecs.imdecode()

        Mat gray = new Mat();
        Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);

        MatOfRect faces = new MatOfRect();
        faceDetector.detectMultiScale(gray, faces, 1.1, 2, 0, new Size(10, 10));

        for (Rect face: faces.toArray()) {
            Imgproc.rectangle(img, face, new Scalar(255, 0, 0));
        }
        
        Bitmap bitmap = Bitmap.createBitmap(480, 320, Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img, bitmap);

        return bitmap;
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        System.out.println("received ByteBuffer from " + conn.getRemoteSocketAddress());
        Bitmap bitmap = BitmapFactory.decodeByteArray(message.array(), 0, message.array().length);
        imageView.setImageBitmap(detectFace(bitmap));
//        imageView.setImageBitmap(detectFace(message));
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("an error occurred on connection " + conn.getRemoteSocketAddress() + ":" + ex);
    }

    @Override
    public void onStart() {
        System.out.println("server started successfully");
    }
}