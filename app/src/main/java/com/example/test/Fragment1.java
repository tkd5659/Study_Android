package com.example.test;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * 날씨 아이콘
 *
 * ① 맑음
 * ② 구름 조금
 * ③ 구름 많음
 * ④ 흐림
 * ⑤ 비
 * ⑥ 눈/비
 * ⑦ 눈
 */
public class Fragment1 extends Fragment {
    private static final String TAG = "Fragment1";

    RecyclerView recyclerView;
    NoteAdapter adapter;

    Context context;
    OnTabItemSelectedListener listener;

    SimpleDateFormat todayDateFormat;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.context = context;

        if (context instanceof OnTabItemSelectedListener) {
            listener = (OnTabItemSelectedListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (context != null) {
            context = null;
            listener = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment1, container, false);

        initUI(rootView);

        // 데이터 로딩
        loadNoteListData();
        Button Backbtn=rootView.findViewById(R.id.BackBtn);
        Backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(),MainActivity.class);
                startActivity(i);
            }
        });
        return rootView;
    }


    private void initUI(ViewGroup rootView) {

        Button todayWriteButton = rootView.findViewById(R.id.WriteBtn);
        todayWriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onTabSelected(1);
                }
            }
        });



        recyclerView = rootView.findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);


        adapter = new NoteAdapter();

        adapter.addItem(new Note(0 ,"광주광역시 농성동","","","Hello","capture1.jpg","11월 20일"));
        adapter.addItem(new Note(1 ,"광주광역시 농성동","","","안녕","capture2.jpg","11월 19일"));
        adapter.addItem(new Note(2 ,"광주광역시 농성동","","","FarmDiary",null,"11월 18일"));


        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new OnNoteItemClickListener() {
            @Override
            public void onItemClick(NoteAdapter.ViewHolder holder, View view, int position) {
                Note item = adapter.getItem(position);

                Log.d(TAG, "아이템 선택됨 : " + item.get_id());

                if (listener != null) {
                    listener.showFragment2(item);
                }
            }
        });

    }

    /**
     * 리스트 데이터 로딩
     */
    public int loadNoteListData() {
        AppConstants.println("loadNoteListData called.");

        String sql = "select _id,ADDRESS, LOCATION_X, LOCATION_Y, CONTENTS, PICTURE, CREATE_DATE, MODIFY_DATE from " + NoteDatabase.TABLE_NOTE + " order by CREATE_DATE desc";

        int recordCount = 0;
        NoteDatabase database = NoteDatabase.getInstance(context);
        if (database != null) {
            Cursor outCursor = database.rawQuery(sql);

            recordCount = outCursor.getCount();
            AppConstants.println("record count : " + recordCount + "\n");

            ArrayList<Note> items = new ArrayList<Note>();

            for (int i = 0; i < recordCount; i++) {
                outCursor.moveToNext();

                int _id = outCursor.getInt(0);
                String address = outCursor.getString(1);
                String locationX = outCursor.getString(2);
                String locationY = outCursor.getString(3);
                String contents = outCursor.getString(4);
                String picture = outCursor.getString(5);
                String dateStr = outCursor.getString(6);
                String createDateStr = null;
                if (dateStr != null && dateStr.length() > 10) {
                    try {
                        Date inDate = AppConstants.dateFormat4.parse(dateStr);

                        if (todayDateFormat == null) {
                            todayDateFormat = new SimpleDateFormat(getResources().getString(R.string.today_date_format));
                        }
                        createDateStr = todayDateFormat.format(inDate);
                        AppConstants.println("currentDateString : " + createDateStr);
                        //createDateStr = AppConstants.dateFormat3.format(inDate);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    createDateStr = "";
                }

                AppConstants.println("#" + i + " -> " + _id + ", "  +
                        address + ", " + locationX + ", " + locationY + ", " + contents + ", " +
                        picture + ", " + createDateStr);

                items.add(new Note(_id,  address, locationX, locationY, contents, picture, createDateStr));
            }

            outCursor.close();

            adapter.setItems(items);
            adapter.notifyDataSetChanged();

        }

        return recordCount;
    }

}