package A2;

import java.io.BufferedReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
//fawdfawddw
public class Main{

	static int quality = 0;
	static double currentBuffer = 0;
	static int old =0;
	//bufferoccupancy, input 1
	static  int[] input1 = new int[565]; 
	//quality to which the stream changed, input 2
	static int [] input2 = new int[565];
	//quality index of the request, input 3
	static int [] input3 = new int[565];

	public static void main(String[] args) throws IOException {
		int[] bandwidthH = readFile();  //Starts with reading the file log.log given in the assignment
		AdaptivePlayer AdaptivePlayer = new AdaptivePlayer(Integer.parseInt(args[0]),Integer.parseInt(args[1]), bandwidthH.length);

		for(int i=0;i<bandwidthH.length;i++) { // will iterate over one second and gives a new throughput every time.
			System.out.println();
			System.out.println("Iteration: "+i);
			int tempBandWidth = bandwidthH[i];
			System.out.println("tempBandWidth: " + tempBandWidth);
			input1[i] = AdaptivePlayer.getBufferOccupancy();		//gives input 1 the buffer occupancy
			input3[i] = AdaptivePlayer.getQualityIndex();			//gives input 3 the index of the quality
			AdaptivePlayer.Start(tempBandWidth);					//Starts the player

		}

		input2 = AdaptivePlayer.getStreamChange();					// gives input 2 streamchange

		writeFile(input1, input2, input3);

	}
	private static void writeFile(int[] input1, int[] input2, int[] input3) throws IOException { //writes the files.
		FileWriter w1 = new FileWriter("/home/henwe331/Desktop/TDDD66/input1.txt");
		FileWriter w2 = new FileWriter("/home/henwe331/Desktop/TDDD66/input2.txt");
		FileWriter w3 = new FileWriter("/home/henwe331/Desktop/TDDD66/input3.txt");

		for (int i = 0; i < input1.length; i++) {
			w1.write(i + " " + input1[i] + "\n");
			w2.write(i + " " + input2[i] + "\n");
			if(input3[i]!=99) { 						//All values that are 99 will not be written in the file. Only when a new request is given.
				w3.write(i + " " + input3[i] + "\n");			
			}
		}
		w1.close();
		w2.close();
		w3.close();
	}

	public static  int[] readFile () throws IOException { //reads the given file.

		int[] throughputs = new int[565];
		int counter = 0;
		BufferedReader br = new BufferedReader(new FileReader("/home/henwe331/Desktop/TDDD66/log.log"));

		for(String i = br.readLine(); i!=null; i = br.readLine()) {
			String[] columns = i.split(" ");
			throughputs[counter] = Integer.parseInt(columns[4]);
			throughputs[counter]=throughputs[counter]*8/1000; 		//converts to KBit/s
			counter ++;
		}

		return throughputs;
	}



}

