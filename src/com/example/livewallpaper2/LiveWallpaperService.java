package com.example.livewallpaper2;

import java.util.ArrayList;
import java.util.Random;

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

public class LiveWallpaperService extends WallpaperService {

	@Override
	public Engine onCreateEngine() {
		return new WallpaperEngine();
	}
	
	private class WallpaperEngine extends Engine implements SensorEventListener, OnSharedPreferenceChangeListener {
		//private final Context context;
		
		private boolean visible = false;
		private final Handler handler = new Handler();
		private int sw, sh, sf; //width, height, scaleFactor
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
		private final int[] theme2Colors = {0xffD6D6D6, 0xffF0E48D, 0xffFFDF12, 0xff2A0266, Color.BLACK};
		private final int[] theme3Colors = {0xffDBDBDB, 0xffDE2A2A, 0xff7A002B, 0xff30000F, Color.BLACK};
		private final int[] theme4Colors = {0xff87EDAD, 0xff72F7E6, 0xff82B4FA, 0xff97A4C9, 0xff3F4159};
		private final int[] theme5Colors = {0xffEC3F8C, 0xff39B1C6, 0xff1FD26A, 0xff32313B};
		private final int[] theme6Colors = {0xffAABDB5, 0xff70A193, 0xff84C2C1, 0xff4E7A6E};
		private final int[] theme7Colors = {0xffADA0AC, 0xffA38BA3, 0xff896D95, 0xff49374A};
		private final int[] uhoh = {Color.BLACK};
		
		
		private final Runnable updateDisplay = new Runnable() {
			@Override
			public void run() {
				draw();
			}
		};
		
		public WallpaperEngine() { }
		
		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);
			
			sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
			accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
			changeThreshold = (float) (accelerometer.getMaximumRange()/1.5);
			
			prefs = PreferenceManager.getDefaultSharedPreferences(LiveWallpaperService.this);
			prefs.registerOnSharedPreferenceChangeListener(this);
			touchEnabled = prefs.getBoolean("touch", true);
			theme = prefs.getString("themes", "default string");
		    
			v = (Vibrator) LiveWallpaperService.this.getSystemService(Context.VIBRATOR_SERVICE);
			
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
			
			if (sw < sh) //portrait/phone
				sf = sw;
			else //landscape/tablet
				sf = sh;
			
			circles = new ArrayList<>();
			for (int i = 0; i < 18; i++) {
				circles.add(generateCircle(RANDOM));
			}
			
			while (!evenlyDistributed(circles)) {
				circles = new ArrayList<>();
				for (int i = 0; i < 18; i++) {
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
			if (backgroundCount <= 6 || backgroundCount >= 12) 
				return false;
			if (left <= 4 || left >= 8 || center <= 4 || center >= 8 || right <= 4 || right >= 8)
				return false;
			return true;
		}
		
		public Circle generateCircle(int type) {
			
			/** FOR SCREEN SIZE SCALING **/
			float radVar = (float)0.09*sf;
			float radBase = (float)0.065*sf;
			float velVar = ((float)(0.005*sf) != 0) ? (float)(0.005*sf) : 1; //0.0045
			float velBase = ((float)(0.0028*sf) != 0) ? (float)(0.0028*sf) : (float)0.5; //0.003
			
			float x = rand.nextInt(sw);
			float y = rand.nextInt(sh);
			float r = rand.nextFloat()*radVar+radBase;
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
			
			/**THEME 3: RUBY **/
			else if (theme.equals("3")) {
				if (x < sw/3) {
					color = theme3Colors[0];
				} else if (x < 2*sw/3) {
					color = theme3Colors[1];
				} else {
					color = theme3Colors[2];
				}
			}
			
			/**THEME 4: LIGHT **/
			else if (theme.equals("4")) {
				if (x < sw/3) {
					color = theme4Colors[0];
				} else if (x < 2*sw/3) {
					color = theme4Colors[1];
				} else {
					color = theme4Colors[2];
				}
			}
			
			/**THEME 5: MATERIAL **/
			else if (theme.equals("5")) {
				if (x < sw/3) {
					color = theme5Colors[0];
				} else if (x < 2*sw/3) {
					color = theme5Colors[1];
				} else {
					color = theme5Colors[2];
				}
			}
			
			/**THEME 6: MONOCHROMATIC GREEN **/
			else if (theme.equals("6")) {
				if (x < sw/3) {
					color = theme6Colors[0];
				} else if (x < 2*sw/3) {
					color = theme6Colors[1];
				} else {
					color = theme6Colors[2];
				}
			} 
			
			/**THEME 7: MONOCHROMATIC PURPLE **/
			else if (theme.equals("7")) {
				if (x < sw/3) {
					color = theme7Colors[0];
				} else if (x < 2*sw/3) {
					color = theme7Colors[1];
				} else {
					color = theme7Colors[2];
				}
			}
			
			/**SHOULD NEVER HAPPEN*/
			else {
				color = uhoh[0];
			}
			
			float velocity = rand.nextFloat()*velVar+velBase;
			boolean background = rand.nextInt(2)==0 ? true : false;
			int alpha;
			if (Integer.parseInt(theme) != 5) {
				if (background) 
					alpha = rand.nextInt(100)+70;
				else 
					alpha = rand.nextInt(80)+100;
			} else {
				if (background) 
					alpha = rand.nextInt(90)+110;
				else 
					alpha = rand.nextInt(60)+170;
			} 
			
			
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
						paint.setShader(new LinearGradient(0, 0, sw, 2*sh/3, backgroundColor1, backgroundColor2, Shader.TileMode.CLAMP));
					} else if (theme.equals("3")) {
						backgroundColor1 = theme3Colors[3];
						backgroundColor2 = theme3Colors[4];
						paint.setShader(new LinearGradient(0, sh, sw, sh/2, backgroundColor1, backgroundColor2, Shader.TileMode.CLAMP));
					} else if (theme.equals("4")) {
						backgroundColor1 = theme4Colors[3];
						backgroundColor2 = theme4Colors[4];
						paint.setShader(new RadialGradient(sw/2, sh/2, sh/2, backgroundColor1, backgroundColor2, Shader.TileMode.CLAMP));
					} else if (theme.equals("5")) {
						backgroundColor1 = theme5Colors[3];
						paint.setColor(backgroundColor1);
					} else if (theme.equals("6")) {
						backgroundColor1 = theme6Colors[3];
						paint.setColor(backgroundColor1);
					} else if (theme.equals("7")) {
						backgroundColor1 = theme7Colors[3];
						paint.setColor(backgroundColor1);
					}
					
					
					c.drawRect(0, 0, sw, sh, paint);
					
					
					for (int i = 0; i < circles.size(); i++) {
						Circle circle = circles.get(i);
						
						Paint p = new Paint();
						p.setColor(circle.getColor());
						p.setAlpha(circle.getAlpha());
						
						//is foreground bubble
						if (!circle.isBackground()) {
							c.drawCircle(circle.getX(), circle.getY(), circle.getR()+(int)(0.0037*sf), p);
							
							//outer edge
							p.setStyle(Paint.Style.STROKE);
							if (theme.equals("5"))
								p.setColor(circle.getColor());
							else
								p.setColor(0xccffffff);
							p.setStrokeWidth((int)(0.0074*sf));
							if (Integer.parseInt(theme) >= 6)
								p.setAlpha(50);
							c.drawCircle(circle.getX(), circle.getY(), circle.getR(), p);
				
						} 
						//is background bubble
						else {
							if ((int)(0.0037*sf) != 0)
								p.setMaskFilter(new BlurMaskFilter(rand.nextInt((int)(0.0037*sf))+(int)(0.0083*sf), BlurMaskFilter.Blur.NORMAL));
							else
								if ((int)(0.0083*sf) != 0)
									p.setMaskFilter(new BlurMaskFilter((int)(0.0083*sf), BlurMaskFilter.Blur.NORMAL));
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
				handler.postDelayed(updateDisplay, 15);
			}
		}
		
		private void drawPop(Canvas c, Circle circle) {
			Paint p = new Paint();
			//fade away: set alpha of color equal to popCount field in circle
			int newColor = Color.argb(circle.getPopCount(), Color.red(circle.getColor()), Color.green(circle.getColor()), Color.blue(circle.getColor()));
			circle.setColor(newColor);
			if (sf > 800)
				p.setMaskFilter(new BlurMaskFilter(2, BlurMaskFilter.Blur.NORMAL));
			p.setColor(circle.getColor());
			if (!circle.hasSparks()) { //just popped, generate sparks
				ArrayList<float[]> newSparks = generateSparks(circle);
				circle.setHasSparks(true);
				circle.sparks = newSparks;
			} 
			for (int i = 0; i < circle.sparks.size(); i++) {
				float cx = circle.sparks.get(i)[0];
				float cy = circle.sparks.get(i)[1];
				float cr = circle.sparks.get(i)[2];
				c.drawCircle(cx, cy, cr, p);
				float cvy = circle.sparks.get(i)[3];
				//increase fall speed gradually
				if (circle.getPopCount() >= 200)
					circle.sparks.get(i)[1] += cvy*(255/circle.getPopCount());
				else
					circle.sparks.get(i)[1] += cvy*2;
			}
			
			//decrease popCount (i.e. decrease transparency of color of sparks)
			circle.setPopCount(circle.getPopCount()-10);
			
			//stop drawing sparks
			if (circle.getPopCount() <= 15) {
				popped.remove(circle);
			}
		}
		
		
		public ArrayList<float[]> generateSparks(Circle circle) {
			
			/**FOR SCREEN SIZE SCALING**/
			int scaledRad = ((int)(0.004*sf) != 0) ? (int)(0.004*sf) : 1;
			int scaledVelVar = ((int)(0.0055*sf) != 0) ? (int)(0.0055*sf) : 1;
			int scaledVelBase = ((int)(0.003*sf) != 0) ? (int)(0.003*sf) : 1;
			
			ArrayList<float[]> sparks = new ArrayList<>();
			for (int i = 0; i < rand.nextInt(3)+circle.getR()/10; i++) {
				float cx = circle.getX()-circle.getR()+rand.nextInt((int)(2*circle.getR())); //anywhere horizontally within circle range
				float cy = circle.getY()-circle.getR()+rand.nextInt((int)(2*circle.getR())); //anywhere vertically within circle range
				float cr = rand.nextInt(scaledRad)+scaledRad;
				float cvy = rand.nextInt(scaledVelVar)+scaledVelBase;
				float[] spark = {cx, cy, cr, cvy};
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
						if (distance((int)x, (int)y, (int)circle.getX(), (int)circle.getY()) < (int)circle.getR()) {
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
			case "3":
				return theme3Colors;
			case "4":
				return theme4Colors;
			case "5":
				return theme5Colors;
			case "6":
				return theme6Colors;
			case "7":
				return theme7Colors;
			default:
				return new int[3];
			}
		}

	}
	
}
