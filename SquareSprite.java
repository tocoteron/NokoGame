public class SquareSprite extends Sprite
{
	public enum Type
	{
		None,
		Kinoko,
		Takenoko
	}

	private final Type type;

	SquareSprite(Type type, String filePath, double x, double y)
	{
		super(filePath, x, y);
		this.type = type;
	}

	public Type getType()
	{
		return type;
	}
}
