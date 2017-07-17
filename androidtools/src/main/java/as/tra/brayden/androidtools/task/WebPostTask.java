package as.tra.brayden.androidtools.task;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import as.tra.brayden.androidtools.Messaging;
import as.tra.brayden.androidtools.R;
import as.tra.brayden.androidtools.annotation.ResponseKey;
import as.tra.brayden.androidtools.annotation.Sound;

/**
 * Created by Brayden on 3/14/2017.
 * Abstract PostTask to the intranet server
 */

public abstract class WebPostTask extends AsyncTask<String, String, Void> {

    private static String TAG = WebPostTask.class.getSimpleName();

    //private static long LAST_FETCH;


    //String url_select = getString(R.string.rest_url) + "m=manifest&app_key="+LoginBarcodeActivity.KEY;

    static {
        CookieHandler.setDefault( new CookieManager( null, CookiePolicy.ACCEPT_ALL ) );
    }



    final JSONObject params;
    JSONObject data;
    public String urlBase;
    private URL urlSelect;
    //public final Context context;
    protected Context ctx;
    protected final Messaging.MessageReceiver messageReceiver;

    public boolean suppressErrors = false;
    private String result = "";

    public WebPostTask(Messaging.MessageReceiver messageReceiver, JSONObject params)  {
        this.messageReceiver = messageReceiver;
        this.ctx = ctx;
        this.params = params;
        this.urlBase = urlBase;
        //this.urlBase = ctx.getString(R.string.rest_url);
    }



    @Override
    @OverridingMethodsMustInvokeSuper
    protected void onPreExecute() {

        if(!isNetworkAvailable(ctx)) {
            messageReceiver.show(ResponseKey.ERROR, "No network connection!");
            //Messaging.showError(ctx, "No network connection!");
            Log.e(TAG, "No network connection!");
            cancel(true);
            return;
        }


        //Iterator i = params.iterator();
        try {
            //params.put("app_key", LoginBarcodeActivity.KEY);
            params.put("app_platform", "android");
            params.put("app_package", ctx.getApplicationContext().getPackageName());
            //params.put("app_version", BuildConfig.VERSION_NAME);
            //params.put("last_fetch", WebPostTask.LAST_FETCH);
            //params.put("latitude", Settings.currentLocation.getLatitude());
            //params.put("longitude", Settings.currentLocation.getLongitude());
            //params.put("accuracy", Math.round(Settings.currentLocation.getAccuracy()));
            //params.put("bearing", Math.round(Settings.currentLocation.getBearing()));
            //params.put("speed", Math.round(Settings.currentLocation.getSpeed()));
            //params.put("gps_time", Settings.currentLocation.getTime()/1000);

            Iterator<String> columns = params.keys();
            while(columns.hasNext()) {
                String key = columns.next();
                urlBase += URLEncoder.encode(key,"UTF-8") + "=" + URLEncoder.encode(params.getString(key), "UTF-8");
                if(columns.hasNext()) urlBase += "&";
            }
            urlSelect = new URL(urlBase);
        } catch (JSONException e) {
            //Messaging.showError(ctx, ctx.getString(R.string.invalid_parameters));
            messageReceiver.show(ResponseKey.ERROR, ctx.getString(R.string.invalid_parameters));
            Log.e(TAG, e.getMessage());
            cancel(true);
            return;

        } catch (MalformedURLException e) {
            //Messaging.showError(ctx, "Invalid URL");
            messageReceiver.show(ResponseKey.ERROR, "Invalid URL");
            Log.e(TAG, e.getMessage());
            cancel(true);
            return;

        } catch (Exception e) {
            messageReceiver.show(ResponseKey.ERROR, e.getMessage());
            //Messaging.showError(ctx, e.getMessage());
            cancel(true);
            return;
            //throw e;
        }



    }


    @Override
    @Nullable
    protected Void doInBackground(String[] params) {

        if(isCancelled()) return (null);

        InputStream inputStream;

        try {
            // Set up HTTP post
            Log.d(TAG, "Opening stream from "+urlSelect.toString());

            if(ctx instanceof Activity) Messaging.hideInput((Activity)ctx);
            inputStream = (urlSelect).openStream();
            java.util.Scanner s = new java.util.Scanner(inputStream).useDelimiter("\\A");
            result = s.hasNext() ? s.next() : "";

        } catch (IOException e3) {
            messageReceiver.show(ResponseKey.ERROR, ctx.getString(R.string.no_connection));
            //Messaging.showError(ctx, ctx.getString(R.string.no_connection));
            Log.e("IO/FNFException", e3.toString());
            e3.printStackTrace();

        }
        catch (Exception e) {
            messageReceiver.show(ResponseKey.ERROR, "Download Failed");
            //Messaging.showError(ctx, "Download Failed");
            Log.e(TAG, "StringBuilding & BufferedReader " + "Error converting result " + e.toString());
            e.printStackTrace();
        }
        data = getData();
        if(data != null) {
            this.callAsyncCallback(data);
            this.callWorkerCallback(data);
        }
        return null;
    } // protected Void doInBackground(String... params)

    @Nullable
    @OverridingMethodsMustInvokeSuper
    protected JSONObject getData() {
        try {
            return (new JSONObject(result));
        } catch (JSONException e) {
            int end = result.length() > 40 ? 40 : result.length();

            if (!WebPostTask.this.suppressErrors) {

                messageReceiver.show(ResponseKey.ERROR, "Invalid server response!");
                Log.e(TAG, "Invalid JSON response: " + result.substring(0, end));
                //Messaging.showError(ctx, "Invalid server response");

            }
            return null;
        }
    }



    protected void onPostExecute(Void v) {

        try {

            //JSONObject data = getData();
            if(data == null) {
                return;
            }
            try {

                if (data.has(ResponseKey.SOUND)) {
                    @Sound String sound = data.getString(ResponseKey.SOUND);
                    Messaging.playSound(ctx, sound);
                }

                // do this first
                if (data.has(ResponseKey.LOGOUT)) {
                    //logout(data);
                   // forceCallback(data); // should we force a callback if logout...?
                    return;
                }

                if (    data.has(ResponseKey.BROADCAST) ||
                        data.has(ResponseKey.ALERT) ||
                        data.has(ResponseKey.ERROR) ||
                        data.has(ResponseKey.SUCCESS) ||
                        data.has(ResponseKey.MESSAGE)) {

                    messageReceiver.handleMessage(data);
                    callFailCallback(data);
                    callForceCallback(data);
                    return;

                }


            } catch (JSONException ignored) {
            }

            Log.d(TAG, "Recieved from server: " + result);

            callCallback(data);
            callForceCallback(data);

        } finally {
            cleanup();

        }

    } // protected void onPostExecute(Void v)

    // these can be overriden by subclasses to prevent calling the callbacks.
    protected void callWorkerCallback(@NonNull JSONObject result) {workerCallback(result);}
    protected void callAsyncCallback(@NonNull final JSONObject result) {new Thread(new Runnable() {public void run() {try {asyncCallback(result);} catch (InterruptedException ignored){}}} ).start();}
    protected void callCallback(@NonNull JSONObject result) {}
    protected void callForceCallback(@NonNull JSONObject result) {}
    protected void callFailCallback(@NonNull JSONObject result) {}

    protected void workerCallback(@NonNull JSONObject result) {};             // after downloading, on the worker thread
    protected void asyncCallback(@NonNull JSONObject result) throws InterruptedException {};              // after downloading, on a new thread
    //protected abstract void callback(@NonNull JSONObject result);           // must be overriden, what happens after (if the server doesn't create a response)
    //protected abstract void forceCallback(@NonNull JSONObject result);      // force a callback regardless of whether the server sends back errors/messages.



    public void execute() {
        this.execute(new String[] {});
    }


    /**
     * call on exceptions (finally)
     */
    @OverridingMethodsMustInvokeSuper
    protected void cleanup() {
    }

    private static boolean isNetworkAvailable(@NonNull Context ctx) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

} //class MyAsyncTask extends AsyncTask<String, String, Void>