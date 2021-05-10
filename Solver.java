import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Arrays;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * <b>A 9x9 Sudoku Solver.</b> <br>
 * <br>
 * This is an interpretation of a Sudoku Solver that was originally made by Dr. Peter Norvig. <br>
 * Like the original program, this solver also uses constraint propagation and depth-first search to find solutions for
 * all Sudoku grids. <br>
 * <br>
 * Some terminology must be cleared up in order to understand how this class works: <br>
 * {@code unit}: A collection of nine squares that constitute a row, column or box in a sudoku grid. <br>
 * {@code peer}: All squares that share a unit. <br>
 * <br>
 * In his paper, Dr. Norvig talks about implementing human strategies into his solving algorithm. <br>
 * Mainly, these two provide the basis for the constraint propagation technique implemented here: <br>
 * 1. If a square has only one possible number remaining, remove that number from its peers <br>
 * 2. If a unit has only one possible place for a value, put it there <br>
 * <br>
 *
 * <b>Sources Used:</b> <br>
 * <a href="http://norvig.com/sudoku.html">Dr. Norvig's Sudoku Solver</a> <br>
 * <a href="https://github.com/norvig/pytudes/blob/master/py/sudoku.py">The GitHub page for the original code</a> <br>
 * <a href="http://logicalgenetics.com/solving-sudoku-puzzles-using-depth-first-search/">Using depth-first search for solving a Sudoku</a> <br>
 *
 *
 * @author Jinseob Sohn
 */
public class Solver
{
    private static final int SIZE = 81;
    private TreeSet<Integer> allNums = new TreeSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));

    private static ArrayList<TreeSet<Integer>> unitList;
    private static HashMap<Integer, ArrayList<TreeSet<Integer>>> units;
    private static HashMap<Integer, TreeSet<Integer>> peers;

    /* Sudoku grid where the final solution will be displayed */
    private HashMap<Integer, TreeSet<Integer>> markupGrid = new HashMap<>();
    /* Initial unsolved grid */
    private String initialGrid;


    /**
     * Solves a sudoku puzzle and returns the time it took to solve it.
     *
     * @param sudoku An unsolved sudoku
     * @return solve time, -1 if solving fails
     */
    public long solvePuzzle(String sudoku)
    {
        if (unitList == null && units == null && peers == null) buildGrid();

        // Measure the time it takes to solve sudoku
        long startNanos = System.nanoTime();
        HashMap<Integer, TreeSet<Integer>> a = solution(parseGrid(sudoku));
        long nanosDiff = System.nanoTime() - startNanos;

        if (a != null) {
            // display time taken to solve sudoku
            System.out.println("\n[Unsolved Grid]");
            displayInitialGrid();
            System.out.println("\n[Solved Grid]");
            displayGrid();
            System.out.print("\n=> Successfully solved in ");
            System.out.print(nanosDiff / Math.pow(10, 9));
            System.out.println(" seconds");
            System.out.println("====================");
            return nanosDiff;
        }
        System.out.println("Failed to solve Sudoku... exiting");
        return -1;
    }

    /**
     * Solves all sudoku puzzles that are in a file.
     *
     * @param pathToFile Path to file with Sudokus
     * @throws FileNotFoundException specified file is not found
     * @throws NullPointerException file pathname is empty
     */
    public void solveFromFile(String pathToFile) throws NullPointerException, FileNotFoundException
    {
        Scanner sc = new Scanner(new File(pathToFile));
        while (sc.hasNextLine()) {
            solvePuzzle(sc.nextLine());
        }
        sc.close();
    }

    /**
     * Get an average of the solve time for each sudoku puzzle in the
     * file.
     *
     * @deprecated Was used only for light benchmarking, not very necessary for the entire program <br>
     *
     * @param pathToFile Path to file with Sudokus
     * @param iterations Number of solving iterations per sudoku
     */
    public void solveFromFileAverage(String pathToFile, int iterations)
    {
        try {
            Scanner sc = new Scanner(new File(pathToFile));
            long solveTime = 0;
            int n = 0;
            while (sc.hasNextLine()) {
                String s = sc.nextLine();
                for (int i = 0; i < iterations; i++) {
                    long t = solvePuzzle(s);
                    System.out.println(t / Math.pow(10, 9));
                    solveTime += t;
                }
                System.out.println("Puzzle " + ++n + ": " + (solveTime / Math.pow(10, 9)) / iterations + " seconds avg.");
                solveTime = 0;
            }
            sc.close();
        }
        catch (NullPointerException npe) {
            System.out.println("Filename not supplied... exiting");
        }
        catch (FileNotFoundException fnfe) {
            System.out.println("File not found... exiting");
        }
    }

    /*
     * Builds a full list of units and peers necessary to solve the sudoku
     */
    private void buildGrid()
    {
        unitList = new ArrayList<>();
        units = new HashMap<>();
        peers = new HashMap<>();

        // Make ArrayList of units
        buildUnitList();
        // Make a map with keys as the square and values as the units it is in
        buildUnits();
        // For each square, make a list of peers
        buildPeers();
    }

    /* Builds a list of all unit sets in a sudoku grid */
    private void buildUnitList()
    {
        // Row units
        int squareIndex = 0, startingIndex = 0;
        TreeSet<Integer> unitSet;
        for (int i = 0; i < 9; i++) {
            unitSet = new TreeSet<>();
            for (int j = 0; j < 9; j++) {
                unitSet.add(squareIndex++);
            }
            Solver.unitList.add(unitSet);
        }

        // Column units
        for (int i = 0; i < 9; i++) {
            squareIndex = startingIndex;
            unitSet = new TreeSet<>();
            for (int j = 0; j < 9; j++) {
                unitSet.add(squareIndex);
                squareIndex += 9;
            }
            unitList.add(unitSet);
            startingIndex++;
        }        
        squareIndex = 0;

        // Box units
        for (int i = 0; i < 9; i++) {
            if (i % 3 == 0 && i != 0)
                squareIndex -= 6;
            else if (i != 0)
                squareIndex -= 24;

            unitSet = new TreeSet<>();
            for (int j = 0; j < 3; j++) {                
                for (int k = 0; k < 3; k++) {
                    unitSet.add(squareIndex);        
                    squareIndex++;            
                }
                squareIndex += 6;
            }
            Solver.unitList.add(unitSet);
        }     
    }

    /* Builds a list of unit sets for each square. */
    private void buildUnits() 
    {
        ArrayList<TreeSet<Integer>> unitArray;
        for (int sq = 0; sq < SIZE; sq++) {
            unitArray = new ArrayList<>();
            for (TreeSet<Integer> u : unitList) {                
                if (u.contains(sq))
                    unitArray.add(u);
            }
            Solver.units.put(sq, unitArray);
        }
    }

    /* Builds a set of peers for each square. */
    private void buildPeers() 
    {
        TreeSet<Integer> peerSet;
        for (int sq : Solver.units.keySet()) {
            peerSet = new TreeSet<>();
            for (TreeSet<Integer> p : Solver.units.get(sq)) {   
                peerSet.addAll(p);             
            }
            peerSet.remove(sq);
            Solver.peers.put(sq, peerSet);
        }
    }


    /*
     * Parses a given Sudoku grid.
     * 
     * Any character that is not a number between 1-9 will be interpreted as a blank square.
     * Also propagates while assigning values to the squares if necessary.
     * The initial unsolved Sudoku grid is supplied in the form of a String
     * and parsed into a HashMap<Integer, TreeSet<Integer>>. 
     */
    private HashMap<Integer, TreeSet<Integer>> parseGrid(String grid)
    {   
        if (grid.length() < SIZE) {
            System.out.println("Parse Error: Sudoku must be longer than 81");
            return null; // invalid sudoku - all grids must be 81 or over in length
        }
        initialGrid = grid;     
        int sq = 0;
        
        for (int i = 0; i < SIZE; i++)
            this.markupGrid.put(i, new TreeSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9)));

        for (char c : grid.toCharArray()) {
            if (sq >= SIZE) break;
            int number = Character.getNumericValue(c);
            if (allNums.contains(number) && assignValue(this.markupGrid, sq, number) == null) {
                return null; // Contradiction if you can't assign number to square (unsolvable sudoku)
            }
            sq++;
        }
        return this.markupGrid;
    }


    /*
     * Assigns a value to a specified square.
     * (Defined as "eliminate values other than 'value' from square")
     */
    private HashMap<Integer, TreeSet<Integer>> assignValue(HashMap<Integer, TreeSet<Integer>> grid, int square, int value)
    {
        TreeSet<Integer> otherValues = new TreeSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));
        otherValues.remove(value);

        // Eliminate all other values from square
        ArrayList<Boolean> eliminated = new ArrayList<>();
        for (int i : otherValues) {
            if (eliminateValue(grid, square, i) != null)
                eliminated.add(true);
            else
                eliminated.add(false);
        }
        if (allTrue(eliminated))
            return grid;
        return null;
    }

    /*
     * Eliminates a value from a specified square.
     *
     * Relies on two human strategies to solving sudoku, per Norvig:
     * 1. If a square has only one possible number remaining, remove that number from its peers
     * 2. If a unit has only one possible place for a value, put it there
     */
    private HashMap<Integer, TreeSet<Integer>> eliminateValue(HashMap<Integer, TreeSet<Integer>> grid, int square, int value) 
    {
        // If value isn't present in square, it has already been removed
        if (!grid.get(square).contains(value)) {
            return grid; // Return early
        }

        // Remove value from square
        grid.get(square).remove(value);

        // Contradiction when all possibilities in square are removed
        if (grid.get(square).size() == 0) {
            return null; 
        }

        // 1. If square has one possibility remaining, then remove that value from its peers (propagate)
        if (grid.get(square).size() == 1) {
            int i = grid.get(square).first();
            for (int peer : Solver.peers.get(square)) {
                eliminateValue(grid, peer, i);
            }
        }

        // 2. If there is only one place for a value, put it there
        for (TreeSet<Integer> unit : Solver.units.get(square)) {
            ArrayList<Integer> potentialPlaces = new ArrayList<>();
            for (int i : unit) {
                if (grid.get(i).contains(value)) {
                    potentialPlaces.add(i); // potential squares
                }
            }
            if (potentialPlaces.size() == 0) {
                return null; // Contradiction if there is no spot within the unit for a number
            }
            else if (potentialPlaces.size() == 1) {
                if (assignValue(grid, potentialPlaces.get(0), value) == null) {
                    return null; // Contradiction if you can't assign the value
                }
            }
        }
        return grid; // elimination successful
    }

    /*
     * Uses depth-first search and constraint propagation to
     * try all possible values in a sudoku grid.
     */
    private HashMap<Integer, TreeSet<Integer>> solution(HashMap<Integer, TreeSet<Integer>> grid) 
    {
        return solutionRecursive(grid);
    }

    /*
     * Recursive helper function that searches for a solution in a sudoku grid.
     */
    private HashMap<Integer, TreeSet<Integer>> solutionRecursive(HashMap<Integer, TreeSet<Integer>> grid) 
    {
        // Failed earlier to find solution
        if (grid == null) return null; 

        // System.out.println(grid);
        // System.out.println('\n');
        // Contradiction if there are no possibilities left in any square
        if (emptySquareExists(grid)) return null;

        // Check if the sudoku is completely solved
        ArrayList<Boolean> lengthOne = new ArrayList<>();
        for (TreeSet<Integer> possibilities : grid.values()) {
            if (possibilities.size() == 1) {
                lengthOne.add(true);
                continue;
            }
            lengthOne.add(false);
            break; // not solved
        }
        // If all cells have only one possibility, then the puzzle is solved.
        if (allTrue(lengthOne)) return grid; // solved

        // Choose square with fewest possibilities
        int fewestSquare = smallestPossibilityGrid(grid);
        // fewestSquare = 10;  // DEBUG : DELETE LATER
        TreeSet<Integer> allPossibilities = new TreeSet<>(grid.get(fewestSquare));

        

        // Try assigning each possibility one by one
        for (int possibilities : allPossibilities) {
            // System.out.println("Smallest square: " + fewestSquare);
            // System.out.println("Possibility: " + possibilities);
            HashMap<Integer, TreeSet<Integer>> a = solutionRecursive(assignValue(DeepCopy.copy(grid), fewestSquare, possibilities));
            if (a != null) {
                this.markupGrid = a;
                return a;   
            }         
        }

        return null; // sudoku ultimately unsolvable - exit
    }

    /*
     * Returns the square with smallest number of possibilities > 1
     */
    private int smallestPossibilityGrid(HashMap<Integer, TreeSet<Integer>> grid)
    {
        int min = 10;   // Minimum number of possibilities
        int minSq = -1; // Square with minimum possibilities
        for (int square : grid.keySet()) {
            // TreeSet<Integer> possibilities = grid.get(square);
            int size = grid.get(square).size();
            if (size > 1 && size <= min) {
                min = size;
                minSq = square;
            }
        }
        return minSq;
    }

    
    /* Checks whether all items in ArrayList are all true */
    private boolean allTrue(ArrayList<Boolean> a) 
    {
        return (!a.contains(false));
    }

    /* Checks whether there is square with no possibilities left in the grid */
    private boolean emptySquareExists(HashMap<Integer, TreeSet<Integer>> hm) 
    {
        for (int i : hm.keySet()) {
            // System.out.println(i);
            if (hm.get(i).size() == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Displays the sudoku grid in graphical form <br>
     * <br>
     * Ex: (a solved sudoku)
     *
     * <pre>
        9 8 6 | 3 2 4 | 1 5 7
        1 2 4 | 7 5 9 | 3 6 8
        5 3 7 | 8 6 1 | 4 2 9
        ------+-------+------
        4 1 3 | 2 8 5 | 9 7 6
        6 9 5 | 1 7 3 | 2 8 4
        2 7 8 | 9 4 6 | 5 1 3
        ------+-------+------
        3 4 2 | 6 1 7 | 8 9 5
        8 6 9 | 5 3 2 | 7 4 1
        7 5 1 | 4 9 8 | 6 3 2
       </pre>
     *
     */
    public void displayGrid() 
    {
        for (int i : markupGrid.keySet()) {
            // Print borders if necessary
            if (i % 3 == 0 && i != 0 && i % 9 != 0)
                System.out.print("| ");
            if (i % 9 == 0 & i != 0)
                System.out.println();
                if (i % 27 == 0 && i != 0)
                    System.out.println("------+-------+------");

            if (markupGrid.get(i).size() == 0) {
                System.out.print('.');
            }
            else {
                for (int j : markupGrid.get(i)) {
                    System.out.print(j);                
                }
            }
            System.out.print(' ');
        }
        System.out.println('\n');
    }

    /**
     * Displays an unsolved Sudoku (initialSudoku) in graphical form. <br>
     * <br>
     * Ex.) If an unsolved sudoku is represented like so, <br>
     * {@code "4.....8.5.3..........7......2.....6.....8.4......1.......6.3.7.5..2.....1.4"} <br>
     * it will be displayed like this:
     *
     * <pre>
        4 . . | . . . | 8 . 5 
        . 3 . | . . . | . . . 
        . . . | 7 . . | . . . 
        ------+-------+------
        . 2 . | . . . | . 6 . 
        . . . | . 8 . | 4 . . 
        . . . | . 1 . | . . . 
        ------+-------+------
        . . . | 6 . 3 | . 7 . 
        5 . . | 2 . . | . . . 
        1 . 4 | . . . | . . .
        </pre>
     *
     * with empty squares represented by periods (.).
     *
     */
    public void displayInitialGrid() 
    {
        int i = 0;
        for (char c : initialGrid.toCharArray()) {
            if (i >= SIZE) break;
            // Print borders if necessary
            if (i % 3 == 0 && i != 0 && i % 9 != 0)
                System.out.print("| ");
            if (i % 9 == 0 & i != 0)
                System.out.println();
                if (i % 27 == 0 && i != 0)
                    System.out.println("------+-------+------");

            int number = Character.getNumericValue(c);
            if (allNums.contains(number))
                System.out.print(number);
            else
                System.out.print('.');
            System.out.print(" ");
            i++;
        }   
        System.out.println('\n');
    }

    private static final class DeepCopy 
    {
        /* Returns a duplicate of HashMap<Integer, TreeSet<Integer>> type objects */
        private static HashMap<Integer, TreeSet<Integer>> copy(HashMap<Integer, TreeSet<Integer>> hm)
        {
            HashMap<Integer, TreeSet<Integer>> copy = new HashMap<>();
    
            for (HashMap.Entry<Integer, TreeSet<Integer>> entry : hm.entrySet()) {
                copy.put(entry.getKey(), new TreeSet<>(entry.getValue()));
            }
            return copy;
        }
    }


    /* Main function for testing */
    public static void main(String[] args)
    {
//        Solver s = new Solver();
//        String sudoku;

        /*
         * I.
         * Tests to check if buildGrid is working properly.
         */
//         System.out.println("Printing out units for each square...");
//         System.out.println(s.getUnits()); // good
//         System.out.println("Printing out peers for each square...");
//         System.out.println(s.getPeers()); // good
//         System.out.println("Building grid...");
//         s.buildGrid();

        /*
         * II.
         * Tests to check if parseGrid (and subsequently assignValue, eliminateValue) is working properly
         * Puzzles from http://norvig.com/easy50.txt
         *
         * The first five puzzles tested below are easy enough so that only the parseGrid,
         * assignValue & eliminateValue functions are able to solve them.
         */
//         sudoku = "003020600900305001001806400008102900700000008006708200002609500800203009005010300";
//         sudoku = "200080300060070084030500209000105408000000000402706000301007040720040060004010003";
//         sudoku = "000000907000420180000705026100904000050000040000507009920108000034059000507000000";
//         sudoku = "030050040008010500460000012070502080000603000040109030250000098001020600080060020";
//         sudoku = "020810740700003100090002805009040087400208003160030200302700060005600008076051090";
//         s.buildGrid();
//         s.parseGrid(sudoku);
//         s.displayInitialGrid(); // display unsolved grid
//         s.displayGrid(); // display the grid

        /*
         * III.
         * Testing solveSudoku (the main solver function)
         * Puzzles from http://magictour.free.fr/top95
         *
         * The first ten puzzles from the source, all solved without error.
         */
//         sudoku = "4.....8.5.3..........7......2.....6.....8.4......1.......6.3.7.5..2.....1.4......";
//         sudoku = "52...6.........7.13...........4..8..6......5...........418.........3..2...87.....";
//         sudoku = "6.....8.3.4.7.................5.4.7.3..2.....1.6.......2.....5.....8.6......1....";
//         sudoku = "48.3............71.2.......7.5....6....2..8.............1.76...3.....4......5....";
//         sudoku = "....14....3....2...7..........9...3.6.1.............8.2.....1.4....5.6.....7.8...";
//         sudoku = "......52..8.4......3...9...5.1...6..2..7........3.....6...1..........7.4.......3.";
//         sudoku = "6.2.5.........3.4..........43...8....1....2........7..5..27...........81...6.....";
//         sudoku = ".524.........7.1..............8.2...3.....6...9.5.....1.6.3...........897........";
//         sudoku = "6.2.5.........4.3..........43...8....1....2........7..5..27...........81...6.....";
//         sudoku = ".923.........8.1...........1.7.4...........658.........6.5.2...4.....7.....9.....";
//         s.buildGrid();
//         s.solvePuzzle(sudoku);
//         s.displayInitialGrid(); // display unsolved grid
//         s.displayGrid(); // display the grid

        /*
         * IV.
         * Testing for edge cases
         */
        /* 1. If the supplied unsolved sudoku is less than 81 in length */
//         (5 squares missing: should return a Parse Error)
//         sudoku = "4.....8.5.3..........7......2.....6.....8.4......1.......6.3.7.5..2.....1.4";
//         s.solvePuzzle(sudoku); // Parse Error

        /* 2. If the supplied unsolved sudoku is more than 81 in length */
//         (5 squares over: only the first 81 characters (squares) are parsed.
//         The rest are ignored)
//         sudoku = "4.....8.5.3..........7......2.....6.....8.4......1.......6.3.7.5..2.....1.4....../..12345";
//         s.solvePuzzle(sudoku);
//         s.displayInitialGrid();
//         s.displayGrid();

        /* 3. Testing how solvePuzzle reacts to a blank sudoku */
//         (Solves on its own)
//         sudoku = ".................................................................................";
//         s.solvePuzzle(sudoku);
//         s.displayInitialGrid();
//         s.displayGrid();

        /* 4. Testing how solvePuzzle reacts to a completely solved sudoku */
//         (Parses the grid, then finishes almost immediately - total solving time is around 2E-5 seconds)
//         sudoku = "417369825632158947958724316825437169791586432346912758289643571573291684164875293";
//         s.solvePuzzle(sudoku);
//         s.displayInitialGrid();
//         s.displayGrid();

        /* 5. Testing how solvePuzzle reacts to sudokus with unknown characters */
//         (Parses the grid correctly. Any unknown character that is not a digit from 1-9
//         will be treated as a blank square)
//         sudoku = "4...?.8/5.3%.)....!..7.^.&..2.*.@.6...(.8.4..)...1...$...6.3.7.5..2.!.#.1.4~.....";
//         s.solvePuzzle(sudoku);
//         s.displayInitialGrid();
//         s.displayGrid();

        /*
         * VI.
         * Miscellaneous Tests
         */
//         Solver s = new Solver();
//         Source: http://magictour.free.fr/top95
//         s.solveFromFile("/Users/jsohn/Desktop/top95.txt");

//         Source: https://www.kaggle.com/bryanpark/sudoku
//         s.solveFromFile("/Users/jsohn/Desktop/Sudoku.csv");

//         Source: http://magictour.free.fr/top1465
//         s.solveFromFile("/Users/jsohn/Desktop/top1465.txt");
    }
}