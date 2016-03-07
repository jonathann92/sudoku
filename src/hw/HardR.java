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

public class HardR {
	
	public static void main(String[] args){ 
		
		int hardestR = -1; 
		double longestTime = Double.MIN_VALUE; 
		
		Integer[] M_Values = {4, 8, 12, 16, 17, 18, 19, 20, 21, 22, 24, 28, 32, 36}; 
		
		for (int M = 0; M < M_Values.length; M++){
			System.out.println("M-Value: " + M_Values[M]);
		
			ArrayList<Double> timeCount = new ArrayList<Double>();
			ArrayList<Integer> nodeCount = new ArrayList<Integer>(); 
			int numberOfFailures = 0; 
			int totalLoops = 0; 
			
			for (int i = 0; i < 25; i++){ // Change this line if you want to change the number of loops
				
				totalLoops++; 
				
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
				
				long timeout = 1 * 10000;
				Thread t1 = new Thread(solver);
				try{
					t1.start();
					t1.join();
				} catch(InterruptedException e){ }
				
				timeCount.add(((solver.getPPTimeTaken() + solver.getTimeTaken()) / 1000.0));
				nodeCount.add(solver.getNumAssignments());
				
				if (!solver.hasSolution()){
					
					numberOfFailures += 1; 
					
				}
				
			}
			
			double averageTime = 0.0; // Average Time
			for (int j = 0; j < timeCount.size(); j++){
				
				averageTime += timeCount.get(j); 
				
			}
			
			averageTime = averageTime / (double) timeCount.size();
			
			double averageNodes = 0.0; // Average Nodes
			for (int k = 0; k < nodeCount.size(); k++){
				
				averageNodes += nodeCount.get(k); 
				
			}
			averageNodes = averageNodes / (double) nodeCount.size(); 
			
			double standardDeviation = 0.0; 
			for (int l = 0; l < timeCount.size(); l++){
				
				standardDeviation = standardDeviation + Math.pow(timeCount.get(l) - averageTime, 2); 
				
			}
			standardDeviation = standardDeviation / (double) timeCount.size(); 
			standardDeviation = Math.sqrt(standardDeviation); 
			
			
			System.out.println("Average Time: " + averageTime);
			System.out.println("Standard Deviation of Time: " + standardDeviation);
			System.out.println("Average Nodes: " + averageNodes);
			System.out.println("Success Rate: " + (100 - ((double) numberOfFailures / totalLoops) * 100.0) + "%");
			System.out.println("-----------------------------------------------------------------");
			
			if (averageTime > longestTime){
				
				longestTime = averageTime; 
				hardestR = M_Values[M]; 
				
			}
			
		}
		
		System.out.println("Hardest R: " + hardestR);
		System.out.println("Longest Time: " + longestTime);
		
	}
	
}
