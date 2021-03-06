package com.hunect.bungae;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Xml;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class Register extends Activity {

	private static final String CRYPTO_SEED_PASSWORD = "abcdefghijuklmno0123456789012345";
	
	public EditText idText;
	public TextView phoneText;
	public EditText ageText;
	public EditText introText;
	public Button idCheckButton;
	public RadioGroup rgGender;
	public RadioButton rbM;
	public RadioButton rbF;
	public CheckBox toaCheckBox;
	public ImageButton toaShowButton;
	public Button submitButton;
	
	public boolean isIdAvailable = false;
	
	public Map<String, String> userInfoBuffer;
	public StringBuffer idCheckBuffer, accountBuffer;
	public boolean parsingForIdCheck = false,
				   parsingForAccount = false;
	
	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		setTitle("계정 생성");
		
		idText = (EditText)findViewById(R.id.registerIdText);
		phoneText = (TextView)findViewById(R.id.registerPhoneNumTextView);
		ageText = (EditText)findViewById(R.id.registerAgeText);
		introText = (EditText)findViewById(R.id.registerIntroText);
		idCheckButton = (Button)findViewById(R.id.registerIdCheckButton);
		rgGender = (RadioGroup)findViewById(R.id.registerRadioGroup);
			rbM = (RadioButton)findViewById(R.id.registerRadioM);
			rbF = (RadioButton)findViewById(R.id.registerRadioF);
		toaCheckBox = (CheckBox)findViewById(R.id.registerTOACheckbox);
		toaShowButton = (ImageButton)findViewById(R.id.registerTOAButton);
		submitButton = (Button)findViewById(R.id.registerSubmitButton);
		
		//키보드가 올라와 있는 상태로 로드되는 것을 방지
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		//뷰에 표시할 폰 번호를 decrypt / 입력 초기화
		String phoneNumDecrypted = new String();
		try {
			phoneNumDecrypted = SimpleCrypto2.decrypt1(CRYPTO_SEED_PASSWORD, AuthInfoClass.getInstance().authInfo.get("auth_phone"));
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		phoneText.setText(phoneNumDecrypted);
		rbM.setChecked(false);
		rbF.setChecked(false);
		toaCheckBox.setChecked(false);
		
		//라디오버튼 동작
		rgGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				
				switch(checkedId) {
				case R.id.registerRadioM:
					rbF.setChecked(false);
//					Toast.makeText(Register.this, String.valueOf(rbM.isChecked())+String.valueOf(rbF.isChecked()), Toast.LENGTH_LONG).show();
					break;
				case R.id.registerRadioF:
					rbM.setChecked(false);
//					Toast.makeText(Register.this, String.valueOf(rbM.isChecked())+String.valueOf(rbF.isChecked()), Toast.LENGTH_LONG).show();
					break;
				default:
					break;
				}
				
			}
		});
		
		//idCheckButton 동작
		idCheckButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				idCheck1();
			}
		});
		
		//약관 열람 버튼 동작
		toaShowButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Intent intent = new Intent(Register.this, RegisterTOA.class);
				startActivity(intent);
			}
		});
		
		//계정생성 버튼 동작
		submitButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if(validEntries() && didCheckId() && toaCheckBox.isChecked()) {
					
					AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
					builder.setTitle("계정 생성")
					.setMessage("입력하신 정보로 계정을 생성하시겠습니까?")
					.setCancelable(true)
					.setNegativeButton("취소", null)
					.setPositiveButton("생성", new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int id) {
							
							createAccount1();
						}
					})
					.create().show();
					
					
				} //if(validEntriesCheck)
				
				else {
					if(!validEntries()) {
						
						if(ageText.getText().toString().equals("") || Integer.parseInt(ageText.getText().toString()) <15 ) {
							AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
							builder.setTitle("올바르지 않은 나이")
						    .setMessage("15세 이상으로 입력해 주세요.")
						    .setCancelable(true)
						    .setNeutralButton("확인", null)
						    .create().show();
						}
						
						else {
							String alertTitle = new String();
							if(idText.getText().toString().equals("")) alertTitle = "아이디";
							else if(!rbM.isChecked() && !rbF.isChecked()) alertTitle = "성별";
							else if(introText.getText().toString().equals("")) alertTitle = "자기소개";
							AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
							builder.setTitle(alertTitle)
						    .setMessage("입력하지 않으셨습니다.")
						    .setCancelable(true)
						    .setNeutralButton("확인", null)
						    .create().show();							
						}
						
					}
					
					else if(!didCheckId()) {
						AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
						builder.setTitle("아이디 중복 확인")
					    .setMessage("아이디 중복을 확인해 주세요.")
					    .setCancelable(true)
					    .setNeutralButton("확인", null)
					    .create().show();
					}
					
					else if(!toaCheckBox.isChecked()) {
						AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
						builder.setTitle("가입 약관")
					    .setMessage("가입 약관에 동의해 주세요.")
					    .setCancelable(true)
					    .setNeutralButton("확인", null)
					    .create().show();
					}
				} //else
				
			} //onClick
			
		}); //onClickListener
		
	}
	
	
	public boolean isOnline() {
	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    else return false;
	}

	public boolean validEntries() {
		if(idText.getText().toString().equals("") ||
			ageText.getText().toString().equals("") ||
			introText.getText().toString().equals("") ||
			(!rbM.isChecked() && !rbF.isChecked()))
			return false;
		else if (Integer.parseInt(ageText.getText().toString()) < 15)
			return true;
		/////////////////////////////////////////////////////////////////false!!!!!!/////////////////////////////////////
		else return true;
	}
	
	public boolean didCheckId() {
		if(!isIdAvailable) return false;
		
		else if(AuthInfoClass.getInstance().authInfo.get("auth_id")
				.equals(idText.getText().toString())
				&& isIdAvailable)
			return true;
		
		else {
			isIdAvailable = false;
			return false;
		}
	}
	
	public void idCheck1() {
		
		if(idText.getText().toString().length()<6) {
			AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
			builder.setTitle("잘못된 아이디")
			.setMessage("아이디는 알파벳과 숫자 조합 6자리 이상으로 선택해 주세요.")
			.setCancelable(true)
			.setNeutralButton("확인", null)
			.create().show();
		}
		else {
			idCheckBuffer = new StringBuffer();
			idCheckBuffer.append("input_id=").append(idText.getText().toString());
			
			parsingForIdCheck = true;
			parsingForAccount = false;
			RegisterParserTask task = new RegisterParserTask(Register.this);
			task.execute(BungaeActivity.MAIN_URL + "/auth_views_php/idCheck.php");
			
			//파싱 후속 처리는 postExecute()에서
			//idCheck2()
			
		}
	}
	
	public void idCheck2() {
		
		if(isIdAvailable) {
			AuthInfoClass.getInstance().authInfo.put("auth_id", idText.getText().toString());
			Toast.makeText(Register.this, "사용할 수 있는 아이디입니다.", Toast.LENGTH_LONG).show();
		}
		if(!isIdAvailable) {
			AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
			builder.setTitle("중복된 아이디")
			.setMessage("이미 해당 아이디가 사용중입니다. 다른 아이디를 이용해 주세요.")
			.setCancelable(true)
			.setNeutralButton("확인", null)
			.create().show();
		}
	}

	public void createAccount1() {
		//radiobutton에서 성별 읽음
		String gender = new String();
		if(rbM.isChecked() && !rbF.isChecked()) gender = "0";
		if(!rbM.isChecked() && rbF.isChecked()) gender = "1";
		
		//urlencoding
		String u_phone_encoded = new String();
		String u_intro_encoded = new String();
		try {
			u_phone_encoded = URLEncoder.encode(AuthInfoClass.getInstance().authInfo.get("auth_phone"), "UTF-8");
			u_intro_encoded = URLEncoder.encode(introText.getText().toString(), "UTF-8");
		} catch (UnsupportedEncodingException e1) {
		}
//		Toast.makeText(Register.this, "pid(AuthInfoClass) : "+AuthInfoClass.getInstance().authInfo.get("auth_pid"), Toast.LENGTH_LONG).show();
		//자료를 서버로 전송
		accountBuffer = new StringBuffer();
		accountBuffer.append("u_phone=").append(u_phone_encoded).append("&")
			  .append("u_id=").append(idText.getText().toString()).append("&")
			  .append("u_sex=").append(gender).append("&")
			  .append("u_age=").append(ageText.getText().toString()).append("&")
			  .append("u_intro=").append(u_intro_encoded).append("&")
			  .append("u_device_id=").append(AuthInfoClass.getInstance().authInfo.get("auth_udid")).append("&")
			  .append("u_push_id=").append(AuthInfoClass.getInstance().authInfo.get("auth_pid"));
		
		parsingForIdCheck = false;
		parsingForAccount = true;
		RegisterParserTask task = new RegisterParserTask(Register.this);
		task.execute(BungaeActivity.MAIN_URL + "/auth_views_php/register-android.php");
		
		//파싱 후 처리는 postExecute()에서
		//다시 해당 row를 파싱해 userInfoBuffer에 기록
		
		//createAccount2();
	}
	
	public void createAccount2() {
		//preference에 기록
		boolean didSuccessCommit = false;
		if(!userInfoBuffer.isEmpty()) {
			final String PREF_FILE_NAME = "UserInfo";
//			Toast.makeText(Register.this, "pid(userInfoBuffer) : "+userInfoBuffer.get("u_push_id"), Toast.LENGTH_LONG).show();
			SharedPreferences pref = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
			SharedPreferences.Editor prefEditor = pref.edit();
			prefEditor.putString("grow_old", "2012");
			prefEditor.putString("u_num", userInfoBuffer.get("u_num"));
			prefEditor.putString("u_id", userInfoBuffer.get("u_id"));
			prefEditor.putString("u_phone", userInfoBuffer.get("u_phone"));
			prefEditor.putString("u_sex", userInfoBuffer.get("u_sex"));
			prefEditor.putString("u_age", userInfoBuffer.get("u_age"));
			prefEditor.putString("u_intro", userInfoBuffer.get("u_intro"));
			prefEditor.putString("u_push_id", userInfoBuffer.get("u_push_id"));
			prefEditor.putString("u_push_state", "1");
			didSuccessCommit = prefEditor.commit();
		}
		
		//다른 액티비티에서 받아 쓸 class기록
		if(didSuccessCommit) {
			SharedPreferences pref = getSharedPreferences("UserInfo", MODE_PRIVATE);
			UserInfoClass.getInstance().userInfo = new HashMap<String, String>();
			UserInfoClass.getInstance().userInfo.put("u_num", pref.getString("u_num", null));
			UserInfoClass.getInstance().userInfo.put("u_id", pref.getString("u_id", null));
			UserInfoClass.getInstance().userInfo.put("u_phone", pref.getString("u_phone", null));
			UserInfoClass.getInstance().userInfo.put("u_sex", pref.getString("u_sex", null));
			UserInfoClass.getInstance().userInfo.put("u_age", pref.getString("u_age", null));
			UserInfoClass.getInstance().userInfo.put("u_intro", pref.getString("u_intro", null));
			UserInfoClass.getInstance().userInfo.put("u_push_id", pref.getString("u_push_id", null));
			UserInfoClass.getInstance().userInfo.put("u_push_state", pref.getString("u_push_state", null));
//			Toast.makeText(Register.this, UserInfoClass.getInstance().userInfo.get("u_id"), Toast.LENGTH_LONG).show();
		}
		
		//class 기록이 되면 가입이 정상적으로 이루어진것으로 간주
		if(!(UserInfoClass.getInstance().userInfo.get("u_id").equals(""))) {
			
			AlertDialog.Builder builder2 = new AlertDialog.Builder(Register.this);
			builder2.setTitle("계정 생성 완료")
			.setMessage("성공적으로 계정이 생성되었습니다. 메인으로 이동합니다.")
			.setCancelable(true)
			.setNeutralButton("확인", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int id) {
					
//					Toast.makeText(Register.this, "로그인 : "+UserInfoClass.getInstance().userInfo.get("u_id"), Toast.LENGTH_LONG).show();
					//메인 로드
					Intent intent = new Intent(Register.this, BungaeActivity.class);
					startActivity(intent);
					finish();
				}
			})
			.create().show();
		}
		else {
			AlertDialog.Builder builder2 = new AlertDialog.Builder(Register.this);
			builder2.setTitle("계정 생성 오류")
			.setMessage("계정 생성 중 오류가 발생했습니다. 다시 시도해 주세요.")
			.setCancelable(true)
			.setNeutralButton("확인", null)
			.create().show();
		}
	}

	
	public class RegisterParserTask extends AsyncTask<String, Integer, String>{  
    	private Register mActivity;    
    	private ProgressDialog mProgressDialog;   
    	
    	private String completeString;
    	
    	private boolean boolean_u_num = false,
						boolean_u_id = false,
						boolean_u_phone = false,
						boolean_u_sex = false,
						boolean_u_age = false,
						boolean_u_intro = false,
						boolean_u_push_id = false,
						boolean_idCheckParsingFlag = false;
    	
    	public RegisterParserTask(Register activity) {  
    		mActivity = activity;     
    	}

    	@Override    
    	protected void onPreExecute() {
    		
    		mProgressDialog = new ProgressDialog(mActivity);     
    		mProgressDialog.setMessage("전송중...");     
    		mProgressDialog.show();      
    	}
    	
    	@Override    
    	protected String doInBackground(String... params) {   
    		//BungaeListAdapter result = null;          
    		try {               
    			
    			URL url = new URL(params[0]);  
    			
    			 HttpURLConnection http = (HttpURLConnection) url.openConnection();   // 접속 
	            //-------------------------- 
	            //   전송 모드 설정 - 기본적인 설정이다 
	            //-------------------------- 
	            http.setDefaultUseCaches(false);                                            
	            http.setDoInput(true);                         // 서버에서 읽기 모드 지정 
	            http.setDoOutput(true);                       // 서버로 쓰기 모드 지정  
	            http.setRequestMethod("POST");         // 전송 방식은 POST 

	            // 서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다 
	            http.setRequestProperty("content-type", "application/x-www-form-urlencoded"); 
	            //-------------------------- 
	            //   서버로 값 전송 
	            //-------------------------- 
	            StringBuffer buffer = new StringBuffer();
	            if(parsingForIdCheck && !parsingForAccount) buffer = idCheckBuffer;
	            if(!parsingForIdCheck && parsingForAccount) buffer = accountBuffer;
	            
	            OutputStreamWriter outStream = new OutputStreamWriter(http.getOutputStream(), "utf-8");
	            PrintWriter writer = new PrintWriter(outStream);
	            writer.write(buffer.toString());
	            writer.flush();
    			
    	        InputStream is = http.getInputStream();
    			parseXml(is);          
    			} catch (Exception e) {      
    				e.printStackTrace();        
    				}          
    		  
    		return completeString;        
    		}       
    	
    	@Override
    	protected void onPostExecute(String completeString) {   
    		
    		mProgressDialog.dismiss();
    		
    		//파싱 결과 처리
    		if(parsingForIdCheck && !parsingForAccount) { idCheck2();		parsingForIdCheck = false; }
    		if(!parsingForIdCheck && parsingForAccount) { createAccount2();	parsingForAccount = false; }
   		}   
    	
    	
    	public String parseXml(InputStream is) throws IOException, XmlPullParserException {    
    		XmlPullParser parser = Xml.newPullParser();     
    		try {
    			parser.setInput(is, null);      
    			int eventType = parser.getEventType();
    			while (eventType != XmlPullParser.END_DOCUMENT) {   
    				String tag = null;     
    				switch (eventType) {    
    				case XmlPullParser.START_TAG:        
    					tag = parser.getName(); 
    					
    					if(parsingForIdCheck && !parsingForAccount) {
    						
        					if(tag.equals("found")) boolean_idCheckParsingFlag = true;
    					}
    					
    					if(!parsingForIdCheck && parsingForAccount) {
    						
    						if(tag.equals("user_info")) userInfoBuffer = new HashMap<String, String>();
    	  					
    	  					if(tag.equals("u_num"))		boolean_u_num = true;
    	  					if(tag.equals("u_id"))		boolean_u_id = true;
    	  					if(tag.equals("u_phone"))	boolean_u_phone = true;
    	  					if(tag.equals("u_sex"))		boolean_u_sex = true;
    	  					if(tag.equals("u_age"))		boolean_u_age = true;
    	  					if(tag.equals("u_intro"))	boolean_u_intro = true;
    	  					if(tag.equals("u_push_id"))	boolean_u_push_id = true;
    					}
    					
     					    						                                   
    					break;
    					
    				case XmlPullParser.TEXT:
    					
    					if(parsingForIdCheck && !parsingForAccount) {
    						if(boolean_idCheckParsingFlag){
    							String found = new String();
    	 						found = parser.getText();
    	 						if(found.equals("0")) isIdAvailable = true;
    	 						if(found.equals("1")) isIdAvailable = false;
    	 						boolean_idCheckParsingFlag = false;
         					}
    					}
    					
    					if(!parsingForIdCheck && parsingForAccount) {
    						
    						if(boolean_u_num){
    	  						userInfoBuffer.put("u_num", parser.getText());
    	  						boolean_u_num = false;
    	  					}
    	  					if(boolean_u_id){
    	  						userInfoBuffer.put("u_id", parser.getText());
    	  						boolean_u_id = false;
    	  					}
    	  					if(boolean_u_phone){
    	  						userInfoBuffer.put("u_phone", parser.getText());
    	  						boolean_u_phone = false;
    	  					}
    	  					if(boolean_u_sex){
    	  						userInfoBuffer.put("u_sex", parser.getText());
    	  						boolean_u_sex = false;
    	  					}
    	  					if(boolean_u_age){
    	  						userInfoBuffer.put("u_age", parser.getText());
    	  						boolean_u_age = false;
    	  					}
    	  					if(boolean_u_intro){
    	  						userInfoBuffer.put("u_intro", parser.getText());
    	  						boolean_u_intro = false;
    	  					}
    	  					if(boolean_u_push_id){
    	  						userInfoBuffer.put("u_push_id", parser.getText());
    	  						boolean_u_push_id = false;
    	  					}
    					}
    					
    					
    					break;
    					
    					
					case XmlPullParser.END_TAG:  
						
						tag = parser.getName();
						
						if(parsingForIdCheck && !parsingForAccount) {
							if (tag.equalsIgnoreCase("check")){
    							completeString = "complete";
    						}
    					}
    					
    					if(!parsingForIdCheck && parsingForAccount) {
    						if (tag.equalsIgnoreCase("user_info") && !userInfoBuffer.isEmpty()){
    							completeString = "complete";
    						}
    					}
						

						break;
    				} //switch
    				eventType = parser.next();           
    			} //while

    		} catch (Exception e) {     
    			e.printStackTrace();    
    		}
    		return completeString;
    	}
    }
	
}