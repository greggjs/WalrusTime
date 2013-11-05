package theviolator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import theviolator.TheViolatorMiniMaxPlayer.ScoredBreakthroughMove;

import breakthrough.*;
import game.*;
public class TheViolatorDominatrix extends TheViolatorMiniMaxPlayer{
	private static int NUM_THREADS = 4;
	private static String nickname = "The Violator Dominatrix";
	private static int depthLimit = 6;
	protected static ScoredBreakthroughMove[] sharedStack;
	public TheViolatorDominatrix (String nickname, int depthLimit) {
		super(nickname, depthLimit);
	}
	
	class Dominatrix extends TheViolatorAlphaBetaPlayer implements Runnable{
		GameState brd;
		String lastMove;
		int id = 0;
		public Dominatrix(String nickname, int depthLimit, GameState brd) {
			super(nickname, depthLimit);
			this.brd = brd;
			//this.lastMove = lastMove;
			this.id = id; 
			id++;
		}
		
		public void run() {
			alphaBeta((BreakthroughState)brd, 0, Double.NEGATIVE_INFINITY, 
					 Double.POSITIVE_INFINITY);
			sharedStack[id] = stack[0];
			System.out.println(stack[0].score);
		}
		
	}
	
	public GameMove getMove(GameState brd, String lastMove)
	{ 
		ArrayList<Thread> tList = new ArrayList<Thread>();
		for (int i = 0; i < NUM_THREADS; i++) {
			Dominatrix dom = new Dominatrix(nickname, depthLimit, (BreakthroughState)brd);
			Thread t = new Thread(dom);
			t.start();
			tList.add(t);
		}
		for (Thread t : tList) {
			try {
				t.join();
			} catch (InterruptedException err) {}
		}
		
		//System.out.println(stack[0].score);
		ArrayList<ScoredBreakthroughMove> list = new ArrayList<ScoredBreakthroughMove>(Arrays.asList(sharedStack));
		ScoredBreakthroughMove best = Collections.min(list);
		return best;
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
		GamePlayer p = new TheViolatorDominatrix(nickname, depthLimit);
		p.compete(args);
	}
}