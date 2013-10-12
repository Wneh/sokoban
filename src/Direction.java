
public enum Direction {
	LEFT, RIGHT, DOWN, UP;

	static char get(Direction dir) {
		switch(dir) {
		case UP:
			return 'U';
		case DOWN:
			return 'D';
		case LEFT:
			return 'L';
		default:
			return 'R';
		}
	}
}
