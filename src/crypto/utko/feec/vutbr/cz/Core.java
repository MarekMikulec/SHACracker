package crypto.utko.feec.vutbr.cz;

import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.stream.Stream;

import javax.xml.bind.DatatypeConverter;

import fr.cryptohash.SHA256;


public final class Core{
	private static HashSet<String> hashSet = new HashSet<>();
	private static int bitNumber = 64;
	/*
	 * 32 bitů = 84 459 rounds and 1.157s bez SQL, se zápisem do SQL 220.199s
	 * --> 200x pomalejší, i se zápisem na SSD, se zápisem do TXT 1.215s -->
	 * nepatrný rozdíl 40 bitů = 2 734 505 rounds 30.556s bez SQL 48 bitů = 5GB
	 * paměti RAM, a 19 379 828 rounds 133.065s, 12 znaků textu 181.692s s
	 * TreeSetem, a také přibližně 5GB RAM 56 bitů = 14 znaků => teoreticky
	 * předchozí krát 2^16, tedy 256 krát tedy asi 8 hodin času, a 1280GB paměti
	 */
	
	/*
	 * Final - nevytváření dalších objektů zbytečných a šetření tak paměti
	 */
	private static int bytNumber = bitNumber / 8;
	private static String hash;
	//private static String hashBefore;
	private static File file = new File("Storage.txt");
	private static BufferedWriter writer;
	private static BufferedReader reader;
	private static MessageDigest mDigest; //vygenerování SHA
	private static byte[] result;
	private static StringBuffer sb;
	private static long size;
	private static double limit = Math.pow(10, (bytNumber/2));
	private static SHA256 sha = new SHA256();


	/*
	 * Tímto bychom měli dosáhnout toho, že z třídy Core nevytvoříme objekt => privátní konstruktor.
	 * Budeme volat všechny metody jen staticky, protože objekt nepotřebujeme.
	 * Taká zajistíme, že bude jen jeden objekt statický této třídy.
	 * Tomuto se říká návrhoví vzor Singleton = jedináček.
	 * */
	private Core(){}
	
	/*
	 * Vytvoření nového StringBufferu + spočítání 1. hashe na základě zprávy + nový writer
	 */
	public static void inicialize(String inputText) {
		sb = new StringBuffer();
		hash = sha256(inputText, bytNumber);
		try {
			writer = new BufferedWriter(new FileWriter(file));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * Spočítá a vypíše hash
	 */
	private static void countHash() {
		hash = sha256(hash, bytNumber);
		//System.out.println(hash);
	}

	/*
	 * Returns true if set already did not contains this hash. Returns false if
	 * hash is already there.
	 * Vypočítá, zapíše do texťáku a na základě podmínky zda první pozice je "a", zapisuje do HashSetu.
	 */
	public static boolean saveHash() {
		//hashBefore = hash;
		countHash();
		writeTextInTxt();
		if (hash.startsWith("abc")) {
			//System.err.println("Written in hashSet: " + hashSet.size());
			return hashSet.add(hash);
		} else
			return true;
	}

	/*
	 * Zapisuje do texťáku + vygeneruje nový řádek
	 */
	private static void writeTextInTxt() {
		try {
			writer.write(hash);
			writer.newLine();
			size++;
		} catch (IOException e) {
			System.err.println("Error while writing hash!!!");
		}
	}

	private static String sha256(String input, int bytNumber) {
		sb.delete(0, sb.length());
		/*
		try {
			mDigest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			System.err.println("No SHA-256 algorith.");
			return null;
		}
		result = mDigest.digest(input.getBytes());*/
		/*sha.update(input.getBytes());
		sha.digest(result, result.length, bytNumber);*/
		result = sha.digest(input.getBytes());
		for (int i = 0; i < bytNumber; i++) {
			sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
		
	}

	public static double getTime() {
		return System.currentTimeMillis();
	}

	/*
	 * !!Metoda se v aktuální verzi nepoužívá 
	 * 
	 */
	public static void findHash() {
		try {
			writer.close();
			reader = new BufferedReader(new FileReader(file));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.err.println("Closing writer or opening reader error. Text file can be corrupted.");
		}
		System.out.println("======================================================================================");
		String before = null;
		String now;
		long rounds = 0;
		while (size > rounds) {
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
			if (now.equals(hash)) {
				System.out.println("Before: " + before + " at " + (rounds));
				System.out.println("After:  " + now + " at " + (rounds + 1));
			}
			before = now;
			rounds++;
		}
		System.out.println("======================================================================================");
		try {
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * Aktuální metoda pro nalezení kolizních hashů v textovém souboru
	 */
	public static void findHashMathInTXT() {
		try {
			writer.close();
			hashSet.clear();
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
		

	}

	/*
	 * 712780769 rounds 7644.444s
	 * =========================================================================
	 * ============= Before: 7761e863bdfb05 at 50464057 After: ae6074abe8657d at
	 * 50464058 Before: 7761e863bdfb05 at 89104341 After: ae6074abe8657d at
	 * 89104342
	 * =========================================================================
	 * =============
	 * 
	 */
/*	
	712780768
	7761e863bdfb05
	712780769
	ae6074abe8657d
	Written in hashSet: 44552170
	712780769 rounds
	Generating time: 8165.966s
	======================================================================================
	Our hash found on line: 403726834
	Our hash found on line: 712780768
	======================================================================================
	Starting loking in past to find the first duplicite...
	ae6074abe8657d x ae6074abe8657d
	7761e863bdfb05 x 7761e863bdfb05
	c965a9ff5cf296 x c965a9ff5cf296
	0ce6c9ffa704d4 x 0ce6c9ffa704d4
	4c411089586964 x 4c411089586964
	5d27e94c7f843f x 5d27e94c7f843f
	35ff3e24e4d688 x 35ff3e24e4d688
	0da01fad201db3 x 0da01fad201db3
	======================================================================================
	Searching time: 130.008s
	Total time: 8295.974s ==> cca. 2 hodiny a čtvrt
*/
	
	/*
======================================================================================
Our hash found on line: 403726834
Our hash found on line: 712780768
======================================================================================
Found!
Starting loking in past to find the first duplicite...
ae6074abe8657d x ae6074abe8657d
7761e863bdfb05 x 7761e863bdfb05
c965a9ff5cf296 x c965a9ff5cf296
0ce6c9ffa704d4 x 0ce6c9ffa704d4
4c411089586964 x 4c411089586964
5d27e94c7f843f x 5d27e94c7f843f
35ff3e24e4d688 x 35ff3e24e4d688
0da01fad201db3 x 0da01fad201db3
8a7a25fb4115b4 x 8a7a25fb4115b4
865760b30a35b1 x 865760b30a35b1
369b7b3708cc43 x 369b7b3708cc43
93615891e28672 x 93615891e28672
0ffbea9f0cfd1e x 0ffbea9f0cfd1e
d6ed2c4701db67 x d6ed2c4701db67
1e63acf43b134a x f217310bbc8a5c ==> naše kolizní hashe
======================================================================================
	 * */
	
	
	
	/*Testing diferent sha:
	 * New sha 32 time: 0.855s
	 * Old sha 32 time: 0.921s
	 * New sha 40 time: 22.015s
	 * Old sha 40 time: 22.047s
	 * New sha 48 time: 101.358s; druhy pokus 96.364s; bez vypisu 56.413s; final debuger: 14.144s a normal: 12.791s 
	 * Old sha 48 time: 102.561s
	 */
	
/*	
	Začátek programu
	:D
	712781359 rounds
	Generating time: 717.445s
	======================================================================================
	Our hash found on line: 403727424
	Our hash found on line: 712781358
	======================================================================================
	Starting loking in past to find the first duplicite...
	ab17c173e2f824 x ab17c173e2f824
	e6c972d77cc747 x e6c972d77cc747
	8c1a4cb663c313 x 8c1a4cb663c313
	5e3424947a885d x 5e3424947a885d
	42a7b455f84a91 x 42a7b455f84a91
	cafb452e834607 x cafb452e834607
	e68b69833fe268 x e68b69833fe268
	7a2279c88943a0 x 7a2279c88943a0
	a907d436fbd326 x a907d436fbd326
	9daaa27cb0d2d2 x 9daaa27cb0d2d2
	94a648c429392d x 94a648c429392d
	3ec6c8e00f7f90 x 3ec6c8e00f7f90
	669b02e17b921a x 669b02e17b921a
	44a77a51192e4d x 44a77a51192e4d
	d40df0059de1b0 x d40df0059de1b0
	78c6da761cb121 x 78c6da761cb121
	5ca6335a7b83ff x 5ca6335a7b83ff
	54dcdbe2687145 x 54dcdbe2687145
	d29762fad87293 x d29762fad87293
	4decf3634895d3 x 4decf3634895d3
	81dc62e1596cc9 x 81dc62e1596cc9
	509b706d32329c x 509b706d32329c
	4388303c1a4655 x 4388303c1a4655
	e21ccbe2fbb30c x e21ccbe2fbb30c
	9e286c6dc11a5e x 9e286c6dc11a5e
	2bb4894122087e x 2bb4894122087e
	7206486db024af x 7206486db024af
	8d030523b1432b x 8d030523b1432b
	3ab588b8790037 x 3ab588b8790037
	3847ba29afbc28 x 3847ba29afbc28
	5b296d7abe8001 x 5b296d7abe8001
	7a89a979e0bf3e x 7a89a979e0bf3e
	d8955d7877774b x d8955d7877774b
	ef324c0682c425 x ef324c0682c425
	21b165420d98e5 x 21b165420d98e5
	a9595170aff57b x a9595170aff57b
	83631dc201820e x 83631dc201820e
	5edffca01cf610 x 5edffca01cf610
	95977a483c67fa x 95977a483c67fa
	447dcebbf7362b x 447dcebbf7362b
	2fb25cd6200909 x 2fb25cd6200909
	33e6040da98a91 x 33e6040da98a91
	857bfa79c1b2cf x 857bfa79c1b2cf
	c3631d7931482b x c3631d7931482b
	f2865c40902623 x f2865c40902623
	100c2d0128784a x 100c2d0128784a
	f90641f0469d57 x f90641f0469d57
	d7342c6c5802fb x d7342c6c5802fb
	82827a0a3be9ea x 82827a0a3be9ea
	d9e44f90dffb8e x d9e44f90dffb8e
	52c4ef4b8dccdb x 52c4ef4b8dccdb
	2c2226a92bdf84 x 2c2226a92bdf84
	77f34318a709dc x 77f34318a709dc
	b4e7096cb109ab x b4e7096cb109ab
	664235414ed53f x 664235414ed53f
	bf03df2ff04ff0 x bf03df2ff04ff0
	3cdeb8e3af15d4 x 3cdeb8e3af15d4
	fd98f54651d96e x fd98f54651d96e
	e1c70dec859967 x e1c70dec859967
	edc09f29981be5 x edc09f29981be5
	ec2d9b1c770fa7 x ec2d9b1c770fa7
	8d17b1e8d0176f x 8d17b1e8d0176f
	b4eeb420cc3228 x b4eeb420cc3228
	d1490dccfb2829 x d1490dccfb2829
	71c0cf17b4fdd3 x 71c0cf17b4fdd3
	f50555fea6cb82 x f50555fea6cb82
	a8541aaf9d490a x a8541aaf9d490a
	c1d72a3dd1d735 x c1d72a3dd1d735
	92ace9b40c79d7 x 92ace9b40c79d7
	0a85a071f04247 x 0a85a071f04247
	4e2151712ce0b7 x 4e2151712ce0b7
	a51c3b9bda5aff x a51c3b9bda5aff
	976aadcc8bd74d x 976aadcc8bd74d
	6f828bbaba6e92 x 6f828bbaba6e92
	772fc63bc548b6 x 772fc63bc548b6
	461c428c10bc06 x 461c428c10bc06
	04186d5bc1f749 x 04186d5bc1f749
	34c6d84d38f6eb x 34c6d84d38f6eb
	fe481f3410acc5 x fe481f3410acc5
	d1f8613f7a4c97 x d1f8613f7a4c97
	4ac308b4cfcac1 x 4ac308b4cfcac1
	c4f0302aa67e74 x c4f0302aa67e74
	d47e2684a99d0c x d47e2684a99d0c
	310618e59f1bf5 x 310618e59f1bf5
	f91c92530ba918 x f91c92530ba918
	c0ce5fe4b13773 x c0ce5fe4b13773
	cdb989e2271b98 x cdb989e2271b98
	443f9c3d1072e9 x 443f9c3d1072e9
	e1a5b0fccd9881 x e1a5b0fccd9881
	33457cbab9edeb x 33457cbab9edeb
	505417a63a4965 x 505417a63a4965
	3e33952911e2b0 x 3e33952911e2b0
	f4c42005a5c641 x f4c42005a5c641
	729824d1243a66 x 729824d1243a66
	69644b240d5a4a x 69644b240d5a4a
	60dec2ce41d80e x 60dec2ce41d80e
	32070f3b189e38 x 32070f3b189e38
	9b861cd41f140c x 9b861cd41f140c
	78de0eba5dd356 x 78de0eba5dd356
	00190d397e26c3 x 00190d397e26c3
	83d23cadf43f9a x 83d23cadf43f9a
	b0c25222ee342b x b0c25222ee342b
	bb0cb1b60d9281 x bb0cb1b60d9281
	8145401c585528 x 8145401c585528
	5691b7d11c4cbf x 5691b7d11c4cbf
	6f39463fed3b1a x 6f39463fed3b1a
	47f340b6dab4ac x 47f340b6dab4ac
	0fb113905e3ef8 x 0fb113905e3ef8
	9675f358b2a243 x 9675f358b2a243
	02b953938d6d94 x 02b953938d6d94
	5769b91ac55576 x 5769b91ac55576
	868e23d14bf9f8 x 868e23d14bf9f8
	166c5574a3c86b x 166c5574a3c86b
	cf19236c625700 x cf19236c625700
	5aac9e5364720f x 5aac9e5364720f
	850be6d4ac9382 x 850be6d4ac9382
	823d7f0228caa8 x 823d7f0228caa8
	d149ec2b33575a x d149ec2b33575a
	83e0bb9e76f96c x 83e0bb9e76f96c
	2bdbac52872021 x 2bdbac52872021
	c40b97c20fd851 x c40b97c20fd851
	65f4ffca625792 x 65f4ffca625792
	403a9ae7c7d6d5 x 403a9ae7c7d6d5
	3698e1b4ba5554 x 3698e1b4ba5554
	7ba4feda2842e8 x 7ba4feda2842e8
	b3e2779c347a93 x b3e2779c347a93
	67da1c694a86db x 67da1c694a86db
	4c8f6272885b25 x 4c8f6272885b25
	5a1d99b11b91aa x 5a1d99b11b91aa
	71b3390d09a769 x 71b3390d09a769
	95e15d1871f8be x 95e15d1871f8be
	60a44382ca8cce x 60a44382ca8cce
	a1d5077e0e5dbe x a1d5077e0e5dbe
	7003788fc19874 x 7003788fc19874
	dac75f441a99f4 x dac75f441a99f4
	564831f05d58cb x 564831f05d58cb
	6cb22bbfd3f027 x 6cb22bbfd3f027
	ea517e6b9f5f9d x ea517e6b9f5f9d
	4c505345859403 x 4c505345859403
	6f6ac16ac97978 x 6f6ac16ac97978
	330a01836c7bf9 x 330a01836c7bf9
	20a5dd5a3408cc x 20a5dd5a3408cc
	02c48e21b9323d x 02c48e21b9323d
	8234fb95fb95ff x 8234fb95fb95ff
	8e2621ca2b695b x 8e2621ca2b695b
	b538f681a214eb x b538f681a214eb
	aca2e97077392b x aca2e97077392b
	278867eae07d3b x 278867eae07d3b
	d47047032f4ff4 x d47047032f4ff4
	e4602bb20b9c7d x e4602bb20b9c7d
	f06b103aef9b3d x f06b103aef9b3d
	e1818deba3cc4a x e1818deba3cc4a
	709369fdb6cc86 x 709369fdb6cc86
	ea943e41d9a424 x ea943e41d9a424
	876d9764fff508 x 876d9764fff508
	edb4adf9b2ebdb x edb4adf9b2ebdb
	7f52d8407aa81c x 7f52d8407aa81c
	f872089d361017 x f872089d361017
	3b1aec43f63a82 x 3b1aec43f63a82
	6b67d633b2ba2e x 6b67d633b2ba2e
	db18093ee13ab0 x db18093ee13ab0
	f85aff29dc6b4e x f85aff29dc6b4e
	fc5480cbff7bc7 x fc5480cbff7bc7
	208f645222f17d x 208f645222f17d
	413066477a9237 x 413066477a9237
	6ddf12fe44356f x 6ddf12fe44356f
	6bdd9e768a9ed2 x 6bdd9e768a9ed2
	9d3f7fb3af5cc5 x 9d3f7fb3af5cc5
	59ed626da33326 x 59ed626da33326
	2207f09efe8c6e x 2207f09efe8c6e
	84c26db7a97722 x 84c26db7a97722
	3182a5309eb417 x 3182a5309eb417
	49bed6d75ecc72 x 49bed6d75ecc72
	31f7f193471ef3 x 31f7f193471ef3
	2bd8c1ce181a65 x 2bd8c1ce181a65
	ad14a1865eff92 x ad14a1865eff92
	5efe439856bde0 x 5efe439856bde0
	3ddf818c022ee5 x 3ddf818c022ee5
	ba93cb3b335a87 x ba93cb3b335a87
	9a505c03f29ef9 x 9a505c03f29ef9
	3bdbd19b1eb2c9 x 3bdbd19b1eb2c9
	247811b86cc8c6 x 247811b86cc8c6
	bca8d8980be401 x bca8d8980be401
	32f8f2a59bf307 x 32f8f2a59bf307
	64099392ab6b29 x 64099392ab6b29
	297f46cac74555 x 297f46cac74555
	6eed1cf533b9b9 x 6eed1cf533b9b9
	e898d9f6eb5ed7 x e898d9f6eb5ed7
	3947261562cdde x 3947261562cdde
	eb1d449754dea5 x eb1d449754dea5
	ec49e9371d6d15 x ec49e9371d6d15
	2f69682957b734 x 2f69682957b734
	f3aee942d78521 x f3aee942d78521
	9bec8345ef6d13 x 9bec8345ef6d13
	b442e91e564d69 x b442e91e564d69
	a1f46887500656 x a1f46887500656
	7980462b4ab5e5 x 7980462b4ab5e5
	6b481cceb97c0f x 6b481cceb97c0f
	01163c5273511a x 01163c5273511a
	303f15ad354a37 x 303f15ad354a37
	2f83638aa4f1e3 x 2f83638aa4f1e3
	cc9f57c9e55c55 x cc9f57c9e55c55
	5ddda1b1f1a9a3 x 5ddda1b1f1a9a3
	52a6f9261aa118 x 52a6f9261aa118
	4a3b42d8276383 x 4a3b42d8276383
	aa21ef09d94e04 x aa21ef09d94e04
	d13dc4c48f45cd x d13dc4c48f45cd
	64f45a38fbb045 x 64f45a38fbb045
	841e93395b96a0 x 841e93395b96a0
	b21a1440212ebc x b21a1440212ebc
	fe98d2ea632057 x fe98d2ea632057
	6ca5250fe0c672 x 6ca5250fe0c672
	a689e968ec0a81 x a689e968ec0a81
	6db5e42e6d9a2b x 6db5e42e6d9a2b
	3c106cfdb8e2a8 x 3c106cfdb8e2a8
	fe789aaa5395d7 x fe789aaa5395d7
	be820d25ffdb61 x be820d25ffdb61
	c0e0f37c264a6c x c0e0f37c264a6c
	ac6b532bc7e11b x ac6b532bc7e11b
	6231621d61c403 x 6231621d61c403
	38aaee9ff9a7c7 x 38aaee9ff9a7c7
	93976a3577fc1c x 93976a3577fc1c
	36491d7946b682 x 36491d7946b682
	1f807ab2c26800 x 1f807ab2c26800
	8d24e5b5650d0b x 8d24e5b5650d0b
	e901b9e1dd1158 x e901b9e1dd1158
	38a448073dd396 x 38a448073dd396
	293cf32c8216a1 x 293cf32c8216a1
	aa478e39949eb8 x aa478e39949eb8
	dc5e6a0b7cdbbd x dc5e6a0b7cdbbd
	666db86964c913 x 666db86964c913
	a9e70f918f1237 x a9e70f918f1237
	b2f942806d8469 x b2f942806d8469
	d62cbf585ee8ad x d62cbf585ee8ad
	624053cb6c5463 x 624053cb6c5463
	417e66fa8c36e6 x 417e66fa8c36e6
	09682ca5100144 x 09682ca5100144
	b6361c5eb6da5d x b6361c5eb6da5d
	6e62d07949c249 x 6e62d07949c249
	0f6a5091113cc3 x 0f6a5091113cc3
	0f840a37ebd5c5 x 0f840a37ebd5c5
	c79c202686f806 x c79c202686f806
	2b9a8974ed6027 x 2b9a8974ed6027
	0cc8e815059904 x 0cc8e815059904
	e032e83b29b254 x e032e83b29b254
	e666a31dc93b8c x e666a31dc93b8c
	0a63a5d56a8149 x 0a63a5d56a8149
	e591a37beaac52 x e591a37beaac52
	e13f545329deb5 x e13f545329deb5
	553cd03980cf40 x 553cd03980cf40
	24e226b2757ffa x 24e226b2757ffa
	7650845ef9c430 x 7650845ef9c430
	65f4b364b6c6c9 x 65f4b364b6c6c9
	b6a4d3839d5a54 x b6a4d3839d5a54
	3229730d2989f0 x 3229730d2989f0
	f908b37e789a21 x f908b37e789a21
	8738119cacfc71 x 8738119cacfc71
	3e4b41a7534cc3 x 3e4b41a7534cc3
	f4d1555379df61 x f4d1555379df61
	4148a606f4cd68 x 4148a606f4cd68
	86488fccf05c6d x 86488fccf05c6d
	4664a64df4eb9a x 4664a64df4eb9a
	7b448919bbd138 x 7b448919bbd138
	346c559cae0f30 x 346c559cae0f30
	f8c0aadad6d807 x f8c0aadad6d807
	1357b7ee451ea7 x 1357b7ee451ea7
	e19ffbe218cd33 x e19ffbe218cd33
	129b6790527de6 x 129b6790527de6
	72f542b32b45e6 x 72f542b32b45e6
	e565989135d7fc x e565989135d7fc
	22f93e5b6a26a1 x 22f93e5b6a26a1
	48d9f222107bb3 x 48d9f222107bb3
	36b118b39572b9 x 36b118b39572b9
	2408d945945b45 x 2408d945945b45
	d0e72aec2bfde7 x d0e72aec2bfde7
	0ad21abd5c85b3 x 0ad21abd5c85b3
	657276b0e3721f x 657276b0e3721f
	c00c608586cd24 x c00c608586cd24
	6268e3272a2e15 x 6268e3272a2e15
	b89560c168d10c x b89560c168d10c
	198d5f1e58afb8 x 198d5f1e58afb8
	7642cda79448ed x 7642cda79448ed
	99a5560541630f x 99a5560541630f
	7a44b51ff48f18 x 7a44b51ff48f18
	595e45e2291c8f x 595e45e2291c8f
	94cb88179f3a61 x 94cb88179f3a61
	8c50cf269ead74 x 8c50cf269ead74
	577cca8af949f5 x 577cca8af949f5
	266afae04106bb x 266afae04106bb
	7a59f6c323adcd x 7a59f6c323adcd
	54ed9afccbf04e x 54ed9afccbf04e
	b665c7d39c4c23 x b665c7d39c4c23
	387d5d569d3f72 x 387d5d569d3f72
	cb793e93e1c3e1 x cb793e93e1c3e1
	71dc9c7d52c63c x 71dc9c7d52c63c
	2ee674afb6edae x 2ee674afb6edae
	92c11e74effbe5 x 92c11e74effbe5
	0bdd31079c33f9 x 0bdd31079c33f9
	b0ed58ac1e1b16 x b0ed58ac1e1b16
	5f65cfa4389026 x 5f65cfa4389026
	6638ad3d23aced x 6638ad3d23aced
	01d36ebf3cb643 x 01d36ebf3cb643
	0791db9cc25c72 x 0791db9cc25c72
	5639eaea9d422e x 5639eaea9d422e
	d55fb9dd51bdae x d55fb9dd51bdae
	7284240c102a61 x 7284240c102a61
	811372889e6b93 x 811372889e6b93
	7d3b3e7f41b6e2 x 7d3b3e7f41b6e2
	e3cfc867906070 x e3cfc867906070
	d99d236b62fe8a x d99d236b62fe8a
	8f82abca707f2d x 8f82abca707f2d
	5a3aa15cf233b4 x 5a3aa15cf233b4
	0039aee2cde5dd x 0039aee2cde5dd
	66496e3d2d77b4 x 66496e3d2d77b4
	0ce1517b2bde9e x 0ce1517b2bde9e
	a59294a1cdf19c x a59294a1cdf19c
	cbc3ff83160588 x cbc3ff83160588
	6cd5e11e8906d8 x 6cd5e11e8906d8
	90f632d746dc1c x 90f632d746dc1c
	07bc61a1031d72 x 07bc61a1031d72
	69b88922ef5dcd x 69b88922ef5dcd
	8b0fbbed87abcb x 8b0fbbed87abcb
	7a4701783b7374 x 7a4701783b7374
	e34261116a3660 x e34261116a3660
	8be7e58d153fde x 8be7e58d153fde
	f1503586a84932 x f1503586a84932
	810faa19dbbfc8 x 810faa19dbbfc8
	049e33caaa94f6 x 049e33caaa94f6
	ec43ad4b83b9e4 x ec43ad4b83b9e4
	cd7861ecbc400d x cd7861ecbc400d
	2cf50d08bb99b3 x 2cf50d08bb99b3
	0c58cbc3cd572c x 0c58cbc3cd572c
	f7e0cab7806ec0 x f7e0cab7806ec0
	b68904c52a0e7d x b68904c52a0e7d
	cce8c947a2b9a6 x cce8c947a2b9a6
	41e8e2be609ee2 x 41e8e2be609ee2
	651c4b87d66130 x 651c4b87d66130
	689fd54ccfc2b3 x 689fd54ccfc2b3
	3c97440cd15476 x 3c97440cd15476
	b03447e884f918 x b03447e884f918
	a32778feebe127 x a32778feebe127
	287c958fad8925 x 287c958fad8925
	a0b3e289eb2479 x a0b3e289eb2479
	09b9b613c83f32 x 09b9b613c83f32
	5c9ebdd71eb157 x 5c9ebdd71eb157
	cbe79c3cb9aa58 x cbe79c3cb9aa58
	8d6abc289b0c83 x 8d6abc289b0c83
	e42f7f4674dfb9 x e42f7f4674dfb9
	8b88b46b5a9a3d x 8b88b46b5a9a3d
	aea0396d03b9af x aea0396d03b9af
	83bfbb7bb4babd x 83bfbb7bb4babd
	e88ea7d3e4f5bb x e88ea7d3e4f5bb
	ed8efee75c5054 x ed8efee75c5054
	228908a9fa37b9 x 228908a9fa37b9
	1c5d8ae4c55cb9 x 1c5d8ae4c55cb9
	17eb042da12125 x 17eb042da12125
	fec0a126c9a065 x fec0a126c9a065
	155a599aaeb9e1 x 155a599aaeb9e1
	73ceec31d83cb1 x 73ceec31d83cb1
	13e36849cffed6 x 13e36849cffed6
	4a403d9f3ea660 x 4a403d9f3ea660
	4fe90cd9efc612 x 4fe90cd9efc612
	b92ee78fb066e1 x b92ee78fb066e1
	74cd3390770aff x 74cd3390770aff
	4e5c869e3ff311 x 4e5c869e3ff311
	3115266479749f x 3115266479749f
	2b903800e342a0 x 2b903800e342a0
	6972ff8cebbf50 x 6972ff8cebbf50
	90d1287af1a61c x 90d1287af1a61c
	10d1732c3cdfcc x 10d1732c3cdfcc
	4bb9b16c22c52d x 4bb9b16c22c52d
	065b9bb7c5ab6a x 065b9bb7c5ab6a
	1e3cfa57ead8b5 x 1e3cfa57ead8b5
	56f94fbe19d2b9 x 56f94fbe19d2b9
	e6462586b97243 x e6462586b97243
	d3671e975545f7 x d3671e975545f7
	f1914716b66e17 x f1914716b66e17
	b9e291f1bc9a18 x b9e291f1bc9a18
	563dc5b724b79d x 563dc5b724b79d
	185b6c76ec6c14 x 185b6c76ec6c14
	8431928dd1d108 x 8431928dd1d108
	a5205356efc4c7 x a5205356efc4c7
	0d31458d7059b6 x 0d31458d7059b6
	3d9b0060dc6e41 x 3d9b0060dc6e41
	c3ef90b97086ae x c3ef90b97086ae
	c03229de4b848d x c03229de4b848d
	903fe74f6c326e x 903fe74f6c326e
	d1954b244e2387 x d1954b244e2387
	7d4235f4a76fcb x 7d4235f4a76fcb
	40efb415a4a90d x 40efb415a4a90d
	1c1d669e286be0 x 1c1d669e286be0
	9fc755fa0b5065 x 9fc755fa0b5065
	5f3c88ff25755c x 5f3c88ff25755c
	91d9a948bd3537 x 91d9a948bd3537
	8486f9ce67e004 x 8486f9ce67e004
	66bf681db05f4f x 66bf681db05f4f
	49c1fb4d5f991b x 49c1fb4d5f991b
	0cc4685556c7d9 x 0cc4685556c7d9
	a22d719158ea96 x a22d719158ea96
	bd10e73a628bb7 x bd10e73a628bb7
	b067f4e911a8a8 x b067f4e911a8a8
	4b294618b81718 x 4b294618b81718
	aa47e418415be7 x aa47e418415be7
	7489f061b8c2e2 x 7489f061b8c2e2
	d47c9d16bee588 x d47c9d16bee588
	f3ce299793cea8 x f3ce299793cea8
	535822cd948a10 x 535822cd948a10
	8ce7d0ddf3f785 x 8ce7d0ddf3f785
	4486e42ac82072 x 4486e42ac82072
	c361ad504adc28 x c361ad504adc28
	a181de19d2d47e x a181de19d2d47e
	57be5187e67e6c x 57be5187e67e6c
	1bb29d4213e20a x 1bb29d4213e20a
	c1ff7121e5b9e4 x c1ff7121e5b9e4
	7867e8683b1503 x 7867e8683b1503
	745ae38c1eff50 x 745ae38c1eff50
	26d94baf0df344 x 26d94baf0df344
	bc183b688387aa x bc183b688387aa
	eb493b4fe7b2e6 x eb493b4fe7b2e6
	f4ff126c08451b x f4ff126c08451b
	80ef68c2afe33c x 80ef68c2afe33c
	e260550374544a x e260550374544a
	2a43f6e04b2b04 x 2a43f6e04b2b04
	3ec1ca0a9b45ef x 3ec1ca0a9b45ef
	116394c9114052 x 116394c9114052
	15468857c5ca79 x 15468857c5ca79
	14c89a862ecd82 x 14c89a862ecd82
	99db0d02d667d9 x 99db0d02d667d9
	9e03f8e917b98b x 9e03f8e917b98b
	031b46c96e11ef x 031b46c96e11ef
	09e421c4c4bd02 x 09e421c4c4bd02
	394eb6ceb59f13 x 394eb6ceb59f13
	12bc58e063e9a8 x 12bc58e063e9a8
	38b2d5e3433710 x 38b2d5e3433710
	10eaf27d889456 x 10eaf27d889456
	c17767b0ecd786 x c17767b0ecd786
	d4e504ffa70532 x d4e504ffa70532
	0ec8454f2df4a2 x 0ec8454f2df4a2
	c52e53d162dbe8 x c52e53d162dbe8
	c2ca47f1b254f6 x c2ca47f1b254f6
	4b6f4f917c8180 x 4b6f4f917c8180
	10b0613f9d0d95 x 10b0613f9d0d95
	7c306f1760b066 x 7c306f1760b066
	9e84c7f8bbbccb x 9e84c7f8bbbccb
	ea41562db3d6e9 x ea41562db3d6e9
	3b26e468b2df5c x 3b26e468b2df5c
	9e30d50c6c0fd9 x 9e30d50c6c0fd9
	8447f2d55d74f0 x 8447f2d55d74f0
	3196c385589599 x 3196c385589599
	1b272e40a394e0 x 1b272e40a394e0
	a77904951498a0 x a77904951498a0
	97024f8013e549 x 97024f8013e549
	3b285cec87b23d x 3b285cec87b23d
	06882199539456 x 06882199539456
	0e76e98ff4b8e6 x 0e76e98ff4b8e6
	a0dba41de7e83a x a0dba41de7e83a
	ea1f79c7cf0c26 x ea1f79c7cf0c26
	23ad5b660e9844 x 23ad5b660e9844
	44a48ef79f1987 x 44a48ef79f1987
	fc679a583a7677 x fc679a583a7677
	72c05b5c293671 x 72c05b5c293671
	9bf8279e196883 x 9bf8279e196883
	81edd2eb674674 x 81edd2eb674674
	245a5ad7659b08 x 245a5ad7659b08
	a310e34a027b6d x a310e34a027b6d
	861135e0eeee60 x 861135e0eeee60
	b412a49f7c0c39 x b412a49f7c0c39
	b9517911a34a3e x b9517911a34a3e
	3db83690b3010f x 3db83690b3010f
	a255df2403ec39 x a255df2403ec39
	1f88f681b49f1b x 1f88f681b49f1b
	4b7e63476af85c x 4b7e63476af85c
	1eafc740661f8f x 1eafc740661f8f
	8ddab6de066324 x 8ddab6de066324
	3915d94441f08e x 3915d94441f08e
	b4c3073bb90885 x b4c3073bb90885
	40af3c6c13dacb x 40af3c6c13dacb
	8bf0024ff582bd x 8bf0024ff582bd
	18673f83ba5e98 x 18673f83ba5e98
	5f314738a41cbf x 5f314738a41cbf
	75f75b869d6836 x 75f75b869d6836
	b813b839c40a11 x b813b839c40a11
	9b5e45fea02de2 x 9b5e45fea02de2
	400b66cf5bc533 x 400b66cf5bc533
	c6a68e6716b24d x c6a68e6716b24d
	568a6adcaf9ef5 x 568a6adcaf9ef5
	b507ccf765010b x b507ccf765010b
	4841831424cd35 x 4841831424cd35
	5715478b3d88b1 x 5715478b3d88b1
	248782098f51a6 x 248782098f51a6
	48e8ba2593189b x 48e8ba2593189b
	c2c4cae756dd38 x c2c4cae756dd38
	2da4c206bd1e75 x 2da4c206bd1e75
	55d3165eec55aa x 55d3165eec55aa
	74df7fdb9a5d8a x 74df7fdb9a5d8a
	eaa25eed0c6581 x eaa25eed0c6581
	154dcc4faba99a x 154dcc4faba99a
	527c612636146c x 527c612636146c
	e4722fee57da01 x e4722fee57da01
	bb4cfe09a46f22 x bb4cfe09a46f22
	43470989e32132 x 43470989e32132
	229eeb5d5a5fbd x 229eeb5d5a5fbd
	8445d9cd460416 x 8445d9cd460416
	328b062c730a7b x 328b062c730a7b
	59645bc14b6d45 x 59645bc14b6d45
	f0887b9624e055 x f0887b9624e055
	5f70951e3b27c7 x 5f70951e3b27c7
	36ed80fb9b7691 x 36ed80fb9b7691
	6ce0a5183bfdf1 x 6ce0a5183bfdf1
	9db82867038173 x 9db82867038173
	ac505e2a0f2d2f x ac505e2a0f2d2f
	cfb9bc85189419 x cfb9bc85189419
	3ca00c5d1926cf x 3ca00c5d1926cf
	9001498ff4d215 x 9001498ff4d215
	091d92650c60a2 x 091d92650c60a2
	0388156562f3ae x 0388156562f3ae
	7160a391416e27 x 7160a391416e27
	debd22bd73a805 x debd22bd73a805
	5beedaea98b963 x 5beedaea98b963
	5965568b05b194 x 5965568b05b194
	f8ad3c104740e9 x f8ad3c104740e9
	f5a30b494b9d2f x f5a30b494b9d2f
	a5193ef9131b60 x a5193ef9131b60
	be1dc3cbaba335 x be1dc3cbaba335
	4d0d43d3e3691f x 4d0d43d3e3691f
	ea4483081f0f87 x ea4483081f0f87
	8cb4e92de51da1 x 8cb4e92de51da1
	3451a495dac524 x 3451a495dac524
	a9b6e57b3616e2 x a9b6e57b3616e2
	2f12b143f8d72b x 2f12b143f8d72b
	a772c3b30d6dfd x a772c3b30d6dfd
	9d3e81126630c1 x 9d3e81126630c1
	b79f95d1980f77 x b79f95d1980f77
	3de0e7bf2b7223 x 3de0e7bf2b7223
	372296c440a6a5 x 372296c440a6a5
	67a602ff9f268d x 67a602ff9f268d
	3ad98d4f11bb36 x 3ad98d4f11bb36
	e3dcdf111e79c4 x e3dcdf111e79c4
	117e9027613934 x 117e9027613934
	5b0ef8a1758e2a x 5b0ef8a1758e2a
	7a113597cfa0b1 x 7a113597cfa0b1
	582c317b2c2216 x 582c317b2c2216
	64e3678270b49c x 64e3678270b49c
	833dce960b53a0 x 833dce960b53a0
	8ff1107718b2ae x 8ff1107718b2ae
	17291e2652537e x 17291e2652537e
	5e7f9bc2dc94ad x 5e7f9bc2dc94ad
	025ee1b1a682b9 x 025ee1b1a682b9
	7e887394c9b0a2 x 7e887394c9b0a2
	3b897c3a3bd1d3 x 3b897c3a3bd1d3
	1c479cb64172df x 1c479cb64172df
	d7a030a9d508aa x d7a030a9d508aa
	a0a45e965b31eb x a0a45e965b31eb
	fa3ff365738773 x fa3ff365738773
	2b046f302e0189 x 2b046f302e0189
	4f9a534a5d8dda x 4f9a534a5d8dda
	5ba0160ae2d4e3 x 5ba0160ae2d4e3
	0cd618d38b0759 x 0cd618d38b0759
	4317d35e4481d7 x 4317d35e4481d7
	8027083865f684 x 8027083865f684
	7e77789b90e998 x 7e77789b90e998
	d8a363f18956dc x d8a363f18956dc
	d35a568b70d780 x d35a568b70d780
	5a9c0dcdbcbddf x 5a9c0dcdbcbddf
	8637a1332d3f8a x 8637a1332d3f8a
	eb26c34181abea x eb26c34181abea
	e0ebd3028d8326 x e0ebd3028d8326
	2f6076c0b78494 x 2f6076c0b78494
	cb9c6737e004f8 x cb9c6737e004f8
	23f8f6702a6ff4 x 23f8f6702a6ff4
	7b893735c286e2 x 7b893735c286e2
	1cb104f90299a4 x 1cb104f90299a4
	14c40810c846cc x 14c40810c846cc
	8423bdf9e4968e x 8423bdf9e4968e
	23c7d7c254f449 x 23c7d7c254f449
	d4f76e55dc2384 x d4f76e55dc2384
	eb5c82897ccf2f x eb5c82897ccf2f
	7e88da0405da4a x 7e88da0405da4a
	8181db35410b1f x 8181db35410b1f
	707f7d53e36748 x 707f7d53e36748
	038f018141725e x 038f018141725e
	c350df4f76a425 x c350df4f76a425
	17354b10cf66fe x 17354b10cf66fe
	8b8323b3d1afae x 8b8323b3d1afae
	d246b5401385d1 x d246b5401385d1
	211d0ce829855f x 211d0ce829855f
	cff9af3f5ece9a x cff9af3f5ece9a
	9d83244662f30a x 9d83244662f30a
	7eb7de1630a4e8 x 7eb7de1630a4e8
	8c7fb670058876 x 8c7fb670058876
	ae6074abe8657d x ae6074abe8657d
	7761e863bdfb05 x 7761e863bdfb05
	c965a9ff5cf296 x c965a9ff5cf296
	0ce6c9ffa704d4 x 0ce6c9ffa704d4
	4c411089586964 x 4c411089586964
	5d27e94c7f843f x 5d27e94c7f843f
	35ff3e24e4d688 x 35ff3e24e4d688
	0da01fad201db3 x 0da01fad201db3
	8a7a25fb4115b4 x 8a7a25fb4115b4
	865760b30a35b1 x 865760b30a35b1
	369b7b3708cc43 x 369b7b3708cc43
	93615891e28672 x 93615891e28672
	0ffbea9f0cfd1e x 0ffbea9f0cfd1e
	d6ed2c4701db67 x d6ed2c4701db67
	1e63acf43b134a x f217310bbc8a5c
	======================================================================================
	Searching time: 135.071s
	Total time: 852.516s
	Found!*/
	
	

}
