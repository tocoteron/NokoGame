import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class ScoreController
{
	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private Button Continue;
	@FXML
	private Button Quit;

	@FXML
	private Label kinoko_score_label;

	@FXML
	private Label takenoko_score_label;

	@FXML
	private ImageView kinoko_winner;

	@FXML
	private ImageView takenoko_winner;

	@FXML
	void ContinueButtonAction(Event event)
	{
		SoundPlayer.play(Sound.Type.StartButton);
		SoundPlayer.stop(Sound.Type.ScoreBgm);
		GameStart.changeView("GameStart.fxml");
	}

	@FXML
	void QuitButtonAction(Event event)
	{
		SoundPlayer.play(Sound.Type.StartButton);
		System.exit(0);
	}

	@FXML
	void initialize()
	{
		int kino = GameSettings.KINOKO_SCORE;
		int take = GameSettings.TAKENOKO_SCORE;

		kinoko_score_label.setText("" + kino);
		takenoko_score_label.setText("" + take);

		if(kino > take)
		{
			takenoko_winner.setVisible(false);
		}
		else
		{
			kinoko_winner.setVisible(false);
		}

		SoundPlayer.play(Sound.Type.ScoreBgm);
	}
}
