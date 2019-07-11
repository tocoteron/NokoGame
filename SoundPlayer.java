public class SoundPlayer
{
	private static Sound sound = new Sound();

	public static void play(Sound.Type soundType)
	{
		sound.play(soundType);
	}

	public static void stop(Sound.Type soundType)
	{
		sound.stop(soundType);
	}
}
