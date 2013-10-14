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

	int width = 0;
	int height = 0;
	private HashSet<Integer> visited;

	public Solver() throws IOException {

	}

	private String readBoard() throws IOException {

		BufferedReader br = new BufferedReader(new FileReader(new File("test000.in")));

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
//				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//				
//				String line;
//				StringBuilder board = new StringBuilder();
//				while(br.ready()) {
//					line = br.readLine();
//					height++;
//					int tempLength = line.length();
//					if(tempLength > width) {
//						width = tempLength;
//					}
//					board.append(line).append('\n');
//				}
//				return board.toString();
	}

	private void aStar() throws IOException {
		String initboard = readBoard();

		Board initialBoard = new Board(initboard, width, height);
		initialBoard.accumulatedCost = initialBoard.calculateHeuristic();
		PriorityQueue<Board> queue = new PriorityQueue<Board>();
		int examined = 0;
		visited = new HashSet<Integer>();
		queue.add(initialBoard);
		
//		for(int i = 0; i < initialBoard.boxes.size(); i++) {
//			Board newBoard = boardWithBfsPath(initialBoard.player, initialBoard.boxes.get(i), initialBoard);
//			newBoard.print();
//			queue.add(newBoard);
//		}
		
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
				newBoard.accumulatedCost -= 10;
				newBoard.accumulatedCost += newBoard.calculateHeuristic();
				queue.add(newBoard);
			}
		}
	}

	private Board boardWithBfsPath(Node start, Node box, Board initialBoard) {
		//Init stuff
		StringBuilder sb = new StringBuilder();
		Node[][] map = initialBoard.nodes;
		Queue<Node> q = new LinkedList<Node>();
		boolean[][] visited = new boolean[map.length][map[0].length];
		//Add the start
		q.add(start);

		Node currentNode;

		while(!q.isEmpty()) {
			//Take out one element
			currentNode = q.poll();
			//Check if it goal
			if(currentNode.equals(box)) {
				//We found the goal
				//Start backtracking

				Node tempNode = currentNode.getProvNode();
				Node newPlayer = tempNode;
				while(!tempNode.equals(start)){
					sb.append(tempNode.getDir());
					tempNode = tempNode.getProvNode();
				}
				return new Board(initialBoard, newPlayer, sb.reverse().toString());
			}

			//Take out each nieghbor from the currentNode and add to the queue
			int x,y;
			//Up

			x = currentNode.row-1;
			y = currentNode.col;

			if(!visited[x][y]) {
				Node up = map[x][y];
				if(up.symbol != Symbol.WALL && currentNode.symbol != Symbol.BOX && currentNode.symbol != Symbol.BOXGOAL) {
					up.setDir('U');
					up.setPrevNode(currentNode);
					q.add(up);
					visited[x][y] = true;
				}
			}
			//Down
			x = currentNode.row+1;
			y = currentNode.col;

			if(!visited[x][y]){
				Node down = map[x][y];
				if(down.symbol != Symbol.WALL && currentNode.symbol != Symbol.BOX && currentNode.symbol != Symbol.BOXGOAL){
					down.setDir('D');
					down.setPrevNode(currentNode);
					q.add(down);
					visited[x][y] = true;
				}
			}

			//Right
			x = currentNode.row;
			y = currentNode.col+1;

			if(!visited[x][y]){
				Node right = map[x][y];
				if(right.symbol != Symbol.WALL && currentNode.symbol != Symbol.BOX && currentNode.symbol != Symbol.BOXGOAL){
					right.setDir('R');
					right.setPrevNode(currentNode);
					q.add(right);
					visited[x][y] = true;
				}
			}

			//Left
			x = currentNode.row;
			y = currentNode.col-1;

			if(!visited[x][y]){
				Node left = map[x][y];
				if(left.symbol != Symbol.WALL && currentNode.symbol != Symbol.BOX && currentNode.symbol != Symbol.BOXGOAL){
					left.setDir('L');
					left.setPrevNode(currentNode);
					q.add(left);
					visited[x][y] = true;
				}
			}
		}
		return initialBoard; // we dont have to do a bfs since we already are at a goal.
	}

	private boolean visited(Board board) {
		return !visited.add(board.hashCode());
	}

	public static void main(String[] args) throws IOException {
		Solver solver = new Solver();
		solver.aStar();
	}
}
