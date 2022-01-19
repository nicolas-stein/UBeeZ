package fr.polytech.ubeez.intro;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.appintro.SlideBackgroundColorHolder;
import com.github.appintro.SlidePolicy;

import java.util.ArrayList;
import java.util.List;

import fr.polytech.ubeez.R;

public class IntroBluetoothConnectFragment extends Fragment implements SlideBackgroundColorHolder, SlidePolicy {

	private ConstraintLayout rootLayout;
	private ProgressBar connection_status_progressBar, telemetry_status_service_progressBar, telemetry_status_temperature_progressBar, telemetry_status_humidity_progressBar, configuration_status_service_progressBar;
	private ImageView connection_status_imageView, telemetry_status_service_imageView, telemetry_status_temperature_imageView, telemetry_status_humidity_imageView, telemetry_status_battery_imageView, telemetry_status_weight_imageView, telemetry_status_light_imageView, configuration_status_service_imageView, configuration_status_delay_imageView, configuration_status_location_imageView;
	private TextView connect_title, connect_subtitle;

	private BluetoothGatt bluetoothGatt;
	private boolean gotTelemetry = false;
	private boolean gotConfiguration = false;
	private int originalColor;

	public static IntroBluetoothConnectFragment newInstance() {
		return new IntroBluetoothConnectFragment();
	}

	public void connectToHive(BluetoothDevice bluetoothDevice) {
		gotTelemetry = false;
		gotConfiguration = false;
		if(bluetoothGatt!=null){
			bluetoothGatt.disconnect();
			bluetoothGatt = null;
		}

		if(getContext()!=null) {
			connection_status_progressBar.setVisibility(View.VISIBLE);
			connection_status_imageView.setVisibility(View.GONE);

			telemetry_status_service_imageView.setVisibility(View.VISIBLE);
			telemetry_status_service_imageView.setImageResource(R.mipmap.ic_not_ok);
			telemetry_status_service_progressBar.setVisibility(View.GONE);

			telemetry_status_temperature_imageView.setVisibility(View.VISIBLE);
			telemetry_status_temperature_imageView.setImageResource(R.mipmap.ic_not_ok);
			telemetry_status_temperature_progressBar.setVisibility(View.GONE);

			telemetry_status_humidity_imageView.setVisibility(View.VISIBLE);
			telemetry_status_humidity_imageView.setImageResource(R.mipmap.ic_not_ok);
			telemetry_status_humidity_progressBar.setVisibility(View.GONE);

			telemetry_status_battery_imageView.setImageResource(R.mipmap.ic_not_ok);
			telemetry_status_weight_imageView.setImageResource(R.mipmap.ic_not_ok);
			telemetry_status_light_imageView.setImageResource(R.mipmap.ic_not_ok);

			configuration_status_service_imageView.setVisibility(View.VISIBLE);
			configuration_status_service_imageView.setImageResource(R.mipmap.ic_not_ok);
			configuration_status_service_progressBar.setVisibility(View.GONE);

			connect_title.setText("Connexion à la ruche");
			connect_title.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
			connect_subtitle.setText("Nous allons maintenant vous connecter à la ruche !");
			connect_subtitle.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
		}

		bluetoothGatt = bluetoothDevice.connectGatt(getContext(), false, bluetoothGattCallback);
	}

	public BluetoothGatt getBluetoothGatt(){
		return bluetoothGatt;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_intro_bluetooth_connect, container, false);

		rootLayout = view.findViewById(R.id.fragment_intro_bluetooth_connect_root);
		originalColor = ((ColorDrawable) rootLayout.getBackground()).getColor();

		connection_status_progressBar = view.findViewById(R.id.fragment_intro_bluetooth_connect_connection_status_progressBar);
		connection_status_imageView = view.findViewById(R.id.fragment_intro_bluetooth_connect_connection_status_imageView);

		telemetry_status_service_progressBar = view.findViewById(R.id.fragment_intro_bluetooth_connect_telemetry_status_service_progressbar);
		telemetry_status_service_imageView = view.findViewById(R.id.fragment_intro_bluetooth_connect_telemetry_status_service_imageview);

		telemetry_status_temperature_progressBar = view.findViewById(R.id.fragment_intro_bluetooth_connect_telemetry_status_temperature_progressbar);
		telemetry_status_temperature_imageView = view.findViewById(R.id.fragment_intro_bluetooth_connect_telemetry_status_temperature_imageview);

		telemetry_status_humidity_progressBar = view.findViewById(R.id.fragment_intro_bluetooth_connect_telemetry_status_humidity_progressbar);
		telemetry_status_humidity_imageView = view.findViewById(R.id.fragment_intro_bluetooth_connect_telemetry_status_humidity_imageview);

		telemetry_status_battery_imageView = view.findViewById(R.id.fragment_intro_bluetooth_connect_telemetry_status_battery_imageview);

		telemetry_status_weight_imageView = view.findViewById(R.id.fragment_intro_bluetooth_connect_telemetry_status_weight_imageview);

		telemetry_status_light_imageView = view.findViewById(R.id.fragment_intro_bluetooth_connect_telemetry_status_light_imageview);

		configuration_status_service_progressBar = view.findViewById(R.id.fragment_intro_bluetooth_connect_configuration_status_service_progressbar);
		configuration_status_service_imageView = view.findViewById(R.id.fragment_intro_bluetooth_connect_configuration_status_service_imageview);

		configuration_status_delay_imageView = view.findViewById(R.id.fragment_intro_bluetooth_connect_configuration_status_delay_imageview);
		configuration_status_location_imageView = view.findViewById(R.id.fragment_intro_bluetooth_connect_configuration_status_location_imageview);

		connect_title = view.findViewById(R.id.fragment_intro_bluetooth_connect_title);
		connect_subtitle = view.findViewById(R.id.fragment_intro_bluetooth_connect_subtitle);

		return view;
	}

	private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

		private BluetoothGattService sensorGattService;
		private BluetoothGattService deviceInfoGattService;
		private List<BluetoothGattDescriptor> descriptorQueue = new ArrayList<>();
		private boolean[] gotTemperatureCharacteristic;
		private boolean[] gotHumidityCharacteristic;
		boolean gotWeightCharacteritic;
		boolean gotBatteryCharacteritic;
		boolean gotLightCharacteristic;
		boolean gotDelayCharacteristic;
		boolean gotLocationCharacteristic;

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			if(newState == BluetoothProfile.STATE_CONNECTED){
				if(getActivity()!=null){
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							connection_status_progressBar.setVisibility(View.GONE);
							connection_status_imageView.setVisibility(View.VISIBLE);
							connection_status_imageView.setImageResource(R.mipmap.ic_ok);
							if(gatt.discoverServices()) {
								telemetry_status_service_progressBar.setVisibility(View.VISIBLE);
								telemetry_status_service_imageView.setVisibility(View.GONE);
								configuration_status_service_progressBar.setVisibility(View.VISIBLE);
								configuration_status_service_imageView.setVisibility(View.GONE);
							}
							else{
								//TODO : discoverServices failed
								Log.d("UBeeZ", "BLE GATT : discoverServices failed 1 !");
							}
						}
					});
				}
			}
			else if(newState == BluetoothProfile.STATE_DISCONNECTED){
				if(getActivity()!=null) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							connection_status_progressBar.setVisibility(View.GONE);
							connection_status_imageView.setVisibility(View.VISIBLE);
							connection_status_imageView.setImageResource(R.mipmap.ic_not_ok);
						}
					});
				}
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			super.onServicesDiscovered(gatt, status);
			if(status==BluetoothGatt.GATT_SUCCESS){
				for (BluetoothGattService service: gatt.getServices()) {
					if(service.getUuid().toString().toUpperCase().startsWith("0000181A")){
						sensorGattService = service;
					}
					else if(service.getUuid().toString().toUpperCase().startsWith("0000180A")){
						deviceInfoGattService = service;
					}
				}
				if (getActivity() != null) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if(sensorGattService != null) {
								telemetry_status_service_progressBar.setVisibility(View.GONE);
								telemetry_status_service_imageView.setImageResource(R.mipmap.ic_ok);
								telemetry_status_service_imageView.setVisibility(View.VISIBLE);
								for (BluetoothGattCharacteristic characteristic: sensorGattService.getCharacteristics()) {
									if(characteristic.getUuid().toString().toUpperCase().startsWith("00002A19")){
										gotBatteryCharacteritic = true;
										telemetry_status_battery_imageView.setImageResource(R.mipmap.ic_ok);
									}
									else if(characteristic.getUuid().toString().toUpperCase().startsWith("00002A98")){
										gotWeightCharacteritic = true;
										telemetry_status_weight_imageView.setImageResource(R.mipmap.ic_ok);
									}
									else if(characteristic.getUuid().toString().toUpperCase().startsWith("00002AFB")){
										gotLightCharacteristic = true;
										telemetry_status_light_imageView.setImageResource(R.mipmap.ic_ok);
									}
									else {
										for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
											if (descriptor.getUuid().toString().toUpperCase().startsWith("00002904")) {
												descriptorQueue.add(descriptor);
											}
										}
									}
								}
								gotTemperatureCharacteristic = new boolean[]{false, false, false, false};
								gotHumidityCharacteristic = new boolean[]{false, false};
								telemetry_status_temperature_imageView.setVisibility(View.GONE);
								telemetry_status_temperature_progressBar.setVisibility(View.VISIBLE);
								telemetry_status_humidity_imageView.setVisibility(View.GONE);
								telemetry_status_humidity_progressBar.setVisibility(View.VISIBLE);
								if(descriptorQueue.size()>0) {
									gatt.readDescriptor(descriptorQueue.get(0));
								}
							}
							else{
								telemetry_status_service_imageView.setImageResource(R.mipmap.ic_not_ok);
							}

							if(deviceInfoGattService != null){
								configuration_status_service_progressBar.setVisibility(View.GONE);
								configuration_status_service_imageView.setImageResource(R.mipmap.ic_ok);
								configuration_status_service_imageView.setVisibility(View.VISIBLE);
								for(BluetoothGattCharacteristic characteristic: deviceInfoGattService.getCharacteristics()){
									if(characteristic.getUuid().toString().toUpperCase().startsWith("00002A21")){
										gotDelayCharacteristic = true;
										configuration_status_delay_imageView.setImageResource(R.mipmap.ic_ok);
									}
									else if(characteristic.getUuid().toString().toUpperCase().startsWith("00002A67")){
										gotLocationCharacteristic = true;
										configuration_status_location_imageView.setImageResource(R.mipmap.ic_ok);
									}
								}
								if(gotDelayCharacteristic && gotLocationCharacteristic){
									gotConfiguration = true;
									if(gotTelemetry){
										connect_title.setText("Connecté à la ruche !");
										connect_title.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorGreen));
										connect_subtitle.setText("Cliquez sur terminer pour continuer");
										connect_subtitle.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorGreen));
									}
								}
							}
						}
					});
				}
			}
			else{
				//TODO : discoverServices failed
				Log.d("UBeeZ", "BLE GATT : discoverServices failed 2 !");
			}
		}

		@Override
		public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
			if(descriptor.getValue().length==7 && descriptor.getUuid().toString().toUpperCase().startsWith("00002904")){//Descriptor : Characteristic Presentation Format
				if(descriptor.getCharacteristic().getUuid().toString().toUpperCase().startsWith("00002A6E")){//Temperature characterictis
					int a = descriptor.getValue()[5]-1;
					if(a>=0 && a<4){
						gotTemperatureCharacteristic[a] = true;
						if(gotTemperatureCharacteristic[0] && gotTemperatureCharacteristic[1] && gotTemperatureCharacteristic[2] && gotTemperatureCharacteristic[3] && getActivity()!=null){
							getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									telemetry_status_temperature_imageView.setVisibility(View.VISIBLE);
									telemetry_status_temperature_imageView.setImageResource(R.mipmap.ic_ok);
									telemetry_status_temperature_progressBar.setVisibility(View.GONE);
									if(gotHumidityCharacteristic[0] && gotHumidityCharacteristic[1] && gotWeightCharacteritic && gotBatteryCharacteritic && gotLightCharacteristic){
										gotTelemetry = true;
										if(gotConfiguration) {
											connect_title.setText("Connecté à la ruche !");
											connect_title.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorGreen));
											connect_subtitle.setText("Cliquez sur terminer pour continuer");
											connect_subtitle.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorGreen));
										}
									}
								}
							});
						}
					}
				}
				else if(descriptor.getCharacteristic().getUuid().toString().toUpperCase().startsWith("00002A6F")){//Humidity characteritics
					int a = descriptor.getValue()[5];
					if(descriptor.getValue()[6]==0x01){
						if(a==0x0B){
							gotHumidityCharacteristic[0] = true;
						}
						else if(a==0x0C){
							gotHumidityCharacteristic[1] = true;
						}

						if(gotHumidityCharacteristic[0] && gotHumidityCharacteristic[1] && getActivity()!=null){
							getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									telemetry_status_humidity_imageView.setVisibility(View.VISIBLE);
									telemetry_status_humidity_imageView.setImageResource(R.mipmap.ic_ok);
									telemetry_status_humidity_progressBar.setVisibility(View.GONE);
									if(gotTemperatureCharacteristic[0] && gotTemperatureCharacteristic[1] && gotTemperatureCharacteristic[2] && gotTemperatureCharacteristic[3] && gotWeightCharacteritic && gotBatteryCharacteritic && gotLightCharacteristic){
										gotTelemetry = true;
										if(gotConfiguration) {
											connect_title.setText("Connecté à la ruche !");
											connect_title.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorGreen));
											connect_subtitle.setText("Cliquez sur terminer pour continuer");
											connect_subtitle.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorGreen));
										}
									}
								}
							});
						}
					}
				}
			}

			descriptorQueue.remove(0);
			if(descriptorQueue.size()>0) {
				gatt.readDescriptor(descriptorQueue.get(0));
			}
		}
	};

	@Override
	public void onPause() {
		super.onPause();
		if(bluetoothGatt!=null){
			bluetoothGatt.disconnect();
			bluetoothGatt = null;
		}
	}

	@Override
	public boolean isPolicyRespected() {
		return gotTelemetry && gotConfiguration;
	}

	@Override
	public void onUserIllegallyRequestedNextPage() {
		//TODO
	}

	@Override
	public int getDefaultBackgroundColor() {
		return originalColor;
	}

	@Override
	public void setBackgroundColor(int i) {
		rootLayout.setBackgroundColor(i);
	}
}
