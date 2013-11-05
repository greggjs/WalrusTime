package breakthrough.WalrusTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import breakthrough.*;
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
	private ScoredBreakthroughMove alphaBeta(BreakthroughState brd, int currDepth,
										double alpha, double beta){
		boolean isTerminal = terminalValue(brd, stack[currDepth]);
		
		ScoredBreakthroughMove noMove = generateStaticMove(brd);
		
		if (currDepth == depthLimit || isLeaf(brd) || isTerminal) {
			return noMove;
		} else {
			ArrayList<ScoredBreakthroughMove> moves = generateMoves(brd);
			//long seed = System.nanoTime();
			//Collections.shuffle(moves, new Random(seed));
			
				Collections.sort(moves, new maxMoveOrdering());
				ScoredBreakthroughMove bestMove = (ScoredBreakthroughMove)noMove.clone();
				bestMove.setScore(alpha);
				GameState.Who currTurn = brd.getWho();
				BreakthroughState tmpbrd = (BreakthroughState) brd.clone();
				for(ScoredBreakthroughMove m: moves){
					if(brd.moveOK(m)){
						int r = m.startRow; int c = m.startCol;
						char prevPiece = tmpbrd.board[r][c];
					
						tmpbrd.makeMove(m);
						
						alphaBeta(tmpbrd, currDepth+1, bestMove.score, beta);
						
						tmpbrd.board[r][c] = prevPiece;
						tmpbrd.numMoves--;
						tmpbrd.status = GameState.Status.GAME_ON;
						tmpbrd.who = currTurn;
					    	
						if(m.score > bestMove.score){
							bestMove.setScore(m);
							alpha = bestMove.score;
						}
						if(bestMove.score >= beta)
							return bestMove;
					}
				}
				return bestMove;
		}
	}
	
	
	public static class maxMoveOrdering implements Comparator<ScoredBreakthroughMove>{
		public int compare(ScoredBreakthroughMove m1, ScoredBreakthroughMove m2) {
			return (int)(m2.score - m1.score);
		}
		
	}
	
	public GameMove getMove(GameState brd, String lastMove)
	{ 
		return alphaBeta((BreakthroughState)brd, 0, Double.NEGATIVE_INFINITY, 
										 Double.POSITIVE_INFINITY);
	}
	
	public static void main(String [] args)
	{
		int depth = 5;
		GamePlayer p = new TheViolatorAlphaBetaPlayer("AlphaBeta " + depth, depth);
		p.compete(args);
	}
	
}
