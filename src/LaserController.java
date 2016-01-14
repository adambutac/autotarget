package adam;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.openimaj.video.capture.VideoCaptureException;

/**
 * @author senketsu
 *The laser controller is a class that supplies access
 *to the laser. It also serves as the middle man between the
 *user/tracker and the audio servo controller.
 */
public class LaserController{
	/*allows servo control using PWM through the audio port*/
	private AudioServoController servoController;
	private final double ANGLE_OF_VIEW = 75;
	private final float PIXEL_SIZE; //number of mm in a pixel
	private final int SCREEN_HEIGHT_IN_PIXELS;
	private final int SCREEN_WIDTH_IN_PIXELS;
	private final int CAMERALENSE_LASERHEAD_DISTANCE = 45;
	private final int DECIMAL_CONVERSION = (int) Math.pow(10, 1);
	private double pan;
	private double tilt;
	private float x;
	private float y;
	private int distance;
	private int degrees;
	
	public LaserController(int width, int height){
		servoController = new AudioServoController();
		distance = 1000;
		SCREEN_WIDTH_IN_PIXELS = width;
		SCREEN_HEIGHT_IN_PIXELS = height;
		PIXEL_SIZE = (float)Math.tan(ANGLE_OF_VIEW/2*Math.PI/180)*distance/(SCREEN_WIDTH_IN_PIXELS/2);
		degrees = 75;
		pan = degrees/2.0;
		tilt = degrees/2.0;
		servoController.buildLeftServo(degrees*DECIMAL_CONVERSION, .7, 2.4);
		servoController.buildRightServo(degrees*DECIMAL_CONVERSION, .7, 2.4);
		setPan(pan);
		setTilt(tilt);
	}
	
	private JPanel createAnglePanel(){
		JPanel anglePanel = new JPanel();
		final JTextField panTextBox = new JTextField(pan + "");
		final JTextField tiltTextBox = new JTextField(tilt + "");
		
		panTextBox.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();
				double value = 0;
				String text = panTextBox.getText();
				try{
					value = Double.parseDouble(text);	
					switch(c){
						case KeyEvent.VK_ENTER:
							setPan(value);
							setTilt(Double.parseDouble(tiltTextBox.getText()));
							break;
						case KeyEvent.VK_UP:
							pan = value + 1;
							panTextBox.setText(pan + "");
							setPan(pan);
							break;
						case KeyEvent.VK_DOWN:
							pan = value - 1;
							panTextBox.setText(pan + "");
							setPan(pan);
							break;
						case KeyEvent.VK_LEFT:
							break;
						case KeyEvent.VK_RIGHT:
							break;
						default:
								panTextBox.setText(text);
								break;
					}
				}catch(NumberFormatException exception){
					if(text.equals("")){
						panTextBox.setText(text);
					}else{
						panTextBox.setText(tilt + "");
					}
					System.out.println(exception.getMessage());
				}
			}
			public void keyTyped(KeyEvent e) {}
		});
		tiltTextBox.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();
				double value = 0;
				String text = tiltTextBox.getText();
				try{
					value = Double.parseDouble(text);	
					switch(c){
						case KeyEvent.VK_ENTER:
							setPan(Double.parseDouble(panTextBox.getText()));
							setTilt(value);
							break;
						case KeyEvent.VK_UP:
							tilt = value + 1;
							tiltTextBox.setText(tilt + "");
							setPan(tilt);
							break;
						case KeyEvent.VK_DOWN:
							tilt = value - 1;
							tiltTextBox.setText(tilt + "");
							setPan(tilt);
							break;
						case KeyEvent.VK_LEFT:
							break;
						case KeyEvent.VK_RIGHT:
							break;
						default:
							tiltTextBox.setText(text);
							break;
					}
				}catch(NumberFormatException exception){
					if(text.equals("")){
						tiltTextBox.setText(text);
					}else{
						tiltTextBox.setText(tilt + "");
					}
					System.out.println(exception.getMessage());
				}
			}
			public void keyTyped(KeyEvent e) {}
		});
		
		anglePanel.add(panTextBox);
		anglePanel.add(tiltTextBox);
		return anglePanel;
	}
	
	private JPanel createCartesianPanel(){
		JPanel cartesianPanel = new JPanel();
		final JTextField xInput = new JTextField(SCREEN_WIDTH_IN_PIXELS/2 + "",5);
		final JTextField yInput = new JTextField(SCREEN_HEIGHT_IN_PIXELS/2 + "",5);
		
		xInput.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();
				Float value = 0f;
				String text = xInput.getText();
				try{
					value = Float.parseFloat(text);
					switch(c){
						case KeyEvent.VK_ENTER:
							move(Float.parseFloat(xInput.getText()),Float.parseFloat(yInput.getText()));
							break;
						case KeyEvent.VK_UP:
							value += 1;
							x = value*PIXEL_SIZE;
							xInput.setText(value + "");
							move(x/PIXEL_SIZE,Float.parseFloat(yInput.getText()));
							break;
						case KeyEvent.VK_DOWN:
							value -= 1;
							x = value*PIXEL_SIZE;
							xInput.setText(value + "");
							move(value,Float.parseFloat(yInput.getText()));
							break;
						case KeyEvent.VK_LEFT:
							break;
						case KeyEvent.VK_RIGHT:
							break;
						default:
							xInput.setText(text);
							break;
					}
				}catch(NumberFormatException exception){
					if(text.equals("")){
						xInput.setText(text);
					}else{
						xInput.setText(x/PIXEL_SIZE + "");
					}
					System.out.println(exception.getMessage());
				}
			}
			public void keyTyped(KeyEvent e) {}
		});
		
		yInput.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent e){}
			public void keyReleased(KeyEvent e){
				int c = e.getKeyCode();
				float value = 0;
				String text = yInput.getText();
				try{
					value = Float.parseFloat(text);
					switch(c){
						case KeyEvent.VK_ENTER:
							move(Float.parseFloat(xInput.getText()),Float.parseFloat(yInput.getText()));
							break;
						case KeyEvent.VK_UP:
							value += 1;
							y = value*PIXEL_SIZE;
							yInput.setText(value + "");
							move(Float.parseFloat(xInput.getText()),value);
							break;
						case KeyEvent.VK_DOWN:
							value -= 1;
							y = value*PIXEL_SIZE;
							yInput.setText(value + "");
							move(Float.parseFloat(xInput.getText()),value);
							break;
						case KeyEvent.VK_LEFT:
							break;
						case KeyEvent.VK_RIGHT:
							break;
						default:
							yInput.setText(text);
							break;
					}
				}catch(NumberFormatException exception){
					if(text.equals("")){
						yInput.setText(text);
					}else{
						yInput.setText(y/PIXEL_SIZE + "");
					}
					System.out.println(exception.getMessage());
				}
			}
			public void keyTyped(KeyEvent e){}
		});
		cartesianPanel.add(xInput);
		cartesianPanel.add(yInput);
		return cartesianPanel;
	}
	
	public JPanel createGUI(){
		JPanel superPanel = new JPanel();
		superPanel.setLayout(new BoxLayout(superPanel,BoxLayout.Y_AXIS));
		superPanel.add(createAnglePanel());
		superPanel.add(createCartesianPanel());
		return superPanel;
	}
	
	public void setPan(double degree){
		System.out.println("PAN" + degree);
		if(degree > degrees){
			servoController.setRightServo(degrees*DECIMAL_CONVERSION);
		}else if(degree < 0){
			servoController.setRightServo(0);
		}else{
			degree = Math.round(degree*DECIMAL_CONVERSION);
			servoController.setRightServo((int) degree);
		}
	}
	
	public void setTilt(double degree){
		System.out.println("TILT" + degree);
		if(degree > degrees){
			servoController.setLeftServo(degrees*DECIMAL_CONVERSION);
		}else if(degree < 0){
			servoController.setLeftServo(0);
		}else{
			degree = Math.round(degree*DECIMAL_CONVERSION);
			servoController.setLeftServo((int) degree);
		}
	}
	
	public void move(float pixelX, float pixelY){
		x = ((pixelX-160)*PIXEL_SIZE) + CAMERALENSE_LASERHEAD_DISTANCE;
		y = ((pixelY-120)*PIXEL_SIZE);
		pan = Math.atan(x/distance)*180/Math.PI;
		tilt = Math.atan(y/distance)*180/Math.PI;
		setPan(pan + degrees/2);
		setTilt(degrees/2 - tilt);
	}
	
	public void start(){
		(new Thread(servoController)).start();
	}
	
	public void stop() throws LineUnavailableException{
		servoController.stop();
	}
	
	public static void main(String[] args) throws LineUnavailableException, InterruptedException, VideoCaptureException{
		JFrame frame = new JFrame("Laser Control");
		LaserController lc = new LaserController(320,240);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(),BoxLayout.X_AXIS));;
  		//frame.getContentPane().add(outputPanel());
  		frame.getContentPane().add(lc.createGUI());
  		frame.pack();
  		frame.setVisible(true);
	}
}
