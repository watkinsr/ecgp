package ecgp2;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.rits.cloning.Cloner;

import robocode.BattleResults;
import robocode.control.*;
import robocode.control.events.*;

public class BattleRunner {
    private final RobocodeEngine engine;
    private final BattlefieldSpecification battlefield;
    private final BattleObserver battleObserver;
    public final static String ROBOCODE_LOCATION = "/home/ryan/robocode/";
    public static double averageFitness;
    public static double fitness, bestFitness = -9999.0;
    public static double genBestFitness = -9999.0;
    public int bestIndex = 0;
    public static List<MetaBot> bestBots = new ArrayList<MetaBot>();
    public static List<MetaBot> pool = new ArrayList<MetaBot>();
    public static boolean newBestFitness = false;

    public BattleRunner() {
	BattleRunner.bestBots.add(new MetaBot("best", 0, GP.GetGenomes())); 
	BattleRunner.bestBots.add(new MetaBot("best", 1, GP.GetGenomes())); 
	engine = new RobocodeEngine(new java.io.File(ROBOCODE_LOCATION));
	battleObserver = new BattleObserver();
	engine.addBattleListener(battleObserver);
	engine.setVisible(true);
	battlefield = new BattlefieldSpecification(800, 600);
    }

    public void BattleShutDown() {
	engine.close();
	System.exit(0);
    }

    public void runBatchWithSamples(List<MetaBot> pool, String[] samples, int cycle, int rounds) throws Exception {
	String bot;
	BattleRunner.pool = pool;
	averageFitness = 0;
	int totalFitness = 0;
	newBestFitness = false;
	genBestFitness = -9999.0;
	System.out.println("ROUND AMOUNT: " + String.valueOf(rounds));

	for (int i = 0; i < pool.size(); i++) {
	    for(int j = 0; j < samples.length; j++) {
		bot = "sampleex." + pool.get(i).botName;
		RobotSpecification[] selectedBots = engine
		    .getLocalRepository(bot + "," + samples[j]);
		BattleSpecification battleSpec = new BattleSpecification(rounds, battlefield, selectedBots);
		engine.runBattle(battleSpec, true);
		// BattleRunner.pool.get(i).fitness += readFitness();
	    }	
	    pool.get(i).fitness = pool.get(i).fitness / (rounds * samples.length);
	    totalFitness += pool.get(i).fitness;
	    System.out.println("Robot[" + String.valueOf(i) + "] fitness: " + String.valueOf(pool.get(i).fitness));

	    if (pool.get(i).fitness > bestFitness) {
		bestFitness = pool.get(i).fitness;
		bestIndex = i;
		newBestFitness = true;
		storeBestBot();
	    } 
	    if (pool.get(i).fitness > genBestFitness) {
		genBestFitness = pool.get(i).fitness;
	    }
	}

	averageFitness = totalFitness / pool.size();
	System.out.println("Average fitness: " + String.valueOf(averageFitness));
	System.out
	    .println("Best fitness: " + String.valueOf(bestFitness) + ", best index: " + String.valueOf(bestIndex));
    }

    private void storeBestBot() {
	BattleRunner.bestBots.set(GP.cycle - 1, BattleRunner.pool.get(bestIndex));
	BattleRunner.bestBots.get(GP.cycle - 1).botName = "bestBot_" + String.valueOf(GP.cycle);
	BattleRunner.bestBots.get(GP.cycle - 1).setCode(BattleRunner.bestBots.get(GP.cycle-1).GetPenalty());
	BattleRunner.bestBots.get(GP.cycle - 1).compile();
    }

    public static double readFitness() {
	try (BufferedReader br = new BufferedReader(new FileReader(MetaBot.FITNESS_LOG_LOC))) {
	    StringBuilder sb = new StringBuilder();
	    String line = br.readLine();
	    String sFitness = null;
	    while (line != null) {
		sb.append(line);
		sb.append(System.lineSeparator());
		line = br.readLine();
	    }
	    if (sb.length() != 0) {
		sFitness = sb.toString();
		fitness = Double.parseDouble(sFitness);
	    } else {
		return readFitness();
	    }

	} catch (FileNotFoundException e1) {
	    e1.printStackTrace();
	} catch (IOException e1) {
	    e1.printStackTrace();
	}
	return fitness;
    }
}

// based on example from Robocode Control API JavaDocs
class BattleObserver extends BattleAdaptor {
    robocode.BattleResults[] results;

    public void onBattleCompleted(BattleCompletedEvent e) {
    	results = e.getIndexedResults();
    }

    public void onBattleError(BattleErrorEvent e) {
	System.out.println("Error running battle: " + e.getError());
    }

    public BattleResults[] getResults() {
	return results;
    }

}
