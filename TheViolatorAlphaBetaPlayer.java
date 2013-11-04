package breakthrough.WalrusTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import breakthrough.*;
import breakthrough.WalrusTime.*;
import game.*;

public class TheViolatorAlphaBetaPlayer extends TheViolatorMiniMaxPlayer {

	public TheViolatorAlphaBetaPlayer(String nickname, int depthLimit) {
		super(nickname, depthLimit);
		// TODO Auto-generated constructor stub
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
					
				alphaBeta(brd, currDepth+1, alpha, beta);  // Check out move
					
				brd.board[r][c] = prevPiece;
				brd.numMoves--;
				brd.status = GameState.Status.GAME_ON;
				brd.who = currTurn;
				    	
				if (toMaximize && nextMove.score > bestMove.score) {
					bestMove.setScore(r,  c, nextMove.startRow, nextMove.startCol,nextMove.score);
				} else if (!toMaximize && nextMove.score < bestMove.score) {
					bestMove.setScore(r,  c, nextMove.startRow, nextMove.startCol,nextMove.score);
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
		int depth = 8;
		GamePlayer p = new TheViolatorAlphaBetaPlayer("C4 A-B F1 " + depth, depth);
		p.compete(args);
	}
	
}
