import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class Board implements Comparable<Board> {
	static Direction UP = Direction.UP;
	static Direction DOWN = Direction.DOWN;
	static Direction LEFT = Direction.LEFT;
	static Direction RIGHT = Direction.RIGHT;
	static boolean[][] deadlocks;
	Node[][] nodes;
	Node player;
	int width;
	int height;
	int numberOfFreeBoxes;
	int boxesOnGoal;
	int accumulatedCost;
//	StringBuilder path;
	ArrayList<Node> boxes;
	ArrayList<Node> goals;
	public int f, g; // Total and accumulated cost
	Board prevBoard;
//	String walkPath;
	Node oldPlayerPos;
	Node pushLocation;
	Direction pushDir;

	public Board(String board, int width, int height) {

		nodes = new Node[height][width];
//		path = new StringBuilder();
		boxes = new ArrayList<Node>();

		goals = new ArrayList<Node>();
		deadlocks = new boolean[height][width];
		this.width = width;
		this.height = height;
		char type = ' ';

		String[] rows = board.split("\n");

		for(int i = 0; i < height; i++) {
			for(int j = 0; j < rows[i].length(); j++) { // assume that there are trailing white spaces
				type = rows[i].charAt(j);
				nodes[i][j] = new Node(type, i, j);

				if(type == '$' || type == '*') {
					boxes.add(nodes[i][j]);
					if(type == '$')
						numberOfFreeBoxes++;
				} else if(type == '@' || type == '+') {
					player = nodes[i][j];
				} else if(type == '.') {
					goals.add(nodes[i][j]);
				}
			}
			for(int j = rows[i].length(); j < width; j++){
				nodes[i][j] = new Node(' ', i, j);
			}
		}

		/**
		 * Check for deadlocks
		 */
		//Corners
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				if(i == 0 || i == height-1 || j == 0 || j == width-1 ) // need to check this to not look for deadlocks outside the map
					continue;
				Node node = nodes[i][j];
				if(at(node) == Symbol.FREE || at(node) == Symbol.PLAYER) {
					if(at(node,DOWN) == Symbol.WALL && at(node,LEFT) == Symbol.WALL || at(node,DOWN) == Symbol.WALL && at(node,RIGHT) == Symbol.WALL
							|| at(node,UP) == Symbol.WALL && at(node,LEFT) == Symbol.WALL || at(node,UP) == Symbol.WALL && at(node,RIGHT) == Symbol.WALL) {
						deadlocks[i][j] = true; // i*width+j will translate an (i,j) coordinate to an array index.
					}
				}
			}
		}
		//Walls
		for(int i = 0 ; i < height ; i++){
			for(int j = 0 ; j < width ; j++){
				Node node = nodes[i][j];
				if(deadlocks[i][j]){
					if(at(node,LEFT) == Symbol.WALL){
						if(at(node,UP) ==  Symbol.WALL){
							boolean dead = false;
							while(at(node,UP) == Symbol.WALL && node.col != width-1 && node.symbol == Symbol.FREE){
								node = to(node,RIGHT);
								if(node.symbol == Symbol.WALL){
									dead = true;
									break;
								}
							}
							if(dead){
								while((node = to(node,LEFT)).symbol != Symbol.WALL){
									deadlocks[node.row][node.col] = true;
								}
							}
						} else if(at(node,DOWN) == Symbol.WALL){
							boolean dead = false;
							while(at(node,LEFT) == Symbol.WALL && node.row != 0 && node.symbol == Symbol.FREE){
								node = to(node,UP);
								if(node.symbol == Symbol.WALL){
									dead = true;
									break;
								}
							}
							if(dead){
								while((node = to(node,DOWN)).symbol != Symbol.WALL){
									deadlocks[node.row][node.col] = true;
								}
							}
						}
					} else if(at(node,RIGHT) == Symbol.WALL){
						if(at(node,UP) ==  Symbol.WALL){
							boolean dead = false;
							while(at(node,RIGHT) == Symbol.WALL && node.row != height-1 && node.symbol == Symbol.FREE){
								node = to(node,DOWN);
								if(node.symbol == Symbol.WALL){
									dead = true;
									break;
								}
							}
							if(dead){
								while((node = to(node,UP)).symbol != Symbol.WALL){
									deadlocks[node.row][node.col] = true;
								}
							}
						} else if(at(node,DOWN) == Symbol.WALL){
							boolean dead = false;
							while(at(node,DOWN) == Symbol.WALL && node.col != 0 && node.symbol == Symbol.FREE){
								node = to(node,LEFT);
								if(node.symbol == Symbol.WALL){
									dead = true;
									break;
								}
							}
							if(dead){
								while((node = to(node,RIGHT)).symbol != Symbol.WALL){
									deadlocks[node.row][node.col] = true;
								}
							}
						}
					}
				}
			}

		}
		
		for(int i = 1 ; i < height-1 ; i++){
			for(int j = 1 ; j < width-1 ; j++){
				if(nodes[i][j-1].symbol == Symbol.WALL && nodes[i][j+1].symbol == Symbol.WALL && (nodes[i][j].symbol == Symbol.FREE || nodes[i][j].symbol == Symbol.PLAYER)){ //Deadlocked tunnel downwards
					int k = i;
					boolean isDeadlock = false;
					while(nodes[k+1][j-1].symbol == Symbol.WALL && nodes[k+1][j+1].symbol == Symbol.WALL && k != height-2){
						if(nodes[k+1][j].symbol == Symbol.WALL){
							isDeadlock = true;
							break;
						} else if(nodes[k+1][j].symbol != Symbol.FREE && nodes[k+1][j].symbol != Symbol.PLAYER){
							break;
						}
						k++;
					}
					if(isDeadlock){
						deadlocks[i][j] = true;
					} else {
						k = i;
						while(nodes[k-1][j-1].symbol == Symbol.WALL && nodes[k-1][j+1].symbol == Symbol.WALL){
							if(nodes[k-1][j].symbol == Symbol.WALL){
								isDeadlock = true;
								break;
							} else if(nodes[k-1][j].symbol != Symbol.FREE && nodes[k-1][j].symbol != Symbol.PLAYER){
								break;
							}
							k--;
						}
						if(isDeadlock){
							deadlocks[i][j] = true;
						}
					}
					
				} else if(nodes[i-1][j].symbol == Symbol.WALL && nodes[i+1][j].symbol == Symbol.WALL && (nodes[i][j].symbol == Symbol.FREE || nodes[i][j].symbol == Symbol.PLAYER)){ //Deadlocked tunnel leftwards
					int k = j;
					boolean isDeadlock = false;
					while(nodes[i-1][k-1].symbol == Symbol.WALL && nodes[i+1][k-1].symbol == Symbol.WALL && k != 1){
						if(nodes[i][k-1].symbol == Symbol.WALL){
							isDeadlock = true;
							break;
						} else if(nodes[i][k-1].symbol != Symbol.FREE && nodes[i][k-1].symbol != Symbol.PLAYER){
							break;
						}
						k--;
					}
					if(isDeadlock){
						deadlocks[i][j] = isDeadlock;
					} else {
						k = j;
						while(nodes[i-1][k+1].symbol == Symbol.WALL && nodes[i+1][k+1].symbol == Symbol.WALL){
							if(nodes[i][k+1].symbol == Symbol.WALL){
								isDeadlock = true;
								break;
							} else if(nodes[i][k+1].symbol != Symbol.FREE && nodes[i][k+1].symbol != Symbol.PLAYER){
								break;
							}
							k++;
						}
						if(isDeadlock){
							deadlocks[i][j] = isDeadlock;
						}
					}
				} 
			}
		}

	}

	public Board(Board oldBoard, Node newPlayerPos, Direction dir) { // create a new board and move the player in a valid direction
		this.nodes = new Node[oldBoard.getHeight()][oldBoard.getWidth()];
		this.player = new Node('@', oldBoard.player.row, oldBoard.player.col);
		//this.player = new Node('@',newPlayerPos.row,newPlayerPos.col);
		this.numberOfFreeBoxes = oldBoard.numberOfFreeBoxes;
		this.width = oldBoard.getWidth();
		this.height = oldBoard.getHeight();
		this.boxes = new ArrayList<Node>(oldBoard.boxes);
		this.goals = new ArrayList<Node>(oldBoard.goals);
		//this.path = new StringBuilder(walk);
//		this.walkPath = walk;
		this.accumulatedCost = oldBoard.accumulatedCost;
		copyOldBoard(oldBoard);
		
		this.oldPlayerPos = new Node('@',player.row,player.col);
		this.pushLocation = to(newPlayerPos,Direction.negDir(dir));
		this.pushDir = dir;
		
		
		moveBox(newPlayerPos,dir); // make one of the valid moves;
		//doStuff(newPlayerPos,player,to(newPlayerPos,dir));
		this.prevBoard = oldBoard;
		
	}

	private void copyOldBoard(Board oldBoard) {
		for(int i = 0; i < oldBoard.height; i++) {
			for(int j = 0; j < oldBoard.width; j++) {
				this.nodes[i][j] = newNode(oldBoard, i, j); // prevent shallow copy
				this.nodes[i][j].row = oldBoard.getnodes()[i][j].row;
				this.nodes[i][j].col = oldBoard.getnodes()[i][j].col;
			}
		}
	}

	public Node newNode(Board oldBoard, int i, int j) {
		return new Node(oldBoard.getnodes()[i][j].toChar(), oldBoard.getnodes()[i][j].row, oldBoard.getnodes()[i][j].col);
	}

	public void moveBox(Node current, Direction dir){
		Node currentBox = nodeAt(current);
		Node nextBox = to(currentBox,dir);

		Node currentPlayer = nodeAt(this.player);

		//Start by removing the old player
		if(currentPlayer.symbol == Symbol.PLAYER){
			currentPlayer.symbol = Symbol.FREE;
		}
		else if(currentPlayer.symbol == Symbol.PLAYERGOAL){
			currentPlayer.symbol = Symbol.GOAL;
		}
		else{
			System.out.println("-----------FUCK UP FROM REMOVING OLD PLAYER");
		}

		//Move the box the new location
		if(nextBox.symbol == Symbol.FREE){
			nextBox.symbol = Symbol.BOX;
		}
		else if(nextBox.symbol == Symbol.GOAL){
			nextBox.symbol = Symbol.BOXGOAL;
			numberOfFreeBoxes--;
		}
		else{
			System.out.println("-----------FUCKED UP FROM MOVING THE BOX");
		}

		//Remove the old box from the box list
		for(Node box : boxes){
			if(box.equals(currentBox)){
				boxes.remove(box);
				break;
			}
		}

		//Set the new player position to the old box position
		if(currentBox.symbol == Symbol.BOX){
			currentBox.symbol = Symbol.PLAYER;
		}
		else if(currentBox.symbol == Symbol.BOXGOAL){
			currentBox.symbol = Symbol.PLAYERGOAL;
			numberOfFreeBoxes++;
		}
		else{
			System.out.println("-----------FUCKED FROM SETTING THE NEW PLAYER POSITION");
		}
		setPlayer(currentBox);
		boxes.add(nextBox);
	}

	private void setPlayer(Node newPos) {
		player.row = newPos.row;
		player.col = newPos.col;
	}

	public ArrayList<Board> getPossibleStates(){
		ArrayList<Board> moves = new ArrayList<Board>();	
		boolean[][] reachables = getReachables(this.player, nodes);
		//Grab the first box in the list that is not on a goal
		for(Node box : boxes){
			//Now move this box to each direction
			for(Direction dir : Direction.values()) {
				//Check if direction we want to push it ok
				Node nextBoxPosition  = to(box, dir);
				if(!deadlocks[nextBoxPosition.row][nextBoxPosition.col]){
					if(nextBoxPosition.symbol == Symbol.FREE || nextBoxPosition.symbol == Symbol.GOAL || nextBoxPosition.symbol == Symbol.PLAYER || nextBoxPosition.symbol == Symbol.PLAYERGOAL){
						//						It was not a deadlock for this push
						//						and the location was okey to push
//						makeNextMove(moves, box, dir,reachables);
						if(dir == RIGHT){
							boolean downRight = !(at(nextBoxPosition,DOWN) == Symbol.BOX && (at(nextBoxPosition,RIGHT) == Symbol.WALL) && (at(to(nextBoxPosition,DOWN),RIGHT) == Symbol.WALL));
							boolean upRight = !(at(nextBoxPosition,UP) == Symbol.BOX && (at(nextBoxPosition,RIGHT) == Symbol.WALL) && (at(to(nextBoxPosition,UP),RIGHT) == Symbol.WALL ));
							boolean rightRightUp = !(at(nextBoxPosition,RIGHT) == Symbol.BOX && at(nextBoxPosition,UP) == Symbol.WALL && at(to(nextBoxPosition, RIGHT), UP) == Symbol.WALL);
							boolean rightRightDown = !(at(nextBoxPosition,RIGHT) == Symbol.BOX && at(nextBoxPosition,DOWN) == Symbol.WALL && at(to(nextBoxPosition, RIGHT), DOWN) == Symbol.WALL);
							if((downRight && upRight && rightRightUp && rightRightDown)){
//								boolean cornerRightUP = !(at(nextBoxPosition,RIGHT) == Symbol.WALL && deadlocks[nextBoxPosition.row-1][nextBoxPosition.col] && at(to(nextBoxPosition,LEFT),UP) == Symbol.BOX && at(to(to(nextBoxPosition,LEFT),UP),UP) == Symbol.WALL);
//								boolean cornerRightDown = !(at(nextBoxPosition,RIGHT) == Symbol.WALL && deadlocks[nextBoxPosition.row+1][nextBoxPosition.col] && at(to(nextBoxPosition,LEFT),DOWN) == Symbol.BOX && at(to(to(nextBoxPosition,LEFT),DOWN),DOWN) == Symbol.WALL);
//								if((cornerRightDown && cornerRightUP)){
									makeNextMove(moves, box, dir,reachables);
//								}
							}
						} else if (dir == LEFT){
							boolean downLeft =  !(at(nextBoxPosition,DOWN) == Symbol.BOX && (at(nextBoxPosition,LEFT) == Symbol.WALL) && (at(to(nextBoxPosition,DOWN),LEFT) == Symbol.WALL ));
							boolean upLeft = !(at(nextBoxPosition,UP) == Symbol.BOX && (at(nextBoxPosition,LEFT) == Symbol.WALL) && (at(to(nextBoxPosition,UP),LEFT) == Symbol.WALL ));
							boolean leftLeftUp = !(at(nextBoxPosition,LEFT) == Symbol.BOX && at(nextBoxPosition,UP) == Symbol.WALL && at(to(nextBoxPosition, LEFT), UP) == Symbol.WALL);
							boolean leftLeftDown = !(at(nextBoxPosition,LEFT) == Symbol.BOX && at(nextBoxPosition,DOWN) == Symbol.WALL && at(to(nextBoxPosition, LEFT), DOWN) == Symbol.WALL);
							if((downLeft && upLeft && leftLeftUp && leftLeftDown)){
//								boolean cornerLeftUP = !(at(nextBoxPosition,LEFT) == Symbol.WALL && deadlocks[nextBoxPosition.row-1][nextBoxPosition.col] && at(to(nextBoxPosition,RIGHT),UP) == Symbol.BOX && at(to(to(nextBoxPosition,RIGHT),UP),UP) == Symbol.WALL);
//								boolean cornerLeftDown = !(at(nextBoxPosition,LEFT) == Symbol.WALL && deadlocks[nextBoxPosition.row+1][nextBoxPosition.col] && at(to(nextBoxPosition,RIGHT),DOWN) == Symbol.BOX && at(to(to(nextBoxPosition,RIGHT),DOWN),DOWN) == Symbol.WALL);
//								if( (cornerLeftDown && cornerLeftUP)){
									makeNextMove(moves, box, dir,reachables);
//								}
							}
						} else if(dir == UP){
							boolean rightUp = !(at(nextBoxPosition,RIGHT) == Symbol.BOX && (at(nextBoxPosition,UP) == Symbol.WALL) && (at(to(nextBoxPosition,RIGHT),UP) == Symbol.WALL ));
							boolean leftUp = !(at(nextBoxPosition,LEFT) == Symbol.BOX && (at(nextBoxPosition,UP) == Symbol.WALL) && (at(to(nextBoxPosition,LEFT),UP) == Symbol.WALL ));
							boolean upUpRight = !(at(nextBoxPosition,UP) == Symbol.BOX && at(nextBoxPosition,RIGHT) == Symbol.WALL && at(to(nextBoxPosition, UP), RIGHT) == Symbol.WALL);
							boolean upUpLeft = !(at(nextBoxPosition,UP) == Symbol.BOX && at(nextBoxPosition,LEFT) == Symbol.WALL && at(to(nextBoxPosition, UP), LEFT) == Symbol.WALL);
							if( (rightUp && leftUp && upUpRight && upUpLeft) ){
//								boolean cornerUpLeft = !(at(nextBoxPosition,UP) == Symbol.WALL && deadlocks[nextBoxPosition.row][nextBoxPosition.col-1] && at(to(nextBoxPosition,DOWN),LEFT) == Symbol.BOX && at(to(to(nextBoxPosition,DOWN),LEFT),LEFT) == Symbol.WALL);
//								boolean cornerUpRight = !(at(nextBoxPosition,UP) == Symbol.WALL && deadlocks[nextBoxPosition.row][nextBoxPosition.col+1] && at(to(nextBoxPosition,DOWN),RIGHT) == Symbol.BOX && at(to(to(nextBoxPosition,DOWN),RIGHT),RIGHT) == Symbol.WALL);
//								if((cornerUpLeft && cornerUpRight)){
									makeNextMove(moves, box, dir,reachables);
//								}
							}
						} else {
							boolean rightDown = !(at(nextBoxPosition,RIGHT) == Symbol.BOX && (at(nextBoxPosition,DOWN) == Symbol.WALL ) && (at(to(nextBoxPosition,RIGHT),DOWN) == Symbol.WALL));
							boolean leftDown = !(at(nextBoxPosition,LEFT) == Symbol.BOX && (at(nextBoxPosition,DOWN) == Symbol.WALL ) && (at(to(nextBoxPosition,LEFT),DOWN) == Symbol.WALL ));
							boolean downDownRight = !(at(nextBoxPosition,DOWN) == Symbol.BOX && at(nextBoxPosition,RIGHT) == Symbol.WALL && at(to(nextBoxPosition, DOWN), RIGHT) == Symbol.WALL);
							boolean downDownLeft = !(at(nextBoxPosition,DOWN) == Symbol.BOX && at(nextBoxPosition,LEFT) == Symbol.WALL && at(to(nextBoxPosition, DOWN), LEFT) == Symbol.WALL);
							if( (rightDown && leftDown && downDownLeft && downDownRight) ){
//								boolean cornerDownLeft = !(at(nextBoxPosition,DOWN) == Symbol.WALL && deadlocks[nextBoxPosition.row][nextBoxPosition.col-1] && at(to(nextBoxPosition,UP),LEFT) == Symbol.BOX && at(to(to(nextBoxPosition,UP),LEFT),LEFT) == Symbol.WALL);
//								boolean cornerDownRight = !(at(nextBoxPosition,DOWN) == Symbol.WALL && deadlocks[nextBoxPosition.row][nextBoxPosition.col+1] && at(to(nextBoxPosition,UP),RIGHT) == Symbol.BOX && at(to(to(nextBoxPosition,UP),RIGHT),RIGHT) == Symbol.WALL);
//								if( (cornerDownLeft && cornerDownRight)){
									makeNextMove(moves, box, dir,reachables);
//								}
							}
						}

					}
				}
			}
		}
		return moves;
	}

	private void makeNextMove(ArrayList<Board> moves, Node box, Direction dir,boolean[][] reachables) {
		Node pushLocation = to(box, Direction.negDir(dir));
		//String walk = getPlayerWalk(player, pushLocation, nodes);
		//If the walk is not null we know the player can get to the required location to make the push
		
		if(reachables[pushLocation.row][pushLocation.col]){
			
//			System.out.println("Box: " + box);
//			System.out.println("Next: " + pushLocation);
//			print();
			//Create a new Board with the box moved
			moves.add(new Board(this,box,dir));
		} else {
			//Do nothing since it's not valid path push
		}
	}

	private Symbol at(Node n) {
		return n.getSymbol();
	}

	private Symbol at(Node n, Direction dir) {
		switch(dir) {
		case UP:
			return nodes[n.row-1][n.col].symbol;
		case DOWN:
			return nodes[n.row+1][n.col].symbol;
		case LEFT:
			return nodes[n.row][n.col-1].symbol;
		default: // RIGHT
			return nodes[n.row][n.col+1].symbol;
		}
	}

	private Node nodeAt(Node n) {
		return nodes[n.getRow()][n.getCol()];
	}

	private Node to(Node n, Direction dir) {
		switch(dir) {
		case UP:
			return nodes[n.row-1][n.col];
		case DOWN:
			return nodes[n.row+1][n.col];
		case LEFT:
			return nodes[n.row][n.col-1];
		default: // RIGHT
			return nodes[n.row][n.col+1];
		}
	}

	public int calculateHeuristic() {
		int h = 0;
		String path = "";
		//For each box
		for(Node box : boxes){
			//Check so the box is not already on a goal 
			if(box.symbol != Symbol.BOXGOAL){
				//If not then do a bfs to and get the distance the closest goal
				path = getClosestGoal(box, this.nodes); 
				if(path != null){
					//Add the lenght of the path to the herustic value
					h += path.length();
				}
				else {
					//We didnt find a path to a goal for a box so add a big value
					//since this is not a good thing
					h += 20;//Math.sqrt(width*height);
				}
				
			}
			else{
				h -= 40;
			}
		}
		return h;

		
//		 int h = 0;
////       for(int i = 0; i < boxes.size(); i++) {
////               if(at(boxes.get(i)) == Symbol.BOX)
////                       h += Math.abs(player.row - boxes.get(i).row) + Math.abs(player.col - boxes.get(i).col);
//////               if(at(boxes.get(i)) == Symbol.BOXGOAL)
//////                       h = 0;
////       }
//
//         for(int i = 0; i < boxes.size(); i++) {
//                 //System.out.println(boxes.get(i).row +" " + boxes.get(i).col);
//                 for(int j = 0; j < goals.size(); j++) {
//                         if(at(boxes.get(i)) == Symbol.BOX)
//                                 h += (Math.abs(goals.get(j).row - boxes.get(i).row) + Math.abs(goals.get(j).col - boxes.get(i).col));
////                         if(at(boxes.get(i)) == Symbol.BOXGOAL)
////                                 h /= 2;
//                 }
//         }
//
//         return h;
		
		
	}
	
	public int calculateHeuristic(int[][] distanceMap) {
		int h = 0;
		
		for(Node box : boxes){
			h += distanceMap[box.row][box.col];
		}
		
		return h;
	}

	public boolean isWin() {
		return numberOfFreeBoxes == 0;
	}

	public Node getPlayer() {
		return player;
	}

	public Node[][] getnodes() {
		return nodes;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void print() {
		for(int i = 0; i < nodes.length; i++) {
			for(int j= 0; j < nodes[i].length; j++) {
				//							if(deadlocks[i][j]){
				//								System.out.print("¤");
				//							} else {
				System.out.print(nodes[i][j].toChar());
				//							}
			}
			System.out.println();
		}
	}

	public void printDeadlock() {
		for(int i = 0; i < deadlocks.length; i++) {
			for(int j= 0; j < deadlocks[i].length; j++) {
				System.out.print(deadlocks[i][j] + "\t");
			}
			System.out.println();
		}
	}

	@Override
	public int hashCode() {
		int h = 0;
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				h ^= ( h << 5 ) + ( h >> 2 ) + nodes[i][j].toChar();
			}
		}
		return h;
	}

	@Override
	public int compareTo(Board otherBoard) {
		return this.accumulatedCost - otherBoard.accumulatedCost;
	}

	public boolean[][] getReachables(Node start,Node[][] map) {

		//Init stuff
		//StringBuilder sb = new StringBuilder();
		Queue<Node> q = new LinkedList<Node>();
		boolean[][] visited = new boolean[map.length][map[0].length];
		visited[start.row][start.col] = true;
		//Add the start
		//start.setDir("");
		q.add(start);

		Node currentNode;

		while(!q.isEmpty()) {
			//Take out one element
			currentNode = q.poll();

			//Go over each direction from this node
			Node nextNode;
			for(Direction dir : Direction.values()){
				nextNode = to(currentNode, dir);
				//Check if we have been here already
				if(!visited[nextNode.row][nextNode.col]){
					//Check if we can go here
					if(nextNode.symbol == Symbol.FREE|| nextNode.symbol == Symbol.GOAL || nextNode.symbol == Symbol.PLAYER || nextNode.symbol == Symbol.PLAYERGOAL){
						//nextNode.setPrevNode(currentNode);
						//nextNode.setDir(Direction.getString(dir));
						q.add(nextNode);
						visited[nextNode.row][nextNode.col] = true;
					}
				}
			}
		}
		//We didn't find a path so send back null to signal that
		return visited;
	}
	
	public String getClosestGoal(Node start, Node[][] map) {

		//Init stuff
		StringBuilder sb = new StringBuilder();
		Queue<Node> q = new LinkedList<Node>();
		boolean[][] visited = new boolean[map.length][map[0].length];
		//Add the start
		start.setDir("");
		q.add(start);

		Node currentNode;

		while(!q.isEmpty()) {
			//Take out one element
			currentNode = q.poll();
			//Check if it goal
			if(currentNode.symbol == Symbol.GOAL) {
				//We found the goal
				sb.append(currentNode.getDir());
				//Start backtracking
				Node tempNode = currentNode.getPrevNode();
				if(tempNode != null){
					while(!tempNode.equals(start)){
						sb.append(tempNode.getDir());
						tempNode = tempNode.getPrevNode();
					}
				}
				return sb.reverse().toString();
			}

			//Go over each direction from this node
			Node nextNode;
			for(Direction dir : Direction.values()){
				nextNode = to(currentNode, dir);
				//Check if we have been here already
				if(!visited[nextNode.row][nextNode.col]){
					//Check if we can go here
					if(nextNode.symbol == Symbol.FREE || nextNode.symbol == Symbol.GOAL){
						nextNode.setPrevNode(currentNode);
						nextNode.setDir(Direction.getString(dir));
						q.add(nextNode);
						visited[nextNode.row][nextNode.col] = true;
					}
				}
			}
		}
		//We didn't find a path so send back null to signal that
		return null;
	}
	
//	public StringBuilder getPlayerWalk() { 
//
//		Node start = this.prevBoard.player; 
//		Node stop = this.pushLocation;
//		Node[][] map = this.prevBoard.nodes;
//
//		System.out.println("");
//		System.out.println("This board");
//		this.print();
//		System.out.println("Prev board");
//		this.prevBoard.print();
//		System.out.println("Old player pos: " + start);
//		System.out.println("Stop: " + stop);
//		System.out.println("");
//		
//		//Init stuff
//		StringBuilder sb = new StringBuilder();
//		Queue<Node> q = new LinkedList<Node>();
//		boolean[][] visited = new boolean[map.length][map[0].length];
//		//Add the start
//		start.setDir("");
//		q.add(start);
//
//		Node currentNode;
//
//		while(!q.isEmpty()) {
//			//Take out one element
//			currentNode = q.poll();
//			//Check if it goal
//			if(currentNode.equals(stop)) {
//				//We found the goal
//				sb.append(currentNode.getDir());
//				//Start backtracking
//				Node tempNode = currentNode.getPrevNode();
//				if(tempNode != null){
//					while(!tempNode.equals(start)){
//						sb.append(tempNode.getDir());
//						tempNode = tempNode.getPrevNode();
//					}
//					
//				}
//				sb.append(Direction.getString(this.pushDir));
//				//sb.append(Direction.getString(pushDir));
//				
//				//sb.reverse();
//				
//				
//				
//				String sub = sb.toString();
//				
//				System.out.println("sub - Path: " + sub);
//				//System.out.println("");
//				//return "|"+sb.reverse().append(Direction.getString(this.pushDir)).toString()+"|";
//				return sb;
//			}
//
//			//Go over each direction from this node
//			Node nextNode;
//			for(Direction dir : Direction.values()){
//				nextNode = to(currentNode, dir);
//				//Check if we have been here already
//				if(!visited[nextNode.row][nextNode.col]){
//					//Check if we can go here
//					if(nextNode.symbol == Symbol.FREE || nextNode.symbol == Symbol.GOAL || nextNode.symbol == Symbol.PLAYER || nextNode.symbol == Symbol.PLAYERGOAL){
//						nextNode.setPrevNode(currentNode);
//						nextNode.setDir(Direction.getString(dir));
//						q.add(nextNode);
//						visited[nextNode.row][nextNode.col] = true;
//					}
//				}
//			}
//		}
//		System.out.println("TOLO");
//		
//		//this.print();
//		System.out.println("Start: " + start);
//		System.out.println("Stop: " + stop);
//		//We didn't find a path so send back null to signal that
//		return null;
//	}
	
	public String getPlayerWalk2(){
		StringBuilder sb = new StringBuilder();
		Queue<Node> q = new LinkedList<Node>();
		
		Node start = this.prevBoard.player; 
		Node stop = this.pushLocation;
		Node[][] map = this.prevBoard.nodes;
		
		boolean[][] visited = new boolean[map.length][map[0].length];
		
		Node currentPos;
		Node nextNode;
		
		start.setDir("");
		start.setPrevNode(null);
		visited[start.row][start.col] = true;
		
		q.add(start);
		
		while(!q.isEmpty()){
			currentPos = q.poll();
			
			//We found the goal
			if(currentPos.row == stop.row && currentPos.col == stop.col){
				//Backtrack it
				Node backTrack = currentPos;
				while(backTrack != null){
					sb.insert(0, backTrack.getDir());
					backTrack = backTrack.getPrevNode();
				}
				
				//Add last push
				sb.append(Direction.getString(pushDir));
				String subPath =  sb.toString();
				
				return subPath;
			}
			
			//Add all new direction
			for(Direction dir: Direction.values()){
				nextNode = prevBoard.to(currentPos,dir);
				//Check the visited list
				if(!visited[nextNode.row][nextNode.col]){
					//Check if it's a valid move location
					if(nextNode.symbol == Symbol.FREE || nextNode.symbol == Symbol.GOAL || nextNode.symbol == Symbol.PLAYER || nextNode.symbol == Symbol.PLAYERGOAL){
						nextNode.setPrevNode(currentPos);
						nextNode.setDir(Direction.getString(dir));
						visited[nextNode.row][nextNode.col] = true;
						q.add(nextNode);
					}
				}
			}
		}
		
		System.out.println("THIS SHOULD NOT HAPPEND");
		
		return null;
	}
}
