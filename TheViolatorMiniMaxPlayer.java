/**
 * 
 */

package breakthrough.WalrusTime;
import breakthrough.*;
import game.*;

import java.util.*;


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
	 * ScoredBreakthroughMove class to allow for Breakthrough moves with associated
	 * scores from evaluation function. Used in minimax to determine optimal move
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
		
		protected void setScore(ScoredBreakthroughMove m){
			startRow = m.startRow;
			startCol = m.startCol;
			endingRow = m.endingRow;
			endingCol = m.endingCol;
			score = m.score;
		}
		
		protected void setEndCol(int c2){
			endingCol = c2;
		}
		
		protected void setScore(double s){
			score = s;
		}
	    public Object clone()
	    { return new ScoredBreakthroughMove(startRow, startCol, endingRow, endingCol, score); }
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
		char away = (who == BreakthroughState.homeSym? BreakthroughState.awaySym : BreakthroughState.homeSym);
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
			/*	else if((brd.board[r][c] == away)){
					if(home){
						eval -= (N-r)*(N-r);
					}
					else{
						eval -= r*r;
					}
				}*/
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
		if (brd.getWho() != GameState.Who.HOME){
			score *= -1;
		}
		if (Math.abs(score) > MAX_SCORE) {
			System.err.println("Problem with eval");
			System.exit(0);
		}
		return score;
	}
	

	/**
	 * Determines a valid move in which the current player does not make a move, but keeps
	 * piece in same space. Adds an evaluation of the current board. Useful in minimax when reach
	 * depth limit, have no successor boards, or have reached a terminal state.
	 * @param brd the current board state
	 * @return ScoredBreakthroughMove that is a valid stayput move with the associated evaluation
	 */
	protected ScoredBreakthroughMove generateStaticMove(BreakthroughState brd){
		char who = (brd.getWho() == GameState.Who.HOME? BreakthroughState.homeSym : BreakthroughState.awaySym);
		for(int r = 0; r < N; r++){
			for(int c = 0; c < N; c++){
				if(brd.board[r][c] == who){
					return new ScoredBreakthroughMove(r,c,r,c,evalBoard(brd));
				}
			}
		}
		return null;
	}

	/**
	 * Determines all of the possible moves the player can make given the current board setup
	 * @param brd
	 * @return ArrayList of possible ScoredBreakthroughMove objects
	 */
	protected ArrayList<ScoredBreakthroughMove> generateMoves(BreakthroughState brd){
		ArrayList<ScoredBreakthroughMove> moves = new ArrayList<ScoredBreakthroughMove>();
		char who = (brd.getWho() == GameState.Who.HOME? BreakthroughState.homeSym : BreakthroughState.awaySym);
		char other = (brd.getWho() == GameState.Who.HOME? BreakthroughState.awaySym : BreakthroughState.homeSym);
		for(int r = 0; r < N; r++){
			for(int c = 0; c < N; c++){
				if(brd.board[r][c] == who){
					int eval = evalBoard(brd) + 1;
					int endRow = (brd.getWho() == GameState.Who.HOME? r+1 : r-1);
					ScoredBreakthroughMove tmp = new ScoredBreakthroughMove(r, c, endRow, c-1, eval);
					if(brd.moveOK(tmp)){
						
						moves.add((ScoredBreakthroughMove) tmp.clone());
					}
					tmp.setEndCol(c);
					if(brd.moveOK(tmp)){
						
						moves.add((ScoredBreakthroughMove) tmp.clone());
					}
					tmp.setEndCol(c+1);
					if(brd.moveOK(tmp)){
						
						moves.add((ScoredBreakthroughMove) tmp.clone());
					}
				}
			}
		}
		return moves;
	}


	/**
	 * Determines whether the current board state has no possible moves for this player
	 * @param brd the current board state
	 * @return true if there are no possible moves; false otherwise
	 */
	protected boolean isLeaf(BreakthroughState brd){
		char who = (brd.getWho() == GameState.Who.HOME? BreakthroughState.homeSym : BreakthroughState.awaySym);
		int count = 0;
		for(int r = 0; r < N; r++){
			for(int c = 0; c < N; c++){
				if(brd.board[r][c] == who){
					int endRow = (brd.getWho() == GameState.Who.HOME? r+1 : r-1);
					ScoredBreakthroughMove tmp = new ScoredBreakthroughMove(r, c, endRow, c-1, 0);
					if(brd.moveOK(tmp)){
						count++;
					}
					tmp.setEndCol(c);
					if(brd.moveOK(tmp)){
						count++;
					}
					tmp.setEndCol(c+1);
					if(brd.moveOK(tmp)){
						count++;
					}
				}
			}
		}
		return count == 0;
	}

	/**
	 * Performs minimax to determine the optimal move for current player
	 * @param brd the current board state
	 * @param currDepth how many times minimax has been called
	 * @return ScoredBreakthroughMove that is the optimal move for this player
	 */
	private ScoredBreakthroughMove minimax(BreakthroughState brd, int currDepth) {
		boolean toMaximize = (brd.getWho() == GameState.Who.HOME);
		
		
		ScoredBreakthroughMove noMove = generateStaticMove(brd);
		boolean isTerminal = terminalValue(brd, stack[currDepth]);
		if (currDepth == depthLimit || isLeaf(brd) || isTerminal) {
			return noMove;
		} else {
			ArrayList<ScoredBreakthroughMove> moves = generateMoves(brd);
			long seed = System.nanoTime();
			Collections.shuffle(moves, new Random(seed));
			
			/*if(moves.size() == 0){
				return noMove;
			}
			
			else*/ if(toMaximize){
				ScoredBreakthroughMove bestMove = noMove;
				bestMove.setScore(Double.NEGATIVE_INFINITY);
				GameState.Who currTurn = brd.getWho();
				BreakthroughState tmpbrd = (BreakthroughState) brd.clone();
				for(ScoredBreakthroughMove m: moves){
					if(brd.moveOK(m)){
						int r = m.startRow; int c = m.startCol;
						char prevPiece = tmpbrd.board[r][c];
					
						tmpbrd.makeMove(m);
						
						minimax(tmpbrd, currDepth+1);
						
						tmpbrd.board[r][c] = prevPiece;
						tmpbrd.numMoves--;
						tmpbrd.status = GameState.Status.GAME_ON;
						tmpbrd.who = currTurn;
					    	
						if(m.score > bestMove.score)
							bestMove.setScore(m);
					}
				}
				return bestMove;
			}
			
			else{
				ScoredBreakthroughMove bestMove = generateStaticMove(brd);
				bestMove.setScore(Double.POSITIVE_INFINITY);
				GameState.Who currTurn = brd.getWho();
				BreakthroughState tmpbrd = (BreakthroughState) brd.clone();
				for(ScoredBreakthroughMove m: moves){
					if(brd.moveOK(m)){
						int r = m.startRow; int c = m.startCol;
						char prevPiece = tmpbrd.board[r][c];
					
						tmpbrd.makeMove(m);
						
						minimax(tmpbrd, currDepth+1);
						
						tmpbrd.board[r][c] = prevPiece;
						tmpbrd.numMoves--;
						tmpbrd.status = GameState.Status.GAME_ON;
						tmpbrd.who = currTurn;
					    	
						if(m.score < bestMove.score)
							bestMove.setScore(m);
					}
				}
				return bestMove;
			}
			
		}
	}

	
	
	/**
	 * Determines what move should be made
	 */
	public GameMove getMove(GameState state, String lastMv) {
		// TODO Auto-generated method stub
		return minimax((BreakthroughState)state, 0);
		//return stack[0];
	}
	
	public static void main(String[] args) {
		int depth = 5;
		GamePlayer p = new TheViolatorMiniMaxPlayer("The Violator is " + depth , depth);
		p.compete(args);
	}

}
