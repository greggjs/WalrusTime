package theviolator;

import java.util.ArrayList;
import java.util.Collections;

import breakthrough.*;
import game.*;

public class TheViolatorMiniMaxPlayer extends GamePlayer {
        protected final int MAX_DEPTH = 50;
        protected final static int MAX_SCORE = 4 * (BreakthroughState.N + 1)
                        * (BreakthroughState.N + 1) * (BreakthroughState.N + 1);
        protected int depthLimit;

        // stack is where the search procedure places it's move recommendation.
        // If the search is at depth, d, the move is stored on mvStack[d].
        // This was done to help efficiency (i.e., reduce number constructor calls)
        protected ScoredBreakthroughMove[] stack;

        public TheViolatorMiniMaxPlayer(String nickname, int depthLimit) {
                super(nickname, new BreakthroughState(), false);
                this.depthLimit = depthLimit;
        }

        /**
         * 
         * @author Jake Gregg, Carly Schaeffer, Xi Chen
         * 
         */

        // A BreakthroughMove with a scored (how well it evaluates)
        protected class ScoredBreakthroughMove extends
                        breakthrough.BreakthroughMove implements
                        Comparable<ScoredBreakthroughMove> {
                public double score;

                protected ScoredBreakthroughMove() {
                        super(0, 0, 0, 0);
                        score = 0;
                }

                protected ScoredBreakthroughMove(int r1, int c1, int r2, int c2,
                                double s) {
                        super(r1, c1, r2, c2);
                        score = s;
                }

                protected void setScore(int r1, int c1, int r2, int c2, double s) {
                        startRow = r1;
                        startCol = c1;
                        endingRow = r2;
                        endingCol = c2;
                        score = s;
                }

                /**
                 * Compares this ScoredBreakthroughMove with another
                 * ScoredBreakthroughMove m Used in order to call Collections.sort() in
                 * getPossibleMoves method (note that it will sort
                 * ScoredBreakthroughMove objects in decreasing order according to their
                 * scores)
                 * 
                 * @param m
                 *            ScoredBreakthroughMove for comparison
                 * @return +1 if this move has a lower score than other move; -1 if this
                 *         move has a higher score than other move; 0 otherwise.
                 */
                @Override
                public int compareTo(ScoredBreakthroughMove m) {
                        if (this.score > m.score)
                                return -1;
                        else if (this.score < m.score)
                                return 1;
                        return 0;
                }

        }

        /**
         * Initializes the stack of Moves.
         */
        public void init() {
                stack = new ScoredBreakthroughMove[MAX_DEPTH];
                for (int i = 0; i < MAX_DEPTH; i++)
                        stack[i] = new ScoredBreakthroughMove(0, 0, 0, 0, 0);
        }

        /**
         * Determines if a board represents a completed game. If it is, the
         * evaluation values for these boards is recorded (i.e., 0 for a draw +X,
         * for a HOME win and -X for an AWAY win.
         * 
         * @param state
         *            breakthrough board to be examined
         * @param mv
         *            where to place the score information; column is irrelevant
         * @return true if the state is a terminal state
         */
        protected boolean terminalValue(GameState state, ScoredBreakthroughMove mv) {
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
         * For homeSym, each checker has the score with the value of its row+1
         * value; For awaySym, each checker has the score with the value of its N-r
         * value; player's piece.
         * 
         * @param brd
         *            board to be evaluated
         * @param who
         *            'B' or 'W'
         * @return number of adjacent pairs equal to who
         */
        private static int eval(BreakthroughState brd, char who) {
                int score = 0;
                for (int r = 0; r < BreakthroughState.N; r++) {
                        for (int c = 0; c < BreakthroughState.N; c++) {
                                // If the board piece is a home piece
                                if (brd.board[r][c] == BreakthroughState.homeSym) {
                                        // we are home player
                                        if (who == BreakthroughState.homeSym) {
                                                // we have won
                                                if (r == BreakthroughState.N - 1)
                                                        score += 100000;
                                                // we are about to win
                                                else if (r == BreakthroughState.N - 2)
                                                        score += 7;
                                                // reward for having multiple pieces grouped together
                                                score += (c + 1 < BreakthroughState.N
                                                                && brd.board[r][c] == brd.board[r][c + 1] ? (r + 1)*(r+1)
                                                                : 0);
                                                
                                                // reward for closer the piece is to a win
                                                score += ((BreakthroughState.N - r) * (BreakthroughState.N - r));
                                        }
                                        // if we are away
                                        else {
                                                // if they're gonna win
                                                if (r == BreakthroughState.N - 1)
                                                        score -= 1000;
                                                // if they're about to win
                                                else if (r == BreakthroughState.N - 2)
                                                        score -= 10;
                                                // penalty for them havin group clusters
                                                score -= (c + 1 < BreakthroughState.N
                                                                && brd.board[r][c] == brd.board[r][c + 1] ? (r + 1)*(r+1)
                                                                : 0);
                                                // penalty for them getting close to a win
                                                score -= ((BreakthroughState.N - r) * (BreakthroughState.N - r));
                                        }
                                // if the piece is an away symbol...
                                } else if (brd.board[r][c] == BreakthroughState.awaySym) {
                                        // if we are home
                                        if (who == BreakthroughState.homeSym) {
                                                // if they win
                                                if (r == 0)
                                                        score -= 1000;
                                                // if they're about to win
                                                else if (r == 1)
                                                        score -= 7;
                                                // penalty for group clusters
                                                score -= (c + 1 < BreakthroughState.N
                                                                && brd.board[r][c] == brd.board[r][c + 1] ? (BreakthroughState.N- r)* (BreakthroughState.N- r)
                                                                : 0);
                                                // penalty for them getting close to a win
                                                score -= ((r + 1) * (r + 1));
                                        // if we are away
                                        } else {
                                                // we win
                                                if (r == 0)
                                                        score += 100000;
                                                // we're about to win
                                                else if (r == 1)
                                                        score += 5;
                                                // reward for clusters
                                                
                                                score += (c + 1 < BreakthroughState.N
                                                                && brd.board[r][c] == brd.board[r][c + 1] ? (BreakthroughState.N- r)* (BreakthroughState.N- r)
                                                                : 0);
                                                
                                                // reward for moving closer to win
                                                score += ((r + 1) * (r + 1));
                                        }
                                }
                                // for empty symbols...
                                else {
                                        if (who == BreakthroughState.homeSym) {
                                                if (r == 0) {
                                                        score -= (c + 1 < BreakthroughState.N
                                                                        && brd.board[r][c] == brd.board[r][c + 1] ? .05
                                                                        : 0);
                                                }

                                        }
                                        else {
                                                if (r == BreakthroughState.N-1) {
                                                        score -= (c + 1 < BreakthroughState.N
                                                                        && brd.board[r][c] == brd.board[r][c + 1] ? .05
                                                                        : 0);
                                                }
                                        }
                                }

                        }

                }

                return score;
        }

        /**
         * The evaluation function
         * 
         * @param state
         *            board to be evaluated
         * @return Black evaluation - White evaluation
         */
        protected static int evalBoard(BreakthroughState state) {
                int score = eval(state, BreakthroughState.homeSym)
                                - eval(state, BreakthroughState.awaySym);
                if (Math.abs(score) > MAX_SCORE) {
                        System.err.println("Problem with eval");
                        System.exit(0);
                }
                return score;
        }

        /**
         * Get an ArrayList of all possible moves in a given state. Adapted from
         * getMove() in RandomBreakthroughPlayer.java
         * 
         * @param state
         * @return
         */
        protected ArrayList<ScoredBreakthroughMove> getPossibleMoves(GameState state) {
                BreakthroughState board = (BreakthroughState) state;
                ArrayList<ScoredBreakthroughMove> list = new ArrayList<ScoredBreakthroughMove>();
                ScoredBreakthroughMove mv = new ScoredBreakthroughMove(0, 0, 0, 0, 0);
                int dir = state.who == GameState.Who.HOME ? +1 : -1;
                char who = state.who == GameState.Who.HOME ? BreakthroughState.homeSym
                                : BreakthroughState.awaySym;
                int winRow = (who == BreakthroughState.homeSym ? BreakthroughState.N - 1
                                : 0);
                for (int r = 0; r < BreakthroughState.N; r++) {
                        for (int c = 0; c < BreakthroughState.N; c++) {
                                // Only perform ops if current board has this piece as start row
                                // and col
                                if (board.board[r][c] == who) {
                                        mv.startRow = r;
                                        mv.startCol = c;
                                        mv.endingRow = r + dir;
                                        mv.endingCol = c;
                                        // Give these moves scores so the children can all be
                                        // compared

                                        if (board.moveOK(mv)) {
                                                if (mv.endingRow == winRow) {
                                                        mv.score = 100000;
                                                } else {
                                                        mv.score = evalBoard(board);
                                                        mv.score += state.who == GameState.Who.HOME ? (mv.endingRow + 1)
                                                                        : (BreakthroughState.N - mv.endingRow);
                                                }
                                                list.add(new ScoredBreakthroughMove(mv.startRow,
                                                                mv.startCol, mv.endingRow, mv.endingCol,
                                                                mv.score));
                                        }
                                        mv.endingRow = r + dir;
                                        mv.endingCol = c + 1;
                                        if (board.moveOK(mv)) {
                                                if (mv.endingRow == winRow) {
                                                        mv.score = 100000;
                                                } else {
                                                        mv.score = evalBoard(board);
                                                        mv.score += state.who == GameState.Who.HOME ? (mv.endingRow + 1)
                                                                        : (BreakthroughState.N - mv.endingRow);
                                                }
                                                list.add(new ScoredBreakthroughMove(mv.startRow,
                                                                mv.startCol, mv.endingRow, mv.endingCol,
                                                                mv.score));
                                        }
                                        mv.endingRow = r + dir;
                                        mv.endingCol = c - 1;
                                        if (board.moveOK(mv)) {
                                                if (mv.endingRow == winRow) {
                                                        mv.score = 100000;
                                                } else {
                                                        // mv.score = evalBoard(board);
                                                        mv.score += state.who == GameState.Who.HOME ? (mv.endingRow + 1)
                                                                        : (BreakthroughState.N - mv.endingRow);
                                                }
                                                list.add(new ScoredBreakthroughMove(mv.startRow,
                                                                mv.startCol, mv.endingRow, mv.endingCol,
                                                                mv.score));
                                        }
                                }
                        }
                }
                // Sort the list according to ScoredBreakthroughMove method compareTo
                // Puts the best moves first. Used in alpha beta for optimal pruning
                Collections.sort(list);
                return list;
        }

        /**
         * Performs the a depth limited minimax algorithm. It leaves it's move
         * recommendation at stack[currDepth].
         * 
         * @param brd
         *            current board state
         * @param currDepth
         *            current depth in the search
         */
        private void minimax(BreakthroughState brd, int currDepth) {
                boolean toMaximize = (brd.getWho() == GameState.Who.HOME);
                boolean isTerminal = terminalValue(brd, stack[currDepth]);

                if (isTerminal) {
                        ;
                } else if (currDepth == depthLimit) {
                        stack[currDepth].setScore(0, 0, 0, 0, evalBoard(brd));
                } else {
                        ScoredBreakthroughMove curr = new ScoredBreakthroughMove(0, 0, 0,
                                        0, 0);
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
                                preState = (BreakthroughState) brd.clone();
                                // Make move on board
                                brd.makeMove(curr);
                                // Check out worth of this move
                                minimax(brd, currDepth + 1);
                                // Undo the move
                                brd = preState;

                                if (toMaximize && nextMove.score > bestMove.score) {
                                        bestMove.setScore(curr.startRow, curr.startCol,
                                                        curr.endingRow, curr.endingCol, nextMove.score);
                                } else if (!toMaximize && nextMove.score < bestMove.score) {
                                        bestMove.setScore(curr.startRow, curr.startCol,
                                                        curr.endingRow, curr.endingCol, nextMove.score);
                                }
                        }
                }
        }

        @Override
        public GameMove getMove(GameState state, String lastMv) {
                minimax((BreakthroughState) state, 0);
                return stack[0];
        }

        public static void main(String[] args) {
                int depth = 3;
                GamePlayer p = new TheViolatorMiniMaxPlayer("The Violator is " + depth
                                + " AUTO", depth);
                p.compete(args);
        }

}