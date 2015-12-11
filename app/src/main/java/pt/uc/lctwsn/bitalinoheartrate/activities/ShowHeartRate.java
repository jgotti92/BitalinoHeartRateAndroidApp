package pt.uc.lctwsn.bitalinoheartrate.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.iHealth.HttpRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import pt.uc.lctwsn.bitalinoheartrate.services.BITalinoService;
import pt.uc.lctwsn.bitalinoheartrate.R;
import pt.uc.lctwsn.bitalinoheartrate.listeners.BITalinoListener;


public class ShowHeartRate extends ActionBarActivity implements BITalinoListener {



    private static final String TAG = "BITalinoShowHeartRate";
    private boolean bluetoothEnabled;
    public TextView tvHeartRate;
    public TextView tvLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_heart_rate);
        tvHeartRate = (TextView)findViewById(R.id.tv_heart_ate);
        tvLog = (TextView)findViewById(R.id.log);
        bluetoothEnabled = false;
        createService();




        final Button high = (Button) findViewById(R.id.button);
        final Button button = (Button) findViewById(R.id.buttonForHttp);

        button.getBackground().setColorFilter(0xe9967a00, PorterDuff.Mode.MULTIPLY);
        high.getBackground().setColorFilter(0xe9967a00, PorterDuff.Mode.MULTIPLY);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Log.i(TAG, "BAKIBAKIBAKIBBAKIBABKAIBKAIBKAIBKAIBK");

                new DownloadFilesTask().execute("http://google.com");
            }
        });

        high.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Log.i(TAG, "BAKIBAKIBAKIBBAKIBABKAIBKAIBKAIBKAIBK");

                new High().execute("http://google.com");
            }
        });

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.show_heart_rate, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_exit) {
            destroyApplication();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void destroyApplication() {
        destroyService();
        Log.d(TAG,"closeApplication");
        this.finish();
    }



    /*=================================================================================*/
    //Listener Functions

    @Override
    public void didGotHeartRate(final int hr) {
        Log.d(TAG, "didGotHeartRate");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvHeartRate.setText(""+hr);
            }
        });
    }

    @Override
    public void didGotLogInfo(final String log) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvLog.append("\n".concat(log));

            }
        });

    }

    /*=================================================================================*/
    //Connect to service

    BITalinoService mService;
    boolean mBound = false;


    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(ShowHeartRate.this, BITalinoService.class);
        Log.d(TAG, "onStart");

        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "onStop");

        // Unbind from the service
        if (mBound) {
            mService.unregisterListener(ShowHeartRate.this);
            unbindService(mConnection);

            mBound = false;
        }
    }


    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            BITalinoService.LocalBinder binder = (BITalinoService.LocalBinder) service;
            mService = binder.getService();
            mService.registerListener(ShowHeartRate.this);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public void onDestroy(){
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        //if we want to close the service when the activity is destroyed by the used
        //destroyService();
    }


    /*
    *Create a service that continues running when the application is done
     */
    public void createService(){
        if(bluetoothEnabled) {
            Log.i(TAG, "create service");
            Context context = getApplicationContext();
            Intent intent = new Intent(ShowHeartRate.this, BITalinoService.class);
            context.startService(intent);
        }
    }

    public void destroyService(){
        Log.i(TAG, "destroy service");
        Intent intent = new Intent(ShowHeartRate.this, BITalinoService.class);
        Context context = getApplicationContext();
        context.stopService(intent);
    }



    /*
     *
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            /*case BioLib.REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK)
                {
                    Toast.makeText(getApplicationContext(), "Bluetooth is now enabled! ", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Bluetooth is now enabled \n");
                    bluetoothEnabled = true;

                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Bluetooth not enabled! ", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Bluetooth not enabled \n");
                    bluetoothEnabled = false;
                }
                break;*/
            case 1:
                if(resultCode == RESULT_OK){
                    String heartRate=data.getStringExtra("heartrate");
                    tvHeartRate.setText(heartRate);
                }
                break;

        }
    }



    private class High extends AsyncTask<String, Integer, Long> {
        protected Long doInBackground(String... urls) {

            try {
                String response = HttpRequest.put("http://192.168.1.2/api/17c46cd56e785471c9be5a12cc66e73/lights/1/state").send("{\"bri\":250}").body();
                //String response = HttpRequest.get("http://google.com").body();
                System.out.println("Response was: " + response);

            } catch (Exception e) {
                Log.d(TAG, "someOtherMethod()", e);
            }


            Long dvica = 2424242442421L;
            return dvica;

        }
    }

    private class DownloadFilesTask extends AsyncTask<String, Integer, Long> {
        protected Long doInBackground(String... urls) {

            try {
                String response = HttpRequest.put("http://192.168.1.2/api/17c46cd56e785471c9be5a12cc66e73/lights/1/state").send("{\"bri\":100}").body();
                //String response = HttpRequest.get("http://google.com").body();
                System.out.println("Response was: " + response);

            } catch (Exception e) {
                Log.d(TAG, "someOtherMethod()", e);
            }



            Long dvica = 2424242442421L;




           // Map<String, Integer> data = new HashMap<String, Integer>();
           // data.put("bri", 25);

            //JSONObject Podatci = new JSONObject();
            //Podatci.put("bri", 10);
            //data.put("state", "CA");
            //String response1 = (HttpRequest.get("http://192.168.1.2/api/17c46cd56e785471c9be5a12cc66e73/lights/1/state").body());
            //System.out.println("OVO MI JE REKOOOOOOOOOOOOOOO: " + response1);
            //System.out.println("Response was: WWWWWWWWWWWWWWWWWWWWWWWWW " + data);
            //Log.i(TAG, "create iIIIIIIJIJIJIJIJIJJIJIJIJIJIJIGJIGJIJGIGJIGJIGIGJGIJGIJG"+response1);
            Log.i(TAG, "create BAKIBAKIBAKIBBAKIBABKAIBKAIBKAIBKAIBK");
            Log.i(TAG, "create BAKIBAKIBAKIBBAKIBABKAIBKAIBKAIBKAIBK BAKIBAKIBAKIBBAKIBABKAIBKAIBKAIBKAIBK");

           System.out.println("NECE NESTO");


            return dvica;

            }





        protected void onPostExecute(Long result) {
            System.out.println("onpostexecute je ovo pozvaooooo: " + result);
        }
    }

}
