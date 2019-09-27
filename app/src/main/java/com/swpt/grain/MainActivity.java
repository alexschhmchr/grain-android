package com.swpt.grain;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCamera2View;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.FileInputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private final static int CAMERA_REQUEST_CODE = 0;
    private RangeSeekBar seekBar1;
    private RangeSeekBar seekBar2;
    private RangeSeekBar seekBar3;
    private SeekBar weightSeekBar;
    private TextView objsTextView;
    private HSVDetector hsvDetector;
    private HOGDetector hogDetector;
    private BOWDetector bowDetector;
    private boolean threshold = false;
    private boolean detect = false;
    private boolean blocking = false;
    private Handler handler = Handler.createAsync(Looper.getMainLooper());

    private enum ObjectDetector {
        HSV, HOG, BOW
    }

    private ObjectDetector currentDetector = ObjectDetector.BOW;

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
        MaterialButton rangeButton = findViewById(R.id.rangeButton);
        rangeButton.setOnClickListener(v -> threshold = !threshold);
        MaterialButton detectButton = findViewById(R.id.detectButton);
        detectButton.setOnClickListener(v -> detect = !detect);
        objsTextView = findViewById(R.id.objsTextView);
        seekBar1 = findViewById(R.id.seekBar);
        seekBar2 = findViewById(R.id.seekBar2);
        seekBar3 = findViewById(R.id.seekBar3);
        weightSeekBar = findViewById(R.id.weightSeekBar);
        seekBar1.setValue(lowerB1, higherB1);
        seekBar2.setValue(lowerB2, higherB2);
        seekBar3.setValue(lowerB3, higherB3);
        TextView lb1TextView = findViewById(R.id.lb1TextView);
        TextView lb2TextView = findViewById(R.id.lb2TextView);
        TextView lb3TextView = findViewById(R.id.lb3TextView);
        TextView ub1TextView = findViewById(R.id.ub1TextView);
        TextView ub2TextView = findViewById(R.id.ub2TextView);
        TextView ub3TextView = findViewById(R.id.ub3TextView);
        TextView weightTextView = findViewById(R.id.weightTextView);
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
        weightSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float progressF = progress/10f;
                hogDetector.setWeightTreshold(progressF);
                weightTextView.setText(Float.toString(progressF));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        OpenCVLoader.initDebug();
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            String[] perms = {Manifest.permission.CAMERA};
            ActivityCompat.requestPermissions(this, perms, CAMERA_REQUEST_CODE);
        } else {

        }

        initModels();
        hsvDetector = new HSVDetector();
        hogDetector = new HOGDetector(0.7, getFilesDir().getAbsolutePath());
        bowDetector = new BOWDetector(null, getFilesDir().getAbsolutePath() + "/matcher.yml", null);
        initCamera();
        System.out.println("ready");
    }

    private void initModels() {
        ModelLoader modelLoader = new ModelLoader(getAssets());
        deleteFile("hog.yml");
        deleteFile("matcher.yml");
        try {
            modelLoader.saveModelToStorage("hog.yml", getFilesDir().getAbsolutePath());
            modelLoader.saveModelToStorage("matcher.yml", getFilesDir().getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(String file : fileList()) {
            System.out.println(file);
        }
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
                if(!blocking && detect) {
                    blocking = true;
                    detectGrain(frame);
                    blocking = false;
                }
                if(!threshold) {
                    Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2RGB);
                }
                return frame;
            }
        });
        cView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);
        cView.enableView();
        cView.setMaxFrameSize(1280, 720);
    }

    private void detectGrain(Mat frame) {
        int objects = 0;
        switch(currentDetector) {
            case HSV:
                objects = hsvDetector.detectObject(frame, detect);
                break;
            case HOG:
                objects = hogDetector.detectObject(frame);
                break;
            case BOW:
                break;
        }
        int n = objects;
        handler.post(() -> objsTextView.setText(Integer.toString(n)));
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
