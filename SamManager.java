package com.cipherlab.samapi;


import com.cipherlab.sam.GeneralString;
import com.cipherlab.sam.params.ApduOutputData;
import com.cipherlab.sam.params.ClResult;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;


/**
 * SamManager class is used to transfer APDU command 
 */
public class SamManager{

	private static SamManager _instance;

	private static SamManagerAPI mSamManagerAPI;
	
	private static Context mContext;
	
	Object mMySamManagerSemaphore = new Object();
	
	
	protected SamManager() {
		mSamManagerAPI = SamManagerAPI.GetInstance(mContext);
		
		if (isReaderServiceRunning() && (mSamManagerAPI != null))
		{
			mSamManagerAPI.bindBReaderService();
		}
	}

	/**
	 * Initial SAM Manager and bind SAM service
	 * @param context - Activity
	 * @return SamManager object if successful or null if initial was failed.
	 */
	public static SamManager InitInstance(Context context) {

		boolean bFindCLService = false;
		
		ActivityManager manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if ((GeneralString.SamServicePackageName + ".MainService").equals(
					service.service.getClassName())) {
				bFindCLService = true;
			}
		}

		if (!bFindCLService)
			return null;
			
		mContext = context;
		if (_instance == null) {
			_instance = new SamManager();		
		}
		return _instance;

	}

	
	/**
	 * Transfer APDU command and get response Data
	 * @param cmd is integer array of APDU command(input)
	 * @param outputData is response data of APDU command(output)
	 * @return ClResult.S_OK if successful or ClResult.S_ERR if was failed.
	 */
	public ClResult ExecuteApdu(int[] cmd, ApduOutputData outputData){
		return mSamManagerAPI.myExecuteApdu(cmd, outputData);
	}
	
	/**
	 * Release SAM API and unbind SAM service.
	 * 
	 */
	public void Release(){
		if (isReaderServiceRunning() && mSamManagerAPI!=null)
		{
			mSamManagerAPI.unbindBReaderService();
		}
		
		try {
			_instance.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		_instance = null;
	}
	
	public String Get_SamServiceVer()
	{
		
		return mSamManagerAPI.GetBarcodeServiceVer();
		
	}
	
	private boolean isReaderServiceRunning() {
		ActivityManager manager = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if ((GeneralString.SamServicePackageName + ".MainService").equals(
					service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
}
