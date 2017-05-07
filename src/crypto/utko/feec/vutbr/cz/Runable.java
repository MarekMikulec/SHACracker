package crypto.utko.feec.vutbr.cz;

import java.sql.SQLException;

public class Runable {

	public static void main(String[] args) throws SQLException {
		
		System.out.println("Začátek programu");
		String inputText = ":D";
		System.out.println(inputText);
		Core.inicialize(inputText);
		long counter = 0;
		double timeAtStart = Core.getTime();
		do {
			
			counter++;
			//System.out.println(counter);
		} while (Core.saveHash());
		System.out.println(counter + " rounds");
		double timeAfterGenerating = Core.getTime();
		System.out.println("Generating time: " + (timeAfterGenerating - timeAtStart) / 1000 + "s");
		Core.findHashMathInTXT();
		double timeAfterSearcihng = Core.getTime();
		System.out.println("Searching time: " + (timeAfterSearcihng - timeAfterGenerating) / 1000 + "s");
		System.out.println("Total time: " + (timeAfterSearcihng - timeAtStart) / 1000 + "s");
	

	}

}
