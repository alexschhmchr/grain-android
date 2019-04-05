package com.swpt.grain;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCamera2View;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity {
    private final static int CAMERA_REQUEST_CODE = 0;
    private RangeSeekBar seekBar1;
    private RangeSeekBar seekBar2;
    private RangeSeekBar seekBar3;
    private HSVDetector hsvDetector;
    private boolean detecting = false;

    float lowerB1 = 0;
    float higherB1 = 255;
    float lowerB2 = 0;
    float higherB2 = 255;
    float lowerB3 = 0;
    float higherB3 = 255;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MaterialButton button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detecting = true;
            }
        });
        hsvDetector = new HSVDetector();
        seekBar1 = findViewById(R.id.seekBar);
        seekBar2 = findViewById(R.id.seekBar2);
        seekBar3 = findViewById(R.id.seekBar3);
        seekBar1.setValue(lowerB1, higherB1);
        seekBar2.setValue(lowerB2, higherB2);
        seekBar3.setValue(lowerB3, higherB3);
        TextView lb1TextView = findViewById(R.id.lb1TextView);
        TextView lb2TextView = findViewById(R.id.lb2TextView);
        TextView lb3TextView = findViewById(R.id.lb3TextView);
        TextView ub1TextView = findViewById(R.id.ub1TextView);
        TextView ub2TextView = findViewById(R.id.ub2TextView);
        TextView ub3TextView = findViewById(R.id.ub3TextView);
        seekBar1.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
                leftValue = Math.round(leftValue*10)/10f;
                rightValue = Math.round(rightValue*10)/10f;
                hsvDetector.setBound1(leftValue, rightValue);
                lb1TextView.setText(Float.toString(leftValue));
                ub1TextView.setText(Float.toString(rightValue));
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) { }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) { }
        });
        seekBar2.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
                leftValue = Math.round(leftValue*10)/10f;
                rightValue = Math.round(rightValue*10)/10f;
                hsvDetector.setBound2(leftValue, rightValue);
                lb2TextView.setText(Float.toString(leftValue));
                ub2TextView.setText(Float.toString(rightValue));
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) { }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) { }
        });
        seekBar3.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
                leftValue = Math.round(leftValue*10)/10f;
                rightValue = Math.round(rightValue*10)/10f;
                hsvDetector.setBound3(leftValue, rightValue);
                lb3TextView.setText(Float.toString(leftValue));
                ub3TextView.setText(Float.toString(rightValue));
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) { }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) { }
        });

        OpenCVLoader.initDebug();
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            String[] perms = {Manifest.permission.CAMERA};
            ActivityCompat.requestPermissions(this, perms, CAMERA_REQUEST_CODE);
        } else {

        }
        initCamera();
    }

    private void initCamera() {
        JavaCamera2View cView = findViewById(R.id.cView);
        cView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        cView.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height) { }

            @Override
            public void onCameraViewStopped() { }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
                Mat frame = inputFrame.rgba();
                if(detecting) {
                    detectGrain(frame);
                }
                return frame;
            }
        });
        cView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);
        cView.enableView();
        cView.setMaxFrameSize(1920, 1080);cView.setMaxFrameSize(1920, 1080);
    }

    private void detectGrain(Mat frame) {
        hsvDetector.detectColor(frame);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode) {
            case CAMERA_REQUEST_CODE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //initCamera();
                }
        }
    }
}
