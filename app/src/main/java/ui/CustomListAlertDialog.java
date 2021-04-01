package ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.brimbay.be.R;

import java.util.ArrayList;
import java.util.List;

import adapter.AlertDialogListAdapter;
import model.AgeSet;
import model.Gender;
import model.Locations;

public class CustomListAlertDialog {
	private Context context;
	private OnTraditionalAlertDialogInteraction traditionalAlertDialogInteraction;
	private OnAlertRadioInteraction onAlertRadioInteraction;
	private OnAlertCheckBoxInteraction onAlertCheckBoxInteraction;
	private OnCustomViewAlertDialogInteraction onCustomViewAlertDialogInteraction;

	public CustomListAlertDialog(Context context) {
		this.context = context;
	}

	public void setOnCustomViewAlertDialogInteraction(OnCustomViewAlertDialogInteraction onCustomViewAlertDialogInteraction) {
		this.onCustomViewAlertDialogInteraction = onCustomViewAlertDialogInteraction;
	}

	public void setOnAlertCheckBoxInteraction(OnAlertCheckBoxInteraction onAlertCheckBoxInteraction) {
		this.onAlertCheckBoxInteraction = onAlertCheckBoxInteraction;
	}

	public void setOnAlertRadioInteraction(OnAlertRadioInteraction onAlertRadioInteraction) {
		this.onAlertRadioInteraction = onAlertRadioInteraction;
	}

	public void setTraditionalAlertDialogInteraction(OnTraditionalAlertDialogInteraction traditionalAlertDialogInteraction) {
		this.traditionalAlertDialogInteraction = traditionalAlertDialogInteraction;
	}

	public void alertTraditionalList(String title, String[] data) {
		// setup the alert builder
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);

		// add a list
		builder.setItems(data, (dialog, which) -> {
			if (traditionalAlertDialogInteraction != null)
				traditionalAlertDialogInteraction.onTraditionalAlertDialogItemClick(data[which], which);
		});

		// create and show the alert dialog
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public void alertRadioList(String title, String[] data, int defaultChecked) {
		// setup the alert builder
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);

		// add a radio button list
		builder.setSingleChoiceItems(data, defaultChecked, (dialog, which) -> {
			if (onAlertRadioInteraction != null)
				onAlertRadioInteraction.onAlertRadioSingleChoiceClick(data[which], which);
		});

		// add OK and Cancel buttons
		builder.setPositiveButton("OK", (dialog, which) -> {
			if (onAlertRadioInteraction != null)
				onAlertRadioInteraction.onAlertRadioPositiveClick(data[which], which);
		});
		builder.setNegativeButton("Cancel", null);

		// create and show the alert dialog
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public void alertCheckboxList(String title, String[] data, boolean[] defaultChecked) {
		// setup the alert builder
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);

		// add a checkbox list
		builder.setMultiChoiceItems(data, defaultChecked, (dialog, which, isChecked) -> {
			// user checked or unchecked a box
		});

		// add OK and Cancel buttons
		builder.setPositiveButton("OK", (dialog, which) -> {
//                if (onAlertCheckBoxInteraction != null)onAlertCheckBoxInteraction.onAlertCheckBoxMultiChoiceClick();
		});
		builder.setNegativeButton("Cancel", null);

		// create and show the alert dialog
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public void alertDialogCustom(String title, ArrayList<String > data) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

		View view = LayoutInflater.from(context).inflate(R.layout.c_alert_dialog_list_parent, null, false);
		TextView _title = view.findViewById(R.id.ad_title);
		RecyclerView _recyclerView = view.findViewById(R.id.ad_recycler);
		AlertDialogListAdapter adapter = new AlertDialogListAdapter();
		alertDialog.setView(view);
		_title.setText(title);

		_recyclerView.setHasFixedSize(true);
		_recyclerView.setAdapter(adapter);
		adapter.setLists(data);
		AlertDialog dialog = alertDialog.create();
		adapter.setOnAlertDialogListInteraction((s, i) -> {
			if (onCustomViewAlertDialogInteraction != null)
				onCustomViewAlertDialogInteraction.onCustomViewAlertItemClick(s, i);
			dialog.dismiss();
		});
		dialog.show();
	}

	public void alertDialogCustomGender(String title, List<Gender> data) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

		View view = LayoutInflater.from(context).inflate(R.layout.c_alert_dialog_list_parent, null, false);
		TextView _title = view.findViewById(R.id.ad_title);
		RecyclerView _recyclerView = view.findViewById(R.id.ad_recycler);
		AlertDialogListAdapter adapter = new AlertDialogListAdapter();
		alertDialog.setView(view);
		_title.setText(title);

		_recyclerView.setHasFixedSize(true);
		_recyclerView.setAdapter(adapter);
		adapter.setGenderLists(data);
		AlertDialog dialog = alertDialog.create();
		adapter.setOnAlertDialogListInteraction((s, i) -> {
			if (onCustomViewAlertDialogInteraction != null)
				onCustomViewAlertDialogInteraction.onCustomViewAlertItemClick(s, i);
			dialog.dismiss();
		});
		dialog.show();
	}

	public void alertDialogCustomLocations(String title, List<Locations> data) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

		View view = LayoutInflater.from(context).inflate(R.layout.c_alert_dialog_list_parent, null, false);
		TextView _title = view.findViewById(R.id.ad_title);
		RecyclerView _recyclerView = view.findViewById(R.id.ad_recycler);
		AlertDialogListAdapter adapter = new AlertDialogListAdapter();
		alertDialog.setView(view);
		_title.setText(title);

		_recyclerView.setHasFixedSize(true);
		_recyclerView.setAdapter(adapter);
		adapter.setLocationLists(data);
		AlertDialog dialog = alertDialog.create();
		adapter.setOnAlertDialogListInteraction((s, i) -> {
			if (onCustomViewAlertDialogInteraction != null)
				onCustomViewAlertDialogInteraction.onCustomViewAlertItemClick(s, i);
			dialog.dismiss();
		});
		dialog.show();
	}

	public void alertDialogCustomAge(String title, List<AgeSet> data) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

		View view = LayoutInflater.from(context).inflate(R.layout.c_alert_dialog_list_parent, null, false);
		TextView _title = view.findViewById(R.id.ad_title);
		RecyclerView _recyclerView = view.findViewById(R.id.ad_recycler);
		AlertDialogListAdapter adapter = new AlertDialogListAdapter();
		alertDialog.setView(view);
		_title.setText(title);

		_recyclerView.setHasFixedSize(true);
		_recyclerView.setAdapter(adapter);
		adapter.setAgeLists(data);
		AlertDialog dialog = alertDialog.create();
		adapter.setOnAlertDialogListInteraction((s, i) -> {
			if (onCustomViewAlertDialogInteraction != null)
				onCustomViewAlertDialogInteraction.onCustomViewAlertItemClick(s, i);
			dialog.dismiss();
		});
		dialog.show();
	}

	public interface OnTraditionalAlertDialogInteraction {
		void onTraditionalAlertDialogItemClick(String s, int i);
	}

	public interface OnAlertRadioInteraction {
		void onAlertRadioSingleChoiceClick(String s, int i);
		void onAlertRadioPositiveClick(String s, int i);
	}

	public interface OnAlertCheckBoxInteraction {
		void onAlertCheckBoxMultiChoiceClick(String s, int i);
		void onAlertCheckBoxPositiveClick(String s, int i);
	}

	public interface OnCustomViewAlertDialogInteraction {
		void onCustomViewAlertItemClick(Object s, int i);
	}
}