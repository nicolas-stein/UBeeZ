package fr.polytech.ubeez;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class StatusFragment extends Fragment {

	private ProgressBar telemetry_status_service_progressBar, configuration_status_service_progressBar;
	private ImageView telemetry_status_service_imageView, telemetry_status_battery_imageView, telemetry_status_weight_imageView, telemetry_status_light_imageView, configuration_status_service_imageView, configuration_status_delay_imageView, configuration_status_location_imageView;
	private ImageView telemetry_status_temperature_1_imageView, telemetry_status_temperature_2_imageView, telemetry_status_temperature_3_imageView, telemetry_status_temperature_4_imageView;
	private ImageView telemetry_status_humidity_1_imageView, telemetry_status_humidity_2_imageView;

	private ProgressBar telemetry_status_temperature_1_progressbar, telemetry_status_temperature_2_progressbar, telemetry_status_temperature_3_progressbar, telemetry_status_temperature_4_progressbar;
	private ProgressBar telemetry_status_humidity_1_progressbar, telemetry_status_humidity_2_progressbar;
	private ProgressBar telemetry_status_battery_progressbar, telemetry_status_weight_progressbar, telemetry_status_light_progressbar;
	private ProgressBar configuration_status_delay_progressbar, configuration_status_location_progressbar;

	private boolean serviceAlreadyDiscovered;

	public StatusFragment() {
		// Required empty public constructor
	}


	public static StatusFragment newInstance() {
		return new StatusFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
	View view = inflater.inflate(R.layout.fragment_status, container, false);

		telemetry_status_service_progressBar = view.findViewById(R.id.fragment_status_telemetry_status_service_progressbar);
		telemetry_status_service_imageView = view.findViewById(R.id.fragment_status_telemetry_status_service_imageview);

		telemetry_status_temperature_1_imageView = view.findViewById(R.id.fragment_status_telemetry_status_temperature_1_imageview);
		telemetry_status_temperature_2_imageView = view.findViewById(R.id.fragment_status_telemetry_status_temperature_2_imageview);
		telemetry_status_temperature_3_imageView = view.findViewById(R.id.fragment_status_telemetry_status_temperature_3_imageview);
		telemetry_status_temperature_4_imageView = view.findViewById(R.id.fragment_status_telemetry_status_temperature_4_imageview);

		telemetry_status_temperature_1_progressbar = view.findViewById(R.id.fragment_status_telemetry_status_temperature_1_progressbar);
		telemetry_status_temperature_2_progressbar = view.findViewById(R.id.fragment_status_telemetry_status_temperature_2_progressbar);
		telemetry_status_temperature_3_progressbar = view.findViewById(R.id.fragment_status_telemetry_status_temperature_3_progressbar);
		telemetry_status_temperature_4_progressbar = view.findViewById(R.id.fragment_status_telemetry_status_temperature_4_progressbar);

		telemetry_status_humidity_1_imageView = view.findViewById(R.id.fragment_status_telemetry_status_humidity_1_imageview);
		telemetry_status_humidity_2_imageView = view.findViewById(R.id.fragment_status_telemetry_status_humidity_2_imageview);

		telemetry_status_humidity_1_progressbar = view.findViewById(R.id.fragment_status_telemetry_status_humidity_1_progressbar);
		telemetry_status_humidity_2_progressbar = view.findViewById(R.id.fragment_status_telemetry_status_humidity_2_progressbar);

		telemetry_status_battery_imageView = view.findViewById(R.id.fragment_status_telemetry_status_battery_imageview);
		telemetry_status_battery_progressbar = view.findViewById(R.id.fragment_status_telemetry_status_battery_progressbar);

		telemetry_status_weight_imageView = view.findViewById(R.id.fragment_status_telemetry_status_weight_imageview);
		telemetry_status_weight_progressbar = view.findViewById(R.id.fragment_status_telemetry_status_weight_progressbar);

		telemetry_status_light_imageView = view.findViewById(R.id.fragment_status_telemetry_status_light_imageview);
		telemetry_status_light_progressbar = view.findViewById(R.id.fragment_status_telemetry_status_light_progressbar);

		configuration_status_service_progressBar = view.findViewById(R.id.fragment_status_configuration_status_service_progressbar);
		configuration_status_service_imageView = view.findViewById(R.id.fragment_status_configuration_status_service_imageview);

		configuration_status_delay_imageView = view.findViewById(R.id.fragment_status_configuration_status_delay_imageview);
		configuration_status_delay_progressbar = view.findViewById(R.id.fragment_status_configuration_status_delay_progressbar);

		configuration_status_location_imageView = view.findViewById(R.id.fragment_status_configuration_status_location_imageview);
		configuration_status_location_progressbar = view.findViewById(R.id.fragment_status_configuration_status_location_progressbar);

		resetFragment();

		return view;
	}

	public void resetFragment(){
		telemetry_status_service_progressBar.setVisibility(View.VISIBLE);
		telemetry_status_service_imageView.setVisibility(View.GONE);
		configuration_status_service_progressBar.setVisibility(View.VISIBLE);
		configuration_status_service_imageView.setVisibility(View.GONE);

		telemetry_status_temperature_1_imageView.setImageResource(R.mipmap.ic_not_ok);
		telemetry_status_temperature_2_imageView.setImageResource(R.mipmap.ic_not_ok);
		telemetry_status_temperature_3_imageView.setImageResource(R.mipmap.ic_not_ok);
		telemetry_status_temperature_4_imageView.setImageResource(R.mipmap.ic_not_ok);

		telemetry_status_temperature_1_imageView.setVisibility(View.VISIBLE);
		telemetry_status_temperature_2_imageView.setVisibility(View.VISIBLE);
		telemetry_status_temperature_3_imageView.setVisibility(View.VISIBLE);
		telemetry_status_temperature_4_imageView.setVisibility(View.VISIBLE);

		telemetry_status_temperature_1_progressbar.setVisibility(View.GONE);
		telemetry_status_temperature_2_progressbar.setVisibility(View.GONE);
		telemetry_status_temperature_3_progressbar.setVisibility(View.GONE);
		telemetry_status_temperature_4_progressbar.setVisibility(View.GONE);

		telemetry_status_humidity_1_imageView.setImageResource(R.mipmap.ic_not_ok);
		telemetry_status_humidity_2_imageView.setImageResource(R.mipmap.ic_not_ok);

		telemetry_status_humidity_1_imageView.setVisibility(View.VISIBLE);
		telemetry_status_humidity_2_imageView.setVisibility(View.VISIBLE);

		telemetry_status_humidity_1_progressbar.setVisibility(View.GONE);
		telemetry_status_humidity_2_progressbar.setVisibility(View.GONE);

		telemetry_status_battery_imageView.setImageResource(R.mipmap.ic_not_ok);
		telemetry_status_battery_imageView.setVisibility(View.VISIBLE);
		telemetry_status_battery_progressbar.setVisibility(View.GONE);

		telemetry_status_weight_imageView.setImageResource(R.mipmap.ic_not_ok);
		telemetry_status_weight_imageView.setVisibility(View.VISIBLE);
		telemetry_status_weight_progressbar.setVisibility(View.GONE);

		telemetry_status_light_imageView.setImageResource(R.mipmap.ic_not_ok);
		telemetry_status_light_imageView.setVisibility(View.VISIBLE);
		telemetry_status_light_progressbar.setVisibility(View.GONE);

		configuration_status_delay_imageView.setImageResource(R.mipmap.ic_not_ok);
		configuration_status_delay_imageView.setVisibility(View.VISIBLE);
		configuration_status_delay_progressbar.setVisibility(View.GONE);

		configuration_status_location_imageView.setImageResource(R.mipmap.ic_not_ok);
		configuration_status_location_imageView.setVisibility(View.VISIBLE);
		configuration_status_location_progressbar.setVisibility(View.GONE);

		serviceAlreadyDiscovered = false;
	}

	public void onServicesDiscovered(boolean searchingTemperature, boolean searchingHumidity){
		MainActivity activity = (MainActivity) getActivity();
		if(activity==null || serviceAlreadyDiscovered){
			return;
		}
		serviceAlreadyDiscovered = true;
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				telemetry_status_service_progressBar.setVisibility(View.GONE);
				telemetry_status_service_imageView.setVisibility(View.VISIBLE);
				configuration_status_service_progressBar.setVisibility(View.GONE);
				configuration_status_service_imageView.setVisibility(View.VISIBLE);
				if(activity.getSensorService()!=null){
					telemetry_status_service_imageView.setImageResource(R.mipmap.ic_ok);
					if(searchingTemperature){
						telemetry_status_temperature_1_imageView.setVisibility(View.GONE);
						telemetry_status_temperature_2_imageView.setVisibility(View.GONE);
						telemetry_status_temperature_3_imageView.setVisibility(View.GONE);
						telemetry_status_temperature_4_imageView.setVisibility(View.GONE);

						telemetry_status_temperature_1_progressbar.setVisibility(View.VISIBLE);
						telemetry_status_temperature_2_progressbar.setVisibility(View.VISIBLE);
						telemetry_status_temperature_3_progressbar.setVisibility(View.VISIBLE);
						telemetry_status_temperature_4_progressbar.setVisibility(View.VISIBLE);
					}
					if(searchingHumidity){
						telemetry_status_humidity_1_imageView.setVisibility(View.GONE);
						telemetry_status_humidity_2_imageView.setVisibility(View.GONE);

						telemetry_status_humidity_1_progressbar.setVisibility(View.VISIBLE);
						telemetry_status_humidity_2_progressbar.setVisibility(View.VISIBLE);
					}
					if(activity.getBatteryCharacteristic()!=null){
						telemetry_status_battery_imageView.setVisibility(View.GONE);
						telemetry_status_battery_progressbar.setVisibility(View.VISIBLE);
						//telemetry_status_battery_imageView.setImageResource(R.mipmap.ic_warning);
					}
					if(activity.getWeightCharacteristic()!=null){
						telemetry_status_weight_imageView.setVisibility(View.GONE);
						telemetry_status_weight_progressbar.setVisibility(View.VISIBLE);
					}
					if(activity.getLightCharacteristic()!=null){
						telemetry_status_light_imageView.setVisibility(View.GONE);
						telemetry_status_light_progressbar.setVisibility(View.VISIBLE);
					}
					if(activity.getDelayCharacteristic()!=null){
						configuration_status_delay_imageView.setVisibility(View.GONE);
						configuration_status_delay_progressbar.setVisibility(View.VISIBLE);
						//configuration_status_delay_imageView.setImageResource(R.mipmap.ic_warning);
					}
					if(activity.getLocationCharacteristic()!=null){
						configuration_status_location_imageView.setVisibility(View.GONE);
						configuration_status_location_progressbar.setVisibility(View.VISIBLE);
						//configuration_status_location_imageView.setImageResource(R.mipmap.ic_warning);
					}
				}
				if(activity.getDeviceInfoService()!=null) {
					configuration_status_service_imageView.setImageResource(R.mipmap.ic_ok);
				}
			}
		});
	}

	public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status){
		MainActivity activity = (MainActivity) getActivity();
		if(characteristic==null || activity == null){
			return;
		}
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(activity.getDelayCharacteristic() == characteristic){
					configuration_status_delay_imageView.setVisibility(View.VISIBLE);
					configuration_status_delay_imageView.setImageResource(R.mipmap.ic_ok);
					configuration_status_delay_progressbar.setVisibility(View.GONE);
				}
				else if(activity.getLocationCharacteristic() == characteristic){
					configuration_status_location_imageView.setVisibility(View.VISIBLE);
					configuration_status_location_imageView.setImageResource(R.mipmap.ic_ok);
					configuration_status_location_progressbar.setVisibility(View.GONE);
				}
			}
		});
	}

	public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){
		MainActivity activity = (MainActivity) getActivity();
		if(characteristic==null || activity == null){
			return;
		}
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				BluetoothGattCharacteristic[] temperatureCharacteristic = activity.getTemperatureCharacteristic();
				BluetoothGattCharacteristic[] humidityCharacteristic = activity.getHumidityCharacteristic();
				BluetoothGattCharacteristic batteryCharacteristic = activity.getBatteryCharacteristic();
				BluetoothGattCharacteristic weightCharacteristic = activity.getWeightCharacteristic();
				BluetoothGattCharacteristic lightCharacteristic = activity.getLightCharacteristic();
				if(characteristic == temperatureCharacteristic[0]){
					telemetry_status_temperature_1_progressbar.setVisibility(View.GONE);
					telemetry_status_temperature_1_imageView.setVisibility(View.VISIBLE);
					if(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0) != 8230){//Temperature reading error
						telemetry_status_temperature_1_imageView.setImageResource(R.mipmap.ic_ok);
					}
					else{
						telemetry_status_temperature_1_imageView.setImageResource(R.mipmap.ic_warning);
					}
				}
				else if(characteristic == temperatureCharacteristic[1]){
					telemetry_status_temperature_2_progressbar.setVisibility(View.GONE);
					telemetry_status_temperature_2_imageView.setVisibility(View.VISIBLE);
					if(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0) != 8230){//Temperature reading error
						telemetry_status_temperature_2_imageView.setImageResource(R.mipmap.ic_ok);
					}
					else{
						telemetry_status_temperature_2_imageView.setImageResource(R.mipmap.ic_warning);
					}
				}
				else if(characteristic == temperatureCharacteristic[2]){
					telemetry_status_temperature_3_progressbar.setVisibility(View.GONE);
					telemetry_status_temperature_3_imageView.setVisibility(View.VISIBLE);
					if(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0) != 8230){//Temperature reading error
						telemetry_status_temperature_3_imageView.setImageResource(R.mipmap.ic_ok);
					}
					else{
						telemetry_status_temperature_3_imageView.setImageResource(R.mipmap.ic_warning);
					}
				}
				else if(characteristic == temperatureCharacteristic[3]){
					telemetry_status_temperature_4_progressbar.setVisibility(View.GONE);
					telemetry_status_temperature_4_imageView.setVisibility(View.VISIBLE);
					if(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0) != 8230){//Temperature reading error
						telemetry_status_temperature_4_imageView.setImageResource(R.mipmap.ic_ok);
					}
					else{
						telemetry_status_temperature_4_imageView.setImageResource(R.mipmap.ic_warning);
					}
				}
				else if(characteristic == humidityCharacteristic[0]){
					telemetry_status_humidity_1_progressbar.setVisibility(View.GONE);
					telemetry_status_humidity_1_imageView.setVisibility(View.VISIBLE);
					if(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0) != 10500){//Temperature reading error
						telemetry_status_humidity_1_imageView.setImageResource(R.mipmap.ic_ok);
					}
					else{
						telemetry_status_humidity_1_imageView.setImageResource(R.mipmap.ic_warning);
					}
				}
				else if(characteristic == humidityCharacteristic[1]){
					telemetry_status_humidity_2_progressbar.setVisibility(View.GONE);
					telemetry_status_humidity_2_imageView.setVisibility(View.VISIBLE);
					if(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0) != 10500){//Temperature reading error
						telemetry_status_humidity_2_imageView.setImageResource(R.mipmap.ic_ok);
					}
					else{
						telemetry_status_humidity_2_imageView.setImageResource(R.mipmap.ic_warning);
					}
				}
				else if(characteristic == batteryCharacteristic){
					if(telemetry_status_battery_imageView.getVisibility()==View.GONE){
						telemetry_status_battery_imageView.setVisibility(View.VISIBLE);
						telemetry_status_battery_progressbar.setVisibility(View.GONE);
					}

					if(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) != 0) {
						telemetry_status_battery_imageView.setImageResource(R.mipmap.ic_ok);
					}
					else{
						telemetry_status_battery_imageView.setImageResource(R.mipmap.ic_warning);
					}
				}
				else if(characteristic == weightCharacteristic){
					if(telemetry_status_weight_imageView.getVisibility()==View.GONE){
						telemetry_status_weight_imageView.setVisibility(View.VISIBLE);
						telemetry_status_weight_progressbar.setVisibility(View.GONE);
					}

					if(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0) != 20460) {
						telemetry_status_weight_imageView.setImageResource(R.mipmap.ic_ok);
					}
					else{
						telemetry_status_weight_imageView.setImageResource(R.mipmap.ic_warning);
					}
				}
				else if(characteristic == lightCharacteristic){
					if(telemetry_status_light_imageView.getVisibility()==View.GONE){
						telemetry_status_light_imageView.setVisibility(View.VISIBLE);
						telemetry_status_light_progressbar.setVisibility(View.GONE);
					}

					byte[] rawValue = characteristic.getValue();
					int value = ((rawValue[2] & 0xFF) << 16) | ((rawValue[1] & 0xFF) << 8) | (rawValue[0] & 0xFF);
					if(value != 1281435) {
						telemetry_status_light_imageView.setImageResource(R.mipmap.ic_ok);
					}
					else{
						telemetry_status_light_imageView.setImageResource(R.mipmap.ic_warning);
					}
				}

			}
		});
	}
}