package ecgp2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
	public final static int POP_SIZE = 2;
	public static final int MAX_GENERATIONS = 10;
	public static final int GENOME_SIZE = 5;
	public static final int ROUND_SIZE = 1;
	static String botNames[] = new String[POP_SIZE];
	public static List<MetaBot> pool = new ArrayList<MetaBot>();
	private List<MetaBot> matingPool = new ArrayList<MetaBot>();
	private static String fiveSd = "sample.SittingDuck, sample.SittingDuck, sample.SittingDuck, sample.SittingDuck, sample.SittingDuck";
	public static String[] sdSamples = {fiveSd};
	public static BattleRunner br;	
	public static double PROB_MUTATE_TERM = 0.2;
	public static double PROB_MUTATE_INT_NODE = 0.8;
	public static double PROB_MUTATE_OP = 0.05;
	public final int TOURNY_SEL_SIZE = 5;
	public static Logger logger;
	public static FileHandler fh;
	public static boolean DEBUG = false;
	public static String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
	public static int cycle = 0;
	
	
	public GP() {
		initLogger();
		long startTime = System.nanoTime();
		String s= "/home/ryan/robocode/robots/GP_" + GP.timeStamp.replaceAll("\\.+","_");
	    File file=new File(s);
	    file.mkdirs();
		GeneratePool(0);
		long endTime = System.nanoTime();
		final double seconds = ((double)(endTime - startTime)/ 1000000000);
		System.out.println("INIT POP CLOCK TIME: "  + String.valueOf(seconds) + "s");
		br = new BattleRunner();

	}

	public static String[] sampleBots = {
			"sample.TrackFire",
			"sample.RamFire",
			"sample.SpinBot"
		};
	
	private void RunBattles(String[] samples) {
		try {
			System.out.println("GENERATION: " + String.valueOf(CUR_GEN));
			br.runBatchWithSamples(pool, samples, cycle, ROUND_SIZE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void ECLoop(String[] samples, int cycle) {
		GP.cycle = cycle;
		InitLogGenFile(String.valueOf(cycle));
		while (CUR_GEN < MAX_GENERATIONS) {
			RunBattles(samples);
			SortFitnesses(pool);
			BuildMatingPool(TOURNY_SEL_SIZE);
			GeneticOp();
			LogGenerationStats();
			CUR_GEN++;
		}
	}

	private void LogGenerationStats() {
		int totalNodes = 0;
		for (int i = 0; i < pool.size(); i++) {
			totalNodes += pool.get(i).totalNodes;
		}
		int averageNodesPerBotGene = totalNodes / pool.size() / GENOME_SIZE;
		System.out.println("average nodes per gene per bot: " + String.valueOf(averageNodesPerBotGene));
		WriteGenerationStats(averageNodesPerBotGene, cycle);
	}
	
	private void BuildMatingPool(int k) {
		Random rand = new Random();
		for (int i = 0; i < k; i++) {
			matingPool.add(pool.get(rand.nextInt(pool.size())));
		}
		SortFitnesses(matingPool);
		matingPool.add(BattleRunner.bestBots.get(cycle));
	}
	
	private void InitLogGenFile(String cycle) {
		try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("/home/ryan/projects/ec/data/generation_" +cycle+"_"+timeStamp+".csv", true)))) {
			out.print("avg.fit, best.fit, newbest?, average.node.gene\n");
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	private void WriteGenerationStats(int averageNodesPerBotGene, int cycle) {
		String avgFit = String.valueOf(BattleRunner.averageFitness);
		String bestFit = String.valueOf(BattleRunner.genBestFitness);
		String newBestFit = String.valueOf(BattleRunner.newBestFitness);
		String avgNodes = String.valueOf(averageNodesPerBotGene);

		String sCycle = String.valueOf(cycle);
		String s = avgFit + ", " + bestFit + ", " + newBestFit + ", " + avgNodes; 
		
		try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("/home/ryan/projects/ec/data/generation_" +sCycle+"_"+timeStamp+".csv", true)))) {
			out.print(s + "\n");
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
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

	public static void Log(String msg) {
		// logger.info(msg);
		if (DEBUG) {
			System.out.println(msg);
		}

	}

	public void SortFitnesses(List<MetaBot> bots) {
		Collections.sort(bots, new Comparator<MetaBot>() {
			@Override
			public int compare(MetaBot b1, MetaBot b2) {
				return Double.compare(b2.fitness, b1.fitness); // Ascending //TESTING:										// Descending..
			}
		});
	}

	public MetaBot GetMate() {
		double PROB_1 = 0.5, PROB_2 = 0.2, PROB_3 = 0.1, PROB_4 = 0.08, PROB_5 = 0.05;
		double prob = new Random().nextDouble();
		if (prob >= PROB_1) {
			return matingPool.get(0);
		} else if (prob < PROB_1 && prob >= PROB_2) {
			return matingPool.get(1);
		} else if (prob < PROB_2 && prob >= PROB_3) {
			return matingPool.get(2);
		} else if (prob < PROB_3 && prob >= PROB_4) {
			return matingPool.get(3);
		} else if (prob < PROB_4 && prob >= PROB_5) {
			return matingPool.get(4);
		} else{
			return matingPool.get(5);
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void GeneticOp() {
		Cloner cloner = new Cloner();
		Random random = new Random();
		/* float popSize = (float) POP_SIZE;
		int removalAmount = (int) (popSize * 80f/ 100f);
		System.out.println("Removal amount: " + String.valueOf(removalAmount));
		for (int i = 0; i < removalAmount; i++) { // Clear 80% of worst fitness populus
			pool.remove(pool.size() - 1);
		}
		System.out.println("pool size: " + String.valueOf(pool.size()));
		*/
		// removalAmount = (int) (popSize * 80f/ 100f);
		
		pool.clear();
		int removalAmount = POP_SIZE;
		
		int[] expectPhenoOneSize = new int[5];
		int[] expectPhenoTwoSize = new int[5];
		

		for (int i = 0; i < removalAmount / 2; i++) {
			Genome[] p1 = cloner.deepClone(GetMate().getGenomes());
			Genome[] p2 = cloner.deepClone(GetMate().getGenomes());
			
			for (int j = 0; j < GENOME_SIZE; j++) {
				double chance = Math.random();
				if (chance <= PROB_MUTATE_OP) { // Recommended 5% mutation rate
					p1[j].tree = mutate(p1[j]);
				}
				p1[j].calculateTreeDepth();
				// log("Genome(1): " + String.valueOf(j)); p1[j].tree.print(); p1[j].calculateTreeDepth();
				// log("Genome(2): " + String.valueOf(j)); p2[j].tree.print(); p2[j].calculateTreeDepth();

				// Targets now working...
				int target1 = p1[j].TREE_DEPTH == 0 ? 1 : p1[j].TREE_DEPTH == 1 ? 1 : random.nextInt(p1[j].TREE_DEPTH) + 1;
				int target2 = p2[j].TREE_DEPTH == 0 ? 1 : p2[j].TREE_DEPTH == 1 ? 1 : random.nextInt(p2[j].TREE_DEPTH) + 1;
				
				int cutTree1 = p1[j].TREE_DEPTH - target1; // how much is taken from pheno1
				int cutTree2 = p2[j].TREE_DEPTH - target2;
				expectPhenoOneSize[j] = p1[j].TREE_DEPTH - cutTree1 + cutTree2;
				expectPhenoTwoSize[j] = p2[j].TREE_DEPTH - cutTree2 + cutTree1;

				int sub1Index = p1[j].tree.getSubTreeTest(target1, new Random());
				TreeNode<String> sub1 = cloner.deepClone(p1[j].tree.getNode(sub1Index));
				Log("Sub1: "); sub1.print();

				int sub2Index = p2[j].tree.getSubTreeTest(target2, new Random());
				TreeNode<String> sub2 = cloner.deepClone(p2[j].tree.getNode(sub2Index));
				Log("Sub2: "); sub2.print();

				TreeNode<String> p1Tree = cloner.deepClone(p1[j].tree);
				TreeNode<String> p2Tree = cloner.deepClone(p2[j].tree);
				p1Tree.getNode(sub1Index).replaceWith(sub2);
				p2Tree.getNode(sub2Index).replaceWith(sub1);
				
				p1[j].tree = p1Tree;
				p2[j].tree = p2Tree;
				
				Log("P1 after cross gene: " + String.valueOf(j)); p1[j].tree.print(); 
				p1[j].calculateTreeDepth();
				Log("P2 after cross gene: " + String.valueOf(j)); p2[j].tree.print();
				p2[j].calculateTreeDepth();

				p1[j]._pheno = new StringBuilder("");
				p2[j]._pheno = new StringBuilder("");
				if (j == p1.length - 1) {
					LogExpectedGrowthInPhenomes(expectPhenoOneSize, expectPhenoTwoSize);
					AddCrossedGenomesToPool(p1, p2, i * 2);
				}
			}
		}

	}
	
	public void LogExpectedGrowthInPhenomes(int[] expectPhenoOneSize, int[] expectPhenoTwoSize) {
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
	
	public void AddCrossedGenomesToPool(Genome[] dad, Genome[] mum, int beginInd) {
		MetaBot dadBot = new MetaBot("GP", pool.size(), dad);
		pool.add(dadBot);
		pool.get(pool.size() - 1).compile();
		System.out.println("ROBOT[" + String.valueOf(beginInd)+"] POST-CROSS ... DONE");
		
		MetaBot mumBot = new MetaBot("GP", pool.size(), mum);
		pool.add(mumBot);
		pool.get(pool.size() - 1).compile();
		System.out.println("ROBOT[" + String.valueOf(beginInd + 1)+"] POST-CROSS ... DONE");
	}

	@SuppressWarnings("unchecked")
	public TreeNode<String> mutate(Genome botGenome) {
		System.out.println("Mutation occured");
		Genome mutantGene = new Genome();
		Random rand = new Random();
		if (rand.nextDouble() < PROB_MUTATE_TERM) {
			TreeNode<String> mutantTerm = new TreeNode(mutantGene.term());
			Log("Gene about to be mutated: "); botGenome.tree.print();
			Log("Got mutant gene: "); mutantTerm.print();
			botGenome.tree.getRandomTerminal().replaceWith(mutantTerm);
			Log("After mutation: "); botGenome.tree.print();
			return botGenome.tree;
		} else {
			int target = botGenome.TREE_DEPTH == 0 ? 1 : botGenome.TREE_DEPTH == 1 ? 1 : 
				rand.nextInt(botGenome.TREE_DEPTH) + 1;
			int subIndex = botGenome.tree.getSubTreeTest(target, rand);
			Cloner cloner = new Cloner();
			TreeNode<String> p1Tree = cloner.deepClone(botGenome.tree);
			p1Tree.getNode(subIndex).replaceWith(mutantGene.tree);
			return p1Tree;
		}
	}

	public static Genome[] GetGenomes() {
		Genome[] genomes = new Genome[GENOME_SIZE];
		for (int i = 0; i < GENOME_SIZE; i++) {
			genomes[i] = new Genome();
		}
		return genomes;
	}

	public void GeneratePool(int offset) {
		for (int i = 0; i < POP_SIZE - offset; i++) {
			Genome[] genomes = GetGenomes();
			System.out.println("FORMING ROBOT " + String.valueOf(i + offset) + " ... DONE");
			pool.add(new MetaBot("GP", i, genomes));
			pool.get(i).compile();
		}
		System.out.println("COMPILING BOTS... DONE");
	}
}
