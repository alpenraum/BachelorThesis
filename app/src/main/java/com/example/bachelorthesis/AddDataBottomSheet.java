package com.example.bachelorthesis;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputLayout;

/**
 * @author Finn Zimmer
 */
public class AddDataBottomSheet extends BottomSheetDialogFragment {

    private ArrayAdapter<String> adapter;
    private final String[] measurements;

    public AddDataBottomSheet() {
        super();
        measurements = new String[0];
    }

    public AddDataBottomSheet(String[] measurements) {
        super();

        this.measurements = measurements;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_data_bottom_sheet, container, false);

        //cancel button
        view.findViewById(R.id.cancel_button).setOnClickListener((View v) -> this.close());


       adapter = new ArrayAdapter<String>(getContext(),
                R.layout.support_simple_spinner_dropdown_item, measurements);

        AutoCompleteTextView aCT = view.findViewById(R.id.autoCompleteTextView);
        aCT.setAdapter(adapter);
        aCT.setThreshold(1);
        aCT.setOnEditorActionListener((v, actionId, event) -> {

            if (actionId == KeyEvent.ACTION_DOWN) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    closeKeyBoard();
                    return true;
                }
            }
            return false;
        });

        aCT.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String measurement = adapter.getItem(position).toString();
                measurementSelected(view,measurement);



                closeKeyBoard();

            }
        });



        return view;
        //TODO: EVERYTHING
    }

    private void close() {
        this.dismiss();
    }

    private void closeKeyBoard() {
        View view = requireActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void measurementSelected(View parentView,String measurement){

        parentView.findViewById(R.id.input_data_layout).setVisibility(View.VISIBLE);

        TextView enter = parentView.findViewById(R.id.enterdata_textView);
        enter.setText(getString(R.string.enterData,measurement));

        if(measurement.equals(getString(R.string.bloodpressure))){
            TextInputLayout data1 = parentView.findViewById(R.id.enter_data1_input_layout);
            TextInputLayout data2 = parentView.findViewById(R.id.enter_data2_input_layout);
            data1.setHint("Systolic");
            data2.setHint("Diastolic");
        }

        //TODO: DATEPICKER, SAVE BUTTON
    }
}
