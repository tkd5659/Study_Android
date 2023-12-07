package com.example.test;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    ImageButton cctvButton, diaryButton, ledButton, houseButton;
    TextView humText, tempText, dustText;
    ImageView dustLogo, humLogo, tempLogo;

    private Handler handler = new Handler();
    private Runnable getDataRunnable;
    private boolean isWaterPumpOn = false;
    private boolean isLEDOn = false;
    private boolean waterLevelAlertShown = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cctvButton = findViewById(R.id.cctvButton);
        diaryButton = findViewById(R.id.diaryButton);
        ledButton = findViewById(R.id.ledButton);
        houseButton = findViewById(R.id.houseButton);
        humText = findViewById(R.id.humText);
        tempText = findViewById(R.id.tempText);
        dustText = findViewById(R.id.dustText);
        dustLogo = findViewById(R.id.dustLogo);
        humLogo = findViewById(R.id.humLogo);
        tempLogo = findViewById(R.id.tempLogo);


        cctvButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), cctvActivity.class);
                startActivity(intent);
            }
        });

        diaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), diaryActivity.class);
                startActivity(intent);
            }
        });

        houseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // AsyncTask를 실행하여 워터펌프를 작동시킵니다.
                houseButton.setEnabled(false); // 버튼 비활성화
                new WaterPumpTask().execute();
            }
        });
        ledButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isLEDOn) {
                    new LedOnOff().execute();
                    ledButton.setImageResource(R.drawable.lightoff);
                    isLEDOn = false;
                } else {
                    new LedOnOff().execute();
                    ledButton.setImageResource(R.drawable.lighton);
                    isLEDOn = true;
                }
            }
        });

        // 주기적으로 데이터를 가져오는 Runnable 설정
        getDataRunnable = new Runnable() {
            @Override
            public void run() {
                // AsyncTask를 실행하여 센서 데이터를 가져옵니다.
                new RetrieveSensorData().execute();

                // 주기적으로 실행하려면 다시 호출
                handler.postDelayed(this, 2000); // 5초마다 데이터 갱신 (5000ms = 5초)
            }
        };

        // 초기 데이터 로드
        getDataRunnable.run();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 액티비티가 종료될 때 Runnable 중지
        handler.removeCallbacks(getDataRunnable);
    }

    private class RetrieveSensorData extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();

            // "YOUR_ESP_IP_ADDRESS"를 ESP8266의 실제 IP 주소로 바꿉니다.
            String url = "http://192.168.0.4/";

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonData = new JSONObject(result);
                    double temperature = jsonData.getDouble("temperature");
                    double humidity = jsonData.getDouble("humidity");
                    // 서버에서 전달한 먼지 데이터가 있다고 가정
                    int dust = jsonData.getInt("soil_moisture") * 100 / 1024;
                    int waterLevel = jsonData.getInt("water_level"); // assuming water level information is available

                    // UI 갱신
                    tempText.setText("온도: " + temperature + " °C");
                    humText.setText("습도: " + humidity + " %");
                    dustText.setText("토양수분: " + dust + " %");

                    // 온도 값에 따라 이미지 업데이트
                    updateTemperatureImage(temperature);
                    updateSoilImage(dust);
                    if (waterLevel == 0 && !waterLevelAlertShown) {
                        showWaterLevelAlert();
                        createNotificationChannel();
                        waterLevelAlertShown = true; // 알림을 표시했음을 나타내는 플래그 설정
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        private void updateTemperatureImage(double temperature) {
            ImageView tempLogo = findViewById(R.id.tempLogo);

            if (temperature >= 10 && temperature < 20) {
                tempLogo.setImageResource(R.drawable.tempcool);
            } else if (temperature >= 20) {
                tempLogo.setImageResource(R.drawable.tempred);
            } else {
                // 기본 이미지 유지
                tempLogo.setImageResource(R.drawable.temp);
            }
        }

        private void updateSoilImage(double dust) {
            ImageView dustLogo = findViewById(R.id.dustLogo);

            if (dust >= 60 && dust < 100) {
                dustLogo.setImageResource(R.drawable.soilgreen);
            } else if (dust >= 30 && dust < 60) {
                dustLogo.setImageResource(R.drawable.soilyellow);
            } else if (dust >= 0 && dust < 30) {
                dustLogo.setImageResource(R.drawable.soilred);
            } else {
                // 기본 이미지 유지
                dustLogo.setImageResource(R.drawable.soil);
            }
        }
    }

    private class WaterPumpTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();

            // 현재 워터펌프 상태에 따라 URL 설정
            String url = isWaterPumpOn ? "http://192.168.0.4/waterpump/off" : "http://192.168.0.4/waterpump/on";

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String result = response.body().string();
                // 결과를 처리하는 코드를 추가하세요.
                Log.d("WaterPumpTask", "Result: " + result);

                // 켜진 상태면 3초 후에 끄는 동작 수행
                if (isWaterPumpOn) {
                    Thread.sleep(3000);
                }

                return result;
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        }

        // onPostExecute 메서드는 그대로 둡니다.
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                // 결과를 처리하는 코드를 추가하세요.
                Log.d("WaterPumpTask", "Result: " + result);

                // 작업이 완료되면 버튼을 활성화
                handleWaterPumpActionCompleted();
            }
        }

        // 결과 처리를 위한 메서드
        private void handleWaterPumpActionCompleted() {
            // 작업이 완료된 후에 실행되어야 할 코드를 추가하세요.
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    houseButton.setEnabled(true); // 버튼 활성화
                }
            });
        }
    }

    private class LedOnOff extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();
            // 현재 워터펌프 상태에 따라 URL 설정
            String url = isLEDOn ? "http://192.168.0.4/led/on/" : "http://192.168.0.4/led/off/";

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String result = response.body().string();
                // 결과를 처리하는 코드를 추가하세요.
                Log.d("LedOnOff", "LedOnOff: " + result);


                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        // onPostExecute 메서드는 그대로 둡니다.
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                // 결과를 처리하는 코드를 추가하세요.
                Log.d("LedOnOff", "Result: " + result);

                // 작업이 완료되면 버튼을 활성화
                handleLedActionCompleted();
            }
        }

        // 결과 처리를 위한 메서드
        private void handleLedActionCompleted() {
            // 작업이 완료된 후에 실행되어야 할 코드를 추가하세요.
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                  ledButton.setEnabled(true); // 버튼 활성화
                }
            });
        }
    }

    private void showWaterLevelAlert() {
        // 알림을 클릭했을 때 실행될 액티비티를 설정합니다.
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // Notification을 생성합니다.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default_channel_id")
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle("알림")
                .setContentText("물이 부족합니다. 물을 채워주세요!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Notification 매니저를 통해 Notification을 표시합니다.
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Default Channel";
            String description = "Default Notification Channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("default_channel_id", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}