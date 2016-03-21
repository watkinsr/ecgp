package ecgp2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Random;

public class MetaBot {
	final static 
		String PATH = new String("/home/ryan/robocode/robots/sampleex");
		String PACKAGE = new String("sampleex");
		String JARS = new String("/home/ryan/robocode/libs/robocode.jar");
		String FITNESS_LOG_LOC = new String("/home/ryan/robocode/fitness.csv");
		int NUM_PHENOMES = 5;
		
	static Random random = new Random(System.currentTimeMillis());
	public String botName;
	private String sourceCode = new String();
	private String[] phenomes = new String[NUM_PHENOMES];
	public double fitness;
	private Genome[] genomes;

	
    public MetaBot(String botName, int _botID, Genome[] genomes) {
		this.botName = botName + String.valueOf(_botID);
		this.genomes = genomes;
		for (int i = 0; i < this.genomes.length; i++) {
			this.phenomes[i] = this.genomes[i].GetPhenomeFromGenome().toString();
		}
		this.fitness = 0.0;
		this.setCode();
	}

    public void setFitness(double val) {
    	this.fitness = val;
    }
    
    public Genome[] getGenomes() {
    	return this.genomes;
    }
    
	public void setCode(){
		sourceCode =
			"package "+PACKAGE+";" +
			"\nimport robocode.*;" +
			"\nimport robocode.util.Utils;" +
			"\nimport java.awt.Color;\n" +
			"\nimport java.io.BufferedWriter;" + 
			"\nimport java.io.FileWriter;" + 
			"\nimport java.io.IOException;" +
			"\nimport java.io.PrintWriter;" +
			"\n" +		
			"\npublic class " + botName + " extends AdvancedRobot {" +
			"\n" +
			"\n double fitness = 0;" +
			"\n FileWriter pw;" + 
			"\n String FITNESS_LOG_LOC = new String(\"/home/ryan/robocode/fitness.csv\");" + 
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
			"\n			fitness += 1;" +
			"\n		}" + 
			"\n 	public void onHitWall(HitWallEvent e) { " + 
			"\n			fitness -= 0.1;" + 	
			"\n		}" + 
			"\n 	public void onWin(WinEvent e) { " + 
			"\n 		fitness += getEnergy() + 100;" +		
			"\n			writeFitnessToFile();" +
			"\n		}" + 
			"\n 	public void onDeath(DeathEvent e) { " +
			"\n			fitness += getEnergy();" + 
			"\n			writeFitnessToFile();" +
			"\n		}" + 	
			"\n 	public void onRobotDeath(DeathEvent e) { " +
			"\n			fitness+= 10;" +
			"\n		}" + 
			"\n 	public void writeFitnessToFile() { " + 
			"\n         System.out.println(\"Writing fitness to file\");" + 
			"\n			try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(FITNESS_LOG_LOC, false)))) {" +
		    "\n				out.print(String.valueOf(fitness));" + 
			"\n			}catch (IOException e) {" + 
		    "\n				//exception handling " +
			"\n 		}" +
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
