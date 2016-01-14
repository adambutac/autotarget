package adam;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.connectedcomponent.ConnectedComponentLabeler;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;


/**
 * 
 * @author senketsu
 *the basic idea of the MotionDetector class is it compares
 *two images and finds the differences between them, returning 
 *the differences as a list of targets. It accomplishes this by
 *taking an initial image (what I refer to as the background or mask image) 
 *and subtracts it from the current image, using filter(MBFImage).
 *
 *Notes:
 *when a target is moving quickly it shrinks in size
 *so it may be a good idea to be able to detect smaller
 *targets when a target is found and subimages are being used
 */

public class MotionDetector{
	/*cclabeler is used to find connected components
	 * with the convenient findComponents()*/
	private ConnectedComponentLabeler ccLabeler;
	/*the background image
	 * this image does not get edited in any way
	 * it should only be replaced/refreshed with 
	 * a new background image*/
	private MBFImage background;
	/*mask is the image to subtract from a given
	 * current frame to locate differences*/
	private MBFImage mask;
	/*the max value of a pixel allowed in a filtered image
	 * this allows background noise to be greatly reduced*/
	private float threshold;
	/*the minimum area of a target
	 * anything that is smaller than this area will not be
	 * considered as a target and will be ignored as noise*/
	private int minArea;
	/*the maximum area of a target
	 * anything larger than this will not be considered a
	 * target we are interested in.*/
	private int maxArea;
	
	
	/*Default constructor method.
	 * WARNING! Don't forget to initialize the background/mask 
	 * using initialize() before trying to use any other methods
	 * other than createGUI()
	 * */
	public MotionDetector(){
		ccLabeler = new ConnectedComponentLabeler(ConnectedComponentLabeler.Algorithm.FLOOD_FILL,ConnectedComponent.ConnectMode.CONNECT_4);
		background = null;
		mask = null;
		threshold = 0.15f;
		minArea = 20;
		maxArea = 50;
	}
	
	/*Constructor with background and mask initialization included
	 * Unfortunately, if you use this method and already created the
	 * GUI you've probably replaced the value of the pointer to your MotionDetector
	 * object. Consequently the GUI will not work.
	 * */
	public MotionDetector(MBFImage frame){
		ccLabeler = new ConnectedComponentLabeler(ConnectedComponentLabeler.Algorithm.FLOOD_FILL,ConnectedComponent.ConnectMode.CONNECT_4);
		background = frame.clone();
		mask = frame.clone();
		threshold = 0.15f;
		minArea = 20;
		maxArea = 50;
	}
	
	/*Creates a GUI JPanel used to change the values of 
	 * threshold,minArea,maxArea using sliders
	 * */
	public JPanel createGUI(){

		JPanel filterPanel = new JPanel();
		JSlider thresholdSlider = new JSlider(SwingConstants.VERTICAL,
		        0, 100, (int) (threshold*100));
		JSlider minAreaSlider = new JSlider(SwingConstants.VERTICAL,
		        0, 100, minArea);
		JSlider maxAreaSlider = new JSlider(SwingConstants.VERTICAL,
		        0, 100, maxArea);
		
		thresholdSlider.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				if (!source.getValueIsAdjusting()) {
					threshold = source.getValue()/100f;
				}
			}
		});
		minAreaSlider.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				if (!source.getValueIsAdjusting()) {
					minArea = source.getValue();
				}
			}
		});
		maxAreaSlider.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				if (!source.getValueIsAdjusting()) {
					maxArea = source.getValue();
				}
			}
		});
		
		thresholdSlider.setMajorTickSpacing(10);
		thresholdSlider.setMinorTickSpacing(1);
		thresholdSlider.setPaintTicks(true);
		thresholdSlider.setPaintLabels(true);
		minAreaSlider.setMajorTickSpacing(10);
		minAreaSlider.setMinorTickSpacing(1);
		minAreaSlider.setPaintTicks(true);
		minAreaSlider.setPaintLabels(true);
		
		maxAreaSlider.setMajorTickSpacing(10);
		maxAreaSlider.setMinorTickSpacing(1);
		maxAreaSlider.setPaintTicks(true);
		maxAreaSlider.setPaintLabels(true);
		
		filterPanel.add(thresholdSlider);
		filterPanel.add(minAreaSlider);
		filterPanel.add(maxAreaSlider);
		return filterPanel;
	
	}
	
	/*initializes the values for background and mask
	 * Use this method when using the default constructor.
	 * */
	public void initialize(MBFImage frame){
		background = frame.clone();
		mask = frame.clone();
	}
	
	
	/*resets the mask to the original background
	 * */
	public MBFImage resetBackgroundMask(){
		mask = background.clone();
		return mask;
	}
	
	/*creates a new mask from the given frame
	 * */
	public MBFImage createBackgroundMask(MBFImage frame){
		mask = frame.clone();
		return mask;
	}
	
	/*updates/edits an area in the current mask
	 * this can be used for removing targets that were moving
	 * but now are static. 
	 * */
	public MBFImage createBackgroundSubMask(MBFImage frame, Rectangle r){
		mask.drawImage(frame.clone().extractROI(r), Math.round(r.x), Math.round(r.y));
		//DisplayUtilities.displayName(mask, "mask ROI");
		return mask;
	}
	
	/*returns a filtered image that shows only the differences between
	 * frame and mask.
	 * */
	private MBFImage filter(MBFImage frame){
		MBFImage image = frame.clone();
		image = mask.subtract(image).threshold(threshold);
		//DisplayUtilities.displayName(image, "motion detection");
		return image;
	}
	
	/*this is the same as filter(MBFImage) except instead of the
	 * entire image, a subimage of frame is filtered with an equal area and location 
	 * in the mask image is filtered and returned.
	 * */
	private MBFImage filterSubImage(MBFImage frame, Rectangle r){
		MBFImage image = frame.clone();
		image = mask.extractROI(r).subtract(image.extractROI(r)).threshold(threshold);
		//DisplayUtilities.displayName(image, "motion detection");
		return image;
	}
	
	/*filters an image and returns the resulting connected differences
	 * of the image as a list of targets.
	 * */
	public List<Target> detectTargets(MBFImage image){
		image = filter(image);
		List<ConnectedComponent> components = ccLabeler.findComponents(image.flatten());
		List<Target> targets = new ArrayList<Target>();
		int targetArea = 0;
		for (ConnectedComponent comp : components) {
			targetArea = comp.calculateArea();
		    if (targetArea < minArea || targetArea > maxArea) {
		        continue;
		    }else{
		    	targets.add(new Target(comp.calculateCentroidPixel().x,
		    						   comp.calculateCentroidPixel().y,
								       comp.calculateRegularBoundingBox()));
		    }
		}
		return targets;
	}
	
	/*does the same as above except uses a subimage
	 * */
	public List<Target> detectSubTargets(MBFImage image, Rectangle r){
		image = filterSubImage(image,r);
		List<ConnectedComponent> components = ccLabeler.findComponents(image.flatten());
		List<Target> targets = new ArrayList<Target>();
		int targetArea = 0;
		for (ConnectedComponent comp : components) {
			targetArea = comp.calculateArea();
		    if (targetArea < minArea || targetArea > maxArea) {
		        continue;
		    }else{
		    	targets.add(new Target(comp.calculateCentroidPixel().y,
		    						   comp.calculateCentroidPixel().y,
								       comp.calculateRegularBoundingBox()));
		    }
		}
		return targets;
	}
	
	public static void main(String[] args) throws VideoCaptureException, InterruptedException{
		Camera c = new Camera(VideoCapture.getVideoDevices().get(0),new JPanel());
		c.start();
		MotionDetector md = new MotionDetector(c.getVideoDisplay().getVideo().getNextFrame());
		long start = System.currentTimeMillis();
		long stop = start + 100 * 1000;
		MBFImage image = null;
		while(System.currentTimeMillis() < stop){
			if(c.getVideoDisplay().getVideo().hasNextFrame())
				image = c.getVideoDisplay().getVideo().getNextFrame();
				DisplayUtilities.displayName(md.filter(image).flatten(), "motion detection");
		}
		c.stop();
		System.exit(0);
	}

}
