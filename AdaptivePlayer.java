package A2;
import java.util.ArrayList;
import java.util.List;

public class AdaptivePlayer {
	private int minBuff;
	private int maxBuff;

	public int  buffer=0;
	private boolean isDownLoading = false;
	private int viewedTime;
	private int TotalTime;
	private boolean pauseVideo;
	private int extraBandWidth;
	private int reqQuality;
	private double alpha = 0.6; 	
	private double tempBandWidth;
	Fragment frag = null;

	List<Fragment> fragmentList = new ArrayList<Fragment>();

	public AdaptivePlayer (int miB, int maB, int totalTime) {		
		this.minBuff = miB;
		this.maxBuff = maB;
		this.TotalTime = totalTime;		
	}

	public int getBufferOccupancy() {			//Gives input 1 the buffer of each iteration.
		System.out.println("Buffer: "+buffer);
		return buffer;
	}

	public int[] getStreamChange() {			//Gives input 2 the quality of each iteration. 
		int[] QualityList = new int[565];

	System.out.println("The list is: "+fragmentList.size()+" Big");
		int y=0;
		for(int i=0; i<fragmentList.size();i++) {
			if(y<564) {
				QualityList[y] = fragmentList.get(i).quality;
				QualityList[y+1] = fragmentList.get(i).quality;
				QualityList[y+2] = fragmentList.get(i).quality;
				QualityList[y+3] = fragmentList.get(i).quality;
				y=y+4;
			}
		}

		return QualityList;
	}

	public int getQualityIndex() {			//Gives input 3 the requested quality of each request.
		return reqQuality;
	}

	public boolean checkDone () {				//Method to check if the movie is done buffering.
		if(viewedTime+buffer == TotalTime) {
			return true;
		}else {
			return false;
		}
	}

	public void Start(int tempBandWidth) {		//Starts the player and the downloader
		this.tempBandWidth = tempBandWidth;
		playerAction();
		downloadAction(tempBandWidth);
	}

	private void downloadAction(int tempBandWidth) {
		tempBandWidth = tempBandWidth + extraBandWidth;
		if(buffer < maxBuff &&!checkDone()) { 

			if(isDownLoading) { 									//continue downloading fragment
				reqQuality = 99; 									//dummy variable to show that we are'nt requesting any new fragment
				frag = fragmentList.get(fragmentList.size()-1); 	//get the fragment
				double soFarDownloaded = frag.getsoFarDownloaded(); //store how much of the fragment that has been downloaded (kBit)
				double totalData = soFarDownloaded + tempBandWidth; //

				if (totalData >= frag.fragmentSize) {									//downloaded entire fragment
					double remainingFragmentSize = frag.fragmentSize - soFarDownloaded;	//calculate remaining fragmentsize
					frag.setdownloadTimeOfFragment(remainingFragmentSize/tempBandWidth);//sets the downloaded time of fragment
					frag.setsoFarDownloaded(frag.fragmentSize);							//100% downloaded
					buffer = buffer+4;													//increase buffer
					extraBandWidth = (int)(totalData - remainingFragmentSize);			//store remaining throuhput for next itteration
					fragmentList.set(fragmentList.size()-1,frag); 						//set downloaded fragment into list
					isDownLoading = false;										
				}
				else {	//---------------------------------------->	//have not downloaded entire fragment
					frag.setdownloadTimeOfFragment(1); 				//add 1 second 
					frag.setsoFarDownloaded((int)totalData);		//
					fragmentList.set(fragmentList.size()-1,frag);	//
					isDownLoading = true;							//
					extraBandWidth = 0;								//we have no leftover bandwidth for next iteration
				}

			} else { 					//Begin download of new fragment
				DownloadFragment();		//method that downloads fragment
				isDownLoading = true;	//

				if(tempBandWidth >= frag.fragmentSize){										//has downloaded entire fragment
					double remainingFragmentSize = frag.fragmentSize - frag.soFarDownloaded;//calculate remaining fragmentsize																				
					frag.setdownloadTimeOfFragment(remainingFragmentSize/tempBandWidth);	//sets the downloaded time of fragment		
					frag.setsoFarDownloaded(frag.fragmentSize);								//100% downloaded
					buffer=buffer+4;  														//increase buffer				
					extraBandWidth = (int)(tempBandWidth - remainingFragmentSize);			//store remaining throuhput for next iteration
					fragmentList.add(frag);													//adds downloaded fragment into list
					isDownLoading = false;
				}else{										//Starts new download of fragment but not in one go.
					frag.setdownloadTimeOfFragment(1);		//add 1 second
					frag.setsoFarDownloaded(tempBandWidth);	//
					fragmentList.add(frag);					//
					extraBandWidth = 0;						//we have no leftover bandwidth for next iteration
				}
			}

		}else{										//We are over maxBuf or download all the fragments.
			System.out.println("STOP DOWNLOADING");
			isDownLoading = false;
			extraBandWidth = 0;
		}
		frag = null;
	}

	private void DownloadFragment() {									//Method to download new fragment 
		frag = new Fragment(chooseQuality(getEstimation2())); 
	}

	private int chooseQuality(double Estimation) { //Method to choose the correct quality for fragment.
		int OldQuality;
		int fragmentQual;

		if(frag==null) { //Sets quality for first iteration.
			OldQuality = 0;
			fragmentQual = 0;
			return fragmentQual;
		}
		frag = fragmentList.get(fragmentList.size()-1); 								//Looks at the quality of the last fragment.
		OldQuality = frag.quality; 														//

		if (Estimation >= 1300 && (OldQuality==2 || OldQuality==3)) {						//If statements to choose the correct quality for new fragment and prevents
			fragmentQual = 3;}															// it for choosing a quality that is not allowed.
		else if(Estimation>= 850 && (OldQuality==2 || OldQuality==3 || OldQuality==1)) { 
			fragmentQual = 2;}
		else if(Estimation >= 500) {
			fragmentQual = 1;}
		else if(Estimation >=250 && (OldQuality==0 || OldQuality==1 || OldQuality==2)) {
			fragmentQual = 0;}
		else {
			frag = fragmentList.get(fragmentList.size()-1); 								//Looks at the quality of the last fragment.
			OldQuality = frag.quality; 
			fragmentQual = OldQuality-2;
		}
		

		reqQuality = fragmentQual;																//Stores requested quality for input3.

		System.out.println("Requesting new fragment, Quality for fragment is: "+fragmentQual);

		return fragmentQual;
	}

	private double getEstimation() { 										//Option 1 (Bandwidth Estimation)
		if (fragmentList.isEmpty()) { 												
			return 0;
		} else {
			frag = fragmentList.get(fragmentList.size()-1); 						
			double average = frag.getdownloadTimeOfFragment(); 						
			return average;
		}
	}

	private double getEstimation2() { 										//Option 2 (Bandwidth Estimation)
		if (fragmentList.isEmpty()) { 												
			return 0;
		} else {
			frag = fragmentList.get(fragmentList.size()-1); 						
			double oldEstimate =  frag.getdownloadTimeOfFragment();					
			return ((1-alpha)*oldEstimate+(alpha*tempBandWidth));
		}
	}

	public void playerAction () {			//Method to control the player
		if(buffer <= 0) { 					//empty buffer or something went wrong			
			pauseVideo = true;				
			System.out.println("PAUSE");
		}

		else if(buffer>0 && !pauseVideo) {		//Buffer is over 0 and the video is not paused
			System.out.println("Playing...");		
			pauseVideo = false; 				// play video
			buffer=buffer-1;					//decrease buffer
			viewedTime=viewedTime+1;			//increase Viewed time
			
		}
		else if (buffer>=minBuff && pauseVideo) { 		//waits until minBuff is reached then plays.
			System.out.println("MinBuff reached, Start playing");	
			pauseVideo = false;							//Play video
			buffer=buffer-1;							//decrease buffer
			viewedTime=viewedTime+1;					//increase Viewed time

		}

	}

}

