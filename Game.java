import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import javafx.event.*;

import javafx.animation.AnimationTimer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.tools.StandardJavaFileManager;

import javafx.application.Platform;

import java.util.*;

public class Game extends Application
{
	private SquareSprite.Type playerType;
	private SquareSprite[][] squareSprites;
	private SquareSprite[][] squareSpritesForComputer;

	private int[][] bombPowers;
	private int[][] bombPowersForComputer;

	private int[][] surroundingBombCounts;
	private int[][] surroundingBombCountsForComputer;

	private final int ROW_COUNT = GameSettings.ROW_COUNT;
	private final int COL_COUNT = GameSettings.COL_COUNT;
	private final double CANVAS_WIDTH = colToX(COL_COUNT);
	private final double CANVAS_HEIGHT = rowToY(ROW_COUNT);

	private Label playerTypeLabel = new Label("TURN");

	private Image kinokoImage = new Image(KinokoSquareSprite.filePath);
	private Image takenokoImage = new Image(TakenokoSquareSprite.filePath);
	private ImageView playerTypeImageView = new ImageView();

	// Level of deep searching.
	// [Warning] Setting it to a larger value takes time, and the processing becomes heavier.
	private final int SEARCH_LEVEL = 4;

	private final int[][] VALUE_OF_PLACE = BoardEvaluation.createvalueOFPlace(COL_COUNT, ROW_COUNT);

	public static void main(String[] args)
	{
		launch(args);
	}

	@Override
	public void start(Stage primaryStage)
	{
		Group root = new Group();
		Canvas canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
		GraphicsContext gc = canvas.getGraphicsContext2D();

		primaryStage.setTitle("GameScene");

		// atatch parts
		root.getChildren().add(canvas);
		root.getChildren().add(playerTypeLabel);
		root.getChildren().add(playerTypeImageView);

		// show stage
		primaryStage.setScene(new Scene(root, CANVAS_WIDTH + GameSettings.WINDOW_RIGHT_PADDING, CANVAS_HEIGHT, GameSettings.WINDOW_BACKGROUND_COLOR));
		primaryStage.show();

		// event for user input
		canvas.addEventHandler(
			MouseEvent.MOUSE_CLICKED,
			new EventHandler<MouseEvent>()
			{
				@Override
				public void handle(MouseEvent e)
				{
					if(GameSettings.GAME_MODE == 2 || playerType == SquareSprite.Type.Takenoko)
					{
						tryPutSquareSprite(squareSprites, bombPowers, surroundingBombCounts, yToRow(e.getY()), xToCol(e.getX()), true);

						squareSpritesForComputer = deepCopy2DArrayForSquareSprite(squareSprites);
						bombPowersForComputer = deepCopy2DArrayForInt(bombPowers);
						surroundingBombCounts = deepCopy2DArrayForInt(surroundingBombCounts);
					}
				}
			}
		);

		// settings for playerTypeLabel
		playerTypeLabel.setTranslateX(CANVAS_WIDTH + 32);
		playerTypeLabel.setTranslateY(32);
		playerTypeLabel.setFont(Font.font("Verdana", 20));

		// settings for playerTypeImageView
		playerTypeImageView.setImage(takenokoImage);
		playerTypeImageView.setX(CANVAS_WIDTH + (GameSettings.WINDOW_RIGHT_PADDING - playerTypeImageView.getImage().getWidth()) / 2);
		playerTypeImageView.setY(48);

		// init board to start the game
		initGame();

		// game loop
		new AnimationTimer()
		{
			long startNanoTime = System.nanoTime();

			public void handle(long currentNanoTime)
			{
				double deltaTime = (currentNanoTime - startNanoTime) / 1000000000.0f;
				startNanoTime = currentNanoTime;

				// computer turn
				if(GameSettings.GAME_MODE == 1 && playerType == SquareSprite.Type.Kinoko)
				{
					int computerPos = AlphaBeta();

					if(computerPos == Integer.MIN_VALUE)
					{
						flipPlayerType();
					}
					else
					{
						new Thread(new Runnable() {
							@Override
							public void run() {
								// thinking time
								try
								{
									Thread.sleep(GameSettings.COMPUTER_THINKING_TIME);
								}
								catch (Exception e)
								{
								}
								Platform.runLater(new Runnable() {
									@Override
									public void run() {
										int computerRow = computerPos % ROW_COUNT;
										int computerCol = computerPos / ROW_COUNT;
										tryPutSquareSprite(squareSprites, bombPowers, surroundingBombCounts, computerRow, computerCol, true);
										squareSpritesForComputer = deepCopy2DArrayForSquareSprite(squareSprites);
										bombPowersForComputer = deepCopy2DArrayForInt(bombPowers);
										surroundingBombCounts = deepCopy2DArrayForInt(surroundingBombCounts);
									}
								});
							}
						}).start();
						/*
						int computerRow = computerPos % ROW_COUNT;
						int computerCol = computerPos / ROW_COUNT;
						tryPutSquareSprite(squareSprites, bombPowers, surroundingBombCounts, computerRow, computerCol, true);
						squareSpritesForComputer = deepCopy2DArrayForSquareSprite(squareSprites);
						bombPowersForComputer = deepCopy2DArrayForInt(bombPowers);
						surroundingBombCounts = deepCopy2DArrayForInt(surroundingBombCounts);
						*/
					}
				}

				// set background color
				gc.setFill(GameSettings.SQUARE_COLOR);
				gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

				// draw squares
				drawBombEstimationSquares(gc);
				drawHighlightSquares(gc, playerType);
				updateSquareSprites(deltaTime);
				drawSquareSprites(gc);

				// draw grid on the board
				gc.setStroke(GameSettings.SQUARE_BORDER_COLOR);
				for (int col = 0; col <= COL_COUNT; ++col)
				{
					gc.strokeLine(colToX(col), 0, colToX(col), CANVAS_HEIGHT);
				}
				for (int row = 0; row <= ROW_COUNT; ++row)
				{
					gc.strokeLine(0, rowToY(row), CANVAS_WIDTH, rowToY(row));
				}
			}
		}.start();
	}
	
	private void initGame()
	{
		playerType = SquareSprite.Type.Takenoko;
		playerTypeImageView.setImage(takenokoImage);

		squareSprites = new SquareSprite[ROW_COUNT][COL_COUNT];
		squareSpritesForComputer = new SquareSprite[ROW_COUNT][COL_COUNT];

		bombPowers = new int[ROW_COUNT][COL_COUNT];
		bombPowersForComputer = new int[ROW_COUNT][COL_COUNT];

		surroundingBombCounts = new int[ROW_COUNT][COL_COUNT];
		surroundingBombCountsForComputer = new int[ROW_COUNT][COL_COUNT];

		initSquareSprites(squareSprites);
		initBombs(squareSprites, bombPowers, surroundingBombCounts);

		squareSpritesForComputer = deepCopy2DArrayForSquareSprite(squareSprites);
		bombPowersForComputer = deepCopy2DArrayForInt(bombPowers);
		surroundingBombCountsForComputer = deepCopy2DArrayForInt(surroundingBombCounts);

		SoundPlayer.play(Sound.Type.GameBgm);
	}

	private void initSquareSprites(SquareSprite[][] squareSprites)
	{
		for (int row = 0; row < ROW_COUNT; ++row)
		{
			for (int col = 0; col < COL_COUNT; ++col)
			{
				putSquareSprite(squareSprites, SquareSprite.Type.None, row, col);
			}
		}

		// put initial squares
		putSquareSprite(squareSprites, SquareSprite.Type.Kinoko, ROW_COUNT / 2 - 1, COL_COUNT / 2 - 1);
		putSquareSprite(squareSprites, SquareSprite.Type.Kinoko, ROW_COUNT / 2, COL_COUNT / 2);
		putSquareSprite(squareSprites, SquareSprite.Type.Takenoko, ROW_COUNT / 2 - 1, COL_COUNT / 2);
		putSquareSprite(squareSprites, SquareSprite.Type.Takenoko, ROW_COUNT / 2, COL_COUNT / 2 - 1);
	}

	private void initBombs(SquareSprite[][] squareSprites, int[][] bombPowers, int[][] surroundingBombCounts)
	{
		for(int row = 0; row < squareSprites.length; ++row)
		{
			for(int col = 0; col < squareSprites[0].length; ++col)
			{
				bombPowers[row][col] = 0;
			}
		}

		Random rnd = new Random();
		int bombCount = 10;
		int bombPower = 1;

		for(int i = 0; i < bombCount; ++i)
		{
			int row, col;

			do
			{
				row = rnd.nextInt(squareSprites.length);
				col = rnd.nextInt(squareSprites[0].length);
			}
			while(!(squareSprites[row][col].getType() == SquareSprite.Type.None && bombPowers[row][col] == 0));

			putBomb(bombPowers, surroundingBombCounts, row, col, bombPower);
		}
	}

	private void explodeBomb(SquareSprite[][] squareSprites, int[][] bombPowers, int[][] surroundingBombCounts, int baseRow, int baseCol, boolean playSound)
	{
		int explosivePower = bombPowers[baseRow][baseCol];
		int topRow = Math.max(0, baseRow - explosivePower);
		int bottomRow = Math.min(ROW_COUNT - 1, baseRow + explosivePower);
		int leftCol = Math.max(0, baseCol - explosivePower);
		int rightCol = Math.min(COL_COUNT - 1, baseCol + explosivePower);

		for(int row = topRow; row <= bottomRow; ++row)
		{
			for(int col = leftCol; col <= rightCol; ++col)
			{
				putSquareSprite(squareSprites, getFlippedSquareType(squareSprites[row][col].getType()), row, col);
			}
		}

		takeBomb(bombPowers, surroundingBombCounts, baseRow, baseCol);

		if(playSound)
		{
			SoundPlayer.play(Sound.Type.Explosion);
		}
	}

	private void putBomb(int[][] bombPowers, int[][] surroundingBombCounts, int row, int col, int explosivePower)
	{
		updateBomb(bombPowers, surroundingBombCounts, row, col, explosivePower, 1);
	}

	private void takeBomb(int[][] bombPowers, int[][] surroundingBombCounts, int row, int col)
	{
		updateBomb(bombPowers, surroundingBombCounts, row, col, -bombPowers[row][col], -1);
	}

	private void updateBomb(int[][] bombPowers, int[][] surroundingBombCounts, int baseRow, int baseCol, int explosivePower, int dCount)
	{
		int topRow = Math.max(0, baseRow - Math.abs(explosivePower));
		int bottomRow = Math.min(ROW_COUNT - 1, baseRow + Math.abs(explosivePower));
		int leftCol = Math.max(0, baseCol - Math.abs(explosivePower));
		int rightCol = Math.min(COL_COUNT - 1, baseCol + Math.abs(explosivePower));

		bombPowers[baseRow][baseCol] += explosivePower;

		for(int row = topRow; row <= bottomRow; ++row)
		{
			for(int col = leftCol; col <= rightCol; ++col)
			{
				surroundingBombCounts[row][col] += dCount;
			}
		}
	}

	private void updateSquareSprites(double deltaTime)
	{
		for(int row = 0; row < squareSprites.length; ++row)
		{
			for(int col = 0; col < squareSprites[0].length; ++col)
			{
				squareSprites[row][col].update(deltaTime);
			}
		}
	}

	private void drawSquareSprites(GraphicsContext graphicsContext)
	{
		for(int row = 0; row < squareSprites.length; ++row)
		{
			for(int col = 0; col < squareSprites[0].length; ++col)
			{
				squareSprites[row][col].draw(graphicsContext);
			}
		}
	}

	private void drawHighlightSquares(GraphicsContext graphicsContext, SquareSprite.Type squareType)
	{
		for(int row = 0; row < squareSprites.length; ++row)
		{
			for(int col = 0; col < squareSprites[0].length; ++col)
			{
				if(checkPuttable(squareSprites, squareType, row, col))
				{
					drawColoredSquare(graphicsContext, GameSettings.PUTTABLE_SQUARE_COLOR, row, col);
				}
			}
		}
	}

	private void drawBombEstimationSquares(GraphicsContext graphicsContext)
	{
		double dRed = GameSettings.BOMB_ESTIMATION_SQUARE_COLOR.getRed() - GameSettings.SQUARE_COLOR.getRed();
		double dGreen = GameSettings.BOMB_ESTIMATION_SQUARE_COLOR.getGreen() - GameSettings.SQUARE_COLOR.getGreen();
		double dBlue = GameSettings.BOMB_ESTIMATION_SQUARE_COLOR.getBlue() - GameSettings.SQUARE_COLOR.getBlue();

		for(int row = 0; row < squareSprites.length; ++row)
		{
			for(int col = 0; col < squareSprites[0].length; ++col)
			{
				int bombCount = surroundingBombCounts[row][col];
				if(bombCount > 0)
				{
					double r = GameSettings.SQUARE_COLOR.getRed() + dRed * bombCount / 9;
					double g = GameSettings.SQUARE_COLOR.getGreen() + dGreen * bombCount / 9;
					double b = GameSettings.SQUARE_COLOR.getBlue() + dBlue * bombCount / 9;
					drawColoredSquare(graphicsContext, Color.color(r, g, b), row, col);
				}
			}
		}
	}

	private void drawColoredSquare(GraphicsContext graphicsContext, Color color, int row, int col)
	{
		graphicsContext.setFill(color);
		graphicsContext.fillRect(colToX(col), rowToY(row), GameSettings.SQUARE_WIDTH, GameSettings.SQUARE_HEIGHT);
	}

	private boolean tryPutSquareSprite(SquareSprite[][] squareSprites, int[][] bombPowers, int[][] surroundingBombCounts, int row, int col, boolean playSound)
	{
		if (checkPuttable(squareSprites, playerType, row, col))
		{
			putSquareSprite(squareSprites, playerType, row, col);
			//flipSquares(playerType, row, col);

			if(bombPowers[row][col] > 0)
			{
				explodeBomb(squareSprites, bombPowers, surroundingBombCounts, row, col, playSound);
			}
			else
			{
				flipSquares(squareSprites, playerType, row, col);
			}

			printNowScore();

			if (hasFinishedGame(squareSprites))
			{
				System.out.println("Clear!!!");
				//showClearMessageDialog();
				GameSettings.KINOKO_SCORE = countSquares(squareSprites, SquareSprite.Type.Kinoko);
				GameSettings.TAKENOKO_SCORE = countSquares(squareSprites, SquareSprite.Type.Takenoko);
				SoundPlayer.stop(Sound.Type.GameBgm);
				GameStart.changeView("score.fxml");
			}
			else if (existsPuttable(squareSprites, getFlippedSquareType(playerType)))
			{
				flipPlayerType();
				System.out.println(playerType);
			}

			if(playSound)
			{
				SoundPlayer.play(Sound.Type.Putting);
			}

			return true;
		}

		return false;
	}

	private void putSquareSprite(SquareSprite[][] squareSprites, SquareSprite.Type squareType, int row, int col)
	{
		final double x = colToX(col);
		final double y = rowToY(row);

		switch (squareType)
		{
		case None:
			squareSprites[row][col] = new NoneSquareSprite(x, y);
			break;
		case Kinoko:
			squareSprites[row][col] = new KinokoSquareSprite(x, y);
			break;
		case Takenoko:
			squareSprites[row][col] = new TakenokoSquareSprite(x, y);
			break;
		}
	}

	private void takeSquareSprite(SquareSprite[][] squareSprites, int row, int col)
	{
		putSquareSprite(squareSprites, SquareSprite.Type.None, row, col);
	}

	private void flipSquares(SquareSprite[][] squareSprites, SquareSprite.Type squareType, int row, int col)
	{
		int[] dRow = { -1, -1, 0, 1, 1, 1, 0, -1 };
		int[] dCol = { 0, 1, 1, 1, 0, -1, -1, -1 };

		for (int d = 0; d < 8; ++d)
		{
			int nextRow = row + dRow[d];
			int nextCol = col + dCol[d];
			if (checkPuttable(squareSprites, squareType, nextRow, nextCol, dRow[d], dCol[d], 0))
			{
				while (squareSprites[nextRow][nextCol].getType() == getFlippedSquareType(squareType))
				{
					putSquareSprite(squareSprites, squareType, nextRow, nextCol);
					nextRow += dRow[d];
					nextCol += dCol[d];
				}
			}
		}
	}

	private boolean hasFinishedGame(SquareSprite[][] squareSprites)
	{
		return !existsPuttable(squareSprites, SquareSprite.Type.Kinoko) && !existsPuttable(squareSprites, SquareSprite.Type.Takenoko);
	}

	// if there is a puttable square, this function returns true
	private boolean existsPuttable(SquareSprite[][] squareSprites, SquareSprite.Type squareType)
	{
		for (int row = 0; row < squareSprites.length; ++row)
		{
			for (int col = 0; col < squareSprites[0].length; ++col)
			{
				if (checkPuttable(squareSprites, squareType, row, col)) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean checkPuttable(SquareSprite[][] squareSprites, SquareSprite.Type squareType, int row, int col)
	{
		if (squareSprites[row][col].getType() != SquareSprite.Type.None)
		{
			return false;
		}

		boolean isPuttable = false;
		int[] dRow = { -1, -1, 0, 1, 1, 1, 0, -1 };
		int[] dCol = { 0, 1, 1, 1, 0, -1, -1, -1 };

		for (int d = 0; d < 8; ++d)
		{
			int nextRow = row + dRow[d];
			int nextCol = col + dCol[d];
			isPuttable = isPuttable || checkPuttable(squareSprites, squareType, nextRow, nextCol, dRow[d], dCol[d], 0);
		}

		return isPuttable;
	}

	private boolean checkPuttable(SquareSprite[][] squareSprites, SquareSprite.Type squareType, int row, int col, int dRow, int dCol, int otherCount)
	{
		if (col < 0 || col >= COL_COUNT || row < 0 || row >= ROW_COUNT)
		{
			return false;
		}

		if (squareSprites[row][col].getType() == squareType)
		{
			return otherCount > 0;
		}
		else if (squareSprites[row][col].getType() == getFlippedSquareType(squareType))
		{
			int nextRow = row + dRow;
			int nextCol = col + dCol;
			return checkPuttable(squareSprites, squareType, nextRow, nextCol, dRow, dCol, otherCount + 1);
		}
		else
		{
			return false;
		}
	}

	private SquareSprite.Type getFlippedSquareType(SquareSprite.Type squareType)
	{
		switch (squareType)
		{
		case None:
			return SquareSprite.Type.None;
		case Kinoko:
			return SquareSprite.Type.Takenoko;
		case Takenoko:
			return SquareSprite.Type.Kinoko;
		default:
			return SquareSprite.Type.None;
		}
	}

	private void flipPlayerType()
	{
		playerType = getFlippedSquareType(playerType);

		switch (playerType)
		{
		case None:
			break;
		case Kinoko:
			playerTypeImageView.setImage(kinokoImage);
			break;
		case Takenoko:
			playerTypeImageView.setImage(takenokoImage);
			break;
		}
	}

	private int countSquares(SquareSprite[][] squareSprites, SquareSprite.Type squareType)
	{
		int cnt = 0;

		for (int row = 0; row < squareSprites.length; ++row)
		{
			for (int col = 0; col < squareSprites[0].length; ++col)
			{
				if (squareSprites[row][col].getType() == squareType)
				{
					++cnt;
				}
			}
		}

		return cnt;
	}

	private void printNowScore()
	{
		SquareSprite.Type checkType = SquareSprite.Type.Takenoko;

		System.out.println("\t" + checkType + " : " + countSquares(squareSprites, checkType));

		checkType = SquareSprite.Type.Kinoko;
		System.out.println("\t" + checkType + " : " + countSquares(squareSprites, checkType));

		checkType = SquareSprite.Type.None;
		System.out.println("\t" + checkType + " : " + countSquares(squareSprites, checkType));
	}

	private void showClearMessageDialog()
	{
		showMessageDialog(getScoreByString());
	}

	private void showMessageDialog(String message)
	{
		Alert dialog = new Alert(AlertType.INFORMATION);
		dialog.setHeaderText(null);
		dialog.setContentText(message);
		dialog.showAndWait();
	}

	private String getScoreByString()
	{
		String kinokoCount = String.valueOf(countSquares(squareSprites, SquareSprite.Type.Kinoko));
		String takenokoCount = String.valueOf(countSquares(squareSprites, SquareSprite.Type.Takenoko));
		return "キノコ - タケノコ\n" + kinokoCount + " - " + takenokoCount;
	}

	private void moveCanvas(Canvas canvas, double x, double y)
	{
		canvas.setTranslateX(x);
		canvas.setTranslateY(y);
	}

	private double colToX(int col)
	{
		return col * GameSettings.SQUARE_WIDTH;
	}

	private double rowToY(int row)
	{
		return row * GameSettings.SQUARE_HEIGHT;
	}

	private int xToCol(double x)
	{
		return (int) x / GameSettings.SQUARE_WIDTH;
	}

	private int yToRow(double y)
	{
		return (int) y / GameSettings.SQUARE_HEIGHT;
	}

	private int AlphaBeta()
	{
		return AlphaBeta(true, true, SEARCH_LEVEL, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * Implement the AlphaBeta method.
	 * 
	 * @flag boolean, if turn is AI, true.
	 * @level int, Number of steps to search.
	 * @alpha int, @beta, int
	 * 
	 * @return if root node, bestX + bestY * ROW_COUNT
	 * @return if child node, Evaluation value
	 */
	private int AlphaBeta(boolean flag, boolean computer, int level, int alpha, int beta)
	{
		// Evaluation value of node
		int value;
		// Evaluation value propagated from the child node
		int childValue;
		// A place having the maximum evaluation value obtained by the AlphaBeta method
		int bestX = 0;
		int bestY = 0;

		// If it is the end of game tree, board surface evaluation.
		// Other nodes propagate at MIN or MAX
		if (level == 0)
		{
			return valueBoard(squareSpritesForComputer, computer);
		}

		if (flag)
		{
			// For AI's turn, I want to find the maximum evaluation value, so first set the minimum value.
			value = Integer.MIN_VALUE;
		}
		else
		{
			// In the player's turn, I want to find the minimum evaluation value, so first set the maximum value.
			value = Integer.MAX_VALUE;
		}

		// Ai is Kinoko.
		// It is hard to understand because useing the ternary operator twice.
		SquareSprite.Type squareType = (computer ? flag : !flag) ? SquareSprite.Type.Kinoko : SquareSprite.Type.Takenoko;

		// If there is no place to put it, return the evaluation value.
		if (existsPuttable(squareSpritesForComputer, squareType) == false)
		{
			if(level == SEARCH_LEVEL)
			{
				System.out.println("can't put");
				return Integer.MIN_VALUE;
			}
			else
			{
				return valueBoard(squareSpritesForComputer, computer);
			}
		}

		for (int y = 0; y < ROW_COUNT; y++)
		{
			for (int x = 0; x < COL_COUNT; x++)
			{
				if (checkPuttable(squareSpritesForComputer, squareType, x, y))
				{
					// create clone (deep copy)
					SquareSprite[][] squareSpritesForComputerLast = deepCopy2DArrayForSquareSprite(squareSpritesForComputer);
					int[][] bombPowersForComputerLast = deepCopy2DArrayForInt(bombPowersForComputer);
					int[][] surroundingBombCountsForComputerLast = deepCopy2DArrayForInt(surroundingBombCountsForComputer);
					// put
					PutSquareSpriteForComputer(squareSpritesForComputer, bombPowersForComputer, surroundingBombCountsForComputer, squareType, x, y);
					// change trun (change flag)
					// Compute evaluation value of child node
					childValue = AlphaBeta(!flag, computer, level - 1, alpha, beta);
					// Compare the evaluation value of this node with the child node
					if (flag) {
						// If the node of AI selects the maximum evaluation value among the child nodes
						if (childValue > value)
						{
							value = childValue;
							// update α value
							alpha = value;
							bestX = x;
							bestY = y;
						}
						// If the current value of this node is larger than the β value inherited from the parent, 
						// branches will not be selected and will not be evaluated any further
						//  = go through for loop
						if (value > beta)
						{
							// β cut
							// Return before putting
							squareSpritesForComputer = squareSpritesForComputerLast;
							return value;
						}
					} else {
						// If the node of the player selects the smallest evaluation value among the child nodes
						if (childValue < value)
						{
							value = childValue;
							// update β value
							beta = value;
							bestX = x;
							bestY = y;
						}
						// If the current value of this node is smaller than the α value inherited from the parent, 
						// branches will not be selected and will not be evaluated any further
						//  = go through for loop
						if (value < alpha)
						{
							// α cut
							// Return before putting
							squareSpritesForComputer = squareSpritesForComputerLast;
							bombPowersForComputer = bombPowersForComputerLast;
							surroundingBombCountsForComputer = surroundingBombCountsForComputerLast;
							return value;
						}
					}
					// Return before putting
					squareSpritesForComputer = squareSpritesForComputerLast;
				}
			}
		}

		if (level == SEARCH_LEVEL)
		{
			// If it is the root node, it returns the place with the maximum evaluation value
			return bestX + bestY * ROW_COUNT;
		}
		else
		{
			// If it is a child node, return the evaluation value of the node
			return value;
		}
	}

	/**
	 * Evaluation function. 
	 * Evaluate the board surface and return the evaluation value. 
	 * Based on the value of the place of the board surface.
	 * 
	 * @return int. Evaluation value.
	 */
	private int valueBoard(SquareSprite[][] squareSprites, boolean computer)
	{
		int value = 0;

		for (int y = 0; y < ROW_COUNT; y++)
		{
			for (int x = 0; x < COL_COUNT; x++)
			{
				// Place the stone placed and the place's value on it.
				if (squareSprites[y][x].getType() == SquareSprite.Type.Kinoko)
				{
					value += VALUE_OF_PLACE[y][x];
				}
				else if (squareSprites[y][x].getType() == SquareSprite.Type.Takenoko)
				{
					value -= VALUE_OF_PLACE[y][x];
				}
			}
		}

		return (computer ? value : -value);
	}

	private void PutSquareSpriteForComputer(SquareSprite[][] squareSprites, int[][] bombPowers, int[][] surroundingBombCounts, SquareSprite.Type squareType, int row, int col)
	{
		putSquareSprite(squareSprites, squareType, row, col);
		//flipSquares(playerType, row, col);

		if(bombPowers[row][col] > 0)
		{
			explodeBomb(squareSprites, bombPowers, surroundingBombCounts, row, col, false);
		}
		else
		{
			flipSquares(squareSprites, squareType, row, col);
		}
	}

	private int[][] deepCopy2DArrayForInt(int[][] from)
	{
		int[][] ints = new int[from.length][from[0].length];

		for(int i = 0; i < from.length; ++i)
		{
			for(int j = 0; j < from[0].length; ++j)
			{
				ints[i][j] = from[i][j];
			}
		}

		return ints;
	}

	private SquareSprite[][] deepCopy2DArrayForSquareSprite(SquareSprite[][] from)
	{
		SquareSprite[][] squareSprites = new SquareSprite[from.length][from[0].length];	

		for(int i = 0; i < from.length; ++i)
		{
			for(int j = 0; j < from[0].length; ++j)
			{
				squareSprites[i][j] = from[i][j];
			}
		}

		return squareSprites;
	}
}
