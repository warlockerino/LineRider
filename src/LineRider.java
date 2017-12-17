import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.nxt.UltrasonicSensor;
import lejos.nxt.addon.ColorHTSensor;
import lejos.robotics.Color;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.util.Delay;


public class LineRider { 
	//static NXTRegulatedMotor links = new NXTRegulatedMotor(MotorPort.B);
    //static NXTRegulatedMotor rechts = new NXTRegulatedMotor(MotorPort.A);
    static DifferentialPilot pilot;
    static UltrasonicSensor sonic_right = new UltrasonicSensor(SensorPort.S1);
    static UltrasonicSensor sonic_left = new UltrasonicSensor(SensorPort.S4);    
    static LightSensor light = new LightSensor(SensorPort.S3); // rechts
    static LightSensor light2 = new LightSensor(SensorPort.S2); // links
    static int light_right=-1;
    static int light_left=-1;
    static int weiss_wert=51;
    //static int weiss_wert=200;
    static int default_motor_strength = 200;

    public static void main(String[] args) throws InterruptedException{
    	//while(!Button.ENTER.isPressed()){
          //        Thread.sleep(1000);
           //}
    
           LCD.clear();
           light.setFloodlight(true);
           light2.setFloodlight(true);
           //init_motoren(default_motor_strength);
    
           //135
           NXTRegulatedMotor links = new NXTRegulatedMotor(MotorPort.B);
           NXTRegulatedMotor rechts = new NXTRegulatedMotor(MotorPort.A);
           links.setSpeed(default_motor_strength);
           rechts.setSpeed(default_motor_strength);
           pilot = new DifferentialPilot(56, 56, 115, links, rechts, false);
           pilot.setRotateSpeed(180);
//           pilot.setTravelSpeed(100);
           pilot.setTravelSpeed(120);
           Button.waitForAnyPress();

           //pilot.forward();
           
           //Delay.msDelay(2000);
           
           //pilot.stop();
           
           /*pilot.rotate(90);
           
           Delay.msDelay(1000);
           
           pilot.rotate(90);
           
           Delay.msDelay(1000);
           
           pilot.rotate(90);
           
           Delay.msDelay(1000);
           
           pilot.rotate(90);*/
           
           linie_folgen();
}
public static void init_motoren(int speed){
            //links.setSpeed(speed);
            //rechts.setSpeed(speed);
    }

private static boolean isStopped = false;
private static boolean isSearching = false;

public static void forward(int ms) {
	//links.forward();
	//rechts.forward();
	pilot.forward();
	
	Delay.msDelay(ms);
	
	//links.stop();
	//rechts.stop();
	pilot.stop();
}

public static void backward(int ms) {
	//links.backward();
	//rechts.backward();
	pilot.backward();
	
	Delay.msDelay(ms);
	
	//links.stop();
	//rechts.stop();
	pilot.stop();
}

public static void forwardTimed(int ms) {
	pilot.forward();
	
	Delay.msDelay(ms);
	
	pilot.stop();
}

public static boolean forwardAndCheck(int ms) {
	int remainingMs = ms;
	
	//links.forward();
	//rechts.forward();
	pilot.forward();
	
	while(remainingMs > 0) {
		light_right=light.getLightValue();			
		light_left=light2.getLightValue();

        //boolean left_weiss = light_right > weiss_wert;
        //boolean right_weiss = light_left > weiss_wert;
		boolean left_weiss = light_right < weiss_wert;
        boolean right_weiss = light_left < weiss_wert;
        
		if(left_weiss && !right_weiss) {
			//links.stop();
			//rechts.stop();
			pilot.stop();
			
			return true;
		}
		
		if(!left_weiss && !right_weiss) {
			//links.backward();
			//rechts.forward();
			pilot.rotate(15);
			
			return true;
		}
		
		Delay.msDelay(50);
		remainingMs -= 50;
	}
	
	//links.stop();
	//rechts.stop();
	
	return false;
}

public static void forwardUntilDark() {
	//links.forward();
	//rechts.forward();
	pilot.forward();
	
	while(true) {
		light_right=light.getLightValue();			
		light_left=light2.getLightValue();

        //boolean left_weiss = light_right > weiss_wert;
        //boolean right_weiss = light_left > weiss_wert;
        boolean left_weiss = light_right < weiss_wert;
        boolean right_weiss = light_left < weiss_wert;
        
		if(left_weiss && !right_weiss) {
			//links.stop();
			//rechts.stop();
			pilot.stop();
			
			return;
		}
		
		if(!left_weiss && !right_weiss) {
			//##links.backward();
			//##rechts.forward();
			pilot.rotate(15);
			
			return;
		}
		
		if(!left_weiss && right_weiss) {
			//##links.backward();
			pilot.rotate(15);
			
			return;
		}
	}
}

public static void rotateLeft() {
	//links.rotate(-180, true);
	//rechts.rotate(180);
	pilot.rotate(90);
}

public static void rotateRight() {
	//links.rotate(180, true);
	//rechts.rotate(-180);
	pilot.rotate(-90);
}

public static void handleSearchEnd() {
	//links.stop();
	//rechts.stop();
	pilot.stop();
	isSearching = false;
	//Button.waitForAnyPress();
	
	backward(100);
	
	//links.rotate(-135);
	pilot.rotate(70);
	
	//links.rotate(-90, true);
	//rechts.rotate(90);
	
	forwardUntilDark();
}

public static void linie_folgen(){     
	while(true) {
		// alles gestoppt, nichts mehr tun
		if(isStopped) {
			//links.stop();
			//rechts.stop();
			pilot.stop();
			Delay.msDelay(1000);
		}
		else if(isSearching) {
			
			// Nach links umfahren
			
			// links lang "strafen" bis kein Hindernis auf beiden Seiten mehr erkannt wurde
			while(sonic_left.getDistance()<=15 || sonic_right.getDistance()<=15) {
				rotateLeft();
				forwardTimed(1000);
				rotateRight();
			}
			
			//Sound.buzz();
			
			// Etwas vorw�rts fahren und nach rechts zum Objekt hin drehen
			
			forwardTimed(1000);
			rotateRight();
			
			// links lang "strafen" bis kein Hindernis auf beiden Seiten mehr erkannt wurde
			while(sonic_left.getDistance()<=15 || sonic_right.getDistance()<=15) {

				rotateLeft();
				forwardTimed(1000);
				if(forwardAndCheck(500)) {
					//isStopped = true;
					//continue;
					//isSearching = false;
					//backward(100);
					handleSearchEnd();
					break;
				}
				rotateRight();
			}
			
			//Sound.buzz();
			
		} else {
            //init_motoren(default_motor_strength);
            
			if(sonic_left.getDistance()<=15 || sonic_right.getDistance()<=15) {
				Sound.beep();
				isSearching = true;
				continue;
			}
			
			light_right = light.getLightValue();			
			light_left = light2.getLightValue();

            boolean leftIsLineColor = light_right < weiss_wert; //left_weiss
            boolean rightIsLineColor = light_left < weiss_wert;
            
            //LCD.drawInt(light_right, 1, 3); //57 nicht-linie, 49 linie
            
            followLineLeftSide(leftIsLineColor, rightIsLineColor);
            //followLineRightSide(leftIsLineColor, rightIsLineColor);
		}
     }
  }

	public static void followLineLeftSide(boolean leftIsLineColor, boolean rightIsLineColor) {
		if(rightIsLineColor && !leftIsLineColor){
        	//##rechts.forward();
        	//##links.stop();
        	pilot.steer(-200);
        	Delay.msDelay(30);
        	//pilot.rotate(5);
        	//pilot.forward();
        	//Delay.msDelay(50);            	
        } else if(rightIsLineColor && leftIsLineColor){
            //##links.forward();
            //##rechts.stop();
        	//pilot.steer(100);
        	pilot.steerBackward(-200);
        	Delay.msDelay(30);
        	//pilot.forward();
        } else if(!rightIsLineColor && !leftIsLineColor){
        	//##rechts.stop();
        	//##links.backward();
        	//pilot.steerBackward(100);
        	pilot.steer(-200); // nach links drehen
        	Delay.msDelay(30);
        	//pilot.rotate(-5);
        	//pilot.forward();
        	//Delay.msDelay(50);
    	}
        // linie gefunden
        else{                        //wenn nicht wei� nach links drehen
        	//Sound.beep();
        	//##forward(150);
            pilot.forward();
        	//pilot.steerBackward(200);
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
}
