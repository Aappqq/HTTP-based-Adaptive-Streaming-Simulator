package A2;

public class Fragment {

	int quality; 							//index of quality, 0 = 250, 1 = 500 ... 
	private double downloadTimeOfFragment; 	//how long it took to download fragment kBit/s
	int fragmentSize; 						//in kBit for whole fragment
	int soFarDownloaded=0; 					// how much we have downloaded in kBit


	public Fragment (int q){				//every fragment needs a quality to start downloading and returns whole fragmentsize
		this.quality = q;
		if(q==0){
			fragmentSize = 250*4;
		}
		else if (q==1){
			fragmentSize = 500*4;
		}
		else if (q==2){
			fragmentSize = 850*4;
		}
		else {
			fragmentSize = 1300*4;
		}

	}

	public double getdownloadTimeOfFragment() {			
		return downloadTimeOfFragment;
	}

	public void setdownloadTimeOfFragment(double time) {	
		this.downloadTimeOfFragment = downloadTimeOfFragment + time;
	} 

	public double getsoFarDownloaded() {
		return soFarDownloaded;
	}

	public void setsoFarDownloaded(int kbit) {		
		soFarDownloaded = kbit;
		
		if (soFarDownloaded == fragmentSize){ //100% downloaded
			downloadTimeOfFragment = fragmentSize/downloadTimeOfFragment; //final downloadtime in kBit/s
			
		}
	} 

}