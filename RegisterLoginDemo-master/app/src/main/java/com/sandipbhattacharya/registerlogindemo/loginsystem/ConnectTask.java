package com.sandipbhattacharya.registerlogindemo.loginsystem;

import android.os.AsyncTask;
import android.util.Log;

public class ConnectTask extends AsyncTask<String, String, TCPclient> {
    public String response;
    @Override
    protected TCPclient doInBackground(String... message) {

        //we create a TCPClient object
        //here the messageReceived method is implemented
        //this method calls the onProgressUpdate
        TCPclient mTcpClient = new TCPclient(new TCPclient.OnMessageReceived() {
            @Override
            //here the messageReceived method is implemented
            public void messageReceived(String message) {
                //this method calls the onProgressUpdate
                publishProgress(message);
                Log.d("doInBackground", "message " + message);
            }
        });
        mTcpClient.run();

        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        //response received from server
        Log.d("test", "response " + values[0]);
        //process server response here....
    }
}
