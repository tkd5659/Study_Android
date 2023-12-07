package com.example.test;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class cctvActivity extends AppCompatActivity {
    private WebView webView;
    private WebSettings webSettings;
    private Button diaryButton, btnBack, cap;
    private static final int REQUEST_WRITE_STORAGE = 112;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cctv);

        webView = findViewById(R.id.cctvWeb);
        diaryButton = findViewById(R.id.diaryButton);
        btnBack = findViewById(R.id.btnBack);
        cap = findViewById(R.id.cap);

        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(true);  // 줌 설정 여부
        webView.getSettings().setBuiltInZoomControls(true);  // 줌 확대/축소 버튼 여부
        webView.loadUrl("http://192.168.0.92:8004/index.html");

        diaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), diaryActivity.class);
                startActivity(intent);
            }
        });


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        cap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureWebView();
            }
        });
    }

    public void captureWebView() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // 권한이 이미 허용된 경우 WebView 캡쳐 진행
            captureAndSave();
        } else {
            // 권한이 없는 경우 권한 요청
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
        }
    }

    private void captureAndSave() {
        webView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        webView.layout(0, 0, webView.getMeasuredWidth(), webView.getMeasuredHeight());
        webView.setDrawingCacheEnabled(true);
        webView.buildDrawingCache(true);

        Bitmap bitmap = Bitmap.createBitmap(webView.getWidth(), webView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        webView.draw(canvas);

        // 현재 날짜와 시간을 포함한 파일 이름 생성
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd hhmmss", Locale.getDefault());
        String currentDateAndTime = sdf.format(new Date());
        String fileName = "capture" + currentDateAndTime + ".png";

        saveBitmap(bitmap, fileName);
        Toast.makeText(cctvActivity.this, "캡쳐 성공", Toast.LENGTH_SHORT).show();
    }


    private void saveBitmap(Bitmap bitmap, String fileName) {
        String sdCardDirectory = Environment.getExternalStorageDirectory().toString();
        File picturesDirectory = new File(sdCardDirectory + "/Pictures");

        // Pictures 폴더가 없다면 생성
        if (!picturesDirectory.exists()) {
            picturesDirectory.mkdirs();
        }

        File file = new File(picturesDirectory, fileName);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            Toast.makeText(cctvActivity.this, "캡쳐 성공", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(cctvActivity.this, "캡쳐 저장 중 오류 발생", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 허용된 경우 WebView 캡쳐 진행
                captureAndSave();
            } else {
                Toast.makeText(cctvActivity.this, "저장소 쓰기 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
