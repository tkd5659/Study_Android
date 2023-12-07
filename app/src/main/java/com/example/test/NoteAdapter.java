package com.example.test;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder>
        implements com.example.test.OnNoteItemClickListener {
    ArrayList<Note> items = new ArrayList<Note>();

    com.example.test.OnNoteItemClickListener listener;

    int layoutType = 0;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View itemView = inflater.inflate(R.layout.note_item, viewGroup, false);

        return new ViewHolder(itemView, this, layoutType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        Note item = items.get(position);
        viewHolder.setItem(item);
        viewHolder.setLayoutType(layoutType);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(Note item) {
        items.add(item);
    }

    public void setItems(ArrayList<Note> items) {
        this.items = items;
    }

    public Note getItem(int position) {
        return items.get(position);
    }

    public void setOnItemClickListener(com.example.test.OnNoteItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onItemClick(ViewHolder holder, View view, int position) {
        if (listener != null) {
            listener.onItemClick(holder, view, position);
        }
    }



    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layout1;
        LinearLayout layout2;


        ImageView pictureExistsImageView;
        ImageView pictureImageView;

        ImageView weatherImageView;
        ImageView weatherImageView2;

        TextView contentsTextView;
        TextView contentsTextView2;

        TextView locationTextView;
        TextView locationTextView2;

        TextView dateTextView;
        TextView dateTextView2;

        public ViewHolder(View itemView, final com.example.test.OnNoteItemClickListener listener, int layoutType) {
            super(itemView);

            layout1 = itemView.findViewById(R.id.layout1);
            layout2 = itemView.findViewById(R.id.layout2);


            pictureExistsImageView = itemView.findViewById(R.id.pictureExistsImageView);
            pictureImageView = itemView.findViewById(R.id.pictureImageView);

            weatherImageView = itemView.findViewById(R.id.weatherImageView);
            weatherImageView2 = itemView.findViewById(R.id.weatherImageView2);

            contentsTextView = itemView.findViewById(R.id.contentsTextView);
            contentsTextView2 = itemView.findViewById(R.id.contentsTextView2);

            locationTextView = itemView.findViewById(R.id.locationTextView);
            locationTextView2 = itemView.findViewById(R.id.locationTextView2);

            dateTextView = itemView.findViewById(R.id.dateTextView);
            dateTextView2 = itemView.findViewById(R.id.dateTextView2);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();

                    if (listener != null) {
                        listener.onItemClick(ViewHolder.this, view, position);
                    }
                }
            });

            setLayoutType(layoutType);
        }

        public void setItem(Note item) {

            // set picture exists
            String picturePath = item.getPicture();
            Log.d("NoteAdapter", "picturePath -> " + picturePath);

            if (picturePath != null && !picturePath.equals("")) {
                pictureExistsImageView.setVisibility(View.VISIBLE);
                pictureImageView.setVisibility(View.VISIBLE);
                pictureImageView.setImageURI(Uri.parse("file://" + picturePath));

            } else {
                pictureExistsImageView.setVisibility(View.GONE);
                pictureImageView.setImageResource(R.drawable.no_img);

            }

            // set weather

            contentsTextView.setText(item.getContents());
            contentsTextView2.setText(item.getContents());

            locationTextView.setText(item.getAddress());
            locationTextView2.setText(item.getAddress());

            dateTextView.setText(item.getCreateDateStr());
            dateTextView2.setText(item.getCreateDateStr());
        }


        public void setLayoutType(int layoutType) {
            if (layoutType == 0) {
                layout1.setVisibility(View.GONE);
                layout2.setVisibility(View.VISIBLE);
            } else if (layoutType == 1) {

                layout1.setVisibility(View.VISIBLE);
                layout2.setVisibility(View.GONE);
            }
        }

    }

}