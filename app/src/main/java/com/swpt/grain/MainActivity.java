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

import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCamera2View;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class MainActivity extends CameraActivity {
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
    private JavaCamera2View cView;

    private enum ObjectDetector {
        HSV, HOG, BOW, None
    }

    private ObjectDetector currentDetector = ObjectDetector.None;

    float lowerB1 = 0;
    float higherB1 = 255;
    float lowerB2 = 0;
    float higherB2 = 255;
    float lowerB3 = 0;
    float higherB3 = 255;

    private MaterialButton hogButton;
    private MaterialButton dbscanButton;
    private MaterialButton bowButton;
    private MaterialButton rangeButton;
    private MaterialButton detectButton;
    private TextView lb1TextView;
    private TextView lb2TextView;
    private TextView lb3TextView;
    private TextView ub1TextView;
    private TextView ub2TextView;
    private TextView ub3TextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rangeButton = findViewById(R.id.rangeButton);
        rangeButton.setOnClickListener(v -> threshold = !threshold);
        detectButton = findViewById(R.id.detectButton);
        detectButton.setOnClickListener(v -> detect = !detect);
        hogButton = findViewById(R.id.hogButton);
        dbscanButton = findViewById(R.id.dbscanButton);
        bowButton = findViewById(R.id.bowButton);
        hogButton.setOnClickListener(this::onClick);
        dbscanButton.setOnClickListener(this::onClick);
        bowButton.setOnClickListener(this::onClick);
        objsTextView = findViewById(R.id.objsTextView);
        seekBar1 = findViewById(R.id.seekBar);
        seekBar2 = findViewById(R.id.seekBar2);
        seekBar3 = findViewById(R.id.seekBar3);
        seekBar1.setValue(lowerB1, higherB1);
        seekBar2.setValue(lowerB2, higherB2);
        seekBar3.setValue(lowerB3, higherB3);
        lb1TextView = findViewById(R.id.lb1TextView);
        lb2TextView = findViewById(R.id.lb2TextView);
        lb3TextView = findViewById(R.id.lb3TextView);
        ub1TextView = findViewById(R.id.ub1TextView);
        ub2TextView = findViewById(R.id.ub2TextView);
        ub3TextView = findViewById(R.id.ub3TextView);
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
        /*weightSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
        });*/
        OpenCVLoader.initDebug();
        /*if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            String[] perms = {Manifest.permission.CAMERA};
            ActivityCompat.requestPermissions(this, perms, CAMERA_REQUEST_CODE);
        } else {

        }*/

        initModels();
        hsvDetector = new HSVDetector();
        hogDetector = new HOGDetector(getFilesDir().getAbsolutePath(), 32, 32);
        bowDetector = new BOWDetector(getFilesDir().getAbsolutePath(), 128, 128);
        initCamera();
        System.out.println("ready");
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(cView);
    }

    private void onClick(View v) {
        rangeButton.setVisibility(MaterialButton.INVISIBLE);
        detectButton.setVisibility(MaterialButton.VISIBLE);

        seekBar1.setVisibility(RangeSeekBar.INVISIBLE);
        seekBar2.setVisibility(RangeSeekBar.INVISIBLE);
        seekBar3.setVisibility(RangeSeekBar.INVISIBLE);
        lb1TextView.setVisibility(TextView.INVISIBLE);
        lb2TextView.setVisibility(TextView.INVISIBLE);
        lb3TextView.setVisibility(TextView.INVISIBLE);
        ub1TextView.setVisibility(TextView.INVISIBLE);
        ub2TextView.setVisibility(TextView.INVISIBLE);
        ub3TextView.setVisibility(TextView.INVISIBLE);



        hogButton.setEnabled(true);
        dbscanButton.setEnabled(true);
        bowButton.setEnabled(true);
        if(v == hogButton) {
            currentDetector = ObjectDetector.HOG;
            v.setEnabled(false);
        } else if(v == dbscanButton) {
            currentDetector = ObjectDetector.HSV;
            v.setEnabled(false);
            rangeButton.setVisibility(MaterialButton.VISIBLE);
            seekBar1.setVisibility(RangeSeekBar.VISIBLE);
            seekBar2.setVisibility(RangeSeekBar.VISIBLE);
            seekBar3.setVisibility(RangeSeekBar.VISIBLE);
            lb1TextView.setVisibility(TextView.VISIBLE);
            lb2TextView.setVisibility(TextView.VISIBLE);
            lb3TextView.setVisibility(TextView.VISIBLE);
            ub1TextView.setVisibility(TextView.VISIBLE);
            ub2TextView.setVisibility(TextView.VISIBLE);
            ub3TextView.setVisibility(TextView.VISIBLE);
        } else if(v == bowButton) {
            currentDetector = ObjectDetector.BOW;
            v.setEnabled(false);
        }
    }

    private void initModels() {
        ModelLoader modelLoader = new ModelLoader(getAssets());
        deleteFile("hog.yml");
        deleteFile("matcher.yml");
        deleteFile("hog_all_params");
        deleteFile("hog_all_svm");
        deleteFile("bow_params");
        deleteFile("bow_svm");
        deleteFile("matcher.yml");
        try {
            modelLoader.saveModelToStorage("hog.yml", getFilesDir().getAbsolutePath());
            modelLoader.saveModelToStorage("matcher.yml", getFilesDir().getAbsolutePath());
            modelLoader.saveModelToStorage("hog_all_params", getFilesDir().getAbsolutePath());
            modelLoader.saveModelToStorage("hog_all_svm", getFilesDir().getAbsolutePath());
            modelLoader.saveModelToStorage("bow_params", getFilesDir().getAbsolutePath());
            modelLoader.saveModelToStorage("bow_svm", getFilesDir().getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(String file : fileList()) {
            System.out.println(file);
        }
    }

    private void initCamera() {
        cView = findViewById(R.id.cView);
        cView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        cView.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height) { }

            @Override
            public void onCameraViewStopped() { }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
                Mat frame = inputFrame.rgba();
                Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2RGB);
                if(!blocking && detect) {
                    blocking = true;
                    detectGrain(frame);
                    blocking = false;
                }
                /*if(!threshold) {
                    Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2RGB);
                }*/
                return frame;
            }
        });
        cView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);
        cView.enableView();
        cView.setMaxFrameSize(1920, 1080);
        System.out.println("init");
    }



    private void detectGrain(Mat frame) {
        int objects = 0;
        switch(currentDetector) {
            case HSV:
                objects = hsvDetector.detectObject(frame, threshold);
                break;
            case HOG:
                objects = hogDetector.detect(frame);
                break;
            case BOW:
                objects = bowDetector.detect(frame);
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
