package dust;

import java.util.Random;
import javafx.scene.paint.Color;

/**
 *
 * @author Ondřej Machač
 */
public final class Particle {
    private final Random r = new Random();
    private final int MAX_TIME_TO_LIVE = 500;
    
    private double posX;
    private double posY;
    private int time;
    private int direction;

    public Particle() {
        posX = r.nextInt(Dust.WIDTH);
        posY = r.nextInt(Dust.HEIGHT);
        newParicle();
    }

    public void step() {
        double distance = Math.sqrt(Math.pow(Dust.mouseX - posX, 2) + Math.pow(Dust.mouseY - posY, 2));
        
        if (distance < Dust.radius && (Dust.mouseLeft || Dust.mouseRight)) {
            double direction = Math.atan2(Dust.mouseY - posY, Dust.mouseX - posX);
            double x = Math.cos(direction) * 1;
            double y = Math.sin(direction) * 1;
            
            if (Dust.mouseLeft) {
                Dust.GC.setFill(Color.LIMEGREEN);
            } else {
                Dust.GC.setFill(Color.RED);
                x *= -1;
                y *= -1;
            }
            if (posX >= 0 && posX <= Dust.WIDTH-1) posX += x; 
            if (posY >= 0 && posY <= Dust.HEIGHT-1) posY += y; 
        } else {
            Dust.GC.setFill(Color.rgb(100, 100, 100));

            if (posX <= 0 || posX >= Dust.WIDTH || posY <= 0 || posY >= Dust.HEIGHT) {
                newParicle();
            }

            posX += Math.sin(direction) / 10;
            posY += Math.cos(direction) / 10;
        }
        if (--time <= 0) newParicle();
        Dust.GC.fillRect(posX, posY, 1, 1);
    }

    public void newParicle() {
        time = r.nextInt(MAX_TIME_TO_LIVE);
        direction = r.nextInt(360);
    }
}
