package ecgp2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
	private final String sd = "sample.SittingDuck";
	static double averageFitness;
	public double fitness, bestFitness = 0;
	public int bestIndex = 0;
	public static MetaBot bestBot;
	private List<MetaBot> pool;

	public BattleRunner() {
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

	public void runBatchWithSamples(List<MetaBot> pool, String opponents[], int rounds) throws Exception {
		String bot;
		this.pool = pool;
		averageFitness = 0;
		bestFitness = 0;
		System.out.println("Running battles..." + String.valueOf(rounds));

		for (int i = 0; i < pool.size(); i++) {
			bot = "sampleex." + pool.get(i).botName;
			RobotSpecification[] selectedBots = engine
					.getLocalRepository(bot + "," + sd + "," + sd + "," + sd + "," + sd + "," + sd);
			BattleSpecification battleSpec = new BattleSpecification(rounds, battlefield, selectedBots);
			engine.runBattle(battleSpec, true);

			pool.get(i).fitness = readFitness();
			System.out.println("Robot[" + String.valueOf(i) + "] fitness: " + String.valueOf(pool.get(i).fitness));
			if (fitness > bestFitness) {
				bestFitness = fitness;
				bestIndex = i;
			}
		}

		storeBestBot();
		averageFitness = averageFitness / pool.size();
		System.out.println("Average fitness: " + String.valueOf(averageFitness));
		System.out
				.println("Best fitness: " + String.valueOf(bestFitness) + ", best index: " + String.valueOf(bestIndex));
		storeAverageFitness();
	}

	private void storeAverageFitness() {
		try (PrintWriter out = new PrintWriter(
				new BufferedWriter(new FileWriter("/home/ryan/robocode/average_fitnesses.csv", true)))) {
			out.print(String.valueOf(averageFitness) + ",");
		} catch (IOException e) {
			// exception handling " +
		}
	}

	private void storeBestBot() {
		Cloner cloner = new Cloner();
		BattleRunner.bestBot = cloner.deepClone(this.pool.get(bestIndex));
		BattleRunner.bestBot.botName = "bestBot_sd";
		BattleRunner.bestBot.setCode();
		BattleRunner.bestBot.compile();
	}

	public double readFitness() {
		try (BufferedReader br = new BufferedReader(new FileReader("/home/ryan/robocode/fitness.csv"))) {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			String everything = sb.toString();

			fitness = Double.parseDouble(everything);
			averageFitness += fitness;

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
	double fitness = 0;

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
