package adam;

import java.text.DecimalFormat;
import java.text.NumberFormat;


public class App{
/*
	public static void main(String[] args) throws VideoCaptureException, InterruptedException{
		JPanel panel = new JPanel();
		Camera cam = new Camera(VideoCapture.getVideoDevices().get(1),panel);
		cam.start();
		Thread.sleep(1000);
		MBFImage mask = cam.getVideoDisplay().getVideo().getNextFrame().clone();
		ConnectedComponentLabeler labeler = new ConnectedComponentLabeler(ConnectedComponentLabeler.Algorithm.FLOOD_FILL,ConnectedComponent.ConnectMode.CONNECT_4);

		//mask.processInplace(edgeDetector);
		//mask.inverse();
		//mask.divideInplace(mask);
		for(MBFImage frame:cam.getVideoDisplay().getVideo()){
			long time = System.currentTimeMillis();
			MBFImage image = mask.subtract(frame.clone()).threshold(.2f);
			System.out.println(System.currentTimeMillis()-time);
			//List<ConnectedComponent> components = labeler.findComponents(image.flatten());
			//int i = 0;
			/*for (ConnectedComponent comp : components) {
			    if (comp.calculateArea() < 10) 
			        continue;
			    Polygon p = comp.calculateConvexHull();
			    image.drawShape(p, RGBColour.RED);
			    image.drawText("Point:" + (i++), comp.calculateCentroidPixel(), HersheyFont.TIMES_MEDIUM,10);
			    }
			DisplayUtilities.displayName(image, "test");

		}
	}*/
	public static void main(String[] args){
		NumberFormat format = new DecimalFormat("###.00");
		double d = 800.1254;
		System.out.println(format.format(d));
	}
}

