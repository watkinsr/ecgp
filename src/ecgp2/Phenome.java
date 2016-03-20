package ecgp2;

import java.util.Random;

public class Phenome {
	public String[] phenomes = new String[5];
	private static final int AMOUNT_PHENOMES = 5;
	public Genome genome;
	
	public Phenome(Genome genome) {
		this.genome = genome;
		for (int i = 0; i < AMOUNT_PHENOMES; i++){
			phenomes[i] = genome.GetPhenomeFromGenome().toString();
		}
		System.out.println("GETTING PHENOMES... DONE");
	}
}

