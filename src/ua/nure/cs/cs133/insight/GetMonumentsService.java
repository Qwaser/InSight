/**
 *
 */
package ua.nure.cs.cs133.insight;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ua.nure.cs.cs133.insight.geo.GPSTracker;
import ua.nure.cs.cs133.insight.server.Monument;
import ua.nure.cs.cs133.insight.server.ServerResponse;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * @author gavr
 *
 */
public class GetMonumentsService extends Service {
	private static final int NEAR_MONUMENT_DISTANCE = 200;

	private static final int CHECK_NEAR_MOUNUMETS_INTERVAL = 500;

	private static final int HTTP_REQUEST_INTERVAL = 1000 * 30;

	private static final String PARAM_LONGITUDE = "LocLon";

	private static final String PARAM_LATITUDE = "LocLat";

	private static final String SERVER_URL = "http://insightapp.azurewebsites.net/sso.php";
	//private static final String SERVER_URL = "http://192.168.0.105/sso.php";

	private static final String TAG = "ua.nure.cs.cs133.insight";
	private final IBinder getMonumentsBinder = new GetMonumentsBinder();

	private ServerResponse serverResponse = null;
	private List<Monument> currentMonuments = new ArrayList<Monument>();
	private String resp = new String();

	private double latitude;
	private double longitude;





	@Override
	public IBinder onBind(Intent intent) {
		Intent myIntent = new Intent(this, GetMonumentsService.class);
		startService(myIntent);
		return getMonumentsBinder;
	}


	public GetMonumentsService() {
		super();
	}



	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		final GPSTracker mGPS = new GPSTracker(this);
		runServerRequests(mGPS);
		getCurrentMonuments(mGPS);
		return Service.START_STICKY;
	}



	private void getCurrentMonuments(final GPSTracker mGPS) {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				while (true) {

					try {
						if (mGPS.canGetLocation()) {
							getCoordinates(mGPS);

							currentMonuments = new ArrayList<Monument>();
							if(serverResponse != null){
								for (Monument monument : serverResponse.getPlaces()) {
									double distance = distFrom(latitude, longitude,
											monument.getLatitude(), monument.getLongitude());
									if (distance < NEAR_MONUMENT_DISTANCE) {
										monument.setDistance(distance);
										currentMonuments.add(monument);
									}
								}
							}
						}
						Thread.sleep(CHECK_NEAR_MOUNUMETS_INTERVAL);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

		};
		Thread thread = new Thread(runnable);
		thread.start();
	}



	private void runServerRequests(final GPSTracker mGPS) {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				while (true) {

					try {
						if (mGPS.canGetLocation()) {
							getCoordinates(mGPS);

							HttpClient httpClient = new DefaultHttpClient();
							HttpGet request = new HttpGet();

							try {
								Uri uri = Uri.parse(SERVER_URL).buildUpon()
										.appendQueryParameter(PARAM_LATITUDE, toStr(mGPS.getLatitude()))
										.appendQueryParameter(PARAM_LONGITUDE, toStr(mGPS.getLongitude()))
										.build();

								request.setURI(new URI(uri.toString()));

								request.addHeader("content-type", "application/x-www-form-urlencoded");
								request.addHeader("Accept", "application/json");

								HttpResponse response = httpClient.execute(request);
								BufferedReader rd = new BufferedReader(
										new InputStreamReader(response.getEntity().getContent()));

								StringBuffer result = new StringBuffer();
								String line = "";
								while ((line = rd.readLine()) != null) {
									result.append(line);
								}


								serverResponse = getResponceObject(result.toString());
								resp = serverResponse.getMessage();


							} catch (ClientProtocolException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							} catch (URISyntaxException e) {
								e.printStackTrace();
							}
						}
						Log.i(TAG, "try go to server");
						Thread.sleep(HTTP_REQUEST_INTERVAL);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}

			}

		};

		Thread thread = new Thread(runnable);
		thread.start();
	}

	private void getCoordinates(final GPSTracker mGPS) {
		mGPS.getLocation();
		longitude = mGPS.getLongitude();
		latitude = mGPS.getLatitude();
	}

	private double distFrom(double lat1, double lng1, double lat2, double lng2) {
		double earthRadius = 6371000; // meters
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2)
				* Math.sin(dLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		float dist = (float) (earthRadius * c);
		return dist;
	}

	protected ServerResponse getResponceObject(String str) {
		ServerResponse serverResponse = new ServerResponse();
		try {
			JSONObject o = new JSONObject(str);
			serverResponse.setMessage(o.getString("message"));
			serverResponse.setSuccess(o.getInt("success"));
			JSONArray placesObjects = o.getJSONArray("places");
			List<Monument> places = new ArrayList<Monument>();
			for (int i = 0; i < placesObjects.length(); i++) {
				JSONObject monumentObj = placesObjects.getJSONObject(i);
				Monument monument = new Monument();
				monument.setId(monumentObj.getLong("id"));
				monument.setName(monumentObj.getString("name"));
				monument.setDesc(monumentObj.getString("desc"));
				monument.setPic(monumentObj.getString("pic"));
				monument.setBuilder(monumentObj.getString("builder"));
				monument.setType(monumentObj.getInt("type"));

				monument.setLatitude(monumentObj.getDouble("locLat"));
				monument.setLongitude(monumentObj.getDouble("locLon"));

				places.add(monument);
			}
			serverResponse.setPlaces(places);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return serverResponse;
	}



	private String toStr(double val) {
		return new StringBuffer().append(val).toString();
	}


	public List<Monument> getMonuments() {
		return currentMonuments;
	}
	public String getMonumentsAsString() {
		return resp;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public class GetMonumentsBinder extends Binder {
		GetMonumentsService getService() {
			return GetMonumentsService.this;
		}
	}

}
