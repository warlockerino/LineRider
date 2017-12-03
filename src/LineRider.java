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
    static LightSensor light = new LightSensor(SensorPort.S3);
    static int light_aktuell=-1;
    static int weiss_wert=50;
    //static int weiss_wert=200;

    public static void main(String[] args) throws InterruptedException{
    	//while(!Button.ENTER.isPressed()){
          //        Thread.sleep(1000);
           //}
    
           LCD.clear();
           light.setFloodlight(true);
           init_motoren(150);
    
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
		light_aktuell=light.getLightValue();
		
		if(light_aktuell<weiss_wert) {
			links.stop();
			rechts.stop();
			
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
	
	while(light.getLightValue() < weiss_wert) {
		links.stop();
		rechts.stop();
		return;
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
			
			if(forwardAndCheck(2000)) {
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
				
				if(forwardAndCheck(4000)) {
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
			
			light_aktuell=light.getLightValue();
            //LCD.drawInt(light_aktuell, 1, 3);
            
            if(light_aktuell>weiss_wert){        //wenn Farbe wei� nach rechts drehen
                    //LCD.drawString("linksrum", 1, 3);

                links.forward();
                rechts.stop();

            	//links.stop();
            	//rechts.forward();
            }
            // linie gefunden
            else{                        //wenn nicht wei� nach links drehen
            	links.stop();
                rechts.forward();
            	
            	//links.forward();
            	//rechts.stop();
            }
		}
     }
}
}
