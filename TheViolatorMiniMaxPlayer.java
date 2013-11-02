/**
 * 
 */

package breakthrough.WalrusTime;
import breakthrough.*;
import game.*;

import java.util.*;

import connect4.MiniMaxConnect4Player.ScoredConnect4Move;

public class TheViolatorMiniMaxPlayer extends game.GamePlayer {
	public static int N = BreakthroughState.N;
	protected static final int MAX_DEPTH = 50;
	protected static final int MAX_SCORE = 4 * (BreakthroughState.N + 1) * (BreakthroughState.N + 1) * (BreakthroughState.N + 1); // Why?
	protected int depthLimit;
	
	// stack is where the search procedure places it's move recommendation.
	// If the search is at depth, d, the move is stored on stack[d].
	// This was done to help efficiency (i.e., reduce number constructor calls)
	protected ScoredBreakthroughMove[] stack;
	
	/**
	 * Constructor. Calls parent GamePlayer constructor
	 */
	public TheViolatorMiniMaxPlayer(String nickname, int depthLimit) {
		super(nickname, new BreakthroughState(), false);
		this.depthLimit = depthLimit;
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 
	 * @author Jake Gregg, Carly Schaeffer, Xi Chen
	 * 
	 */
	protected class ScoredBreakthroughMove extends
			breakthrough.BreakthroughMove {
		protected double score;
		
		protected ScoredBreakthroughMove(int r1, int c1, int r2,
				int c2, double s) {
			super(r1, c1, r2, c2);
			score = s;
		}

		protected void setScore(int r1, int c1, int r2, int c2,
				double s) {
			startRow = r1;
			startCol = c1;
			endingRow = r2;
			endingCol = c2;
			score = s;
		}
		
		protected void setEndCol(int c2){
			endingCol = c2;
		}
		
		protected void setScore(int s){
			score = s;
		}
	}
	
	public void init() {
		stack = new ScoredBreakthroughMove[MAX_DEPTH];
		for (int i = 0; i < MAX_DEPTH; i++)
			stack[i] = new ScoredBreakthroughMove(0, 0, 0, 0, 0);
	}
	
	/**
	 * Determines if a board represents a completed game. If it is, the
	 * evaluation values for these boards is recorded (i.e., 0 for a draw
	 * +X, for a HOME win and -X for an AWAY win.
	 * @param brd Breakthrough board to be examined
	 * @param mv where to place the score information; column is irrelevant
	 * @return true if the brd is a terminal state
	 */
	protected boolean terminalValue(GameState state,
			ScoredBreakthroughMove mv) {
		GameState.Status status = state.getStatus();
		boolean isTerminal = true;

		if (status == GameState.Status.HOME_WIN) {
			mv.setScore(0, 0, 0, 0, MAX_SCORE);
		} else if (status == GameState.Status.AWAY_WIN) {
			mv.setScore(0, 0, 0, 0, -MAX_SCORE);
		} else if (status == GameState.Status.DRAW) {
			mv.setScore(0, 0, 0, 0, 0);
		} else {
			isTerminal = false;
		}
		return isTerminal;
	}	
	
	/**
	 * A function that essentially counts the number of pieces a particular
	 * player has on the board, giving more weight to pieces that are closer to
	 * the opposite side of the board
	 * @param brd Breakthrough board to be examined
	 * @param who 'W' or 'B'
	 * @return an integer representing current status of who
	 */
	private static int eval(BreakthroughState brd, char who){
		boolean home = (who == BreakthroughState.homeSym);
		int eval = 0;
		for(int r = 0; r < N; r++){
			for(int c = 0; c < N; c++){
				if((brd.board[r][c] == who)){
					if(home){
						eval += r*r;
					}
					else{
						eval += (N-r)*(N-r);
					}
				}
			}
		}
		return eval;
	}
	
	/**
	 * The evaluation function
	 * @param brd board to be evaluated
	 * @return White evaluation - Black evaluation
	 */
	public static int evalBoard(BreakthroughState brd)
	{ 
		int score = eval(brd, BreakthroughState.homeSym) - eval(brd, BreakthroughState.awaySym);
		if (Math.abs(score) > MAX_SCORE) {
			System.err.println("Problem with eval");
			System.exit(0);
		}
		return score;
	}
	


	protected ArrayList<ScoredBreakthroughMove> generateMoves(BreakthroughState brd){
		ArrayList<ScoredBreakthroughMove> moves = new ArrayList<ScoredBreakthroughMove>();
		for(int r = 0; r < N; r++){
			for(int c = 0; c < N; c++){
				int endRow = (brd.getWho() == GameState.Who.HOME? r+1 : r-1);
				ScoredBreakthroughMove tmp = new ScoredBreakthroughMove(r, c, endRow, c-1, 0);
				if(brd.moveOK(tmp)){
					moves.add(tmp);
				}
				tmp.setEndCol(c);
				if(brd.moveOK(tmp)){
					moves.add(tmp);
				}
				tmp.setEndCol(c+1);
				if(brd.moveOK(tmp)){
					moves.add(tmp);
				}
			}
		}
		return moves;
	}





	private void minimax(BreakthroughState brd, int currDepth) {
		boolean toMaximize = (brd.getWho() == GameState.Who.HOME);
		boolean isTerminal = terminalValue(brd, stack[currDepth]);
		
		if (isTerminal) {
			;
		} else if (currDepth == depthLimit) {
			stack[currDepth].setScore(0, 0, 0, 0, evalBoard(brd));
		} else {

			double bestScore = (brd.getWho() == GameState.Who.HOME ? 
												Double.NEGATIVE_INFINITY :
												Double.POSITIVE_INFINITY);
			ScoredBreakthroughMove bestMove = stack[currDepth];
			ScoredBreakthroughMove nextMove = stack[currDepth + 1];
			bestMove.setScore(0, 0, 0, 0, bestScore);			
			GameState.Who currTurn = brd.getWho();
			
			ArrayList<ScoredBreakthroughMove> moves = generateMoves(brd);
			long seed = System.nanoTime();
			Collections.shuffle(moves, new Random(seed));
			
			for(ScoredBreakthroughMove m: moves){
				int r = m.startRow; int c = m.startCol;
				char prevPiece = brd.board[r][c];
				
				brd.makeMove(m);
					
				minimax(brd, currDepth+1);
					
				brd.board[r][c] = prevPiece;
				brd.numMoves--;
				brd.status = GameState.Status.GAME_ON;
				brd.who = currTurn;
				    	
				if (toMaximize && nextMove.score > bestMove.score) {
					bestMove.setScore(nextMove.startRow, nextMove.startCol, r, c, nextMove.score);
				} else if (!toMaximize && nextMove.score < bestMove.score) {
					bestMove.setScore(nextMove.startRow, nextMove.startCol, r, c, nextMove.score);
				}
			}
		}
	}

	
	@Override
	public GameMove getMove(GameState state, String lastMv) {
		// TODO Auto-generated method stub
		minimax((BreakthroughState)state, 0);
		return stack[0];
	}
	
	public static void main(String[] args) {
		int depth = 5;
		GamePlayer p = new TheViolatorMiniMaxPlayer("The Violator is " + depth , depth);
		p.compete(args);
	}

}
