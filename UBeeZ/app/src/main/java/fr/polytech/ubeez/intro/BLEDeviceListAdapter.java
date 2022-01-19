package fr.polytech.ubeez.intro;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Typeface;
import android.media.Image;
import android.os.ParcelUuid;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import fr.polytech.ubeez.R;

public class BLEDeviceListAdapter extends BaseAdapter {

	public static class BluetoothScannedDevice{
		private final BluetoothDevice device;
		private boolean advertisingSearchedService = false;

		public BluetoothScannedDevice(BluetoothDevice device, List<ParcelUuid> serviceUuids) {
			this.device = device;
			if(serviceUuids!=null) {
				for (ParcelUuid uuid : serviceUuids) {
					if (uuid.toString().toUpperCase().startsWith("0000181A")) {
						advertisingSearchedService = true;
						break;
					}
				}
			}
		}

		public BluetoothDevice getDevice() {
			return device;
		}

		public boolean isAdvertisingSearchedService() {
			return advertisingSearchedService;
		}
	}

	List<BluetoothScannedDevice> bluetoothDeviceList;
	List<String> bluetoothDeviceAddressList;
	Context context;

	public BLEDeviceListAdapter(Context context) {
		bluetoothDeviceList = new ArrayList<>();
		bluetoothDeviceAddressList = new ArrayList<>();
		this.context = context;
	}

	public void addDevice(BluetoothScannedDevice bluetoothDevice){
		if(!bluetoothDeviceAddressList.contains(bluetoothDevice.getDevice().getAddress())) {
			bluetoothDeviceList.add(bluetoothDevice);
			bluetoothDeviceAddressList.add(bluetoothDevice.getDevice().getAddress());
		}
		Collections.sort(bluetoothDeviceList, new Comparator<BluetoothScannedDevice>() {
			@Override
			public int compare(BluetoothScannedDevice device1, BluetoothScannedDevice device2) {
				if(device1.isAdvertisingSearchedService()){
					if(device2.isAdvertisingSearchedService()){
						return 0;
					}
					else{
						return -1;
					}
				}
				else if(device2.isAdvertisingSearchedService()){
					return 1;
				}

				if(device1.getDevice().getName()!=null && !device1.getDevice().getName().isEmpty()){
					if(device2.getDevice().getName()!=null && !device2.getDevice().getName().isEmpty()){
						return device1.getDevice().getName().compareTo(device2.getDevice().getName());
					}
					else{
						return -1;
					}
				}
				else if(device2.getDevice().getName()!=null && !device2.getDevice().getName().isEmpty()){
					return 1;
				}

				return device1.getDevice().getAddress().compareTo(device2.getDevice().getAddress());
			}
		});
		notifyDataSetChanged();
	}

	public void clearDeviceList(){
		bluetoothDeviceList.clear();
		bluetoothDeviceAddressList.clear();
		notifyDataSetInvalidated();
	}

	@Override
	public int getCount() {
		return bluetoothDeviceList.size();
	}

	@Override
	public Object getItem(int position) {
		return bluetoothDeviceList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;

		if(convertView == null){
			convertView = LayoutInflater.from(context).inflate(R.layout.layout_list_bledevice, parent, false);
			viewHolder = new ViewHolder(convertView);
			convertView.setTag(viewHolder);
		}
		else{
			viewHolder = (ViewHolder) convertView.getTag();
		}

		BluetoothScannedDevice currentScannedDevice = bluetoothDeviceList.get(position);
		BluetoothDevice currentDevice = currentScannedDevice.getDevice();
		if(currentDevice.getName()!=null && !currentDevice.getName().isEmpty()){
			viewHolder.name_textView.setText(currentDevice.getName());
			if(currentScannedDevice.isAdvertisingSearchedService()){
				viewHolder.name_textView.setTypeface(viewHolder.name_textView.getTypeface(), Typeface.BOLD);
				viewHolder.name_textView.setTextColor(ContextCompat.getColor(convertView.getContext(), R.color.colorGreen));
				viewHolder.icon_imageView.setImageDrawable(ContextCompat.getDrawable(convertView.getContext(), R.mipmap.ic_hive));
			}
			else{
				viewHolder.name_textView.setTypeface(viewHolder.name_textView.getTypeface(), Typeface.NORMAL);
				viewHolder.name_textView.setTextColor(ContextCompat.getColor(convertView.getContext(), android.R.color.black));
				viewHolder.icon_imageView.setImageDrawable(ContextCompat.getDrawable(convertView.getContext(), R.mipmap.ic_bluetooth));
			}
		}
		else{
			viewHolder.name_textView.setText("Nom inconnu");
			viewHolder.icon_imageView.setImageDrawable(ContextCompat.getDrawable(convertView.getContext(), R.mipmap.ic_bluetooth));
		}
		viewHolder.address_textView.setText(currentDevice.getAddress());

		return convertView;
	}

	private class ViewHolder{
		TextView name_textView;
		TextView address_textView;
		ImageView icon_imageView;

		public ViewHolder(View view) {
			this.name_textView = view.findViewById(R.id.layout_list_bledevice_name_textView);
			this.address_textView = view.findViewById(R.id.layout_list_bledevice_address_textView);
			this.icon_imageView = view.findViewById(R.id.layout_list_bledevice_icon_imageview);
		}
	}
}
