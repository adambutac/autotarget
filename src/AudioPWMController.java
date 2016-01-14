package adam;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class AudioPWMController implements Runnable{
    private static int SAMPLING_RATE = 44100;
    private static int NUMBER_OF_CHANNELS = 1;
    private static int BYTES_PER_CHANNEL = 2;
    private static int FREQUENCY = 90;
    private static int PULSE_WIDTH_SIZE = BYTES_PER_CHANNEL*NUMBER_OF_CHANNELS;     
    private static int BITS_IN_SAMPLE = 16;
    private static int BYTES_IN_PERIOD = (int) Math.ceil(SAMPLING_RATE*PULSE_WIDTH_SIZE/FREQUENCY);
    private SourceDataLine line;
    private boolean running;
	private byte[] sample = new byte[BYTES_IN_PERIOD];
	private PWMDevice[] device = new PWMDevice[NUMBER_OF_CHANNELS];

////////////////////////////////////////////////////////////////////////////////
///////////////////////////////PWM Device///////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
//////////A PWM Device is an object used to create a pwm signal/////////////////
////////////for n increments between a min and max pulse width./////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

	public class PWMDevice{
	    private int increments;
	    private double minPulseWidth;
	    private double maxPulseWidth;
	    private double[] dutyCycle;
		private byte[][] PWM;
		
		public PWMDevice(){
			//There is always at least one increment available.
			//I mean, without an increment is there a pulse width? No.
			increments = 0;
			
			//Minimum pulse width in milliseconds
			//Must always be less than or equal to maximum.
			minPulseWidth = 0;
			
			//Maximum pulse width in milliseconds
			//Must always be greater than or equal to minimum.
			maxPulseWidth = 0;
			
			//An array to hold each increments duty cycle
			//The duty cycle of a PWM signal is expressed in percentage.
			//The formulae of duty cycle is as follows:
			//PulseWidth/(1.0/FREQUENCY*1000)
			//where pulse width is in milliseconds
			//For example: a duty cycle of 100% would be a constant voltage.
			//Similarly, a duty cycle of 0% would be a constant 0 voltage.
			dutyCycle = new double[increments];
			
			//An array of byte[] which hold the PWM signal corresponding
			//to its index in the array. Each byte[] is a representation
			//of a PWM signal in an audio data format. Each byte[] is 
			//initialized to hold the amount of bytes in a single period
			//(one full cycle) for a single channel.
			PWM = new byte[increments][BYTES_IN_PERIOD/NUMBER_OF_CHANNELS];
			buildSample();
		}
		
		////////////////////////////////////////////////////////////////////////////////
		////////PWMDevice: inc, int, number of PWM signals to genereate/////////////////
		//////////////////     I used this to set the amount of angles my servo motor///
		//////////////////     was able to rotate. By multiplying by a factor higher////
		//////////////////     degrees of accuracy can be obtained. Example: 10.5///////
		//////////////////     would become 105 when multiplied by a factor of 10.//////
		//////////////////     A normal servo can rotate 180 degrees so the max degree//
		//////////////////     in this example would be 1800.///////////////////////////
		///////////////////minPW, double, minimum pulse width in milliseconds///////////
		///////////////////maxPW, double, maximum pulse width in milliseconds///////////
		////////////////////////////////////////////////////////////////////////////////
		////////PWMDevice creates a new PWMDevice with the given parameters, while//////
		////////  simultaneously synthesizing all PWM signals using buildSample().//////
		////////////////////////////////////////////////////////////////////////////////

		public PWMDevice(int inc, double minPW, double maxPW){
			if(inc < 1){
				throw new IllegalArgumentException("Increment must be at least 1.");
			}
			if(minPW < 0){
				throw new IllegalArgumentException("Minimum pulse width must greater than or equal to 0.");
			}
			if(minPW > maxPW){
				throw new IllegalArgumentException("Minimum pulse width must be less than or equal to maximum pulse width.");
			}
			if(maxPW < 0 ){
				throw new IllegalArgumentException("Maximum pulse width must be greater than or equal to 0.");
			}
			if(maxPW > 100){
				throw new IllegalArgumentException("Maximum pulse width must be less or equal to 100.");
			}
				
			increments = inc;
			minPulseWidth = minPW;
			maxPulseWidth = maxPW;
			dutyCycle = new double[increments];
			PWM = new byte[increments][BYTES_IN_PERIOD/NUMBER_OF_CHANNELS];
			buildSample();
		}
		////////////////////////////////////////////////////////////////////////////////
		//////////////buildSample:  Calculates and populates the dutyCycle[] as well////
		//////////////////////////    as the PWM[]./////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////

		private void buildSample(){
			double minDutyCycle = minPulseWidth/(1.0/FREQUENCY*1000);
			double maxDutyCycle = maxPulseWidth/(1.0/FREQUENCY*1000);
			double incDutyCycle = (maxDutyCycle - minDutyCycle)/increments;
	    	for(int i = 0; i < increments; i++){
	    		dutyCycle[i] = incDutyCycle * i + minDutyCycle;
	    		for(int j = 0; j < PWM[i].length; j++){
	    			if(j < PWM[i].length*dutyCycle[i]){
	    				System.out.print(1);
	    				PWM[i][j] = Byte.MAX_VALUE;
	    			}
	    			else{
	    				System.out.print(0);
	    				PWM[i][j] = 0;
	    			}
	    		}
    			System.out.println();
	    	}
		}
		
		////////////////////////////////////////////////////////////////////////////////
		///////////////getPWM:  index, int, the index to retrieve a PWM signal./////////
		////////////////////////////////////////////////////////////////////////////////
		///////////////getPWM gets the PWM byte[] representing an audio signal./////////
		////////////////////////////////////////////////////////////////////////////////
	    public byte[] getPWM(int index){
	    	if(index < increments && index >= 0){
	    		return PWM[index];
	    	}else{
	    		throw new IndexOutOfBoundsException("Index: " + index
	    				+ "\nServo cannot move to this position.");
	    	}
	    }
	}
	
////////////////////////////////////////////////////////////////////////////////
///////////////////////////AudioPWMController///////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
    public AudioPWMController(){
    }
    
    public void buildPWMDevice(int channel, int inc, double min, double max){
    	device[channel] = new PWMDevice(inc, min, max);
    }
    
    public void setDevice(int channel, int index){
    	byte[] signal = device[channel].getPWM(index);
    	for(int i = 0; i < BYTES_IN_PERIOD/PULSE_WIDTH_SIZE; i++){
    		for(int j = 0; j < BYTES_PER_CHANNEL; j++){
    			sample[(i*PULSE_WIDTH_SIZE)+(channel*BYTES_PER_CHANNEL) + j] = signal[(i*BYTES_PER_CHANNEL)+j];
    			sample[(i*PULSE_WIDTH_SIZE)+(channel*BYTES_PER_CHANNEL) + j] = signal[(i*BYTES_PER_CHANNEL)+j];
    		}
    	}
    }
    
    private void initializeLine() throws LineUnavailableException{
        AudioFormat format = new AudioFormat(SAMPLING_RATE, BITS_IN_SAMPLE, NUMBER_OF_CHANNELS, false, true);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)){
           System.out.println("Line matching " + info + " is not supported.");
           throw new LineUnavailableException();
        }
        line = (SourceDataLine)AudioSystem.getLine(info);
        line.open(format, BYTES_IN_PERIOD);
        line.start();
    }

    public void stop(){
    	running = false;
    	line.drain();
    	line.close();
    }
    
	public void run() {
		try {
			initializeLine();
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		running = true;
		while(running){
			line.write(sample, 0, BYTES_IN_PERIOD);
		}
	}   
	
	public void circle() throws InterruptedException{
		for(int i = 1; i < 2*Math.PI*100*100; i++){
			//servoController.setLeftServo((int) ((Math.sin(((i/100.0)*Math.PI)%(Math.PI*2))*20+90)*10));
			Thread.sleep(5);
			//servoController.setRightServo((int) ((Math.cos(((i/100.0)*Math.PI)%(Math.PI*2))*20+90)*10));
			Thread.sleep(5);
		}
	}

	public static void main(String[] args){
		AudioPWMController apwmc = new AudioPWMController();
		apwmc.buildPWMDevice(0, 1, .1, .2);
		apwmc.setDevice(0, 0);
	}
}
