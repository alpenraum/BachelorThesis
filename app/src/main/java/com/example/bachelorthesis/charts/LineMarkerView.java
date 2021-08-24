package com.example.bachelorthesis.charts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.graphics.ColorUtils;

import com.example.bachelorthesis.R;
import com.example.bachelorthesis.persistence.entities.PatientDataRecord;
import com.example.bachelorthesis.utils.CustomColorUtils;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@SuppressLint("ViewConstructor")
public class LineMarkerView extends MarkerView {


    private final TextView value1;
    private final TextView value2;
    private final TextView date;

    private final TextView label_value1;
    private final TextView label_value2;

    private final CardView rootView;

    private final DateTimeFormatter mFormat = DateTimeFormatter.ofPattern("dd.MM.yy",
            Locale.ENGLISH);

    private final int backgroundColor;
    private final int textColor;

    private boolean nearRightBorder = false;
    private boolean nearLeftBorder =false;


    /**
     * Constructor. Sets up the MarkerView with a custom layout resource.
     *
     * @param context The current Context
     * @param layoutResource the layout resource to use for the MarkerView
     */
    public LineMarkerView(Context context, int layoutResource, int backgroundColor) {
        super(context, layoutResource);

        date = findViewById(R.id.marker_date);
        value1 = findViewById(R.id.marker_value1);
        value2 = findViewById(R.id.marker_value2);

        label_value1 = findViewById(R.id.label_marker_value1);
        label_value2 = findViewById(R.id.label_marker_value2);

        rootView = findViewById(R.id.marker_root);
        this.backgroundColor = ColorUtils.setAlphaComponent(backgroundColor,127);
        this.textColor = CustomColorUtils.isBrightColor(backgroundColor)?
                getContext().getColor(R.color.black):
                getContext().getColor(R.color.white);


    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        nearRightBorder = false;
        nearLeftBorder = false;

        if(e.getData()!=null){
            PatientDataRecord p = (PatientDataRecord) e.getData();

            value1.setText(p.value1);
            value2.setText(p.value2);
            date.setText(mFormat.format(p.timeStamp));

            rootView.setCardBackgroundColor((backgroundColor));
            value1.setTextColor((textColor));
            value2.setTextColor((textColor));
            label_value1.setTextColor((textColor));
            label_value2.setTextColor((textColor));
            date.setTextColor((textColor));
        }

        if(highlight.getXPx()>=700.0f){
            nearRightBorder = true;
        }else if(highlight.getXPx()<=100.0f){
            nearLeftBorder = true;
        }


        super.refreshContent(e, highlight);


    }

    @Override
    public MPPointF getOffset() {
        MPPointF pointF =new MPPointF(-(getWidth() / 2.0f), -getHeight());
        if(nearRightBorder){
            pointF.x =-getWidth();
        }
        if(nearLeftBorder){
            pointF.x = 0;
        }
        return pointF;
    }


}
