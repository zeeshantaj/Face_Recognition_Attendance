package com.example.face_recognition_attendance_app.Activities.Adapter;

import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.face_recognition_attendance_app.Activities.Models.AttendanceHistoryModel;
import com.example.face_recognition_attendance_app.R;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<AttendanceHistoryModel> modelList;

    public HistoryAdapter(List<AttendanceHistoryModel> modelList) {
        this.modelList = modelList;
    }

    @NonNull
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.attendace_recycler_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.ViewHolder holder, int position) {
        AttendanceHistoryModel model = modelList.get(position);
        holder.date.setText(model.getCurrentDateTime());
        holder.checkInTine.setText(model.getCheckInTime());
        holder.checkOutTime.setText(model.getCheckOutTime());
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView date,checkInTine,checkOutTime,totalTime;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.dateTxt);
            checkInTine = itemView.findViewById(R.id.checkInTime);
            checkOutTime = itemView.findViewById(R.id.checkOutTime);
            totalTime = itemView.findViewById(R.id.totalTime);

        }
    }
}
