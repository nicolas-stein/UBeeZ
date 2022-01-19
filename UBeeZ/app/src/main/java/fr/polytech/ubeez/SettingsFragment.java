package fr.polytech.ubeez;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.akexorcist.snaptimepicker.SnapTimePickerDialog;
import com.akexorcist.snaptimepicker.TimeRange;
import com.akexorcist.snaptimepicker.TimeValue;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SettingsFragment extends Fragment {

	private TextView sync_delay_textView;
	private ImageView sync_delay_edit_imageView;
	private Button save_button;

	public SettingsFragment() {
		// Required empty public constructor
	}

	public static SettingsFragment newInstance() {
		return new SettingsFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_settings, container, false);

		sync_delay_textView = view.findViewById(R.id.fragment_settings_configuration_sync_delay_textView);
		sync_delay_edit_imageView = view.findViewById(R.id.fragment_settings_configuration_sync_delay_edit_imageView);
		save_button = view.findViewById(R.id.fragment_settings_configuration_save_button);

		sync_delay_edit_imageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SnapTimePickerDialog.Builder dialogBuilder = new SnapTimePickerDialog.Builder()
						.setTitle(R.string.sync_delay)
						.setThemeColor(R.color.colorPrimary)
						.setSelectableTimeRange(new TimeRange(new TimeValue(0, 10), new TimeValue(23, 59)));
				MainActivity activity = (MainActivity) getActivity();
				if(activity != null) {
					if(activity.getDelayCharacteristic() != null){
						int seconds = activity.getDelayCharacteristic().getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
						int hours = seconds / 3600;
						int minutes = seconds/60 % 60;
						dialogBuilder.setPreselectedTime(new TimeValue(hours, minutes));
					}
					SnapTimePickerDialog dialog = dialogBuilder.build();
					dialog.setListener(new SnapTimePickerDialog.Listener() {
						@Override
						public void onTimePicked(int hours, int minutes) {
							if(activity.getDelayCharacteristic() != null && activity.getBluetoothGatt() != null){
								activity.getDelayCharacteristic().setValue(hours*3600+minutes*60, BluetoothGattCharacteristic.FORMAT_UINT16, 0);
								if(!activity.getBluetoothGatt().writeCharacteristic(activity.getDelayCharacteristic())){
									//TODO handle error
									Log.d("UBeeZ", "Error writing delay characteristic !");
								}
							}
						}
					});
					dialog.show(getActivity().getSupportFragmentManager(), SnapTimePickerDialog.TAG);
				}
			}
		});

		resetFragment();
		return view;
	}

	public void resetFragment(){
		MainActivity activity = (MainActivity) getActivity();
		if(activity == null){
			return;
		}
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				save_button.setEnabled(false);
				save_button.setVisibility(View.VISIBLE);
				if(activity.getDelayCharacteristic() != null){
					sync_delay_textView.setText("Délai de synchronisation : "+activity.getDelayCharacteristic().getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0).toString()+" s");
					sync_delay_edit_imageView.setVisibility(View.VISIBLE);
				}
				else{
					sync_delay_textView.setText("Délai de synchronisation :");
					sync_delay_edit_imageView.setVisibility(View.GONE);
				}
			}
		});
	}

	public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
		MainActivity activity = (MainActivity) getActivity();
		if(characteristic==null || activity == null){
			return;
		}
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(activity.getDelayCharacteristic() == characteristic){
					sync_delay_textView.setText("Délai de synchronisation : "+characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0).toString()+" s");
					sync_delay_edit_imageView.setVisibility(View.VISIBLE);
				}
			}
		});
	}
}