import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import tester.Tester;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;


// Represents a single square of the game area
class Cell {
  // In logical coordinates, with the origin at the top-left corner of the screen
  int x;
  int y;
  Color color;
  boolean flooded;
  int cellSize = 16;

  public Cell(int x, int y, Color color, boolean flooded) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.flooded = flooded;
  }

  // to draw a cell
  public WorldImage drawCell() {
    return new RectangleImage(this.cellSize, this.cellSize, OutlineMode.SOLID, this.color);
  }

  // to compare if two cells have the same color
  public boolean compareColor(Cell that) {
    return this.color == that.color;
  }

  // to change make this cell's color, the given cell's color
  public void changeColor(Cell clickedCell) {
    this.color = clickedCell.color;
  }

  // to flood the given cell and return those adjacent cells on which we want to call
  // floodCells on the next tick
  public ArrayList<Cell> floodCells(Cell topLeft, ArrayList<Cell> adjacentCellsCopy) {
    Color floodColor = topLeft.color;
    if (this.flooded && this.color != floodColor) {
      this.color = topLeft.color; 
    }  
    if (!this.flooded && this.color == floodColor) {
      this.flooded = true;
    } 
    if (!this.flooded && this.color != floodColor) {
      adjacentCellsCopy.remove(this);
    }
    return adjacentCellsCopy;
  }

  // checks if this cell has the same color as the flood color
  // if it does, it changes this cell's status to flooded
  public void floodThisHuh(Cell first, ArrayList<Cell> adjacentCellsCopy) {
    if (first.color == this.color) {
      this.flooded = true;
    } else {
      adjacentCellsCopy.remove(this);
    }
  }

  //returns the adjacent cells as an array list given the row and column of the current cell
  public ArrayList<Cell> adjacentCells(ArrayList<ArrayList<Cell>> board) {
    int maxLength = board.size() - 1;
    Posn pos = new Utils().findClickedCell(new Posn(this.x, this.y));
    int i = pos.x;
    int j = pos.y;
    if (i == 0 && j == 0) {
      return new ArrayList<Cell>(Arrays.asList(board.get(i).get(j + 1),
          board.get(i + 1).get(j)));
    } else if (i == maxLength && j == maxLength) {
      return new ArrayList<Cell>(Arrays.asList(board.get(i).get(j - 1),
          board.get(i - 1).get(j)));
    } else if (i == 0 && j == maxLength) {
      return new ArrayList<Cell>(Arrays.asList(board.get(i).get(j - 1),
          board.get(i + 1).get(j)));
    } else if (i == maxLength && j == 0) {
      return new ArrayList<Cell>(Arrays.asList(board.get(i).get(j + 1),
          board.get(i - 1).get(j)));
    } else if (i == 0) {
      return new ArrayList<Cell>(Arrays.asList(board.get(i).get(j - 1),
          board.get(i).get(j + 1), board.get(i + 1).get(j)));
    } else if (i == maxLength) {
      return new ArrayList<Cell>(Arrays.asList(board.get(i).get(j - 1),
          board.get(i).get(j + 1), board.get(i - 1).get(j)));
    } else if (j == 0) {
      return new ArrayList<Cell>(Arrays.asList(board.get(i - 1).get(j),
          board.get(i + 1).get(j), board.get(i).get(j + 1)));
    } else if (j == maxLength) {
      return new ArrayList<Cell>(Arrays.asList(board.get(i - 1).get(j),
          board.get(i + 1).get(j), board.get(i).get(j - 1)));
    } else {
      return new ArrayList<Cell>(Arrays.asList(board.get(i - 1).get(j),
          board.get(i + 1).get(j), board.get(i).get(j - 1),
          board.get(i).get(j + 1)));
    }
  }

  // to draw a cell and place it at the appropriate spot on the scene
  public void placeCell(WorldScene scene) {
    scene.placeImageXY(this.drawCell(), this.x, this.y);
  }
}

class Utils {
  // returns the row and column of the cell that was clicked packed in a posn
  public Posn findClickedCell(Posn click) {
    int i = (click.y - 50) / 16 ;
    int j = (click.x - 50) / 16 ;
    return new Posn(i, j);
  }
}

class FloodItWorld extends World {
  // All the cells of the game
  ArrayList<ArrayList<Cell>> board;
  int boardSize;
  int numColors;
  Random rand;
  boolean evaluationMode; // if true, onTick updates adjacent cells to current input
  int steps;
  ArrayList<Cell> adjacentCells;
  ArrayList<Cell> adjacentCellsCopy;
  ArrayList<Cell> visited;
  double timeElapsed;

  // two constructors, one which takes in a random object and another which
  // initializes to a random one
  FloodItWorld(int boardSize, int numColors) {
    if (boardSize > 14 || boardSize < 2) {
      throw new IllegalArgumentException("Board size should be between 2 and 14, given: "
          + boardSize);
    } else {
      this.boardSize = boardSize;
    }
    if (numColors > 8 || numColors < 2) {
      throw new IllegalArgumentException("Number of colors should be between 2 and 8, given: "
          + numColors);
    } else {
      this.numColors = numColors;
    }
    this.rand = new Random();
    this.board = this.initialStateGenerator();
    this.evaluationMode = false;
    this.steps = 0;
    this.adjacentCells = this.board.get(0).get(0).adjacentCells(board);
    this.adjacentCellsCopy = this.board.get(0).get(0).adjacentCells(board);
    this.visited = new ArrayList<Cell>(Arrays.asList(this.board.get(0).get(0)));
    this.initialFloodGenerator();
    this.timeElapsed = 0;
  }

  FloodItWorld(int boardSize, int numColors, Random rand) {
    if (boardSize > 14 || boardSize < 2) {
      throw new IllegalArgumentException("Board size should be between 2 and 14, given: "
          + boardSize);
    } else {
      this.boardSize = boardSize;
    }
    if (numColors > 8 || numColors < 2) {
      throw new IllegalArgumentException("Number of colors should be between 2 and 8, given: "
          + numColors);
    } else {
      this.numColors = numColors;
    }
    this.rand = rand;
    this.board = this.initialStateGenerator();
    this.evaluationMode = false;
    this.steps = 0;
    this.adjacentCells = this.board.get(0).get(0).adjacentCells(board);
    this.adjacentCellsCopy = this.board.get(0).get(0).adjacentCells(board);
    this.visited = new ArrayList<Cell>(Arrays.asList(this.board.get(0).get(0)));
    this.initialFloodGenerator();
    this.timeElapsed = 0;
  }

  // to draw the current board
  public javalib.impworld.WorldScene makeScene() {
    WorldScene scene = new WorldScene(750, 750);
    scene.placeImageXY(new RectangleImage(500, 500, OutlineMode.SOLID, Color.gray), 250, 250);
    for (int i = 0; i < this.boardSize; i++) {
      for (int j = 0; j < this.boardSize; j++) {
        Cell currentCell = this.board.get(i).get(j);
        currentCell.placeCell(scene);        
      }
    }
    scene.placeImageXY(new TextImage("Steps: " + this.steps + " / " + this.maxStepsAllowed(),
        14, FontStyle.BOLD, Color.black), 250, 400);
    scene.placeImageXY(new TextImage("Time elapsed: " + (int)this.timeElapsed + " seconds",
        14, FontStyle.BOLD, Color.black), 250, 375);
    scene.placeImageXY(new TextImage("Flood It!", 24, FontStyle.BOLD, Color.black), 250, 25);    
    scene.placeImageXY(new TextImage("Click cells.", 14, FontStyle.BOLD, Color.black), 340, 100);
    scene.placeImageXY(new TextImage("Fill the board with a single color.", 14, FontStyle.BOLD,
        Color.black), 340, 125);
    scene.placeImageXY(new TextImage("Board Size: " + this.boardSize + " * " + this.boardSize, 14,
        FontStyle.BOLD, Color.black), 340, 150);
    scene.placeImageXY(new TextImage("Number of Colors: " + this.numColors, 14, FontStyle.BOLD,
        Color.black), 340, 175);
    scene.placeImageXY(new TextImage("Press 'r' to refresh the board.", 14, FontStyle.BOLD,
        Color.black), 340, 200);
    return scene;
  }

  // to accept input from the user
  public void onMouseReleased(Posn pos) {
    int maxSteps = this.maxStepsAllowed();
    if (!this.evaluationMode) {
      Posn cellPosition = new Utils().findClickedCell(pos);
      Cell clickedCell = this.board.get(cellPosition.x).get(cellPosition.y);
      if (!clickedCell.compareColor(this.board.get(0).get(0))) {
        this.steps++;
        if (this.steps <= maxSteps) {
          this.evaluationMode = true;
          this.board.get(0).get(0).changeColor(clickedCell);
        } else {
          this.endOfWorld("You ran out of steps!");
        }
      }
    }
  }

  // to refresh the game when the user presses "r"
  public void onKeyReleased(String key) {
    if (!this.evaluationMode && key.equals("r")) {
      this.board = this.initialStateGenerator();
      this.evaluationMode = false;
      this.steps = 0;
      this.adjacentCells = this.board.get(0).get(0).adjacentCells(board);
      this.adjacentCellsCopy = this.board.get(0).get(0).adjacentCells(board);
      this.visited = new ArrayList<Cell>(Arrays.asList(this.board.get(0).get(0)));
      this.initialFloodGenerator();
      this.timeElapsed = 0;
    }
  }

  // to change adjacent cells every tick in response to the user's click
  public void onTick() {
    this.timeElapsed = this.timeElapsed + 0.1;
    if (this.evaluationMode) {
      // to remove cells that we have already visited from the adjacent cells list
      for (Cell c : this.adjacentCells) {
        if (this.visited.contains(c)) {
          this.adjacentCellsCopy.remove(c);
        }
      }
      // to copy the modified list to the adjacentCells list
      this.adjacentCells = new ArrayList<Cell>(0);
      for (Cell e : this.adjacentCellsCopy) { 
        this.adjacentCells.add(e);
      }
      // to visit the unvisited cells and add them to the visited cells list 
      for (Cell c : this.adjacentCells) {
        if (!this.visited.contains(c)) {
          this.adjacentCellsCopy =  c.floodCells(this.board.get(0).get(0), this.adjacentCellsCopy);
          this.visited.add(c);
        }
      }
      // to end the game if it is won or to wait for the user's input if
      // we are done evaluating the previous click or to keep evaluating the current click
      if (this.adjacentCellsCopy.size() == 0) {
        this.evaluationMode = false;
        this.visited = new ArrayList<Cell>(0);
        this.adjacentCells = this.board.get(0).get(0).adjacentCells(board);
        this.adjacentCellsCopy = this.board.get(0).get(0).adjacentCells(board);
        if (this.gameWon()) {
          this.endOfWorld("You win!");
        }
      } else {
        this.adjacentCells = this.findAdjacentCells(this.adjacentCellsCopy, this.visited);
        this.adjacentCellsCopy = new ArrayList<Cell>(0);
        for (Cell e : this.adjacentCells) {
          this.adjacentCellsCopy.add(e);
        }
      }
    }
  }

  // to return a list of adjacent cells of the given list of cells
  public ArrayList<Cell> findAdjacentCells(ArrayList<Cell> currentCells, ArrayList<Cell> visited) {
    ArrayList<Cell> uniqueAdjacentCells = new ArrayList<Cell>();
    for (int i = 0; i < currentCells.size(); i++) {
      Cell c = currentCells.get(i);
      for (Cell cell : c.adjacentCells(board)) {
        if (!uniqueAdjacentCells.contains(cell) && !visited.contains(cell)) {
          uniqueAdjacentCells.add(cell);
        }
      }
    }
    return uniqueAdjacentCells;
  }

  // to end the game if the user runs out of steps
  public javalib.impworld.WorldScene lastScene(String msg) {
    WorldScene scene = new WorldScene(500, 500); 
    scene.placeImageXY(new RectangleImage(500, 500, OutlineMode.SOLID, Color.gray), 250, 250);
    scene.placeImageXY(new TextImage(msg, 14, FontStyle.BOLD, Color.black), 250, 250);
    return scene;
  }

  // to return an array with random colors of size equal to numColors
  public ArrayList<Color> getColors() {
    ArrayList<Color> acceptableColors = new ArrayList<Color>(Arrays.asList(Color.BLACK,
        Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED));
    ArrayList<Color> finalColors = new ArrayList<Color>(this.numColors);
    for (int i = 0; i < this.numColors; i++) {
      int num = this.rand.nextInt(acceptableColors.size());
      Color currentColor = acceptableColors.get(num);
      finalColors.add(currentColor);
      acceptableColors.remove(currentColor);
    }
    return finalColors;
  }

  // to create the initial state of the game
  public ArrayList<ArrayList<Cell>> initialStateGenerator() {
    ArrayList<ArrayList<Cell>> fullBoard = new ArrayList<ArrayList<Cell>>(this.boardSize);
    ArrayList<Color> finalColors = this.getColors();
    for (int i = 0; i < this.boardSize; i++) {
      ArrayList<Cell> currentRow = new ArrayList<Cell>(this.boardSize);
      for (int j = 0; j < this.boardSize; j++) {
        Cell currentCell;
        currentCell = new Cell(58 + j * 16, 58 + i * 16,
            finalColors.get(this.rand.nextInt(this.numColors)), 
            i == 0 && j == 0);
        currentRow.add(currentCell);
      }
      fullBoard.add(currentRow);
    }
    return fullBoard;
  }

  // determines which cells are flooded initially and makes them flooded
  public void initialFloodGenerator() {
    while (this.adjacentCellsCopy.size() != 0) {    
      for (Cell c : this.adjacentCells) {
        if (this.visited.contains(c)) {
          this.adjacentCellsCopy.remove(c);
        }
        if (!this.visited.contains(c)) {
          this.visited.add(c);
          c.floodThisHuh(this.board.get(0).get(0), this.adjacentCellsCopy);
        }
      }
      this.adjacentCells = this.findAdjacentCells(this.adjacentCellsCopy, this.visited);
      this.adjacentCellsCopy = new ArrayList<Cell>();
      for (Cell c : this.adjacentCells) {
        this.adjacentCellsCopy.add(c);
      }
    }
    this.adjacentCells = this.board.get(0).get(0).adjacentCells(board);
    this.adjacentCellsCopy = this.board.get(0).get(0).adjacentCells(board);
    this.visited = new ArrayList<Cell>(Arrays.asList(this.board.get(0).get(0)));
  }

  // to find the maximum number of steps a user is allowed for this game configuration
  public int maxStepsAllowed() {
    return (this.boardSize * Math.round(this.numColors + 1)) / 4 + 1;
  }

  // to check if all the cells are of the same color, that is, 
  // whether the user has won the game
  public boolean gameWon() {
    for (int i = 0; i < this.boardSize; i++) {
      for (int j = 0; j < this.boardSize; j++) {
        boolean sameColor = this.board.get(i).get(j).compareColor(this.board.get(0).get(0));
        if (!sameColor) {
          return false;
        }
      }  
    }
    return true;
  }
}

class ExamplesFloodItWorld {
  FloodItWorld smallWorld;
  FloodItWorld mediumWorld;
  FloodItWorld largeWorld;
  Cell c = new Cell(58, 58, Color.blue, true);
  Cell c2 = new Cell(58, 58, Color.blue, false);
  Cell c3 = new Cell(58, 58, Color.red, false);
  ArrayList<Cell> cellArray1 = new ArrayList<Cell>(Arrays.asList(this.c2, this.c3));
  ArrayList<Cell> cellArray2 = new ArrayList<Cell>(Arrays.asList(this.c2));

  void initData() {
    this.smallWorld = new FloodItWorld(3, 2, new Random(5));
    this.mediumWorld = new FloodItWorld(6, 3, new Random(5));
    this.largeWorld = new FloodItWorld(10, 6, new Random(10)); 
  }

  boolean testCheckConstructorException(Tester t) {
    return t.checkConstructorException(new IllegalArgumentException(
        "Board size should be between 2 and 14, given: 15"), "FloodItWorld", 15, 9)
        && t.checkConstructorException(new IllegalArgumentException(
            "Number of colors should be between 2 and 8, given: 9"), "FloodItWorld", 14, 9);
  }

  // to test the method getColors() in the class FloodItWorld
  void testGetColors(Tester t) {
    initData();
    t.checkExpect(this.mediumWorld.getColors(), new ArrayList<Color>(Arrays.asList(
        Color.GREEN, Color.PINK, Color.RED)));
    t.checkExpect(this.largeWorld.getColors(), new ArrayList<Color>(Arrays.asList(
        Color.MAGENTA, Color.PINK, Color.RED, Color.GREEN, Color.CYAN, Color.BLUE)));
  }

  // to test the method initialStateGenerator() in the class FloodItWorld
  void testInitialStateGenerator(Tester t) {
    initData();
    t.checkExpect(this.smallWorld.initialStateGenerator(), new ArrayList<ArrayList<Cell>>(
        Arrays.asList(new ArrayList<Cell>(Arrays.asList(new Cell(58, 58, Color.PINK, true),
            new Cell(74, 58, Color.GREEN, false),
            new Cell(90, 58, Color.GREEN, false))),
            new ArrayList<Cell>(Arrays.asList(new Cell(58, 74, Color.PINK, false),
                new Cell(74, 74, Color.PINK, false),
                new Cell(90, 74, Color.GREEN, false))),
            new ArrayList<Cell>(Arrays.asList(new Cell(58, 90, Color.GREEN, false),
                new Cell(74, 90, Color.GREEN, false),
                new Cell(90, 90, Color.PINK, false))))));
  }

  // to test the method findClickedCell(Posn) in the class Utils
  void testFindClickedCell(Tester t) {
    Utils util = new Utils();
    t.checkExpect(util.findClickedCell(new Posn(500, 66)), new Posn(1, 28));
  }

  // to test the method drawCell() in the class Cell
  void testDrawCell(Tester t) {
    t.checkExpect(this.c.drawCell(), new RectangleImage(16, 16, OutlineMode.SOLID, Color.BLUE));
  }

  // to test the method compareColor(Cell) in the class Cell
  void testCompareCell(Tester t) {
    t.checkExpect(this.c.compareColor(c2), true);
    t.checkExpect(this.c.compareColor(c3), false);
  }

  // to test the method changeColor(Cell) in the class Cell
  void testChangeColor(Tester t) {
    t.checkExpect(this.c.color, Color.BLUE);
    this.c.changeColor(c3);
    t.checkExpect(this.c.color, Color.RED);
    this.c.changeColor(c2);
  }

  // to test the method floodCells(Cell, ArrayList<Cell>) in the class Cell
  void testFloodCells(Tester t) {
    t.checkExpect(this.c.floodCells(c3, this.cellArray1), this.cellArray1);
    t.checkExpect(this.c.color, Color.RED);
    this.c.color = Color.blue;

    t.checkExpect(this.c2.flooded, false);
    t.checkExpect(this.c2.floodCells(c, this.cellArray1), this.cellArray1);
    t.checkExpect(this.c2.flooded, true);
    this.c2.flooded = false;

    t.checkExpect(this.c3.floodCells(c, this.cellArray1), this.cellArray2);
  }

  // to test the method floodThisHuh(Cell, ArrayList<Cell>) in the class Cell 
  void testFloodThisHuh(Tester t) {
    this.c.flooded = false;
    t.checkExpect(this.c.flooded, false);
    this.c.floodThisHuh(c2, cellArray1);
    t.checkExpect(this.c.flooded, true);
    this.c.flooded = true;

    this.c3.floodThisHuh(c, cellArray1);
    t.checkExpect(this.cellArray1.contains(c3), false);
    this.cellArray1.add(c3);
  }

  // to test the method placeCell(WorldScene) in the class Cell 
  void testPlaceCell(Tester t) {
    WorldScene scene = new WorldScene(500, 500);
    WorldScene scene2 = new WorldScene(500, 500);
    this.c.placeCell(scene);
    scene2.placeImageXY(
        new RectangleImage(16, 16, OutlineMode.SOLID, Color.BLUE), 58, 58);
    t.checkExpect(scene, scene2);
  }

  // to test the method adjacentCells(ArrayList<ArrayList<Cell>>) in the class Cell
  void testAdjacentCells(Tester t) {
    initData();
    t.checkExpect(this.smallWorld.board.get(0).get(0).adjacentCells(this.smallWorld.board),
        new ArrayList<Cell>(Arrays.asList(this.smallWorld.board.get(0).get(1),
            this.smallWorld.board.get(1).get(0))));
    t.checkExpect(this.smallWorld.board.get(0).get(1).adjacentCells(this.smallWorld.board),
        new ArrayList<Cell>(Arrays.asList(this.smallWorld.board.get(0).get(0),
            this.smallWorld.board.get(0).get(2),
            this.smallWorld.board.get(1).get(1))));
    t.checkExpect(this.smallWorld.board.get(2).get(2).adjacentCells(this.smallWorld.board),
        new ArrayList<Cell>(Arrays.asList(this.smallWorld.board.get(2).get(1),
            this.smallWorld.board.get(1).get(2))));
    t.checkExpect(this.smallWorld.board.get(0).get(2).adjacentCells(this.smallWorld.board),
        new ArrayList<Cell>(Arrays.asList(this.smallWorld.board.get(0).get(1),
            this.smallWorld.board.get(1).get(2))));
    t.checkExpect(this.smallWorld.board.get(2).get(0).adjacentCells(this.smallWorld.board),
        new ArrayList<Cell>(Arrays.asList(this.smallWorld.board.get(2).get(1),
            this.smallWorld.board.get(1).get(0))));
    t.checkExpect(this.smallWorld.board.get(1).get(0).adjacentCells(this.smallWorld.board),
        new ArrayList<Cell>(Arrays.asList(this.smallWorld.board.get(0).get(0),
            this.smallWorld.board.get(2).get(0),
            this.smallWorld.board.get(1).get(1))));
    t.checkExpect(this.smallWorld.board.get(1).get(2).adjacentCells(this.smallWorld.board),
        new ArrayList<Cell>(Arrays.asList(this.smallWorld.board.get(0).get(2),
            this.smallWorld.board.get(2).get(2),
            this.smallWorld.board.get(1).get(1))));
    t.checkExpect(this.smallWorld.board.get(2).get(1).adjacentCells(this.smallWorld.board),
        new ArrayList<Cell>(Arrays.asList(this.smallWorld.board.get(2).get(0),
            this.smallWorld.board.get(2).get(2),
            this.smallWorld.board.get(1).get(1))));
    t.checkExpect(this.smallWorld.board.get(1).get(1).adjacentCells(this.smallWorld.board),
        new ArrayList<Cell>(Arrays.asList(this.smallWorld.board.get(0).get(1),
            this.smallWorld.board.get(2).get(1),
            this.smallWorld.board.get(1).get(0), this.smallWorld.board.get(1).get(2))));    
  }

  // to test the method makeScene() in the class FloodItWorld
  void testMakeScene(Tester t) {
    initData();
    WorldScene scene = new WorldScene(750, 750);
    scene.placeImageXY(new RectangleImage(500, 500, OutlineMode.SOLID, Color.gray), 250, 250);
    scene.placeImageXY(new RectangleImage(16, 16, OutlineMode.SOLID, Color.orange), 58, 58);
    scene.placeImageXY(new RectangleImage(16, 16, OutlineMode.SOLID, Color.magenta), 74, 58);
    scene.placeImageXY(new RectangleImage(16, 16, OutlineMode.SOLID, Color.orange), 90, 58);
    scene.placeImageXY(new RectangleImage(16, 16, OutlineMode.SOLID, Color.magenta), 58, 74);
    scene.placeImageXY(new RectangleImage(16, 16, OutlineMode.SOLID, Color.orange), 74, 74);
    scene.placeImageXY(new RectangleImage(16, 16, OutlineMode.SOLID, Color.magenta), 90, 74);
    scene.placeImageXY(new RectangleImage(16, 16, OutlineMode.SOLID, Color.orange), 58, 90);
    scene.placeImageXY(new RectangleImage(16, 16, OutlineMode.SOLID, Color.orange), 74, 90);
    scene.placeImageXY(new RectangleImage(16, 16, OutlineMode.SOLID, Color.magenta), 90, 90);

    scene.placeImageXY(new TextImage("Steps: 0 / 3", 14, FontStyle.BOLD, Color.black), 250, 400);
    scene.placeImageXY(new TextImage("Time elapsed: 0 seconds",
        14, FontStyle.BOLD, Color.black), 250, 375);
    scene.placeImageXY(new TextImage("Flood It!", 24, FontStyle.BOLD, Color.black), 250, 25);    
    scene.placeImageXY(new TextImage("Click cells.", 14, FontStyle.BOLD, Color.black), 340, 100);
    scene.placeImageXY(new TextImage("Fill the board with a single color.", 14, FontStyle.BOLD,
        Color.black), 340, 125);
    scene.placeImageXY(new TextImage("Board Size: 3 * 3", 14,FontStyle.BOLD, Color.black),
        340, 150);
    scene.placeImageXY(new TextImage("Number of Colors: 2", 14, FontStyle.BOLD,
        Color.black), 340, 175);
    scene.placeImageXY(new TextImage("Press 'r' to refresh the board.", 14, FontStyle.BOLD,
        Color.black), 340, 200);

    t.checkExpect(this.smallWorld.makeScene(), scene); 
  }

  // to test the method onMouseReleased(Posn) in the class FloodItWorld
  void testOnMouseReleased(Tester t) {
    initData();
    // to check that nothing changes on click when evaluationMode is true
    this.smallWorld.evaluationMode = true;
    int initialSteps = this.smallWorld.steps;
    this.smallWorld.onMouseReleased(new Posn(80, 80));
    t.checkExpect(this.smallWorld.steps, initialSteps);

    initData();
    // to check that nothing changes when evaluation mode is false and the clicked
    // cell is same as the flooded color
    initialSteps = this.smallWorld.steps;
    this.smallWorld.onMouseReleased(new Posn(80, 80));
    t.checkExpect(this.smallWorld.steps, initialSteps);

    initData();    
    // to check that we add a step when evaluation mode is false and the clicked
    // cell is not the same as the flooded color
    initialSteps = this.smallWorld.steps;
    this.smallWorld.onMouseReleased(new Posn(74, 58));
    t.checkExpect(this.smallWorld.steps, initialSteps + 1);
    t.checkExpect(this.smallWorld.evaluationMode, true);

    initData();    
    // to check for the case where we run out of steps
    this.smallWorld.steps = 6;
    initialSteps = this.smallWorld.steps;
    this.smallWorld.onMouseReleased(new Posn(74, 58));
    t.checkExpect(this.smallWorld.steps, initialSteps + 1);
    t.checkExpect(this.smallWorld.evaluationMode, false);
  }

  // to test the method onKeyReleased() in the class FloodItWorld
  void testOnKeyReleased(Tester t) {
    initData();
    // to check that releasing the wrong key does not change anything
    this.smallWorld.steps = 2;
    this.smallWorld.onKeyReleased("p");
    t.checkExpect(this.smallWorld.steps, 2);

    // to check that pressing the correct key refreshes the board 
    initData();
    this.smallWorld.steps = 2;
    this.smallWorld.onKeyReleased("r");
    t.checkExpect(this.smallWorld.steps, 0);
  }

  // to test the method onTick() in the class FloodItWorld
  void testOnTick(Tester t) {
    initData();
    this.smallWorld.onMouseReleased(new Posn(74, 58));
    this.smallWorld.onTick();
    t.checkExpect(this.smallWorld.board.get(0).get(1).flooded, true);
    t.checkExpect(this.smallWorld.board.get(1).get(0).flooded, true);
    t.checkExpect(this.smallWorld.board.get(0).get(0).flooded, true);
    t.checkExpect(this.smallWorld.board.get(1).get(1).flooded, false);
    t.checkExpect(this.smallWorld.board.get(2).get(0).flooded, false);
    t.checkExpect(this.smallWorld.board.get(2).get(1).flooded, false);
    t.checkExpect(this.smallWorld.board.get(2).get(2).flooded, false);
    t.checkExpect(this.smallWorld.board.get(1).get(2).flooded, false);
    t.checkExpect(this.smallWorld.board.get(0).get(2).flooded, false);
  }

  // to test the method findAdjacentCells() in the class FloodItWorld
  void testFindAdjacentCells(Tester t) {
    initData();
    ArrayList<Cell> array = new ArrayList<Cell>(Arrays.asList(
        this.smallWorld.board.get(0).get(0), this.smallWorld.board.get(0).get(1)));
    t.checkExpect(this.smallWorld.findAdjacentCells(array, array),      
        new ArrayList<Cell>(Arrays.asList(
            this.smallWorld.board.get(1).get(0), this.smallWorld.board.get(0).get(2),
            this.smallWorld.board.get(1).get(1))));
  }
  
  // to test the method lastScene() in the class FloodItWorld
  void testLastScene(Tester t) {
    initData();
    WorldScene scene = new WorldScene(500, 500); 
    scene.placeImageXY(new RectangleImage(500, 500, OutlineMode.SOLID, Color.gray), 250, 250);
    scene.placeImageXY(new TextImage("You win!", 14, FontStyle.BOLD, Color.black), 250, 250);
    t.checkExpect(this.smallWorld.lastScene("You win!"), scene);
  }
  
  // to test the method initialFloodGenerator() in the class FloodItWorld
  void testInitialFloodGenerator(Tester t) {
    initData();
    this.smallWorld = new FloodItWorld(3, 2, new Random(696));
    //this.smallWorld.bigBang(500, 500, 0.1);
    t.checkExpect(this.smallWorld.board.get(0).get(0).flooded, true);
    t.checkExpect(this.smallWorld.board.get(0).get(1).flooded, true);
    t.checkExpect(this.smallWorld.board.get(0).get(2).flooded, true);
    t.checkExpect(this.smallWorld.board.get(1).get(0).flooded, true);
    t.checkExpect(this.smallWorld.board.get(1).get(2).flooded, true);
    t.checkExpect(this.smallWorld.board.get(1).get(1).flooded, false);
    t.checkExpect(this.smallWorld.board.get(2).get(0).flooded, false);
    t.checkExpect(this.smallWorld.board.get(2).get(1).flooded, false);
    t.checkExpect(this.smallWorld.board.get(2).get(2).flooded, false);
  }
  
  // to test the method maxStepsAllowed() in the class FloodItWorld
  void testMaxStepsAllowed(Tester t) {
    initData();
    t.checkExpect(this.smallWorld.maxStepsAllowed(), 3);
    t.checkExpect(this.largeWorld.maxStepsAllowed(), 18);
  }
  
  // to test the method gameWon() in the class FloodItWorld
  void testGameWon(Tester t) {
    initData();
    t.checkExpect(this.smallWorld.gameWon(), false);
  }
  
  void testBigBang(Tester t) {
    initData();
    this.largeWorld.bigBang(500, 500, 0.1);
  }

}







