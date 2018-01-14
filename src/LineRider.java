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
    static int distance_threshold_value = 17; //15
    
    private static boolean isStopped = false;
    private static boolean isSearchingLeft = false;
    private static boolean isSearchingRight = false;
    private static boolean isMovingToExit = false;
    
    private static boolean isLeftGuided = true;
    
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
    
    public static void runRotationTest() {
    	while(true) {
    		rotateLeft();
    		Delay.msDelay(500);
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
           pilot = new DifferentialPilot(56, 56, 115, links, rechts, false); //115  138
           pilot.setRotateSpeed(90); //180
           pilot.setTravelSpeed(120);
           //Button.waitForAnyPress();
           //runLightSensorTest();
           //runUltraSonicTest();
           //runRotationTest();
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

public static final int FOUND_NOTHING = 0;
public static final int FOUND_LINE = 16;
public static final int FOUND_OBJECT = 32;

public static int forwardAndCheck(int ms) {
	int remainingMs = ms;
	
	pilot.forward();
	
	while(remainingMs > 0) {
		boolean leftIsLineColor  = isLeftLineColor();
        boolean rightIsLineColor = isRightLineColor();
        
		if(leftIsLineColor || rightIsLineColor) {
			Sound.beepSequence();
			pilot.stop();
			//pilot.rotate(15);
			
			return FOUND_LINE;
		}
		
		Delay.msDelay(foo);
		remainingMs -= foo;
	}
	
	pilot.stop();
	
	return FOUND_OBJECT;
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
	//return colorValue < weiss_wert;
	return colorValue > weiss_wert;
}

public static void handleSearchEnd() {
	pilot.stop();
	
	if(isSearchingLeft) {
		isLeftGuided = true;
		isSearchingLeft = false;
	} else {
		isLeftGuided = false;
		isSearchingRight = false;		
	}
	
	// TODO verify search end turning direction
	// i.e. (vllt condition vertauschen)
	// if (isLeftGuided) {
	//		pilot.rotate(70);
	//	else
	//		pilot.rotate(-70);
	
	
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

public static boolean hindernisErkanntLang() {
	int leftValue = sonic_left.getDistance();
	int rightValue = sonic_right.getDistance();
	//LCD.clear();
	//LCD.drawString(Integer.toString(leftValue), 0, 1);
	//LCD.drawString(Integer.toString(rightValue), 0, 2);
	//Button.waitForAnyPress();
	return leftValue<=distance_threshold_value+5 || rightValue<=distance_threshold_value+5;
}

public static boolean hindernisErkanntLangBoth() {
	int leftValue = sonic_left.getDistance();
	int rightValue = sonic_right.getDistance();
	//LCD.clear();
	//LCD.drawString(Integer.toString(leftValue), 0, 1);
	//LCD.drawString(Integer.toString(rightValue), 0, 2);
	//Button.waitForAnyPress();
	return leftValue<=distance_threshold_value+5 && rightValue<=distance_threshold_value+5;
}

public static boolean hindernisErkanntExtraLang() {
	int leftValue = sonic_left.getDistance();
	int rightValue = sonic_right.getDistance();
	LCD.clear();
	LCD.drawString(Integer.toString(leftValue), 0, 1);
	LCD.drawString(Integer.toString(rightValue), 0, 2);
	Button.waitForAnyPress();
	return leftValue<=50 || rightValue<=50;
}

public static void ausrichten() {
	int leftDistance  = sonic_left.getDistance();
	int rightDistance = sonic_right.getDistance();
	
	while(leftDistance != rightDistance) {
		if(leftDistance > 50 || rightDistance > 50)
			break;
		if (leftDistance > rightDistance) {
			pilot.rotate(-8);
		} else {
			pilot.rotate(8);
		}
		leftDistance  = sonic_left.getDistance();
		rightDistance = sonic_right.getDistance();
	}
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
		else if(isSearchingLeft) {
			// Nach links umfahren

			Sound.beep();
			// links lang "strafen" bis kein Hindernis auf beiden Seiten mehr erkannt wurde
			boolean hindernisBoth = false;
			while(hindernisErkanntLang()) {
				hindernisBoth = hindernisErkanntLangBoth();
				rotateLeft();
				forwardTimed(1500);
				rotateRight();
		        ausrichten();
			}
			
			if (hindernisBoth) {	
				rotateLeft();
				forwardTimed(1000);
				rotateRight();
			}
			
			// Etwas vorwärts fahren und nach rechts zum Objekt hin drehen
			
			//forwardTimed(2000);
			int x = forwardAndCheck(2000);
			if(x == FOUND_LINE) {
				Sound.buzz();
				handleSearchEnd();
				continue;
			}
			rotateRight();
			ausrichten();
			
			// links lang "strafen" bis kein Hindernis auf beiden Seiten mehr erkannt wurde
			while(hindernisErkanntLang()) {
				hindernisBoth = hindernisErkanntLangBoth();
				Sound.beep();
				rotateLeft();
				//forwardTimed(1000);
				x = forwardAndCheck(1500);
				if(x == FOUND_LINE) { //false && 
					Sound.buzz();
					//return;
					handleSearchEnd();
					break;
				}
				//forwardTimed(1000);
		        ausrichten();
				rotateRight();
			}

			if (hindernisBoth) {	
				rotateLeft();
				forwardTimed(1000);
				rotateRight();
			}
			
		} else if(isSearchingRight) {
			// Nach links umfahren

			Sound.beep();
			// links lang "strafen" bis kein Hindernis auf beiden Seiten mehr erkannt wurde
			boolean hindernisBoth = false;
			while(hindernisErkanntLang()) {
				hindernisBoth = hindernisErkanntLangBoth();
				rotateRight();
				forwardTimed(1500);
				rotateLeft();
				ausrichten();
			}

			if (hindernisBoth) {
			rotateRight();
			forwardTimed(1000);
			rotateLeft();
			}
			
			// Etwas vorwärts fahren und nach rechts zum Objekt hin drehen
			
			//forwardTimed(2000);
			int x = forwardAndCheck(2000);
			if(x == FOUND_LINE) {
				Sound.buzz();
				handleSearchEnd();
				continue;
			}
			rotateLeft();
			ausrichten();
			
			// links lang "strafen" bis kein Hindernis auf beiden Seiten mehr erkannt wurde
			while(hindernisErkanntLang()) {
				hindernisBoth = hindernisErkanntLangBoth();
				Sound.beep();
				rotateRight();
				//forwardTimed(1000);
				x = forwardAndCheck(1500);
				if(x == FOUND_LINE) { //false && 
					Sound.buzz();
					//return;
					handleSearchEnd();
					break;
				}
				//forwardTimed(1000);
				rotateLeft();
				ausrichten();
			}
			
			if (hindernisBoth) {	
				rotateRight();
				forwardTimed(1000);
				rotateLeft();
			}
		} else {
			if(sonic_left.getDistance()<=distance_threshold_value || sonic_right.getDistance()<=distance_threshold_value) {
				Sound.buzz();
				
				/*int testRotateAngle = 35;
				
				// try left
				pilot.rotate(testRotateAngle);
				int leftTestValue = sonic_left.getDistance();
				if(leftTestValue > 200) {
					isSearchingLeft = true;
					pilot.rotate(-testRotateAngle);
					continue;
				}
				pilot.rotate(-testRotateAngle);
				// try right
				pilot.rotate(-testRotateAngle);
				int rightTestValue = sonic_right.getDistance();
				if(rightTestValue > 200) {
					isSearchingRight = true;
					pilot.rotate(testRotateAngle);
					continue;
				}
				pilot.rotate(testRotateAngle);*/

				//TODO: maybe drive back a bit?
				
				backward(500); //500

		        ausrichten();				
				int testRotateAngle = 35;
				//int testRotateAngle = 45;
				
				pilot.rotate(testRotateAngle);
				int leftTestValue = sonic_left.getDistance();
				pilot.rotate(-testRotateAngle * 2);
				int rightTestValue = sonic_right.getDistance();
				pilot.rotate(testRotateAngle);
				
				// try left

				if(rightTestValue > leftTestValue) {
					isSearchingRight = true;
					continue;
				} else {
					// defaulting to right
					isSearchingLeft = true;
					continue;
				}
			}

            boolean leftIsLineColor = isLeftLineColor();
            boolean rightIsLineColor = isRightLineColor();
            
            if(isLeftGuided)
            	followLineLeftSide(leftIsLineColor, rightIsLineColor);
            else
            	followLineRightSide(leftIsLineColor, rightIsLineColor);
		}
     }
  }

public static int steerCount = 0;
public static int steerCountThreshold = 12; // 10 12

	public static void followLineLeftSide(boolean leftIsLineColor, boolean rightIsLineColor) {
		if(steerCount > steerCountThreshold) {
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
        else{                        //wenn nicht weiß nach links drehen
        	steerCount = 0;
            pilot.forward();
        }
	}
	
	public static void followLineRightSide(boolean leftIsLineColor, boolean rightIsLineColor) {
		if(steerCount > steerCountThreshold) {
			Sound.buzz();
			isMovingToExit = true;
			while(true) {
				pilot.steer(200);
	        	Delay.msDelay(30);
	        	
	        	leftIsLineColor = isLeftLineColor(); //left_weiss
	            rightIsLineColor = isRightLineColor();
	            
	            if(leftIsLineColor && !rightIsLineColor) {
	    			pilot.rotate(180);
	            	
	            	return;
	            }
			}
		}
		
		if(!rightIsLineColor && leftIsLineColor){
			steerCount = 0;
        	pilot.steer(200);
        	Delay.msDelay(30);           	
        } else if(rightIsLineColor && leftIsLineColor){
        	steerCount = 0;
        	pilot.steerBackward(200);
        	Delay.msDelay(30);
        	//pilot.forward();
        } else if(!rightIsLineColor && !leftIsLineColor){
        	steerCount++;
        	pilot.steer(200); // nach rechts drehen
        	Delay.msDelay(30);
    	}
        // linie gefunden
        else{
        	steerCount = 0;
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
