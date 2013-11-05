package theviolator;

import breakthrough.BreakthroughMove;
import breakthrough.BreakthroughState;
import game.*;

public class Opening {
	private int numOurMoves;
    private BreakthroughState board;
    
    public Opening(BreakthroughState board, int numOurMoves){
            this.board = board;
            this.numOurMoves = numOurMoves;
    }
    
    protected boolean isBeginning(){
            return numOurMoves < 2;
    }
    
    public BreakthroughMove openingMove(){
            BreakthroughMove move = new BreakthroughMove();
            if (isBeginning()){
                    if (numOurMoves==0 && board.who == GameState.Who.HOME){
                            move.startRow = 1;
                            move.startCol = 0;
                            move.endingRow = 2;
                            move.endingCol = 1;
                    }
                    else if (numOurMoves==0 && board.who == GameState.Who.AWAY){
                            move.startRow = BreakthroughState.N-2;
                            move.startCol = 0;
                            move.endingRow = BreakthroughState.N-3;
                            move.endingCol = 1;
                    }
                    else if(numOurMoves==1 && board.who == GameState.Who.HOME){
                            move.startRow = 1;
                            move.startCol = BreakthroughState.N-1;
                            move.endingRow = 2;
                            move.endingCol = BreakthroughState.N-2;
                    }
                    else if (numOurMoves==1 && board.who == GameState.Who.AWAY){
                            move.startRow = BreakthroughState.N-2;
                            move.startCol = BreakthroughState.N-1;
                            move.endingRow = BreakthroughState.N-3;
                            move.endingCol = BreakthroughState.N-2;
                    }
            }
            return move;
    }
}
