package com.thinus.wallet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SmsListener extends BroadcastReceiver {
    public SmsListener() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving an Intent broadcast.

        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            Bundle bundle = intent.getExtras();           //---get the SMS message passed in---
            SmsMessage[] msgs = null;
            String msg_from;
            if (bundle != null){
                //---retrieve the SMS message received---
                try{
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];
                    for(int i=0; i<msgs.length; i++){
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        msg_from = msgs[i].getOriginatingAddress();
                        String msgBody = msgs[i].getMessageBody();
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(msgs[i].getTimestampMillis());
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        String msgDate = dateFormat.format(calendar.getTime());


                        Transaction sr2 = TransactionListActivity.getTransactionByReference(msgBody);
                        if (sr2 == null) {
                            Transaction sr1 = TransactionListActivity.ProcessSMSBody(msgDate, msgBody, true);
                            if (sr1 != null) {
                                if (sr1.getCategoryId() != 0) {
                                    Toast.makeText(context, "Wallet. Received sms for R" + sr1.getAmount() + " for " + sr1.getDescription() + ". Linked to " + sr1.getCategoryName(), Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(context, "Wallet. Received sms for R" + sr1.getAmount() + " for " + sr1.getDescription() + ". Could not link", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    }
                }catch(Exception e){
//                            Log.d("Exception caught",e.getMessage());
                }
            }
        }
    }
}
