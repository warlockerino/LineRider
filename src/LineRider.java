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
	// navigator
    static DifferentialPilot pilot;
    
    // component objects
    static UltrasonicSensor sonic_right = new UltrasonicSensor(SensorPort.S1);
    static UltrasonicSensor sonic_left = new UltrasonicSensor(SensorPort.S4);    
    static LightSensor light = new LightSensor(SensorPort.S3); // rechts
    static LightSensor light2 = new LightSensor(SensorPort.S2); // links
    
    // line-detection threshold value
    // calibrated, 45 or 51
    static int weiss_wert = 45;
    
    // 
    static int default_motor_strength = 200;
    
    //
    static int distance_threshold_value = 20;
    
    // state machine flags
    private static boolean isStopped = false;
    private static boolean isSearchingLeft = false;
    private static boolean isSearchingRight = false;
    private static boolean isMovingToExit = false;
    
    // movement variation flag
    // defines if robot is following the left or right edge of the line
    private static boolean isLeftGuided = true;
    
    public static void main(String[] args) throws InterruptedException {
       LCD.clear();
           light.setFloodlight(true);
           light2.setFloodlight(true);
    
           NXTRegulatedMotor links = new NXTRegulatedMotor(MotorPort.B);
           NXTRegulatedMotor rechts = new NXTRegulatedMotor(MotorPort.A);
           links.setSpeed(default_motor_strength);
           rechts.setSpeed(default_motor_strength);
           pilot = new DifferentialPilot(56, 56, 115, links, rechts, false); //115  138
           pilot.setRotateSpeed(90);
           pilot.setTravelSpeed(120);
           
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

static final int foo = 10;

public static final int FOUND_NOTHING = 0;
public static final int FOUND_LINE = 16;
public static final int FOUND_OBJECT = 32;

public static int forwardAndCheck(int ms) {
	return forwardAndCheck(ms, false);
}

public static int forwardAndCheck(int ms, boolean includeDistanceSensor) {
	int remainingMs = ms;
	
	pilot.forward();
	
	while(remainingMs > 0) {
		boolean leftIsLineColor  = isLeftLineColor();
        boolean rightIsLineColor = isRightLineColor();
        
		if(leftIsLineColor || rightIsLineColor) {
			pilot.stop();
			
			return FOUND_LINE;
		}
		if(includeDistanceSensor) {
			if(hindernisErkannt()) // lang??
			{ 
				pilot.stop();
				return FOUND_OBJECT;
			}				
		}
		
		Delay.msDelay(foo);
		remainingMs -= foo;
	}
	
	pilot.stop();
	
	return FOUND_NOTHING;
}

public static void forwardUntilLine() {
	pilot.forward();
	
	while(true) {
        boolean leftIsLineColor = isLeftLineColor();
        boolean rightIsLineColor = isRightLineColor();
        
		if(leftIsLineColor && !rightIsLineColor) {
			pilot.stop();
			
			return;
		}
		
		if(!leftIsLineColor && !rightIsLineColor) {
			pilot.rotate(15);
			
			return;
		}
		
		if(!leftIsLineColor && rightIsLineColor) {
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
	return colorValue > weiss_wert;
}

public static void handleSearchEnd() {
	pilot.stop();
	backward(100);
	if(isSearchingLeft) {
		isLeftGuided = true;
		isSearchingLeft = false;
		
		while(true) {
	        boolean leftIsLineColor = isLeftLineColor();
	        boolean rightIsLineColor = isRightLineColor();
	        
			if(leftIsLineColor && !rightIsLineColor) {
				Sound.beep();
				pilot.forward();
				Delay.msDelay(150);
				return;
			}
			
			if(!leftIsLineColor && !rightIsLineColor) {
	        	pilot.steer(50);
			} else if(!leftIsLineColor && rightIsLineColor) {
				pilot.steerBackward(-50);
			} else if(leftIsLineColor && rightIsLineColor) {
				pilot.backward();
			}
			Delay.msDelay(30);
		}
		
	} else {
		isLeftGuided = false;
		isSearchingRight = false;
		
		while(true) {
	        boolean leftIsLineColor  = isLeftLineColor();
	        boolean rightIsLineColor = isRightLineColor();
	        
			if(!leftIsLineColor && rightIsLineColor) {
				Sound.beep();
				pilot.forward();
				Delay.msDelay(150);
				return;
			}
			
			if(!leftIsLineColor && !rightIsLineColor) {
	        	pilot.steer(-50);
			} else if(!leftIsLineColor && rightIsLineColor) {
				pilot.steerBackward(50);
			} else if(leftIsLineColor && rightIsLineColor) {
				pilot.backward();
			}
			Delay.msDelay(30);
		}
	}
}

public static boolean hindernisErkannt() {
	return sonic_left.getDistance() <= distance_threshold_value || sonic_right.getDistance() <= distance_threshold_value;
}

public static boolean hindernisErkanntLang() {
	int leftValue = sonic_left.getDistance();
	int rightValue = sonic_right.getDistance();
	return leftValue <= distance_threshold_value + 5 || rightValue <= distance_threshold_value + 5;
}

public static boolean hindernisErkanntLangBoth() {
	int leftValue = sonic_left.getDistance();
	int rightValue = sonic_right.getDistance();
	return leftValue <= distance_threshold_value + 5 && rightValue <= distance_threshold_value + 5;
}

public static boolean hindernisErkanntExtraLang() {
	int leftValue = sonic_left.getDistance();
	int rightValue = sonic_right.getDistance();
	return leftValue <= 50 || rightValue <= 50;
}

public static void ausrichten() {
	// Diese Funktion wurde mit Absicht auskommentiert, da diese in dem derzeitigen Stand für mehr Probleme gesorgt
	// hat als sie löst. Das ausrichten ist prinzipiell jedoch eine gute Idee (mehr Details hierzu in der Dokumentation),
	// konnte jedoch bisher nicht erfolgreich implementiert werden da die Ultraschall-Sensoren hierzu einfach zu 
	// unverlässige Werte liefern bzw. in zu vielen Situationen (Schieflage etc.) nicht korrekt funktionieren (können).
	return;
	
	/*int leftDistance  = sonic_left.getDistance();
	int rightDistance = sonic_right.getDistance();	

	int leftTestValue = sonic_left.getDistance();
	int rightTestValue = sonic_right.getDistance();
	
	if(rightTestValue > 60 || leftTestValue > 60)
		return;

	int testRotateAngle = 16;
	pilot.rotate(testRotateAngle);
	leftTestValue = sonic_left.getDistance();
	pilot.rotate(-testRotateAngle * 2);
	rightTestValue = sonic_right.getDistance();
	pilot.rotate(testRotateAngle);
	
	// try left

	if(rightTestValue > 60 || leftTestValue > 60) {
		return;
	}
	
	while(leftDistance != rightDistance) {
		LCD.clear();
		LCD.drawString(Integer.toString(leftDistance), 0, 1);
		LCD.drawString(Integer.toString(rightDistance), 0, 2);
		//Button.waitForAnyPress();
		int diff = Math.abs(leftDistance - rightDistance);
		if(leftDistance > 40 || rightDistance > 40)
			break;
		//if (diff > 1) {
			if (leftDistance > rightDistance) {
				pilot.rotate(-8);
			} else {
				pilot.rotate(8);
			}
		//}
		leftDistance  = sonic_left.getDistance();
		rightDistance = sonic_right.getDistance();
	}*/
}

public static void linie_folgen(){     
	while(true) {
		if(isStopped) {
			// alles gestoppt, nichts mehr tun
			
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

			// links lang "strafen" bis kein Hindernis auf beiden Seiten mehr erkannt wurde
			boolean hindernisBoth = false;
			while(hindernisErkanntLang()) {
				hindernisBoth = hindernisErkannt();
				rotateLeft();
				forwardTimed(1500);
				rotateRight();
		        ausrichten();
			}
			
			if (hindernisBoth) {	
				rotateLeft();
				forwardTimed(500);
				rotateRight();
			}
			
			// Etwas vorwärts fahren und nach rechts zum Objekt hin drehen
			
			int x = forwardAndCheck(2000);
			if(x == FOUND_LINE) {
				handleSearchEnd();
				continue;
			}
			
			rotateRight();
			ausrichten();
			
			// links lang "strafen" bis kein Hindernis auf beiden Seiten mehr erkannt wurde
			while(hindernisErkanntLang()) {
				hindernisBoth = hindernisErkanntLangBoth();
				rotateLeft();
				x = forwardAndCheck(1500);
				if(x == FOUND_LINE) {
					handleSearchEnd();
					break;
				}
		        ausrichten();
				rotateRight();
			}

			if (hindernisBoth) {	
				rotateLeft();
				forwardTimed(500);
				rotateRight();
			}
			
		} else if(isSearchingRight) {
			// Nach rechts umfahren

			// links lang "strafen" bis kein Hindernis auf beiden Seiten mehr erkannt wurde
			boolean hindernisBoth = false;
			
				
			while(hindernisErkanntLang()) {
				hindernisBoth = hindernisErkannt();
				rotateRight();
				forwardTimed(1500);
				rotateLeft();
				ausrichten();
			}

			if (hindernisBoth) {
				rotateRight();
				forwardTimed(500);
				rotateLeft();
			}
			
			// Etwas vorwärts fahren und nach rechts zum Objekt hin drehen
			
			int x = forwardAndCheck(2000);
			if(x == FOUND_LINE) {
				handleSearchEnd();
				continue;
			}
			rotateLeft();
			ausrichten();
			
			// links lang "strafen" bis kein Hindernis auf beiden Seiten mehr erkannt wurde
			while(hindernisErkanntLang()) {
				hindernisBoth = hindernisErkanntLangBoth();
				rotateRight();
				x = forwardAndCheck(1500);
				if(x == FOUND_LINE) {
					handleSearchEnd();
					break;
				}
				rotateLeft();
				ausrichten();
			}
			
			if (hindernisBoth) {	
				rotateRight();
				forwardTimed(500);
				rotateLeft();
			}
		} else {
			if(sonic_left.getDistance()<=distance_threshold_value/2 || sonic_right.getDistance()<=distance_threshold_value/2) {
		        ausrichten();	
				backward(700);
			
				int testRotateAngle = 35;
				
				pilot.rotate(testRotateAngle);
				int leftTestValue = sonic_left.getDistance();
				pilot.rotate(-testRotateAngle * 2);
				int rightTestValue = sonic_right.getDistance();
				pilot.rotate(testRotateAngle);
				
				// try left

				ausrichten();
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
public static int steerCountThreshold = 14; // 10 12

	// follow the line at the left edge
	public static void followLineLeftSide(boolean leftIsLineColor, boolean rightIsLineColor) {
		if(steerCount > steerCountThreshold) {
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
	
	// follow the line at the right edge
	public static void followLineRightSide(boolean leftIsLineColor, boolean rightIsLineColor) {
		if(steerCount > steerCountThreshold) {
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
		int light_left = light.getLightValue();
		return isLineColor(light_left);
	}
	
	public static boolean isRightLineColor() {
		int light_right = light2.getLightValue();	
		return isLineColor(light_right);
	}
}
