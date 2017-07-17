package as.tra.brayden.androidtools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.support.annotation.NonNull;


/**
 * Created by Brayd on 7/1/2017.
 */

public abstract class RegisterReceiver extends BroadcastReceiver {
    protected IntentFilter filter = new IntentFilter();
    protected Context ctx;
    protected RegisterReceiver(@NonNull Context ctx, String[] actions) {
        this.ctx = ctx;
        for(int i = 0; i < actions.length; ++i) {
            filter.addAction(actions[i]);
        }
        register();
    }
    public void register() {
        ctx.registerReceiver(this,filter);
    }
    public void unregister() {
        ctx.unregisterReceiver(this);
    }
}
