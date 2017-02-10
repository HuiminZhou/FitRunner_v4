package com.example.huimin_zhou.Huimin_Zhou_FitRunner;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.huimin_zhou.Huimin_Zhou_FitRunner.Database.ExerciseEntry;

import java.util.Calendar;

/**
 * Created by Lucidity on 17/1/18.
 */

public class DialogsFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private EditText editText = null;
    private int id = 0;

    public static DialogsFragment newInstance(int id) {
        DialogsFragment dialogsFragment = new DialogsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Global.DIALOG_ID_KEY, id);
        dialogsFragment.setArguments(bundle);
        return dialogsFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle saveInstanceState) {
        id = getArguments().getInt(Global.DIALOG_ID_KEY);
        final Activity container = getActivity();

        Calendar mCalendar = Calendar.getInstance();
        AlertDialog.Builder mDialog = new AlertDialog.Builder(container);
        View view = container.getLayoutInflater().inflate(R.layout.layout_startdialog, null);
        mDialog.setView(view);

        editText = (EditText)view.findViewById(R.id.dialog_input);
        mDialog.setPositiveButton(R.string.btn_ok, this);
        mDialog.setNegativeButton(R.string.btn_cancel, null);

        switch (id) {
            case Global.DIALOG_DATE_PICKER:
                DatePickerDialog.OnDateSetListener mListner =
                        new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        ManualActivity.mEntry.setDate(year, month, day);
                    }
                };
                return new DatePickerDialog(
                        container,
                        mListner,
                        mCalendar.get(Calendar.YEAR),
                        mCalendar.get(Calendar.MONTH),
                        mCalendar.get(Calendar.DAY_OF_MONTH));
            case Global.DIALOG_TIME_PICKER:
                TimePickerDialog.OnTimeSetListener mListener =
                        new TimePickerDialog.OnTimeSetListener() {
                    public void onTimeSet(TimePicker view, int hour, int minute) {
                        ManualActivity.mEntry.setTime(hour, minute, 0);
                    }
                };
                return new TimePickerDialog(container, mListener, mCalendar.get(Calendar.HOUR_OF_DAY),
                        mCalendar.get(Calendar.MINUTE), true);
            case Global.DIALOG_DURATION:
                mDialog.setTitle(R.string.text_dura);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                return mDialog.create();
            case Global.DIALOG_DISTANCE:
                mDialog.setTitle(R.string.text_dist);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                return mDialog.create();
            case Global.DIALOG_CALORIES:
                mDialog.setTitle(R.string.text_calo);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                return mDialog.create();
            case Global.DIALOG_HEART_RATE:
                mDialog.setTitle(R.string.text_hear);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                return mDialog.create();
            case Global.DIALOG_COMMENT:
                mDialog.setTitle(R.string.text_comm);
                editText.setHint(R.string.text_hint);
                return mDialog.create();
            case Global.DIALOG_ID_PHOTO_PICKER:
                AlertDialog.Builder dialog = new AlertDialog.Builder(container);
                dialog.setTitle(R.string.dialog_photo);
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((ProfileActivity)container).onCamera(which);
                    }
                };
                dialog.setItems(R.array.entries_photo, listener);
                return dialog.create();
            default:
                return null;
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                switch (id) {
                    case Global.DIALOG_DURATION:
                        ManualActivity.mEntry.setDuration(
                                (int)(Double.parseDouble(editText.getText().toString()) * 60)); // minute to second
                        return;
                    case Global.DIALOG_DISTANCE:
                        ManualActivity.mEntry.setDistance(
                                Float.parseFloat(editText.getText().toString())); // Miles
                        return;
                    case Global.DIALOG_CALORIES:
                        ManualActivity.mEntry.setCalories(
                                Integer.parseInt(editText.getText().toString()));
                        return;
                    case Global.DIALOG_HEART_RATE:
                        ManualActivity.mEntry.setHeart(
                                Integer.parseInt(editText.getText().toString()));
                        return;
                    case Global.DIALOG_COMMENT:
                        return;
                    default:
                        return;
                }
            case DialogInterface.BUTTON_NEGATIVE:
                return;
            default:
                return;
        }
    }


}
