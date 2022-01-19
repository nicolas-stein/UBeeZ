package fr.polytech.ubeez.intro;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroFragment;

import fr.polytech.ubeez.R;

public class IntroActivity extends AppIntro {

	private AlertDialog permissionAlertDialog;
	private IntroBluetoothConnectFragment introBluetoothConnectFragment;
	private BluetoothDevice lastBluetoothDevice = null;

	private String intentExtraString;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		intentExtraString = getIntent().getStringExtra("type");

		if(intentExtraString!=null && intentExtraString.equals("permission")) {
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				addSlide(AppIntroFragment.newInstance(
						"Permissions",
						"Afin de communiquer avec la ruche en bluetooth, l'application UBeeZ a besoin de l'autorisation d'utiliser le bluetooth.",
						R.drawable.ic_permission_shield,
						ContextCompat.getColor(this, R.color.colorPrimaryDark),
						ContextCompat.getColor(this, R.color.white),
						ContextCompat.getColor(this, R.color.white)));
			}

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
				askForPermissions(new String[] {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, 1, true);
			}
			else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
				askForPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1, true);
			}
			else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
				askForPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, 1, true);
			}
		}
		else{
			addSlide(AppIntroFragment.newInstance("Bienvenue !",
					"L'application UBeeZ vous permet de vous connecter à la ruche, afficher les données des capteurs, modifier la configuration.",
					R.drawable.ic_intro_bee,
					ContextCompat.getColor(this, R.color.colorSecondaryDark),
					ContextCompat.getColor(this, R.color.white),
					ContextCompat.getColor(this, R.color.white)));

			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				addSlide(AppIntroFragment.newInstance(
						"Permissions",
						"Afin de communiquer avec la ruche en bluetooth, l'application UBeeZ a besoin de l'autorisation d'utiliser le bluetooth.",
						R.drawable.ic_permission_shield,
						ContextCompat.getColor(this, R.color.colorPrimaryDark),
						ContextCompat.getColor(this, R.color.white),
						ContextCompat.getColor(this, R.color.white)));
			}

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
				askForPermissions(new String[] {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, 2, true);
			}
			else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
				askForPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 2, true);
			}
			else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
				askForPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, 2, true);
			}

			addSlide(IntroBluetoothSearchFragment.newInstance());
			introBluetoothConnectFragment = IntroBluetoothConnectFragment.newInstance();
			addSlide(introBluetoothConnectFragment);
		}

		//showStatusBar(true);
		//setImmersiveMode();
		setImmersive(true);
		setColorTransitionsEnabled(true);
		setSystemBackButtonLocked(true);
		setWizardMode(true);
		setSkipButtonEnabled(false);
		setIndicatorEnabled(true);
		setBarColor(ContextCompat.getColor(this, android.R.color.black));
		setIndicatorColor(ContextCompat.getColor(this, android.R.color.white), ContextCompat.getColor(this, android.R.color.darker_gray));
		//setNavBarColorRes(R.color.colorPrimaryDark);
	}

	public void doneBluetoothSearching(BluetoothDevice bluetoothDevice){
		goToNextSlide(false);
		introBluetoothConnectFragment.connectToHive(bluetoothDevice);
	}

	@Override
	protected void onUserDeniedPermission(@NonNull String permissionName) {
		super.onUserDeniedPermission(permissionName);
		if(permissionAlertDialog==null || !permissionAlertDialog.isShowing()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Refus des permissions")
					.setMessage("Vous avez refusé 1ère fois") //TODO
					.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			permissionAlertDialog = builder.create();
			permissionAlertDialog.show();
		}
	}

	@Override
	protected void onUserDisabledPermission(@NonNull String permissionName) {
		super.onUserDisabledPermission(permissionName);
		if(permissionAlertDialog==null || !permissionAlertDialog.isShowing()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Refus des permissions")
					.setMessage("Vous avez refusé à vie") //TODO
					.setPositiveButton("Ouvrir les paramètres", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
							Uri uri = Uri.fromParts("package", getPackageName(), null);
							intent.setData(uri);
							startActivity(intent);
						}
					})
					.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			permissionAlertDialog = builder.create();
			permissionAlertDialog.show();
		}
	}

	@Override
	protected void onDonePressed(Fragment currentFragment) {
		super.onDonePressed(currentFragment);
		if(intentExtraString!=null && intentExtraString.equals("permission")){

		}
		else{
			SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.preference_filename), MODE_PRIVATE).edit();
			editor.putString(getString(R.string.preference_BLE_addr), introBluetoothConnectFragment.getBluetoothGatt().getDevice().getAddress());
			editor.apply();
		}
		finish();
	}

	@Override
	public void onBackPressed() {
		finishAffinity();
	}
}
