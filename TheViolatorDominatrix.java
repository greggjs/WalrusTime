package theviolator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import theviolator.TheViolatorMiniMaxPlayer.ScoredBreakthroughMove;

//import theviolator.TheViolatorMiniMaxPlayer.ScoredBreakthroughMove;

import breakthrough.*;
import game.*;
public class TheViolatorDominatrix extends TheViolatorAlphaBetaPlayer{
	private static int NUM_THREADS = 4;
	private static int tId = 0;
	private static String nickname = "The Violator Dominatrix";
	private int numMoves;
	protected static ScoredBreakthroughMove[] sharedStack = new ScoredBreakthroughMove[30];
	public TheViolatorDominatrix (String nickname, int depthLimit) {
		super(nickname, depthLimit);
	}
	
	class Dominatrix extends TheViolatorAlphaBetaPlayer implements Runnable{
		GameState brd;
		String lastMove;
		int id;
		public Dominatrix(String nickname, int depthLimit, GameState brd) {
			super(nickname, depthLimit);
			this.brd = brd;
			//this.lastMove = lastMove;
			this.id = tId; 
			tId++;
			stack = new ScoredBreakthroughMove[MAX_DEPTH];
			for (int i = 0; i < MAX_DEPTH; i++)
				stack[i] = new ScoredBreakthroughMove(0, 0, 0, 0, 0);
		}
		
		public void run() {
			this.alphaBeta((BreakthroughState)brd, 0, Double.NEGATIVE_INFINITY, 
					 Double.POSITIVE_INFINITY);
			sharedStack[id] = stack[0];
			//System.out.println(id + ": " + stack[0]);
			//System.out.println(stack[1]);
			//System.out.println(stack[2]);
		}
		
	}
	
	public GameMove getMove(GameState brd, String lastMove)
	{ 
		BreakthroughState board = (BreakthroughState) brd;
        BreakthroughMove trial = this.hasWinningState(board);
		Opening open = new Opening((BreakthroughState)brd, numMoves);
		if (open.isBeginning()) {
			BreakthroughMove openMove = open.openingMove();
			numMoves++;
			return openMove;
		}
		this.alphaBeta((BreakthroughState)brd, 0, Double.NEGATIVE_INFINITY, 
				 Double.POSITIVE_INFINITY);
		System.out.println(stack[0].score);
		return stack[0];
		
	}
	
	private BreakthroughMove hasWinningState(BreakthroughState board) {
        char ours = board.who == GameState.Who.HOME ? BreakthroughState.homeSym : BreakthroughState.awaySym;
        int row = board.who == GameState.Who.HOME ? BreakthroughState.N - 2 : 1;
        int dir = board.who == GameState.Who.HOME ? 1 : -1;
        for (int col = 0; col < BreakthroughState.N; col++) {
                if (board.board[row][col] == ours) {
                        if (col > 0) {
                                return new BreakthroughMove(row, col, row + dir, col - 1);
                        }
                        else {
                                return new BreakthroughMove(row, col, row + dir, col + 1);
                        }
                }
        }
        
        return null;
}
	public void init() {
		numMoves = 0;
		depthLimit = 7;
		stack = new ScoredBreakthroughMove[MAX_DEPTH];
		for (int i = 0; i < MAX_DEPTH; i++)
			stack[i] = new ScoredBreakthroughMove(0, 0, 0, 0, 0);
		for (int i = 0; i < NUM_THREADS; i++)
			sharedStack[i] = new ScoredBreakthroughMove(0, 0, 0, 0, 0);
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
		//int depth = 6;
		GamePlayer p = new TheViolatorDominatrix(nickname, 6);
		p.compete(args);
	}
}
