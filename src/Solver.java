import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.PriorityQueue;


public class Solver {

	int width = 0;
	int height = 0;
	private HashSet<Integer> visited;
	
	public Solver() throws IOException {

	}
	
	private String readBoard() throws IOException {

		BufferedReader br = new BufferedReader(new FileReader(new File("//afs/nada.kth.se/home//y//u1sw5gwy//workspace//Sokoban//src//test000.in")));
		
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
	}
	
	private void aStar() throws IOException {
		String initboard = readBoard();
		Board initialBoard = new Board(initboard, width, height);
		PriorityQueue<Board> queue = new PriorityQueue<Board>();
		visited = new HashSet<Integer>();
		int examined = 0;
		queue.add(initialBoard);
		
		while(!queue.isEmpty()) {
			Board oldBoard = queue.poll();
			examined++;
			oldBoard.print();
			if(oldBoard.isWin()) {
				System.out.println(oldBoard.path);
				System.out.println("Examined: " + examined);
				break;
			}
			
			for(Direction dir : oldBoard.getPossibleMoves()) {
				Board newBoard = new Board(oldBoard, dir); // create a new board and move the player in a valid direction
				if(visited(newBoard))
                    continue;
				//newBoard.accumulatedCost += 1;
				queue.add(newBoard);
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
