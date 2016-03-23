package ecgp2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

public class MetaBot {
    public final static 
	String PATH = new String("/home/ryan/robocode/robots/GP_" + GP.timeStamp.replaceAll("\\.+","_")),
	       PACKAGE = new String("GP_" + GP.timeStamp.replaceAll("\\.+","_")),
	       JARS = new String("/home/ryan/robocode/libs/robocode.jar"),
	       // FITNESS_LOG_LOC = new String("/home/ryan/robocode/data/fitness.csv");
	       // FITNESS_LOG_LOC = new String("/home/ryan/projects/ec/data/fitness_" + GP.timeStamp.replaceAll("\\.+","_") + ".csv");
	       // FITNESS_LOG_LOC = new String("/home/ryan/robocode/data/fitness_2016_03_23_03_36_06.csv");
	       FITNESS_LOG_LOC = new String("/home/ryan/robocode/fitness.csv");
	       // FITNESS_LOG_LOC = new String("/home/ryan/robocode/projects/ec/data/fitness.csv");

	       final int NUM_PHENOMES = 5;

    static Random random = new Random(System.currentTimeMillis());
    public String botName;
    private String sourceCode = new String();
    private String[] phenomes = new String[NUM_PHENOMES];
    public double fitness;
    public int[] nodes = new int[NUM_PHENOMES];
    public int totalNodes = 0;
    private Genome[] genomes;


    public MetaBot(String botName, int _botID, Genome[] genomes) {
	this.botName = botName + String.valueOf(_botID);
	this.genomes = genomes;
	this.totalNodes = 0;
	for (int i = 0; i < this.genomes.length; i++) {
	    this.phenomes[i] = this.genomes[i].GetPhenomeFromGenome().toString();
	    this.nodes[i] = this.genomes[i].nodes;
	    this.totalNodes += this.nodes[i];
	}
	double penalty = GetPenalty();
	this.fitness = 0.0;
	this.setCode(penalty);
    }

    public double GetPenalty() {
	double penalty = 0;
	for (int i = 0; i < genomes.length; i++) {
	    if (genomes[i].TREE_DEPTH > Genome.MAX_DEPTH) {
		int amountOver = Genome.MAX_DEPTH - genomes[i].TREE_DEPTH;
		penalty += amountOver;
	    }
	}
	return penalty;
    }

    public void setFitness(double val) {
	this.fitness = val;
    }

    public Genome[] getGenomes() {
	return this.genomes;
    }

    public void setCode(double penalty){
	sourceCode =
	    "package "+PACKAGE+";" +
	    "\nimport robocode.*;" +
	    "\nimport robocode.util.Utils;" +
	    "\nimport java.awt.Color;\n" +
	    "\nimport java.io.BufferedWriter;" + 
	    "\nimport java.io.FileWriter;" + 
	    "\nimport java.io.IOException;" +
	    "\nimport java.io.PrintWriter;" +
	    "\nimport java.io.Writer;" +
	    "\n import java.io.OutputStreamWriter;" +
	    "\n import java.io.FileOutputStream;" + 
	    "\n import java.io.File;" + 
	    "\n import java.io.FileNotFoundException;" +
	    "\n import java.io.UnsupportedEncodingException;" + 
	    "\n import java.io.FileWriter;" + 
	    "\n import java.io.PrintStream;" +
	    "\n import robocode.AdvancedRobot; " + 
	    "\n import robocode.RobocodeFileOutputStream; "+ 
	    "\npublic class " + botName + " extends AdvancedRobot {" +
	    "\n" +
	    "\n double fitness = 0.0 + " + String.valueOf(penalty) + "; // Potential penalty here for tree growth" +
	    "\n FileWriter pw;" + 
	    "\n String FITNESS_LOG_LOC = new String(\"" + FITNESS_LOG_LOC +"\");" + 
	    "\n" +
	    "\n	public void run() {" +
	    "\n" +
	    "\n		setAdjustGunForRobotTurn(true);" +
	    "\n" +
	    "\n		setColors(Color.red,Color.blue,Color.green);" +	
	    "\n		while(true) {" +
	    "\n			turnGunRight(Double.POSITIVE_INFINITY);" +
	    "\n		}" +
	    "\n" +	
	    "\n	}" +
	    "\n	public void onScannedRobot(ScannedRobotEvent e) {" +
	    "\n" +
	    "\n // --- PHENOME 1 ---" +
	    "\n		setAhead(" + phenomes[0] + ");" +
	    "\n" +
	    "\n // --- PHENOME 2 ---" +
	    "\n		setTurnRight("+ phenomes[1] +");"  +
	    "\n" +
	    "\n // --- PHENOME 3 ---" +
	    "\n		setTurnGunRight("+ phenomes[2] +");"  +
	    "\n" +
	    "\n // --- PHENOME 4 ---" +
	    "\n		setTurnRadarRight("+ phenomes[3] +");"  +
	    "\n" +
	    "\n // --- PHENOME 5 ---" +
	    "\n		setFire("+ phenomes[4] +");"  +
	    "\n" +
	    "\n	}" +
	    "\n 	public void onBulletHit(BulletHitEvent e) { " + 
	    "\n			fitness += e.getBullet().getPower();" +
	    "\n		}" + 
	    "\n 	public void onHitByBullet(HitByBulletEvent e) { " + 
	    "\n			fitness -= e.getBullet().getPower();" +
	    "\n		}" + 
	    "\n     public void onBulletMissed(BulletMissedEvent e) {" +
	    "\n         fitness -= e.getBullet().getPower() / 2 ;" +
	    "\n 	} " +									
	    "\n 	public void onHitWall(HitWallEvent e) { " + 
	    "\n			fitness -= 2;" + 	
	    "\n		}" + 
	    "\n 	public void onWin(WinEvent e) { " + 
	    "\n 		fitness += 5.0;" +
	    "\n		}" + 
	    "\n 	public void onDeath(DeathEvent e) { " +
	    "\n			fitness -= 5.0;" + 
	    "\n		}" + 	
	    "\n 	public void onRoundEnded(RoundEndedEvent e) { " +
	    "\n			PrintStream w = null; "+ 
	    "\n			try { "+ 
	    "\n 			w = new PrintStream(new RobocodeFileOutputStream(FITNESS_LOG_LOC, true)); "+
	    "\n				w.append(String.valueOf(fitness)+\",\");	" + 
	    "\n				if (w.checkError()) { " +
	    "\n					"+
	    "\n 			} " + 
	    "\n 			} catch (IOException ex) { " +
	    "\n					ex.printStackTrace(out); "+ 
	    "\n 			} finally { "+ 
	    "\n 				if (w != null) { "+ 
	    "\n						w.close(); "+ 
	    "\n					} "+ 
	    "\n				} "+ 
	    "\n		}" + 	
	    "\n" +
	    "\n" +	
	    "\n}"
	    ;
    }

    /**
     * Writes the Robot class source code to a .java file and compiles it
     * @return the absolute path to the generated .class file
     */
    String compile(){
	try{
	    FileWriter fstream = new FileWriter(PATH+"/"+botName+".java");
	    BufferedWriter out = new BufferedWriter(fstream);
	    out.write(sourceCode);
	    out.close();
	} catch(Exception e){
	    System.err.println("Error: " + e.getMessage());
	}

	// Compile code
	try {
	    execute("javac -cp " + JARS + " " + PATH + "/" + botName + ".java");
	}catch(Exception e){
	    e.printStackTrace();
	    System.exit(1);
	}
	return (PATH+"/"+botName+".class");
    }

    public static void execute(String command) throws Exception	   {
	Process process = Runtime.getRuntime().exec(command);
	printMsg(command + " stdout:", process.getInputStream());
	printMsg(command + " stderr:", process.getErrorStream());
	process.waitFor();
	if(process.exitValue() != 0){
	    System.out.println(command + "exited with value " + process.exitValue());
	    throw new Exception("Incorrect GenomeToPhenome conversion, failed to compile bot");
	}
    }

    private static void printMsg(String name, InputStream ins) throws Exception {
	String line = null;
	BufferedReader in = new BufferedReader(new InputStreamReader(ins));
	while((line = in.readLine()) != null){
	    System.out.println(name + " " + line);
	}
    }
}
