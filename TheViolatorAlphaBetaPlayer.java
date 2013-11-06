package theviolator;
import java.util.ArrayList;
import java.util.Collections;

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
	public void alphaBeta(BreakthroughState brd, int currDepth,
										double alpha, double beta)
	{
		boolean toMaximize = (brd.getWho() == GameState.Who.HOME);
		boolean isTerminal = terminalValue(brd, stack[currDepth]);
		boolean toMinimize = !toMaximize;
		
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
			
			ArrayList<ScoredBreakthroughMove> allMv = getPossibleMoves(brd);
			Collections.shuffle(allMv);
			BreakthroughState preState;
			for (int i = 0; i < allMv.size(); i++) {
				curr = allMv.get(i); // moveOK(curr) is always true
				preState = (BreakthroughState)brd.clone();
				// Make move on board
				brd.makeMove(curr);
				// Check out worth of this move
				alphaBeta(brd, currDepth+1, alpha, beta);
				// Undo the move
				brd = preState;
		    	
		    	if (toMaximize && nextMove.score > bestMove.score) {
					bestMove.setScore(curr.startRow, curr.startCol, curr.endingRow, curr.endingCol, nextMove.score);
				} else if (!toMaximize && nextMove.score < bestMove.score) {
					bestMove.setScore(curr.startRow, curr.startCol, curr.endingRow, curr.endingCol, nextMove.score);
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
		int depth = 6;
		GamePlayer p = new TheViolatorAlphaBetaPlayer("The Violator is " + depth, depth);
		p.compete(args);
	}

}
