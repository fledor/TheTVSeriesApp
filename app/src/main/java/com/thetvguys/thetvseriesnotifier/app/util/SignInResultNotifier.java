package com.thetvguys.thetvseriesnotifier.app.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.thetvguys.thetvseriesnotifier.app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

/*
    *   Diese Klasse kann unabhängig von Aufrufdauer und folglich Lebenszeit einzelner Aktivitäten aufgerufen
    *   werden und wird für die Anzeige erfolgreicher oder fehlgeschlagener Authentifizierung genutzt.
    */
@SuppressWarnings("unused")
public class SignInResultNotifier implements OnCompleteListener<AuthResult> {
    private Context mContext;

    public SignInResultNotifier(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public void onComplete(@NonNull Task<AuthResult> task) {
        if (task.isSuccessful()) {
            Toast.makeText(mContext, R.string.signed_in, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, R.string.anonymous_auth_failed_msg, Toast.LENGTH_LONG).show();
        }
    }
}
