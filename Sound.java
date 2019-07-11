import java.io.File;
import javafx.scene.media.Media;
import java.net.MalformedURLException;
import javafx.scene.media.AudioClip;

import java.util.HashMap;

public class Sound
{
	enum Type
	{
		MenuBgm,
		GameBgm,
		ScoreBgm,
		StartButton,
		Putting,
		Explosion
	}

	enum PlayMode
	{
		OneShot,
		Loop
	}

	private static HashMap<Type, AudioClip> audioClips = new HashMap<Type, AudioClip>();

	Sound()
	{
		loadSound(Type.MenuBgm, PlayMode.Loop, "sounds/menu_bgm.wav");
		loadSound(Type.GameBgm, PlayMode.Loop, "sounds/game_bgm.wav");
		loadSound(Type.ScoreBgm, PlayMode.Loop, "sounds/score_bgm.wav");
		loadSound(Type.StartButton, PlayMode.OneShot, "sounds/start_button.wav");
		loadSound(Type.Putting, PlayMode.OneShot, "sounds/putting.wav");
		loadSound(Type.Explosion, PlayMode.OneShot, "sounds/explosion.wav");
	}

	private void loadSound(Type type, PlayMode playMode, String filePath)
	{
		if(!audioClips.containsKey(type))
		{
			audioClips.put(type, new AudioClip(new File(filePath).toURI().toString()));
			setPlayMode(type, playMode);
			System.out.println("Loaded '" + filePath + "'");
		}
	}

	private void setPlayMode(Type type, PlayMode playMode)
	{
		switch(playMode)
		{
			case OneShot:
				setCycleCount(type, 1);
				break;
			case Loop:
				setCycleCount(type, AudioClip.INDEFINITE);
				break;
		}
	}

	private void setCycleCount(Type type, int count)
	{
		audioClips.get(type).setCycleCount(count);
	}

	public void play(Type type)
	{
		stop(type);
		audioClips.get(type).play();
	}

	public void stop(Type type)
	{
		audioClips.get(type).stop();
	}
}
