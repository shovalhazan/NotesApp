package com.main.notesapplication.View.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.main.notesapplication.Control.FireBase;
import com.main.notesapplication.Model.Note;
import com.main.notesapplication.View.Activities.NoteActivity;
import com.main.notesapplication.R;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;


public class CustomAdapter extends RecyclerView.Adapter<ViewHolder> {
    private ArrayList<Note> localDataSet;
    private Context context;

    public CustomAdapter(ArrayList<Note> list, Context context) {
        localDataSet=list;
        this.context=context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.note_recycler_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if((localDataSet.get(position).getImageId()!=null)){
            FireBase.getInstance().downloadStorageData(localDataSet.get(position).getImageId(),context,holder.getIMG_holder());
        }
        holder.getLBL_title().setText(localDataSet.get(position).getTitle());
        holder.getLBL_body().setText(localDataSet.get(position).getBody());
        holder.getDate().setText(localDataSet.get(position).getDate().toString());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("pttt", "onClick: "+localDataSet.get(position).getTitle()+localDataSet.get(position).getImageId());
                openNoteActivity(localDataSet.get(position));
            }
        });


    }

    private void openNoteActivity(Note note) {
        Log.d("pttt", "openNoteActivity: ");
        Intent myIntent = new Intent(context, NoteActivity.class);
        myIntent.putExtra("note",note);
         context.startActivity(myIntent);
    }

    @Override
    public int getItemCount() {
        Log.d("pttt", "getItemCount: ");
        return localDataSet.size();
    }

}
