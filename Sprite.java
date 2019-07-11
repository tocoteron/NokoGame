import javafx.scene.image.Image;
import javafx.scene.canvas.GraphicsContext;

import java.util.HashMap;

public class Sprite
{
	//private GraphicsContext graphicsContext;
	private static HashMap<String, Image> images = new HashMap<String, Image>();

	private Image image;
	private double width;
	private double height;

	private double positionX;
	private double positionY;
	private double velocityX;
	private double velocityY;
	private double accelerationX;
	private double accelerationY;

	public Sprite(String filePath, double x, double y)
	{
		setImage(filePath);
		positionX = x;
		positionY = y;
		velocityX = 0;
		velocityY = 0;
		accelerationX = 0;
		accelerationY = 0;
	}

	public void setImage(String filePath)
	{
		if(!images.containsKey(filePath))
		{
			images.put(filePath, new Image(filePath));
			System.out.println("Loaded '" + filePath + "'");
		}

		image = images.get(filePath);
		width = image.getWidth();
		height = image.getHeight();
	}

	public double getWidth()
	{
		return width;
	}

	public double getHeight()
	{
		return height;
	}

	public double getPositionX()
	{
		return positionX;
	}

	public double getPositionY()
	{
		return positionY;
	}

	public double getVelocityX()
	{
		return velocityX;
	}

	public double getVelocityY()
	{
		return velocityY;
	}

	public double getAccelerationX()
	{
		return accelerationX;
	}

	public double getAccelerationY()
	{
		return accelerationY;
	}

	public void setPosition(double x, double y)
	{
		positionX = x;
		positionY = y;
	}

	public void addPosition(double x, double y)
	{
		positionX += x;
		positionY += y;
	}

	public void setVelocity(double vx, double vy)
	{
		velocityX = vx;
		velocityY = vy;
	}

	public void addVelocity(double vx, double vy)
	{
		velocityX += vx;
		velocityY += vy;
	}

	public void setForce(double forceX, double forceY)
	{
		accelerationX = forceX;
		accelerationY = forceY;
	}

	public void addForce(double forceX, double forceY)
	{
		accelerationX += forceX;
		accelerationY += forceY;
	}

	public void update(double deltaTime)
	{
		addPosition(velocityX * deltaTime, velocityY * deltaTime);
		addVelocity(accelerationX * deltaTime, accelerationY * deltaTime);
	}

	public void draw(GraphicsContext graphicsContext)
	{
		graphicsContext.drawImage(image, positionX, positionY);
	}
}
