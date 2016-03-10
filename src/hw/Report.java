package hw;


import java.util.*;

import cspSolver.BTSolver;
import cspSolver.BTSolver.ConsistencyCheck;
import cspSolver.BTSolver.ValueSelectionHeuristic;
import cspSolver.BTSolver.VariableSelectionHeuristic;
import sudoku.SudokuBoardGenerator;
import sudoku.SudokuBoardReader;
import sudoku.SudokuFile;


class Report {
  public static void main(String[] args){

    part2(args);
    hardRpart3(); // part 3
    largeN(); // part 4
    hardRpart5(); // part 5
  } // end main

  public static void part2(String[] args){
     List<String> a = new ArrayList<String>();
		// for(String tag:args){
		// 	arguments.add(tag.toUpperCase());
		// }

    // Find the bestFlags
    //bestFlags();

    // Checking to see flags performance
    // FC
    a.add("FC");
		statistics(a);
    a.clear();

    a.add("MRV");
    statistics(a);
    a.clear();

    a.add("DH");
    statistics(a);
    a.clear();

    a.add("LCV");
    statistics(a);
    a.clear();

    a.add("ACP");
    statistics(a);
    a.clear();

    a.add("MAC");
    statistics(a);
    a.clear();

    a.add("FC");
    a.add("MRV");
    a.add("DH");
    a.add("LCV");
    statistics(a);
    a.clear();

    a.add("FC");
    a.add("MRV");
    a.add("ACP");
    a.add("MAC");
    a.add("LCV");
    statistics(a);
    a.clear();


  }

  public static void statistics(List<String> flags) {
		long timeout = 30 * 1000;

		System.out.println(flags);


		long nodes = 0;

		List<Long> times = new ArrayList<Long>();
		long totalTime = 0;

		for(int i = 1; i <= 5; ++i){
			String file = "ExampleSudokuFiles/PH" + i + ".txt";
			System.out.println(file);
			SudokuFile sf = SudokuBoardReader.readFile(file);
			BTSolver solver = new BTSolver(sf);

			heuristics(solver, flags);

			Thread t1 = new Thread(solver);
			t1.start();
			try {
				t1.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			times.add(solver.getTotalTime());
			totalTime += solver.getTotalTime();
			nodes += solver.getNumAssignments();
			System.out.println("TIME: " + solver.getTotalTime());
			System.out.println("NODES: " + solver.getNumAssignments() + "\n\n");
		}

		double avgTime = totalTime / 5.0;

		double std = 0.0;
		for(Long xi : times){
			std += ( (xi - avgTime) * (xi - avgTime) );
		}

		std /= 5.0;
		std = Math.sqrt(std);

		System.out.println("avg # nodes: " + nodes / 5.0);
		System.out.println("avg time: " + avgTime);
		System.out.println("STD Dev Time: " + std);
	}

	public static void heuristics(BTSolver solver, List<String> arguments){

		solver.setValueSelectionHeuristic(ValueSelectionHeuristic.None);
		solver.setVariableSelectionHeuristic(VariableSelectionHeuristic.None);
		solver.setConsistencyChecks(ConsistencyCheck.None);

		// FC MAC ACP
		if(arguments.contains("FC")){
			//System.out.print("FC ");
			solver.setConsistencyChecks(ConsistencyCheck.ForwardChecking);
			solver.preprocessFlags.add("FC");
		}
		if(arguments.contains("MAC")){
			//System.out.print("MAC ");
			solver.setConsistencyChecks(ConsistencyCheck.ArcConsistency);
		}
		if(arguments.contains("ACP")){
			//System.out.print("ACP ");
			solver.preprocessFlags.add("ACP");
		}

		// MRV DH
		if(arguments.contains("MRV") && arguments.contains("DH")){
			//System.out.print("MRVDH ");
			solver.setVariableSelectionHeuristic(VariableSelectionHeuristic.MRVDH);
		}
		else if (arguments.contains("MRV")){
			//System.out.print("MRV ");
			solver.setVariableSelectionHeuristic(VariableSelectionHeuristic.MinimumRemainingValue);
		}
		else if (arguments.contains("DH")){
//			System.out.print("DH ");
			solver.setVariableSelectionHeuristic(VariableSelectionHeuristic.Degree);
		}

		if(arguments.contains("LCV")){
//			System.out.print("LCV ");
			solver.setValueSelectionHeuristic(ValueSelectionHeuristic.LeastConstrainingValue);
		}


	}

	public static List<String> arguments(int number){
		ArrayList<Character> binary = new ArrayList<Character>(Arrays.asList('0','0','0','0','0'));
		String values = Integer.toBinaryString(number);
		for(int i = values.length()-1, j = binary.size() - 1; i >=0 ; --i, --j){
			binary.set(j, values.charAt(i));
		}


		System.out.println(binary);
		List<String> args = new ArrayList<String>();

		// This is to have FC always in there
		args.add("FC");
		if(binary.get(0) == '0')
			args.add("LCV");
		if(binary.get(1) == '0')
			args.add("MAC");
		if(binary.get(2) == '0')
			args.add("ACP");
		if(binary.get(3) == '0')
			args.add("MRV");
		if(binary.get(4) == '0')
			args.add("DH");

		System.out.println(args);
		return args;
	}

	private static void bestFlags() {
		long timeout =  Long.MAX_VALUE;

		String input = "ExampleSudokuFiles/PM2.txt";
		SudokuFile sf = SudokuBoardReader.readFile(input);

		System.out.println(sf);



		// Figure out best combination of flags
		List<Long> times = new ArrayList<Long>();
		List<List<String>> arguments = new ArrayList<List<String>>();

		for(int i = 0; i < 32; ++i){
			BTSolver solver = new BTSolver(sf);
			List<String> flags = arguments(i);
			heuristics(solver, flags);

			Thread t1 = new Thread(solver);
			try{
				t1.start();
				t1.join(timeout);
				if(t1.isAlive()){
					System.out.println((timeout / 1000.0) + " Seconds have passed, killing thread");
					t1.interrupt();
				}
			} catch(InterruptedException e){ }

			if(solver.hasSolution()){
				System.out.println(solver.getSolution());
				long time = solver.getPPTimeTaken() + solver.getTimeTaken();

				System.out.println("TOTAL TIME: " + time / 1000.0);
				timeout = time+10000;

				times.add(time);
				arguments.add(flags);

			}
			System.out.println("\n*********************\n");
		}

		long bestTime = Long.MAX_VALUE;
		List<String> bestFlags = null;

		for(int i = 0; i < times.size(); ++i){
			if(times.get(i) < bestTime){
				bestFlags = arguments.get(i);
				bestTime = times.get(i);
			}
		}

		System.out.println("Best flags are: " + bestFlags);
		System.out.println("Best time is: " + bestTime);
	}

  public static void hardRpart3(){
    int hardestR = -1;
		double longestTime = Double.MIN_VALUE;


		Integer[] M_Values = {1,2,3,4, 8, 12, 16, 17, 18, 19, 20, 21, 22, 24, 28, 32, 36};

		for (int M = 0; M < M_Values.length; M++){
			System.out.println("M-Value: " + M_Values[M]);

			ArrayList<Double> timeCount = new ArrayList<Double>();
			ArrayList<Integer> nodeCount = new ArrayList<Integer>();
			int numberOfFailures = 0;
			int threadNum = 200;
			int timeouts = 0;
			long end = System.currentTimeMillis() + (60 * 1000);

			Thread[] thread = new Thread[threadNum];
			BTSolver[] btsolver = new BTSolver[threadNum];

			for (int i = 0; i < threadNum; i++){ // Change this line if you want to change the number of loops


				SudokuFile sf = SudokuBoardGenerator.generateBoard(9, 3, 3, M_Values[M]); // Number of Assignments

				BTSolver solver = new BTSolver(sf);

				solver.setValueSelectionHeuristic(ValueSelectionHeuristic.None);
				solver.setVariableSelectionHeuristic(VariableSelectionHeuristic.None);
				solver.setConsistencyChecks(ConsistencyCheck.None);

				solver.setConsistencyChecks(ConsistencyCheck.ForwardChecking);
				solver.setConsistencyChecks(ConsistencyCheck.ArcConsistency);
				solver.setVariableSelectionHeuristic(VariableSelectionHeuristic.MinimumRemainingValue);
				solver.setValueSelectionHeuristic(ValueSelectionHeuristic.LeastConstrainingValue);
				solver.preprocessFlags.add("ACP");
				solver.preprocessFlags.add("FC");

				btsolver[i] = solver;
				thread[i] = new Thread(solver);
				thread[i].start();
			}


			boolean print = true;
			for(int i = 0; i < threadNum; ++i){
				try {
					long time = (end - System.currentTimeMillis());
//					if(print)
//						System.out.println(time);
					thread[i].join(Math.max(1, time));
					if(thread[i].isAlive()){
						print = false;
						thread[i].interrupt();
						++timeouts;
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				BTSolver solver = btsolver[i];


				if(!thread[i].isInterrupted()){
					timeCount.add(((solver.getPPTimeTaken() + solver.getTimeTaken()) / 1000.0));
					nodeCount.add(solver.getNumAssignments());
				}

				if (!solver.hasSolution()){

					numberOfFailures += 1;

				}

			}

			double averageTime = 0.0; // Average Time
			for (int j = 10; j < timeCount.size(); j++){

				averageTime += timeCount.get(j);

			}

			averageTime = averageTime / (double) timeCount.size();

			double averageNodes = 0.0; // Average Nodes
			for (int k = 10; k < nodeCount.size(); k++){

				averageNodes += nodeCount.get(k);

			}
			averageNodes = averageNodes / (double) nodeCount.size();

			double standardDeviation = 0.0;
			for (int l = 10; l < timeCount.size(); l++){

				standardDeviation = standardDeviation + Math.pow(timeCount.get(l) - averageTime, 2);

			}
			standardDeviation = standardDeviation / (double) timeCount.size();
			standardDeviation = Math.sqrt(standardDeviation);


			System.out.println("Average Time: " + averageTime);
			System.out.println("Standard Deviation of Time: " + standardDeviation);
			System.out.println("Average Nodes: " + averageNodes);
			System.out.println("Success Rate: " + (100 - ((double) numberOfFailures / threadNum) * 100.0) + "%");
			System.out.println("Timeouts: " + timeouts);
			System.out.println("-----------------------------------------------------------------");

			if (averageTime > longestTime){

				longestTime = averageTime;
				hardestR = M_Values[M];

			}

		}

		System.out.println("Hardest R: " + hardestR);
		System.out.println("Longest Time: " + longestTime);
  }

  public static void hardRpart5(){
    int hardestR = -1;
		double longestTime = Double.MIN_VALUE;
		int n = 15;
		int p = 3;
		int q = 5;
		int skip = 0;

		double[] R = {0.0494, 0.0494, 0.0988, 0.148, 0.198, 0.210, 0.222, 0.235, 0.247, 0.259, 0.272, 0.296, 0.346, 0.395, 0.444};


		for (int M = 0; M < R.length; M++){
			int m = (int) Math.round(n*n*R[M]);
            System.out.println("R-Value: " + R[M]);
			System.out.println("M-Value: " + m);

			ArrayList<Double> timeCount = new ArrayList<Double>();
			ArrayList<Integer> nodeCount = new ArrayList<Integer>();
			int numberOfFailures = 0;
			int threadNum = 50;
			int timeouts = 0;
			long end = System.currentTimeMillis() + (10 * 60 * 1000);

			Thread[] thread = new Thread[threadNum];
			BTSolver[] btsolver = new BTSolver[threadNum];

			for (int i = 0; i < threadNum; i++){ // Change this line if you want to change the number of loops


				SudokuFile sf = SudokuBoardGenerator.generateBoard(n, p, q, m); // Number of Assignments

				BTSolver solver = new BTSolver(sf);

				solver.setValueSelectionHeuristic(ValueSelectionHeuristic.None);
				solver.setVariableSelectionHeuristic(VariableSelectionHeuristic.None);
				solver.setConsistencyChecks(ConsistencyCheck.None);

				solver.setConsistencyChecks(ConsistencyCheck.ForwardChecking);
				solver.setConsistencyChecks(ConsistencyCheck.ArcConsistency);
				solver.setVariableSelectionHeuristic(VariableSelectionHeuristic.MinimumRemainingValue);
				solver.setValueSelectionHeuristic(ValueSelectionHeuristic.LeastConstrainingValue);
				solver.preprocessFlags.add("ACP");
				solver.preprocessFlags.add("FC");

				btsolver[i] = solver;
				thread[i] = new Thread(solver);
				thread[i].start();
			}


			boolean print = true;
			for(int i = 0; i < threadNum; ++i){
				try {
					long time = (end - System.currentTimeMillis());
//					if(print)
//						System.out.println(time);
					thread[i].join(Math.max(1, time));
					if(thread[i].isAlive()){
						print = false;
						thread[i].interrupt();
						++timeouts;
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				BTSolver solver = btsolver[i];


				if(!thread[i].isInterrupted()){
					timeCount.add(((solver.getPPTimeTaken() + solver.getTimeTaken()) / 1000.0));
					nodeCount.add(solver.getNumAssignments());
				}

				if (!solver.hasSolution()){

					numberOfFailures += 1;

				}

			}

			double averageTime = 0.0; // Average Time
			for (int j = skip; j < timeCount.size(); j++){

				averageTime += timeCount.get(j);

			}

			averageTime = averageTime / (double) timeCount.size();

			double averageNodes = 0.0; // Average Nodes
			for (int k = skip; k < nodeCount.size(); k++){

				averageNodes += nodeCount.get(k);

			}
			averageNodes = averageNodes / (double) nodeCount.size();

			double standardDeviation = 0.0;
			for (int l = skip; l < timeCount.size(); l++){

				standardDeviation = standardDeviation + Math.pow(timeCount.get(l) - averageTime, 2);

			}
			standardDeviation = standardDeviation / (double) timeCount.size();
			standardDeviation = Math.sqrt(standardDeviation);


			System.out.println("Average Time: " + averageTime);
			System.out.println("Standard Deviation of Time: " + standardDeviation);
			System.out.println("Average Nodes: " + averageNodes);
			System.out.println("Success Rate: " + (100 - ((double) numberOfFailures / threadNum) * 100.0) + "%");
			System.out.println("Timeouts: " + timeouts);
			System.out.println("-----------------------------------------------------------------");

			if (averageTime > longestTime){

				longestTime = averageTime;
				hardestR = m;

			}

		}

		System.out.println("Hardest R: " + hardestR);
		System.out.println("Longest Time: " + longestTime);
  }

  public static void largeN(){
    int skip = 0;
		double R = 0.247;
		int[] N = { 9, 12, 15, 16, 18, 20, 21, 24, 27, 28, 30 ,32 ,35};
		int[] P = { 3, 3, 3, 4, 3, 4, 3, 4, 3, 4, 5, 4, 5};
		int[] Q = { 3, 4, 5, 4, 6, 5, 7, 6, 9, 7, 6, 8, 7};
		int size = N.length;

		for(int i = 0; i < size; ++i){
			int n = N[i];
			int p = P[i];
			int q = Q[i];
			int m = (int)Math.round(n*n*R);
			System.out.println("M-Value: " + m);

			if((p*q) != n){
				System.out.println("Error with n p q " + n + ", " + p + ", " + q);
				System.exit(1);
			}

			System.out.println("N P Q = " + n + ", " + p + ", " + q);

			ArrayList<Double> timeCount = new ArrayList<Double>();
			ArrayList<Integer> nodeCount = new ArrayList<Integer>();
			int numberOfFailures = 0;
			int threadNum = 200;
			int timeouts = 0;
			int numSolved = 0;

			Thread[] thread = new Thread[threadNum];
			BTSolver[] btsolver = new BTSolver[threadNum];

			for(int j = 0; j < threadNum; ++j){
				SudokuFile sf = SudokuBoardGenerator.generateBoard(n,p,q, m); // Number of Assignments



				BTSolver solver = new BTSolver(sf);

				solver.setValueSelectionHeuristic(ValueSelectionHeuristic.None);
				solver.setVariableSelectionHeuristic(VariableSelectionHeuristic.None);
				solver.setConsistencyChecks(ConsistencyCheck.None);

				solver.setConsistencyChecks(ConsistencyCheck.ForwardChecking);
				solver.setConsistencyChecks(ConsistencyCheck.ArcConsistency);
				solver.setVariableSelectionHeuristic(VariableSelectionHeuristic.MinimumRemainingValue);
				solver.setValueSelectionHeuristic(ValueSelectionHeuristic.LeastConstrainingValue);
				solver.preprocessFlags.add("ACP");
				solver.preprocessFlags.add("FC");

				btsolver[j] = solver;
				thread[j] = new Thread(solver);
			}

			long end = System.currentTimeMillis() + (60 * 60 * 1000);
			for(Thread t : thread)
				t.start();

			for(int j = 0; j < threadNum; ++j) {
				long time = (end - System.currentTimeMillis());

				try {
					thread[j].join(Math.max(1,  time));
					if(thread[j].isAlive()){
						thread[j].interrupt();
						++timeouts;
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				BTSolver solver = btsolver[j];


				if(solver.hasSolution()){
					timeCount.add(((solver.getPPTimeTaken() + solver.getTimeTaken()) / 1000.0));
					nodeCount.add(solver.getNumAssignments());
					++numSolved;
				} else
					++numberOfFailures;



			} // ends for J

			double averageTime = 0.0; // Average Time
			for (int j = skip; j < timeCount.size(); j++){

				averageTime += timeCount.get(j);

			}

			averageTime = averageTime / (double) timeCount.size();

			double averageNodes = 0.0; // Average Nodes
			for (int k = skip; k < nodeCount.size(); k++){

				averageNodes += nodeCount.get(k);

			}
			averageNodes = averageNodes / (double) nodeCount.size();

			double standardDeviation = 0.0;
			for (int l = skip; l < timeCount.size(); l++){

				standardDeviation = standardDeviation + Math.pow(timeCount.get(l) - averageTime, 2);

			}
			standardDeviation = standardDeviation / (double) timeCount.size();
			standardDeviation = Math.sqrt(standardDeviation);

			System.out.println("Success Rate: " + (100 - ((double) numberOfFailures / threadNum) * 100.0) + "%");
			System.out.println("Num solved: " + numSolved);
			System.out.println("Average Nodes: " + averageNodes);
			System.out.println("Average Time: " + averageTime);
			System.out.println("Standard Deviation of Time: " + standardDeviation);
			System.out.println("Timeouts: " + timeouts);
			System.out.println("-----------------------------------------------------------------");

			if(timeouts == threadNum){
				break;
			}






		}// END FOR I
  }

}
