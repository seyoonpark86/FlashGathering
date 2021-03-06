/* 
 * NMapViewer.java $version 2010. 1. 1
 * 
 * Copyright 2010 NHN Corp. All rights Reserved. 
 * NHN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms. 
 */

package com.hunect.bungae;

import net.daum.mobilead.AdConfig;
import net.daum.mobilead.AdHttpListener;
import net.daum.mobilead.MobileAdView;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.nhn.android.maps.NMapActivity;
import com.nhn.android.maps.NMapCompassManager;
import com.nhn.android.maps.NMapController;
import com.nhn.android.maps.NMapLocationManager;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.nmapmodel.NMapError;
import com.nhn.android.maps.nmapmodel.NMapPlacemark;
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.maps.overlay.NMapPOIitem;
import com.nhn.android.mapviewer.overlay.NMapMyLocationOverlay;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay;

/**
 * Sample class for map viewer library.
 * 
 * @author kyjkim
 */
public class AddBungaeMap extends NMapActivity implements AdHttpListener {
	private static final String LOG_TAG = "NMapViewer";
	private static final boolean DEBUG = false;

	// set your API key which is registered for NMapViewer library.
	private static final String API_KEY = "377c2d68832d33b53ea3e0273e226b08";

	private MobileAdView adView = null;
	
	private FrameLayout mapLinearLayout = null;
	
	private MapContainerView mMapContainerView;

	private NMapView mMapView;
	private NMapController mMapController;

	private static final NGeoPoint NMAP_LOCATION_DEFAULT = new NGeoPoint(126.978371, 37.5666091);
	private static final int NMAP_ZOOMLEVEL_DEFAULT = 11;
	private static final int NMAP_VIEW_MODE_DEFAULT = NMapView.VIEW_MODE_VECTOR;
	private static final boolean NMAP_TRAFFIC_MODE_DEFAULT = false;
	private static final boolean NMAP_BICYCLE_MODE_DEFAULT = false;

	private static final String KEY_ZOOM_LEVEL = "NMapViewer.zoomLevel";
	private static final String KEY_CENTER_LONGITUDE = "NMapViewer.centerLongitudeE6";
	private static final String KEY_CENTER_LATITUDE = "NMapViewer.centerLatitudeE6";
	private static final String KEY_VIEW_MODE = "NMapViewer.viewMode";
	private static final String KEY_TRAFFIC_MODE = "NMapViewer.trafficMode";
	private static final String KEY_BICYCLE_MODE = "NMapViewer.bicycleMode";

	private SharedPreferences mPreferences;

	private NMapOverlayManager mOverlayManager;

	private NMapMyLocationOverlay mMyLocationOverlay;
	private NMapLocationManager mMapLocationManager;
	private NMapCompassManager mMapCompassManager;

	private NMapViewerResourceProvider mMapViewerResourceProvider;

	private NMapPOIdataOverlay mFloatingPOIdataOverlay;
	private NMapPOIitem mFloatingPOIitem;
	
	//private String locationTitle;
	
	private NMapPOIdata poiData;
	private NMapPOIitem item;
	
	private Button cancelButton;
	private Button saveButton;
	
	
	String longitude;
	String latitude;
	
	int createFlag = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addbungae_map);
		setTitle("번개 장소");
		
		initAdam();
		
		mapLinearLayout = (FrameLayout)findViewById(R.id.AddMaplinearLayout);
		
		
		// 할당 받은 clientId 설정
        AdConfig.setClientId("1c3aZ5ZT134b0fed431");
		
		// create map view
		mMapView = new NMapView(this);
		cancelButton = new Button(this);
		saveButton = new Button(this);
		
		cancelButton.setText("취소");
		saveButton.setText("저장");
		
		LinearLayout LLayout = new LinearLayout(this);
		LLayout.setOrientation(LinearLayout.HORIZONTAL);
		LLayout.setWeightSum(2.0f);
		
		
		LLayout.addView(cancelButton);
		LLayout.addView(saveButton);
		
		LinearLayout.LayoutParams lp2 = (LinearLayout.LayoutParams) cancelButton.getLayoutParams();
		
		lp2.setMargins(0, 10, 0, 0);
		lp2.weight = 1.0f;
		
		LinearLayout.LayoutParams lp3 = (LinearLayout.LayoutParams) saveButton.getLayoutParams();
		
		lp3.setMargins(0, 10, 0, 0);
		lp3.weight = 1.0f;
		
		


		// set a registered API key for Open MapViewer Library
		mMapView.setApiKey(API_KEY);

		// create parent view to rotate map view
		mMapContainerView = new MapContainerView(this);
		mMapContainerView.addView(mMapView);

		mapLinearLayout.addView(mMapContainerView);
		mapLinearLayout.addView(LLayout);
		
		
		
		// set the activity content to the parent view
		//setContentView(mMapContainerView);
		
		// initialize map view
		mMapView.setClickable(true);
		mMapView.setEnabled(true);
		mMapView.setFocusable(true);
		mMapView.setFocusableInTouchMode(true);
		mMapView.requestFocus();

		// register listener for map state changes
		mMapView.setOnMapStateChangeListener(onMapViewStateChangeListener);
		mMapView.setOnMapViewTouchEventListener(onMapViewTouchEventListener);
		mMapView.setOnMapViewDelegate(onMapViewTouchDelegate);

		// use map controller to zoom in/out, pan and set map center, zoom level etc.
		mMapController = mMapView.getMapController();

		// use built in zoom controls
		NMapView.LayoutParams lp = new NMapView.LayoutParams(LayoutParams.WRAP_CONTENT,
			LayoutParams.WRAP_CONTENT, NMapView.LayoutParams.BOTTOM_RIGHT);
		mMapView.setBuiltInZoomControls(true, lp);

		// create resource provider
		mMapViewerResourceProvider = new NMapViewerResourceProvider(this);

		// set data provider listener
		super.setMapDataProviderListener(onDataProviderListener);

		// create overlay manager
		mOverlayManager = new NMapOverlayManager(this, mMapView, mMapViewerResourceProvider);
		// register callout overlay listener to customize it.
		//mOverlayManager.setOnCalloutOverlayListener(onCalloutOverlayListener);
		mOverlayManager.canRefreshOverlayData(true);
		
		// location manager
		mMapLocationManager = new NMapLocationManager(this);
		mMapLocationManager.setOnLocationChangeListener(onMyLocationChangeListener);

		// compass manager
		mMapCompassManager = new NMapCompassManager(this);

		// create my location overlay
		mMyLocationOverlay = mOverlayManager.createMyLocationOverlay(mMapLocationManager, mMapCompassManager);
		
		// Button click!!
		cancelButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				Toast.makeText(AddBungaeMap.this, "번개 장소 설정 취소!", Toast.LENGTH_LONG).show();
				
//				AddBungaeMapClass.getInstance().addBungaeMapInfo.put("locationTitle", "");
//			 	AddBungaeMapClass.getInstance().addBungaeMapInfo.put("longitude", "");
//			 	AddBungaeMapClass.getInstance().addBungaeMapInfo.put("latitude", "");
				finish();
			}
		});
		
		saveButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				if ( createFlag == 1 )
				{
					AddBungaeMapClass.getInstance().addBungaeMapInfo.put("locationTitle", item.getTitle());
					AddBungaeMapClass.getInstance().addBungaeMapInfo.put("longitude", longitude);
					AddBungaeMapClass.getInstance().addBungaeMapInfo.put("latitude", latitude);
					
					Toast.makeText(AddBungaeMap.this, "번개가 장소가 설정되었습니다!", Toast.LENGTH_LONG).show();
					
					finish();
				}
				
				else
				{
					Toast.makeText(AddBungaeMap.this, "번개 장소를 설정해 주세요!", Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	@SuppressWarnings("deprecation")
	private void initAdam() {
		
        // 할당 받은 clientId 설정
        AdConfig.setClientId("TestClientId");
        
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
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if ( !(AddBungaeMapClass.getInstance().addBungaeMapInfo.get("locationTitle").equals("")) )
			locationOverlay();
		else
			startMyLocation();
	}

	@Override
	protected void onStop() {

		stopMyLocation();

		super.onStop();
	}

	@Override
	protected void onDestroy() {

		// save map view state such as map center position and zoom level.
		//saveInstanceState();

		super.onDestroy();
		
		if( adView != null ) {
        	adView.destroy();
            adView = null;
        }	
	}

	/* Test Functions */

	private void startMyLocation() {

		if (mMyLocationOverlay != null) {
			if (!mOverlayManager.hasOverlay(mMyLocationOverlay)) {
				mOverlayManager.addOverlay(mMyLocationOverlay);
			}

			if (mMapLocationManager.isMyLocationEnabled()) {

				if (!mMapView.isAutoRotateEnabled()) {
					mMyLocationOverlay.setCompassHeadingVisible(true);

					mMapCompassManager.enableCompass();

					mMapView.setAutoRotateEnabled(true, false);

					mMapContainerView.requestLayout();
				} else {
					stopMyLocation();
				}

				mMapView.postInvalidate();
			} else {
				boolean isMyLocationEnabled = mMapLocationManager.enableMyLocation(false);
				if (!isMyLocationEnabled) {
					Toast.makeText(AddBungaeMap.this, "Please enable a My Location source in system settings",
						Toast.LENGTH_LONG).show();

					Intent goToSettings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					startActivity(goToSettings);

					return;
				}
			}
		}
	}

	private void stopMyLocation() {
		if (mMyLocationOverlay != null) {
			mMapLocationManager.disableMyLocation();

			if (mMapView.isAutoRotateEnabled()) {
				mMyLocationOverlay.setCompassHeadingVisible(false);

				mMapCompassManager.disableCompass();

				mMapView.setAutoRotateEnabled(false, false);

				mMapContainerView.requestLayout();
			}
		}
	}

	private void addFloatingPOIdataOverlay() {
		// Remove overlays
		if (mMyLocationOverlay != null) {
			stopMyLocation();
			mOverlayManager.removeOverlay(mMyLocationOverlay);
		}

		mOverlayManager.clearOverlays();
		
		// Markers for POI item
		int marker1 = NMapPOIflagType.PIN;

		// set POI data
		//NMapPOIdata poiData = new NMapPOIdata(1, mMapViewerResourceProvider);
		poiData = new NMapPOIdata(1, mMapViewerResourceProvider);
		poiData.beginPOIdata(1);
		//NMapPOIitem item = poiData.addPOIitem(null, locationTitle, marker1, 0);
		item = poiData.addPOIitem(null, "장소를 입력하세요.", marker1, 0);
		//NMapPOIitem item = poiData.addPOIitem(127.0630205, 37.5091300, "Pizza 777-111", marker1, 0);
		if (item != null) {
			// initialize location to the center of the map view.
			item.setPoint(mMapController.getMapCenter());
			// set floating mode
			item.setFloatingMode(NMapPOIitem.FLOATING_TOUCH | NMapPOIitem.FLOATING_DRAG);
			// show right button on callout
			item.setRightButton(true);

			mFloatingPOIitem = item;
		}
		poiData.endPOIdata();

		// create POI data overlay
		NMapPOIdataOverlay poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);
		if (poiDataOverlay != null) {
			poiDataOverlay.setOnFloatingItemChangeListener(onPOIdataFloatingItemChangeListener);
			
			// set event listener to the overlay
			poiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);

			poiDataOverlay.selectPOIitem(0, false);

			mFloatingPOIdataOverlay = poiDataOverlay;
		}
	}
	
private void locationOverlay() {
		
		// Markers for POI item
				int markerId = NMapPOIflagType.PIN;

				
				
				// set POI data
				NMapPOIdata poiData = new NMapPOIdata(1, mMapViewerResourceProvider);
				poiData.beginPOIdata(1);
				poiData.addPOIitem(Double.parseDouble(AddBungaeMapClass.getInstance().addBungaeMapInfo.get("longitude")),
						Double.parseDouble(AddBungaeMapClass.getInstance().addBungaeMapInfo.get("latitude")),
						AddBungaeMapClass.getInstance().addBungaeMapInfo.get("locationTitle"), 
						markerId, 0);
				poiData.endPOIdata();

				// create POI data overlay
				NMapPOIdataOverlay poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);

				// set event listener to the overlay
				poiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);

				// select an item
				poiDataOverlay.selectPOIitem(0, true);

				// show all POI data
				//poiDataOverlay.showAllPOIdata(0);
	}

	/* NMapDataProvider Listener */
	private final NMapActivity.OnDataProviderListener onDataProviderListener = new NMapActivity.OnDataProviderListener() {

		@Override
		public void onReverseGeocoderResponse(NMapPlacemark placeMark, NMapError errInfo) {

			if (DEBUG) {
				Log.i(LOG_TAG, "onReverseGeocoderResponse: placeMark="
					+ ((placeMark != null) ? placeMark.toString() : null));
			}

			if (errInfo != null) {
				Log.e(LOG_TAG, "Failed to findPlacemarkAtLocation: error=" + errInfo.toString());

				Toast.makeText(AddBungaeMap.this, errInfo.toString(), Toast.LENGTH_LONG).show();
				return;
			}

			if (mFloatingPOIitem != null && mFloatingPOIdataOverlay != null) {
				mFloatingPOIdataOverlay.deselectFocusedPOIitem();
				
				if (placeMark != null) {
					mFloatingPOIitem.setTitle(placeMark.toString());
				}
				mFloatingPOIdataOverlay.selectPOIitemBy(mFloatingPOIitem.getId(), false);
			}
		}

	};

	/* MyLocation Listener */
	private final NMapLocationManager.OnLocationChangeListener onMyLocationChangeListener = new NMapLocationManager.OnLocationChangeListener() {

		@Override
		public boolean onLocationChanged(NMapLocationManager locationManager, NGeoPoint myLocation) {

			if (mMapController != null) {
				mMapController.animateTo(myLocation);
			}

			return true;
		}

		@Override
		public void onLocationUpdateTimeout(NMapLocationManager locationManager) {

			// stop location updating
			//			Runnable runnable = new Runnable() {
			//				public void run() {										
			//					stopMyLocation();
			//				}
			//			};
			//			runnable.run();	

			Toast.makeText(AddBungaeMap.this, "Your current location is temporarily unavailable.", Toast.LENGTH_LONG).show();
		}

		@Override
		public void onLocationUnavailableArea(NMapLocationManager locationManager, NGeoPoint myLocation) {

			Toast.makeText(AddBungaeMap.this, "Your current location is unavailable area.", Toast.LENGTH_LONG).show();

			stopMyLocation();
		}

	};

	/* MapView State Change Listener*/
	private final NMapView.OnMapStateChangeListener onMapViewStateChangeListener = new NMapView.OnMapStateChangeListener() {

		@Override
		public void onMapInitHandler(NMapView mapView, NMapError errorInfo) {

			if (errorInfo == null) { // success
				// restore map view state such as map center position and zoom level.
				restoreInstanceState();

			} else { // fail
				Log.e(LOG_TAG, "onFailedToInitializeWithError: " + errorInfo.toString());

				Toast.makeText(AddBungaeMap.this, errorInfo.toString(), Toast.LENGTH_LONG).show();
			}
		}

		@Override
		public void onAnimationStateChange(NMapView mapView, int animType, int animState) {
			if (DEBUG) {
				Log.i(LOG_TAG, "onAnimationStateChange: animType=" + animType + ", animState=" + animState);
			}
		}

		@Override
		public void onMapCenterChange(NMapView mapView, NGeoPoint center) {
			if (DEBUG) {
				Log.i(LOG_TAG, "onMapCenterChange: center=" + center.toString());
			}
		}

		@Override
		public void onZoomLevelChange(NMapView mapView, int level) {
			if (DEBUG) {
				Log.i(LOG_TAG, "onZoomLevelChange: level=" + level);
			}
		}

		@Override
		public void onMapCenterChangeFine(NMapView mapView) {

		}
	};

	private final NMapView.OnMapViewTouchEventListener onMapViewTouchEventListener = new NMapView.OnMapViewTouchEventListener() {

		@Override
		public void onLongPress(NMapView mapView, MotionEvent ev) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onLongPressCanceled(NMapView mapView) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSingleTapUp(NMapView mapView, MotionEvent ev) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onTouchDown(NMapView mapView, MotionEvent ev) {

		}

		@Override
		public void onScroll(NMapView mapView, MotionEvent e1, MotionEvent e2) {
		}

	};

	private final NMapView.OnMapViewDelegate onMapViewTouchDelegate = new NMapView.OnMapViewDelegate() {

		@Override
		public boolean isLocationTracking() {
			if (mMapLocationManager != null) {
				if (mMapLocationManager.isMyLocationEnabled()) {
					return mMapLocationManager.isMyLocationFixed();
				}
			}
			return false;
		}

	};

	/* POI data State Change Listener*/
	private final NMapPOIdataOverlay.OnStateChangeListener onPOIdataStateChangeListener = new NMapPOIdataOverlay.OnStateChangeListener() {
		
		@Override
		public void onCalloutClick(NMapPOIdataOverlay poiDataOverlay, NMapPOIitem item) {
						
			enterLocationAlert();
			
		}

		@Override
		public void onFocusChanged(NMapPOIdataOverlay poiDataOverlay, NMapPOIitem item) {
			if (DEBUG) {
				if (item != null) {
					Log.i(LOG_TAG, "onFocusChanged: " + item.toString());
				} else {
					Log.i(LOG_TAG, "onFocusChanged: ");
				}
			}
		}
	};

	private final NMapPOIdataOverlay.OnFloatingItemChangeListener onPOIdataFloatingItemChangeListener = new NMapPOIdataOverlay.OnFloatingItemChangeListener() {

		@Override
		public void onPointChanged(NMapPOIdataOverlay poiDataOverlay, NMapPOIitem item) {

			item.setTitle("장소를 입력하세요.");
			mFloatingPOIdataOverlay.deselectFocusedPOIitem();
			mFloatingPOIdataOverlay.selectPOIitem(0, true);
			createFlag = 0;

		}
	};
	
//	private final NMapOverlayManager.OnCalloutOverlayListener onCalloutOverlayListener = new NMapOverlayManager.OnCalloutOverlayListener() {
//
//		@Override
//		public NMapCalloutOverlay onCreateCalloutOverlay(NMapOverlay itemOverlay, NMapOverlayItem overlayItem,
//			Rect itemBounds) {
//			
//			// use custom callout overlay
//			return new NMapCalloutCustomOverlay(itemOverlay, overlayItem, itemBounds, mMapViewerResourceProvider);
//
//			// set basic callout overlay
//			//return new NMapCalloutBasicOverlay(itemOverlay, overlayItem, itemBounds);			
//		}
//
//	};

	/* Local Functions */

	private void restoreInstanceState() {
		mPreferences = getPreferences(MODE_PRIVATE);

		int longitudeE6 = mPreferences.getInt(KEY_CENTER_LONGITUDE, NMAP_LOCATION_DEFAULT.getLongitudeE6());
		int latitudeE6 = mPreferences.getInt(KEY_CENTER_LATITUDE, NMAP_LOCATION_DEFAULT.getLatitudeE6());
		int level = mPreferences.getInt(KEY_ZOOM_LEVEL, NMAP_ZOOMLEVEL_DEFAULT);
		int viewMode = mPreferences.getInt(KEY_VIEW_MODE, NMAP_VIEW_MODE_DEFAULT);
		boolean trafficMode = mPreferences.getBoolean(KEY_TRAFFIC_MODE, NMAP_TRAFFIC_MODE_DEFAULT);
		boolean bicycleMode = mPreferences.getBoolean(KEY_BICYCLE_MODE, NMAP_BICYCLE_MODE_DEFAULT);

		mMapController.setMapViewMode(viewMode);
		mMapController.setMapViewTrafficMode(trafficMode);
		mMapController.setMapViewBicycleMode(bicycleMode);
		mMapController.setMapCenter(new NGeoPoint(longitudeE6, latitudeE6), level);
	}


	/* Menus */
	private static final int MENU_ITEM_CLEAR_MAP = 10;
	private static final int MENU_ITEM_MY_LOCATION = 20;
	private static final int MENU_ITEM_SET_LOCATION = 30;

	/**
	 * Invoked during init to give the Activity a chance to set up its Menu.
	 * 
	 * @param menu the Menu to which entries may be added
	 * @return true
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuItem menuItem = null;

		menuItem = menu.add(Menu.NONE, MENU_ITEM_CLEAR_MAP, Menu.CATEGORY_SECONDARY, "지우기");
		menuItem.setIcon(android.R.drawable.ic_menu_revert);

		menuItem = menu.add(0, MENU_ITEM_MY_LOCATION, Menu.CATEGORY_SECONDARY, "내위치");
		menuItem.setIcon(android.R.drawable.ic_menu_mylocation);
		
		menuItem = menu.add(0, MENU_ITEM_SET_LOCATION, Menu.CATEGORY_SECONDARY, "장소 설정");
		menuItem.setIcon(android.R.drawable.ic_menu_add);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu pMenu) {
		super.onPrepareOptionsMenu(pMenu);

		int viewMode = mMapController.getMapViewMode();
		boolean isTraffic = mMapController.getMapViewTrafficMode();

		pMenu.findItem(MENU_ITEM_CLEAR_MAP).setEnabled(
			(viewMode != NMapView.VIEW_MODE_VECTOR) || isTraffic || mOverlayManager.sizeofOverlays() > 0);

		if (mMyLocationOverlay == null) {
			pMenu.findItem(MENU_ITEM_MY_LOCATION).setEnabled(false);
		}

		return true;
	}

	/**
	 * Invoked when the user selects an item from the Menu.
	 * 
	 * @param item the Menu entry which was selected
	 * @return true if the Menu item was legit (and we consumed it), false
	 *         otherwise
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case MENU_ITEM_CLEAR_MAP:
				if (mMyLocationOverlay != null) {
					stopMyLocation();
					mOverlayManager.removeOverlay(mMyLocationOverlay);
				}

				mMapController.setMapViewMode(NMapView.VIEW_MODE_VECTOR);
				mMapController.setMapViewTrafficMode(false);
				mMapController.setMapViewBicycleMode(false);

				mOverlayManager.clearOverlays();

				return true;

			case MENU_ITEM_MY_LOCATION:
				startMyLocation();
				return true;
				
			case MENU_ITEM_SET_LOCATION:
				addFloatingPOIdataOverlay();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/** 
	 * Container view class to rotate map view.
	 */
	private class MapContainerView extends ViewGroup {

		public MapContainerView(Context context) {
			super(context);
		}

		@Override
		protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
			final int width = getWidth();
			final int height = getHeight();
			final int count = getChildCount();
			for (int i = 0; i < count; i++) {
				final View view = getChildAt(i);
				final int childWidth = view.getMeasuredWidth();
				final int childHeight = view.getMeasuredHeight();
				final int childLeft = (width - childWidth) / 2;
				final int childTop = (height - childHeight) / 2;
				view.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
			}

			if (changed) {
				mOverlayManager.onSizeChanged(width, height);
			}
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			int w = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
			int h = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
			int sizeSpecWidth = widthMeasureSpec;
			int sizeSpecHeight = heightMeasureSpec;

			final int count = getChildCount();
			for (int i = 0; i < count; i++) {
				final View view = getChildAt(i);

				if (view instanceof NMapView) {
					if (mMapView.isAutoRotateEnabled()) {
						int diag = (((int)(Math.sqrt(w * w + h * h)) + 1) / 2 * 2);
						sizeSpecWidth = MeasureSpec.makeMeasureSpec(diag, MeasureSpec.EXACTLY);
						sizeSpecHeight = sizeSpecWidth;
					}
				}

				view.measure(sizeSpecWidth, sizeSpecHeight);
			}
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}
	
	private void enterLocationAlert()
	{
		LayoutInflater inflator = LayoutInflater.from(this);
    	View addView = inflator.inflate(R.layout.enter_location, null);
    	
    	final EditText locationEnter;
    	
    	locationEnter = (EditText)addView.findViewById(R.id.locationEnter);
    	
    	AlertDialog.Builder alert =  new AlertDialog.Builder(this);
    	alert.setTitle("번개 장소");
    	alert.setView(addView);
    	
    	alert.setPositiveButton("확인", new DialogInterface.OnClickListener() 
    	{	
    		
			@Override
			public void onClick(DialogInterface dialog, int whichButton) 
			{
				if ( locationEnter.getText().toString().equals("") )
				{
					enterLocationAlert();
					Toast.makeText(AddBungaeMap.this, "장소를 입력하세요.", Toast.LENGTH_LONG).show();
				}
				
				else
				{
					item.setTitle(locationEnter.getText().toString());
					mFloatingPOIdataOverlay.deselectFocusedPOIitem();
					mFloatingPOIdataOverlay.selectPOIitem(0, true);
					NGeoPoint point = item.getPoint();
					
					longitude = String.valueOf(point.longitude);
					latitude = String.valueOf(point.latitude);
					
					createFlag = 1;
				}
			}
		});
    	alert.setNegativeButton("취소", new DialogInterface.OnClickListener() 
		{
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
				item.setTitle("장소를 입력하세요.");
				mFloatingPOIdataOverlay.deselectFocusedPOIitem();
				mFloatingPOIdataOverlay.selectPOIitem(0, true);
				
			}
		});
    	alert.show();
	}
}
