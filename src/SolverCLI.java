import java.util.Scanner;
import java.util.InputMismatchException;
import java.io.FileNotFoundException;

/**
 * Provides a command-line interface for the Solver class.
 */
public class SolverCLI
{
    /**
     * Provides the main command-line interface.
     */
    public static void mainInterface()
    {
        Scanner s = new Scanner(System.in);
        Solver solver = new Solver();
        boolean stop = false;

        try {
            do {
                displayOptions();
                int choice = s.nextInt();
                s.nextLine();
                switch (choice) {
                    case 1:
                        System.out.println("Enter a sudoku: ");
                        System.out.println("(Format: 1~9 for numbers, any other character for empty spaces)");
                        String sudoku = s.nextLine();
                        solver.solvePuzzle(sudoku);
                        break;
                    case 2:
                        System.out.println("Filename: ");
                        String filename = s.nextLine();
                        solver.solveFromFile(filename);
                        break;
                    case 0:
                        stop = true;
                        break;
                }
            }
            while (!stop);
        }
        catch (InputMismatchException ime) {
            System.out.println("Invalid option.");
            mainInterface();
            return;
        }
        catch (NullPointerException npe) {
            System.out.println("Filename not supplied... exiting");
            mainInterface();
            return;
        }
        catch (FileNotFoundException fnfe) {
            System.out.println("File not found... exiting");
            mainInterface();
            return;
        }

        System.out.println("Exiting...");
        s.close();
    }

    /* Displays splash screen for sudoku solver */
    private static void splashScreen()
    {
        System.out.println("-------------\nSudoku Solver\n-------------");
    }

    /* Displays a list of options available for solver */
    private static void displayOptions()
    {
        System.out.print("(1) Enter a sudoku to solve\n");
        System.out.print("(2) Solve sudoku from file\n");
        System.out.println("(0) Quit\n");
        System.out.print("...");

    }

    public static void main(String[] args)
    {
        splashScreen();
        mainInterface();
    }
}
