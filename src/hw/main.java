package hw;

/*
 * Finished:
 * 	FC
 * 	MAC
 * 	ACP
 * 
 * 	
 */

import sudoku.SudokuBoardGenerator;
import sudoku.SudokuFile; 
import sudoku.Odometer; 
import sudoku.Converter; 
import sudoku.SudokuBoardReader; 
import cspSolver.BTSolver.ConsistencyCheck;
import cspSolver.BTSolver.ValueSelectionHeuristic;
import cspSolver.BTSolver.VariableSelectionHeuristic;
import java.util.*;
import java.io.*;  

import cspSolver.BTSolver; 

class main {
	
	private static String printSolution(BTSolver solver){
		
		String solution = "("; 
		int length = solver.getSolution().getN(); 
		
		if (solver.hasSolution()){
			
			int[][] solutionBoard = solver.getSolution().getBoard(); 
			
			for (int k = 0; k < length; k++){
				
				for (int l = 0; l < length; l++){
					
					solution = solution.concat(Integer.toString(solutionBoard[k][l]) + ","); 
					
				}
				
			}
			
			return solution.substring(0, solution.length() - 1).concat(")"); 
			
		}
		
		else {
			
			for (int i = 0; i < length; i++){
				
				for (int j = 0; j < length; j++){
					
					solution = solution.concat("0,"); 
					
				}
				
			}
			
			return solution.substring(0, solution.length() - 1).concat(")"); 
			
		}
		
	}
	
	// Prints the STATUS and SOLUTION_TIME given the circumstances
	private static String printStatus(BTSolver solver, long time){
		
		if (solver.hasSolution()){
			
			return "SOLUTION_TIME=" + ((time) / 1000.0) + "\n" + "STATUS=" + "success"; 
			
		}
		
		else {
			
			return "STATUS=" + "timeout"; 
			
		}
		
	}
		
	private static SudokuFile generateBoardFromFile(String input){
		System.out.println("Generating board from file");
		BufferedReader br = null;
		String[] tokens = null;
		try{
			br = new BufferedReader(new FileReader(input));
			String line = br.readLine();
			tokens = line.split("\\s+");
			if(tokens.length != 4){
				System.err.println("File does not have exactly 4 arguments");
				return null;
			}		
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(br != null){
				try {
					br.close();
				} catch (Exception e2) { e2.printStackTrace(); }
			}
		}
		
		int n = Integer.parseInt(tokens[0]);
		int p = Integer.parseInt(tokens[1]);
		int q = Integer.parseInt(tokens[2]);
		int numAssign = Integer.parseInt(tokens[3]);
		
		return SudokuBoardGenerator.generateBoard(n, p, q, numAssign);
	}
	
	public static void setSolverMethods(BTSolver solver, List<String> arguments){
		solver.setValueSelectionHeuristic(ValueSelectionHeuristic.None);
		solver.setVariableSelectionHeuristic(VariableSelectionHeuristic.None);
		solver.setConsistencyChecks(ConsistencyCheck.None);
		
		if(arguments.contains("FC")){
			System.out.println("Forward Checking");
			solver.setConsistencyChecks(ConsistencyCheck.ForwardChecking);
			solver.preprocessFlags.add("FC");
		}
		if(arguments.contains("MAC")){
			System.out.println("Maintaining Arc Consistency");
			solver.setConsistencyChecks(ConsistencyCheck.ArcConsistency);
		}
		if(arguments.contains("ACP")){
			solver.preprocessFlags.add("ACP");
		}
		
		
	}
	
	public static void main(String[] args) {
		if(args.length < 3){
			System.err.println("Need atleast 3 arguments");
			return;
		}
		List<String> arguments = new ArrayList<String>();
		for(String tag:args){
			arguments.add(tag.toUpperCase());
		}
		
		String inputFile = args[0]; 
		String outputFile = args[1]; 
		int timeOut = Integer.parseInt(args[2]); 
		
		SudokuFile sf = null;

		if(args.length >= 4 && arguments.contains("GEN")){
			sf = generateBoardFromFile(inputFile);
		} else {
			sf = SudokuBoardReader.readFile(inputFile); 
		}
		
		System.out.println(sf.toString()); 
		
		BTSolver solver = new BTSolver(sf); 
		
		setSolverMethods(solver, arguments);

		Thread t1 = new Thread(solver);
		try
		{
			t1.start();
			t1.join(timeOut * 1000);
			if(t1.isAlive())
			{
				System.out.println(timeOut + " Seconds have passed, program will now timeout");
				t1.interrupt();
			}
		}catch(InterruptedException e)
		{
		}
		
		System.out.println(solver.getSolution());
		
		try {
		
			File fileOut = new File(outputFile); 
			PrintWriter outputWriter = new PrintWriter(System.out); 
			
			outputWriter.println("TOTAL_START=" + solver.getStartTime() / 1000.0); 
			outputWriter.println("PREPROCESSING_START=" + solver.getPPStartTime() / 1000.0);
			outputWriter.println("PREPROCESSING_DONE=" + solver.getPPEndTime() / 1000.0);
			outputWriter.println("SEARCH_START=" + solver.getStartTime() / 1000.0); 
			outputWriter.println("SEARCH_DONE=" + solver.getEndTime() / 1000.0); 
			//outputWriter.println("SOLUTION_TIME=" + ((ppEndTime - ppStartTime) + (endTime - solverStartTime)) );
			outputWriter.println(printStatus(solver, (solver.getPPTimeTaken() + solver.getTimeTaken()))); 
			outputWriter.println("SOLUTION=" + printSolution(solver)); 
			outputWriter.println("COUNT_NODES=" + solver.getNumAssignments()); 
			outputWriter.println("COUNT_DEADENDS=" + solver.getNumBacktracks());;
			
			outputWriter.close(); 
			
		}
		catch(Exception e){ e.printStackTrace();}
		
		
	}
	
}