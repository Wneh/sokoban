public class Node {

	Symbol symbol;
	int row;
	int col;
	private char dir;
	private Node prevNode;
	
	public Node(char type, int row, int col) {
		symbol = Symbol.get(type);
		this.row = row;
		this.col = col;
	}
	
	public Symbol getSymbol() {
		return symbol;
	}

	public int getRow() {
		return row;
	}

	public int getCol() {
		return col;
	}
	
	public char getDir(){
		return this.dir;
	}
	
	public void setDir(char dir){
		this.dir = dir;
	}
	
	public void setPrevNode(Node prev){
		this.prevNode = prev;
	}
	
	public Node getProvNode(){
		return this.prevNode;
	}
	
	public char toChar() {
		return Symbol.get(symbol);
	}
	
	public boolean equals(Node other){
		return (this.symbol == other.symbol && this.row == other.row && this.col == other.col);
	}
}
