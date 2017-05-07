package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;


public class TestTxtSearch {

	private static File file = new File("Storage.txt");
	private static long size = 712780769; 
	private static BufferedReader reader;
	private static int bitNumber = 56;
	private static int bytNumber = bitNumber / 8;
	private static String hash = "ae6074abe8657d";
	private static double limit = Math.pow(10, (bytNumber/2));
	
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.err.println("Closing writer or opening reader error. Text file can be broken.");
		}
		System.out.println("======================================================================================");
		String now;
		LinkedList<String> buffer1 = new LinkedList<>();
		LinkedList<String> buffer2 = new LinkedList<>();
		LinkedList<String> usedNow;
		usedNow = buffer1;	
		for (long rounds = 0; rounds < size; rounds++) {
			try {
				now = reader.readLine().trim();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
			if (now == null) {
				System.err
						.println("Searching in text file not working properly, you will need to find it on your own.");
				return;
			}
			usedNow.addFirst(now);
			
			if(usedNow.size() >= limit){
				usedNow.removeLast();
			}
			if (now.equals(hash)) {
				System.out.println("Our hash found on line: " + (rounds));
				usedNow = buffer2;
			}
		}
		System.out.println("======================================================================================");
		try {
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Starting loking in past to find the first duplicite...");
		String hash1, hash2;
		while(!buffer1.isEmpty()){
			hash1 = buffer1.removeFirst();
			System.out.print(hash1);
			hash2= buffer2.removeFirst();
			System.out.print(" x " + hash2 + "\n");
			if(!hash1.equals(hash2)){
				System.err.println("Found!");
				break;
			}
		}
		System.out.println("======================================================================================");
		long stopTime = System.currentTimeMillis();
		System.out.println("Time: " + (stopTime-startTime)/1000 + "s");
	}
}
