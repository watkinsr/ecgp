package ecgp2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.rits.cloning.Cloner;

/**
 * This class represents the main genetic programming operations.
 * 
 * @author Ryan Watkins
 *
 */

public class GP {
	public int CUR_GEN = 0;
	public final static int POP_SIZE = 25;
	public static final int MAX_GENERATIONS = 25;
	public static final int GENOME_SIZE = 5;
	public static final int ROUND_SIZE = 1;
	static String botNames[] = new String[POP_SIZE];
	private List<MetaBot> pool = new ArrayList<MetaBot>();
	private final String sd = "sample.SittingDuck";
	private String[] opponentSamples = { sd, sd, sd, sd, sd };
	private BattleRunner br;

	public static Logger logger;
	public static FileHandler fh;
	public static boolean DEBUG = true;

	public GP() {
		initLogger();
		generatePool(0);
		br = new BattleRunner();
	}

	public void ECLoop() {
		while (CUR_GEN < MAX_GENERATIONS) {
			try {
				br.runBatchWithSamples(pool, opponentSamples, ROUND_SIZE);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sortFitnesses();
			geneticOp(pool.get(0).getGenomes(), pool.get(1).getGenomes());
			CUR_GEN++;
		}

		br.BattleShutDown();
	}

	public void initLogger() {
		logger = Logger.getLogger("MyLog");
		try {
			// This block configure the logger with handler and formatter
			fh = new FileHandler("/home/ryan/GP.log");
			logger.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
			// the following statement is used to log any messages
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void log(String msg) {
		// logger.info(msg);
		if (DEBUG) {
			System.out.println(msg);
		}

	}

	public void sortFitnesses() {
		Collections.sort(pool, new Comparator<MetaBot>() {
			@Override
			public int compare(MetaBot b1, MetaBot b2) {
				return (int) (b2.fitness - b1.fitness); // Ascending //TESTING:
														// Descending..
			}
		});
	}

	public int getRandomBranch(TreeNode<String> node, Random random) {
		return random.nextInt(node.children.size());
	}

	@SuppressWarnings("unchecked")
	public void geneticOp(Genome[] parent1, Genome[] parent2) {
		
		Cloner cloner = new Cloner();
		Random random = new Random();
		pool.clear();
		int target = 0;
		int[] expectPhenoOneSize = new int[5];
		int[] expectPhenoTwoSize = new int[5];
		
		for (int i = 0; i < POP_SIZE / 2; i++) {
			Genome[] p1 = cloner.deepClone(parent1);
			Genome[] p2 = cloner.deepClone(parent2);
			for (int j = 0; j < parent1.length; j++) {
				double chance = Math.random();
				if (chance <= 0.05) {
					p1[j].tree = mutate(p1[j]);
				}
				

				// log("Genome(1): " + String.valueOf(j)); p1[j].tree.print(); p1[j].calculateTreeDepth();
				// log("Genome(2): " + String.valueOf(j)); p2[j].tree.print(); p2[j].calculateTreeDepth();

				// Targets now working...
				int target1 = p1[j].TREE_DEPTH == 0 ? 1 : p1[j].TREE_DEPTH == 1 ? 1 : random.nextInt(p1[j].TREE_DEPTH) + 1;
				int target2 = p2[j].TREE_DEPTH == 0 ? 1 : p2[j].TREE_DEPTH == 1 ? 1 : random.nextInt(p2[j].TREE_DEPTH) + 1;
				
				int cutTree1 = p1[j].TREE_DEPTH - target1; // how much is taken from pheno1
				int cutTree2 = p2[j].TREE_DEPTH - target2;
				expectPhenoOneSize[j] = p1[j].TREE_DEPTH - cutTree1 + cutTree2;
				expectPhenoTwoSize[j] = p2[j].TREE_DEPTH - cutTree2 + cutTree1;

				int sub1Index = p1[j].tree.getSubTreeTest(target1, random);
				TreeNode<String> sub1 = cloner.deepClone(p1[j].tree.getNode(sub1Index));
				// log("Sub1: "); // sub1.print();

				int sub2Index = p2[j].tree.getSubTreeTest(target2, random);
				TreeNode<String> sub2 = cloner.deepClone(p2[j].tree.getNode(sub2Index));
				// log("Sub2: "); // sub2.print();

				TreeNode<String> p1Tree = cloner.deepClone(p1[j].tree);

				// log("P1 after cross gene: " + String.valueOf(j)); p1[j].tree.print(); 
				p1[j].calculateTreeDepth();
				// log("P2 after cross gene: " + String.valueOf(j)); p2[j].tree.print();
				p2[j].calculateTreeDepth();

				p1[j]._pheno = new StringBuilder("");
				p2[j]._pheno = new StringBuilder("");
				if (j == p1.length - 1) {
					logExpectedGrowthInPhenomes(expectPhenoOneSize, expectPhenoTwoSize);
					addCrossedGenomesToPool(p1, p2);
				}
			}
		}

	}
	
	public void logExpectedGrowthInPhenomes(int[] expectPhenoOneSize, int[] expectPhenoTwoSize) {
		String phenoOneGrowths = "P1 Exp Vals: [";
		String phenoTwoGrowths = "P2 Exp Vals: [";
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < expectPhenoOneSize.length; j++) {
				if (i == 0)
					phenoOneGrowths = phenoOneGrowths.concat(String.valueOf(expectPhenoOneSize[j]) + ",");
				else
					phenoTwoGrowths = phenoTwoGrowths.concat(String.valueOf(expectPhenoTwoSize[j]) + ",");
			}
		}
		System.out.println("\n"+ phenoOneGrowths +"\n" + phenoTwoGrowths);
	}
	
	public void addCrossedGenomesToPool(Genome[] dad, Genome[] mum) {
		System.out.println("Creating metaBot Parent 1 after crossover: ");
		MetaBot dadBot = new MetaBot("GP", pool.size(), dad);
		pool.add(dadBot);
		pool.get(pool.size() - 1).compile();
		System.out.println("Creating metaBot Parent 2 after crossover: ");
		MetaBot mumBot = new MetaBot("GP", pool.size(), mum);
		pool.add(mumBot);
		pool.get(pool.size() - 1).compile();
	}

	public TreeNode<String> mutate(Genome botGenome) {
		System.out.println("Mutation occured");
		Genome mutantGene = new Genome();
		TreeNode<String> mutantTerm = new TreeNode(mutantGene.term());
		log("Gene about to be mutated: "); botGenome.tree.print();
		log("Got mutant gene: "); mutantTerm.print();
		botGenome.tree.getRandomTerminal().replaceWith(mutantTerm);
		log("After mutation: "); botGenome.tree.print();
		return botGenome.tree;
	}

	public Genome[] getGenomes() {
		Genome[] genomes = new Genome[GENOME_SIZE];
		for (int i = 0; i < GENOME_SIZE; i++) {
			genomes[i] = new Genome();
		}
		return genomes;
	}

	public void generatePool(int offset) {
		for (int i = 0; i < POP_SIZE - offset; i++) {
			Genome[] genomes = getGenomes();
			System.out.println("FORMING ROBOT " + String.valueOf(i + offset) + " ... DONE");
			pool.add(new MetaBot("GP", i, genomes));
			pool.get(i).compile();
		}
		System.out.println("COMPILING BOTS... DONE");
	}

	static String[] sampleBots = { "sample.TrackFire", "sample.VelociRobot", "sample.Walls", "sample.RamFire",
			"sample.SpinBot" };
}