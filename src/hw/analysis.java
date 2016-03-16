package hw;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cspSolver.BTSolver;
import cspSolver.BTSolver.ConsistencyCheck;
import cspSolver.BTSolver.ValueSelectionHeuristic;
import cspSolver.BTSolver.VariableSelectionHeuristic;
import sudoku.SudokuBoardGenerator;
import sudoku.SudokuBoardReader;
import sudoku.SudokuFile;

public class analysis {
	
	public static void main(String[] args) {
		
		
		List<String> a = new ArrayList<String>();


    // Find the bestFlags
    bestFlags();
    
    
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
	}


	public static void statistics(List<String> flags) {
		long timeout = 30 * 1000;
		
		System.out.println(flags);

		
		long nodes = 0;
		
		List<Long> times = new ArrayList<Long>();
		long totalTime = 0;
		
		for(int i = 1; i <= 5; ++i){
			String file = "ExampleSudokuFiles/PM" + i + ".txt";
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

}
