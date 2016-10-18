package ua.nure.cs.cs133.insight;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import ua.nure.cs.cs133.insight.GetMonumentsService.GetMonumentsBinder;
import ua.nure.cs.cs133.insight.server.Monument;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private GetMonumentsService getMonumentsService;

	private TextView numMonuments;
	private TextView monumentName;
	private TextView currentCoordinates;
	private TextView monumentDescription;
	private TextView monumentBuilder;
	private TextView monumentDistance;

	private ScrollView monumentInfo;

	private ImageView monumentImage;

	private Button prevMonumentBtn;
	private Button nextMonumentBtn;

	private Bitmap bitmap;
	private ProgressDialog pDialog;

	private ProgressDialog loadApplicationDialog;

	private int displayMonumentIndex = 0;



	public void getMonumentsList(View view) {
		showInformation();
	}
	public void showPrevMonument(View view) {
		int index = displayMonumentIndex - 1;
		displayMonumentIndex = index >= 0 ? index : 0;
		showInformation();
	}
	public void showNextMonument(View view) {
		int index = displayMonumentIndex + 1;
		List<Monument> monuments = getMonumentsService.getMonuments();
		if(monuments != null && !monuments.isEmpty()){
			displayMonumentIndex = index < monuments.size() ? index : (monuments.size() - 1);
		} else {
			displayMonumentIndex = 0;
		}
		showInformation();
	}

	private void showInformation() {
		List<Monument> monuments = getMonumentsService.getMonuments();
		if(monuments != null && !monuments.isEmpty()){
			prevMonumentBtn.setVisibility(View.VISIBLE);
			nextMonumentBtn.setVisibility(View.VISIBLE);
			monumentInfo.setVisibility(View.VISIBLE);

			monumentInfo.fullScroll(View.FOCUS_UP);

			monumentImage.setImageResource(R.drawable.ic_launcher);

			numMonuments.setText(Integer.valueOf(monuments.size()).toString());
			Monument monument = displayMonumentIndex >= 0 && displayMonumentIndex < monuments.size()
					? monuments.get(displayMonumentIndex) : null;
			if(monument != null){
				monumentName.setText(monument.getName());
				new LoadImage().execute(monument.getPic());
				monumentDescription.setText(monument.getDesc());
				monumentBuilder.setText(monument.getBuilder());
				monumentDistance.setText(String.format("%.01f m", monument.getDistance()));
			}
			prevMonumentBtn.setEnabled(displayMonumentIndex > 0);
			nextMonumentBtn.setEnabled(displayMonumentIndex < (monuments.size() - 1));

		} else {
			numMonuments.setText(Integer.valueOf(0).toString());
			prevMonumentBtn.setVisibility(View.INVISIBLE);
			nextMonumentBtn.setVisibility(View.INVISIBLE);
			monumentInfo.setVisibility(View.INVISIBLE);
		}

		currentCoordinates.setText(getCurrentCoordinates(
				getMonumentsService.getLatitude(), getMonumentsService.getLongitude()));
	}

	private String getCurrentCoordinates(double latitude, double longitude) {
		StringBuffer str = new StringBuffer();
		str.append(Location.convert(latitude, Location.FORMAT_SECONDS)).append("; ")
				.append(Location.convert(longitude, Location.FORMAT_SECONDS));
		return str.toString();
	}



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		numMonuments = (TextView)findViewById(R.id.numMonuments);
		monumentName = (TextView)findViewById(R.id.monumentName);
		currentCoordinates = (TextView)findViewById(R.id.currentCoordinates);
		monumentDescription = (EditText)findViewById(R.id.monumentDescription);
		monumentBuilder = (TextView)findViewById(R.id.monumentBuilder);
		monumentDistance = (TextView)findViewById(R.id.monumentDistance);
		monumentInfo = (ScrollView)findViewById(R.id.monumentInfoScrollView);
		prevMonumentBtn = (Button)findViewById(R.id.prevMonumentBtn);
		nextMonumentBtn = (Button)findViewById(R.id.netxMonumentBtn);
		monumentImage = (ImageView)findViewById(R.id.monumentImage);

		Intent intent = new Intent(this, GetMonumentsService.class);
		bindService(intent, getMonumentsConnection, Context.BIND_AUTO_CREATE);


		loadApplicationDialog = new ProgressDialog(MainActivity.this);
		loadApplicationDialog.setMessage("Loading Application ....");
		loadApplicationDialog.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private ServiceConnection getMonumentsConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			//do nothing
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			GetMonumentsBinder binder = (GetMonumentsBinder) service;
			getMonumentsService = binder.getService();

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			loadApplicationDialog.dismiss();
			showInformation();

		}
	};

	private class LoadImage extends AsyncTask<String, String, Bitmap> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(MainActivity.this);
			pDialog.setMessage("Loading Image ....");
			pDialog.show();

		}

		@Override
		protected Bitmap doInBackground(String... args) {
			try {
				bitmap = BitmapFactory.decodeStream((InputStream) new URL(
						args[0]).getContent());

			} catch (Exception e) {
				e.printStackTrace();
			}
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap image) {

			if (image != null) {
				monumentImage.setImageBitmap(image);
				pDialog.dismiss();

			} else {

				pDialog.dismiss();
				Toast.makeText(MainActivity.this,
						"Image Does Not exist or Network Error",
						Toast.LENGTH_SHORT).show();

			}
		}
	}
}
