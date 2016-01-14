package adam;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;
import org.openimaj.video.capture.Device;
import org.openimaj.video.capture.VideoCapture;

public class CameraList implements Iterable<Camera>{
	
	private ArrayList<Camera> list;
	private Camera camera;
	
	public CameraList(JPanel panel){
		list = new ArrayList<Camera>();
		List<Device> captureDevice = VideoCapture.getVideoDevices();
		for(Device d:captureDevice){
			list.add(new Camera(d, panel));
		}
		camera = list.get(0);
	}
	
	public Camera getCamera(){
		return camera;
	}
	
	public Camera getCamera(String name){
		for(Camera cl: list){
			if(cl.getName().equals(name)){
				camera = cl;
				return camera;
			}
		}
		return null;
	}

	
	public Iterator<Camera> iterator() {
		return list.iterator();
	}
}
