package gameoflife;

/**
 *
 * @author Ondřej Machač
 */
public class Cell {
    private boolean state = false;
    private boolean newState;
    
    public void setState(boolean state){
        newState = state;
    }
    public boolean getState(){
        return state;
    }
    public void updateState(){
        state = newState;
    }
}
