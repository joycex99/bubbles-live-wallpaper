package com.example.livewallpaper2;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends Activity {
	
	SharedPreferences prefs;
	String theme;
	ImageView background;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		theme = prefs.getString("themes", "default");
		background = (ImageView) findViewById(R.id.app_background);
		
		switch(Integer.parseInt(theme)) {
		case 1:
			background.setImageResource(R.drawable.background_theme_1);
			break;
		case 2:
			background.setImageResource(R.drawable.background_theme_2);
			break;
		case 3:
			background.setImageResource(R.drawable.background_theme_3);
			break;
		case 4:
			background.setImageResource(R.drawable.background_theme_4);
			break;
		case 5:
			background.setImageResource(R.drawable.background_theme_5);
			break;
		case 6:
			background.setImageResource(R.drawable.background_theme_6);
			break;
		case 7:
			background.setImageResource(R.drawable.background_theme_7);
			break;
		default:
			break;
		}
	}
	
	public void setWallpaper(View view) {
		Intent intent = new Intent();
		if (Build.VERSION.SDK_INT >= 16) {
			intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
			intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(this, LiveWallpaperService.class));
		} else {
			intent.setAction(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
		}
		startActivity(intent);
		
		background = (ImageView)findViewById(R.id.app_background);
		background.setImageResource(R.drawable.background_theme_2);
	}
	
	public void openSettings(View view) {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}
	
}
