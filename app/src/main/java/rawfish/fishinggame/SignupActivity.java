package rawfish.fishinggame;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;


public class SignupActivity extends Activity {

    private EditText IDEdit;
    private EditText passwordEdit;
    private EditText confirmPasswordEdit;
    private EditText emailEdit;
    private EditText phoneNumberEdit;
    private Button signupBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_activity);

        IDEdit = (EditText)findViewById(R.id.id);
        passwordEdit = (EditText)findViewById(R.id.password);
        confirmPasswordEdit = (EditText)findViewById(R.id.confirmPassword);
        emailEdit = (EditText)findViewById(R.id.email);
        phoneNumberEdit = (EditText)findViewById(R.id.phoneNumber);
        signupBtn = (Button)findViewById(R.id.signupBtn);

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ID = IDEdit.getText().toString();
                String password = passwordEdit.getText().toString();
                String email = emailEdit.getText().toString();
                String phoneNumber = phoneNumberEdit.getText().toString();

                if(ID.length()<4) {
                    Toast.makeText(SignupActivity.this, "ID는 4자리 이상 입력하세요", Toast.LENGTH_SHORT).show();
                } else if(!password.equals(confirmPasswordEdit.getText().toString())) {
                    Toast.makeText(SignupActivity.this, "비밀번호를 확인하세요", Toast.LENGTH_SHORT).show();
                } else if(password.length()<6) {
                    Toast.makeText(SignupActivity.this, "비밀번호는 6자리 이상을 입력하세요", Toast.LENGTH_SHORT).show();
                } else if(email.length()<9) {
                    Toast.makeText(SignupActivity.this, "이메일을 올바르게 입력하세요", Toast.LENGTH_SHORT).show();
                } else if(phoneNumber.length()<11) {
                    Toast.makeText(SignupActivity.this, "휴대전화 번호를 확인하세요", Toast.LENGTH_SHORT).show();
                } else {
                    //TODO 밑에 주석한줄만 진짜고 그 밑에꺼는 원래 없어야함
                    /*insertToDatabase(ID, password, email, phoneNumber);*/

                    ProgressDialog loading;
                    loading = ProgressDialog.show(SignupActivity.this, "Please Wait", null, true, true);
                    loading.dismiss();
                    Toast.makeText(getApplicationContext(), "회원가입을 환영합니다" ,Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                    startActivity(intent);
                }

            }
        });
    }

    private void insertToDatabase(String ID, String password, String email, String phoneNumber){

        class InsertData extends AsyncTask<String, Void, String> {
            private ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(SignupActivity.this, "Please Wait", null, true, true);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                Toast.makeText(getApplicationContext(), "회원가입을 환영합니다" ,Toast.LENGTH_LONG).show();

                Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                startActivity(intent);
            }

            @Override
            protected String doInBackground(String... params) {

                try{
                    String ID = (String)params[0];
                    String password = (String)params[1];
                    String email = (String)params[2];
                    String phoneNumber = (String)params[3];

                    String link="http://119.205.220.8/asd.php";
                    String data  = URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(ID, "UTF-8");
                    data += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");
                    data += "&" + URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8");
                    data += "&" + URLEncoder.encode("phoneNumber", "UTF-8") + "=" + URLEncoder.encode(phoneNumber, "UTF-8");

                    URL url = new URL(link);
                    URLConnection conn = url.openConnection();

                    conn.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                    wr.write( data );
                    wr.flush();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    // Read Server Response
                    while((line = reader.readLine()) != null)
                    {
                        sb.append(line);
                        break;
                    }
                    return sb.toString();
                }
                catch(Exception e){
                    return new String("Exception: " + e.getMessage());
                }

            }
        }

        InsertData task = new InsertData();
        task.execute(ID, password, email, phoneNumber);
    }
}
