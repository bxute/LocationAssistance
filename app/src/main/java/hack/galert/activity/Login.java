package hack.galert.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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

import hack.galert.BlurBuilder;
import hack.galert.Configs.Constants;
import hack.galert.connnections.ConnectionUtils;
import hack.galert.font.FontManager;
import hack.galert.R;
import hack.galert.sharedpref.SharedPreferenceManager;
import hack.galert.connnections.VolleyUtils;

public class Login extends AppCompatActivity {

    TextView appIconText;
    TextView personEmailIconText;
    TextView passwordIconText;
    TextView loginBtnText;
    TextView appTitlleText;

    EditText mEmail;
    EditText mPassword;
    private ProgressDialog progressDialog;
    private TextView registerText;
    private View decorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (SharedPreferenceManager.getInstance(this).isLoggedIn()) {
            navigate();
        }

        initializeComponents();
        attachListeners();
    }

    public void initializeComponents() {

//        ImageView bg = (ImageView) findViewById(R.id.login_bg);
//        Bitmap oldBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.loginbg);
//        Bitmap blurredBitmap = BlurBuilder.blur(this,oldBitmap);
//        bg.setImageBitmap(blurredBitmap);

        appIconText = (TextView) findViewById(R.id.appIconText);
        personEmailIconText = (TextView) findViewById(R.id.profileIcon);
        passwordIconText = (TextView) findViewById(R.id.lockIcon);
        loginBtnText = (TextView) findViewById(R.id.loginBtn);
        appTitlleText = (TextView) findViewById(R.id.appTitle);
        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);
        registerText = (TextView) findViewById(R.id.registerText);
        String link = "<U>New User ? Register</U>";
        registerText.setText(Html.fromHtml(link));
        // full screen mode
        decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        setTypeFace();

    }

    public void setTypeFace() {

        Typeface materialTypeFace = FontManager.getInstance(this).getTypeFace(FontManager.FONT_MATERIAL);
        Typeface robotoMedium = FontManager.getInstance(this).getTypeFace(FontManager.FONT_ROBOTO_MEDIUM);
        Typeface robotoRegular = FontManager.getInstance(this).getTypeFace(FontManager.FONT_ROBOTO_REGULAR);

        //material Icon Font
        appIconText.setTypeface(materialTypeFace);
        personEmailIconText.setTypeface(materialTypeFace);
        passwordIconText.setTypeface(materialTypeFace);

        //roboto regular
        mEmail.setTypeface(robotoRegular);
        mPassword.setTypeface(robotoRegular);
        loginBtnText.setTypeface(robotoMedium);
        appTitlleText.setTypeface(robotoMedium);

    }

    public void attachListeners() {
        loginBtnText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ConnectionUtils.getInstance(Login.this).isConnected()) {
                    attemptLogin();
                } else {
                    makeSnackbar("No Connectivity !");
                }

            }
        });

        registerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, Register.class));
                finish();
            }
        });

    }

    public void attemptLogin() {
        // send server request for validation
        if (validate()) {
            login();
        }

    }

    public boolean validate() {

        boolean validation = false;
        View viewToFocus = null;
        if (!mEmail.getText().toString().isEmpty()) {
            if (!mPassword.getText().toString().isEmpty()) {
                validation = true;
            } else {
                viewToFocus = mPassword;
            }
        } else {
            viewToFocus = mEmail;
        }

        if (viewToFocus != null) {
            viewToFocus.requestFocus();
            makeSnackbar("Empty Fields !! ");
        }

        return validation;
    }

    public void makeSnackbar(String msg) {
        Snackbar.make(passwordIconText, msg, Snackbar.LENGTH_LONG).show();
    }

    public void login() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Authenticating You In ....");
        final String email = mEmail.getText().toString().trim();
        final String password = mPassword.getText().toString().trim();
        final String TAG = "login";


        final String url = Constants.SERVER_URL_LOGIN;

        StringRequest loginJsonRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    Log.d("LoginTest","resp "+response);
                        boolean succ = response.equals("login successful");
                        if (succ) {
                            SharedPreferenceManager utils = SharedPreferenceManager.getInstance(Login.this);
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
