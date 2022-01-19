package fr.polytech.ubeez.intro;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.github.appintro.SlideBackgroundColorHolder;
import com.github.appintro.SlidePolicy;

import fr.polytech.ubeez.R;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;


public class IntroBluetoothSearchFragment extends Fragment implements SlideBackgroundColorHolder, SlidePolicy {

	private ConstraintLayout rootLayout;
	private TextView connectivite_subtitle, devices_subtitle, devices_search_top_textView, devices_search_bottom_textView;
	private ImageView bluetooth_status_imageView;
	private ProgressBar bluetooth_progressBar;
	private Button connectivite_button, hive_not_detected_button;
	private CardView devices_cardView;
	private ListView devices_listView;
	private ProgressBar devices_search_progressBar;

	private BluetoothAdapter bluetoothAdapter;
	private BluetoothLeScannerCompat bluetoothLeScanner;
	private boolean isBLEScanning = false;
	private boolean showHiddenDevices = false;
	private BLEDeviceListAdapter bleDeviceListAdapter;
	private int originalColor;

	public static IntroBluetoothSearchFragment newInstance() {
		return new IntroBluetoothSearchFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_intro_bluetooth_search, container, false);

		rootLayout = view.findViewById(R.id.fragment_intro_bluetooth_search_root);
		originalColor = ((ColorDrawable) rootLayout.getBackground()).getColor();

		bluetooth_status_imageView = view.findViewById(R.id.fragment_intro_bluetooth_search_connectivite_bluetooth_status_imageView);
		bluetooth_progressBar = view.findViewById(R.id.fragment_intro_bluetooth_search_connectivite_bluetooth_status_progressBar);
		connectivite_button = view.findViewById(R.id.fragment_intro_bluetooth_search_connectivite_button);
		connectivite_subtitle = view.findViewById(R.id.fragment_intro_bluetooth_search_connectivite_subtitle);
		devices_cardView = view.findViewById(R.id.fragment_intro_bluetooth_search_devices_cardview);
		devices_listView = view.findViewById(R.id.fragment_intro_bluetooth_search_devices_listView);
		devices_subtitle = view.findViewById(R.id.fragment_intro_bluetooth_search_devices_subtitle);
		devices_search_top_textView = view.findViewById(R.id.fragment_intro_bluetooth_search_devices_search_top_textView);
		devices_search_bottom_textView = view.findViewById(R.id.fragment_intro_bluetooth_search_devices_search_bottom_textView);
		devices_search_progressBar = view.findViewById(R.id.fragment_intro_bluetooth_search_devices_search_progressBar);
		hive_not_detected_button = view.findViewById(R.id.fragment_intro_bluetooth_search_hive_not_detected_button);

		connectivite_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
			}
		});

		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(getActivity()!=null) {
			getActivity().registerReceiver(bluetoothBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
		}

		bleDeviceListAdapter = new BLEDeviceListAdapter(getContext());
		devices_listView.setAdapter(bleDeviceListAdapter);
		devices_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				BLEDeviceListAdapter.BluetoothScannedDevice bluetoothScannedDevice = (BLEDeviceListAdapter.BluetoothScannedDevice) bleDeviceListAdapter.getItem(position);
				if(!bluetoothScannedDevice.isAdvertisingSearchedService() && getActivity()!=null){
					new AlertDialog.Builder(getActivity())
							.setTitle("Etes-vous sûr ?")
							.setMessage("L'appareil que vous avez séléctionné n'est pas reconnu en tant que ruche. Voulez-vous essayer de vous y connecter ?")
							.setPositiveButton("Se connecter", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
									((IntroActivity) getActivity()).doneBluetoothSearching(bluetoothScannedDevice.getDevice());
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
				else if(getActivity()!=null){
					((IntroActivity) getActivity()).doneBluetoothSearching(bluetoothScannedDevice.getDevice());
				}
			}
		});

		hive_not_detected_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//TODO : afficher un vrai message
				AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext())
						.setTitle("TODO title")
						.setMessage("TODO message")
						.setNegativeButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});
				if (!showHiddenDevices) {
					builder.setPositiveButton("Afficher tous les appareils", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							showHiddenDevices = true;
							dialog.dismiss();
						}
					});
				}
				builder.show();
		}
		});

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		bluetooth_progressBar.setVisibility(View.GONE);
		bluetooth_status_imageView.setVisibility(View.VISIBLE);
		if(bluetoothAdapter.isEnabled()){
			bluetooth_status_imageView.setImageResource(R.mipmap.ic_ok);
			connectivite_button.setVisibility(View.GONE);
			connectivite_subtitle.setVisibility(View.GONE);
			devices_cardView.setVisibility(View.VISIBLE);

			if(!isBLEScanning) {
				bluetoothLeScanner = BluetoothLeScannerCompat.getScanner();
				bluetoothLeScanner.startScan(bleScanCallback);
				isBLEScanning = true;
			}
		}
		else{
			bluetooth_status_imageView.setImageResource(R.mipmap.ic_not_ok);
			connectivite_button.setVisibility(View.VISIBLE);
			connectivite_subtitle.setVisibility(View.VISIBLE);
			devices_cardView.setVisibility(View.GONE);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if(bluetoothLeScanner!=null) {
			bluetoothLeScanner.stopScan(bleScanCallback);
			bluetoothLeScanner = null;
		}
		isBLEScanning = false;
	}

	private final BroadcastReceiver bluetoothBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
				switch (state) {
					case BluetoothAdapter.STATE_OFF:
						connectivite_button.setVisibility(View.VISIBLE);
						connectivite_subtitle.setVisibility(View.VISIBLE);
						bluetooth_status_imageView.setImageResource(R.mipmap.ic_not_ok);
						bluetooth_status_imageView.setVisibility(View.VISIBLE);
						bluetooth_progressBar.setVisibility(View.GONE);
						bluetoothLeScanner = null;
						break;
					case BluetoothAdapter.STATE_TURNING_OFF:
						connectivite_button.setVisibility(View.VISIBLE);
						connectivite_subtitle.setVisibility(View.VISIBLE);
						bluetooth_status_imageView.setVisibility(View.GONE);
						bluetooth_progressBar.setVisibility(View.VISIBLE);
						break;
					case BluetoothAdapter.STATE_ON:
						connectivite_button.setVisibility(View.GONE);
						connectivite_subtitle.setVisibility(View.GONE);
						bluetooth_status_imageView.setImageResource(R.mipmap.ic_ok);
						bluetooth_status_imageView.setVisibility(View.VISIBLE);
						bluetooth_progressBar.setVisibility(View.GONE);
						break;
					case BluetoothAdapter.STATE_TURNING_ON:
						connectivite_button.setVisibility(View.GONE);
						connectivite_subtitle.setVisibility(View.GONE);
						bluetooth_status_imageView.setVisibility(View.GONE);
						bluetooth_progressBar.setVisibility(View.VISIBLE);
						break;
				}

				if(state == BluetoothAdapter.STATE_ON){
					devices_cardView.setVisibility(View.VISIBLE);
					devices_subtitle.setVisibility(View.GONE);
					devices_listView.setVisibility(View.GONE);
					devices_search_top_textView.setVisibility(View.VISIBLE);
					devices_search_progressBar.setVisibility(View.VISIBLE);
					devices_search_bottom_textView.setVisibility(View.VISIBLE);

					if(!isBLEScanning) {
						bluetoothLeScanner = BluetoothLeScannerCompat.getScanner();
						bluetoothLeScanner.startScan(bleScanCallback);
						isBLEScanning = true;
					}
					bleDeviceListAdapter.clearDeviceList();
				}
				else{
					devices_cardView.setVisibility(View.GONE);
					if(isBLEScanning) {
						bluetoothLeScanner.stopScan(bleScanCallback);
						isBLEScanning = false;
					}
				}
			}
		}
	};

	private final ScanCallback bleScanCallback = new ScanCallback() {
		@Override
		public void onScanResult(int callbackType, @NonNull ScanResult result) {
			super.onScanResult(callbackType, result);
			if(showHiddenDevices || (result.getDevice().getName()!=null && !result.getDevice().getName().isEmpty())) {
				if(devices_listView.getVisibility() != View.VISIBLE) {
					devices_cardView.setVisibility(View.VISIBLE);
					devices_subtitle.setVisibility(View.VISIBLE);
					devices_listView.setVisibility(View.VISIBLE);

					devices_search_top_textView.setVisibility(View.GONE);
					devices_search_progressBar.setVisibility(View.GONE);
					devices_search_bottom_textView.setVisibility(View.GONE);
				}

				if(result.getScanRecord()!=null) {
					bleDeviceListAdapter.addDevice(new BLEDeviceListAdapter.BluetoothScannedDevice(result.getDevice(), result.getScanRecord().getServiceUuids()));
				}
				else{
					bleDeviceListAdapter.addDevice(new BLEDeviceListAdapter.BluetoothScannedDevice(result.getDevice(), null));
				}
			}
		}

		@Override
		public void onScanFailed(int errorCode) {
			super.onScanFailed(errorCode);
			bluetoothLeScanner.stopScan(bleScanCallback);
			if(getActivity()!=null) {
				new AlertDialog.Builder(getActivity())
						.setMessage("Erreur lors du scan !\nCode d'erreur : "+errorCode)
						.show();
			}

		}
	};

	@Override
	public boolean isPolicyRespected() {
		return false;
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