package breakthrough.WalrusTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import breakthrough.*;
import breakthrough.WalrusTime.*;
import breakthrough.WalrusTime.TheViolatorMiniMaxPlayer.ScoredBreakthroughMove;
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
		boolean toMaximize = (brd.getWho() == GameState.Who.HOME);
		boolean isTerminal = terminalValue(brd, stack[currDepth]);
		
		ScoredBreakthroughMove noMove = generateStaticMove(brd);
		
		if (isTerminal || currDepth == depthLimit ) {
			return noMove;
		} else {
			ArrayList<ScoredBreakthroughMove> moves = generateMoves(brd);
			long seed = System.nanoTime();
			Collections.shuffle(moves, new Random(seed));
			
			if(moves.size() == 0){
				return noMove;
			}
			
			else if(toMaximize){
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
					    	
						if(m.score > bestMove.score)
							bestMove.setScore(m);
						if(bestMove.score >= beta)
							return bestMove;
					}
				}
				return bestMove;
			}
			
			else{
				ScoredBreakthroughMove bestMove = generateStaticMove(brd);
				bestMove.setScore(beta);
				GameState.Who currTurn = brd.getWho();
				BreakthroughState tmpbrd = (BreakthroughState) brd.clone();
				for(ScoredBreakthroughMove m: moves){
					if(brd.moveOK(m)){
						int r = m.startRow; int c = m.startCol;
						char prevPiece = tmpbrd.board[r][c];
					
						tmpbrd.makeMove(m);
						
						alphaBeta(tmpbrd, currDepth+1, alpha, bestMove.score);
						
						tmpbrd.board[r][c] = prevPiece;
						tmpbrd.numMoves--;
						tmpbrd.status = GameState.Status.GAME_ON;
						tmpbrd.who = currTurn;
					    	
						if(m.score < bestMove.score)
							bestMove.setScore(m);
						if(bestMove.score <= alpha){
							return bestMove;
						}
					}
				}
				return bestMove;
			}
			
		}
	}
		
	public GameMove getMove(GameState brd, String lastMove)
	{ 
		return alphaBeta((BreakthroughState)brd, 0, Double.NEGATIVE_INFINITY, 
										 Double.POSITIVE_INFINITY);
	}
	
	public static void main(String [] args)
	{
		int depth = 8;
		GamePlayer p = new TheViolatorAlphaBetaPlayer("AlphaBeta " + depth, depth);
		p.compete(args);
	}
	
}
