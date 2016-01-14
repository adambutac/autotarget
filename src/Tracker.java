package adam;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.LineUnavailableException;
import org.openimaj.image.MBFImage;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;

public class Tracker{
	/**
	 * TODO 
	 * increase accuracy of predicted target by including angles
	 * remove the possibility that two targets can become the same
	 * 
	 */
	
	/**
	 * trackerCamera: the camera assigned to this tracker
	 * previousTarget: the circle targeted in the previous frame
	 * currentTarget: the circle targeted in the current frame
	 * hc: the HoughCircles object to detect circles in a frame
	 * roi: the subframe containing our region of interest
	 * previousFrame: the 
	 */
	private Camera trackerCamera;
	private LaserController laserController;
	private MotionDetector motionDetector;
	private Rectangle rectangle;
	
	public Tracker() throws LineUnavailableException{
		trackerCamera = null;
		laserController = null;
		motionDetector = null;
		rectangle = null;
	}
	
	public Tracker(Camera cam,LaserController lc, MotionDetector md){
		trackerCamera = cam;
		laserController = lc;
		motionDetector = md;
		rectangle  = new Rectangle();
	}
	
	private int NUM_OF_TARGETS = 1;
	private ArrayList<Target> targetList = new ArrayList<Target>();
	
	public void start(){
		/**
		 * constant values of the tracker
		 * TOLERANCE: the maximum distance the previous target can be
		 * 			from the current target before it is considered lost
		 * 			or a different/new target.
		 * NUMOFBESTCIRCLES: the maximum number of circles to look for in
		 * 					a given ROI (region of interest)
		 ic void start() throws VideoCaptureException{
		/* BACKGROUNDREFRESHRATE: the number of frames to skip before refreshing
		 * 					the background mask.
		 * TARGETCOLOR,PREVIOUSTARGETCOLOR,
		 * PREDICTEDTARGETCOLOR,ROICOLOR: color assignments for gui
		 */
		motionDetector.initialize(trackerCamera.getVideoDisplay().getVideo().getNextFrame());
		trackerCamera.getVideoDisplay().addVideoListener(
		new VideoDisplayListener<MBFImage>(){
			public void beforeUpdate(MBFImage frame){
				if(targetList.size() < NUM_OF_TARGETS){
					List<Target> detectedTargets = motionDetector.detectTargets(frame);
					addUniqueTargets(detectedTargets,NUM_OF_TARGETS - targetList.size());
				}

				for(int i = 0; i < targetList.size(); i++){
					Target t = targetList.get(i);
					//we check the time stamp of the target to find its age in milliseconds
					//if it is too static (doesn't move for a period of time) it is removed from the list
					if(t.isExpired()){
						//System.out.println("EXPIRED");
						motionDetector.createBackgroundSubMask(frame,t.getBounds().calculateRegularBoundingBox());
						targetList.remove(i);
						i--;
					//if the target is moving in a predictable pattern
					}else{
						Rectangle r = t.getBounds().calculateRegularBoundingBox();
						
						
						r.setBounds(r.x - 3, r.y - 3, r.width + 6, r.height + 6);
						//frame.drawShape(r, RGBColour.GREEN);
						//DisplayUtilities.displayName(roi.flatten(), "roi");			
						//System.out.println("SUBFRAME");
						List<Target> detectedSubTargets = motionDetector.detectSubTargets(frame,r);
						if(detectedSubTargets.isEmpty()){
							//System.out.println("UNDETECTED SUBTARGET REMOVED");
							targetList.remove(i);
							i--;
						}else{
							//System.out.println("TARGET UPDATED");
							Target subTarget = detectedSubTargets.get(0);
							subTarget.getBounds().translate(r.x,r.y);
							t.update(subTarget.getBounds().getCOG().getX(),subTarget.getBounds().getCOG().getY(),subTarget.getBounds());
							Target predictedTarget = t.predict(50000000);
							if(predictedTarget != null){
								//if the target is predictable for a while the laser head
								//is moved over to the targets real time position
								laserController.move(predictedTarget.getX(), predictedTarget.getY());
								if(t.isLocked()){
									//laserController.move((int)xPredicted, (int)yPredicted);
								}
							}else{
								//otherwise if the target was not moving in a predictable
								//pattern it is removed from the list
								//System.out.println("UNPREDICTABLE REMOVED");
								targetList.remove(i);
								i--;
							}
						}
					}
				}
				for(Target t:targetList){
					t.drawTarget(frame);
					frame.drawText(targetList.indexOf(t) + "", t.getBounds().getTopLeft(), HersheyFont.TIMES_MEDIUM,10);
					
				}
			}

			public void afterUpdate(VideoDisplay<MBFImage> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	private void addUniqueTargets(List<Target> targets, int amount){
		if(targets.size() < amount){
			amount = targets.size();
		}
		
		for(int j = 0; j < amount; j++){
			boolean unique = true;
			Target t = null;
			Target q = targets.get(j);
			for(int i = 0; i < targetList.size(); i++){
				t = targetList.get(i);
				if(t.getBounds().isOverlapping(q.getBounds())){
					i = targetList.size();
					unique = false;
				}else{
					unique = true;
				}
			}
			if(unique){
				targetList.add(q);
			}
		}
	}
	
	@SuppressWarnings("unused")
	private Target getClosestTarget(Target lastTarget, List<ConnectedComponent> comp){
		float x1 = 0;
		float y1 = 0;
		float x2 = lastTarget.getX();
		float y2 = lastTarget.getY();
		int numOfCircles = comp.size();
		double[] d = new double[numOfCircles];
		double dCurrent = 0.0;
		int index = 0;
		for(int i = 0; i < numOfCircles; i++){
			x1 = (float) comp.get(i).calculateCentroid()[0];
			y1 = (float) comp.get(i).calculateCentroid()[1];
			d[i] = Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
		}
		dCurrent = d[0];
		for(int i = 1; i < numOfCircles; i++){
		  if(dCurrent > d[i]){
			  dCurrent = d[i];
			  index = i;
		  }
		}
		return new Target((float)comp.get(index).calculateCentroid()[0]+rectangle.x,(float)comp.get(index).calculateCentroid()[1]+rectangle.y);
	}
}