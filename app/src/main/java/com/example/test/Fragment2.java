package com.example.test;

import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Fragment2 extends Fragment {
    private static final String TAG = "Fragment2";

    int mMode = AppConstants.MODE_INSERT;
    int _id = -1;
    int weatherIndex = 0;

    Note item;

    Button deleteButton;
    Button saveButton;
    Context context;
    OnTabItemSelectedListener listener;
    OnRequestListener requestListener;

    ImageView weatherIcon;
    TextView dateTextView;
    TextView locationTextView;

    EditText contentsInput;
    ImageView pictureImageView;

    boolean isPhotoCaptured;
    boolean isPhotoFileSaved;
    boolean isPhotoCanceled;

    int selectedPhotoMenu;

    Uri uri;
    File file;
    Bitmap resultPhotoBitmap;

    SimpleDateFormat todayDateFormat;
    String currentDateString;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.context = context;

        if (context instanceof OnTabItemSelectedListener) {
            listener = (OnTabItemSelectedListener) context;
        }

        if (context instanceof OnRequestListener) {
            requestListener = (OnRequestListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (context != null) {
            context = null;
            listener = null;
            requestListener = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment2, container, false);

        initUI(rootView);

        // check current location
        if (requestListener != null) {
            requestListener.onRequest("getCurrentLocation");
        }

        applyItem();

        return rootView;
    }

    private void initUI(ViewGroup rootView) {

        weatherIcon = rootView.findViewById(R.id.weatherIcon);
        dateTextView = rootView.findViewById(R.id.dateTextView);
        locationTextView = rootView.findViewById(R.id.locationTextView);

        contentsInput = rootView.findViewById(R.id.contentsInput);
        pictureImageView = rootView.findViewById(R.id.pictureImageView);

        pictureImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPhotoCaptured || isPhotoFileSaved) {
                    showDialog(AppConstants.CONTENT_PHOTO_EX);
                } else {
                    showDialog(AppConstants.CONTENT_PHOTO);
                }
            }
        });

        saveButton = rootView.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mMode == AppConstants.MODE_INSERT) {
                    saveNote();
                } else if(mMode == AppConstants.MODE_MODIFY) {
                    modifyNote();
                }

                if (listener != null) {
                    listener.onTabSelected(0);
                }
            }
        });

        deleteButton = rootView.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteNote();

                if (listener != null) {
                    listener.onTabSelected(0);
                }
            }
        });

        Button closeButton = rootView.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onTabSelected(0);
                }
            }
        });



    }

    public void setAddress(String data) {
        locationTextView.setText(data);
    }

    public void setDateString(String dateString) {
        dateTextView.setText(dateString);
    }

    public void setContents(String data) {
        contentsInput.setText(data);
    }

    public void setPicture(String picturePath, int sampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        resultPhotoBitmap = BitmapFactory.decodeFile(picturePath, options);

        pictureImageView.setImageBitmap(resultPhotoBitmap);
    }



    public void setItem(Note item) {
        this.item = item;
    }

    public void applyItem() {
        AppConstants.println("applyItem called.");

        if (item != null) {
            mMode = AppConstants.MODE_MODIFY;
            saveButton.setText("수정");
            deleteButton.setVisibility(View.VISIBLE);
            setAddress(item.getAddress());
            setDateString(item.getCreateDateStr());
            setContents(item.getContents());

            String picturePath = item.getPicture();
            AppConstants.println("picturePath : " + picturePath);

            if (picturePath == null || picturePath.equals("")) {
                pictureImageView.setImageResource(R.drawable.no_img);
            } else {
                setPicture(item.getPicture(), 1);
            }

        } else {
            mMode = AppConstants.MODE_INSERT;
            deleteButton.setVisibility(GONE);
            setAddress("");

            Date currentDate = new Date();
            if (todayDateFormat == null) {
                todayDateFormat = new SimpleDateFormat(getResources().getString(R.string.today_date_format));
            }
            currentDateString = todayDateFormat.format(currentDate);
            AppConstants.println("currentDateString : " + currentDateString);
            setDateString(currentDateString);

            contentsInput.setText("");
            pictureImageView.setImageResource(R.drawable.no_img);

        }

    }

    /**
     * ① 맑음
     * ② 구름 조금
     * ③ 구름 많음
     * ④ 흐림
     * ⑤ 비
     * ⑥ 눈/비
     * ⑦ 눈
     *
     */

    public void showDialog(int id) {
        AlertDialog.Builder builder = null;

        switch(id) {

            case AppConstants.CONTENT_PHOTO:
                builder = new AlertDialog.Builder(context);

                builder.setTitle("사진 메뉴 선택");
                builder.setSingleChoiceItems(R.array.array_photo, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        selectedPhotoMenu = whichButton;
                    }
                });
                builder.setPositiveButton("선택", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(selectedPhotoMenu == 0 ) {
                            showPhotoCaptureActivity();
                        } else if(selectedPhotoMenu == 1) {
                            showPhotoSelectionActivity();
                        }
                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });

                break;

            case AppConstants.CONTENT_PHOTO_EX:
                builder = new AlertDialog.Builder(context);

                builder.setTitle("사진 메뉴 선택");
                builder.setSingleChoiceItems(R.array.array_photo_ex, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        selectedPhotoMenu = whichButton;
                    }
                });
                builder.setPositiveButton("선택", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(selectedPhotoMenu == 0) {
                            showPhotoCaptureActivity();
                        } else if(selectedPhotoMenu == 1) {
                            showPhotoSelectionActivity();
                        } else if(selectedPhotoMenu == 2) {
                            isPhotoCanceled = true;
                            isPhotoCaptured = false;

                            pictureImageView.setImageResource(R.drawable.no_img);
                        }
                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });

                break;

            default:
                break;
        }

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void showPhotoSelectionActivity() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(intent, AppConstants.REQ_PHOTO_SELECTION);
    }
    public void showPhotoCaptureActivity() {
        Intent i = new Intent(getContext(),CaptureActivity.class);
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i,AppConstants.REQ_PHOTO_CAPTURE);
    }


    public static Uri getFileUri(Context context, File file) {
        return FileProvider.getUriForFile(context, "com.example.test.fileprovider", file);
    }

    /**
     * 다른 액티비티로부터의 응답 처리
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);


        if (intent != null) {

            switch (requestCode) {

                case AppConstants.REQ_PHOTO_CAPTURE:
                    String filename=intent.getStringExtra("imgpath");
                    File file=new File(filename);
                    Uri fileUri1= getFileUri(context,file);

                    ContentResolver resolver1 = context.getContentResolver();


                    Log.d("","사진부르기성공");
                    try{
                        InputStream instream = resolver1.openInputStream(fileUri1);
                        resultPhotoBitmap = BitmapFactory.decodeStream(instream);
                        pictureImageView.setImageBitmap(resultPhotoBitmap);

                        instream.close();
                        isPhotoCaptured = true;
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;

                case AppConstants.REQ_PHOTO_SELECTION:  // 사진을 앨범에서 선택하는 경우
                    Log.d(TAG, "onActivityResult() for REQ_PHOTO_SELECTION.");

                    Uri fileUri = intent.getData();

                    ContentResolver resolver = context.getContentResolver();
                    Log.d("","uri:"+fileUri);
                    try {
                        InputStream instream = resolver.openInputStream(fileUri);
                        resultPhotoBitmap = BitmapFactory.decodeStream(instream);
                        pictureImageView.setImageBitmap(resultPhotoBitmap);

                        instream.close();

                        Log.d("","사진앨범부르기성공"+fileUri);
                        isPhotoCaptured = true;
                    } catch(Exception e) {
                        e.printStackTrace();
                    }

                    break;

            }
        }
    }

    public static Bitmap decodeSampledBitmapFromResource(File res, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(res.getAbsolutePath(),options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(res.getAbsolutePath(),options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height;
            final int halfWidth = width;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


    private String createFilename() {
        Date curDate = new Date();
        String curDateStr = String.valueOf(curDate.getTime());

        return curDateStr;
    }


    /**
     * 데이터베이스 레코드 추가
     */
    private void saveNote() {
        String address = locationTextView.getText().toString();
        String contents = contentsInput.getText().toString();

        String picturePath = savePicture();

        String sql = "insert into " + NoteDatabase.TABLE_NOTE +
                "(WEATHER, ADDRESS, LOCATION_X, LOCATION_Y, CONTENTS, PICTURE) values(" +
                "'"+ weatherIndex + "', " +
                "'"+ address + "', " +
                "'"+ "" + "', " +
                "'"+ "" + "', " +
                "'"+ contents + "', " +
                "'"+ picturePath + "')";

        Log.d(TAG, "sql : " + sql);
        NoteDatabase database = NoteDatabase.getInstance(context);
        database.execSQL(sql);

    }

    /**
     * 데이터베이스 레코드 수정
     */
    private void modifyNote() {
        if (item != null) {
            String address = locationTextView.getText().toString();
            String contents = contentsInput.getText().toString();

            String picturePath = savePicture();

            // update note
            String sql = "update " + NoteDatabase.TABLE_NOTE +
                    " set " +
                    "   WEATHER = '" + weatherIndex + "'" +
                    "   ,ADDRESS = '" + address + "'" +
                    "   ,LOCATION_X = '" + "" + "'" +
                    "   ,LOCATION_Y = '" + "" + "'" +
                    "   ,CONTENTS = '" + contents + "'" +
                    "   ,PICTURE = '" + picturePath + "'" +
                    " where " +
                    "   _id = " + item._id;

            Log.d(TAG, "sql : " + sql);
            NoteDatabase database = NoteDatabase.getInstance(context);
            database.execSQL(sql);
        }
    }


    /**
     * 레코드 삭제
     */
    private void deleteNote() {
        AppConstants.println("deleteNote called.");

        if (item != null) {
            // delete note
            String sql = "delete from " + NoteDatabase.TABLE_NOTE +
                    " where " +
                    "   _id = " + item._id;

            Log.d(TAG, "sql : " + sql);
            NoteDatabase database = NoteDatabase.getInstance(context);
            database.execSQL(sql);
        }
    }

    private String savePicture() {
        if (resultPhotoBitmap == null) {
            AppConstants.println("No picture to be saved.");
            return "";
        }

        File photoFolder = new File(AppConstants.FOLDER_PHOTO);

        if(!photoFolder.isDirectory()) {
            Log.d(TAG, "creating photo folder : " + photoFolder);
            photoFolder.mkdirs();
        }

        String photoFilename = createFilename();
        String picturePath = photoFolder + File.separator + photoFilename;

        try {
            FileOutputStream outstream = new FileOutputStream(picturePath);
            resultPhotoBitmap.compress(Bitmap.CompressFormat.PNG, 100, outstream);
            outstream.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        return picturePath;
    }

}