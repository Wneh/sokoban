
public enum Direction {
	LEFT, RIGHT, DOWN, UP;

//	static char get(Direction dir) {
//		switch(dir) {
//		case UP:
//			return 'U';
//		case DOWN:
//			return 'D';
//		case LEFT:
//			return 'L';
//		default:
//			return 'R';
//		}
//	}
	
	static String getString(Direction dir){
		switch(dir) {
		case UP:
			return "U";
		case DOWN:
			return "D";
		case LEFT:
			return "L";
		case RIGHT:
			return "R";
		default:
			System.out.println("Null from Direction.getString()");
			return null;	
		}
	}
	
	static Direction negDir(Direction dir){
		switch(dir) {
		case UP:
			return DOWN;
		case DOWN:
			return UP;
		case LEFT:
			return RIGHT;
		case RIGHT:
			return LEFT;
		default:
			System.out.println("Null from Direction.negDir()");
			return null;
		}
	}
}
