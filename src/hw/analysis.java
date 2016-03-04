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
		
		System.out.println(arguments);
	}
	
	public static List<String> arguments(int number){
		ArrayList<Character> binary = new ArrayList<Character>(Arrays.asList('0','0','0','0','0','0'));
		String values = Integer.toBinaryString(number);
		for(int i = values.length()-1, j = binary.size() - 1; i >=0 ; --i, --j){
			binary.set(j, values.charAt(i));
		}
		
		
		System.out.println(binary);
		List<String> args = new ArrayList<String>();
		
		if(binary.get(0) == '0')
			args.add("FC");
		if(binary.get(1) == '1')
			args.add("MAC");
		if(binary.get(2) == '1')
			args.add("ACP");
		if(binary.get(3) == '1')
			args.add("MRV");
		if(binary.get(4) == '1')
			args.add("DH");
		if(binary.get(5) == '1')
			args.add("LCV");

		return args;
	}

	public static void main(String[] args) {
		int timeout = 10;
		
//		int P = 3;
//		int Q = 3;
//		int N = P * Q;
//		int M = 12;
//		int R = M / (N*N); 
//		SudokuFile sf = SudokuBoardGenerator.generateBoard(N, P, Q, M);		
		
		String input = "ExampleSudokuFiles/PH1.txt";
		SudokuFile sf = SudokuBoardReader.readFile(input);		
		
		System.out.println(sf);
		
		
		
		// Figure out best combination of flags
		List<Double> times = new ArrayList<Double>();
		List<List<String>> arguments = new ArrayList<List<String>>();

		for(int i = 0; i < 64; ++i){
			BTSolver solver = new BTSolver(sf);
			List<String> flags = arguments(i);
			heuristics(solver, flags);
			
			Thread t1 = new Thread(solver);
			try{
				t1.start();
				t1.join(timeout * 1000);
				if(t1.isAlive()){
					System.out.println(timeout + " Seconds have passed, killing thread");
					t1.interrupt();
				}
			} catch(InterruptedException e){ }
			
			if(solver.hasSolution()){
				System.out.println(solver.getSolution());
				long time = solver.getPPTimeTaken() + solver.getTimeTaken();
				
				System.out.println("TOTAL TIME: " + time / 1000.0);
				times.add(time / 1000.0);
				arguments.add(flags);

			}
			System.out.println("\n*********************\n");
		}
		
		
		
		

	}

}