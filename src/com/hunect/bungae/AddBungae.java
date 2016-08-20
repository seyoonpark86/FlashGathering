package com.hunect.bungae;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import net.daum.mobilead.AdConfig;
import net.daum.mobilead.AdHttpListener;
import net.daum.mobilead.MobileAdView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class AddBungae extends Activity implements OnClickListener, AdHttpListener {
	
	//AddBungae.java

	private ProgressDialog mProgressDialog;
	
	private MobileAdView adView = null;
	
	private static final String CATEGORY_URL = BungaeActivity.MAIN_URL + "/category.php";
	private static final String BLOCK_USER_URL = BungaeActivity.MAIN_URL + "/findblock.php";
	private static final String PRESENT_BUNGAE_URL = BungaeActivity.MAIN_URL + "/presentmybungae.php";
	private static final String ADD_BUNGAE_URL = BungaeActivity.MAIN_URL + "/addbungae-r.php";
	
	
	int categoryChecked = 0;
	
	private TextView categoryEdit;
	private Button categoryButton;
	private EditText titleEdit;
	private Button timeButton;
	private TextView timeEdit;
	private Button mapButton;
	private EditText contentEdit;
	private TextView hostEdit;
	private TextView locationEdit;
	
	private TextView minNumView;
	private ImageButton minMinusButton;
	private ImageButton minPlusButton;
	
	private TextView maxNumView;
	private ImageButton maxMinusButton;
	private ImageButton maxPlusButton;
	
	private RadioButton openRadioButton;
	private RadioButton privateRadioButton;
	private TextView password;
	
	private Button createButton;
	
	private SimpleDateFormat dateFormat;
	private Date bungaeDateTime;
	
	private String bungaeTime;
	
	int maxNum;
	int minNum;
	int openPrivateFlag = 0;
	
	int cannotCreateFlag;
	
	CategoryData categoryString = null;
	ArrayList<CategoryData> categoryList = null;
	
	BlockUserData blockUserData = null;
	ArrayList<BlockUserData> blockUserList = null;
	
	PresentBungaeData presentBungaeData = null;
	ArrayList<PresentBungaeData> presentBungaeList = null;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.addbungae);
	    // TODO Auto-generated method stub
	    
	    initAdam();
	    
	    categoryEdit = (TextView)findViewById(R.id.categoryEdit);
	    categoryButton = (Button)findViewById(R.id.addCategoryButton);
	    titleEdit = (EditText)findViewById(R.id.titleEdit);
	    timeButton = (Button)findViewById(R.id.addTimeButton);
	    timeEdit = (TextView)findViewById(R.id.timeLabel);
	    mapButton = (Button)findViewById(R.id.addMapButton);
	    contentEdit = (EditText)findViewById(R.id.contentEdit);
	    hostEdit = (TextView)findViewById(R.id.hostLabel);
	    locationEdit = (TextView)findViewById(R.id.locationLabel);
	    
	    minNumView = (TextView)findViewById(R.id.minNumLabel);
	    minMinusButton = (ImageButton)findViewById(R.id.minMinusButton);
	    minPlusButton = (ImageButton)findViewById(R.id.minPlusButton);
	    
	    maxNumView = (TextView)findViewById(R.id.maxNumLabel);
	    maxMinusButton = (ImageButton)findViewById(R.id.maxMinusButton);
	    maxPlusButton = (ImageButton)findViewById(R.id.maxPlusButton);
	    
	    openRadioButton = (RadioButton)findViewById(R.id.openButton);
	    privateRadioButton = (RadioButton)findViewById(R.id.privateButton);
	    password = (TextView)findViewById(R.id.password);
	    
	    createButton = (Button)findViewById(R.id.createButton);
	    
	    categoryButton.setOnClickListener(this);
	    timeButton.setOnClickListener(this);
	    mapButton.setOnClickListener(this);
	    minMinusButton.setOnClickListener(this);
	    minPlusButton.setOnClickListener(this);
	    maxMinusButton.setOnClickListener(this);
	    maxPlusButton.setOnClickListener(this);
	    openRadioButton.setOnClickListener(this);
	    privateRadioButton.setOnClickListener(this);
	    createButton.setOnClickListener(this);
	    
	    categoryList = new ArrayList<CategoryData>();
	    blockUserList = new ArrayList<BlockUserData>();
	    presentBungaeList = new ArrayList<PresentBungaeData>();
	    
	    dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    
	    bungaeTime = new String();
	    
	    // AddBungaeMapClass 선언
	 	AddBungaeMapClass.getInstance().addBungaeMapInfo = new HashMap<String, String>();
	 	AddBungaeMapClass.getInstance().addBungaeMapInfo.put("locationTitle", "");
	 	AddBungaeMapClass.getInstance().addBungaeMapInfo.put("longitude", "");
	 	AddBungaeMapClass.getInstance().addBungaeMapInfo.put("latitude", "");
	    
	    //키보드가 올라와 있는 상태로 로드되는 것을 방지
	    this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	    
	    //       Parsing Start!!!
	    CategoryParserTask task = new CategoryParserTask(this);   
		task.execute(CATEGORY_URL);
		
		//		주최자 이름 불러오기
		hostEdit.setText(UserInfoClass.getInstance().userInfo.get("u_id"));
		//locationEdit.setText(AddBungaeMapClass.getInstance().addBungaeMapInfo.get("locationTitle"));
		
	}
	
	@Override
	public void onDestroy() {
        super.onDestroy();     
        
        if( adView != null ) {
        	adView.destroy();
            adView = null;
        }
    }
	
	
	@SuppressWarnings("deprecation")
	private void initAdam() {
		
        // 할당 받은 clientId 설정
        AdConfig.setClientId("1c3aZ5ZT134b0fed431");
        
        // Ad@m sdk 초기화 시작
        adView = (MobileAdView)findViewById(R.id.adview);
        adView.setRequestInterval(15);
    	adView.setAdListener(this);
       	adView.setVisibility(View.VISIBLE);
	}
   
	@Override
	public void failedDownloadAd_AdListener(int errorno, String errMsg) {
		// fail to receive Ad
		Log.d("AdSample", errorno +":"+ errMsg);
		
	}

	
	@Override
	public void didDownloadAd_AdListener() {
	   // success to receive Ad
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		locationEdit.setText(AddBungaeMapClass.getInstance().addBungaeMapInfo.get("locationTitle"));
	}
	
	///////////////////////////////////////////
	//// Button Clicked!!                  ////
	///////////////////////////////////////////
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){

		case R.id.addCategoryButton:
			categoryDialog();
			break;
			
		case R.id.addTimeButton:
			timePickerDialog();
			break;
			
		case R.id.addMapButton:
			Intent intent3 = new Intent(this, AddBungaeMap.class); 
			startActivity(intent3);
			break;
			
		case R.id.minMinusButton:
			minMinusButtonClicked();
			break;
			
		case R.id.minPlusButton:
			minPlusButtonClicked();
			break;
			
		case R.id.maxMinusButton:
			maxMinusButtonClicked();
			break;
			
		case R.id.maxPlusButton:
			maxPlusButtonClicked();
			break;
			
		case R.id.openButton:
			openButtonClicked();
			break;
		
		case R.id.privateButton:
			privateButtonClicked();
			break;
			
		case R.id.createButton:
			createButtonClicked();
			break;
		}
	}
	
	///////////////////////////////////////////
	//// 기타 함수들 집합                     ////
	///////////////////////////////////////////
	
	// 최대 최소 인원 버
	
	public void minMinusButtonClicked()
	{
		String minString = (String) minNumView.getText();
		
		// 5, 6, 7, 8, 9, 10 일때는 -1
		if ( (minNumView.getText().equals("5")) || (minNumView.getText().equals("6")) || (minNumView.getText().equals("7")) || 
				(minNumView.getText().equals("8")) || (minNumView.getText().equals("9")) || (minNumView.getText().equals("10")) )
		{
			minNumView.setText(String.format("%d", Integer.parseInt(minString)-1));
		}
	}
	
	public void minPlusButtonClicked()
	{
		String minString = (String) minNumView.getText();
		
		// 4, 5, 6, 7, 8, 9 일때는 +1
		if ( (minNumView.getText().equals("4")) || (minNumView.getText().equals("5")) || (minNumView.getText().equals("6")) || 
				(minNumView.getText().equals("7")) || (minNumView.getText().equals("8")) || (minNumView.getText().equals("9")) )
		{
			minNumView.setText(String.format("%d", Integer.parseInt(minString)+1));
		}
	}
	
	public void maxMinusButtonClicked()
	{
		String maxString = (String) maxNumView.getText();
		
		// 5, 6, 7, 8, 9, 10 일때는 -1
		if ( (maxNumView.getText().equals("5")) || (maxNumView.getText().equals("6")) || (maxNumView.getText().equals("7")) || 
				(maxNumView.getText().equals("8")) || (maxNumView.getText().equals("9")) || (maxNumView.getText().equals("10")) )
		{
			maxNumView.setText(String.format("%d", Integer.parseInt(maxString)-1));
		}
		// 무제한 일때는 10으로
		else if ( maxNumView.getText().equals("무제한") )
		{
			maxNumView.setText("10");
		}
	}
	
	public void maxPlusButtonClicked()
	{
		String maxString = (String) maxNumView.getText();
		
		// 4, 5, 6, 7, 8, 9 일때는 +1
		if ( (maxNumView.getText().equals("4")) || (maxNumView.getText().equals("5")) || (maxNumView.getText().equals("6")) || 
				(maxNumView.getText().equals("7")) || (maxNumView.getText().equals("8")) || (maxNumView.getText().equals("9")) )
		{
			maxNumView.setText(String.format("%d", Integer.parseInt(maxString)+1));
		}
		// 10 일때는 무제한으
		else if ( maxNumView.getText().equals("10") )
		{
			maxNumView.setText("무제한");
		}
	}
	
	// 공개 비공개 버튼 관련
	
	public void openButtonClicked()
	{
		//Toast.makeText(AddBungae.this, "open Clicked!!", 0).show();
		openPrivateFlag = 0;
		password.setText("");
	}
	
	public void privateButtonClicked()
	{
		//Toast.makeText(AddBungae.this, "private Clicked!!", 0).show();
		openPrivateFlag = 1;
		passwordEnter();
	}

	// 번개 생성 버튼 관련
	
	public void createButtonClicked()
	{
		// 최대인원, 최소인원을 받고, 서버에 무제한을 11로 저장하기 위해서 maxNum에 최대 인원을 기록하는 단계  
		minNum = Integer.parseInt((String)minNumView.getText());
		
		if ( maxNumView.getText().equals("무제한") )
			maxNum = 11;
		else
			maxNum = Integer.parseInt((String)maxNumView.getText());
		
		if ( minNum > maxNum )
		{
			minMaxErrorDialog();
		}
		
		else
		{
			// minNum > maxNum의 else
			
			if ( !(categoryEdit.getText().equals("")) && !(titleEdit.getText().toString().equals("")) && !(hostEdit.getText().equals("")) 
					&& !(timeEdit.getText().equals("")) && !(locationEdit.getText().equals("")) && !(contentEdit.getText().toString().equals(""))
					&& openRadioButton.isChecked() )
			{
				BlockUserParserTask task = new BlockUserParserTask(this);   
				task.execute(BLOCK_USER_URL);
			}
			
			else if ( !(categoryEdit.getText().equals("")) && !(titleEdit.getText().toString().equals("")) && !(hostEdit.getText().equals("")) 
					&& !(timeEdit.getText().equals("")) && !(locationEdit.getText().equals("")) && !(contentEdit.getText().toString().equals(""))
					&& privateRadioButton.isChecked() && (password.length() == 4) )
			{
				BlockUserParserTask task = new BlockUserParserTask(this);   
				task.execute(BLOCK_USER_URL);
			}
			
			else
			{
				//카테고리 입력 경고
				if ( categoryEdit.getText().equals("") )
				{
					categoryAlert();
				}
				
				//제목 입력 경고
				else if ( titleEdit.getText().toString().equals("") )
				{
					titleAlert();
				}
				//시간 입력 경고
				else if ( timeEdit.getText().equals("") )
				{
					timeAlert();
				}
				//장소 입력 경고
				else if ( locationEdit.getText().equals("") )
				{
					locationAlert();
				}
				//내용 입력 경고
				else if ( contentEdit.getText().toString().equals("") )
				{
					contentAlert();
				}
				//비밀번호 입력 경고
				else if ( !(password.length() == 4) )
				{
					passwordAlert();
				}
			}
		}
		
	}
	
	///////////////////////////////////////////
	//// Dialog load!!                     ////
	///////////////////////////////////////////
	
	// Category Dialog 부분 입니다.
	
	private void categoryDialog()
	{	
		final String[] categoryArray;
		categoryArray = new String[categoryList.size()];
		for ( int i = 0 ; i < categoryList.size() ; i++)
		{
			categoryArray[i] = categoryList.get(i).getCategory();
		}
		
		AlertDialog.Builder alert = new AlertDialog.Builder(AddBungae.this);
		alert.setTitle("분류를 선택하세요");
		alert.setSingleChoiceItems(categoryArray, categoryChecked, new DialogInterface.OnClickListener() 
		{
			
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				categoryChecked = which;
				
			}
		});
		alert.setPositiveButton("확인", new DialogInterface.OnClickListener() 
		{
			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				categoryEdit.setText(categoryArray[categoryChecked]);
			}
		});
		alert.setNegativeButton("취소", null);
		alert.show();
	}
	
	// TimePicker Dialog 부분 입니다.
	
	private void timePickerDialog()
	{
		LayoutInflater inflator = LayoutInflater.from(this);
    	View addView = inflator.inflate(R.layout.timepicker_dialog, null);
    	
    	final TimePicker timePicker;
    	final RadioButton radioButtonToday;
    	//final RadioButton radioButtonTommorow;
    	final Calendar myCalendar;
    	
    	
    	//DateFormat fmtDateAndTime = DateFormat.getDateTimeInstance();
    	
    	timePicker = (TimePicker)addView.findViewById(R.id.timePicker);
    	radioButtonToday = (RadioButton)addView.findViewById(R.id.todayButton);
    	//radioButtonTommorow = (RadioButton)addView.findViewById(R.id.tommorowButton);
    	myCalendar = Calendar.getInstance();
    	
    	AlertDialog.Builder alert2 =  new AlertDialog.Builder(this);
    	alert2.setTitle("번개 시간");
    	alert2.setView(addView);
    	
    	alert2.setPositiveButton("확인", new DialogInterface.OnClickListener() 
    	{	
    		
			@Override
			public void onClick(DialogInterface dialog, int whichButton) 
			{
				myCalendar.add(Calendar.MONTH,+1);
				//라디오 버튼이 내일에 checked되어 있으면 날짜에 하루를 더해준다.
				if ( !(radioButtonToday.isChecked()) )
				{
					myCalendar.add(Calendar.DATE,+1);
				}
				
				String timeString;
				
				int year = myCalendar.get(Calendar.YEAR);
				int month = myCalendar.get(Calendar.MONTH);
				int day = myCalendar.get(Calendar.DAY_OF_MONTH);
				
				int hour = timePicker.getCurrentHour();
				int minute = timePicker.getCurrentMinute();
				
				if ( month < 10 && day < 10 )
					timeString = String.format("%d-0%d-0%d", year, month, day);
				else if ( month < 0 && day >= 10 )
					timeString = String.format("%d-0%d-%d", year, month, day);
				else if ( month >= 0 && day < 10 )
					timeString = String.format("%d-%d-0%d", year, month, day);
				else
					timeString = String.format("%d-%d-%d", year, month, day);
					
				if ( hour < 10 && minute < 10 )
					timeString = timeString + String.format(" 0%d:0%d:00", hour, minute);
				else if ( hour < 10 && minute >= 10 )
					timeString = timeString + String.format(" 0%d:%d:00", hour, minute);
				else if ( hour >= 10 && minute < 10 )
					timeString = timeString + String.format(" %d:0%d:00", hour, minute);
				else
					timeString = timeString + String.format(" %d:%d:00", hour, minute);
				
				
				bungaeTime = timeString;
				
				try {
					bungaeDateTime = dateFormat.parse(bungaeTime);
				} catch (ParseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				//현재 시간을 생성
				Date nowDate = new Date();
				
				//현재시간과 선택한 시간간의 차이를 계산
				long difference = (bungaeDateTime.getTime()-nowDate.getTime())/1000;
				
				if (difference<2*60*60)
				{
					// 현재시간보다 2시간 내의 시간을 선택해서 시간을 다시 정하도록 설정.
					timePickerDialog();
					Toast.makeText(AddBungae.this, "현재시간보다 2시간 이후의 번개를 생성해 주세요", Toast.LENGTH_LONG).show();
				}
				
				else
				{
					// 선택한 시간을 뷰에 전달
					//timeEdit.setText(bungaeTime);
					timeEdit.setText((bungaeDateTime.getMonth()+1)+"월"+bungaeDateTime.getDate()+"일"+" "+bungaeDateTime.getHours()+"시"+bungaeDateTime.getMinutes()+"분");
				}
				
				
			}
		});
    	alert2.setNegativeButton("취소", new DialogInterface.OnClickListener() 
		{
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		});
    	alert2.show();
    }
	
	// Password Dialog 부분 입니다.
	
	private void passwordEnter()
	{
		LayoutInflater inflator = LayoutInflater.from(this);
    	View addView2 = inflator.inflate(R.layout.password_dialog, null);
    	
    	final EditText passwordEdit;
    	
    	passwordEdit = (EditText)addView2.findViewById(R.id.passwordEdit);
    	
    	AlertDialog.Builder alert3 =  new AlertDialog.Builder(this);
    	alert3.setTitle("비밀번호 입력");
    	alert3.setView(addView2);
    	
    	alert3.setPositiveButton("확인", new DialogInterface.OnClickListener() 
    	{	
    		
			@Override
			public void onClick(DialogInterface dialog, int whichButton) 
			{
				if ( passwordEdit.length() == 4 )
					password.setText(passwordEdit.getText());					
				else
				{
					passwordEnter();
					Toast.makeText(AddBungae.this, "비밀번호를 4자리 입력하세요!!", 0).show();
				}
			}
		});
    	alert3.setNegativeButton("취소", new DialogInterface.OnClickListener() 
		{
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				privateRadioButton.setChecked(false);
				openRadioButton.setChecked(true);
				openButtonClicked();
			}
		});
    	alert3.show();
	}
	
	// 최대, 최소 인원 오류 Dialog
	
	private void minMaxErrorDialog()
	{
		LayoutInflater inflator = LayoutInflater.from(this);
    	View addView = inflator.inflate(R.layout.minmax_dialog, null);
    	
    	AlertDialog.Builder alert =  new AlertDialog.Builder(this);
    	alert.setTitle("인원설정 오류");
    	alert.setView(addView);
    	
    	alert.setNeutralButton("확인", new DialogInterface.OnClickListener() 
    	{	
    		
			@Override
			public void onClick(DialogInterface dialog, int whichButton) 
			{
				
			}
		});
    	
    	alert.show();
	}
	
	// Blocked User Alert Dialog
	
	private void blockedUserDialog()
	{
		LayoutInflater inflator = LayoutInflater.from(this);
    	View addView = inflator.inflate(R.layout.alert_dialog, null);
    	
    	TextView alertMessage;
    	alertMessage = (TextView)addView.findViewById(R.id.alertEdit);
    	alertMessage.setText("다수의 신고에 의해 차단된 사용자 입니다.");
    	
    	AlertDialog.Builder alert =  new AlertDialog.Builder(this);
    	alert.setTitle("차단된 계정");
    	alert.setView(addView);
    	
    	alert.setNeutralButton("확인", new DialogInterface.OnClickListener() 
    	{	
    		
			@Override
			public void onClick(DialogInterface dialog, int whichButton) 
			{
				
			}
		});
    	
    	alert.show();
	}
	
	// Cannot Create Alert Dialog
	
	private void cannotCreateDialog()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(AddBungae.this);
		builder.setTitle("만들기 실패")
	    .setMessage("참여중인 다른 번개와 시간이 가깝습니다. 다른 번개와 최소 8시간 이상 떨어진 번개를 만드실 수 있습니다.")
	    .setCancelable(true)
	    .setNeutralButton("확인", null)
	    .create().show();
		
	}
	
	// Category Error Dialog
	
	private void categoryAlert()
	{
		LayoutInflater inflator = LayoutInflater.from(this);
    	View addView = inflator.inflate(R.layout.alert_dialog, null);
    	
    	TextView alertMessage;
    	alertMessage = (TextView)addView.findViewById(R.id.alertEdit);
    	alertMessage.setText("번개 분류를 선택해 주세요.");
    	
    	AlertDialog.Builder alert =  new AlertDialog.Builder(this);
    	alert.setTitle("번개 분류");
    	alert.setView(addView);
    	
    	alert.setNeutralButton("확인", new DialogInterface.OnClickListener() 
    	{	
    		
			@Override
			public void onClick(DialogInterface dialog, int whichButton) 
			{
				
			}
		});
    	
    	alert.show();
	}
	
	// Title Error Dialog
	
	private void titleAlert()
	{
		LayoutInflater inflator = LayoutInflater.from(this);
    	View addView = inflator.inflate(R.layout.alert_dialog, null);
    	
    	TextView alertMessage;
    	alertMessage = (TextView)addView.findViewById(R.id.alertEdit);
    	alertMessage.setText("번개 제목을 입력해 주세요.");
    	
    	AlertDialog.Builder alert =  new AlertDialog.Builder(this);
    	alert.setTitle("번개 제목");
    	alert.setView(addView);
    	
    	alert.setNeutralButton("확인", new DialogInterface.OnClickListener() 
    	{	
    		
			@Override
			public void onClick(DialogInterface dialog, int whichButton) 
			{
				
			}
		});
    	
    	alert.show();
	}
	
	// Time Error Dialog
	
	private void timeAlert()
	{
		LayoutInflater inflator = LayoutInflater.from(this);
    	View addView = inflator.inflate(R.layout.alert_dialog, null);
    	
    	TextView alertMessage;
    	alertMessage = (TextView)addView.findViewById(R.id.alertEdit);
    	alertMessage.setText("번개 시간을 입력해 주세요.");
    	
    	AlertDialog.Builder alert =  new AlertDialog.Builder(this);
    	alert.setTitle("번개 시간");
    	alert.setView(addView);
    	
    	alert.setNeutralButton("확인", new DialogInterface.OnClickListener() 
    	{	
    		
			@Override
			public void onClick(DialogInterface dialog, int whichButton) 
			{
				
			}
		});
    	
    	alert.show();
	}
	
	// Location Error Dialog
	
	private void locationAlert()
	{
		LayoutInflater inflator = LayoutInflater.from(this);
    	View addView = inflator.inflate(R.layout.alert_dialog, null);
    	
    	TextView alertMessage;
    	alertMessage = (TextView)addView.findViewById(R.id.alertEdit);
    	alertMessage.setText("번개 장소를 입력해 주세요.");
    	
    	AlertDialog.Builder alert =  new AlertDialog.Builder(this);
    	alert.setTitle("번개 장소");
    	alert.setView(addView);
    	
    	alert.setNeutralButton("확인", new DialogInterface.OnClickListener() 
    	{	
    		
			@Override
			public void onClick(DialogInterface dialog, int whichButton) 
			{
				
			}
		});
    	
    	alert.show();
	}
	
	// Content Error Dialog
	
	private void contentAlert()
	{
		LayoutInflater inflator = LayoutInflater.from(this);
    	View addView = inflator.inflate(R.layout.alert_dialog, null);
    	
    	TextView alertMessage;
    	alertMessage = (TextView)addView.findViewById(R.id.alertEdit);
    	alertMessage.setText("번개 내용을 입력해 주세요.");
    	
    	AlertDialog.Builder alert =  new AlertDialog.Builder(this);
    	alert.setTitle("번개 내용");
    	alert.setView(addView);
    	
    	alert.setNeutralButton("확인", new DialogInterface.OnClickListener() 
    	{	
    		
			@Override
			public void onClick(DialogInterface dialog, int whichButton) 
			{
				
			}
		});
    	
    	alert.show();
	}
	
	// Password Error Dialog
	
	private void passwordAlert()
	{
		LayoutInflater inflator = LayoutInflater.from(this);
    	View addView = inflator.inflate(R.layout.alert_dialog, null);
    	
    	TextView alertMessage;
    	alertMessage = (TextView)addView.findViewById(R.id.alertEdit);
    	alertMessage.setText("비밀번호를 4자리 입력하세요.");
    	
    	AlertDialog.Builder alert =  new AlertDialog.Builder(this);
    	alert.setTitle("비밀번호");
    	alert.setView(addView);
    	
    	alert.setNeutralButton("확인", new DialogInterface.OnClickListener() 
    	{	
    		
			@Override
			public void onClick(DialogInterface dialog, int whichButton) 
			{
				
			}
		});
    	
    	alert.show();
	}
	
	
	
	///////////////////////////////////////////
	//// Parsing Start                     ////
	///////////////////////////////////////////
	
	// Category Parsing Start!!
	
	public class CategoryParserTask extends AsyncTask<String, Integer, String>
	{  
    	private AddBungae mActivity;    
    	private ProgressDialog mProgressDialog;   
    	
    	private String completeString;
    	
    	boolean boolean_b_category = false;
    	
    	public CategoryParserTask(AddBungae activity) {  
    		mActivity = activity;     
    		}       
    	  
    	@Override    
    	protected void onPreExecute() { 
    		
    		mProgressDialog = new ProgressDialog(mActivity);     
    		mProgressDialog.setMessage("로딩중...");     
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
    	            http.setDoOutput(false);                       // 서버로 쓰기 모드 지정  
    	            http.setRequestMethod("POST");         // 전송 방식은 POST 

    	            // 서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다 
    	            http.setRequestProperty("content-type", "application/x-www-form-urlencoded"); 
    	            //-------------------------- 
    	            //   서버로 값 전송 
    	            //-------------------------- 
    	            //StringBuffer buffer = new StringBuffer(); 
    	            //buffer.append("num").append("=").append(NumStr); 
    	           
    	            //OutputStreamWriter outStream = new OutputStreamWriter(http.getOutputStream(), "utf-8"); 
    	            //PrintWriter writer = new PrintWriter(outStream); 
    	            //writer.write(buffer.toString()); 
    	            //writer.flush(); 
    			
    	          
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

           //Toast.makeText(AddBungae.this, "전송 후 결과 받음", 0).show(); 
    		
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
    					
    					if(tag.equals("category"))
    						categoryString = new CategoryData();
    					if(tag.equals("b_category"))
    						boolean_b_category = true;   
    						                                   
    					break;           
    					
    				case XmlPullParser.TEXT:
    					if(boolean_b_category){
    						categoryString.setCategory(parser.getText());
    						//Log.d("ParserTest", "파싱을 잘 받아왔어요!! 이거봐요~ "+ categoryString.getCategory());
    						boolean_b_category = false;
    					}
    					
    					break;
    					
    					
    					case XmlPullParser.END_TAG:  
    						
    						tag = parser.getName();
    						if (tag.equalsIgnoreCase("category") && categoryString != null){
    							categoryList.add(categoryString);
    							completeString = "complete";
    						}

    						break;                       
    						}                         
    				eventType = parser.next();           
    				}     

    			
    			
    			} catch (Exception e) {     
    				e.printStackTrace();    
    				}          
    		
    		return completeString;
    	} 
    	
    }

	// Block User Parsing Start!!
	
	public class BlockUserParserTask extends AsyncTask<String, Integer, String>
	{  
    	private AddBungae mActivity;    
//    	private ProgressDialog mProgressDialog;
    	
    	private String completeString;
    	
    	boolean boolean_bl_num = false,
    			boolean_bl_id = false,
    			boolean_bl_end_time = false;
    	
    	public BlockUserParserTask(AddBungae activity) {  
    		mActivity = activity;     
    		}       
    	  
    	@Override    
    	protected void onPreExecute() { 
    		
    		if ( blockUserList.size() != 0 )
    			blockUserList.clear();
    		
    		
    		mProgressDialog = new ProgressDialog(mActivity);     
    		mProgressDialog.setMessage("처리중...");     
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
    	            buffer.append("id").append("=").append(UserInfoClass.getInstance().userInfo.get("u_id")); 
    	           
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
			
    		
    		
//    		mProgressDialog.dismiss();      

           //Toast.makeText(AddBungae.this, blockUserList.get(0).getBlockId(), 0).show();
    		
    		if ( blockUserList.size() == 0 )
    		{
    			// 블럭당한 사용자가 아니기 때문에 현재 참여한 번개가 있는지 확인하는 파싱 실행
    			PresentBungaeParserTask task = new PresentBungaeParserTask();   
    			task.execute(PRESENT_BUNGAE_URL);
    		}
    		else
    		{
    			mProgressDialog.dismiss();
    			// 블럭당한 사용자이니 블럭당했다고 번개를 생성할 수 없다는 경고 메세지를 날린다.
    			blockedUserDialog();
    			
    		}
    		
    	}
    	
    	
    	public String parseXml(InputStream is) throws IOException, XmlPullParserException {    
    		XmlPullParser parser = Xml.newPullParser();     
    		try {
    			
    			parser.setInput(is, null);      
    			int eventType = parser.getEventType();      
    			BlockUserData blockUserData = null;
    			while (eventType != XmlPullParser.END_DOCUMENT) {   
    				String tag = null;     
    				switch (eventType) {    
    				case XmlPullParser.START_TAG:        
    					tag = parser.getName(); 
    					
    					if(tag.equals("bungae"))
    						blockUserData = new BlockUserData();
    					if(tag.equals("bl_num"))
    						boolean_bl_num = true;
    					if(tag.equals("bl_id"))
    						boolean_bl_id = true;
    					if(tag.equals("bl_end_time"))
    						boolean_bl_end_time = true;
    						                                   
    					break;           
    					
    				case XmlPullParser.TEXT:
    					if(boolean_bl_num){
    						blockUserData.setBlockNum(parser.getText());
    						boolean_bl_num = false;
    					}
    					if(boolean_bl_id){
    						blockUserData.setBlockId(parser.getText());
    						boolean_bl_id = false;
    					}
    					if(boolean_bl_end_time){
    						blockUserData.setBlockEndTime(parser.getText());
    						boolean_bl_end_time = false;
    					}
    					
    					break;
    					
    					
    					case XmlPullParser.END_TAG:  
    						
    						tag = parser.getName();
    						if (tag.equalsIgnoreCase("bungae") && blockUserData != null){
    							blockUserList.add(blockUserData);
    							completeString = "complete";
    						}
    						else if ( tag.equalsIgnoreCase("nowbungae") )
    							completeString = "complete";
    							
    						break;                       
    						}                         
    				eventType = parser.next();           
    				}     

    			
    			
    			} catch (Exception e) {     
    				e.printStackTrace();    
    				}          
    		
    		return completeString;
    	} 
    	
    }

	// Present Bungae Parsing Start!!
	
	public class PresentBungaeParserTask extends AsyncTask<String, Integer, String>
	{  
//    	private AddBungae mActivity;    
//    	private ProgressDialog mProgressDialog;   
    	
    	private String completeString;
    	
    	boolean boolean_b_num = false,
    			boolean_b_title = false,
    			boolean_b_time = false;
    	
//    	public PresentBungaeParserTask(AddBungae activity) {  
//    		mActivity = activity;     
//    		}       
    	  
    	@Override    
    	protected void onPreExecute() { 
    		
    		if ( presentBungaeList.size() != 0 )
    			presentBungaeList.clear();
    		
    		cannotCreateFlag = 0;
    		
//    		mProgressDialog = new ProgressDialog(mActivity);     
//    		mProgressDialog.setMessage("로딩중...");     
//    		mProgressDialog.show();      
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
    	            buffer.append("id").append("=").append(UserInfoClass.getInstance().userInfo.get("u_id")); 
    	           
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
    		
//    		mProgressDialog.dismiss();
    		
    		if ( presentBungaeList.size() == 0 )
    		{
    			// 참여중인 번개가 없을 때 번개 생성을 시작 한다.
    			//Toast.makeText(AddBungae.this, "현재 참여한 번개 없이 번개생성시작!!", 0).show();
    			
    			// Parsing Start!!!
    		    CreateBungaeTask task = new CreateBungaeTask();   
    			task.execute(ADD_BUNGAE_URL);
    		}
    		
    		else
    		{
    			// 참여중인 번개가 있을 때 시간을 검사 한다.
    			for ( int i=0 ; i < presentBungaeList.size() ; i++ )
    			{
    				Date compareDate = new Date();
    				
    				try {
						compareDate = dateFormat.parse(presentBungaeList.get(i).getBungaeTime());
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    				
//    				Log.d("시간확인1", bungaeTime);
//    				Log.d("시간확인2", presentBungaeList.get(i).getBungaeTime());
//    				System.out.println(compareDate);
    				
    				long difference = (bungaeDateTime.getTime()-compareDate.getTime())/1000;
    						
    				if (Math.abs(difference)<8*60*60)
    				{
    					cannotCreateFlag = 1;
    					break;
    				}

    			}
    			
    			
    			if ( cannotCreateFlag == 1 )
    			{
    				mProgressDialog.dismiss();
    				// 번개를 생성 할 수 없을 때!!
    				//Toast.makeText(AddBungae.this, "번개 생성 실패!!", 0).show();
    				cannotCreateDialog();
    			}
    			else
    			{
    				// 번개를 생성할 수 있을 때!!
    				//Toast.makeText(AddBungae.this, "현재 참여한 번개가 있음에도 불구하고 번개생성시작!!", 0).show();

    				// Parsing Start!!!
        		    CreateBungaeTask task = new CreateBungaeTask();   
        			task.execute(ADD_BUNGAE_URL);
    			}
    			
    			
    		}
    		
    		}         
    	
    	
    	public String parseXml(InputStream is) throws IOException, XmlPullParserException {    
    		XmlPullParser parser = Xml.newPullParser();     
    		try {
    			
    			parser.setInput(is, null);      
    			int eventType = parser.getEventType();
    			PresentBungaeData presentBungaeData = null;
    			while (eventType != XmlPullParser.END_DOCUMENT) {   
    				String tag = null;     
    				switch (eventType) {    
    				case XmlPullParser.START_TAG:        
    					tag = parser.getName(); 
    					
    					if(tag.equals("bungae"))
    						presentBungaeData = new PresentBungaeData();
    					if(tag.equals("b_num"))
    						boolean_b_num = true;
    					if(tag.equals("b_title"))
    						boolean_b_title = true;
    					if(tag.equals("b_time"))
    						boolean_b_time = true;
    						                                   
    					break;           
    					
    				case XmlPullParser.TEXT:
    					if(boolean_b_num){
    						presentBungaeData.setBungaeNum(parser.getText());
    						boolean_b_num = false;
    					}
    					if(boolean_b_title){
    						presentBungaeData.setBungaeTitle(parser.getText());
    						boolean_b_title = false;
    					}
    					if(boolean_b_time){
    						presentBungaeData.setBungaeTime(parser.getText());
    						boolean_b_time = false;
    					}
    					
    					break;
    					
    					
    					case XmlPullParser.END_TAG:  
    						
    						tag = parser.getName();
    						if (tag.equalsIgnoreCase("bungae") && presentBungaeData != null){
    							presentBungaeList.add(presentBungaeData);
    							completeString = "complete";
    						}
    						else if (tag.equalsIgnoreCase("nowbungae") )
    							completeString = "complete";
    							
    						break;                       
    						}                         
    				eventType = parser.next();           
    				}     

    			
    			
    			} catch (Exception e) {     
    				e.printStackTrace();    
    				}          
    		
    		return completeString;
    	} 
    	
    }

	// Create Bungae Parsing Start!!
	
	public class CreateBungaeTask extends AsyncTask<String, Integer, String>
	{  
//    	private AddBungae mActivity;    
//    	private ProgressDialog mProgressDialog;   
    	
    	private String completeString;

    	
//    	public CreateBungaeTask(AddBungae activity) {  
//    		mActivity = activity;     
//    		}            
    	  
    	@Override    
    	protected void onPreExecute() { 

//    		mProgressDialog = new ProgressDialog(mActivity);     
//    		mProgressDialog.setMessage("번개 생성중...");     
//    		mProgressDialog.show();      
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
    	            //String deStr = URLDecoder.encode(enStr, "utf-8");
    	            String titleStr;
    	            titleStr = titleEdit.getText().toString();
    	            String encodeTitleStr = URLEncoder.encode(titleStr, "utf-8");
    	            
    	            String locationStr;
    	            locationStr = locationEdit.getText().toString();
    	            String encodeLocationStr = URLEncoder.encode(locationStr, "utf-8");
    	            
    	            String contentStr;
    	            contentStr = contentEdit.getText().toString();
    	            String encodeContentStr = URLEncoder.encode(contentStr, "utf-8");
    	            
    	            StringBuffer buffer = new StringBuffer(); 
    	            buffer.append("b_category").append("=").append(categoryEdit.getText()).append("&")
    	            .append("b_title").append("=").append(encodeTitleStr).append("&")
    	            .append("b_host_id").append("=").append(UserInfoClass.getInstance().userInfo.get("u_id")).append("&")
    	            .append("b_time").append("=").append(bungaeTime).append("&")
    	            .append("b_loca").append("=").append(encodeLocationStr).append("&")
    	            .append("b_loca_lon").append("=").append(AddBungaeMapClass.getInstance().addBungaeMapInfo.get("longitude")).append("&")
    	            .append("b_loca_lat").append("=").append(AddBungaeMapClass.getInstance().addBungaeMapInfo.get("latitude")).append("&")
    	            .append("b_content").append("=").append(encodeContentStr).append("&")
    	            .append("b_max").append("=").append(maxNum).append("&")
    	            .append("b_min").append("=").append(minNum).append("&")
    	            .append("b_cur").append("=").append("1").append("&")
    	            .append("b_members=(")
    	            .append("id").append(":").append(UserInfoClass.getInstance().userInfo.get("u_id")).append(", ")
    	            .append("sex").append(":").append(UserInfoClass.getInstance().userInfo.get("u_sex")).append(", ")
    	            .append("pushid").append(":").append("A_"+UserInfoClass.getInstance().userInfo.get("u_push_id")).append(", ")
    	            .append("age").append(":").append(UserInfoClass.getInstance().userInfo.get("u_age")).append(")&")
    	            .append("b_open_private").append("=").append(openPrivateFlag).append("&")
    	            .append("b_password").append("=").append(password.getText().toString()).append("&");
    	            
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
    		
    		Toast.makeText(AddBungae.this, "번개가 생성되었습니다.", 0).show();
    		
    		onBackPressed();
    		mProgressDialog.dismiss();      

    		}         
    	
    	
    	public String parseXml(InputStream is) throws IOException, XmlPullParserException {    
    		XmlPullParser parser = Xml.newPullParser();     
    		try {
    			
    			completeString = "complete";
    			
    			parser.setInput(is, null);      
    			int eventType = parser.getEventType();     
    			          
    			while (eventType != XmlPullParser.END_DOCUMENT) {   
    				
    				switch (eventType) {    
    				case XmlPullParser.START_TAG:        
    					
    					
    						                                   
    					break;           
    					
    				case XmlPullParser.TEXT:
    					
    						break;                       
    						}                         
    				eventType = parser.next();           
    				}     

    			
    			
    			} catch (Exception e) {     
    				e.printStackTrace();    
    				}          
    		return completeString;     
    	} 
    	
    	
    }
}
