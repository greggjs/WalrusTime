package breakthrough.WalrusTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import breakthrough.*;
import breakthrough.WalrusTime.*;
import game.*;

public class TheViolatorScout extends TheViolatorMiniMaxPlayer{

	public TheViolatorScout(String nickname, int depthLimit) {
		super(nickname, depthLimit);
		// TODO Auto-generated constructor stub
	}


	private boolean testScout(BreakthroughState brd, int currDepth, double cutoff){
		boolean toMaximize = (brd.getWho() == GameState.Who.HOME);
		boolean isTerminal = terminalValue(brd, stack[currDepth]);
		
		if( isTerminal || currDepth == depthLimit || isLeaf(brd)){
			if(evalBoard(brd) > cutoff){
				return true;
			}
			else{
				return false;
			}
		}
		else{				
			ArrayList<ScoredBreakthroughMove> moves = generateMoves(brd);
			long seed = System.nanoTime();
			Collections.shuffle(moves, new Random(seed));
			GameState.Who currTurn = brd.getWho();
			BreakthroughState tmpbrd = (BreakthroughState) brd.clone();
			if(toMaximize){
				for(ScoredBreakthroughMove m: moves){
					if(brd.moveOK(m)){
						int r = m.startRow; int c = m.startCol;
						char prevPiece = tmpbrd.board[r][c];
					
						tmpbrd.makeMove(m);
						
						if(testScout(tmpbrd, currDepth+1, cutoff)){
							return true;
						}
						tmpbrd.board[r][c] = prevPiece;
						tmpbrd.numMoves--;
						tmpbrd.status = GameState.Status.GAME_ON;
						tmpbrd.who = currTurn;
					}	
				}
				return false;
				
			}
			else{
				for(ScoredBreakthroughMove m: moves){
					if(brd.moveOK(m)){
						int r = m.startRow; int c = m.startCol;
						char prevPiece = tmpbrd.board[r][c];
					
						tmpbrd.makeMove(m);
						
						if(!testScout(tmpbrd, currDepth+1, cutoff)){
							return false;
						}
						tmpbrd.board[r][c] = prevPiece;
						tmpbrd.numMoves--;
						tmpbrd.status = GameState.Status.GAME_ON;
						tmpbrd.who = currTurn;
					}
				}
				return true;
			}
		}
		
	}
	
	private ScoredBreakthroughMove evalScout(BreakthroughState brd, int currDepth){
		boolean toMaximize = (brd.getWho() == GameState.Who.HOME);
		boolean isTerminal = terminalValue(brd, stack[currDepth]);
		
		ScoredBreakthroughMove noMove = generateStaticMove(brd);
		
		if (isTerminal|| currDepth == depthLimit || isLeaf(brd)) {
			return noMove;
		} else {
			ArrayList<ScoredBreakthroughMove> moves = generateMoves(brd);
			long seed = System.nanoTime();
			Collections.shuffle(moves, new Random(seed));
			GameState.Who currTurn = brd.getWho();
			BreakthroughState tmpbrd = (BreakthroughState) brd.clone();
			if(toMaximize){
				ScoredBreakthroughMove leftChild = moves.get(0);
				for(int i = 1; i < moves.size(); i++){
					ScoredBreakthroughMove m = moves.get(i);
					if(brd.moveOK(m)){
						int r = m.startRow; int c = m.startCol;
						char prevPiece = tmpbrd.board[r][c];
					
						tmpbrd.makeMove(m);
						
						if(testScout(tmpbrd, currDepth+1, leftChild.score)){
							leftChild.setScore(m);
						}
						tmpbrd.board[r][c] = prevPiece;
						tmpbrd.numMoves--;
						tmpbrd.status = GameState.Status.GAME_ON;
						tmpbrd.who = currTurn;
					}
				}
				return leftChild;
			}
			
			else{
				ScoredBreakthroughMove leftChild = moves.get(0);
				for(int i = 1; i < moves.size(); i++){
					ScoredBreakthroughMove m = moves.get(i);
					if(brd.moveOK(m)){
						int r = m.startRow; int c = m.startCol;
						char prevPiece = tmpbrd.board[r][c];
					
						tmpbrd.makeMove(m);
						
						if(!testScout(tmpbrd, currDepth+1, leftChild.score)){
							leftChild.setScore(m);
						}
						tmpbrd.board[r][c] = prevPiece;
						tmpbrd.numMoves--;
						tmpbrd.status = GameState.Status.GAME_ON;
						tmpbrd.who = currTurn;
					}
				}
				return leftChild;
			}

		}
	}
	
	
	public GameMove getMove(GameState brd, String lastMove)
	{ 
		return evalScout((BreakthroughState)brd, 0);
	}
	
	public static void main(String [] args)
	{
		int depth = 5;
		GamePlayer p = new TheViolatorScout("Scout " + depth, depth);
		p.compete(args);
	}
	
	
}
