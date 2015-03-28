package com.example.livewallpaper2;

import java.util.ArrayList;
import java.util.Random;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.WindowManager;

@SuppressLint("NewApi")
public class LiveWallpaperService extends WallpaperService {

	@Override
	public Engine onCreateEngine() {
		return new WallpaperEngine(this);
	}
	
	private class WallpaperEngine extends Engine implements SensorEventListener, OnSharedPreferenceChangeListener {
		private final Context context;
		
		private boolean visible = false;
		private final Handler handler = new Handler();
		private int sw, sh;
		Random rand = new Random();
		ArrayList<Circle> circles;
		ArrayList<Circle> popped;
		
		private final int RANDOM = 0;
		private final int BOTTOM = 1;
		private final int BELOW = 2;
		
		private Vibrator v;
		private SensorManager sensorManager;
		private Sensor accelerometer;
		private float changeThreshold;
		private float lastX, lastY;
		private float deltaX, deltaY;
		
		//PREFERENCES
		SharedPreferences prefs;
		private boolean touchEnabled;
		private String theme = "";
		private String prevTheme = "";
		
		//THEME COLOR SETS
		private final int[] theme1Colors = {0xff7DF0AC, 0xff7DDAF0, 0xffC993F5, 0xff010B91, Color.BLACK}; //left, center, right, backgroundColor1, backgroundColor2
		private final int[] theme2Colors = {0xffD9D9D9, 0xffE8E1AC, 0xffE8D25A, Color.BLACK, Color.BLACK};
		private final int[] uhoh = {Color.BLACK};
		
		//TEST
		
		private final Runnable updateDisplay = new Runnable() {
			@Override
			public void run() {
				draw();
			}
		};
		
		public WallpaperEngine(Context c) {
			this.context = c;
		}
		
		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);
			
			sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
			accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
			changeThreshold = (float) (accelerometer.getMaximumRange()/1.5);
			
			prefs = PreferenceManager.getDefaultSharedPreferences(context);
			prefs.registerOnSharedPreferenceChangeListener(this);
			touchEnabled = prefs.getBoolean("touch", true);
			theme = prefs.getString("themes", "default string");
		    
			v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
			
			Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
			Point size = new Point();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
				display.getRealSize(size);
				sw = size.x;
				sh = size.y;
			} else {
			display.getSize(size); 
				sw = size.x;
				sh = size.y;
			}
			
			circles = new ArrayList<>();
			for (int i = 0; i < 20; i++) {
				circles.add(generateCircle(RANDOM));
			}
			
			while (!evenlyDistributed(circles)) {
				circles = new ArrayList<>();
				for (int i = 0; i < 20; i++) {
					circles.add(generateCircle(RANDOM));
				}
			}
			
			popped = new ArrayList<>();
        }
		
		
		public boolean evenlyDistributed(ArrayList<Circle> circles) {
			int backgroundCount = 0;
			int left = 0;
			int center = 0;
			int right = 0;
			for (Circle circ : circles) {
				if (circ.isBackground())
					backgroundCount++;
				if (circ.getX() < sw/3)	
					left++;
				else if (circ.getX() >= sw/3 && circ.getX() <= 2*sw/3)
					center++;
				else if (circ.getX() > 2*sw/3)
					right++;
			}
			if (backgroundCount <= 7 || backgroundCount >= 13) 
				return false;
			if (left <= 5 || left >= 9 || center <= 5 || center >= 9 || right <= 5 || right >= 9)
				return false;
			return true;
		}
		
		public Circle generateCircle(int type) {
			//int x = idealPos()[0];
			//int y = idealPos()[1];
			int x = rand.nextInt(sw);
			int y = rand.nextInt(sh);
			int r = rand.nextInt(100)+60;
			int color; 
			
			/**THEME 1: DEFAULT**/
			if (theme.equals("1")) {
				if (x < sw/3) {
					color = theme1Colors[0];
				} else if (x < 2*sw/3) {
					color = theme1Colors[1];
				} else {
					color = theme1Colors[2];
				}
			}
			
			/**THEME 2: SILVER & GOLD**/
			else if (theme.equals("2")) {
				if (x < sw/3) {
					color = theme2Colors[0];
				} else if (x < 2*sw/3) {
					color = theme2Colors[1];
				} else {
					color = theme2Colors[2];
				}
			}
			
			/**SHOULD NEVER HAPPEN*/
			else {
				color = uhoh[0];
			}
			
			int velocity = rand.nextInt(4)+3;
			boolean background = rand.nextInt(2)==0 ? true : false;
			int alpha;
			if (background) 
				alpha = rand.nextInt(100)+70;
			else 
				alpha = rand.nextInt(80)+100;
			
			
			if (type == RANDOM) {
				return new Circle(x, y, r, color, alpha, velocity, background);
			} else if (type == BOTTOM) {
				return new Circle(x, sh+r, r, color, alpha, velocity, background);
			}
			return new Circle(x, sh+r+rand.nextInt(10)+10, r, color, alpha, velocity, background);
		}
		
		
		
		private void draw() {
			SurfaceHolder holder = getSurfaceHolder();
			Canvas c = null;
			try {
				c = holder.lockCanvas();
				if (c != null) {
					c.drawColor(Color.BLACK);
					Paint paint = new Paint();
					int backgroundColor1, backgroundColor2;
					if (theme.equals("1")) {
						backgroundColor1 = theme1Colors[3];
						backgroundColor2 = theme1Colors[4];
						paint.setShader(new RadialGradient(sw/2, sh/2, sw/2, backgroundColor1, backgroundColor2, Shader.TileMode.CLAMP));
					} else if (theme.equals("2")) {
						backgroundColor1 = theme2Colors[3];
						backgroundColor2 = theme2Colors[4];
						paint.setShader(new LinearGradient(0, 2*sh/3, sw, sh/3, backgroundColor1, backgroundColor2, Shader.TileMode.CLAMP));
					} else { //SHOULD NEVER HAPPEN
						backgroundColor1 = Color.WHITE;
						backgroundColor2 = Color.WHITE;
					}
					
						
					//paint.setShader(new RadialGradient(sw/2, sh/2, sw/2, backgroundColor1, backgroundColor2, Shader.TileMode.CLAMP));
					c.drawRect(0, 0, sw, sh, paint);
					
					
					for (int i = 0; i < circles.size(); i++) {
						Circle circle = circles.get(i);
						
						Paint p = new Paint();
						p.setColor(circle.getColor());
						p.setAlpha(circle.getAlpha());
						
						//is foreground bubble
						if (!circle.isBackground()) {
							c.drawCircle(circle.getX(), circle.getY(), circle.getR()+4, p);
							
							//outer edge
							p.setStyle(Paint.Style.STROKE);
							p.setColor(0xccffffff);
							p.setStrokeWidth(8);
							c.drawCircle(circle.getX(), circle.getY(), circle.getR(), p);
				
						} 
						//is background bubble
						else {
							p.setMaskFilter(new BlurMaskFilter(rand.nextInt(4)+9, BlurMaskFilter.Blur.NORMAL));
							c.drawCircle(circle.getX(), circle.getY(), circle.getR(), p);
						}
						
						circle.setY(circle.getY()-circle.getVelocity());
						
						//at top: remove; generate new circle(s)
						if (circle.getY() + circle.getR() < 0) {
							int seed = rand.nextInt(5);
							if (seed == 0) {
								//don't generate new
							} else if (seed == 1){
								// generate 2
								circles.add(generateCircle(BOTTOM));
								circles.add(generateCircle(BELOW));
							} else {
								circles.add(generateCircle(BOTTOM));
							}
							if (i != 0)
								i--;
							circles.remove(circle);
						}
						
						if (circle.isToBePopped()) {
							circles.remove(circle);
							popped.add(circle);
							if (i != 0)
								i--;
							
							//spawn new
							Circle newCircle = generateCircle(RANDOM);
							newCircle.setBackground(false);
							circles.add(newCircle);
						}
					}
					
					//for each circle just popped, draw sparks, spawn new circle
					for (int i = 0; i < popped.size(); i++) {
						drawPop(c, popped.get(i));
					}
					
				}
			} finally {
				if (c != null)
					holder.unlockCanvasAndPost(c);
			}
			
			
			handler.removeCallbacks(updateDisplay);
			if (visible) {
				handler.postDelayed(updateDisplay, 10);
			}
		}
		
		private void drawPop(Canvas c, Circle circle) {
			Paint p = new Paint();
			//fade away: set alpha of color equal to popCount field in circle
			int newColor = Color.argb(circle.getPopCount(), Color.red(circle.getColor()), Color.green(circle.getColor()), Color.blue(circle.getColor()));
			circle.setColor(newColor);
			p.setMaskFilter(new BlurMaskFilter(2, BlurMaskFilter.Blur.NORMAL));
			p.setColor(circle.getColor());
			if (!circle.hasSparks()) { //just popped, generate sparks
				ArrayList<int[]> newSparks = generateSparks(circle);
				circle.setHasSparks(true);
				circle.sparks = newSparks;
			} 
			for (int i = 0; i < circle.sparks.size(); i++) {
				int cx = circle.sparks.get(i)[0];
				int cy = circle.sparks.get(i)[1];
				int cr = circle.sparks.get(i)[2];
				c.drawCircle(cx, cy, cr, p);
				int cvy = circle.sparks.get(i)[3];
				//increase fall speed gradually
				if (circle.getPopCount() >= 200)
					circle.sparks.get(i)[1] += cvy*(255/circle.getPopCount());
				else
					circle.sparks.get(i)[1] += cvy*2;
			}
			
			//decrease popCount (i.e. decrease transparency of color of sparks)
			circle.setPopCount(circle.getPopCount()-8);
			
			//stop drawing sparks
			if (circle.getPopCount() <= 15) {
				popped.remove(circle);
			}
		}
		
		
		public ArrayList<int[]> generateSparks(Circle circle) {
			ArrayList<int[]> sparks = new ArrayList<int[]>();
			for (int i = 0; i < rand.nextInt(3)+circle.getR()/10; i++) {
				int cx = circle.getX()-circle.getR()+rand.nextInt(2*circle.getR()); //anywhere horizontally within circle range
				int cy = circle.getY()-circle.getR()+rand.nextInt(2*circle.getR()); //anywhere vertically within circle range
				int cr = rand.nextInt(4)+4;
				int cvy = rand.nextInt(6)+3;
				int[] spark = {cx, cy, cr, cvy};
				sparks.add(spark);
			}
			return sparks;
		}
		
		
		@Override
		public void onTouchEvent(MotionEvent e) {
			if (touchEnabled) {
				float x = e.getX();
				float y = e.getY();
				for (int i = 0; i < circles.size(); i++) {
					Circle circle = circles.get(i);
					if (!circle.isBackground()) {
						if (distance((int)x, (int)y, circle.getX(), circle.getY()) < circle.getR()) {
							circle.setToBePopped(true);
							break;
						}
					}
				}
			}
			super.onTouchEvent(e);
		}
		
		
		public double distance (int x0, int y0, int x1, int y1) {
			return Math.sqrt(Math.pow(x0-x1, 2) + Math.pow(y0-y1, 2));
		}
	
		@Override
		public void onVisibilityChanged(boolean visible) {
			this.visible = visible;
			if (visible) {
				if (sensorManager != null)
					sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
				handler.post(updateDisplay);
			} else {
				if (sensorManager != null)
					sensorManager.unregisterListener(this);
				handler.removeCallbacks(updateDisplay);
			}
		}
		
		
		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			super.onSurfaceChanged(holder, format, width, height);
		}
		
		@Override 
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			super.onSurfaceDestroyed(holder);
			this.visible = false;
			handler.removeCallbacks(updateDisplay);
			sensorManager.unregisterListener(this);
			prefs.unregisterOnSharedPreferenceChangeListener(this);
		}
		
		@Override
		public void onDestroy() {
			super.onDestroy();
			this.visible = false;
			handler.removeCallbacks(updateDisplay);
			sensorManager.unregisterListener(this);
			prefs.unregisterOnSharedPreferenceChangeListener(this);
		}

		int shakeCount = 0;
		@Override
		public void onSensorChanged(SensorEvent event) {
			deltaX = Math.abs(lastX-event.values[0]);
			deltaY = Math.abs(lastY-event.values[1]);
			
			lastX = event.values[0];
			lastY = event.values[1];
			
			if (deltaX > changeThreshold || deltaY > changeThreshold) {
				shakeCount++;
			}
			
			if (shakeCount >= 2) {
				shakeCount = 0;
				
				for (int i = 0; i < rand.nextInt(3)+1; i++) { //do it 1-3 times
					int index = rand.nextInt(circles.size());
					while (circles.get(index).isBackground()) { //re-pick if index is background bubble
						index = rand.nextInt(circles.size());
					}
					circles.get(index).setToBePopped(true);
				}
				
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			if (key.equals("touch")) {
				touchEnabled = prefs.getBoolean("touch", true);
			}
			if (key.equals("shake")) {
				boolean shake = prefs.getBoolean("shake", true);
				if (shake) {
					sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
					sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
				}
				else {
					sensorManager.unregisterListener(this);
					sensorManager = null;
				}
			}
			if (key.equals("themes")) {
				prevTheme = theme; //set previous theme to current theme
				theme = prefs.getString("themes", "default"); //set current theme to changed theme
				changeTheme(prevTheme, theme);
			}
		}
		
		public void changeTheme(String previous, String current) {
			int[] oldC = colorsOfTheme(previous);
			int[] newC = colorsOfTheme(current);
			for (Circle c : circles) {
				if (c.getColor() == oldC[0])
					c.setColor(newC[0]);
				else if (c.getColor() == oldC[1])
					c.setColor(newC[1]);
				else if (c.getColor() == oldC[2])
					c.setColor(newC[2]);
				else
					c.setColor(Color.RED);
			}
		}
		
		public int[] colorsOfTheme(String theme) {
			switch (theme) {
			case "1":
				return theme1Colors;
			case "2":
				return theme2Colors;
			default:
				return new int[3];
			}
		}

	}
	
}
