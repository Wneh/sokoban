import java.util.ArrayList;
import java.util.HashSet;

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
	StringBuilder path;
	ArrayList<Node> boxes;
	ArrayList<Node> goals;
	public int f, g; // Total and accumulated cost

	public Board(String board, int width, int height) {

		nodes = new Node[height][width];
		path = new StringBuilder();
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

				if(type == '$') {
					boxes.add(nodes[i][j]);
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

	}

	public Board(Board oldBoard, Direction dir) { // create a new board and move the player in a valid direction
		this.nodes = new Node[oldBoard.getHeight()][oldBoard.getWidth()];
		this.player = new Node('@', oldBoard.player.row, oldBoard.player.col);
		this.numberOfFreeBoxes = oldBoard.numberOfFreeBoxes;
		this.width = oldBoard.getWidth();
		this.height = oldBoard.getHeight();
		this.boxes = new ArrayList<Node>(oldBoard.boxes);
		this.goals = new ArrayList<Node>(oldBoard.goals);
		this.path = new StringBuilder(oldBoard.path);
		this.accumulatedCost = oldBoard.accumulatedCost;
		copyOldBoard(oldBoard);
		move(dir); // make one of the valid moves
	}

	// For BFS
	public Board(Board oldBoard, Node newPlayer, String path) {
		this.nodes = new Node[oldBoard.getHeight()][oldBoard.getWidth()];
		this.numberOfFreeBoxes = oldBoard.numberOfFreeBoxes;
		this.width = oldBoard.getWidth();
		this.height = oldBoard.getHeight();
		this.boxes = new ArrayList<Node>(oldBoard.boxes);
		this.goals = new ArrayList<Node>(oldBoard.goals);
		this.path = new StringBuilder(path);
		this.accumulatedCost = oldBoard.accumulatedCost;
		copyOldBoard(oldBoard);
		if(!path.isEmpty()) {
			if(oldBoard.player.symbol == Symbol.PLAYERGOAL) {
				this.nodes[oldBoard.player.row][oldBoard.player.col].symbol = Symbol.GOAL;
			} else {
				this.nodes[oldBoard.player.row][oldBoard.player.col].symbol = Symbol.FREE;
			}
			if(newPlayer.symbol == Symbol.GOAL)
				this.nodes[newPlayer.row][newPlayer.col].symbol = Symbol.PLAYERGOAL;
			else
				this.nodes[newPlayer.row][newPlayer.col].symbol = Symbol.PLAYER;
		}
		this.player = newPlayer;
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

	public void move(Direction dir) {
		Node newPos = to(player, dir); // get the node in the given direction.
		Node playerPos = nodeAt(player); // get the node where the player is
		if(at(newPos) == Symbol.FREE) { // if the node we want to move to is free
			if(at(playerPos) == Symbol.PLAYERGOAL) { // and if the player is standing on a goal
				playerPos.symbol = Symbol.GOAL; // then the position will be a goal again
			} else {
				playerPos.symbol = Symbol.FREE; // else the position will be free again
			}
			newPos.symbol = Symbol.PLAYER; // the new position will be the player
			setPlayer(newPos); // set player's new row and col index.
		} else if(at(newPos) == Symbol.GOAL) { // if the new position is a goal
			if(playerPos.symbol == Symbol.PLAYERGOAL) // and if the player is on goal
				playerPos.symbol = Symbol.GOAL; // then the position will be goal again
			else if(playerPos.symbol == Symbol.PLAYER) { // if it is just the player
				playerPos.symbol = Symbol.FREE; // then the position will be free again
			}
			newPos.symbol = Symbol.PLAYERGOAL; // the new position will be player on goal
			setPlayer(newPos); // set player's new row and col index.
		} else if(at(newPos) == Symbol.BOX || at(newPos) == Symbol.BOXGOAL) { // if the new position is a box or a boxgoal.
			Node nextBoxPos = to(newPos, dir); // the position to which we are trying to move a box
			if(at(nextBoxPos) == Symbol.FREE) { // if the position is free then the box can be moved there
				
				
				if(dir == RIGHT){
					boolean downRight = !(at(nextBoxPos,DOWN) == Symbol.BOX && (at(nextBoxPos,RIGHT) == Symbol.WALL) && (at(to(nextBoxPos,DOWN),RIGHT) == Symbol.WALL));
					boolean upRight = !(at(nextBoxPos,UP) == Symbol.BOX && (at(nextBoxPos,RIGHT) == Symbol.WALL) && (at(to(nextBoxPos,UP),RIGHT) == Symbol.WALL ));
					if(downRight && upRight)
						doStuff(newPos, playerPos, nextBoxPos);
				} else if(dir == LEFT){
					boolean downLeft =  !(at(nextBoxPos,DOWN) == Symbol.BOX && (at(nextBoxPos,LEFT) == Symbol.WALL) && (at(to(nextBoxPos,DOWN),LEFT) == Symbol.WALL ));
					boolean upLeft = !(at(nextBoxPos,UP) == Symbol.BOX && (at(nextBoxPos,LEFT) == Symbol.WALL) && (at(to(nextBoxPos,UP),LEFT) == Symbol.WALL ));
					if(downLeft && upLeft)
						doStuff(newPos, playerPos, nextBoxPos);
				} else if(dir == UP){
					boolean rightUp = !(at(nextBoxPos,RIGHT) == Symbol.BOX && (at(nextBoxPos,UP) == Symbol.WALL) && (at(to(nextBoxPos,RIGHT),UP) == Symbol.WALL ));
					boolean leftUp = !(at(nextBoxPos,LEFT) == Symbol.BOX && (at(nextBoxPos,UP) == Symbol.WALL) && (at(to(nextBoxPos,LEFT),UP) == Symbol.WALL ));
					if(rightUp && leftUp)
						doStuff(newPos, playerPos, nextBoxPos);
				} else if(dir == DOWN){
					boolean rightDown = !(at(nextBoxPos,RIGHT) == Symbol.BOX && (at(nextBoxPos,DOWN) == Symbol.WALL ) && (at(to(nextBoxPos,RIGHT),DOWN) == Symbol.WALL));
					boolean leftDown = !(at(nextBoxPos,LEFT) == Symbol.BOX && (at(nextBoxPos,DOWN) == Symbol.WALL ) && (at(to(nextBoxPos,LEFT),DOWN) == Symbol.WALL ));
					if(rightDown && leftDown)
						doStuff(newPos, playerPos, nextBoxPos);
				}
			} else if(at(nextBoxPos) == Symbol.GOAL) { // if the position is goal then the box can be moved there, but will be on a goal

				for(int i = 0; i < boxes.size(); i++) {
					if(boxes.get(i).equals(newPos)) {
						boxes.remove(i);
					}
				}

				if(playerPos.symbol == Symbol.PLAYERGOAL) // // if the player was standing on goal
					playerPos.symbol = Symbol.GOAL; // then the position will be a goal again
				else if(playerPos.symbol == Symbol.PLAYER) { // if it is just the player
					playerPos.symbol = Symbol.FREE; // then the position will be free again.
				}
				if(at(newPos) == Symbol.BOXGOAL) { // if the new position is a box on goal
					newPos.symbol = Symbol.PLAYERGOAL; // then player will stand on a goal after moving the box
				} else {
					newPos.symbol = Symbol.PLAYER; // else the player will stand by itself.
					numberOfFreeBoxes--;
				}
				boxes.add(nextBoxPos);
				nextBoxPos.symbol = Symbol.BOXGOAL;// the new box position is a box ON GOAL
				setPlayer(newPos); // set player's new row and col index.
			}
		}
		path.append(Direction.get(dir));
	}
	
	public void doStuff(Node newPos, Node playerPos, Node nextBoxPos){
		for(int i = 0; i < boxes.size(); i++) {
			if(boxes.get(i).equals(newPos)) {
				boxes.remove(i);
			} 
		}

		if(playerPos.symbol == Symbol.PLAYERGOAL) { // if the player was standing on goal
			playerPos.symbol = Symbol.GOAL; // then the position will be a goal again
		}
		else if(playerPos.symbol == Symbol.PLAYER) { // if it is just the player
			playerPos.symbol = Symbol.FREE; // then the position will be free again.
		}
		if(at(newPos) == Symbol.BOXGOAL) { // if the new position is a box on goal
			numberOfFreeBoxes++;
			newPos.symbol = Symbol.PLAYERGOAL; // then player will stand on a goal after moving the box
		} else {
			newPos.symbol = Symbol.PLAYER; // else the player will stand by itself.
		}
		boxes.add(nextBoxPos);
		nextBoxPos.symbol = Symbol.BOX; // the new box position is a box
		setPlayer(newPos); // set player's new row and col index.
	}

	private void setPlayer(Node newPos) {
		player.row = newPos.row;
		player.col = newPos.col;
	}

	public ArrayList<Direction> getPossibleMoves() {
		ArrayList<Direction> moves = new ArrayList<Direction>();
		for(Direction dir: Direction.values()) {
			Symbol newDir = at(player, dir);
			if(newDir == Symbol.FREE || newDir == Symbol.BOX || newDir == Symbol.BOXGOAL || newDir == Symbol.GOAL) {
				if(newDir == Symbol.BOX || newDir == Symbol.BOXGOAL) {
					Node boxDir = to(to(player,dir),dir);
					if(deadlocks[boxDir.row][boxDir.col]) {
						//System.out.println("deadlock");
						continue;
					}
				}
				moves.add(dir);
			}
		}
		return moves;
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
		for(int i = 0; i < boxes.size(); i++) {
			if(at(boxes.get(i)) == Symbol.BOX)
				h += Math.abs(player.row - boxes.get(i).row) + Math.abs(player.col - boxes.get(i).col);
			if(at(boxes.get(i)) == Symbol.BOXGOAL)
				h-=6;
		}

		for(int i = 0; i < boxes.size(); i++) {
			//System.out.println(boxes.get(i).row +" " + boxes.get(i).col);
			for(int j = 0; j < goals.size(); j++) {
				if(at(boxes.get(i)) == Symbol.BOX)
					h += (Math.abs(goals.get(j).row - boxes.get(i).row) + Math.abs(goals.get(j).col - boxes.get(i).col));
				if(at(boxes.get(i)) == Symbol.BOXGOAL)
					h-= 6;
			}
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
				if(deadlocks[i][j]){
					System.out.print("¤");
				} else {
					System.out.print(nodes[i][j].toChar());
				}
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
}
