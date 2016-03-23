package ecgp2;

public class Main
{
    public static void main(String[] args){
	GP gp = new GP();
	gp.ECLoop(GP.sdSamples, 1);
	BattleRunner.bestFitness = 0; // New gen cycle, => need a new best bot.
	gp.CUR_GEN = 0;
	gp.ECLoop(GP.sampleBots, 2);
	GP.br.BattleShutDown();
    }
}
