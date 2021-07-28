package com.example.bachelorthesis.PatientRecyclerView;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bachelorthesis.R;
import com.example.bachelorthesis.persistence.entities.Patient;

import java.util.List;

public class PatientListAdapter extends RecyclerView.Adapter<PatientListAdapter.ViewHolder>{

    private final List<Patient> localDataSet;
    private int adapterPosition = -1;

    private RecyclerView.LayoutManager layoutManager;

    private PatientVisualizationCallback visualizationCallback;

    public PatientListAdapter(List<Patient> localDataSet, PatientVisualizationCallback callback, RecyclerView.LayoutManager layoutManager) {
        this.localDataSet = localDataSet;
        this.visualizationCallback = callback;
        this.layoutManager = layoutManager;
    }

    public void updateData(List<Patient> data) {
        localDataSet.clear();
        localDataSet.addAll(data);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.patient_recyclerview_listitem, parent, false);

        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(PatientListAdapter.ViewHolder holder, int position) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        holder.getTextview_patientNumber().setText(localDataSet.get(position).patientNumber);
        holder.getTextviewName().setText(localDataSet.get(position).name);


        holder.itemView.setOnClickListener(v -> {
            if (position != getAdapterPosition()) {

                if(getAdapterPosition()!=-1) {
                    View oldView = layoutManager.findViewByPosition(getAdapterPosition());
                    if (oldView != null) {
                        changeSelectedAppearance(oldView,false);
                    }
                }
                setAdapterPosition(position);
                changeSelectedAppearance(holder.itemView,true);
                visualizationCallback.showPatientData(
                        localDataSet.get(position));
            }
        });

    }

    private void changeSelectedAppearance(View view, boolean selected){
        int defaultColor = view.getContext().getColor(R.color.design_default_color_background);
        int selectedColor = view.getContext().getColor(R.color.primary_variant);

        ValueAnimator colorAnimation;
        if(selected){
            colorAnimation = ValueAnimator.ofArgb(defaultColor,selectedColor);
        }else{
            colorAnimation = ValueAnimator.ofArgb(selectedColor,defaultColor);
        }
        colorAnimation.setDuration(250);
        colorAnimation.addUpdateListener(
                valueAnimator -> view.setBackgroundColor((int)valueAnimator.getAnimatedValue()));

        colorAnimation.start();

    }

    public void clickAndScrollToPosition(int position){
        layoutManager.scrollToPosition(position);

        View old = layoutManager.findViewByPosition(position);
        if (old != null) {
            changeSelectedAppearance(old,true);
        }
        if(getAdapterPosition()!=-1) {
            View newView = layoutManager.findViewByPosition(getAdapterPosition());
            if (newView != null) {
                changeSelectedAppearance(newView, false);
            }
        }
        setAdapterPosition(position);
        visualizationCallback.showPatientData(
                localDataSet.get(position));

    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        holder.itemView.setOnLongClickListener(null);
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

    public int getAdapterPosition() {
        return adapterPosition;
    }

    public void setAdapterPosition(int adapterPosition) {
        this.adapterPosition = adapterPosition;
    }







    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textview_patientNumber;
        private final TextView textview_name;


        public ViewHolder(View itemView) {
            super(itemView);

            textview_patientNumber = itemView.findViewById(R.id.patient_list_textview_name);
            textview_name = itemView.findViewById(R.id.patientlist_textview_patientnumber);



        }

        public TextView getTextview_patientNumber() {
            return textview_patientNumber;
        }

        public TextView getTextviewName() {
            return textview_name;
        }




    }
}
