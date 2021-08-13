package com.example.bachelorthesis;

import android.app.DatePickerDialog;
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
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.bachelorthesis.persistence.DAOs.PatientDataDAO;
import com.example.bachelorthesis.persistence.databases.AppDataBase;
import com.example.bachelorthesis.persistence.entities.PatientDataRecord;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author Finn Zimmer
 */
//TODO: TESTING
public class AddDataBottomSheet extends BottomSheetDialogFragment {

    private ArrayAdapter<String> adapter;
    private final String[] measurements;
    private final long patientId;
    private final Calendar calendar = Calendar.getInstance();

    private String chosenType;

    private TextInputEditText data1;
    private TextInputEditText data2;

    public AddDataBottomSheet() {
        super();
        measurements = new String[0];
        patientId = -1;
    }

    public AddDataBottomSheet(String[] measurements, long patientId) {
        super();
        this.patientId = patientId;
        this.measurements = measurements;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_data_bottom_sheet, container, false);

        //cancel button
        view.findViewById(R.id.cancel_button).setOnClickListener((View v) -> this.close());


        data1 = view.findViewById(R.id.enter_data1_edit_text);
        data2 = view.findViewById(R.id.enter_data2_edit_text);

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
                chosenType = adapter.getItem(position).toString();
                measurementSelected(view,chosenType);



                closeKeyBoard();

            }
        });

        EditText dateView = view.findViewById(R.id.editTextDate2);
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {

                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                DateFormat df = SimpleDateFormat.getDateInstance();
                dateView.setText(df.format(calendar.getTime()));

            }

        };

        dateView.setOnClickListener((View v)->{
            new DatePickerDialog(getContext(), date, calendar
                    .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
        });


        SwitchMaterial switchM = view.findViewById(R.id.switch1);
        switchM.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    view.findViewById(R.id.editTextDate2).setVisibility(View.GONE);
                }else{
                    view.findViewById(R.id.editTextDate2).setVisibility(View.VISIBLE);
                }
            }
        });

        ExtendedFloatingActionButton saveFab = view.findViewById(R.id.save_data_fab);
        saveFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });


        return view;

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



    }

    private void saveData(){
        PatientDataDAO patientDataDAO = AppDataBase.getInstance(getContext()).patientDataDAO();

        if(chosenType.equals(getString(R.string.bloodpressure))){
            PatientDataRecord dataRecordSys = new PatientDataRecord(patientId,
                    getString(R.string.m_bpsys),
                    calendar.getTime(), data1.getText().toString(), "");
            PatientDataRecord dataRecordDia = new PatientDataRecord(patientId,
                    getString(R.string.m_bpdia),
                    calendar.getTime(), data2.getText().toString(), "");

            patientDataDAO.insertPatientDataRecords(dataRecordSys,dataRecordDia);
        }else {
            PatientDataRecord dataRecord = new PatientDataRecord(patientId, chosenType,
                    calendar.getTime(), data1.getText().toString(), data2.getText().toString());

            patientDataDAO.insertPatientDataRecord(dataRecord);
        }

        this.close();
    }
}
