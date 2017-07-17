package as.tra.brayden.androidtools;

import android.content.Context;
import android.os.Build;

import com.jaredrummler.android.device.DeviceName;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;

import as.tra.brayden.androidtools.task.WebPostTask;

/**
 * Created by Brayden on 6/13/2017.
 */

public class UploadExceptionHandler implements Thread.UncaughtExceptionHandler {
    private Thread.UncaughtExceptionHandler defaultUEH;

    private Context ctx;
    private String localPath;

    private String url;

    /*
     * if any of the parameters is null, the respective functionality
     * will not be used
     */
    public UploadExceptionHandler(Context ctx, String localPath, String url) {
        this.ctx = ctx;
        this.localPath = localPath;
        this.url = url;
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
    }

    public void uncaughtException(Thread t, Throwable e) {
        String timestamp = new Date().toString();
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        String stacktrace = result.toString();
        printWriter.close();
        String filename = timestamp + ".stacktrace";

        if (localPath != null) {
            //writeToFile(stacktrace, filename);
        }
        if (url != null) {
            sendToServer(stacktrace, filename);
        }

        defaultUEH.uncaughtException(t, e);
    }

    private void writeToFile(String stacktrace, String filename) {
        try {
            BufferedWriter bos = new BufferedWriter(new FileWriter(
                    localPath + "/" + filename));
            bos.write(stacktrace);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendToServer(String stacktrace, String filename) {


        JSONObject data = new JSONObject();
        try {
            data.put("device_desc", Build.BRAND.substring(0,1).toUpperCase() + Build.BRAND.substring(1) + " " + DeviceName.getDeviceName() + " " + Build.VERSION.RELEASE);
            data.put("brand", Build.BRAND);
            data.put("product",Build.PRODUCT);
            data.put("model", Build.MODEL);
            data.put("device", Build.DEVICE);
            data.put("os_version", Build.VERSION.RELEASE);
            data.put("sdk_version", Build.VERSION.SDK_INT);
            data.put("hardware",Build.HARDWARE);
            data.put("display", Build.DISPLAY);
            data.put("user", Build.USER);
            data.put("host", Build.HOST);
            data.put("build_id", Build.ID);
            data.put("serial",Build.SERIAL);
            data.put("stacktrace",stacktrace);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        /*
        IntranetPostTask task = new BroadcastPostTask(ctx,data);
        task.urlBase = url; // change to debugging url instead of api url
        task.execute();
        */

        WebPostTask task = new WebPostTask(url, ctx, new Messaging.BroadcastMessageReceiver(ctx), data) {};
        task.execute();

    }
}
