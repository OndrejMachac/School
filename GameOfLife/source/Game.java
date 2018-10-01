package gameoflife;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Random;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 *
 * @author Ondřej Machač
 */
public class Game {

    private final int[] patternX = {-1, 0, 1, -1, 1, -1, 0, 1};
    private final int[] patternY = {-1, -1, -1, 0, 0, 1, 1, 1};

    private Canvas canvas;
    private GraphicsContext gc;
    private int rows;
    private int columns;
    private Cell[][] cells;
    private float size;

    private Timeline timeline;

    public Game(Canvas canvas, int columns, int rows) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.columns = columns;
        this.rows = rows;
        this.cells = new Cell[columns][rows];
        this.size = (float) Math.min(canvas.getWidth(), canvas.getHeight()) / Math.min(rows, columns);

        for (int column = 0; column < columns; column++) {
            for (int row = 0; row < rows; row++) {
                this.cells[column][row] = new Cell();
            }
        }

        timeline = new Timeline(new KeyFrame(
                Duration.millis(1000),
                ae -> tick()
        ));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    void generate(int count) {
        Random rand = new Random();
        int i = 0;
        do {
            int column = rand.nextInt(this.columns - 1);
            int row = rand.nextInt(this.rows - 1);

            if (!cells[column][row].getState()) {
                cells[column][row].setState(true);
                i++;
            }
        } while (i < count);
        draw();
    }

    void draw() {
        gc.setFill(Color.BLACK);
        for (int column = 0; column < columns; column++) {
            for (int row = 0; row < rows; row++) {
                this.cells[column][row].updateState();
                if (this.cells[column][row].getState()) {
                    gc.fillRect(column * this.size, row * this.size, this.size, this.size);
                }
            }
        }
    }

    void clear() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, this.size * columns, this.size * rows);
    }

    void tick() {
        for (int column = 0; column < columns; column++) {
            for (int row = 0; row < rows; row++) {
                int neighbors = 0;

                for (int t = 0; t < 8; t++) {
                    if (row + patternX[t] >= 0 && row + patternX[t] < rows && column + patternY[t] >= 0 && column + patternY[t] < columns) {
                        if (this.cells[column + patternY[t]][row + patternX[t]].getState()) {
                            neighbors++;
                        }
                    }
                }
                if (this.cells[column][row].getState()) {
                    if (neighbors > 3 || neighbors < 2) {
                        this.cells[column][row].setState(false);
                    }
                } else {
                    if (neighbors == 3) {
                        this.cells[column][row].setState(true);
                    }
                }
            }
        }
        clear();
        draw();
    }

    void load(String file) {

        try {
            File field = new File(file);

            BufferedReader br = new BufferedReader(new FileReader(field));

            String line;
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                String[] text = line.split("");
                for (int x = 0; x < text.length; x++) {
                    if (text[x].equals("1")) {
                        cells[x][lineNumber].setState(true);

                    }
                    System.out.print(text[x]);
                }
                lineNumber++;
                System.out.println();
            }
            draw();
        } catch (Exception e) {

        }

    }
}
