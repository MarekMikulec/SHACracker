package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;

public class Find68bit {
	private static File file = new File("Storage.txt");
	private static long size = 0;
	private static BufferedReader reader;
	private static HashSet<String> h = new HashSet<>();
	private static int endIndex = 10;

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.err.println("Closing writer or opening reader error. Text file can be broken.");
			return;
		}
		System.out.println("======================================================================================");
		/*
		 * Naplnění hashsetu a hledání kolize na 68bitech
		 */
		String now = null;
		boolean endNow = false;
		
		 try { reader.mark(Integer.MAX_VALUE-4); } catch (IOException e2) { 
		// TODO Auto-generated catch block 
			 e2.printStackTrace(); 
			 }
		 
		for (long i = 0; i > -1; i++) {
			try {
				now = reader.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (now == null) {
				System.err.println("Colission not found!");
				System.out.println("Lines read: " + i);
				return;
			}
			now = now.substring(0, endIndex);
			//System.out.println(now);
			if (now.startsWith("abc")) {
				endNow = !h.add(now);
				if (endNow) {
					System.out.println("Found collision starting with abc: " + now);
					System.out.println("Lines read: " + i);
					size = i;
					break;
				}
			}
		}

		long position1 = 0;
		long position2 = size;

		try {
			reader.reset();
			/*
			reader.close();
			reader = new BufferedReader(new FileReader(file));*/
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			System.err.println("Cannot go to the beginning!");
			e1.printStackTrace();
		}

		String search = null;
		for (long i = 0; i < size; i++) {
			try {
				search = reader.readLine().substring(0, endIndex);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (search.equals(now)) {
				position1 = i;
				System.out.println("First found at: " + position1);
				break;
			}
		}
		h.clear();
		System.out.println("HashSet was cleared, freeing space...");
		System.out.println("Filling buffers and looking for " + now + "...");
		LinkedList<String> buffer1 = new LinkedList<>();
		LinkedList<String> buffer2 = new LinkedList<>();

		try {
			reader.reset();
		/*	reader.close();
			reader = new BufferedReader(new FileReader(file));*/
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			System.err.println("Cannot go to the beginning!");
			e1.printStackTrace();
			return;
		}
		for (long rounds = 0; rounds < size +1; rounds++) {
			if (rounds < position1 - 1000) {
				try {
					reader.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (rounds >= position1 - 1000 && rounds <= position1) {
				try {
					search = reader.readLine().trim().substring(0, endIndex);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					break;
				}
				if (search == null) {
					System.err.println(
							"Searching in text file not working properly, you will need to find it on your own.");
					return;
				}
				buffer1.addFirst(search);

			} else if (rounds < position2 - 1000) {
				try {
					reader.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (rounds >= position2 - 1000 && rounds <= position2) {
				try {
					search = reader.readLine().trim().substring(0, endIndex);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					break;
				}
				if (search == null) {
					System.err.println(
							"Searching in text file not working properly, you will need to find it on your own.");
					return;
				}
				buffer2.addFirst(search);

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
		while (!buffer1.isEmpty()) {
			hash1 = buffer1.removeFirst();
			System.out.print(hash1);
			hash2 = buffer2.removeFirst();
			System.out.print(" x " + hash2 + "\n");
			if (!hash1.equals(hash2)) {
				System.err.println("Found!");
				break;
			}
		}
		System.out.println("======================================================================================");
		long stopTime = System.currentTimeMillis();
		System.out.println("Time: " + (stopTime - startTime) / 1000 + "s");
	}

}
