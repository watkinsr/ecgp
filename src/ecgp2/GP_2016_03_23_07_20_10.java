package ecgp2;
import robocode.*;
import robocode.util.Utils;
import java.awt.Color;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
 import java.io.OutputStreamWriter;
 import java.io.FileOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.UnsupportedEncodingException;
 import java.io.FileWriter;
 import java.io.PrintStream;
 import robocode.AdvancedRobot; 
 import robocode.RobocodeFileOutputStream; 
public class GP_2016_03_23_07_20_10 extends AdvancedRobot {

 double fitness = 0.0 + -9.0; // Potential penalty here for tree growth
 FileWriter pw;
 String FITNESS_LOG_LOC = new String("/home/ryan/robocode/fitness.csv");

	public void run() {

		setAdjustGunForRobotTurn(true);

		setColors(Color.red,Color.blue,Color.green);
		while(true) {
			turnGunRight(Double.POSITIVE_INFINITY);
		}

	}
	public void onScannedRobot(ScannedRobotEvent e) {

 // --- PHENOME 1 ---
		setAhead(Math.random()*2 - 1 + Math.acos(Math.sin(Math.abs(e.getEnergy() )))/ Math.abs(Math.cos(Math.cos(Math.sin(Math.sin(Math.abs(e.getEnergy())))))));

 // --- PHENOME 2 ---
		setTurnRight(Math.abs(Math.abs(Math.acos(getRadarTurnRemainingRadians() + getWidth() * getVelocity()-getWidth() ))* Math.acos(Math.cos(Math.cos(getY() ))))* Math.abs(getRadarHeadingRadians()));

 // --- PHENOME 3 ---
		setTurnGunRight(Math.cos(Math.random()*2 - 1));

 // --- PHENOME 4 ---
		setTurnRadarRight(Math.cos(Math.sin(Math.abs(Math.toRadians(Math.floor((Math.random()*10)) - 0.01 )+ Math.toDegrees(getGunTurnRemainingRadians() )/ getGunHeadingRadians() / Math.cos(Math.sin(Math.asin(getGunHeadingRadians() )))/ Math.asin(Math.toDegrees(getGunHeadingRadians()))/getHeadingRadians() )))+ Math.cos(Math.toDegrees(Math.asin(e.getBearingRadians())+e.getVelocity() )/ Math.cos(Math.acos(Math.asin(getHeight() )))+ Math.sin(Math.acos(getGunTurnRemainingRadians() / Math.floor((Math.random()*10)) )* Math.sin(e.getVelocity() )))* e.getEnergy() * getHeading() * e.getDistance() - Math.cos(e.getHeadingRadians() )* Math.cos(Math.toDegrees(Math.asin(e.getBearingRadians())+e.getVelocity() )/ Math.cos(Math.acos(Math.asin(getHeight() )))+ Math.sin(Math.acos(getGunTurnRemainingRadians() / Math.floor((Math.random()*10)) )* Math.sin(e.getVelocity() )))* e.getEnergy() * getHeading() * e.getDistance() - Math.cos(e.getHeadingRadians() )* Math.toRadians(Math.abs(Math.floor((Math.random()*10)) )* Math.toDegrees(getY() ))/ Math.toRadians(Math.abs(Math.asin(e.getBearingRadians() + Math.random()*2 - 1 )))/ Math.toDegrees(Math.acos(Math.acos(getRadarHeadingRadians()))-getRadarTurnRemainingRadians() - Math.sin(getVelocity() )- Math.abs(getRadarTurnRemainingRadians() * getDistanceRemaining() ))/ Math.toRadians(Math.abs(Math.asin(e.getBearingRadians() + Math.random()*2 - 1 )))/ Math.toDegrees(Math.acos(Math.acos(getRadarHeadingRadians()))-getRadarTurnRemainingRadians() - Math.sin(getVelocity() )- Math.abs(getRadarTurnRemainingRadians() * getDistanceRemaining() )));

 // --- PHENOME 5 ---
		setFire(Math.cos(Math.abs(Math.abs(Math.sin(Math.asin(Math.sin(Math.toDegrees(Math.cos(Math.sin(0.01))))))))));

	}
 	public void onBulletHit(BulletHitEvent e) { 
			fitness += e.getBullet().getPower();
		}
 	public void onHitByBullet(HitByBulletEvent e) { 
			fitness -= e.getBullet().getPower();
		}
     public void onBulletMissed(BulletMissedEvent e) {
         fitness -= e.getBullet().getPower() / 2 ;
 	} 
 	public void onHitWall(HitWallEvent e) { 
			fitness -= 2;
		}
 	public void onWin(WinEvent e) { 
 		fitness += 5.0;
		}
 	public void onDeath(DeathEvent e) { 
			fitness -= 5.0;
		}
 	public void onRoundEnded(RoundEndedEvent e) { 
			PrintStream w = null; 
			try { 
 			w = new PrintStream(new RobocodeFileOutputStream(FITNESS_LOG_LOC, true)); 
				w.append(String.valueOf(fitness)+", sup jiggaboo");
				if (w.checkError()) { 
					
 			} 
 			} catch (IOException ex) { 
					ex.printStackTrace(out); 
 			} finally { 
 				if (w != null) { 
						w.close(); 
					} 
				} 
		}


}