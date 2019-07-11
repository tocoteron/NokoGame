import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

public class GameStartController
{
	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private Button Start;

	@FXML
	private ComboBox<String> SelectMapsize;

	@FXML
	void initialize()
	{
		assert Start != null : "fx:id=\"GameStart1\" was not injected: check your FXML file 'GameStart.fxml'.";
		SoundPlayer.play(Sound.Type.MenuBgm);
	}

	@FXML
	public void initialize(URL location, ResourceBundle resources)
	{
		SelectMapsize.getSelectionModel().select(0);
	}

	@FXML
	void GameStart1ButtonAction(Event event)
	{
		GameSettings.GAME_MODE = 1;
		startGame();
	}

	@FXML
	void GameStart2ButtonAction(ActionEvent event)
	{
		GameSettings.GAME_MODE = 2;
		startGame();
	}

	boolean startGame()
	{
		String mapSizeStr = SelectMapsize.getSelectionModel().getSelectedItem();

		if(mapSizeStr == null)
		{
			return false;
		}

		System.out.println(mapSizeStr);

		String[] splittedMapSizeStr = mapSizeStr.split(" ", 3);
		GameSettings.ROW_COUNT = Integer.parseInt(splittedMapSizeStr[0]);
		GameSettings.COL_COUNT = Integer.parseInt(splittedMapSizeStr[2]);

		SoundPlayer.stop(Sound.Type.MenuBgm);
		SoundPlayer.play(Sound.Type.StartButton);

		new Game().start(GameStart.getStage());

		return true;
	}
}
