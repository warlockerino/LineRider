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
	static NXTRegulatedMotor links = new NXTRegulatedMotor(MotorPort.A);
    static NXTRegulatedMotor rechts = new NXTRegulatedMotor(MotorPort.B);
    static UltrasonicSensor sonic = new UltrasonicSensor(SensorPort.S1);
    static LightSensor light = new LightSensor(SensorPort.S3);
    static int light_aktuell=-1;
    static int weiss_wert=50;

    public static void main(String[] args) throws InterruptedException{
    	//while(!Button.ENTER.isPressed()){
          //        Thread.sleep(1000);
           //}
    
           LCD.clear();
           light.setFloodlight(true);
           init_motoren(100);
    
           linie_folgen();
}
public static void init_motoren(int speed){
            links.setSpeed(speed);
            rechts.setSpeed(speed);
    }

private static boolean isStopped = false;
private static boolean isSearching = false;

public static void linie_folgen(){     
	while(true) {
		// alles gestoppt, nichts mehr tun
		if(isStopped) {
			Delay.msDelay(1000);
		}
		else if(isSearching) {
			links.rotate(-180, true);
			rechts.rotate(180);
			
			links.forward();
			rechts.forward();
			
			Delay.msDelay(2000);
			
			links.rotate(180, true);
			rechts.rotate(-180);
			
			if(sonic.getDistance()>=10) {
				isSearching = false;
				isStopped = true;
				continue;
			}
		} else {
			if(sonic.getDistance()<=10) {
				isSearching = true;
				continue;
			}
			
			light_aktuell=light.getLightValue();
            //LCD.drawInt(light_aktuell, 1, 3);
            
            if(light_aktuell>weiss_wert){        //wenn Farbe weiﬂ nach rechts drehen
                    //LCD.drawString("linksrum", 1, 3);
            	//links.setSpeed(120);
                //rechts.setSpeed(120);
            	
                    rechts.stop();
                    links.forward();
            }
            // linie gefunden
            else{                        //wenn nicht weiﬂ nach links drehen
                    //LCD.drawInt(light_aktuell, 1, 3);
            	//links.setSpeed(50);
                //rechts.setSpeed(120);
            	
                //rechts.forward();
                //links.forward();
            	
                    rechts.forward();
                    links.stop();
            }
		}
     }
}
}
