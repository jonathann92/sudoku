package hw;

import java.util.ArrayList;

import cspSolver.BTSolver;
import cspSolver.BTSolver.ConsistencyCheck;
import cspSolver.BTSolver.ValueSelectionHeuristic;
import cspSolver.BTSolver.VariableSelectionHeuristic;
import sudoku.SudokuBoardGenerator;
import sudoku.SudokuFile;

public class LargestN {

	public static void main(String[] args) {
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
