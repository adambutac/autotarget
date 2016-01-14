package adam;


import javax.swing.JPanel;
import org.openimaj.image.MBFImage;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.capture.Device;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;

public class Camera{
	
  private Device device;
  private String name;
  private VideoCapture video;
  private VideoDisplay<MBFImage> videoDisplay;
  private JPanel panel;
  
  public Camera(){
	  device = null;
	  name = null;
	  panel = null;
	  video = null;
	  videoDisplay = null;
  }
  
  public Camera(Device d, JPanel p){
	  device = d;
	  name = device.getNameStr();
	  panel = p;
	  video = null;
  }
  
  public String getName(){
	  return name;
  }
  
  public VideoDisplay<MBFImage> getVideoDisplay(){
	  return videoDisplay;
  }
  
  public boolean isRunning(){
	  if(video == null)
		  return false;
	  else
		  return true;
  }
  
  public void stop() throws InterruptedException{
	  if(videoDisplay != null){
	    panel.removeAll();
	    video.stopCapture();
	    videoDisplay.close();
	    video = null;
	    videoDisplay = null;
	  }
  }
  
  public void start() throws VideoCaptureException{
	  if(video == null){
		//video = new VideoCapture(640, 480,60.0,device);
	    video = new VideoCapture(320, 240, 120.0, device);
	    videoDisplay = VideoDisplay.createVideoDisplay(video, panel);
	  }
  }
}
