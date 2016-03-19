package ecgp2;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ReadCSV implements AutoCloseable {

  @SuppressWarnings("null")
public double[] getFitnesses(String csvFile) {
	BufferedReader br = null;
	String line = "";
	String cvsSplitBy = ",";
	String[] fitnesses = null;
	double[] dblFitnesses = null;
	
	try {
		br = new BufferedReader(new FileReader(csvFile));
		while ((line = br.readLine()) != null) {
		    // use comma as separator
			fitnesses = line.split(cvsSplitBy);
		}
		for (int i = 0; i < fitnesses.length; i++) {
			dblFitnesses[i] = Double.valueOf(fitnesses[i]);
		}
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	} finally {
		if (br != null) {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	System.out.println("Retrieving Fitnesses... Done");
	return dblFitnesses;
  }

@Override
public void close() throws Exception {
	// TODO Auto-generated method stub
	
}

}