import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;
import lejos.nxt.addon.ColorHTSensor;
import lejos.robotics.Color;


public class LineRider {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Farbsensor
		// Port 3 empfohlen
		ColorHTSensor cs = new ColorHTSensor(SensorPort.S3);
		// Entfernunggmesser
		UltrasonicSensor sonar=new UltrasonicSensor(SensorPort.S1);
		sonar.continuous();
		
		for (int i=0; i <= 10; i++){
			Color capture =cs.getColor();
			LCD.drawString("Distanz: "+  sonar.getDistance(), 0, 0);
			
			LCD.drawString("Rot  : "+  capture.getRed(), 0,2);
			LCD.drawString("Gruen: "+  capture.getGreen(), 0,3);
			LCD.drawString("Blau : "+  capture.getBlue(), 0,4);
			
	        Button.waitForAnyPress();
	        LCD.clear();

		}

	}

}
