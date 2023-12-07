package com.example.test;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CaptureActivity extends AppCompatActivity {
    private WebView webView;
    private WebSettings webSettings;
    private Button BackD, capD;
    private static final int REQUEST_WRITE_STORAGE1 = 112;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        webView=findViewById(R.id.cctvWebD);
        BackD=findViewById(R.id.BackD);
        capD=findViewById(R.id.capD);

        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(true);  // 줌 설정 여부
        webView.getSettings().setBuiltInZoomControls(true);  // 줌 확대/축소 버튼 여부
        webView.loadUrl("http://192.168.0.92:8004/index.html");

        BackD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        capD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                capture();
            }
        });

    }

//    public void captureWebView() {
//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//            // 권한이 이미 허용된 경우 WebView 캡쳐 진행
//            capture();
//        } else {
//            // 권한이 없는 경우 권한 요청
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE1);
//        }
//    }

    private void capture() {
        webView.setDrawingCacheEnabled(true);
        webView.buildDrawingCache();

        Bitmap webViewBitmap = Bitmap.createBitmap(webView.getMeasuredWidth(),
                webView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(webViewBitmap);
        webView.draw(canvas);

        // 현재 날짜와 시간을 포함한 파일 이름 생성
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd hhmmss", Locale.getDefault());
        String currentDateAndTime = sdf.format(new Date());
        String fileName = "capture" + currentDateAndTime + ".png";

        String derectory= saveBitmap(webViewBitmap, fileName);
        if(!derectory.equals("/Pictures")){
            String imgpath=derectory+"/"+fileName;
            Intent i=new Intent();
            i.putExtra("imgpath",imgpath);
            setResult(RESULT_OK,i);
            Log.d("","사진부르기전"+imgpath);
            finish();
        }

    }

    private String saveBitmap(Bitmap bitmap, String fileName) {
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
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(CaptureActivity.this, "캡쳐 저장 중 오류 발생", Toast.LENGTH_SHORT).show();
        }
        return sdCardDirectory+"/Pictures";
    }


}