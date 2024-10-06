package com.example.footset;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor stepDetectorSensor;
    private boolean isSensorPresent = false;
    private int stepCount = 0;
    private final double actualDistance = 10.0; // 실제 이동 거리 10m로 고정
    private TextView kValueTextView; // K 값을 표시할 TextView
    private TextView stepCountTextView; // 걸음 수를 표시할 TextView
    private TextView iValueTextView; // 예상 걸음 수를 표시할 TextVieww

    private static final int ACTIVITY_RECOGNITION_REQUEST_CODE = 100; // 권한 요청 코드

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // 레이아웃 파일 설정

        kValueTextView = findViewById(R.id.kValueTextView); // K 값 TextView 연결
        stepCountTextView = findViewById(R.id.stepCountTextView); // 걸음 수 TextView 연결
        iValueTextView = findViewById(R.id.iValueTextView); // 예상 걸음 수 TextView 연결

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // 권한 확인 및 요청
        requestPermissions();
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {
                // 권한이 없으면 요청
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACTIVITY_RECOGNITION},
                        ACTIVITY_RECOGNITION_REQUEST_CODE);
            } else {
                // 권한이 이미 있을 때 센서 설정
                setupStepDetectorSensor();
            }
        } else {
            // Android Q 이하에서는 권한 없이 센서 설정
            setupStepDetectorSensor();
        }
    }

    private void setupStepDetectorSensor() {
        // Step Detector 센서가 있는지 확인
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
            stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            isSensorPresent = true;
            // 센서 리스너 등록
            sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Toast.makeText(this, "Step Detector Sensor not available!", Toast.LENGTH_SHORT).show();
            isSensorPresent = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 센서 리스너 등록
        if (isSensorPresent) {
            sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 센서 리스너 해제
        if (isSensorPresent) {
            sensorManager.unregisterListener(this, stepDetectorSensor);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == stepDetectorSensor) {
            // 걸음 수 증가
            stepCount++;

            // K 값 계산 (걸음 보폭)
            double kValue = actualDistance / stepCount;  // actualDistance는 10m로 고정됨

            // 계산된 K 값 로그 출력
            Log.d("DEBUG", "K 값 (걸음 보폭): " + kValue);

            // K 값이 0보다 커야 제대로 나누기가 가능
            if (kValue > 0) {
                // I 값 계산 (앞으로 100m를 걷기 위해 필요한 예상 걸음 수)
                double iValue = 100.0 / kValue;

                // 계산된 I 값 로그 출력
                Log.d("DEBUG", "I 값 (100m 예상 걸음 수): " + iValue);

                // UI 업데이트
                kValueTextView.setText(String.format("K (걸음 보폭): %.2f m", kValue));
                stepCountTextView.setText(String.format("걸음 수: %d", stepCount));
                iValueTextView.setText(String.format("I (100m 예상 걸음 수): %.2f", iValue));
            } else {
                // K 값이 0이면 계산하지 않음
                iValueTextView.setText("I (100m 예상 걸음 수): 계산 불가");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 이 예제에서는 사용하지 않음
    }

    // 권한 요청 결과 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == ACTIVITY_RECOGNITION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 승인되면 센서를 설정
                setupStepDetectorSensor();
            } else {
                // 권한이 거부된 경우 처리
                Toast.makeText(this, "ACTIVITY_RECOGNITION 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
