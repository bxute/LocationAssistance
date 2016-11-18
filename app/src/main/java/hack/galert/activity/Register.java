package hack.galert.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import hack.galert.Configs.Constants;
import hack.galert.connnections.ConnectionUtils;
import hack.galert.font.FontManager;
import hack.galert.R;
import hack.galert.sharedpref.SharedPreferenceManager;
import hack.galert.connnections.VolleyUtils;

public class Register extends AppCompatActivity {

    private static final String TAG = "Register";

    TextView userNameIcon;
    TextView confPassIcon;

    TextView appIconText;
    TextView personEmailIconText;
    TextView passwordIconText;
    TextView registerBtnText;
    TextView appTitlleText;
    TextView loginText;
    EditText mEmail;
    EditText mPassword;
    EditText mPasswordCnf;
    EditText fullName;
    private ProgressDialog progressDialog;
    private View decorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeComponents();

    }

    public void initializeComponents() {

        appIconText = (TextView) findViewById(R.id.appIconText);
        personEmailIconText = (TextView) findViewById(R.id.profileIcon);
        passwordIconText = (TextView) findViewById(R.id.lockIcon);
        registerBtnText = (TextView) findViewById(R.id.loginBtn);
        appTitlleText = (TextView) findViewById(R.id.appTitle);
        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);
        mPasswordCnf = (EditText) findViewById(R.id.passwordCnf);
        fullName = (EditText) findViewById(R.id.fullName);
        loginText = (TextView) findViewById(R.id.loginText);
        userNameIcon = (TextView) findViewById(R.id.userIcon);
        confPassIcon = (TextView) findViewById(R.id.cnfPassIcon);

        String link = "<U>Already Registered. Login </U>";
        loginText.setText(Html.fromHtml(link));

        // full screen mode
        decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        setTypeFace();
        attachListeners();
    }

    public void setTypeFace() {

        Typeface materialTypeFace = FontManager.getInstance(this).getTypeFace(FontManager.FONT_MATERIAL);
        Typeface robotoMedium = FontManager.getInstance(this).getTypeFace(FontManager.FONT_ROBOTO_MEDIUM);
        Typeface robotoRegular = FontManager.getInstance(this).getTypeFace(FontManager.FONT_ROBOTO_REGULAR);

        //material Icon Font
        appIconText.setTypeface(materialTypeFace);
        personEmailIconText.setTypeface(materialTypeFace);
        passwordIconText.setTypeface(materialTypeFace);
        confPassIcon.setTypeface(materialTypeFace);
        userNameIcon.setTypeface(materialTypeFace);

        //roboto regular
        mEmail.setTypeface(robotoRegular);
        mPassword.setTypeface(robotoRegular);
        registerBtnText.setTypeface(robotoMedium);
        appTitlleText.setTypeface(robotoMedium);

    }

    public void attachListeners() {
        registerBtnText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ConnectionUtils.getInstance(Register.this).isConnected()) {
                    attemptRegister();
                } else {
                    makeSnackbar("No Connectivity !");
                }

            }
        });

        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferenceManager.getInstance(Register.this).setLastLoadedSubs(0);
                startActivity(new Intent(Register.this, Login.class));
                finish();
            }
        });
    }

    public void attemptRegister() {
        // send server request for validation
        if (validate()) {
            register();
        }

    }

    public boolean validate() {

        boolean validation = false;
        View viewToFocus = null;
        String error = "";
        if (!mEmail.getText().toString().isEmpty()) {

            if (fullName.getText().toString().isEmpty()) {
                viewToFocus = fullName;
                error = "Have a Full Name !";
            }

            if (!mPassword.getText().toString().isEmpty()) {
                if (mPassword.getText().toString().equals(mPasswordCnf.getText().toString())) {
                    validation = true;
                } else {

                    error = "Password Does Not Matches !";
                }
            } else {
                error = "Must Have Password !";
                viewToFocus = mPassword;
            }
        } else {
            error = "Email Cannot Be Blank ! ";
            viewToFocus = mEmail;
        }



        if (viewToFocus != null) {
            viewToFocus.requestFocus();
            makeSnackbar(error);
        }

        return validation;
    }

    public void makeSnackbar(String msg) {
        Snackbar.make(passwordIconText, msg, Snackbar.LENGTH_LONG).show();
    }

    public void register() {

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Applying Registration Process....");
        final String email = mEmail.getText().toString();
        final String password = mPassword.getText().toString();
        final String fullname = fullName.getText().toString();
        final String TAG = "register";


        final String url = Constants.SERVER_URL_REGISTER;

        StringRequest loginJsonRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d("Register","response "+response);
                        boolean registered = response.equals("new user registration successful");

                        if (registered) {
//                            SharedPreferenceManager.getInstance(Register.this).setUserFullName(fName);
                            SharedPreferenceManager.getInstance(Register.this).setUserEmail(email);
                            redirectLogin(email, password);
                        } else {
                            progressDialog.hide();
                            progressDialog.dismiss();
                            makeSnackbar("Registration Failed ! ");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.dismiss();
                        Log.d("Login", volleyError.toString());
                        makeSnackbar("Registration Failed !");
                    }
                }){

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                HashMap<String,String> map = new HashMap<>();
                map.put("username",email);
                map.put("first_name",fullname);
                map.put("last_name","");
                map.put("password",password);

                return map;
            }
        };

        progressDialog.show();

        loginJsonRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleyUtils.getInstance().addToRequestQueue(loginJsonRequest, TAG, this);
    }

    public void redirectLogin(final String email,final String password) {

        final String url = Constants.SERVER_URL_LOGIN;

        StringRequest loginJsonRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d("LoginTest","resp "+response);
                        boolean succ = response.equals("login successful");
                        if (succ) {
                            SharedPreferenceManager utils = SharedPreferenceManager.getInstance(Register.this);
                            utils.setUserEmail(email);
                            utils.setUserToken("");
                            utils.setLoginStatus(true);
                            progressDialog.dismiss();
                            navigate();
                        } else {
                            progressDialog.hide();
                            progressDialog.dismiss();
                            makeSnackbar("Authentication Failed ! ");
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.dismiss();
                        Log.d("Login", volleyError.toString());
                        makeSnackbar("Authentication Failed !");
                    }
                }){

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("username",email);
                map.put("password",password);
                return map;
            }
        };

        progressDialog.show();

        loginJsonRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleyUtils.getInstance().addToRequestQueue(loginJsonRequest, TAG, this);

    }

    private void navigate() {

        Intent homeIntent = new Intent(this, LocationTrack.class);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
        finish();
    }

}
