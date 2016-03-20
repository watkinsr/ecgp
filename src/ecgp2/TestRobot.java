package ecgp2;
import robocode.*;
import robocode.util.Utils;
import java.awt.Color;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class TestRobot extends AdvancedRobot {

 double fitness = 0;
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
		setAhead(-0.7848808121401423 / getY() - getGunTurnRemainingRadians() - getVelocity() * 0.4804608184551362 - getVelocity() / 3 * e.getHeadingRadians() * 3 * 0.001 - 0.001 / getX() * -0.21338023657199567 + e.getDistance() * getX() + getGunHeading() / 0.001 - getHeading() / getY() - getGunHeading() + getHeading());

 // --- PHENOME 2 ---
		setTurnRight(3 - getGunHeat() + Math.PI * getX() / getGunHeading() * getVelocity() + getY() + getX() - Math.PI + Math.PI * e.getHeadingRadians() / 0.2754107163664967 + getVelocity() - getGunTurnRemainingRadians() * 0.7979404811810695 - -0.6582794086783514 - getGunHeading() / getY() * getGunTurnRemainingRadians() * -0.7846089470236857 * e.getDistance() - Math.PI * getY() / Math.PI / getX() / getVelocity() * getX() / 0.8479135871374235 * getGunTurnRemainingRadians() / e.getDistance());

 // --- PHENOME 3 ---
		setTurnGunRight(-0.42728325815185175 * getGunTurnRemainingRadians() + getGunHeat() + 0.001 / e.getDistance() - -0.6413971618575238 * e.getDistance() / getGunHeat() / getGunTurnRemainingRadians() - getVelocity() - e.getHeadingRadians() + getGunHeat() / getY() - getGunHeat() - getHeading() + 0.8897168779784972 * getGunHeat() + getX() );

 // --- PHENOME 4 ---
		setTurnRadarRight(0.001 / getGunHeading() + 3 / 0.001 * 0.7102221507111148 / -0.7821984217176774 + 3 - 0.001 / e.getDistance() * -0.8368910046554994 / e.getHeadingRadians() / e.getDistance() + getHeading() / e.getHeadingRadians() - getGunHeat() * getGunTurnRemainingRadians() * Math.PI / e.getDistance() / 0.6043204316788084 + getGunTurnRemainingRadians() * getHeading() + 0.6455885473567617 + getGunTurnRemainingRadians() * getGunHeading() + 0.001 + getGunTurnRemainingRadians() * getY() * getY() + 0.2246311824979248 * Math.PI);

 // --- PHENOME 5 ---
		setFire(3 * getY() / getY() / e.getDistance() - 0.001 + getGunHeading() / getX() * getGunHeat() + getGunHeat() * 0.7774270035149391 * getY() + getGunHeading() + e.getHeadingRadians() - 0.001 / getGunHeat() + e.getHeadingRadians());

	}
 	public void onBulletHit(BulletHitEvent e) { 
			fitness += 1;
		}
 	public void onHitWall(HitWallEvent e) { 
			fitness -= 0.1;
		}
 	public void onWin(WinEvent e) { 
 		fitness += getEnergy() + 100;
			writeFitnessToFile();
		}
 	public void onDeath(DeathEvent e) { 
			fitness += getEnergy();
			writeFitnessToFile();
		}
 	public void onRobotDeath(DeathEvent e) { 
			fitness+= 10;
		}
 	public void writeFitnessToFile() { 
         System.out.println("Writing fitness to file");
			try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(FITNESS_LOG_LOC, true)))) {
				out.print(String.valueOf(fitness));
			}catch (IOException e) {
				//exception handling 
 		}
		}


}