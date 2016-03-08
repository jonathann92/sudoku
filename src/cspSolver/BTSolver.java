package cspSolver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import cspSolver.BTSolver.ConsistencyCheck;
import sudoku.Converter;
import sudoku.SudokuFile;
/**
 * Backtracking solver.
 *
 */
public class BTSolver implements Runnable{

	//===============================================================================
	// Properties
	//===============================================================================

	private ConstraintNetwork network;
	//private static Trail trail = Trail.getTrail();
	private boolean hasSolution = false;
	private SudokuFile sudokuGrid;

	private int numAssignments;
	private int numBacktracks;
	private long startTime;
	private long endTime;
	
	//Added
	private long totalStartTime;
	private long ppStartTime;
	private long ppEndTime;

	public enum VariableSelectionHeuristic 	{ None, MinimumRemainingValue, Degree, MRVDH };
	public enum ValueSelectionHeuristic 		{ None, LeastConstrainingValue };
	public enum ConsistencyCheck				{ None, ForwardChecking, ArcConsistency };
	
	//Added
	public List<String> preprocessFlags = null;

	private VariableSelectionHeuristic varHeuristics;
	private ValueSelectionHeuristic valHeuristics;
	private ConsistencyCheck cChecks;
	//===============================================================================
	// Constructors
	//===============================================================================

	public BTSolver(SudokuFile sf)
	{
		this.network = Converter.SudokuFileToConstraintNetwork(sf);
		this.sudokuGrid = sf;
		numAssignments = 0;
		numBacktracks = 0;
		this.preprocessFlags = new ArrayList<String>();
	}

	//===============================================================================
	// Modifiers
	//===============================================================================

	public void setVariableSelectionHeuristic(VariableSelectionHeuristic vsh)
	{
		this.varHeuristics = vsh;
	}

	public void setValueSelectionHeuristic(ValueSelectionHeuristic vsh)
	{
		this.valHeuristics = vsh;
	}

	public void setConsistencyChecks(ConsistencyCheck cc)
	{
		this.cChecks = cc;
	}

	
	//===============================================================================
	// Accessors
	//===============================================================================

	/**
	 * @return true if a solution has been found, false otherwise.
	 */
	public boolean hasSolution()
	{
		return hasSolution;
	}

	/**
	 * @return solution if a solution has been found, otherwise returns the unsolved puzzle.
	 */
	public SudokuFile getSolution()
	{
		return sudokuGrid;
	}

	public void printSolverStats()
	{
		System.out.println("Time taken:" + (endTime-startTime) + " ms");
		System.out.println("Number of assignments: " + numAssignments);
		System.out.println("Number of backtracks: " + numBacktracks);
	}

	/**
	 *
	 * @return time required for the solver to attain in seconds
	 */
	public long getTimeTaken()
	{
		return endTime-startTime;
	}
	
	// Added
	public long getTotalStartTime(){
		return totalStartTime;
	}
	
	public long getStartTime(){
		return startTime;
	}
	
	public long getEndTime(){
		return endTime;
	}
	public long getPPStartTime(){
		return ppStartTime;
	}
	
	public long getPPEndTime(){
		return ppEndTime;
	}
	
	public long getPPTimeTaken(){
		return ppEndTime - ppStartTime;
	}
	
	public long getTotalTime(){
		return endTime - totalStartTime;
	}
	//

	public int getNumAssignments()
	{
		return numAssignments;
	}

	public int getNumBacktracks()
	{
		return numBacktracks;
	}

	public ConstraintNetwork getNetwork()
	{
		return network;
	}

	//===============================================================================
	// Helper Methods
	//===============================================================================

	/**
	 * Checks whether the changes from the last time this method was called are consistent.
	 * @return true if consistent, false otherwise
	 */
	private boolean checkConsistency(Variable v)
	{
		boolean isConsistent = false;
		switch(cChecks)
		{
		case None: 				isConsistent = assignmentsCheck();
		break;
		case ForwardChecking: 	isConsistent = forwardChecking(v);
		break;
		case ArcConsistency: 	isConsistent = arcConsistency(v);
		break;
		default: 				isConsistent = assignmentsCheck();
		break;
		}
		return isConsistent;
	}

	/**
	 * default consistency check. Ensures no two variables are assigned to the same value.
	 * @return true if consistent, false otherwise.
	 */
	private boolean assignmentsCheck()
	{
		for(Variable v : network.getVariables())
		{
			if(v.isAssigned())
			{
				for(Variable vOther : network.getNeighborsOfVariable(v))
				{
					if (v.getAssignment() == vOther.getAssignment())
					{
						return false;
					}
				}
			}
		}
		return true;
	}
	
	private boolean isConsistent(Variable v){
		for(Variable neighbor: network.getNeighborsOfVariable(v)){
			if(v.getAssignment() == neighbor.getAssignment())
				return false;
		}
		return true;
	}
	
	/**
	 * PreProcess Forward Checking
	 */
	
	private boolean ppForwardChecking(){
		for(Variable v : network.getVariables()){
			if(v.isAssigned() && !forwardChecking(v))
				return false;
		}
		
		return true;
	}

	/**
	 * TODO: Implement forward checking.
	 */
	private boolean forwardChecking(Variable v){
		int num = v.getAssignment();
		for(Variable vOther : network.getNeighborsOfVariable(v)){
			vOther.removeValueFromDomain(num);
			if(vOther.size() == 0 || ( vOther.isAssigned() && !isConsistent(vOther))) return false;
		}
		return true;
	}

	/**
	 * TODO: Implement Maintaining Arc Consistency.
	 */

	// A
	private boolean arcConsistency(Variable v)
	{
		int arc = 1;
		// 0 == lecture slides 2x as slow as jonathan
		// 1 = jonathan and colin
		
		if(arc == 0){
			Queue<Variable[]> queue = new LinkedList<Variable[]>();
			Set<Variable[]> set = new HashSet<Variable[]>();
			for(Variable vOther : network.getNeighborsOfVariable(v)){
				Variable[] pair = {vOther, v};
				queue.add(pair);
				set.add(pair);
			}
			
			while(!queue.isEmpty()){
				Variable[] pair = queue.remove();
				set.remove(pair);
				Variable v1 = pair[0];
				Variable v2 = pair[1];
				int oldSize = v1.size();
				v1.removeValueFromDomain(v2.getAssignment());
				if(v1.size() == 0) return false;
				if(v1.size() > 1 || v1.size() == oldSize) continue;
				List<Variable> neighbors = network.getNeighborsOfVariable(v1);
				neighbors.remove(v2);
				for(Variable k : neighbors){
					Variable[] newPair = {k, v1};
					if(!set.contains(newPair)){
						queue.add(newPair);
						set.add(newPair);
					}
				}
			}
		
			return true;
			}

		else if (arc == 1){
		
		List<Constraint> constraints = null; 
		
		while (!(constraints = network.getModifiedConstraints()).isEmpty()){
			Set<Constraint> cSet = new HashSet<Constraint>(constraints);
			Set<Variable> unique = new HashSet<Variable>();
			for(Constraint c : cSet){
				for (Variable var : c.vars){
					if(!unique.add(var)) continue;
					if (var.isAssigned() && !forwardChecking(var))
						return false;
				}
			}	
		}
		
		return true; 
		}
		
		System.out.println("Reached bottom of arc consistency");
		return true;
	}

	/**
	 * ACP
	 * PreProcessing for Arc Consistency
	 */
	
	public boolean ACP(){
		for(Variable v : network.getVariables()){
			if(v.isAssigned()){
				v.setModified(true);
			}
		}
		
		List<Constraint> constraints = null; 
		
		while (!(constraints = network.getModifiedConstraints()).isEmpty()){
			Set<Constraint> cSet = new HashSet<Constraint>(constraints);
			Set<Variable> unique = new HashSet<Variable>();
			for(Constraint c : cSet){
				for (Variable var : c.vars){
					if(!unique.add(var)) continue;
					if (var.isAssigned() && !forwardChecking(var))
						return false;
				}
			}	
		}
		
		return true; 		
		/*
		Queue<Variable[]> queue = new LinkedList<Variable[]>();
		Set<Variable[]> set = new HashSet<Variable[]>();
		
		for(Variable v : network.getVariables()){
			if(v.isAssigned()){
				for(Variable vOther : network.getNeighborsOfVariable(v)){
					Variable[] pair = {vOther, v};
					queue.add(pair);
					set.add(pair);
				}
			}
		}
		
		while(!queue.isEmpty()){
			Variable[] pair = queue.remove();
			set.remove(pair);
			Variable v1 = pair[0];
			Variable v2 = pair[1];
			int oldSize = v1.size();
			v1.removeValueFromDomain(v2.getAssignment());
			if(v1.size() == 0) return false;
			if(v1.size() > 1 || v1.size() == oldSize) continue;
			List<Variable> neighbors = network.getNeighborsOfVariable(v1);
			neighbors.remove(v2);
			for(Variable k : neighbors){
				Variable[] newPair = {k, v1};
				if(!set.contains(newPair)){
					queue.add(newPair);
					set.add(newPair);
				}
			}
		}
	
		return true;
		*/
	}

	/**
	 * Selects the next variable to check.
	 * @return next variable to check. null if there are no more variables to check.
	 */
	private Variable selectNextVariable()
	{
		Variable next = null;
		switch(varHeuristics)
		{
		case None: 					next = getfirstUnassignedVariable();
		break;
		case MinimumRemainingValue: next = getMRV();
		break;
		case Degree:				next = getDegree();
		break;
		case MRVDH:					next = getMRVDH();
		break;
		default:					next = getfirstUnassignedVariable();
		break;
		}
		return next;
	}

	/**
	 * default next variable selection heuristic. Selects the first unassigned variable.
	 * @return first unassigned variable. null if no variables are unassigned.
	 */
	private Variable getfirstUnassignedVariable()
	{
		for(Variable v : network.getVariables())
		{
			if(!v.isAssigned())
			{
				return v;
			}
		}
		return null;
	}

	/**
	 * TODO: Implement MRV heuristic
	 * @return variable with minimum remaining values that isn't assigned, null if all variables are assigned.
	 */
	private Variable getMRV()
	{
		Variable next = null;
		int min = Integer.MAX_VALUE;
		
		for(Variable v : network.getVariables()){
			if(!v.isAssigned() && v.size() < min){
				min = v.size();
				next = v;
			}
		}
			
		return next;
	}

	/**
	 * TODO: Implement Degree heuristic
	 * @return variable constrained by the most unassigned variables, null if all variables are assigned.
	 */
	private Variable getDegree()
	{

		Variable next = null;
		int max = -1;
		
		for(Variable v : network.getVariables()){
			if(v.isAssigned()) continue;
			int count = 0;
			for(Variable neighbor : network.getNeighborsOfVariable(v)){
				if(!neighbor.isAssigned())
					++count;
			}
			if(count > max){
				next = v;
				max = count;
			}
		}
		
		return next;
	}
	
	private Variable getMRVDH(){
		Variable next = null;
		List<Variable> ties = new ArrayList<Variable>();
		int min = Integer.MAX_VALUE;
		
		// MRV Part
		for(Variable v : network.getVariables()){
			if(!v.isAssigned()){
				if(v.size() == min){
					ties.add(v);
				} else if (v.size() < min){
					ties.clear();
					ties.add(v);
					min = v.size();
				}
			}
		}
		
		// DH part
		int max = -1;
		
		for(Variable v : ties){
			int count = 0;
			for(Variable neighbor : network.getNeighborsOfVariable(v)){
				if(!v.isAssigned()){
					++count;
				}
			}
			if(count > max){
				next = v;
				max = count;
			}
		}
		
		return next;
	}

	/**
	 * Value Selection Heuristics. Orders the values in the domain of the variable
	 * passed as a parameter and returns them as a list.
	 * @return List of values in the domain of a variable in a specified order.
	 */
	public List<Integer> getNextValues(Variable v)
	{
		List<Integer> orderedValues;
		switch(valHeuristics)
		{
		case None: 						orderedValues = getValuesInOrder(v);
		break;
		case LeastConstrainingValue: 	orderedValues = getValuesLCVOrder(v);
		break;
		default:						orderedValues = getValuesInOrder(v);
		break;
		}
		return orderedValues;
	}

	/**
	 * Default value ordering.
	 * @param v Variable whose values need to be ordered
	 * @return values ordered by lowest to highest.
	 */
	public List<Integer> getValuesInOrder(Variable v)
	{
		List<Integer> values = v.getDomain().getValues();

		Comparator<Integer> valueComparator = new Comparator<Integer>(){

			@Override
			public int compare(Integer i1, Integer i2) {
				return i1.compareTo(i2);
			}
		};
		Collections.sort(values, valueComparator);
		return values;
	}

	/**
	 * TODO: LCV heuristic
	 */
	public List<Integer> getValuesLCVOrder(Variable v)
	{
		ArrayList<Integer[]> values = new ArrayList<Integer[]>();
		
		for(Integer value : v.getDomain().getValues()){
			int count = 0;
			for(Variable neighbor: network.getNeighborsOfVariable(v)){
				if(neighbor.getDomain().contains(value))
					++count;
			}
			Integer[] current = {value, count};
			values.add(current);
		}
		
		Comparator<Integer[]> valueComparator = new Comparator<Integer[]>(){

			@Override
			public int compare(Integer[] i1, Integer[] i2) {
				return i1[1] - i2[1];
			}
		};
		Collections.sort(values, valueComparator);
		
		List<Integer> nextValues = new ArrayList<Integer>();
		for(Integer[] i : values){
			nextValues.add(i[0]);
		}
		
		return nextValues;
	}
	/**
	 * Called when solver finds a solution
	 */
	private void success()
	{
		hasSolution = true;
		sudokuGrid = Converter.ConstraintNetworkToSudokuFile(network, sudokuGrid.getN(), sudokuGrid.getP(), sudokuGrid.getQ());
	}

	//===============================================================================
	// Solver
	//===============================================================================
	
	
	private boolean preprocess(){
		if(this.preprocessFlags.contains("FC")){
			
			//System.out.println("Preprocessing FC");
			if(!ppForwardChecking()) return false;
			//System.out.println("After preprocessing FC");
			//System.out.println(Converter.ConstraintNetworkToSudokuFile(network, sudokuGrid.getN(), sudokuGrid.getP(), sudokuGrid.getQ()));
		} 
		
		if(this.preprocessFlags.contains("ACP")){
			//System.out.println("Preprocessing ACP");
			if(!ACP()) return false;
			//System.out.println("After Preprocessing ACP");
			//System.out.println(Converter.ConstraintNetworkToSudokuFile(network, sudokuGrid.getN(), sudokuGrid.getP(), sudokuGrid.getQ()));
		}
		
		return true;
	}
	
	/**
	 * Method to start the solver
	 */
	public void solve()
	{		
		totalStartTime = System.currentTimeMillis();
		ppStartTime = System.currentTimeMillis();
		boolean pp = preprocess();
		ppEndTime = System.currentTimeMillis();
		startTime = System.currentTimeMillis();
		
		try {
			if(pp)
				solve(0);
		}catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("error with variable selection heuristic.");
		}
		
		endTime = System.currentTimeMillis();
		for(Variable v : network.getVariables()){
			v.clearTrail();
			break;
		}
	}

	/**
	 * Solver
	 * @param level How deep the solver is in its recursion.
	 * @throws Exception 
	 */

	private void solve(int level) throws Exception
	{
		if(!Thread.currentThread().isInterrupted())

		{//Check if assignment is completed
			if(hasSolution)
			{
				return;
			}

			//Select unassigned variable
			Variable v = selectNextVariable();

			//check if the assignment is complete
			if(v == null)
			{
				for(Variable var : network.getVariables())
				{
					if(!var.isAssigned())
					{
						System.out.println(var.getName() +" IS NOT ASSIGNED");
						System.out.println(sudokuGrid);
						throw new VariableSelectionException("Something happened with the variable selection heuristic");
					}
				}
				if(!assignmentsCheck()){
					throw new Exception("Something went wrong!");
				}
				success();
				return;
			}

			//loop through the values of the variable being checked LCV


			for(Integer i : getNextValues(v))
			{
				v.trail.placeBreadCrumb();

				//check a value
				v.updateDomain(new Domain(i));
				numAssignments++;
				boolean isConsistent = checkConsistency(v);

				//move to the next assignment
				if(isConsistent)
				{
					solve(level + 1);
				}

				//if this assignment failed at any stage, backtrack
				if(!hasSolution)
				{
					v.trail.undo();
					numBacktracks++;
				} else {
					return;
				}
			}
		}
	}

	@Override
	public void run() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		solve();
	}
}
