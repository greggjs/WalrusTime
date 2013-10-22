/**
 * 
 */

package theviolator;

import breakthrough.*;
import game.*;

public class TheViolatorMiniMaxPlayer extends GamePlayer {
	protected final int MAX_DEPTH = 50;
	protected final static int MAX_SCORE = 12 * BreakthroughState.N
			* BreakthroughState.N + 1;
	protected int depthLimit;
	protected ScoredBreakthroughMove[] stack;

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
	}

	protected static void shuffle(int[] cols) {
		for (int i = 0; i < cols.length; i++) {
			int val1 = Util.randInt(0, cols.length);
			int swap = cols[i];
			cols[i] = cols[val1];
			cols[val1] = swap;
		}
	}

	public void init() {
		stack = new ScoredBreakthroughMove[MAX_DEPTH];
		for (ScoredBreakthroughMove m : stack)
			m = new ScoredBreakthroughMove(0, 0, 0, 0, 0);
	}

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

	protected static int evalBoard(BreakthroughState state) {
		int score = 0;
		for (int r = 0; r < BreakthroughState.N; r++) {
			for (int c = 0; c < BreakthroughState.N; c++) {
				if (state.board[r][c] == BreakthroughState.homeSym) {
					score += (r+1);
				} else if (state.board[r][c] == BreakthroughState.awaySym) {
					score -= (BreakthroughState.N-r);
				}
			}
		}
		
		if (Math.abs(score) > MAX_SCORE) {
			System.err.println("Problem with eval");
			System.exit(0);
		}
		return score;
	}

	private void minimax(BreakthroughState brd, int currDepth) {
		boolean toMaximize = (brd.getWho() == GameState.Who.HOME);
		boolean isTerminal = terminalValue(brd, stack[currDepth]);
		
		if (isTerminal) {
			;
		} else if (currDepth == depthLimit) {
			stack[currDepth].setScore(0, 0, 0, 0, evalBoard(brd));
		} else {
			ScoredBreakthroughMove curr = new ScoredBreakthroughMove(
					0, 0, 0, 0, 0);
			double bestScore = (toMaximize ? Double.NEGATIVE_INFINITY
					: Double.POSITIVE_INFINITY);
			ScoredBreakthroughMove bestMove = stack[currDepth];
			ScoredBreakthroughMove nextMove = stack[currDepth + 1];
			bestMove.setScore(0, 0, 0, 0, bestScore);
			GameState.Who currTurn = brd.getWho();
			
			int[] cols = new int[BreakthroughState.N];
			int[] rows = new int[BreakthroughState.N];
			for (int i = 0; i < cols.length; i++) {
				cols[i] = i;
				rows[i] = i;
			}
			shuffle(cols); shuffle(rows);
			for (int i = 0; i < cols.length; i++) {
				for (int j = 0; j < rows.length; j++) {
					int c = cols[i];
					int r = rows[j];
					curr.endingCol = c;
					curr.endingRow = r;
					char prevPiece = brd.board[r][c];
					if (brd.moveOK(curr)) {
					
						brd.makeMove(curr);
					
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

		}
	}

	@Override
	public GameMove getMove(GameState state, String lastMv) {
		// TODO Auto-generated method stub
		minimax((BreakthroughState)state, 0);
		return stack[0];
	}
	
	public static void main(String[] args) {
		int depth = 6;
		GamePlayer p = new TheViolatorMiniMaxPlayer("The Violator is " + depth + " deep in your mother", depth);
		p.compete(args);
	}

}
