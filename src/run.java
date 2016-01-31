import sudoku.SudokuFile;
import sudoku.SudokuBoardGenerator;
import sudoku.SudokuBoardReader;
import cspSolver.BTSolver;

class run {
  public static void main(String[] args) {
    
    SudokuBoardGenerator board = new SudokuBoardGenerator();
    SudokuFile sf = board.generateBoard(9,3,3,10);
    System.out.println(sf);

    BTSolver bt = new BTSolver(sf);
    bt.solve();


    }
}
