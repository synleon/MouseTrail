package trail;


import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import math.Vector;

public class AnimatorSkeleton extends AnimationTimer{

	private static final double ONE_SECOND = 1000000000L;
	private static final double HALF_SECOND = ONE_SECOND / 2F;
	private static final int MAX_VECTORS = 360;
	private final Random RAND = new Random();

	private LinkedList< Vector> trailVectors;
	private GraphicsContext gc;
	private Vector lastVector;
	private String fpsDisplay;
	private Canvas canvas;

	private Vector lastVector2;
	private boolean clean;
	private int frameCount;
	private double hue;
	private double hueShift;
	private double lastTime;
	
	public AnimatorSkeleton( Canvas canvas){
		this.canvas = canvas;
		gc = canvas.getGraphicsContext2D();
	}

	public void start(){
		if( trailVectors == null)
			trailVectors = new LinkedList<>();
		trailVectors.clear();
		frameCount = 0;
		clean = false;
		lastTime = System.nanoTime();
		super.start();
	}

	public void clean(){
		clean = true;
	}

	public void addStartVector( double x, double y){
		clean = false;
		lastVector = new Vector( x, y);
	}

	public double rand( double min, double max){
		return min + (max - min) * RAND.nextDouble();
	}

	public double roughRand( double min, double max, double minScale, double maxScale){
		if( min > max){
			double temp = min;
			min = max;
			max = temp;
		}
		return rand( min * rand( minScale, maxScale), max * rand( minScale, maxScale));
	}

	public void clearCanvas(){
		gc.clearRect( 0, 0, canvas.getWidth(), canvas.getHeight());
	}

	public void displayFPS(){
		gc.setFont( Font.font( gc.getFont().getFamily(), FontWeight.BLACK, 24));
		gc.setStroke( Color.WHITE);
		gc.setLineWidth( 1);
		gc.strokeText( fpsDisplay, 10, 25);
	}

	public void drawPerpendicularLines( Vector middle, Vector perpendicularCenterX, Vector perpendicularCenterY){
		gc.setLineWidth( 5);
		gc.setStroke( Color.hsb( (hue += .5f) % 360, 1, 1));
		gc.strokeLine( middle.getX(), middle.getY(), perpendicularCenterX.getX(), perpendicularCenterX.getY());
		gc.strokeLine( middle.getX(), middle.getY(), perpendicularCenterY.getX(), perpendicularCenterY.getY());
	}

	public void drawCloud( Vector middle, Vector perpendicularCenterX, Vector perpendicularCenterY){
		gc.setFill( Color.hsb( (hue += .5f) % 360, 1, .7));
		for( int i = 0; i < 100; i++){
			double x = roughRand( perpendicularCenterY.getX(), perpendicularCenterX.getX(), .95, 1.05);
			double y = roughRand( perpendicularCenterY.getY(), perpendicularCenterX.getY(), .95, 1.05);
			gc.fillOval( x, y, 2, 2);
		}
	}

	public void drawingLoop(List<Vector> vectors, DrawInterface drawInterface) {
		for (Vector currentVector : vectors) {
			// 1.1 1.2 1.3 1.4
			Vector perpendicularY = currentVector.sub(lastVector).mult(4.0).perpendicularY();

			// 1.5 1.6 1.7 1.8
			Vector perpendicularX = currentVector.sub(lastVector).mult(4.0).perpendicularX();

			// 1.9 1.10 1.11
			Vector middle = lastVector.add(currentVector).mult(0.5);

			// 1.12 ~ 1.13
			Vector perpendicularCenterY = perpendicularY.add(middle);

			// 1.14 1.15
			Vector perpendicularCenterX = perpendicularX.add(middle);

			// 1.16
			drawInterface.draw(middle, perpendicularCenterX, perpendicularCenterY);

			// at the end of the loop assign currentVector to lastVector
			lastVector = currentVector;
		}

	}

	public void addVector(double x, double y) {
		if (trailVectors.size() >= MAX_VECTORS) {
			removeFirst(1);
		}
		trailVectors.addLast(new Vector(x, y));
	}

	public void removeFirst(int count) {
		for (int i = 0; i < count && !trailVectors.isEmpty(); i++) {
			trailVectors.removeFirst();
		}
	}

	/**
	 * increment hueShift then store hueShift modules 360 in hue.
	 */
	public void resetColorAndVector() {
		if (!trailVectors.isEmpty()) {
			lastVector = trailVectors.removeFirst();
		}
	}

	public void calculateFPS(long now) {
		if ((now - lastTime) > HALF_SECOND) {
			fpsDisplay = Integer.toString(frameCount * 2);
			lastTime = now;
			frameCount = 0;
		}
		frameCount++;
	}

	public void shortenTrail(int count) {
		if (clean && !trailVectors.isEmpty()) {
			removeFirst(count);
		}
	}

	public void handle(long now) {
		if (lastVector != null) {
			// 1.1
			calculateFPS(now);
			// 1.2
			clearCanvas();
			// 1.3
			shortenTrail(5);
			// 1.4
			drawingLoop(trailVectors, (m, px, py) ->drawCloud(m, px, py));
			// 1.5
			resetColorAndVector();
			// 1.6
			drawingLoop(trailVectors, (m, px, py) ->drawPerpendicularLines(m, px, py));
			// 1.7
			resetColorAndVector();;
			// 1.8
			displayFPS();
		}
	}
}