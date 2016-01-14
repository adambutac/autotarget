package adam;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCaptureException;


public class TrackerGUI{
	
	private JTextArea output;
	private JFrame frame;
	private Camera camera;
	private LaserController laserController;
	private MotionDetector motionDetector;
	private Tracker tracker;
	
	public TrackerGUI() throws AWTException, IOException, LineUnavailableException{
		camera = new Camera();
		laserController = new LaserController(320,240);
		motionDetector = new MotionDetector();
		tracker = null;
		output = new JTextArea();
		frame = new JFrame("Adam's Tracker");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.setBounds(400, 100, 0, 0);
  		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(),BoxLayout.X_AXIS));;
  		frame.getContentPane().add(videoPanel());
  		//frame.getContentPane().add(outputPanel());
  		frame.getContentPane().add(controlPanel());
  		frame.pack();
  		frame.setVisible(true);
  	    frame.addWindowListener(new WindowAdapter() {
  	        public void windowClosing(WindowEvent evt) {
  	            // Exit the application
  	        	try {
					laserController.stop();
				} catch (LineUnavailableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
  	            System.exit(0);
  	        }
  	    });
  	}

	private JPanel videoPanel(){
		final JPanel superPanel = new JPanel();
		superPanel.setLayout(new BoxLayout(superPanel,BoxLayout.Y_AXIS));
		final JPanel videoPanel = new JPanel();
		final CameraList cameraList = new CameraList(videoPanel);
		JPanel buttonPanel = new JPanel();
		for(Camera c:cameraList){
			JButton b = new JButton(c.getName());
			buttonPanel.add(b);
			b.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					try {
						String name = ((JButton) evt.getSource()).getText();
						output.append("Starting capture on " + name + "\n");
						Camera currentCamera = cameraList.getCamera();
						Camera newCamera = cameraList.getCamera(name);
						if(newCamera != null){
							if(currentCamera != null){
								currentCamera.stop();
							}
							camera = newCamera;
							System.out.println(camera.getName());
							camera.start();
							frame.pack();
							camera.getVideoDisplay().addVideoListener(
							new VideoDisplayListener<MBFImage>(){
								public void beforeUpdate(MBFImage frame){
									int x = frame.getWidth();
									int y = frame.getHeight();
								  frame.drawLine(x/2, 0, x/2, y, RGBColour.BLACK);
								  frame.drawLine(0, y/2, x, y/2, RGBColour.BLACK);
								}
								public void afterUpdate(VideoDisplay<MBFImage> arg0) {
									// TODO Auto-generated method stub	
								}
								});
							camera.getVideoDisplay().getScreen().addMouseMotionListener(new MouseMotionListener(){
								public void mouseDragged(MouseEvent arg0) {
									if(tracker == null && camera.isRunning()){
										laserController.move(arg0.getX(), arg0.getY());
									}
								}
								public void mouseMoved(MouseEvent arg0) {
									if(tracker == null && camera.isRunning()){
										laserController.move(arg0.getX(), arg0.getY());
									}
								}
							});
						}else{
							output.append("Camera " + name + " could not be found.");
						}
					} catch (VideoCaptureException e) {
						output.append("Camera " + camera.getName() + " could not be started.\n");
						output.append(e.getMessage());
						e.printStackTrace();
					} catch (InterruptedException e) {
						output.append("Camera " + camera.getName() + " cannot be stopped as it is not running.\n");
					}
				}	
			});
		}
		videoPanel.setPreferredSize(new Dimension(320,240));
		//videoPanel.setBackground(Color.BLACK);
		superPanel.add(videoPanel);
		//superPanel.add(Box.createRigidArea(new Dimension(0,5)));
		superPanel.add(buttonPanel);
		return superPanel;
	}

	private JPanel controlPanel(){
		JPanel superPanel = new JPanel();
		superPanel.setLayout(new BoxLayout(superPanel,BoxLayout.Y_AXIS));
		JPanel buttonPanel = new JPanel();
		JPanel trackerPanel = new JPanel();
		JPanel servoPanel = laserController.createGUI();
		JPanel filterPanel = motionDetector.createGUI();
		JButton startButton = new JButton("Start");
		JButton stopButton = new JButton("Stop");
		
		startButton.addActionListener(new ActionListener() {	 
            public void actionPerformed(ActionEvent e)
            {
            	if(camera.isRunning()){
            		tracker = new Tracker(camera,laserController,motionDetector);
					tracker.start();
					output.append("Tracking enabled!\n");
            	}else{
            		output.append("Tracker camera was not enabled.");
            	}
            }
        }); 
		
        stopButton.addActionListener(new ActionListener() { 	 
            public void actionPerformed(ActionEvent e)
            {
                try {
                	output.append("Tracking disbled.\n");
                	if(camera.isRunning()){
                		tracker = null;
                		camera.stop();
                		camera.start();
                	}
					frame.pack();
				} catch (VideoCaptureException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
        }); 
		buttonPanel.add(startButton);
		buttonPanel.add(stopButton);
		trackerPanel.add(servoPanel);
		trackerPanel.add(filterPanel);
		superPanel.add(trackerPanel);
		superPanel.add(buttonPanel);
		return superPanel;
	}
	
	@SuppressWarnings("unused")
	private JPanel outputPanel(){
		JPanel outputPanel = new JPanel();
		output = new JTextArea();
		JScrollPane outputSP = new JScrollPane(output, 
												ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
												ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		outputSP.setPreferredSize(new Dimension(256,256));
		outputPanel.add(outputSP);
		return outputPanel;
	}

	public static void main(String[] args) throws AWTException, IOException, LineUnavailableException{
	  	@SuppressWarnings("unused")
			TrackerGUI gui = new TrackerGUI();
	}
}
