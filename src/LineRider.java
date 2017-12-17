import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.nxt.TouchSensor;
import lejos.nxt.UltrasonicSensor;
import lejos.nxt.addon.ColorHTSensor;
import lejos.robotics.Color;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.util.Delay;


public class LineRider {
    static DifferentialPilot pilot;
    static UltrasonicSensor sonic_right = new UltrasonicSensor(SensorPort.S1);
    static UltrasonicSensor sonic_left = new UltrasonicSensor(SensorPort.S4);    
    static LightSensor light = new LightSensor(SensorPort.S3); // rechts
    static LightSensor light2 = new LightSensor(SensorPort.S2); // links
    static int light_right=-1;
    static int light_left=-1;
    static int weiss_wert=51;//51
    //static int weiss_wert=200;
    static int default_motor_strength = 200;
    static int distance_threshold_value = 15;
    
    private static boolean isStopped = false;
    private static boolean isSearching = false;
    private static boolean isMovingToExit = false;
    
    public static void runUltraSonicTest() {
    	while(true) {
    		LCD.clear();
    		LCD.drawString(Integer.toString(sonic_left.getDistance()), 0, 1);
    		LCD.drawString(Integer.toString(sonic_right.getDistance()), 0, 2);  
    		Delay.msDelay(500);
    	}
    }
    
    public static void runLightSensorTest() {
    	while(true) {
    		Button.waitForAnyPress();
    		LCD.drawString(Integer.toString(light.getLightValue()), 0, 1);
    	}
    }
    
    public static void main(String[] args) throws InterruptedException{
           LCD.clear();
           light.setFloodlight(true);
           light2.setFloodlight(true);
    
           NXTRegulatedMotor links = new NXTRegulatedMotor(MotorPort.B);
           NXTRegulatedMotor rechts = new NXTRegulatedMotor(MotorPort.A);
           links.setSpeed(default_motor_strength);
           rechts.setSpeed(default_motor_strength);
           pilot = new DifferentialPilot(56, 56, 115, links, rechts, false);
           pilot.setRotateSpeed(180);
           pilot.setTravelSpeed(120);
           //Button.waitForAnyPress();
           //runLightSensorTest();
           //runUltraSonicTest();

           linie_folgen();
}

public static void forward(int ms) {
	pilot.forward();
	
	Delay.msDelay(ms);
	
	pilot.stop();
}

public static void backward(int ms) {
	pilot.backward();
	
	Delay.msDelay(ms);
	
	pilot.stop();
}

public static void forwardTimed(int ms) {
	pilot.forward();
	
	Delay.msDelay(ms);
	
	pilot.stop();
}

static final int foo = 20;

public static boolean forwardAndCheck(int ms) {
	int remainingMs = ms;
	
	pilot.forward();
	
	while(remainingMs > 0) {
		boolean leftIsLineColor = isLeftLineColor();
        boolean rightIsLineColor = isRightLineColor();
        
		if(leftIsLineColor || rightIsLineColor) {
			Sound.beepSequence();
			pilot.stop();
			//pilot.rotate(15);
			
			return true;
		}
		
		Delay.msDelay(foo);
		remainingMs -= foo;
	}
	
	pilot.stop();
	
	return false;
}

public static void forwardUntilLine() {
	//links.forward();
	//rechts.forward();
	pilot.forward();
	
	while(true) {
        boolean leftIsLineColor = isLeftLineColor(); //left_weiss
        boolean rightIsLineColor = isRightLineColor();
        
		if(leftIsLineColor && !rightIsLineColor) {
			//links.stop();
			//rechts.stop();
			pilot.stop();
			
			return;
		}
		
		if(!leftIsLineColor && !rightIsLineColor) {
			//##links.backward();
			//##rechts.forward();
			pilot.rotate(15);
			
			return;
		}
		
		if(!leftIsLineColor && rightIsLineColor) {
			//##links.backward();
			pilot.rotate(15);
			
			return;
		}
	}
}

public static void rotateLeft() {
	pilot.rotate(90);
}

public static void rotateRight() {
	pilot.rotate(-90);
}

public static boolean isLineColor(int colorValue) {
	return colorValue < weiss_wert;
	//return colorValue > weiss_wert;
}

public static void handleSearchEnd() {
	pilot.stop();
	isSearching = false;
	
	backward(100);
	
	//links.rotate(-135);
	pilot.rotate(70);
	
	//links.rotate(-90, true);
	//rechts.rotate(90);
	
	//forwardUntilLine();
}

public static boolean hindernisErkannt() {
	return sonic_left.getDistance()<=distance_threshold_value || sonic_right.getDistance()<=distance_threshold_value;
}

public static void linie_folgen(){     
	while(true) {
		// alles gestoppt, nichts mehr tun
		if(isStopped) {
			pilot.stop();
			Delay.msDelay(1000);
		}
		else if(isMovingToExit) {
			pilot.forward();
			
			if(hindernisErkannt()) {
				pilot.stop();
				return;
			}
		}
		else if(isSearching) {
			// Nach links umfahren

			Sound.beep();
			// links lang "strafen" bis kein Hindernis auf beiden Seiten mehr erkannt wurde
			while(hindernisErkannt()) {
				rotateLeft();
				forwardTimed(1500);
				rotateRight();
			}
			
			// Etwas vorw‰rts fahren und nach rechts zum Objekt hin drehen
			
			//forwardTimed(2000);
			if(forwardAndCheck(2000)) {
				Sound.buzz();
				handleSearchEnd();
				continue;
			}
			rotateRight();
			
			// links lang "strafen" bis kein Hindernis auf beiden Seiten mehr erkannt wurde
			while(hindernisErkannt()) {
				Sound.beep();
				rotateLeft();
				//forwardTimed(1000);
				if(forwardAndCheck(1500)) { //false && 
					Sound.buzz();
					//return;
					handleSearchEnd();
					break;
				}
				//forwardTimed(1000);
				rotateRight();
			}
		} else {
			if(sonic_left.getDistance()<=distance_threshold_value || sonic_right.getDistance()<=distance_threshold_value) {
				isSearching = true;
				continue;
			}

            boolean leftIsLineColor = isLeftLineColor();
            boolean rightIsLineColor = isRightLineColor();
            
            followLineLeftSide(leftIsLineColor, rightIsLineColor);
            //followLineRightSide(leftIsLineColor, rightIsLineColor);
		}
     }
  }

public static int steerCount = 0;

	public static void followLineLeftSide(boolean leftIsLineColor, boolean rightIsLineColor) {
		if(steerCount > 10) { //12
			Sound.buzz();
			isMovingToExit = true;
			while(true) {
				pilot.steer(-200);
	        	Delay.msDelay(30);
	        	
	        	leftIsLineColor = isLeftLineColor(); //left_weiss
	            rightIsLineColor = isRightLineColor();
	            
	            if(!leftIsLineColor && rightIsLineColor) {
	    			pilot.rotate(180);
	            	
	            	return;
	            }
			}
		}
		
		if(rightIsLineColor && !leftIsLineColor){
			steerCount = 0;
        	pilot.steer(-200);
        	Delay.msDelay(30);           	
        } else if(rightIsLineColor && leftIsLineColor){
        	steerCount = 0;
        	pilot.steerBackward(-200);
        	Delay.msDelay(30);
        } else if(!rightIsLineColor && !leftIsLineColor){
        	steerCount++;
        	pilot.steer(-200); // nach links drehen
        	Delay.msDelay(30);
    	}
        // linie gefunden
        else{                        //wenn nicht weiﬂ nach links drehen
        	steerCount = 0;
            pilot.forward();
        }
	}
	
	public static void followLineRightSide(boolean leftIsLineColor, boolean rightIsLineColor) {
		if(!rightIsLineColor && leftIsLineColor){
        	pilot.steer(200);
        	Delay.msDelay(30);           	
        } else if(rightIsLineColor && leftIsLineColor){
        	pilot.steerBackward(200);
        	Delay.msDelay(30);
        	//pilot.forward();
        } else if(!rightIsLineColor && !leftIsLineColor){
        	pilot.steer(200); // nach rechts drehen
        	Delay.msDelay(30);
    	}
        // linie gefunden
        else{
            pilot.forward();
        }
	}
	
	public static boolean isLeftLineColor() {
		light_left = light.getLightValue();
		return isLineColor(light_left);
	}
	
	public static boolean isRightLineColor() {
		light_right = light2.getLightValue();	
		return isLineColor(light_right);
	}
}
