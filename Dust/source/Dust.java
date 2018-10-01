package dust;

import java.util.ArrayList;
import java.util.List;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author Ondřej Machač
 */
public class Dust extends Application {

    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    public static final Canvas CANVAS = new Canvas(WIDTH, HEIGHT);
    public static final GraphicsContext GC = CANVAS.getGraphicsContext2D();
    public static final PixelWriter PW = GC.getPixelWriter();
    
    private static List<Particle> particles = new ArrayList<>();
    
    public static final double radius = 100;
    public static double mouseX;
    public static double mouseY;
    public static boolean mouseLeft;
    public static boolean mouseRight;
    
    private Timeline timeline;

    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();
        root.getChildren().add(CANVAS);
        
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        primaryStage.setTitle("Dust");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, (MouseEvent event) -> {
            if(event.isPrimaryButtonDown()) {
                mouseRight = false;
                mouseLeft = true;
            } else if(event.isSecondaryButtonDown()){
                mouseLeft = false;
                mouseRight = true;
            }
            mouseX = event.getX();
            mouseY = event.getY();
        });
        scene.addEventFilter(MouseEvent.MOUSE_RELEASED, (MouseEvent event) -> {
            mouseLeft = false;
            mouseRight = false;
        });
        scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, (MouseEvent event) -> {
            mouseX = event.getX();
            mouseY = event.getY();
        });
        
        for (int i = 0; i < 10000; i++) {
            particles.add(new Particle());

        }

        timeline = new Timeline(new KeyFrame(
                Duration.millis(1000 / 60),
                ae -> cycle()
        ));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void cycle() {
        GC.setFill(Color.BLACK);
        GC.fillRect(0, 0, WIDTH, HEIGHT);
        particles.forEach(e -> e.step());
    }

    public static void main(String[] args) {
        launch(args);
    }

}