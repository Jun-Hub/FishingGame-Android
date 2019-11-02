package rawfish.fishinggame;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {

    private EditText IDEdit;
    private EditText passwordEdit;
    private Button loginBtn, signupBtn;

    HttpPost httppost;
    HttpResponse response;
    HttpClient httpclient;
    List<NameValuePair> nameValuePairs;
    ProgressDialog dialog = null;

    CallbackManager callbackManager;
    private SessionCallback callback;      //콜백 선언

    String name; //페이스북 로그인할때 넘겨줄 이름정보

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        setContentView(R.layout.main_activity);

        IDEdit = (EditText)findViewById(R.id.IDEdit);
        passwordEdit = (EditText)findViewById(R.id.passwordEdit);
        loginBtn = (Button)findViewById(R.id.loginBtn);
        signupBtn = (Button)findViewById(R.id.signupBtn);

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = IDEdit.getText().toString();
                String password = passwordEdit.getText().toString();

                //new AsyncLogin().execute(email, password);

                dialog = ProgressDialog.show(MainActivity.this, "",  "로그인 중...", true);
                //로그인버튼 누르고 잠시 기다리는 동안 출력되는 다이얼로그
                new Thread(new Runnable() {
                    public void run() {
                        Looper.prepare();
                        login();
                        Looper.loop();
                    }
                }).start();

                Log.e(" "+email, " "+ password);
            }
        });

        callbackManager = CallbackManager.Factory.create();

        LoginButton facebookLoginBtn = (LoginButton)findViewById(R.id.facebookBtn);
        facebookLoginBtn.setReadPermissions(Arrays.asList("public_profile", "email"));
        facebookLoginBtn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.v("result",object.toString());

                        try {
                            name = object.getString("name");         // 이름

                            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                            intent.putExtra("loginWithWhat", 1);
                            intent.putExtra("name", name);
                            startActivity(intent);
                            finish();

                        }catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday");
                graphRequest.setParameters(parameters);
                graphRequest.executeAsync();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Log.e("LoginErr",error.toString());
            }
        });

        //카카오톡 로그인 구현
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    this.getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("test", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

        callback = new SessionCallback();                  // 이 두개의 함수 중요함
        Session.getCurrentSession().addCallback(callback);

    }

    //TODO 해당 주석들...
    void login(){   //자체로그인 구현
        try{
            /*httpclient=new DefaultHttpClient();
            httppost= new HttpPost("http://119.205.220.8/logincheck.php");

            nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("email", IDEdit.getText().toString()));
            nameValuePairs.add(new BasicNameValuePair("password", passwordEdit.getText().toString()));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            response=httpclient.execute(httppost);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            final String response = httpclient.execute(httppost, responseHandler);
            System.out.println("Response :" + response); //메시지 요청이 제대로 됬는지 확인용!
            runOnUiThread(new Runnable() {
                public void run() {
                    dialog.dismiss();
                }
            });

            if(response.equalsIgnoreCase("User Found")){
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this,"환영합니다", Toast.LENGTH_SHORT).show();
                        //로그인에 성공하면 토스트메시지 출력하고,
                    }
                });*/
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                intent.putExtra("loginWithWhat", 0);
                intent.putExtra("ID", IDEdit.getText().toString());
                startActivity(intent);
                finish();
                //로그인 성공시 다음 화면으로 넘어감!

            /*} else{
                Toast.makeText(MainActivity.this,"ID나 Password를 확인하세요", Toast.LENGTH_SHORT).show();
            }*/
        }catch(Exception e){
            dialog.dismiss();
            System.out.println("Exception : " + e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Session.getCurrentSession().removeCallback(callback);
    }

    private class SessionCallback implements ISessionCallback {

        @Override
        public void onSessionOpened() {
            redirectSignupActivity();  // 세션 연결성공 시 redirectSignupActivity() 호출
            Log.d("test", "세션연결 성공!! ");
        }

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            if(exception != null) {
                Log.d("test", "세션연결 실패했네.. ㅠㅠ");
                Logger.e(exception);
            }
            setContentView(R.layout.main_activity); // 세션 연결이 실패했을때
        }                                            // 로그인화면을 다시 불러옴
    }

    protected void redirectSignupActivity() {       //세션 연결 성공 시 SignupActivity로 넘김
        final Intent intent = new Intent(this, KakaoSignupActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }
}