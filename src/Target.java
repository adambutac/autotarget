package adam;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Rectangle;

import Jama.Matrix;

public class Target implements Point2d {
	private ArrayList<Target> previousPosition = new ArrayList<Target>();
	private ArrayList<Target> predictedPosition  = new ArrayList<Target>();
	private Rectangle boundingPerimeter = new Rectangle();
	private final int STATIC_MOVEMENT = 1;
	private float direction;
	private float[] velocity = new float[2];
	private float[] acceleration = new float[2];
	private float x;
	private float y;
	private long timestamp;
	
	public Target(Target t){
		x = t.x;
		y = t.y;
		boundingPerimeter = t.boundingPerimeter;
		timestamp = t.timestamp;
		direction = t.direction;
		velocity[0] = t.velocity[0];
		velocity[1] = t.velocity[1];
		acceleration[0] = t.acceleration[0];
		acceleration[1] = t.acceleration[1];
	}
	
	public Target(float xIn, float yIn, Rectangle p){
		x = xIn;
		y = yIn;
		boundingPerimeter = p;
		timestamp = System.nanoTime();
		calculateDirection();
		calculateVelocity();
		calculateAcceleration();
	}
	
	public Target(float xIn, float yIn){
		x = xIn;
		y = yIn;
		timestamp = System.nanoTime();
		calculateDirection();
		calculateVelocity();
		calculateAcceleration();
	}
	
	public void reset(){
		predictedPosition.removeAll(predictedPosition);
		previousPosition.removeAll(previousPosition);
		timestamp = System.nanoTime();
	}
	
	public void update(float newX, float newY, Rectangle r){
		previousPosition.add(new Target(this));
		x = newX;
		y = newY;
		boundingPerimeter = r;
		if(getDistance(previousPosition.get(previousPosition.size() - 1)) > STATIC_MOVEMENT)
			timestamp = System.nanoTime();
		calculateDirection();
		calculateVelocity();
		calculateAcceleration();
	}
	
	private void calculateDirection(){
		if(previousPosition.isEmpty()){
			direction = 0;
		}else{
			Target previousTarget = getPreviousTarget(0);
			direction = (float)Math.acos((x - previousTarget.getX())/getDistance(previousTarget));
		}
	}
	
	private void calculateVelocity(){
		if(previousPosition.isEmpty()){
			velocity[0] = 0;
			velocity[1] = 0;
		}else{
			Target previousTarget = getPreviousTarget(0);
			float xDiff = x - previousTarget.getX();
			float yDiff = y - previousTarget.getY();
			long timeDiff = timestamp - previousTarget.timestamp;
			if(timeDiff == 0){
				velocity[0] = 0;
				velocity[1] = 0;
			}else{
				velocity[0] = Math.abs(xDiff/timeDiff);
				velocity[1] = Math.abs(yDiff/timeDiff);
			}
		}
	}

	private void calculateAcceleration(){
		if(previousPosition.size() < 1){
			acceleration[0] = 0;
			acceleration[1] = 0;
		}else{
			Target previousTarget = getPreviousTarget(0);
			float vDiffX = velocity[0] - previousTarget.velocity[0];
			float vDiffY = velocity[1] - previousTarget.velocity[1];
			long timeDiff = timestamp - previousTarget.timestamp;
			if(timeDiff == 0){
				acceleration[0] = 0;
				acceleration[1] = 0;
			}else{
				acceleration[0] = vDiffX/timeDiff;
				acceleration[1] = vDiffY/timeDiff;
			}
		}
	}
	
	
	//if a number of predicted positions match
	//a number of previous or current positions
	//in order then it is locked
	public boolean isLocked(){
		final int lockedSize = 10;
		if(previousPosition.size() >= lockedSize){
			return true;
		}else{
			return false;
		}
	}
	
	public Target predict(long t){
		if(previousPosition.isEmpty()){
			return new Target(x,y);
		}else{
			float xPredicted = (float) (x + velocity[0]*t + 0.5*acceleration[0]*Math.pow(t,2));
			float yPredicted = (float) (y + velocity[1]*t + 0.5*acceleration[1]*Math.pow(t,2));
			//System.out.println("Coordinates:" + xPredicted + " " + yPredicted);
			//System.out.println("Velocity:" + velocity[0]*Math.pow(10, 9) + " " + velocity[1]*Math.pow(10, 9));
			//System.out.println("Acceleration:" + acceleration[0]*Math.pow(10, 9) + " " + acceleration[1]*Math.pow(10, 9));
			//System.out.println();
			predictedPosition.add(new Target(xPredicted,yPredicted));
			return predictedPosition.get(predictedPosition.size() - 1);
		}
	}
	
	public boolean isExpired(){
		if(getAgeInNanoSeconds() > 5 * Math.pow(10, 9))
			return true;
		else
			return false;
	}
	
	public long getTimestamp(){
		return timestamp;
	}
	
	public long getAgeInNanoSeconds(){
		return System.nanoTime() - timestamp;
	}
	
	public Rectangle getBounds(){
		return boundingPerimeter;
	}
	
	public Target getPreviousTarget(int indexFromEnd){
		if(previousPosition.isEmpty()){
			return null;
		}else{
			return previousPosition.get(previousPosition.size() - (1 + indexFromEnd));
		}
	}
	
	public void drawTarget(MBFImage frame){
		final Float[] TARGETCOLOR = RGBColour.CYAN;
		final Float[] PREVIOUSTARGETCOLOR = RGBColour.BLUE;
		final Float[] PREDICTEDTARGETCOLOR = RGBColour.MAGENTA;
		final Float[] ROICOLOR = RGBColour.RED;

		for(Target t:previousPosition){
			frame.drawPoint(t, PREVIOUSTARGETCOLOR, 1);
		}
		frame.drawShape(this.getBounds(), ROICOLOR);
		frame.drawPoint(this, TARGETCOLOR, 1);
		for(Target t:predictedPosition){
			frame.drawPoint(t, PREDICTEDTARGETCOLOR, 1);
		}
	}
	
	public int getDimensions() {
		// TODO Auto-generated method stub
		return 0;
	}
	public Number getOrdinate(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String asciiHeader() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void readASCII(Scanner arg0) throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	public byte[] binaryHeader() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void readBinary(DataInput arg0) throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	public void writeASCII(PrintWriter arg0) throws IOException {
		// TODO Auto-generated method stubS
		
	}
	
	public void writeBinary(DataOutput arg0) throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	public Point2d copy() {
		return new Target(x,y);
	}
	
	public void copyFrom(Point2d arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}
	
	public float getDistance(Target t){
		return (float) Math.sqrt((x-t.getX())*(x-t.getX()) + (y-t.getY())*(y-t.getY()));
	}
	
	public float getDistance(float x2, float y2) {
		return (float) Math.sqrt((x-x2)*(x-x2) + (y-y2)*(y-y2));
	}
	
	public Point2d minus(Point2d arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void setX(float arg0) {
		x = arg0;
	}
	
	public void setY(float arg0) {
		y = arg0;
	}
	
	public Point2d transform(Matrix arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void translate(Point2d arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public void translate(float arg0, float arg1) {
		// TODO Auto-generated method stub
		
	}
}
