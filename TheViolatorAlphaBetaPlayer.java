package theviolator;
import breakthrough.*;
import game.*;

//TheViolatorAlphaBetaPlayer is identical to TheViolatorMiniMaxPlayer
//except for the search process, which uses alpha beta pruning.

public class TheViolatorAlphaBetaPlayer extends TheViolatorMiniMaxPlayer {

	public TheViolatorAlphaBetaPlayer(String nickname, int depthLimit) {
		super(nickname, depthLimit);
	}
	
	/**
	 * Performs alpha beta pruning.
	 * @param brd
	 * @param currDepth
	 * @param alpha
	 * @param beta
	 */
	private void alphaBeta(BreakthroughState brd, int currDepth,
										double alpha, double beta)
	{
		boolean toMaximize = (brd.getWho() == GameState.Who.HOME);
		boolean toMinimize = !toMaximize;

		boolean isTerminal = terminalValue(brd, stack[currDepth]);
		
		if (isTerminal) {
			;
		} else if (currDepth == depthLimit) {
			stack[currDepth].setScore(0, 0, 0, 0, evalBoard(brd));
		} else {
			ScoredBreakthroughMove curr = new ScoredBreakthroughMove(0, 0, 0, 0, 0);

			double bestScore = (toMaximize ? 
					Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
			ScoredBreakthroughMove bestMove = stack[currDepth];
			ScoredBreakthroughMove nextMove = stack[currDepth+1];

			bestMove.setScore(0, 0, 0, 0, bestScore);
			GameState.Who currTurn = brd.getWho();

			int[] cols = new int[BreakthroughState.N];
			int[] rows = new int[BreakthroughState.N];
			int len = cols.length;
			for (int i = 0; i < len; i++) {
				cols[i] = i;
				rows[i] = i;
			}
			shuffle(cols); shuffle(rows);
			for (int i = 0; i < len; i++) {
				for (int j = 0; j < len; j++) {
					int c = cols[i];
					int r = rows[j];
					curr.endingCol = c;
					curr.endingRow = r;
					char prevPiece = brd.board[r][c];
					if (brd.moveOK(curr)) {
						// Make move on board
						brd.makeMove(curr);
						
					    // Check out worth of this move			
						alphaBeta(brd, currDepth+1, alpha, beta);	
						
						// Undo move
						brd.board[r][c] = prevPiece;
						brd.numMoves--;
						brd.status = GameState.Status.GAME_ON;
						brd.who = currTurn;
						
						// Check out the results, relative to what we've seen before
						if (toMaximize && nextMove.score > bestMove.score) {
							bestMove.setScore(nextMove.startRow, nextMove.startCol, r, c, nextMove.score);
						} else if (!toMaximize && nextMove.score < bestMove.score) {
							bestMove.setScore(nextMove.startRow, nextMove.startCol, r, c, nextMove.score);
						}
	
						// Update alpha and beta. Perform pruning, if possible.
						if (toMinimize) {
							beta = Math.min(bestMove.score, beta);
							if (bestMove.score <= alpha || bestMove.score == -MAX_SCORE) {
								return;
							}
						} else {
							alpha = Math.max(bestMove.score, alpha);
							if (bestMove.score >= beta || bestMove.score == MAX_SCORE) {
								return;
							}
						}
					}
				}
			}
		}
	}
		
	public GameMove getMove(GameState brd, String lastMove)
	{ 
		alphaBeta((BreakthroughState)brd, 0, Double.NEGATIVE_INFINITY, 
										 Double.POSITIVE_INFINITY);
		System.out.println(stack[0].score);
		return stack[0];
	}
	
	public static char [] toChars(String x)
	{
		char [] res = new char [x.length()];
		for (int i=0; i<x.length(); i++)
			res[i] = x.charAt(i);
		return res;
	}
	
	public static void main(String [] args)
	{
		int depth = 8;
		GamePlayer p = new TheViolatorAlphaBetaPlayer("The Violator is " + depth, depth);
		p.compete(args);
	}

}
