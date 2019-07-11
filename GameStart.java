import javafx.application.Application;
import java.io.IOException;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

public class GameStart extends Application
{
	private static Stage stage;

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		stage = primaryStage;
		stage.setTitle("オセロマン");
		changeView("GameStart.fxml");
	}

	public static void main(String[] args)
	{
		launch(args);
	}

	public static Stage getStage()
	{
		return stage;
	}

	public static void changeView(String fxml)
	{
		try
		{
			stage.setScene(new Scene(FXMLLoader.load(GameStart.class.getResource(fxml))));
			stage.show();
		}
		catch (IOException ex)
		{
			System.out.println("changeView : " + fxml);
			ex.printStackTrace();
		}
	}
}
