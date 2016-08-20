package com.hunect.bungae;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MyBungaeListAdapter extends ArrayAdapter<MyBungaeItem>{
	private LayoutInflater mInflater;   
	private TextView category;
	private TextView title;         
	private TextView time; 
	private TextView host;
	private TextView memberNum;
	private ImageView bungaeIcon;
	
	public MyBungaeListAdapter(Context context, List<MyBungaeItem> objects) {    
		super(context, 0, objects);       
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);     
	}
	
	@Override   
	public View getView(int position, View convertView, ViewGroup parent) {  
		View view = convertView;           
		if (convertView == null) {       
			view = mInflater.inflate(R.layout.bungae_list_row, null);    
		}

		MyBungaeItem item = this.getItem(position);
		
		if (item != null) {
			if(item.isSection()) {
				SectionItem si = (SectionItem)item;
				view = mInflater.inflate(R.layout.mybungae_section, null);

				view.setOnClickListener(null);
				view.setOnLongClickListener(null);
				view.setLongClickable(false);
				
				final TextView sectionView = (TextView) view.findViewById(R.id.section_text);
				sectionView.setText(si.getTitle());
			}
			else {
				BungaeListData ei = (BungaeListData)item;
				view = mInflater.inflate(R.layout.bungae_list_row, null);
				
				this.bungaeIcon = (ImageView) view.findViewById(R.id.bungaeIcon);
				if (ei.getBungaeOpenPrivate().toString().equals("0"))
				{
					this.bungaeIcon.setImageResource(R.drawable.lightning1);
				}
				else
				{
					this.bungaeIcon.setImageResource(R.drawable.lock);
				}
				
				
				
				String category = ei.getBungaeCategory().toString();
				String categoryString = new String();
				
				String enStr = new String();
				
				try {
					enStr = URLEncoder.encode(category, "utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				
				
				if (enStr.equals("%0A%09"))
				{
					category = "일반";
				}
				
				
				categoryString = "["+category+" 번개]";
				
				this.category = (TextView) view.findViewById(R.id.categoryLabel);
				this.title = (TextView) view.findViewById(R.id.titleLabel); 
				this.time = (TextView) view.findViewById(R.id.timeLabel);  
				this.host = (TextView) view.findViewById(R.id.hostLabel);        
				this.memberNum = (TextView) view.findViewById(R.id.numLabel);      
				
				
				this.category.setText(categoryString);
				
				
				String title = ei.getBungaeTitle().toString();   
				this.title.setPaintFlags(this.title.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
				this.title.setText(title);              
				
				//String time = item.getBungaeTime().toString();
				String timeStr = (ei.getBungaeTimeConvert().getMonth()+1)+"월"+ei.getBungaeTimeConvert().getDate()+"일  "+ei.getBungaeTimeConvert().getHours()+"시"+ei.getBungaeTimeConvert().getMinutes()+"분";
				this.time.setText(timeStr);
				Log.d("time", timeStr);
				
				
				String host = ei.getBungaeHostId().toString();
				this.host.setText(host);
				
				
				String memberNum;
				
				if ( ei.getBungaeMax().equals("11") )
					memberNum = ei.getBungaeCur() + " / " + "∞";
				else
					memberNum = ei.getBungaeCur() + " / " + ei.getBungaeMax();
				
				this.memberNum.setText(memberNum);
				

				//history를 시간으로 판별
				
				//현재 시간을 생성
				Date nowDate = new Date();
				
				//현재시간과 선택한 시간간의 차이를 계산
				long difference = (nowDate.getTime()-ei.getBungaeTimeConvert().getTime())/1000;
				
				if (difference>3*60*60) {
					view.setOnClickListener(null);
					view.setOnLongClickListener(null);
					view.setLongClickable(false);

					this.category.setTextColor(Color.rgb(100, 100, 100));
					this.title.setTextColor(Color.rgb(100, 100, 100));
					this.time.setTextColor(Color.rgb(100, 100, 100));
					this.host.setTextColor(Color.rgb(100, 100, 100));
					this.memberNum.setTextColor(Color.rgb(100, 100, 100));
				}
			}
		}                
		return view;      
	}  
}