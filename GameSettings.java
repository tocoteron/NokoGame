import javafx.scene.paint.Color;

public class GameSettings
{
	public static final int WINDOW_RIGHT_PADDING = 256;
	public static final int SQUARE_WIDTH = 64;
	public static final int SQUARE_HEIGHT = 64;
	public static final Color WINDOW_BACKGROUND_COLOR = Color.rgb(255, 255, 255);
	public static final Color SQUARE_COLOR = Color.rgb(32, 160, 128);
	public static final Color SQUARE_BORDER_COLOR = Color.rgb(32, 192, 192);
	public static final Color PUTTABLE_SQUARE_COLOR = Color.rgb(180, 96, 96);
	public static final Color BOMB_ESTIMATION_SQUARE_COLOR = Color.rgb(192, 32, 192);

	public static final int COMPUTER_THINKING_TIME = 500;

	// 1 or 2
	public static int GAME_MODE = 1;

	public static int ROW_COUNT = 8;
	public static int COL_COUNT = 8;

	public static int KINOKO_SCORE = 0;
	public static int TAKENOKO_SCORE = 0;
}
