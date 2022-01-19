package fr.polytech.ubeez;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

import fr.polytech.ubeez.intro.IntroActivity;

public class MainActivity extends AppCompatActivity {

	private BottomNavigationView bottomNavigationView;
	private ViewPager2 viewPager;

	private ConstraintLayout connectingConstraintLayout;
	private LottieAnimationView lottieAnimationView;
	private TextView connectingTopTextView, connectingBottomTextView;
	private Button enableBluetoothButton, retryConnectButton, setupButton;

	private SharedPreferences sharedPreferences;
	private MainFragmentStateAdapter mainFragmentStateAdapter;

	private BluetoothAdapter bluetoothAdapter;
	private BluetoothDevice bluetoothDevice;
	private BluetoothGatt bluetoothGatt;

	private BluetoothGattService sensorService, deviceInfoService;
	private BluetoothGattCharacteristic[] temperatureCharacteristic, humidityCharacteristic;
	private BluetoothGattCharacteristic batteryCharacteristic, weightCharacteristic, lightCharacteristic, delayCharacteristic, locationCharacteristic;

	private boolean isConnected = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		bottomNavigationView = findViewById(R.id.activity_main_bottomNavigationView);
		viewPager = findViewById(R.id.activity_main_viewPager);
		connectingConstraintLayout = findViewById(R.id.activity_main_connecting_constraintLayout);
		lottieAnimationView = findViewById(R.id.activity_main_connecting_lottieAnimationView);
		connectingTopTextView = findViewById(R.id.activity_main_connecting_top_textView);
		connectingBottomTextView = findViewById(R.id.activity_main_connecting_bottom_textView);
		enableBluetoothButton = findViewById(R.id.activity_main_connecting_enable_bluetooth_button);
		retryConnectButton = findViewById(R.id.activity_main_connecting_retry_button);
		setupButton = findViewById(R.id.activity_main_connecting_setup_button);

		temperatureCharacteristic = new BluetoothGattCharacteristic[4];
		humidityCharacteristic = new BluetoothGattCharacteristic[4];

		sharedPreferences = getSharedPreferences(getString(R.string.preference_filename), MODE_PRIVATE);

		mainFragmentStateAdapter = new MainFragmentStateAdapter(this);
		viewPager.setAdapter(mainFragmentStateAdapter);
		bottomNavigationView.setItemIconTintList(null);
		bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
			@Override
			public boolean onNavigationItemSelected(@NonNull MenuItem item) {
				if(item.getItemId() == R.id.activity_main_bottom_navigation_page_status){
					viewPager.setCurrentItem(0);
				}
				else if(item.getItemId() == R.id.activity_main_bottom_navigation_page_data){
					viewPager.setCurrentItem(1);
				}
				else{
					viewPager.setCurrentItem(2);
				}
				return true;
			}
		});
		viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
			@Override
			public void onPageSelected(int position) {
				switch (position){
					case 0:
						bottomNavigationView.setSelectedItemId(R.id.activity_main_bottom_navigation_page_status);
						break;
					case 1:
						bottomNavigationView.setSelectedItemId(R.id.activity_main_bottom_navigation_page_data);
						break;
					case 2:
						bottomNavigationView.setSelectedItemId(R.id.activity_main_bottom_navigation_page_settings);
						break;
				}
			}
		});

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
		}
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		enableBluetoothButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
			}
		});
		retryConnectButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(bluetoothGatt==null){
					bluetoothDevice = bluetoothAdapter.getRemoteDevice(sharedPreferences.getString(getString(R.string.preference_BLE_addr), ""));
					bluetoothGatt = bluetoothDevice.connectGatt(v.getContext(), false, bluetoothGattCallback);
					connectingConstraintLayout.setVisibility(View.VISIBLE);
					enableBluetoothButton.setVisibility(View.GONE);
					retryConnectButton.setVisibility(View.GONE);
					setupButton.setVisibility(View.GONE);
					lottieAnimationView.setAnimation(R.raw.bluetooth_scan);
					lottieAnimationView.setRepeatCount(LottieDrawable.INFINITE);
					lottieAnimationView.resumeAnimation();
					connectingTopTextView.setText("Connexion en cours");
					connectingBottomTextView.setText("Merci de patienter pendant la connexion à la ruche...");
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						getWindow().setNavigationBarColor(ContextCompat.getColor(v.getContext(), R.color.colorPrimaryDark));
					}

					bottomNavigationView.setVisibility(View.GONE);
					viewPager.setVisibility(View.GONE);
				}
			}
		});
		setupButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(v.getContext())
						.setTitle("Se connecter à une autre ruche ?")
						.setMessage("Si vous n'arrivez pas à vous connecter à la ruche actuelle ou si vous souhaitez vous connecter à une autre ruche vous pouvez relancer l'étape de configuration.")
						.setPositiveButton("Continuer", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								Intent intent = new Intent(v.getContext(), IntroActivity.class);
								startActivity(intent);
							}
						})
						.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						})
						.show();
			}
		});

		registerReceiver(bluetoothBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
	}

	@Override
	protected void onResume() {
		super.onResume();
		String deviceAddress = sharedPreferences.getString(getString(R.string.preference_BLE_addr), "");
		if(deviceAddress.isEmpty()){
			Intent intent = new Intent(this, IntroActivity.class);
			startActivity(intent);
		}
		else if((Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED))
				|| (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
				|| (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)){
			Intent intent = new Intent(this, IntroActivity.class);
			intent.putExtra("type", "permission");
			startActivity(intent);
		}
		else if(!bluetoothAdapter.isEnabled()){
			connectingConstraintLayout.setVisibility(View.VISIBLE);
			enableBluetoothButton.setVisibility(View.VISIBLE);
			retryConnectButton.setVisibility(View.GONE);
			setupButton.setVisibility(View.GONE);
			lottieAnimationView.setRepeatCount(0);
			lottieAnimationView.setAnimation(R.raw.bluetooth_enable);
			lottieAnimationView.resumeAnimation();
			connectingTopTextView.setText("Activez le Bluetooth");
			connectingBottomTextView.setText("Pour que l'application puisse se connecter à la ruche, vous devez activer le Bluetooth !");
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
			}

			bottomNavigationView.setVisibility(View.GONE);
			viewPager.setVisibility(View.GONE);
		}
		else if(!isConnected){
			connectingConstraintLayout.setVisibility(View.VISIBLE);
			enableBluetoothButton.setVisibility(View.GONE);
			retryConnectButton.setVisibility(View.GONE);
			setupButton.setVisibility(View.GONE);
			lottieAnimationView.setAnimation(R.raw.bluetooth_scan);
			lottieAnimationView.setRepeatCount(LottieDrawable.INFINITE);
			lottieAnimationView.resumeAnimation();
			connectingTopTextView.setText("Connexion en cours");
			connectingBottomTextView.setText("Merci de patienter pendant la connexion à la ruche...");
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
			}

			bottomNavigationView.setVisibility(View.GONE);
			viewPager.setVisibility(View.GONE);

			if(bluetoothGatt==null){
				bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);
				bluetoothGatt = bluetoothDevice.connectGatt(this, false, bluetoothGattCallback);
			}
		}
		else{
			connectingConstraintLayout.setVisibility(View.GONE);
			bottomNavigationView.setVisibility(View.VISIBLE);
			viewPager.setVisibility(View.VISIBLE);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if(isConnected && bluetoothGatt!=null){
			bluetoothGatt.disconnect();
			bluetoothGatt.close();
			bluetoothGatt = null;
			isConnected = false;

			sensorService = null;
			deviceInfoService = null;
			temperatureCharacteristic[0] = null; temperatureCharacteristic[1] = null; temperatureCharacteristic[2] = null; temperatureCharacteristic[3] = null;
			humidityCharacteristic[0] = null; humidityCharacteristic[1] = null;
			batteryCharacteristic = null;
			weightCharacteristic = null;
			lightCharacteristic = null;
			delayCharacteristic = null;
			locationCharacteristic = null;
		}
	}

	private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

		private List<BluetoothGattDescriptor> descriptorQueue = new ArrayList<>();
		private List<BluetoothGattCharacteristic> characteristicReadQueue = new ArrayList<>();
		private List<BluetoothGattDescriptor> descriptorSubscribeQueue = new ArrayList<>();

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			super.onConnectionStateChange(gatt, status, newState);
			switch(newState){
				case BluetoothProfile.STATE_CONNECTED:
					Log.d("UBeeZ", "onConnectionStateChange: STATE_CONNECTED");
					if(sensorService != null && deviceInfoService !=null){
						Log.d("UBeeZ", "onConnectionStateChange: FALSE STATE_CONNECTED (ALREADY CONNECTED)");
						return;
					}
					if(!bluetoothGatt.discoverServices()){
						connectingConstraintLayout.setVisibility(View.VISIBLE);
						enableBluetoothButton.setVisibility(View.GONE);
						retryConnectButton.setVisibility(View.VISIBLE);
						setupButton.setVisibility(View.VISIBLE);
						lottieAnimationView.setRepeatCount(0);
						lottieAnimationView.setAnimation(R.raw.error);
						lottieAnimationView.setFrame(0);
						lottieAnimationView.resumeAnimation();
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
							getWindow().setNavigationBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
						}
						bottomNavigationView.setVisibility(View.GONE);
						viewPager.setVisibility(View.GONE);
						connectingTopTextView.setText("Echec de la connexion");
						connectingBottomTextView.setText("L'application n'a pas réussi à se connecter à la ruche : impossible de découvrir les services");
					}
					else{
						bottomNavigationView.setSelectedItemId(R.id.activity_main_bottom_navigation_page_status);
						viewPager.setCurrentItem(0);
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								connectingConstraintLayout.setVisibility(View.GONE);
								bottomNavigationView.setVisibility(View.VISIBLE);
								viewPager.setVisibility(View.VISIBLE);
								if(mainFragmentStateAdapter.getFragment(0) != null){
									((StatusFragment)mainFragmentStateAdapter.getFragment(0)).resetFragment();
								}
								if(mainFragmentStateAdapter.getFragment(1) != null){
									((DataFragment)mainFragmentStateAdapter.getFragment(1)).resetFragment();
								}
								if(mainFragmentStateAdapter.getFragment(2) != null){
									((SettingsFragment)mainFragmentStateAdapter.getFragment(2)).resetFragment();
								}
							}
						});
						isConnected = true;
					}
					break;
				case BluetoothProfile.STATE_DISCONNECTED:
					Log.d("UBeeZ", "onConnectionStateChange: STATE_DISCONNECTED");
					final boolean connected = isConnected;
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							connectingConstraintLayout.setVisibility(View.VISIBLE);
							enableBluetoothButton.setVisibility(View.GONE);
							retryConnectButton.setVisibility(View.VISIBLE);
							setupButton.setVisibility(View.VISIBLE);
							lottieAnimationView.setRepeatCount(0);
							lottieAnimationView.setAnimation(R.raw.error);
							lottieAnimationView.setFrame(0);
							lottieAnimationView.resumeAnimation();
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
								getWindow().setNavigationBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
							}

							bottomNavigationView.setVisibility(View.GONE);
							viewPager.setVisibility(View.GONE);
							if(connected){
								//déconnecté de la ruche
								connectingTopTextView.setText("Fin de la connexion");
								connectingBottomTextView.setText("L'application a été déconnectée de la ruche !");
							}
							else {
								//ruche non trouvée
								connectingTopTextView.setText("Echec de la connexion");
								connectingBottomTextView.setText("L'application n'a pas réussi à se connecter à la ruche !");
							}
						}
					});
					sensorService = null;
					deviceInfoService = null;
					temperatureCharacteristic[0] = null; temperatureCharacteristic[1] = null; temperatureCharacteristic[2] = null; temperatureCharacteristic[3] = null;
					humidityCharacteristic[0] = null; humidityCharacteristic[1] = null;
					batteryCharacteristic = null;
					weightCharacteristic = null;
					lightCharacteristic = null;
					delayCharacteristic = null;
					locationCharacteristic = null;

					bluetoothGatt = null;
					isConnected = false;
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			super.onServicesDiscovered(gatt, status);
			boolean searchingTemperature = false;
			boolean searchingHumidity = false;
			for (BluetoothGattService service : gatt.getServices()) {
				if (service.getUuid().toString().toUpperCase().startsWith("0000181A") && sensorService == null) {        //sensor service
					sensorService = service;
					for(BluetoothGattCharacteristic characteristic : service.getCharacteristics()){
						if(characteristic.getUuid().toString().toUpperCase().startsWith("00002A19")){
							batteryCharacteristic = characteristic;
						}
						else if(characteristic.getUuid().toString().toUpperCase().startsWith("00002A98")){
							weightCharacteristic = characteristic;
						}
						else if(characteristic.getUuid().toString().toUpperCase().startsWith("00002AFB")){
							lightCharacteristic = characteristic;
						}
						else {
							if(characteristic.getUuid().toString().toUpperCase().startsWith("00002A6E")){
								searchingTemperature = true;
							}
							else if(characteristic.getUuid().toString().toUpperCase().startsWith("00002A6F")){
								searchingHumidity = true;
							}
							for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
								if (descriptor.getUuid().toString().toUpperCase().startsWith("00002904")) {
									descriptorQueue.add(descriptor);
								}
							}
						}

						for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()){
							if (descriptor.getUuid().toString().toUpperCase().startsWith("00002902")) {
								descriptorSubscribeQueue.add(descriptor);
							}
						}
					}
				} else if (service.getUuid().toString().toUpperCase().startsWith("0000180A") && deviceInfoService==null) {    //device info service
					deviceInfoService = service;
					for(BluetoothGattCharacteristic characteristic : service.getCharacteristics()){
						if(characteristic.getUuid().toString().toUpperCase().startsWith("00002A21")){
							delayCharacteristic = characteristic;
							characteristicReadQueue.add(characteristic);
						}
						else if(characteristic.getUuid().toString().toUpperCase().startsWith("00002A67")){
							locationCharacteristic = characteristic;
							characteristicReadQueue.add(characteristic);
						}
					}
				}
			}

			if(mainFragmentStateAdapter.getFragment(0)!=null){
				((StatusFragment)mainFragmentStateAdapter.getFragment(0)).onServicesDiscovered(searchingTemperature, searchingHumidity);
			}

			if(descriptorQueue.size()>0){
				gatt.readDescriptor(descriptorQueue.get(0));
			}
			else if(characteristicReadQueue.size()>0){
				gatt.readCharacteristic(characteristicReadQueue.get(0));
			}
		}



		@Override
		public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
			super.onDescriptorRead(gatt, descriptor, status);
			if(descriptor.getValue().length==7 && descriptor.getUuid().toString().toUpperCase().startsWith("00002904")) {//Descriptor : Characteristic Presentation Format
				if (descriptor.getCharacteristic().getUuid().toString().toUpperCase().startsWith("00002A6E")) {//Temperature characteristic
					int a = descriptor.getValue()[5] - 1;
					if(a>=0 && a<4){
						temperatureCharacteristic[a] = descriptor.getCharacteristic();
					}
				}
				else if(descriptor.getCharacteristic().getUuid().toString().toUpperCase().startsWith("00002A6F")){//Humidity characteristic
					int a = descriptor.getValue()[5];
					if(descriptor.getValue()[6]==0x01) {
						if (a == 0x0B) {
							humidityCharacteristic[0] = descriptor.getCharacteristic();
						} else if (a == 0x0C) {
							humidityCharacteristic[1] = descriptor.getCharacteristic();
						}
					}
				}
			}

			descriptorQueue.remove(0);
			if(descriptorQueue.size()>0) {
				gatt.readDescriptor(descriptorQueue.get(0));
			}
			else if(characteristicReadQueue.size()>0){
				gatt.readCharacteristic(characteristicReadQueue.get(0));
			}

			/*if(mainFragmentStateAdapter.getFragment(0)!=null){
				((StatusFragment)mainFragmentStateAdapter.getFragment(0)).onDescriptorRead(descriptorQueue.size());
			}*/
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			super.onCharacteristicRead(gatt, characteristic, status);

			characteristicReadQueue.remove(0);
			if(characteristicReadQueue.size()>0){
				gatt.readCharacteristic(characteristicReadQueue.get(0));
			}
			else if(descriptorSubscribeQueue.size()>0){
				gatt.setCharacteristicNotification(descriptorSubscribeQueue.get(0).getCharacteristic(), true);
				descriptorSubscribeQueue.get(0).setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
				gatt.writeDescriptor(descriptorSubscribeQueue.get(0));
			}

			if(mainFragmentStateAdapter.getFragment(0)!=null){
				((StatusFragment)mainFragmentStateAdapter.getFragment(0)).onCharacteristicRead(gatt, characteristic, status);
			}
			if(mainFragmentStateAdapter.getFragment(2)!=null){
				((SettingsFragment)mainFragmentStateAdapter.getFragment(2)).onCharacteristicRead(gatt, characteristic, status);
			}
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
			super.onDescriptorWrite(gatt, descriptor, status);

			descriptorSubscribeQueue.remove(0);
			if(descriptorSubscribeQueue.size()>0){
				gatt.setCharacteristicNotification(descriptorSubscribeQueue.get(0).getCharacteristic(), true);
				descriptorSubscribeQueue.get(0).setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
				gatt.writeDescriptor(descriptorSubscribeQueue.get(0));
			}
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			super.onCharacteristicChanged(gatt, characteristic);
			//Log.d("BluetoothGatt", "onCharacteristicChanged: "+characteristic.getUuid());

			if(mainFragmentStateAdapter.getFragment(0)!=null){
				((StatusFragment)mainFragmentStateAdapter.getFragment(0)).onCharacteristicChanged(gatt, characteristic);
			}

			if(mainFragmentStateAdapter.getFragment(1)!=null){
				((DataFragment)mainFragmentStateAdapter.getFragment(1)).onCharacteristicChanged(gatt, characteristic);
			}
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			super.onCharacteristicWrite(gatt, characteristic, status);
			characteristicReadQueue.add(characteristic);
			if(characteristicReadQueue.size()> 0){
				gatt.readCharacteristic(characteristic);
			}
		}
	};

	private final BroadcastReceiver bluetoothBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
				switch (state) {
					case BluetoothAdapter.STATE_OFF:
					case BluetoothAdapter.STATE_TURNING_OFF:
						connectingConstraintLayout.setVisibility(View.VISIBLE);
						enableBluetoothButton.setVisibility(View.VISIBLE);
						retryConnectButton.setVisibility(View.GONE);
						setupButton.setVisibility(View.GONE);
						lottieAnimationView.setRepeatCount(0);
						lottieAnimationView.setAnimation(R.raw.bluetooth_enable);
						lottieAnimationView.setFrame(0);
						lottieAnimationView.resumeAnimation();
						connectingTopTextView.setText("Activez le Bluetooth");
						connectingBottomTextView.setText("Pour que l'application puisse se connecter à la ruche, vous devez activer le Bluetooth !");
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
							getWindow().setNavigationBarColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
						}

						bottomNavigationView.setVisibility(View.GONE);
						viewPager.setVisibility(View.GONE);
						break;
					case BluetoothAdapter.STATE_ON:
						if(bluetoothGatt==null){
							bluetoothDevice = bluetoothAdapter.getRemoteDevice(sharedPreferences.getString(getString(R.string.preference_BLE_addr), ""));
							bluetoothGatt = bluetoothDevice.connectGatt(context, false, bluetoothGattCallback);
							connectingConstraintLayout.setVisibility(View.VISIBLE);
							enableBluetoothButton.setVisibility(View.GONE);
							retryConnectButton.setVisibility(View.GONE);
							setupButton.setVisibility(View.GONE);
							lottieAnimationView.setAnimation(R.raw.bluetooth_scan);
							lottieAnimationView.setRepeatCount(LottieDrawable.INFINITE);
							lottieAnimationView.resumeAnimation();
							connectingTopTextView.setText("Connexion en cours");
							connectingBottomTextView.setText("Merci de patienter pendant la connexion à la ruche...");
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
								getWindow().setNavigationBarColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
							}

							bottomNavigationView.setVisibility(View.GONE);
							viewPager.setVisibility(View.GONE);
						}
						break;
				}
			}
		}
	};

	public BluetoothGatt getBluetoothGatt() {
		return bluetoothGatt;
	}
	public BluetoothGattService getSensorService() {
		return sensorService;
	}
	public BluetoothGattService getDeviceInfoService() {
		return deviceInfoService;
	}

	public BluetoothGattCharacteristic[] getTemperatureCharacteristic() {
		return temperatureCharacteristic;
	}

	public BluetoothGattCharacteristic[] getHumidityCharacteristic() {
		return humidityCharacteristic;
	}

	public BluetoothGattCharacteristic getBatteryCharacteristic() {
		return batteryCharacteristic;
	}

	public BluetoothGattCharacteristic getWeightCharacteristic() {
		return weightCharacteristic;
	}

	public BluetoothGattCharacteristic getLightCharacteristic() {
		return lightCharacteristic;
	}

	public BluetoothGattCharacteristic getDelayCharacteristic() {
		return delayCharacteristic;
	}

	public BluetoothGattCharacteristic getLocationCharacteristic() {
		return locationCharacteristic;
	}
}