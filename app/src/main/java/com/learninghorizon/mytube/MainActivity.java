package com.learninghorizon.mytube;


import android.Manifest;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.api.services.youtube.YouTubeScopes;
import com.learninghorizon.mytube.util.DataHolder;

/**
 * Minimal activity demonstrating basic Google Sign-In.
 */
public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
        {

    private static final String TAG = "MainActivity";

    /* RequestCode for resolutions involving sign-in */
    private static final int RC_SIGN_IN = 1;

    /* RequestCode for resolutions to get GET_ACCOUNTS permission on M */
    private static final int RC_PERM_GET_ACCOUNTS = 2;

    /* Keys for persisting instance variables in savedInstanceState */
    private static final String KEY_IS_RESOLVING = "is_resolving";
    private static final String KEY_SHOULD_RESOLVE = "should_resolve";

    /* Client for accessing Google APIs */
    private static GoogleApiClient mGoogleApiClient;

    /* View to display current status (signed-in, signed-out, disconnected, etc) */
    private TextView mStatus;

    // [START resolution_variables]
    /* Is there a ConnectionResult resolution in progress? */
    private boolean mIsResolving = false;

    /* Should we automatically resolve ConnectionResults when possible? */
    private boolean mShouldResolve = false;

    private SignInButton signInButton;
    private Button signOut;
    private static Bundle singoutBundle;
            private final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Plus.API)
                    .addScope(new Scope(Scopes.PLUS_ME))
                    .addScope(new Scope(Scopes.PROFILE))
                    .addScope(new Scope(YouTubeScopes.YOUTUBE))
                    .build();

            signInButton = (SignInButton) findViewById(R.id.sign_in_button);
            signInButton.setOnClickListener(this);

            mStatus = (TextView) findViewById(R.id.status);

            signOut = (Button) findViewById(R.id.sign_out_button);
            signOut.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button: {
                onSignInClicked();
                break;
            }
            case R.id.sign_out_button:{
                onSignOutClicked(null);
                break;
            }
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        if(null==mGoogleApiClient){
            return;
        }
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop(){
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int code){
    }

    @Override
    public void onConnected(Bundle bundle){

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.GET_ACCOUNTS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            TextView mStatus = (TextView) findViewById(R.id.status);
            mStatus.setText("Allow application permissions from settings");
        }else {
            String currentAccount = Plus.AccountApi.getAccountName(mGoogleApiClient);
            DataHolder.setmGoogleApiClient(mGoogleApiClient);
            mShouldResolve = false;
            signOut.setEnabled(true);
            signInButton.setEnabled(false);
            if (null == bundle) {
                bundle = new Bundle();
            }
            bundle.putString("accountName", currentAccount);
            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }
        //REDIRECT USER TO VIDEO's LIST
    }

            @Override
            public void onRequestPermissionsResult(int requestCode,
                                                   String permissions[], int[] grantResults) {
                switch (requestCode) {
                    case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                        // If request is cancelled, the result arrays are empty.
                        if (grantResults.length > 0
                                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                            // permission was granted, yay! Do the
                            // contacts-related task you need to do.

                        } else {

                            // permission denied, boo! Disable the
                            // functionality that depends on this permission.
                        }
                        return;
                    }

                    // other 'case' lines to check for other
                    // permissions this app might request
                }
            }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Could not connect to Google Play Services.  The user needs to select an account,
        // grant permissions or resolve an error in order to sign in. Refer to the javadoc for
        // ConnectionResult to see possible error codes.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);

        if (!mIsResolving && mShouldResolve) {
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(this, RC_SIGN_IN);
                    mIsResolving = true;
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, "Could not resolve ConnectionResult.", e);
                    mIsResolving = false;
                    mGoogleApiClient.connect();
                }
            } else {
                // Could not resolve the connection result, show the user an
                // error dialog.
                //showErrorDialog(connectionResult);
            }
        } else {
            // Show the signed-out UI
            //showSignedOutUI();
        }
    }




    private void onSignInClicked() {
        // User clicked the sign-in button, so begin the sign-in process and automatically
        // attempt to resolve any errors that occur.
        mShouldResolve = true;
        mGoogleApiClient.connect();

        // Show a message to the user that we are signing in.
        mStatus.setText(R.string.signing_in);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

        if (requestCode == RC_SIGN_IN) {
            // If the error resolution was not successful we should not resolve further.
            if (resultCode != RESULT_OK) {
                mShouldResolve = false;
            }

            mIsResolving = false;
            mGoogleApiClient.connect();
        }
    }


    //SING OUT CODE
    private void onSignOutClicked(Bundle bundle) {
        // Clear the default account so that GoogleApiClient will not automatically
        // connect in the future.
        if(null!=bundle){
           bundle.putBoolean("signout", false);

        }
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            signOut.setEnabled(false);
            signInButton.setEnabled(true);
        }

        //showSignedOutUI();
    }

}