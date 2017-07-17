package as.tra.brayden.androidtools;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.json.JSONException;
import org.json.JSONObject;

import as.tra.brayden.androidtools.annotation.Action;
import as.tra.brayden.androidtools.annotation.ResponseKey;
import as.tra.brayden.androidtools.annotation.Sound;


/**
 * Created by Brayden on 3/16/2017.
 *
 * Messaging tools
 */

public abstract class Messaging {

    private static final int DEFAULT_LENGTH = Snackbar.LENGTH_LONG;
    private static AlertDialog dialog;

    private static String LAST_MESSAGE = "";



    private static final int TONES_ERROR = 3;
    private static final int TONE_ERROR_DELAY_MS = 600;
    private static final int TONE_SUCCESS_DELAY_MS = 100;
    private static final int TONE_UPDATE_DELAY_MS = 100;

    private static boolean TONE_PLAYING = false;
    public static final int STREAM_TYPE = AudioManager.STREAM_RING;
    private static ToneGenerator generator = new ToneGenerator(STREAM_TYPE, 100);
    private static void playTone(@Sound.Tone int tone, int delayMS) {
        generator.startTone(tone, delayMS);
    }

    public abstract static class MessageReceiver extends BroadcastReceiver {

        public void handleMessage(JSONObject message) {
            try {
                handleMessageOrThrow(message);
            } catch (NoMessageException ignored) {

            }
        }
        public abstract void handleMessageOrThrow(JSONObject message) throws NoMessageException;

        public void show(String type, String msg) {
            JSONObject o = new JSONObject();
            try {
                o.put(type, msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            handleMessage(o);
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                handleMessage(new JSONObject(intent.getStringExtra("json")));
                //Messaging.handle(context, new JSONObject(intent.getStringExtra("json")));
            } catch (JSONException e) {
                e.printStackTrace();
                //Messaging.showError(context, "Invalid format for MessageReceiver");
            }

        }
    }

    public static class DisplayMessageReceiver extends MessageReceiver {

        private final Activity activity;

        public DisplayMessageReceiver(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void handleMessageOrThrow(@NonNull JSONObject message) throws NoMessageException {
            Messaging.handleOrThrow(activity, message);
        }
    }

    /**
     * Broadcasts ACTION_MESSAGE. The current activity should have a DisplayMessageReceiver registered.
     */
    public static class BroadcastMessageReceiver extends MessageReceiver {
        private final Context ctx;
        private final String TAG = BroadcastMessageReceiver.class.getSimpleName();

        public BroadcastMessageReceiver(Context ctx) {
            this.ctx = ctx;
        }

        @Override
        public void handleMessageOrThrow(@NonNull JSONObject message) throws NoMessageException {
            Log.d(TAG, "broadcasting "+message.toString());
            Intent intent = new Intent();
            intent.setAction(Action.MESSAGE);
            intent.putExtra("json", message.toString());
            ctx.sendBroadcast(intent);
        }
    }

    public static class SilentMessageReceiver extends MessageReceiver {

        private static String TAG = SilentMessageReceiver.class.toString();

        @Override
        public void handleMessageOrThrow(@NonNull JSONObject message) throws NoMessageException {
            Log.i(TAG, message.toString());
        }
    }

    @Nullable
    private static View getView(@NonNull Activity activity) {
        return activity.findViewById(android.R.id.content).getRootView();
    }

    @Nullable
    private static android.os.IBinder getBinder(Activity activity) {
        View view;
        if(activity == null || (view = getView(activity)) == null) return null;
        return view.getWindowToken();
    }

    public static void hideInput(@Nullable Activity activity) {
        if(activity == null) return;
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getBinder(activity), 0);
    }

    public static class NoMessageException extends Exception {
        public NoMessageException(String message) {
            super(message);
        }
    }

    public static class SilentlyFailExceptionHandler implements Thread.UncaughtExceptionHandler {
        public void uncaughtException(Thread th, Throwable ex) {
            ex.printStackTrace();
            th.interrupt();
        }
    }


    public static void handle(final Activity activity, JSONObject json) {
        try {
            handleOrThrow(activity, json);
        } catch (NoMessageException e) {

        }
    }

    public static void handleOrThrow(final @Nullable Activity activity, @NonNull JSONObject json) throws NoMessageException {
        if(activity == null) return;
        try {
            final JSONObject data = json;

            // Broadcast instantly
            if(data.has(ResponseKey.BROADCAST)) {
                JSONObject extras = data.has(ResponseKey.BROADCAST_EXTRAS) ? new JSONObject(data.getString(ResponseKey.BROADCAST_EXTRAS)) : new JSONObject();
                @Action String broadcastAction = data.getString(ResponseKey.BROADCAST);
                new Action.Broadcast(activity, broadcastAction, extras);
            }


            if(data.has(ResponseKey.ALERT)) {

                Messaging.OnClickListener dialogAction, cancelAction = null;

                // link = URL to load on OK
                // action_broadcast = message to broadcast on OK
                if(data.has(ResponseKey.LINK) || data.has(ResponseKey.ACTION_BROADCAST)) {

                    final @Action String actionBroadcast = data.has(ResponseKey.ACTION_BROADCAST) ? data.getString(ResponseKey.ACTION_BROADCAST) : null;
                    final Intent browserIntent   = data.has(ResponseKey.LINK) ? new Intent(Intent.ACTION_VIEW, Uri.parse(data.getString(ResponseKey.LINK))) : null;

                    final JSONObject extras = data.has(ResponseKey.ACTION_BROADCAST_EXTRAS) ? new JSONObject(data.getString(ResponseKey.ACTION_BROADCAST_EXTRAS)) : new JSONObject();

                    dialogAction = new Messaging.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(actionBroadcast != null) new Action.Broadcast(activity,actionBroadcast,extras);
                            if(browserIntent != null)   activity.startActivity(browserIntent);
                        }

                        @Override
                        public void onClick(DialogInterface iface, int num) {
                            onClick(null);
                        }
                    };


                    if(data.has(ResponseKey.CANCEL_BROADCAST)) {
                        final @Action String cancelBroadcast = data.getString(ResponseKey.CANCEL_BROADCAST);
                        cancelAction = new Messaging.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new Action.Broadcast(activity,cancelBroadcast,new JSONObject());
                            }
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new Action.Broadcast(activity,cancelBroadcast,new JSONObject());
                            }
                        };

                    }


                    //ctx, String msg, String buttonText, View.OnClickListener action) {

                } else {
                    dialogAction = new Messaging.OnClickListener() {
                        @Override
                        public void onClick(View v) {}
                        @Override
                        public void onClick(DialogInterface iface, int num) { }
                    };
                }
                String buttonText = data.has(ResponseKey.BUTTONTEXT) ? data.getString(ResponseKey.BUTTONTEXT)
                        : (data.has(ResponseKey.LINK) ? "Download" : "OK");
                if(data.has(ResponseKey.DESCRIPTION) ||
                        ( data.has(ResponseKey.DIALOG) && data.getBoolean(ResponseKey.DIALOG))) { // show dialog

                    String msg = data.has(ResponseKey.DESCRIPTION) ? data.getString(ResponseKey.DESCRIPTION) : "App version "+BuildConfig.VERSION_NAME+" is too old - please update now";
                    String no = data.has(ResponseKey.CANCEL) ? data.getString(ResponseKey.CANCEL) : null;

                    if(dialog != null && dialog.isShowing()) return;

                    if(no != null) {

                        dialog = new AlertDialog.Builder(activity)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle(data.getString(ResponseKey.ALERT))
                                .setMessage(msg)
                                .setPositiveButton(buttonText, dialogAction)
                                .setNegativeButton(no, cancelAction)
                                .show();
                    } else {
                        dialog = new AlertDialog.Builder(activity)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle(data.getString(ResponseKey.ALERT))
                                .setMessage(msg)
                                .setPositiveButton(buttonText, dialogAction)
                                .show();
                    }


                } else { // show snackbar
                    Messaging.showAction(activity, data.getString(ResponseKey.ALERT), buttonText, dialogAction);
                }

                //Messaging.showAction(ctx, data.getString("alert"), "OK", action, Snackbar.LENGTH_INDEFINITE
                //Messaging.showAction(ctx, data.getString(""))
                return;

            }
            if(data.has(ResponseKey.ERROR)) {
                Messaging.showError(activity, data.get(ResponseKey.ERROR).toString());
                return;
            } else if(data.has(ResponseKey.SUCCESS)) {
                Messaging.showSuccess(activity, data.get(ResponseKey.SUCCESS).toString());
                return;
            } else if(data.has(ResponseKey.MESSAGE)) {
                Messaging.showMessage(activity, data.get(ResponseKey.MESSAGE).toString());
                return;
            }
            throw new NoMessageException("No message found in "+data.toString());

        } catch (JSONException e) {
            e.printStackTrace();
            showError(activity, "JSON parse exception...");
        }
    }

    // Not going to remove the following methods, but they should be called via a MessageReceiver instead of directly.


    public static void show(Activity activity, @NonNull Snackbar snackbar, int color) {
        snackbar.getView().setBackgroundColor(color);
        show(activity, snackbar);
    }


    public static void show(Activity activity, @NonNull Snackbar snackbar) {
        Messaging.LAST_MESSAGE = snackbar.toString();
        hideInput(activity);
        snackbar.show();
    }


    public static void showSuccess(@Nullable Activity activity, String msg, int length) {

        // if(!msg.trim().substring(0,6).toLowerCase().equals("success")) msg = "Success: " + msg;
        View view;
        if(activity == null || (view = getView(activity)) == null) return;
        Snackbar snackbar = Snackbar.make(view, msg, length);
        show(activity, snackbar, ContextCompat.getColor(activity, R.color.colorSuccess));
    }


    public static void showSuccess(@Nullable Activity activity, String msg) {
        showSuccess(activity, msg, Snackbar.LENGTH_SHORT);
    }


    public static void showMessage(@Nullable Activity activity, String msg, int length) {
        View view;
        if(activity == null || (view = getView(activity)) == null) return;
        Snackbar snackbar = Snackbar.make(view, msg, length);
        show(activity, snackbar);
    }


    public static void showMessage(@Nullable Activity activity, String msg) {
        showMessage(activity, msg, Snackbar.LENGTH_SHORT);
    }


    public static void showError(@Nullable Activity activity, String msg, int length) {

        // if(!msg.trim().substring(0,4).toLowerCase().equals("error")) msg = "Error: " + msg;
        View view;
        if(activity == null || (view = getView(activity)) == null) return;
        Snackbar snackbar = Snackbar.make(view, msg, length);
        show(activity, snackbar, ContextCompat.getColor(activity, R.color.colorError));
    }


    public static void showError(Activity activity, String msg) {
        showError(activity, msg, Snackbar.LENGTH_LONG);
    }



    public static void showAction(@Nullable Activity activity, String msg, String buttonText, View.OnClickListener action, int length) {
        View view;
        if(activity == null || (view = getView(activity)) == null) return;
        Snackbar snackbar = Snackbar.make(view, msg, length)
                .setAction(buttonText, action);

        show(activity, snackbar);
    }


    public static void showAction(@Nullable Activity activity, String msg, String buttonText, View.OnClickListener action) {
        showAction(activity, msg, buttonText, action, Snackbar.LENGTH_INDEFINITE);
    }


    public static void showAction(@Nullable Activity activity, @NonNull Snackbar snackbar, int color) {
        snackbar.getView().setBackgroundColor(color);
        showAction(activity, snackbar);
    }


    public static void showAction(@Nullable Activity activity, @NonNull Snackbar snackbar) {
        if(activity == null) return;
        LAST_MESSAGE = snackbar.toString();
        hideInput(activity);
        snackbar.show();
    }

    public static void playSound(final Context ctx, final @Sound String sound) {
        if(sound == null || TONE_PLAYING) return;
        TONE_PLAYING = true;


        final AudioManager audio  = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        final Vibrator vibrator = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);

        new Thread(new Runnable()  {


            @Override
            public void run() {

                switch(sound) {
                    case Sound.SUCCESS :

                        if(audio.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) playTone(Sound.Tone.SUCCESS,TONE_SUCCESS_DELAY_MS);
                        if(audio.getRingerMode() != (AudioManager.RINGER_MODE_SILENT)) vibrator.vibrate(TONE_SUCCESS_DELAY_MS);
                        
                        //play(TONE_SUCCESS, new long[] {0,TONE_SUCCESS_DELAY_MS,TONE_SUCCESS_DELAY_MS});
                        break;
                    case Sound.FAILURE :

                        long vLen = TONE_ERROR_DELAY_MS / 3 / 2;

                        if(audio.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) playTone(Sound.Tone.FAILURE, TONE_ERROR_DELAY_MS);
                        if(audio.getRingerMode() != (AudioManager.RINGER_MODE_SILENT))vibrator.vibrate(new long[] {0,vLen,vLen,vLen,vLen,vLen,vLen,vLen,vLen}, -1);

                        break;
                    case Sound.UPDATE :
                        //if(audio.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) playTone(Sound.Tone.UPDATE, TONE_UPDATE_DELAY_MS);
                        //if(audio.getRingerMode() != (AudioManager.RINGER_MODE_SILENT))
                        vibrator.vibrate(TONE_SUCCESS_DELAY_MS);

                        //play(TONE_SUCCESS, new long[] {0,TONE_SUCCESS_DELAY_MS,TONE_SUCCESS_DELAY_MS});
                        break;

                }

                TONE_PLAYING = false;
            }
        }).start();


    }

    public interface OnClickListener extends DialogInterface.OnClickListener, View.OnClickListener {

    }

}
