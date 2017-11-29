package com.project.context.iparking.server.service;


import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class BootBroadcastReceiver extends BroadcastReceiver {
    static final String action_boot = "android.intent.action.BOOT_COMPLETED";

    public  KeyguardLock kl;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(action_boot)) {
//			Intent bootIntent = new Intent(context, MyService.class);
//			bootIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);    
//			context.startService(bootIntent);
//			Toast.makeText(context, "kajiqidong............", 3000);


        } else if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
            System.out.println("pingmu kai");
            KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            kl = km.newKeyguardLock("unLock");
            kl.disableKeyguard();
        }else if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
//			if(kl != null){
//				kl.reenableKeyguard();
//			}
        }
    }
}
