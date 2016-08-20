package com.hunect.bungae;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import net.daum.mobilead.AdConfig;
import net.daum.mobilead.AdHttpListener;
import net.daum.mobilead.MobileAdView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class BungaeDetailActivity extends Activity implements OnClickListener, AdHttpListener {

	private ProgressDialog mProgressDialog;
	
	private MobileAdView adView = null;
	
	private static final int DIALOG_OK_CANCEL = 0;
	
	private TextView CategoryText;
	private TextView TitleText;
	private TextView TimeText;
	private TextView HostText;
	private TextView LocationText;
	private TextView ContentText;
	private TextView CurrentText;
	private ListView MemberList;
	
	private TextView minNoticeText;
	private TextView openNoticeText;
	
	private ImageView openImage;
	
	private TextView memberIdText;
	private TextView memberSexText;
	private TextView memberAgeText;
	
	private Button hostButton;
	private Button mapButton;
	private Button enterButton;
	private ImageButton reportButton;
	
	private String timeStr;

	private SimpleDateFormat dateFormat;
	
	private String bungaeTime;
	private Date bungaeDateTime;
	
	private int notEnterFlag;
	
	private String enterInfoString;
	
	private ArrayList<String> memberTmpList;

	String NumStr;

	String memberStr;
	
	
	ArrayList<BungaeMember> SelectedMemberList = null;
	
	BungaeMember selectedBungaeMember = null;
	
	ArrayList<BungaeDetailData> BungaeDetail = null;
	 
    BungaeDetailData selectedBungaeData = null;
    
    ArrayList<PresentBungaeData> PresentMyBungae = null;
	 
    PresentBungaeData presentBungae = null;
    
    ArrayList<NewBungaeDetailData> NewBungaeData = null;
    
    NewBungaeDetailData newBungae = null;
    
    
    ScrollView scrollView;
    ListView listView;
    
    
	boolean boolean_b_num = false,
			boolean_b_category = false,
			boolean_b_title = false,
			boolean_b_host_id = false,
			boolean_b_time = false,
			boolean_b_loca = false,
			boolean_b_loca_lon = false,
			boolean_b_loca_lat = false,
			boolean_b_content = false,
			boolean_b_cur = false,
			boolean_b_max = false,
			boolean_b_min = false,
			boolean_b_members = false,
			boolean_b_open_private = false;
	
	//BungaeDetailActivity.java

	private static final String BUNGAE_DETAIL_URL = BungaeActivity.MAIN_URL + "/selectedbungae-r.php"; 
	private static final String PRESENT_BUNGAE_URL = BungaeActivity.MAIN_URL + "/presentmybungae.php"; 
	private static final String ENTER_BUNGAE_URL = BungaeActivity.MAIN_URL + "/updateenter.php";
	
	
//	private static final String BUNGAE_DETAIL_URL = "http://www.hunect.com/testphp/selectedbungae-r.php"; 
//	
//	private static final String PRESENT_BUNGAE_URL = "http://www.hunect.com/testphp/presentmybungae.php"; 
//	
//	private static final String ENTER_BUNGAE_URL = "http://www.hunect.com/testphp/updateenter.php";
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.bungae_detail);
	    // TODO Auto-generated method stub
	    
	    initAdam();
	    
	    scrollView = (ScrollView)findViewById(R.id.scrollView1);
	    listView = (ListView)findViewById(R.id.memberListView);
	    
	    
	    listView.setOnTouchListener(new OnTouchListener(){

	    	@Override
	    	 
	    	 public boolean onTouch(View v, MotionEvent event){
	    	 
	    	if(event.getAction() == MotionEvent.ACTION_UP)
	    		scrollView.requestDisallowInterceptTouchEvent(false);
	    	 
	    	else
	    	 	scrollView.requestDisallowInterceptTouchEvent(true);

	    	return false;
    	 
	    	}
	    });


	    
	    
	    
	    notEnterFlag = 0;
	    
	    CategoryText = (TextView)findViewById(R.id.categoryLabel);
	    TitleText = (TextView)findViewById(R.id.titleLabel);
	    HostText = (TextView)findViewById(R.id.hostLabel);
	    TimeText = (TextView)findViewById(R.id.timeLabel);
	    LocationText = (TextView)findViewById(R.id.locationLabel);
	    ContentText = (TextView)findViewById(R.id.contentText);
	    CurrentText = (TextView)findViewById(R.id.currentNumLabel);
	    MemberList = (ListView)findViewById(R.id.memberListView);
	    
	    minNoticeText = (TextView)findViewById(R.id.minNoticeLabel);
	    openNoticeText = (TextView)findViewById(R.id.openNoticeLabel);
	    openImage = (ImageView)findViewById(R.id.openImageView);
	    
	    hostButton = (Button)findViewById(R.id.hostButton);
	    mapButton = (Button)findViewById(R.id.mapButton);
	    enterButton = (Button)findViewById(R.id.enterButton);
	    reportButton = (ImageButton)findViewById(R.id.reportButton);
	    
	    NumStr = getIntent().getStringExtra("selectedNum"); //인텐트의 key값을 통해 해당 String을 받는다.

	    //Toast.makeText(this, str, Toast.LENGTH_LONG).show(); //토스트 기능으로 확인해보자.

	    //NumberText.setText(str);
	    
	    timeStr = new String();
	    
	    memberTmpList = new ArrayList<String>();
	    
	    hostButton.setOnClickListener(this);
	    mapButton.setOnClickListener(this);
	    enterButton.setOnClickListener(this);
	    reportButton.setOnClickListener(this);
	    
	    BungaeDetail = new ArrayList<BungaeDetailData>();
	    
	    PresentMyBungae = new ArrayList<PresentBungaeData>();
	    
	    NewBungaeData = new ArrayList<NewBungaeDetailData>();
	    
	    bungaeTime = new String();
	    //bungaeDateTime = new Date();
	    
	    dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    
	    enterInfoString = new String();
	    
	    //enterInfoString = "(id:seyoun, sex:0, pushid:A_8, age:26)";
	    enterInfoString = "(id:"+UserInfoClass.getInstance().userInfo.get("u_id")+", sex:"+UserInfoClass.getInstance().userInfo.get("u_sex")+", pushid:A_"+UserInfoClass.getInstance().userInfo.get("u_push_id")+", age:"+UserInfoClass.getInstance().userInfo.get("u_age")+")";
	    
	    BungaeDetailParserTask task = new BungaeDetailParserTask(this);   
		task.execute(BUNGAE_DETAIL_URL);

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
	public void onClick(View v) {
		switch(v.getId()){
		
			case R.id.hostButton:
			{
				
				Intent intent = new Intent(this, BungaeDetailHostActivity.class);   
				intent.putExtra("HostId", BungaeDetail.get(0).getBungaeHostId());       
				startActivity(intent);  
				
				//Toast.makeText(this, BungaeDetail.get(0).getBungaeHostId(), Toast.LENGTH_LONG).show();
			}
			break;
			
			
			case R.id.enterButton:
			{
				//Toast.makeText(this, "참여 버튼 작동!!", Toast.LENGTH_LONG).show();
				showDialog(DIALOG_OK_CANCEL);	
			}
			break;
			
			
			case R.id.mapButton:
			{
				
				Intent intent = new Intent(this, BungaeDetailMap.class);
				intent.putExtra("Location", BungaeDetail.get(0).getBungaeLocation());
		    	intent.putExtra("Loca_Lon", BungaeDetail.get(0).getBungaeLocationLon());
		    	intent.putExtra("Loca_Lat", BungaeDetail.get(0).getBungaeLocationLat());
				startActivity(intent);
				
				
				//Toast.makeText(this, "맵 버튼 작동!!", Toast.LENGTH_LONG).show();
			}
			break;
			
			case R.id.reportButton:
			{
				Toast.makeText(this, "이 번개를 신고합니다.", Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(this, Report.class);   
				intent.putExtra("bHostId", BungaeDetail.get(0).getBungaeHostId());
				startActivity(intent);
			}
		
		}
	}
	
	@Override
    protected Dialog onCreateDialog(int id){
    	switch(id){
    	case DIALOG_OK_CANCEL:
    		return new AlertDialog.Builder(this)
    			.setTitle("번개 참여")
    			.setMessage("번개에 참여하시겠습니까?")
    			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if ( BungaeDetail.get(0).getBungaeMax().equals("11") )
						{
							PresentBungaeParserTask task = new PresentBungaeParserTask(this);   
							task.execute(PRESENT_BUNGAE_URL);
						}
						
						else if (BungaeDetail.get(0).getBungaeCur().equals(BungaeDetail.get(0).getBungaeMax()))
						{
							
							AlertDialog.Builder builder = new AlertDialog.Builder(BungaeDetailActivity.this);
							builder.setTitle("참여 불가")
						    .setMessage("이미 참여 인원이 모두 찼습니다.")
						    .setCancelable(true)
						    .setNeutralButton("확인", null)
						    .create().show();
							
						}
						
						else if (Integer.parseInt(BungaeDetail.get(0).getBungaeCur())<Integer.parseInt(BungaeDetail.get(0).getBungaeMax()))
						{
							PresentBungaeParserTask task = new PresentBungaeParserTask(this);   
							task.execute(PRESENT_BUNGAE_URL);
						}
					}
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//Toast.makeText(BungaeDetailActivity.this, "Cancel", Toast.LENGTH_SHORT).show();						
					}
				}).create();
    	
    	}
    	
    	return null;
    	
    	
    }

	
	@Override
	public void onResume() {
		super.onResume();       // 액티비티가 화면에 보임, 필요한 모든 UI 변경 사항을 적용한다.
		
	}
	
	   
	public void classifyMember(){
		
		memberStr = BungaeDetail.get(0).getBungaeMembers();
		

		StringTokenizer tokens = new StringTokenizer(memberStr, ")");
		for (int x=0; tokens.hasMoreElements(); x++){
			
			memberTmpList.add(tokens.nextToken());
			Log.i("PullXML_1", memberTmpList.get(x));

		}
		
		int size = memberTmpList.size();
		
		String sizeStr = String.valueOf(size);
		
		Log.i("PullXML_1", sizeStr);
		
		SelectedMemberList = new ArrayList<BungaeMember>();
		
		for (int a=0; a<size ;a++)
		{
			selectedBungaeMember = new BungaeMember();
			
			int count = 0;
			
			StringTokenizer tokens2nd = new StringTokenizer(memberTmpList.get(a), ",");
			for (; tokens2nd.hasMoreElements();){
				
				//Log.i("PullXML_2", tokens2nd.nextToken());
				
				
				
				StringTokenizer tokens3rd = new StringTokenizer(tokens2nd.nextToken(), ":");
				for (int z=0; tokens3rd.hasMoreElements();z++){
					
					
					
					if (z==0)
					{
						//selectedBungaeMember.setMemberId(tokens3rd.nextToken());
						Log.i("PullXML_3", "Trash Token = " + tokens3rd.nextToken());
					}
					
					if (z==1)
					{
						
						//selectedBungaeMember.setMemberSex(tokens3rd.nextToken());
						//Log.i("PullXML_3", "Value Token = " + tokens3rd.nextToken());
						
						if (count==0)
						{
							selectedBungaeMember.setMemberId(tokens3rd.nextToken());
							Log.i("PullXML_Mem", "Id = " + selectedBungaeMember.getMemberId());
						}
						else if (count==1)
						{
							selectedBungaeMember.setMemberSex(tokens3rd.nextToken());
							Log.i("PullXML_Mem", "Sex = " + selectedBungaeMember.getMemberSex());
						}
						else if (count==2)
						{
							selectedBungaeMember.setMemberPushId(tokens3rd.nextToken());
							Log.i("PullXML_Mem", "PushId = " + selectedBungaeMember.getMemberPushId());
						}
						else if (count==3)
						{
							selectedBungaeMember.setMemberAge(tokens3rd.nextToken());
							Log.i("PullXML_Mem", "Age = " + selectedBungaeMember.getMemberAge());
						}
						
						count++;
					}
					
					
				
				}
				 
			}
			
			SelectedMemberList.add(selectedBungaeMember);
		}
		
		MemberList.setAdapter(new MemberCustomRow(this));

		
	}
	

	
    
    class MemberCustomRow extends ArrayAdapter<BungaeMember>{
    	Activity context;
		public MemberCustomRow(Activity c) {
			super(c,R.layout.bungae_detail_member_row,SelectedMemberList);
			this.context = c;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
		
			 LayoutInflater inf=context.getLayoutInflater();
			 View customcell=inf.inflate(R.layout.bungae_detail_member_row, null);
			 
			 memberIdText = (TextView)customcell.findViewById(R.id.memberIdText);
			 memberSexText = (TextView)customcell.findViewById(R.id.memberSexText);
			 memberAgeText = (TextView)customcell.findViewById(R.id.memberAgeText);
			 
			 
			 memberIdText.setText(SelectedMemberList.get(position).getMemberId());
			 if (SelectedMemberList.get(position).getMemberSex().equals("0"))
			 {
				 memberSexText.setText("남");
			 }
			 else
			 {
				 memberSexText.setText("여");
			 }
			 
			 memberAgeText.setText(SelectedMemberList.get(position).getMemberAge());
			 
			 
			 return customcell; 
		}
		
		
    }
    
    public class BungaeDetailParserTask extends AsyncTask<String, Integer, String>{  
    	private BungaeDetailActivity mActivity;    
//    	private ProgressDialog mProgressDialog;   
    	
    	private String completeString;
    	
    	boolean boolean_b_num = false,
    			boolean_b_category = false,
    			boolean_b_title = false,
    			boolean_b_host_id = false,
    			boolean_b_time = false,
    			boolean_b_loca = false,
    			boolean_b_loca_lon = false,
    			boolean_b_loca_lat = false,
    			boolean_b_content = false,
    			boolean_b_cur = false,
    			boolean_b_max = false,
    			boolean_b_min = false,
    			boolean_b_members = false,
    			boolean_b_open_private = false;
    	
    	public BungaeDetailParserTask(BungaeDetailActivity activity) {  
    		mActivity = activity;     
    		}       
    	  
    	@Override    
    	protected void onPreExecute() { 
    		BungaeDetail.clear();
    		
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
    	            http.setDoOutput(true);                       // 서버로 쓰기 모드 지정  
    	            http.setRequestMethod("POST");         // 전송 방식은 POST 

    	            // 서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다 
    	            http.setRequestProperty("content-type", "application/x-www-form-urlencoded"); 
    	            //-------------------------- 
    	            //   서버로 값 전송 
    	            //-------------------------- 
    	            StringBuffer buffer = new StringBuffer(); 
    	            buffer.append("num").append("=").append(NumStr); 
    	           
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
    		
    		timeStr = (BungaeDetail.get(0).getBungaeTimeConvert().getMonth()+1)+"월"+BungaeDetail.get(0).getBungaeTimeConvert().getDate()+"일  "+BungaeDetail.get(0).getBungaeTimeConvert().getHours()+"시"+BungaeDetail.get(0).getBungaeTimeConvert().getMinutes()+"분";
    		
    		CategoryText.setText(BungaeDetail.get(0).getBungaeCategory());
			TitleText.setText(BungaeDetail.get(0).getBungaeTitle());
			HostText.setText(BungaeDetail.get(0).getBungaeHostId());
			TimeText.setText(timeStr);
			LocationText.setText(BungaeDetail.get(0).getBungaeLocation());
			ContentText.setText(BungaeDetail.get(0).getBungaeContent());
			
			minNoticeText.setText("최소 "+BungaeDetail.get(0).getBungaeMin()+"명이 모이면 성사 됩니다.");
			if (BungaeDetail.get(0).getBungaeOpenPrivate().equals("0"))
			{
				openNoticeText.setText("이 번개는 공개 번개 입니다.");
				openImage.setImageResource(R.drawable.lightning1);
			}
			else
			{
				openNoticeText.setText("이 번개는 비밀 번개 입니다.");
				openImage.setImageResource(R.drawable.lock);
			}
			
			
			if ( BungaeDetail.get(0).getBungaeMax().equals("11") )
				CurrentText.setText(BungaeDetail.get(0).getBungaeCur()+"    /    "+"무제한");
			
			else 
				CurrentText.setText(BungaeDetail.get(0).getBungaeCur()+"    /    "+BungaeDetail.get(0).getBungaeMax());
			

			
			bungaeTime = BungaeDetail.get(0).getBungaeTime();
			
			try {
				bungaeDateTime = dateFormat.parse(bungaeTime);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println(bungaeDateTime);
			
			classifyMember();
			
    		mProgressDialog.dismiss();      

           //Toast.makeText(BungaeDetailActivity.this, "전송 후 결과 받음", 0).show(); 
    		
    		setTitle("번개 정보 - "+BungaeDetail.get(0).getBungaeTitle());
    		
    	    scrollView.setSmoothScrollingEnabled(true);
    	    scrollView.smoothScrollTo(0, 0);
    	    
    	}
    	
    	
    	public String parseXml(InputStream is) throws IOException, XmlPullParserException {    
    		XmlPullParser parser = Xml.newPullParser();     
    		try {
    			
    			parser.setInput(is, null);      
    			int eventType = parser.getEventType();     
    			BungaeDetailData selectedBungaeData = null;              
    			while (eventType != XmlPullParser.END_DOCUMENT) {   
    				String tag = null;     
    				switch (eventType) {    
    				case XmlPullParser.START_TAG:        
    					tag = parser.getName(); 
    					
    					if(tag.equals("bungae"))
    						selectedBungaeData = new BungaeDetailData();
    					if(tag.equals("b_num"))
    						boolean_b_num = true;
    					if(tag.equals("b_category"))
    						boolean_b_category = true;
    					if(tag.equals("b_title"))
    						boolean_b_title = true;
    					if(tag.equals("b_host_id"))
    						boolean_b_host_id = true;
    					if(tag.equals("b_time"))
    						boolean_b_time = true;
    					if(tag.equals("b_loca"))
    						boolean_b_loca = true;
    					if(tag.equals("b_loca_lon"))
    						boolean_b_loca_lon = true;
    					if(tag.equals("b_loca_lat"))
    						boolean_b_loca_lat = true;
    					if(tag.equals("b_content"))
    						boolean_b_content = true;
    					if(tag.equals("b_cur"))
    						boolean_b_cur = true;
    					if(tag.equals("b_max"))
    						boolean_b_max = true;
    					if(tag.equals("b_min"))
    						boolean_b_min = true;
    					if(tag.equals("b_members"))
    						boolean_b_members = true;
    					if(tag.equals("b_open_private"))
    						boolean_b_open_private = true;   
    						                                   
    					break;           
    					
    				case XmlPullParser.TEXT:
    					if(boolean_b_num){
    						selectedBungaeData.setBungaeNum(parser.getText());
    						boolean_b_num = false;
    					}
    					if(boolean_b_category){
    						String caStr = parser.getText();
    						
    						String enStr = new String();
    						
    						try {
    							enStr = URLEncoder.encode(caStr, "utf-8");
    						} catch (UnsupportedEncodingException e) {
    							// TODO Auto-generated catch block
    							e.printStackTrace();
    						}
    						
    						
    						if (enStr.equals("%0A%09"))
    						{
    							selectedBungaeData.setBungaeCategory("일반");
    						}
    						else
    						{
    							selectedBungaeData.setBungaeCategory(parser.getText());
    						}
    							
    						
    						boolean_b_category = false;
    					}
    					if(boolean_b_title){
    						
    						String enStr = parser.getText();
    						
    						String deStr = URLDecoder.decode(enStr, "utf-8");

    						
    						selectedBungaeData.setBungaeTitle(deStr);
    						
    						boolean_b_title = false;
    					}
    					if(boolean_b_host_id){
    						selectedBungaeData.setBungaeHostId(parser.getText());
    						boolean_b_host_id = false;
    					}
    					if(boolean_b_time){
    						selectedBungaeData.setBungaeTime(parser.getText());
    						selectedBungaeData.setBungaeTimeConvert(dateFormat.parse(parser.getText()));
    						boolean_b_time = false;
    					}
    					if(boolean_b_loca){
    						
    						String enStr = parser.getText();
    						
    						String deStr = URLDecoder.decode(enStr, "utf-8");
    						
    						selectedBungaeData.setBungaeLocation(deStr);
    						boolean_b_loca = false;
    					}
    					if(boolean_b_loca_lon){
    						selectedBungaeData.setBungaeLocationLon(parser.getText());
    						boolean_b_loca_lon = false;
    					}
    					if(boolean_b_loca_lat){
    						selectedBungaeData.setBungaeLocationLat(parser.getText());
    						boolean_b_loca_lat = false;
    					}
    					if(boolean_b_content){
    						String enStr = parser.getText();
    						
    						String deStr = URLDecoder.decode(enStr, "utf-8");
    						
    						selectedBungaeData.setBungaeContent(deStr);
    						boolean_b_content = false;
    					}
    					if(boolean_b_cur){
    						selectedBungaeData.setBungaeCur(parser.getText());
    						boolean_b_cur = false;
    					}
    					if(boolean_b_max){
    						selectedBungaeData.setBungaeMax(parser.getText());
    						boolean_b_max = false;
    					}
    					if(boolean_b_min){
    						selectedBungaeData.setBungaeMin(parser.getText());
    						boolean_b_min = false;
    					}
    					if(boolean_b_members){
    						selectedBungaeData.setBungaeMembers(parser.getText());
    						boolean_b_members = false;
    					}
    					if(boolean_b_open_private){
    						selectedBungaeData.setBungaeOpenPrivate(parser.getText());
    						boolean_b_open_private = false;
    					}
    					
    					break;
    					
    					
    					case XmlPullParser.END_TAG:  
    						
    						tag = parser.getName();
    						if (tag.equalsIgnoreCase("bungae") && selectedBungaeData != null){
    							BungaeDetail.add(selectedBungaeData);
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
    
    
    public class PresentBungaeParserTask extends AsyncTask<String, Integer, String>{  
//    	private android.content.DialogInterface.OnClickListener onClickListenerIns;    
//    	private ProgressDialog mProgressDialog;   
    	
    	private String completeString;
    	
    	boolean boolean_b_num = false,
    			boolean_b_title = false,
    			boolean_b_time = false;
    	
    	public PresentBungaeParserTask(android.content.DialogInterface.OnClickListener onClickListener) {  
//    		onClickListenerIns = onClickListener;     
    		}       
    	  
    	@Override    
    	protected void onPreExecute() { 
    		
    		mProgressDialog = new ProgressDialog(BungaeDetailActivity.this);     
    		mProgressDialog.setMessage("번개 참여중...");     
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
    		
    		if (PresentMyBungae.size()==0)
    		{
    			//Toast.makeText(BungaeDetailActivity.this, "참여중인 번개 없음", 0).show(); 
    		}
    		else
    		{
    			
    			for (int i=0;i<PresentMyBungae.size();i++)
    			{
    				Date compareDate = new Date();
    				
    				try {
						compareDate = dateFormat.parse(PresentMyBungae.get(i).getBungaeTime());
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    				
    				
    				long difference = (bungaeDateTime.getTime()-compareDate.getTime())/1000;
    						
    				if (Math.abs(difference)<8*60*60)
    				{
    					notEnterFlag = 1;
    					break;
    				}

    			}

    		}
    		
    		    		
    		if (notEnterFlag == 1)
    		{
    			//Toast.makeText(BungaeDetailActivity.this, "시간이 가까워서 번개 참여 못함", 0).show(); 
    			AlertDialog.Builder builder = new AlertDialog.Builder(BungaeDetailActivity.this);
				builder.setTitle("참여 불가")
			    .setMessage("현재 참여중인 다른 번개와 시간이 너무 가깝습니다.")
			    .setCancelable(true)
			    .setNeutralButton("확인", null)
			    .create().show();
				
				mProgressDialog.dismiss();
    		}
    		else
    		{
    			//Toast.makeText(BungaeDetailActivity.this, "번개 참여 시도", 0).show(); 
    			
    			NewBungaeDetailParserTask task = new NewBungaeDetailParserTask();   
    			task.execute(BUNGAE_DETAIL_URL);
    		}
    		
//    		mProgressDialog.dismiss();      

    		
    		
    		}         
    	
    	
    	public String parseXml(InputStream is) throws IOException, XmlPullParserException {    
    		XmlPullParser parser = Xml.newPullParser();     
    		try {
    			
    			parser.setInput(is, null);      
    			int eventType = parser.getEventType();     
    			PresentBungaeData presentBungae = null;            
    			while (eventType != XmlPullParser.END_DOCUMENT) {   
    				String tag = null;     
    				switch (eventType) {    
    				case XmlPullParser.START_TAG:        
    					tag = parser.getName(); 
    					
    					if(tag.equals("bungae"))
    						presentBungae = new PresentBungaeData();
    					if(tag.equals("b_num"))
    						boolean_b_num = true;
    					if(tag.equals("b_title"))
    						boolean_b_title = true;
    					if(tag.equals("b_time"))
    						boolean_b_time = true;
    						                                   
    					break;           
    					
    				case XmlPullParser.TEXT:
    					if(boolean_b_num){
    						presentBungae.setBungaeNum(parser.getText());
    						boolean_b_num = false;
    					}
    					if(boolean_b_title){
    						
    						String enStr = parser.getText();
    						
    						String deStr = URLDecoder.decode(enStr, "utf-8");

    						
    						presentBungae.setBungaeTitle(deStr);
    						
    						boolean_b_title = false;
    					}
    					if(boolean_b_time){
    						presentBungae.setBungaeTime(parser.getText());
    						boolean_b_time = false;
    					}
    					
    					break;
    					
    					
    					case XmlPullParser.END_TAG:  
    						
    						tag = parser.getName();
    						if (tag.equalsIgnoreCase("bungae") && presentBungae != null){
    							PresentMyBungae.add(presentBungae);
    							completeString = "complete";
    							
    						}
    						else if (tag.equalsIgnoreCase("nowbungae"))
    						{
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
    
    
    public class NewBungaeDetailParserTask extends AsyncTask<String, Integer, String>{  
//    	private BungaeDetailActivity mActivity;    
//    	private ProgressDialog mProgressDialog;   
    	
    	private String completeString;
    	
    	boolean boolean_b_num = false,
    			boolean_b_cur = false,
    			boolean_b_members = false;
    	
//    	public NewBungaeDetailParserTask(BungaeDetailActivity activity) {  
//    		mActivity = activity;     
//    		}       
    	  
    	@Override    
    	protected void onPreExecute() { 
    		if (NewBungaeData.size() != 0)
    		{
    			NewBungaeData.clear();
    		}
    		
//    		mProgressDialog = new ProgressDialog(mActivity);     
//    		mProgressDialog.setMessage("번개정보 업데이트...");     
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
    	            buffer.append("num").append("=").append(BungaeDetail.get(0).getBungaeNum()); 
    	           
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
    		
    		if (NewBungaeData.size() != 0)
    		{
//    			Toast.makeText(BungaeDetailActivity.this, "번개가 살아있습니다", 0).show(); 
    			
    			EnterBungaeTask task = new EnterBungaeTask();   
    			task.execute(ENTER_BUNGAE_URL);
    		}
    		else if (NewBungaeData.size() == 0)
    		{
    			mProgressDialog.dismiss();
    			
//    			Toast.makeText(BungaeDetailActivity.this, "번개가 삭제되었습니다", 0).show(); 
    			AlertDialog.Builder builder = new AlertDialog.Builder(BungaeDetailActivity.this);
				builder.setTitle("삭제된 번개")
			    .setMessage("방금 삭제된 번개입니다.")
			    .setCancelable(true)
			    .setNeutralButton("확인", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						onBackPressed();
					}
				})
			    .create().show();
    			
    		}
			
//    		mProgressDialog.dismiss();      

           
    		}         
    	
    	
    	public String parseXml(InputStream is) throws IOException, XmlPullParserException {    
    		XmlPullParser parser = Xml.newPullParser();     
    		try {
    			
    			parser.setInput(is, null);      
    			int eventType = parser.getEventType();     
    			NewBungaeDetailData newBungae = null;              
    			while (eventType != XmlPullParser.END_DOCUMENT) {   
    				String tag = null;     
    				switch (eventType) {    
    				case XmlPullParser.START_TAG:        
    					tag = parser.getName(); 
    					
    					if(tag.equals("bungae"))
    						newBungae = new NewBungaeDetailData();
    					if(tag.equals("b_num"))
    						boolean_b_num = true;
    					if(tag.equals("b_cur"))
    						boolean_b_cur = true;
    					if(tag.equals("b_members"))
    						boolean_b_members = true;
    						                                   
    					break;           
    					
    				case XmlPullParser.TEXT:
    					if(boolean_b_num){
    						newBungae.setBungaeNum(parser.getText());
    						Log.d("파싱 테스트", parser.getText());
    						boolean_b_num = false;
    					}
    					if(boolean_b_cur){
    						newBungae.setBungaeCur(parser.getText());
    						boolean_b_cur = false;
    					}
    					if(boolean_b_members){
    						newBungae.setBungaeMembers(parser.getText());
    						boolean_b_members = false;
    					}
    					
    					break;
    					
    					
    					case XmlPullParser.END_TAG:  
    						
    						tag = parser.getName();
    						if (tag.equalsIgnoreCase("bungae") && newBungae != null){
    							NewBungaeData.add(newBungae);
    							completeString = "complete";
    							
    						}
    						else if (tag.equalsIgnoreCase("nowbungae"))
    						{
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
    
    
    
    public class EnterBungaeTask extends AsyncTask<String, Integer, String>{  
//    	private BungaeDetailActivity mActivity;    
//    	private ProgressDialog mProgressDialog;   
    	
    	private String completeString;

    	
//    	public EnterBungaeTask(BungaeDetailActivity activity) {  
//    		mActivity = activity;     
//    		}            
    	  
    	@Override    
    	protected void onPreExecute() { 

//    		mProgressDialog = new ProgressDialog(mActivity);     
//    		mProgressDialog.setMessage("번개 참여중...");     
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
    	            buffer.append("num").append("=").append(NewBungaeData.get(0).getBungaeNum()+"&")
    	            .append("b_members").append("=").append(NewBungaeData.get(0).getBungaeMembers()).append(", ").append(enterInfoString+"&")
    	            .append("b_cur").append("=").append((Integer.parseInt(NewBungaeData.get(0).getBungaeCur())+1));
    	           
    	            
    	            
    	            Log.d("BungaeEnter", buffer.toString());
    	            
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
