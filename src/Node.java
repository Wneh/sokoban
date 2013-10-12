public class Node {

	Symbol symbol;
	int row;
	int col;
	
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
	
	public char toChar() {
		return Symbol.get(symbol);
	}
}
