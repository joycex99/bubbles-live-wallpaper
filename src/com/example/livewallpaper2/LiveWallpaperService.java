package com.example.livewallpaper2;

import java.util.ArrayList;
import java.util.Random;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
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
import android.service.wallpaper.WallpaperService;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.WindowManager;

@SuppressLint("NewApi")
public class LiveWallpaperService extends WallpaperService {

	@Override
	public Engine onCreateEngine() {
		return new WallpaperEngine();
	}
	
	private class WallpaperEngine extends Engine implements SensorEventListener {
		private boolean visible = false;
		private final Handler handler = new Handler();
		private int sw, sh;
		Random rand = new Random();
		ArrayList<Circle> circles;
		ArrayList<Circle> popped;
		
		private final int RANDOM = 0;
		private final int BOTTOM = 1;
		private final int BELOW = 2;
		
		private SensorManager sensorManager;
		private Sensor accelerometer;
		private Vibrator v;
		private float changeThreshold;
		private float lastX, lastY;
		private float deltaX, deltaY;
		
		//TEST
		
		private final Runnable updateDisplay = new Runnable() {
			@Override
			public void run() {
				draw();
			}
		};
		
		public WallpaperEngine() {
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
			if (backgroundCount <= 5 || backgroundCount >= 14) 
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
			int color; // = colors[rand.nextInt(4)];
			if (x < sw/3) {
				color = 0xff7DF0AC;
			} else if (x < 2*sw/3) {
				color = 0xff7DDAF0;
			} else {
				color = 0xffC993F5;
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
		
		/*public int[] idealPos() {
			int[] idealSpot = new int[2];
			
			//LEFTMOST SECTION
			if (mostEmpty() <= 3) {
				idealSpot[0] = rand.nextInt(sw/3);
				switch (mostEmpty()) {
				case 0:
					idealSpot[1] = rand.nextInt(sh/4);
					break;
				case 1:
					idealSpot[1] = rand.nextInt(sh/4)+sh/4;
					break;
				case 2:
					idealSpot[1] = rand.nextInt(sh/4)+sh/2;
					break;
				case 3: 
					idealSpot[1] = rand.nextInt(sh/4)+3*sh/4;
					break;
				}
			}
			//CENTER SECTION
			else if (mostEmpty() <= 7) {
				idealSpot[0] = rand.nextInt(sw/3)+(sw/3);
				switch (mostEmpty()) {
				case 4:
					idealSpot[1] = rand.nextInt(sh/4);
					break;
				case 5:
					idealSpot[1] = rand.nextInt(sh/4)+sh/4;
					break;
				case 6:
					idealSpot[1] = rand.nextInt(sh/4)+sh/2;
					break;
				case 7: 
					idealSpot[1] = rand.nextInt(sh/4)+3*sh/4;
					break;
				}
			}
			//RIGHT SECTION
			else {
				idealSpot[0] = rand.nextInt(sw/3)+(2*sw/3);
				switch (mostEmpty()) {
				case 8:
					idealSpot[1] = rand.nextInt(sh/4);
					break;
				case 9:
					idealSpot[1] = rand.nextInt(sh/4)+sh/4;
					break;
				case 10:
					idealSpot[1] = rand.nextInt(sh/4)+sh/2;
					break;
				case 11: 
					idealSpot[1] = rand.nextInt(sh/4)+3*sh/4;
					break;
				}
			}
			return idealSpot;
		}
		public int mostEmpty() {
			//int a1 = 0, a2 = 0, a3 = 0, a4 = 0, b1 = 0, b2 = 0, b3 = 0, b4 = 0, c1 = 0, c2 = 0, c3 = 0, c4 = 0;
			int[] sections = new int[12];
			for (Circle c : circles) {
				if (c.getX() < sw/3) { //in left section
					if (c.getY() < sh/4) //top quarter
						sections[0]++;
					else if (c.getY() < sh/2) //second quarter
						sections[1]++;
					else if (c.getY() < 3*sh/4) //third quarter
						sections[2]++;
					else //fourth quarter
						sections[3]++;
				} else if (c.getX() < 2*sw/3) { //in center
					if (c.getY() < sh/4) 
						sections[4]++;
					else if (c.getY() < sh/2) 
						sections[5]++;
					else if (c.getY() < 3*sh/4) 
						sections[6]++;
					else 
						sections[7]++;
				} else { //right section
					if (c.getY() < sh/4) 
						sections[8]++;
					else if (c.getY() < sh/2) 
						sections[9]++;
					else if (c.getY() < 3*sh/4) 
						sections[10]++;
					else 
						sections[11]++;
				}
			}	
			return minValIndex(sections);
		}
		
		public int minValIndex(int[] nums) {
			ArrayList<Integer> allMin = new ArrayList<>();
			
			//find minimum value
			int minVal = nums[0];
			for (int i = 1; i < nums.length; i++) {
				if (nums[i] < minVal)
					minVal = nums[i];
			}
			
			//find all regions with that minimum value and add to arraylist
			for (int i = 0; i < nums.length; i++) {
				if (nums[i] == minVal)
					allMin.add(i); //REMEMBER: adding INDEX (i.e. region), not actual value (i.e. minimum # of circles)
			}
			
			//randomly select from arraylist
			int randomIndex = rand.nextInt(allMin.size()); //select index
			return allMin.get(randomIndex); //return value at index, i.e. the region
		}*/
		
		private void draw() {
			SurfaceHolder holder = getSurfaceHolder();
			Canvas c = null;
			try {
				c = holder.lockCanvas();
				if (c != null) {
					c.drawColor(Color.BLACK);
					Paint paint = new Paint();
					paint.setShader(new RadialGradient(sw/2, sh/2, sw/2, 0xff010B91, Color.BLACK, Shader.TileMode.CLAMP));
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
			super.onTouchEvent(e);
		}
		
		
		public double distance (int x0, int y0, int x1, int y1) {
			return Math.sqrt(Math.pow(x0-x1, 2) + Math.pow(y0-y1, 2));
		}
	
		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);
			
			sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
			accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
			changeThreshold = (float) (accelerometer.getMaximumRange()/1.4);
			
			Context ctx = getApplicationContext();
			v = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
        }
		
		@Override
		public void onVisibilityChanged(boolean visible) {
			this.visible = visible;
			if (visible) {
				sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
				handler.post(updateDisplay);
			} else {
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
		}
		
		@Override
		public void onDestroy() {
			super.onDestroy();
			this.visible = false;
			handler.removeCallbacks(updateDisplay);
			sensorManager.unregisterListener(this);
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
				//v.vibrate(100);
				shakeCount = 0;
				
				for (int i = 0; i < rand.nextInt(3)+1; i++) { //do it 1-3 times
					int index = rand.nextInt(circles.size());
					while (circles.get(index).isBackground()) { //repick if index is background bubble
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
	}

}
