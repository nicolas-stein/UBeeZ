package fr.polytech.ubeez;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MainFragmentStateAdapter extends FragmentStateAdapter {

	private Activity mainActivity;

	Fragment[] fragments;

	public MainFragmentStateAdapter(@NonNull FragmentActivity fragmentActivity) {
		super(fragmentActivity);
		mainActivity = fragmentActivity;
		fragments = new Fragment[3];
	}

	@NonNull
	@Override
	public Fragment createFragment(int position) {
		switch (position){
			case 0:
				fragments[0] = StatusFragment.newInstance();
				return fragments[0];
			case 1:
				fragments[1] = DataFragment.newInstance();
				return fragments[1];
		}
		fragments[2] = SettingsFragment.newInstance();
		return fragments[2];
	}

	@Override
	public int getItemCount() {
		return 3;
	}

	public Fragment getFragment(int position){
		return fragments[position];
	}
}
