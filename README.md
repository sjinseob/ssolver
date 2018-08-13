# SudokuSolver

A ```9x9```Sudoku solver written for Java. (Written for educational purposes!)

## How this solver is implemented
Inspired by [Dr. Peter Norvig's Sudoku solver](https://github.com/norvig/pytudes/blob/master/py/sudoku.py) (originally written in Python), this solver makes use of *constraint propagation* and *depth-first search* to find solutions for all Sudoku grids.

This program also takes advantage of actual human strategies used in sudoku solving, described by Norvig in his [paper](http://norvig.com/sudoku.html):

1. If a square (in a sudoku grid) has only one possible number remaining, remove that number from its peers (any square that shares a unit with a particular square).
2. If a unit (any collection of boxes, rows or columns that each of every number from 1 to 9) has only one possible place for a value, put it there.

There are some differences between my implementation and Professor Norvig's code. Notwithstanding the fact that this solver is written in Java, this program is considerably larger: the class that deals with the actual solving process has 5 times more lines than the Python code. This is mainly due to the fact I included more documentation, but also because Java is more verbose than Python.

I also didn't include a Sudoku generator, since I decided to focus more on the solving aspect of Sudokus. 

There is also a separate class file for a simple command-line interface for the solver method named ```SolverCLI```, in addition to the main ```Solver``` class.

## Using the SolverCLI Interface
Run ```java SolverCLI``` to display the main terminal interface. You will be presented with three options:

* ```(1) Enter a sudoku to solve```

This option lets you manually enter a sudoku and lets the solver do its job. An unsolved sudoku has the following format:

```...9.31..6.7....8.2.........5....4......6..2..1.......8...7.......3..5.....4....9```

An unsolved sudoku grid is represented as a ```String```.

Each square is represented as a number from 0 to 81, where 0 is the top left square, and the 80 is the bottom right square. Every character in the string represents a value in a square. For example, the character at index 0 (.) is the value at square 0, which is empty. The character at index 3 is 9, therefore the value at square 3 is also 9. Feeding in a Sudoku String will initiate the solving process, and it will print out its result in the terminal screen.

* ```(2) Solve sudoku from file```

If you have a file that has unsolved sudokus on each line, then the solver can solve each and every one of them and print out the results just like when you select the first option.

* ```(0) Quit```

Exit the solver.

## Sources used
[Solving Sudoku puzzles using Depth First Search](http://logicalgenetics.com/solving-sudoku-puzzles-using-depth-first-search/)

["Solving Every Sudoku Puzzle", by Peter Norvig](http://norvig.com/sudoku.html)

[Github page for Norvig's solver](https://github.com/norvig/pytudes/blob/master/py/sudoku.py)
