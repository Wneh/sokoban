import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;


public class Solver {
	
	private final boolean DEBUGG = true;

	int width = 0;
	int height = 0;
	private HashSet<Integer> visited;

	public Solver() throws IOException {

	}

	private String readBoard() throws IOException {
		if(DEBUGG){
		BufferedReader br = new BufferedReader(new FileReader(new File("src/test000.in")));

		String line;
		StringBuilder board = new StringBuilder();
		while((line = br.readLine()) != null) {
			height++;
			int tempLength = line.length();
			if(tempLength > width) {
				width = tempLength;
			}
			board.append(line).append('\n');
		}
		return board.toString();
		} else {
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				
				String line;
				StringBuilder board = new StringBuilder();
				while(br.ready()) {
					line = br.readLine();
					height++;
					int tempLength = line.length();
					if(tempLength > width) {
						width = tempLength;
					}
					board.append(line).append('\n');
				}
				return board.toString();
		}
	}

	private void aStar() throws IOException {
		String initboard = readBoard();

		Board initialBoard = new Board(initboard, width, height);
		initialBoard.accumulatedCost = initialBoard.calculateHeuristic();
		PriorityQueue<Board> queue = new PriorityQueue<Board>();
		int examined = 0;
		visited = new HashSet<Integer>();
		queue.add(initialBoard);
		
		while(!queue.isEmpty()) {
			Board oldBoard = queue.poll();
			examined++;
			//System.err.println("Queue size: " + queue.size());
			//oldBoard.print();
			if(oldBoard.isWin()) {
				StringBuilder sb = new StringBuilder();
				sb.append(oldBoard.getPlayerWalk());
				Board nextOldBoard = oldBoard.prevBoard;
				while(nextOldBoard.prevBoard != null){
					sb.append(nextOldBoard.getPlayerWalk());
					nextOldBoard = nextOldBoard.prevBoard;
				}
				System.out.println(sb.reverse().toString());
				
				if(DEBUGG){
					System.out.println("Examined: " + examined);
					System.out.println("Queue size: " + queue.size());
				}
				break;
			}

			for(Board board : oldBoard.getPossibleStates()) {
				if(visited(board)){
					continue;
				}
			//	board.accumulatedCost = 00000000;
				board.accumulatedCost += board.calculateHeuristic();
				queue.add(board);
			}
		}
	}

	private boolean visited(Board board) {
		return !visited.add(board.hashCode());
	}

	public static void main(String[] args) throws IOException {
		Solver solver = new Solver();
		solver.aStar();
	}
}
