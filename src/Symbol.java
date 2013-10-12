
public enum Symbol {
	WALL, FREE, BOX, PLAYER, PLAYERGOAL, BOXGOAL, GOAL;

	static Symbol get(char type) {
		switch(type) {
		case '#':
			return WALL;
		case '.':
			return GOAL;
		case '$':
			return BOX;
		case '@':
			return PLAYER;
		case '+':
			return PLAYERGOAL;
		case '*':
			return BOXGOAL;
		default:
			return FREE;
		}
	}

	static char get(Symbol type) {
		switch(type) {
		case WALL:
			return '#';
		case GOAL:
			return '.';
		case BOX:
			return '$';
		case PLAYER:
			return '@';
		case PLAYERGOAL:
			return '+';
		case BOXGOAL:
			return '*';
		default:
			return ' ';
		}
	}
}