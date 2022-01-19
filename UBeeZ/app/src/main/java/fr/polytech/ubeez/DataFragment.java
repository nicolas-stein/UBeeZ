package fr.polytech.ubeez;

import android.animation.ArgbEvaluator;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.akexorcist.roundcornerprogressbar.TextRoundCornerProgressBar;

import java.text.DecimalFormat;

public class DataFragment extends Fragment {

	private TextRoundCornerProgressBar interieur_battery_progressBar;
	private TextView exterieur_temperature_textView, exterieur_humidity_textView, exterieur_light_textView, interieur_weight_textView, interieur_temperature_1_textView, interieur_humidity_1_textView, interieur_temperature_2_textView, interieur_temperature_3_textView;

	public DataFragment() {
		// Required empty public constructor
	}

	public static DataFragment newInstance() {
		return new DataFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_data, container, false);

		exterieur_temperature_textView = view.findViewById(R.id.fragment_data_exterieur_data_temperature_textview);
		exterieur_humidity_textView = view.findViewById(R.id.fragment_data_exterieur_data_humidity_textview);
		exterieur_light_textView = view.findViewById(R.id.fragment_data_exterieur_data_light_textview);

		interieur_battery_progressBar = view.findViewById(R.id.fragment_data_interieur_battery_progressbar);
		interieur_weight_textView = view.findViewById(R.id.fragment_data_interieur_weight_textview);

		interieur_temperature_1_textView = view.findViewById(R.id.fragment_data_interieur_temperature_1_textview);
		interieur_humidity_1_textView = view.findViewById(R.id.fragment_data_interieur_humidity_1_textview);

		interieur_temperature_2_textView = view.findViewById(R.id.fragment_data_interieur_temperature_2_textview);
		interieur_temperature_3_textView = view.findViewById(R.id.fragment_data_interieur_temperature_3_textview);

		resetFragment();
		return view;
	}

	public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){
		MainActivity activity = (MainActivity) getActivity();
		if(characteristic==null || activity == null){
			return;
		}
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (characteristic == activity.getTemperatureCharacteristic()[0]) {
					int temperature = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0);
					if(temperature != 8230){//Temperature reading error
						DecimalFormat f = new DecimalFormat("##.#");
						interieur_temperature_1_textView.setText(f.format((double)temperature/100)+" °C");
					}
					else{
						interieur_temperature_1_textView.setText("X °C");
					}
				}
				else if (characteristic == activity.getTemperatureCharacteristic()[1]) {
					int temperature = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0);
					if(temperature != 8230){//Temperature reading error
						DecimalFormat f = new DecimalFormat("##.#");
						interieur_temperature_2_textView.setText(f.format((double)temperature/100)+" °C");
					}
					else{
						interieur_temperature_2_textView.setText("X °C");
					}
				}
				else if (characteristic == activity.getTemperatureCharacteristic()[2]) {
					int temperature = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0);
					if(temperature != 8230){//Temperature reading error
						DecimalFormat f = new DecimalFormat("##.#");
						interieur_temperature_3_textView.setText(f.format((double)temperature/100)+" °C");
					}
					else{
						interieur_temperature_3_textView.setText("X °C");
					}
				}
				else if (characteristic == activity.getTemperatureCharacteristic()[3]) {
					int temperature = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0);
					if(temperature != 8230){//Temperature reading error
						DecimalFormat f = new DecimalFormat("##.#");
						exterieur_temperature_textView.setText(f.format((double)temperature/100)+" °C");
					}
					else{
						exterieur_temperature_textView.setText("X °C");
					}
				}
				else if(characteristic == activity.getHumidityCharacteristic()[0]){
					int humidity = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
					if(humidity != 10500){//Temperature reading error
						DecimalFormat f = new DecimalFormat("##.#");
						interieur_humidity_1_textView.setText(f.format((double)humidity/100)+" %");
					}
					else{
						interieur_humidity_1_textView.setText("X %");
					}
				}
				else if(characteristic == activity.getHumidityCharacteristic()[1]){
					int humidity = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
					if(humidity != 10500){//Temperature reading error
						DecimalFormat f = new DecimalFormat("##.#");
						exterieur_humidity_textView.setText(f.format((double)humidity/100)+" %");
					}
					else{
						exterieur_humidity_textView.setText("X %");
					}
				}
				else if(characteristic == activity.getWeightCharacteristic()){
					int weight = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
					if(weight != 20460) {
						DecimalFormat f = new DecimalFormat("###.#");
						interieur_weight_textView.setText(f.format((double)weight/200)+" kg");
					}
					else{
						interieur_weight_textView.setText("X kg");
					}
				}
				else if(characteristic == activity.getBatteryCharacteristic()){
					int battery = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) ;
					if(battery >0 && battery <= 100) {
						interieur_battery_progressBar.setProgressText(battery+" %");
						interieur_battery_progressBar.setProgress(battery);
						interieur_battery_progressBar.setProgressColor((Integer)new ArgbEvaluator().evaluate(((float)battery)/100, 0xffff0000, 0xff00ff00));
					}
					else{
						interieur_battery_progressBar.setProgressText("X %");
						interieur_battery_progressBar.setProgress(0);
					}
				}
				else if(characteristic == activity.getLightCharacteristic()){
					byte[] rawValue = characteristic.getValue();
					int value = ((rawValue[2] & 0xFF) << 16) | ((rawValue[1] & 0xFF) << 8) | (rawValue[0] & 0xFF);
					if(value != 1281435) {
						exterieur_light_textView.setText(value/100+" lux");
					}
					else{
						exterieur_light_textView.setText("X lux");
					}
				}

			}
		});
	}

	public void resetFragment(){
		MainActivity activity = (MainActivity) getActivity();
		if(activity==null){
			return;
		}
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				exterieur_temperature_textView.setText("- °C");
				exterieur_humidity_textView.setText("- %");
				exterieur_light_textView.setText("- lux");

				interieur_battery_progressBar.setProgressText("- %");
				interieur_battery_progressBar.setProgress(0);
			}
		});
	}
}