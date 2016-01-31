package hw;

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
	
	// Returns the number of Non-Empty Slots of the Sudoku Board
	private static int countNumberOfNonEmpty(SudokuFile sudoku){
		
		int empty = 0; 
		int [][] board = sudoku.getBoard(); 
		
		for (int i = 0; i < sudoku.getN(); i++){
			
			for (int j = 0; j < sudoku.getN(); j++){
				
				if (board[i][j] != 0)
					empty ++;
				
			}
			
		}
		
		return empty; 
		
	}
	
	private static String printSolution(BTSolver solver){
		
		String solution = "("; 
		int length = solver.getSolution().getN(); 
		
		if (solver.hasSolution()){
			
			int[][] solutionBoard = solver.getSolution().getBoard(); 
			
			for (int k = 0; k < length; k++){
				
				for (int l = 0; l < length; l++){
					
					solution = solution.concat(Integer.toString(solutionBoard[l][k]) + ","); 
					
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
	private static String printStatus(BTSolver solver, long programStartTime, long endTime){
		
		if (solver.hasSolution()){
			
			return "SOLUTION_TIME=" + ((endTime - programStartTime) / 1000) + "\n" + "STATUS=" + "success"; 
			
		}
		
		else {
			
			return "STATUS=" + "timeout"; 
			
		}
		
	}
	
	public static void main(String[] args) {
		
		long programStartTime = System.currentTimeMillis(); 
		
		String inputFile = args[0]; 
		String outputFile = args[1]; 
		int timeOut = Integer.parseInt(args[2]); 
		
		SudokuBoardReader reader = new SudokuBoardReader(); 
		SudokuFile sf = reader.readFile(inputFile); 
		
		System.out.println(sf.toString()); 
		
		BTSolver solver = new BTSolver(sf); 
		
		solver.setConsistencyChecks(ConsistencyCheck.None);
		solver.setValueSelectionHeuristic(ValueSelectionHeuristic.None);
		solver.setVariableSelectionHeuristic(VariableSelectionHeuristic.None);
		
		long startTime = System.currentTimeMillis(); 
		
		Thread t1 = new Thread(solver);
		try
		{
			t1.start();
			t1.join(timeOut * 1000);
			if(t1.isAlive())
			{
				t1.interrupt();
			}
		}catch(InterruptedException e)
		{
		}
		
		long endTime = System.currentTimeMillis(); 
		
		System.out.println(solver.getSolution().toString()); 
		
		try {
		
			File fileOut = new File(outputFile); 
			PrintWriter outputWriter = new PrintWriter(fileOut); 
			
			outputWriter.println("TOTAL_START=" + programStartTime / 1000); 
			outputWriter.println("SEARCH_START=" + startTime / 1000); 
			outputWriter.println("SEARCH_DONE=" + endTime / 1000); 
			outputWriter.println(printStatus(solver, programStartTime, endTime)); 
			outputWriter.println("SOLUTION=" + printSolution(solver)); 
			outputWriter.println("COUNT_NODES=" + countNumberOfNonEmpty(solver.getSolution())); 
			outputWriter.println("COUNT_DEADENDS=" + solver.getNumBacktracks());
			
			outputWriter.close(); 
			
		}
		catch(Exception e){}
		
		
	}
	
}