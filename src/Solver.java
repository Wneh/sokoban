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
	private int[][] distanceMap;

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
		
		generateDistanceMap(initialBoard.nodes);
		initialBoard.printDeadlock();
		initialBoard.accumulatedCost = initialBoard.calculateHeuristic(distanceMap);
		PriorityQueue<Board> queue = new PriorityQueue<Board>();
		int examined = 0;
		visited = new HashSet<Integer>();
		queue.add(initialBoard);
		
		while(!queue.isEmpty()) {
			Board oldBoard = queue.poll();
			examined++;
			if(oldBoard.isWin()) {
				
				StringBuilder sb = new StringBuilder();
				Board nextBoard = oldBoard;
				while(nextBoard.prevBoard != null){
					sb.insert(0, nextBoard.getPlayerWalk2());
					nextBoard = nextBoard.prevBoard;
				}
				
				System.out.println(sb.toString());
				
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
				board.accumulatedCost = board.calculateHeuristic(distanceMap);
				queue.add(board);
			}
		}
	}
	
	private void generateDistanceMap(Node[][] map){
		
		distanceMap = new int[map.length][map[0].length];
		Queue<Node> q = new LinkedList<Node>();
		boolean[][] visited = new boolean[map.length][map[0].length];
		
		//Start by adding all the goals
		for(int i = 0; i < map.length; i++){
			for(int j = 0; j < map[0].length; j++){
				if(map[i][j].symbol == Symbol.BOXGOAL || map[i][j].symbol == Symbol.GOAL || map[i][j].symbol == Symbol.PLAYERGOAL){
					q.add(map[i][j]);
					visited[i][j] = true;
				}
			}
		}
		int x,y;
		Node currentNode;
		while(!q.isEmpty()){
			currentNode = q.poll();
			
			//Up
			x = currentNode.row-1;
			y = currentNode.col;
			if(!visited[x][y] && map[x][y].symbol != Symbol.WALL){
				distanceMap[x][y] = distanceMap[currentNode.row][currentNode.col] + 1;
				q.add(map[x][y]);
				visited[x][y] = true; 
			}
			
			//Down
			x = currentNode.row+1;
			y = currentNode.col;
			if(!visited[x][y] && map[x][y].symbol != Symbol.WALL){
				distanceMap[x][y] = distanceMap[currentNode.row][currentNode.col] +1;
				q.add(map[x][y]);
				visited[x][y] = true;
			}
			
			//Right
			x = currentNode.row;
			y = currentNode.col+1;
			if(!visited[x][y] && map[x][y].symbol != Symbol.WALL){
				distanceMap[x][y] = distanceMap[currentNode.row][currentNode.col] + 1;
				q.add(map[x][y]);
				visited[x][y] = true;
			}
			
			//Left
			x = currentNode.row;
			y = currentNode.col-1;
			if(!visited[x][y] && map[x][y].symbol != Symbol.WALL){
				distanceMap[x][y] = distanceMap[currentNode.row][currentNode.col] + 1;
				q.add(map[x][y]);
				visited[x][y] = true;
			}
		}
		
		for(int i = 0; i < map.length; i++){
			for(int j = 0; j < map[0].length; j++){
				System.out.print(distanceMap[i][j] + " ");
			}
			System.out.println();
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
