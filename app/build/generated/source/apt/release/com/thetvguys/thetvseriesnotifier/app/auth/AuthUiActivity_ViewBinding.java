// Generated code from Butter Knife. Do not modify!
package com.thetvguys.thetvseriesnotifier.app.auth;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.thetvguys.thetvseriesnotifier.app.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class AuthUiActivity_ViewBinding implements Unbinder {
  private AuthUiActivity target;

  private View view2131296478;

  @UiThread
  public AuthUiActivity_ViewBinding(AuthUiActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public AuthUiActivity_ViewBinding(final AuthUiActivity target, View source) {
    this.target = target;

    View view;
    target.mUseEmailProvider = Utils.findRequiredViewAsType(source, R.id.email_provider, "field 'mUseEmailProvider'", CheckBox.class);
    target.mUsePhoneProvider = Utils.findRequiredViewAsType(source, R.id.phone_provider, "field 'mUsePhoneProvider'", CheckBox.class);
    target.mUseGoogleProvider = Utils.findRequiredViewAsType(source, R.id.google_provider, "field 'mUseGoogleProvider'", CheckBox.class);
    target.mUseGoogleTos = Utils.findRequiredViewAsType(source, R.id.google_tos, "field 'mUseGoogleTos'", RadioButton.class);
    target.mUseFirebaseTos = Utils.findRequiredViewAsType(source, R.id.firebase_tos, "field 'mUseFirebaseTos'", RadioButton.class);
    target.mUseGooglePrivacyPolicy = Utils.findRequiredViewAsType(source, R.id.google_privacy, "field 'mUseGooglePrivacyPolicy'", RadioButton.class);
    target.mUseFirebasePrivacyPolicy = Utils.findRequiredViewAsType(source, R.id.firebase_privacy, "field 'mUseFirebasePrivacyPolicy'", RadioButton.class);
    view = Utils.findRequiredView(source, R.id.sign_in, "field 'mSignIn' and method 'signIn'");
    target.mSignIn = Utils.castView(view, R.id.sign_in, "field 'mSignIn'", Button.class);
    view2131296478 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.signIn(p0);
      }
    });
    target.mRootView = Utils.findRequiredView(source, R.id.root, "field 'mRootView'");
    target.mFirebaseLogo = Utils.findRequiredViewAsType(source, R.id.firebase_logo, "field 'mFirebaseLogo'", RadioButton.class);
    target.mGoogleLogo = Utils.findRequiredViewAsType(source, R.id.google_logo, "field 'mGoogleLogo'", RadioButton.class);
    target.mNoLogo = Utils.findRequiredViewAsType(source, R.id.no_logo, "field 'mNoLogo'", RadioButton.class);
    target.mEnableCredentialSelector = Utils.findRequiredViewAsType(source, R.id.credential_selector_enabled, "field 'mEnableCredentialSelector'", CheckBox.class);
    target.mEnableHintSelector = Utils.findRequiredViewAsType(source, R.id.hint_selector_enabled, "field 'mEnableHintSelector'", CheckBox.class);
    target.mAllowNewEmailAccounts = Utils.findRequiredViewAsType(source, R.id.allow_new_email_accounts, "field 'mAllowNewEmailAccounts'", CheckBox.class);
    target.mRequireName = Utils.findRequiredViewAsType(source, R.id.require_name, "field 'mRequireName'", CheckBox.class);
    target.mGoogleScopesLabel = Utils.findRequiredViewAsType(source, R.id.google_scopes_label, "field 'mGoogleScopesLabel'", TextView.class);
    target.mGoogleScopeDriveFile = Utils.findRequiredViewAsType(source, R.id.google_scope_drive_file, "field 'mGoogleScopeDriveFile'", CheckBox.class);
    target.mGoogleScopeYoutubeData = Utils.findRequiredViewAsType(source, R.id.google_scope_youtube_data, "field 'mGoogleScopeYoutubeData'", CheckBox.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    AuthUiActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.mUseEmailProvider = null;
    target.mUsePhoneProvider = null;
    target.mUseGoogleProvider = null;
    target.mUseGoogleTos = null;
    target.mUseFirebaseTos = null;
    target.mUseGooglePrivacyPolicy = null;
    target.mUseFirebasePrivacyPolicy = null;
    target.mSignIn = null;
    target.mRootView = null;
    target.mFirebaseLogo = null;
    target.mGoogleLogo = null;
    target.mNoLogo = null;
    target.mEnableCredentialSelector = null;
    target.mEnableHintSelector = null;
    target.mAllowNewEmailAccounts = null;
    target.mRequireName = null;
    target.mGoogleScopesLabel = null;
    target.mGoogleScopeDriveFile = null;
    target.mGoogleScopeYoutubeData = null;

    view2131296478.setOnClickListener(null);
    view2131296478 = null;
  }
}
