import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;
import lejos.nxt.addon.ColorHTSensor;
import lejos.robotics.Color;
import lejos.util.Delay;


public class LineRider { 
	static NXTRegulatedMotor links = new NXTRegulatedMotor(MotorPort.B);
    static NXTRegulatedMotor rechts = new NXTRegulatedMotor(MotorPort.A);
    static UltrasonicSensor sonic = new UltrasonicSensor(SensorPort.S1);
    static LightSensor light = new LightSensor(SensorPort.S3); // rechts
    static LightSensor light2 = new LightSensor(SensorPort.S2); // links
    static int light_right=-1;
    static int light_left=-1;
    static int weiss_wert=45;
    //static int weiss_wert=200;

    public static void main(String[] args) throws InterruptedException{
    	//while(!Button.ENTER.isPressed()){
          //        Thread.sleep(1000);
           //}
    
           LCD.clear();
           light.setFloodlight(true);
           light2.setFloodlight(true);
           init_motoren(150);
    
           Button.waitForAnyPress();
           
           linie_folgen();
}
public static void init_motoren(int speed){
            links.setSpeed(speed);
            rechts.setSpeed(speed);
    }

private static boolean isStopped = false;
private static boolean isSearching = false;

public static void forward(int ms) {
	links.forward();
	rechts.forward();
	
	Delay.msDelay(ms);
	
	links.stop();
	rechts.stop();
}

public static void backward(int ms) {
	links.backward();
	rechts.backward();
	
	Delay.msDelay(ms);
	
	links.stop();
	rechts.stop();
}

public static boolean forwardAndCheck(int ms) {
	int remainingMs = ms;
	
	links.forward();
	rechts.forward();
	
	while(remainingMs > 0) {
		light_right=light.getLightValue();			
		light_left=light2.getLightValue();

        //boolean left_weiss = light_right > weiss_wert;
        //boolean right_weiss = light_left > weiss_wert;
		boolean left_weiss = light_right < weiss_wert;
        boolean right_weiss = light_left < weiss_wert;
        
		if(left_weiss && !right_weiss) {
			links.stop();
			rechts.stop();
			
			return true;
		}
		
		if(!left_weiss && !right_weiss) {
			links.backward();
			rechts.forward();
			
			return true;
		}
		
		Delay.msDelay(50);
		remainingMs -= 50;
	}
	
	links.stop();
	rechts.stop();
	
	return false;
}

public static void forwardUntilDark() {
	links.forward();
	rechts.forward();
	
       
	while(true) {
		light_right=light.getLightValue();			
		light_left=light2.getLightValue();

        //boolean left_weiss = light_right > weiss_wert;
        //boolean right_weiss = light_left > weiss_wert;
        boolean left_weiss = light_right < weiss_wert;
        boolean right_weiss = light_left < weiss_wert;
        
		if(left_weiss && !right_weiss) {
			links.stop();
			rechts.stop();
			
			return;
		}
		
		if(!left_weiss && !right_weiss) {
			links.backward();
			rechts.forward();
			
			return;
		}
		
		if(!left_weiss && right_weiss) {
			links.backward();
			
			return;
		}
	}
}

public static void rotateLeft() {
	links.rotate(-180, true);
	rechts.rotate(180);
}

public static void rotateRight() {
	links.rotate(180, true);
	rechts.rotate(-180);
}

public static void handleSearchEnd() {
	links.stop();
	rechts.stop();
	isSearching = false;
	//Button.waitForAnyPress();
	
	backward(100);
	
	links.rotate(-135);
	
	//links.rotate(-90, true);
	//rechts.rotate(90);
	
	forwardUntilDark();
}

public static void linie_folgen(){     
	while(true) {
		// alles gestoppt, nichts mehr tun
		if(isStopped) {
			links.stop();
			rechts.stop();
			Delay.msDelay(1000);
		}
		else if(isSearching) {
			
			// suche zuende?
			//if(sonic.getDistance()>=15) {
			//	isSearching = false;
			//	continue;
			//}
			
			rotateLeft();
			
			//links.forward();
			//rechts.forward();
			
			//Delay.msDelay(2000);
			
			if(forwardAndCheck(1000)) {
				//isStopped = true;
				//continue;
				//isSearching = false;
				//backward(100);
				handleSearchEnd();
				continue;
			}
			
			rotateRight();
			
			// weg frei
			if(sonic.getDistance()>=15) {
				// extraplatz verschaffen 
				rotateLeft();
				
				//links.forward();
				//rechts.forward();
				
				//Delay.msDelay(1000);
				
				if(forwardAndCheck(1000)) {
					//isStopped = true;
					//continue;
					//isSearching = false;
					//backward(100);
					handleSearchEnd();
					continue;
				}
				
				rotateRight();
				
				// extraplatz verschaffen #2
				
				//links.forward();
				//rechts.forward();
				
				//Delay.msDelay(4000);
				
				if(forwardAndCheck(2000)) {
					//isStopped = true;
					//continue;
					//isSearching = false;
					//backward(100);
					handleSearchEnd();
					continue;
				}
				
				rotateRight();
				
				//isSearching = false;
				//isStopped = true;
				//continue;
			}
		} else {
			if(sonic.getDistance()<=15) {
				isSearching = true;
				continue;
			}
			
			light_right=light.getLightValue();			
			light_left=light2.getLightValue();

			LCD.drawInt(light_left, 1, 1);
			
            //boolean left_weiss = light_right > weiss_wert;
            //boolean right_weiss = light_left > weiss_wert;
            boolean left_weiss = light_right < weiss_wert;
            boolean right_weiss = light_left < weiss_wert;
            
            if(right_weiss && !left_weiss){
            	rechts.forward();
            	links.stop();
            } else if(right_weiss && left_weiss){
                links.forward();
                rechts.stop();
            } else if(!right_weiss && !left_weiss){
            	rechts.stop();
            	links.backward();

        	}
            // linie gefunden
            else{                        //wenn nicht weiß nach links drehen
            	forward(150);
            }
		}
     }
}
}
