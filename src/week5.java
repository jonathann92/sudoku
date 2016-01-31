

import sudoku.SudokuFile;
import sudoku.SudokuBoardGenerator;
import sudoku.SudokuBoardReader;
import cspSolver.BTSolver;
import java.util.*;
import java.io.*;

import cspSolver.BTSolver.*;


class week5 {
	
	public static SudokuFile generateBoardFromFile(String input){
		int n = 0, p = 0, q = 0, d = 0;
		String line = null;
		
		try (BufferedReader br = new BufferedReader(new FileReader(input))) {
		    line = br.readLine();
		    } catch (IOException e1) {
		    	// TODO
		    	
		    }
		
		String[] tokens = line.split(" ");
		n = Integer.parseInt(tokens[0]);
		p = Integer.parseInt(tokens[1]);
		q = Integer.parseInt(tokens[2]);
		d = Integer.parseInt(tokens[3]);
		
	    return SudokuBoardGenerator.generateBoard(n,p,q,d);
	}
	
  public static void main(String[] args) {
		String input = args[0];
		String output = args[1];
		int timeout = Integer.parseInt(args[2]);
		
	  if(args.length < 4){
		  System.out.println("Not enough aruments. We need option flags");
		  return;
	  }
	  
	  SudokuFile sf = null;
	  
	  if(args[3].equals("GEN")){
		  sf = generateBoardFromFile(input);
	  } else if (args[3].equals("BT")){
		  sf = SudokuBoardReader.readFile(input);
	  } else {
		  System.out.println("Unexpected option flag");
		  return;	  
	  }
	  System.out.println(sf);
    
	BTSolver solver = new BTSolver(sf);
	
	solver.setConsistencyChecks(ConsistencyCheck.None);
	solver.setValueSelectionHeuristic(ValueSelectionHeuristic.None);
	solver.setVariableSelectionHeuristic(VariableSelectionHeuristic.None);
	
	
	Thread t1 = new Thread(solver);
	try
	{
		t1.start();
		t1.join(timeout*1000);
		if(t1.isAlive())
		{
			t1.interrupt();
		}
	}catch(InterruptedException e)
	{
		System.out.println("Time out after: " + timeout + " seconds");
	}
	
	System.out.println(solver.getSolution().toString());
    

    }
}
