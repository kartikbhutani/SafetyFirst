package com.vikas.dtu.safetyfirst2.mSignUp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.vikas.dtu.safetyfirst2.BaseActivity;
import com.vikas.dtu.safetyfirst2.DynamicDashboardNav;
import com.vikas.dtu.safetyfirst2.R;
import com.vikas.dtu.safetyfirst2.TermsnCondition;
import com.vikas.dtu.safetyfirst2.mData.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vikas.dtu.safetyfirst2.mUtils.DialogUtils;

public class SignUpActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener{

    private static final String TAG = "SignUnActivity";

    private DatabaseReference mDatabase;
    private FirebaseAuth mFirebaseAuth;

    private EditText mNameField;
    private EditText mEmailField;
    private EditText mPasswordField;
    private EditText mConfirmPasswordField;
    private Button mSignUpButton;
    private TextView mSigninText;
    CheckBox checkBox;
    TextView tncLink;

    private static final int RC_SIGN_IN = 9001;
    private SignInButton mGoogleSignInButton;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth.AuthStateListener mAuthListener;
    int tncFlag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        tncFlag = 0;


        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();

        // Views
        mNameField = (EditText) findViewById(R.id.input_name);
        mEmailField = (EditText) findViewById(R.id.input_email);
        mPasswordField = (EditText) findViewById(R.id.input_password);
        mConfirmPasswordField = (EditText) findViewById(R.id.input_password_confirm);
        mSignUpButton = (Button) findViewById(R.id.btn_signup);
        mSigninText = (TextView) findViewById(R.id.link_login);
        checkBox = (CheckBox) findViewById(R.id.checkbox);
        tncLink = (TextView) findViewById(R.id.terms_cond);

        if(checkBox.isChecked()) {
            checkBox.setChecked(false);
        }
        // Click listeners
        mSignUpButton.setOnClickListener(this);
        mSigninText.setOnClickListener(this);
        mGoogleSignInButton = (SignInButton) findViewById(R.id.sign_in_button);

        mGoogleSignInButton.setOnClickListener(this);

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();

        // Initialize FirebaseAuth
    }

    public void clickCheckbox(View v){
        if(checkBox.isChecked()){
            tncFlag = 1;
        }
        else{
            tncFlag = 0;
        }
    }

    public void clickTnc(View v){
        startActivity(new Intent(SignUpActivity.this, TermsnCondition.class));
    }

    private void signUp() {
            Log.d(TAG, "signUp");
            if (!validateForm()) {
                return;
            }

            showProgressDialog();
            String email = mEmailField.getText().toString();
            String password = mPasswordField.getText().toString();
            String confirmPassword = mConfirmPasswordField.getText().toString();

            if (password.equals(confirmPassword)) {
                if (password.length() < 6) {
                    Toast.makeText(SignUpActivity.this, "Password length should be atleast 6", Toast.LENGTH_SHORT).show();
                } else {
                    mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    Log.d(TAG, "createUser:onComplete:" + task.isSuccessful());
                                    hideProgressDialog();

                                    if (task.isSuccessful()) {
                                        onAuthSuccess(task.getResult().getUser());
                                    } else {
                                        Toast.makeText(SignUpActivity.this, "Sign Up Failed",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            } else {
                hideProgressDialog();
                Toast.makeText(SignUpActivity.this, "Passwords don't match", Toast.LENGTH_SHORT).show();
            }
    }

    private void onAuthSuccess(FirebaseUser user) {

        String username;
        if(user.getDisplayName()==null){
            username = usernameFromEmail(user.getEmail());
        }
        else username = user.getDisplayName();
        // String username = mNameField.getText().toString();
        // Write new user
        writeNewUser(user.getUid(), username, user.getEmail(), user.getPhotoUrl()!= null ?user.getPhotoUrl().toString():null);

        // Go to DashboardActivity
        startActivity(new Intent(SignUpActivity.this, DynamicDashboardNav.class));
        finish();
    }

    private String usernameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }

    private boolean validateForm() {
        boolean result = true;
        if (TextUtils.isEmpty(mNameField.getText().toString())) {
            mNameField.setError("Required");
            result = false;
        } else {
            mNameField.setError(null);
        }
        if (TextUtils.isEmpty(mEmailField.getText().toString())) {
            mEmailField.setError("Required");
            result = false;
        } else {
            mEmailField.setError(null);
        }

        if (TextUtils.isEmpty(mPasswordField.getText().toString())) {
            mPasswordField.setError("Required");
            result = false;
        } else {
            mPasswordField.setError(null);
        }

        if (mPasswordField.getText().toString().length()<6){
            mPasswordField.setError("Password Length should be greater than 6");
            result = false;
        }else {
            mPasswordField.setError(null);
        }

        /*if (mPasswordField.getText().toString() != mConfirmPasswordField.getText().toString() ) {
            mPasswordField.setError("Passwords do not match");
            mConfirmPasswordField.setError("Passwords do not match");
            result = false;
        } else {
            mPasswordField.setError(null);
            mConfirmPasswordField.setError(null);
        }*/
        return result;
    }

    // [START basic_write]
    private void writeNewUser(String userId, String name, String email, String image) {
        User user = new User(name, email, image);

        mDatabase.child("users").child(userId).setValue(user);
    }

    // [END basic_write]
    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_signup) {
            if(tncFlag==1){
                signUp();
            }
            else{
                Toast.makeText(this, "Agree to Terms and Conditions.", Toast.LENGTH_SHORT).show();
            }
        } else if (i == R.id.link_login) {
            startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
            finish();
        }
        else  if (i == R.id.sign_in_button) {
            if(tncFlag==1){
                googleSignIn();
            }
            else{
                Toast.makeText(this, "Agree to Terms and Conditions.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void googleSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        callbackManager.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                Log.d("google sign in", "successful");
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed
                Log.e(TAG, "Google Sign In failed.");
            }
        }
    }

    @Override
    public void onBackPressed(){
        startActivity(new Intent(this,SignInActivity.class));
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        DialogUtils.showProgressDialog(SignUpActivity.this, "", getString(R.string.sign_in), false);

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            DialogUtils.dismissProgressDialog();
                        } else {
                            onAuthSuccess(mFirebaseAuth.getCurrentUser());
                        }
                    }
                });
    }
    @Override
    public void onStart() {
        super.onStart();

        // Check auth on Activity start
        if (mFirebaseAuth.getCurrentUser() != null) {
            onAuthSuccess(mFirebaseAuth.getCurrentUser());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }


}
