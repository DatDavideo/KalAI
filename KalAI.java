
package info.kwarc.teaching.AI.Kalah.Agents

import java.util.ArrayList;
import java.util.Iterator;
import info.kwarc.teaching.AI.Kalah.Board;
import info.kwarc.teaching.AI.Kalah.Agents.Agent;
import scala.*;

public class KalAI extends Agent{
	// parameters for the heuristic function: 
	// s - score multiplier
	// i - incline
	// e - empty multiplier
	// m - moving multiplier
	// h - hunger multiplier
	private double s, e, m, h, a, b;
	// the Board of the current match
	private Board board;
	// root of the game tree the agent uses for search
	private KalahNode root;
	// true if Agent is player one, false if Agent is player 2
	private boolean player;
	// size of the board
	private int size;
	// half the amount of seeds on the board
	private int win;
	
	private int searchTo;
	private int maxSearch;
	
	public void initParams() {
		this.s = 1000;
		this.e = 100 / size;
		this.m = 100 * size;
		this.h = 100 / (win * 2);
		double x2 = (double)size - 1;
		double x1 = x2 / 2;
		double p = x1 * (1 + (1000/100));
		b = (p - (x1*x1/x2)) / (x1-(x1*x1/x2));
		a = (1 - b)/x2;
	}
	
	// creates the first root, sets win, size, player and board
	@Override
	public void init(Board board, boolean playerOne) {
		this.board = board;
		player = playerOne;
		Tuple4<Iterable<Object>, Iterable<Object>, Object, Object> s = board.getState();
		size = 0;
		Iterator<Object> i1 = s._1().iterator();
		Iterator<Object> i2 = s._2().iterator();
		ArrayList<Integer> h1 = new ArrayList<Integer>();
		ArrayList<Integer> h2 = new ArrayList<Integer>();
		while(i1.hasNext()) {
			h1.add((Integer) i1.next());
			h2.add((Integer) i2.next());
			size++;
		}
		int[] h1arr = new int[size];
		int[] h2arr = new int[size];
		for(int i = 0; i < size; i++) {
			h1arr[i] = h1.get(i);
			h2arr[i] = h2.get(i);
		}
		win = h1arr[0] * size;
		
		this.root = new KalahNode(true, 0, 0, h1arr, h2arr);
		
		initParams();
	}

	
	// cuts root, then lets the best move be computed
	@Override
	public int move() {
		Tuple4<Iterable<Object>, Iterable<Object>, Object, Object> s = board.getState();
		int[] houses1 = new int[size];
		int[] houses2 = new int[size];
		Iterator<Object> i1 = s._1().iterator();
		Iterator<Object> i2 = s._2().iterator();
		for(int k = 0; k < size; k++) {
			houses1[k] = (int) i1.next();
			houses2[k] = (int) i2.next();
		}
		root = new KalahNode(player, (int) s._3(), (int) s._4(), houses1, houses2);
		
		
		searchTo = 0;
		do {
			searchTo += 2;
			maxSearch = 0;
			try {
				this.timeoutMove_$eq(goodMove());
			} catch(Exception e) {
				
			}
			//System.out.println("KalAI: " + searchTo);
		}while(maxSearch >= searchTo);
		return this.timeoutMove();
	}
	
	private int goodMove() {
		root.expand();
		int max = 0;
		int maxI = 0;
		for(int i = 0; i < root.children.length; i++) {
			int tmp = alphabeta(root.children[i], 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
			if(tmp > max) {
				max = tmp;
				maxI = i;
			}
			
		}
		return root.moves.get(maxI);
	}
	
	private int alphabeta(KalahNode n, int depth, int alpha, int beta) {
		if(depth == searchTo || n.isTerminal(win)) {
			if(depth > maxSearch) maxSearch = depth;
			return n.heuristic(player, win, s, e, m, h, a, b);
		}
		int value;
		int tmp;
		n.expand();
		if(n.moving == player) {
			// maximising
			value = Integer.MIN_VALUE;
			for(KalahNode n2 : n.children) {
				tmp = alphabeta(n2, depth + 1, alpha, beta);
				value = (value > tmp) ? value : tmp;
				alpha = (alpha > value) ? alpha : value;
				if(alpha >= beta)
					break;
			}
		} else {
			// minimising
			value = Integer.MAX_VALUE;
			for(KalahNode n2 : n.children) {
				tmp = alphabeta(n2, depth + 1, alpha, beta);
				value = (value < tmp) ? value : tmp;
				beta = (beta < value) ? beta : value;
				if(alpha >= beta)
					break;
			}
		}
		//System.out.println(value);
		return value;
	}

	@Override
	public String name() {
		return "KalAI";
	}

	@Override
	public Iterable<String> students() {
		ArrayList<String> students = new ArrayList<String>();
		students.add("David Kreisl");
		return students;
	}
	
	private class KalahNode {
		public boolean moving;
		private int score1;
		private int score2;
		private int[] houses1;
		private int[] houses2;
		public KalahNode[] children;
		public ArrayList<Integer> moves;
		
		public KalahNode(boolean moving, int score1, int score2, int[] houses1, int[] houses2) {
			this.moving = moving;
			this.score1 = score1;
			this.score2 = score2;
			this.houses1 = houses1;
			this.houses2 = houses2;
		}
		
		public boolean isTerminal(int win) {
			if(score1 > win || score2 > win)
				return true;
			else
				return false;
		}
		
		public int heuristic(boolean player, int win, double s, double e, double m, double h, double a, double b) {
			int score;
			int move;
			int starve = 0;
			int empty = 0;
			if(player) {
				if(score1 > win) return Integer.MAX_VALUE;
				else if(score2 > win) return Integer.MIN_VALUE;
				score = score1 - score2;
				move = (moving) ? 1 : -1;
				for(int p = 0; p < houses1.length; p++) {
					starve += houses1[p] * a * p * p + b;
					starve -= houses2[houses2.length - 1 - p] * a * p * p + b;
					empty += (houses1[p] == 0 && houses2[houses2.length - 1 - p] > 0) ? 1 : 0;
					empty -= (houses1[p] > 0 && houses2[houses2.length - 1 - p] == 0) ? 1 : 0;
				}
			} else {
				if(score2 > win) return Integer.MAX_VALUE;
				else if(score1 > win) return Integer.MIN_VALUE;
				score = score2 - score1;
				move = (moving) ? -1 : 1;
				for(int p = 0; p < houses2.length; p++) {
					starve += houses2[p] * a * p * p + b;
					starve -= houses1[houses1.length - 1 - p] * a * p * p + b;
					empty += (houses2[p] == 0 && houses1[houses1.length - 1 - p] > 0) ? 1 : 0;
					empty -= (houses2[p] > 0 && houses1[houses1.length - 1 - p] == 0) ? 1 : 0;
				}
			}
			return (int)(score * s + move * m + empty * e + starve * h);
		}
		
		public void expand() {
			moves = new ArrayList<Integer> ();
			ArrayList<KalahNode> list = new ArrayList<KalahNode>();
			if(moving) {
				for(int i = 0; i < houses1.length; i++) {
					if(houses1[i] > 0) {
						list.add(move(i));
						moves.add(i + 1);
					}
				}
			} else {
				for(int i = 0; i < houses2.length; i++) {
					if(houses2[i] > 0) {
						list.add(move(i));
						moves.add(i + 1);
					}
				}
			}
			children = new KalahNode[list.size()];
			for(int i = 0; i < children.length; i++)
				children[i] = list.get(i);
		}
		
		public KalahNode move(int m) {
			boolean nextMove = !moving;
			int score1New = score1;
			int score2New = score2;
			int[] houses1New = houses1.clone();
			int[] houses2New = houses2.clone();
			
			int value = (moving) ? houses1New[m] : houses2New[m];
			if(moving)
				houses1New[m] = 0;
			else
				houses2New[m] = 0;
			boolean row = moving;
			m++;
			
			for(int i = 0; i < value; i++) {
				if(m >= houses1New.length) {
					if(row && moving) {
						score1New++;
						i++;
					} else if(!row && !moving) {
						score2New++;
						i++;
					}
					m = 0;
					row = !row;
					i--;
				} else {
					if(row) {
						houses1New[m]++;
						m++;
					} else {
						houses2New[m]++;
						m++;
					}
				}
			}
			
			if(m == 0) {
				if(row)
					nextMove = false;
				else
					nextMove = true;
			} else if(moving && row && houses1New[m - 1] == 1 && houses2New[houses2New.length - m] > 0) {
				score1New += houses2New[houses2New.length - m] + 1;
				houses2New[houses2New.length - m] = 0;
				houses1New[m - 1] = 0;
			} else if(!moving && !row && houses2New[m - 1] == 1 && houses1New[houses2New.length - m] > 0) {
				score2New += houses1New[houses1New.length - m] + 1;
				houses1New[houses2New.length - m] = 0;
				houses2New[m - 1] = 0;
			}
			
			boolean empty1 = true;
			boolean empty2 = true;
			for(int i = 0; i < houses1New.length; i++) {
				if(houses1New[i] > 1) empty1 = false;
				if(houses2New[i] > 1) empty2 = false;
				if(!empty1 && ! empty2) break;
			}
			if(empty1) {
				for(int i = 0; i < houses2New.length; i++) {
					score2New += houses2New[i];
					houses2New[i] = 0;
				}
			} else if(empty2) {
				for(int i = 0; i < houses1New.length; i++) {
					score1New += houses1[i];
					houses1New[i] = 0;
				}
			}

			return new KalahNode(nextMove, score1New, score2New, houses1New, houses2New);
		}
		
		/*public boolean getMoving() {
			return moving;
		}*/
	}
}