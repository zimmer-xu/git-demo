/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package com.cipherlab.samapi;



import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.List;

import com.cipherlab.sam.GeneralString;
import com.cipherlab.sam.codebase.ISamServiceInterface;
import com.cipherlab.sam.params.ApduOutputData;
import com.cipherlab.sam.params.ClResult;
import com.cipherlab.samapi.*;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;


public class SamManagerAPI extends android.app.Application implements Serializable{

	private static SamManagerAPI _instance;
	private static Context context;

	
	
	private boolean bindServiceFlag=false;
	ISamServiceInterface mService=null;
	
	
	public SamManagerAPI()
	{

	}
	
	public static SamManagerAPI GetInstance(Context context)
    {
		SamManagerAPI.context = context;
		if (_instance == null) {
			_instance = new SamManagerAPI();
		}

		return _instance;

    }
	
	public static SamManagerAPI GetExistInstance()
    {
		return _instance;
    }
	
	
	public void  deinit() throws SecurityException, IOException, InvalidParameterException {

		_instance = null;

	}
	
	
	public ClResult myExecuteApdu(int[] cmd, ApduOutputData outputData){
		try {
			int iRet = mService.sendAPDUcmd(cmd, outputData);
			return ClResult.values()[iRet];
		} catch (RemoteException e) {
			e.printStackTrace();
			return ClResult.S_ERR;
		}
	}
	
	public void bindBReaderService(){
		if (!bindServiceFlag) {
			ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			List<ActivityManager.RunningServiceInfo> list = activityManager.getRunningServices(100);
			for (ActivityManager.RunningServiceInfo serviceInfo : list) {
				if (serviceInfo.service.getPackageName().equals(GeneralString.SamServicePackageName)) {
					Intent intent = new Intent();
					intent.setComponent(serviceInfo.service);
					context.bindService(intent, conn, Context.BIND_AUTO_CREATE);
					break;
				}
			}
		}
    }
    
	public void unbindBReaderService(){
        if(bindServiceFlag == true){
        	context.unbindService(conn);
            bindServiceFlag = false;
        }
    }
    
	public String GetBarcodeServiceVer(){
		assert(mService!=null) : "Can't bind reader service";
		String sRet="";
		try {
			sRet = mService.GetServiceVersion();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return sRet;
	}
	
    public ServiceConnection conn = new ServiceConnection() {
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            
        }
        
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        	mService = ISamServiceInterface.Stub.asInterface(service);

            bindServiceFlag = true;
            
            Intent RTintent = new Intent(GeneralString.Intent_SAMSERVICE_CONNECTED);
			RTintent.setFlags(Intent.FLAG_FROM_BACKGROUND);
			context.sendBroadcast(RTintent);
        }
    };
    
 
}
