package io.percept.percept;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

/**
 * An {@link Activity} showing a tuggable "Hello World!" card.
 * <p>
 * The main content view is composed of a one-card {@link CardScrollView} that provides tugging
 * feedback to the user when swipe gestures are detected.
 * If your Glassware intends to intercept swipe gestures, you should set the content view directly
 * and use a {@link com.google.android.glass.touchpad.GestureDetector}.
 *
 * @see <a href="https://developers.google.com/glass/develop/gdk/touch">GDK Developer Guide</a>
 */
public class MainActivity extends Activity {



    private View mView;
    private static final int CAMERA_REQUEST = 0;
    LineGraphSeries<DataPoint> heartRateSeries;
    LineGraphSeries<DataPoint> tempSeries;


    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        startQRActivity();
        setContentView(R.layout.user);
        GraphView heartRateGraph = (GraphView) findViewById(R.id.heartRate);
        heartRateSeries = new LineGraphSeries<DataPoint>(new DataPoint[] {

        });
        heartRateGraph.addSeries(heartRateSeries);
        heartRateGraph.getViewport().setYAxisBoundsManual(true);
        heartRateGraph.getViewport().setMinY(65);
        heartRateGraph.getViewport().setMaxY(90);
        heartRateGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        heartRateGraph.getGridLabelRenderer().setGridStyle( GridLabelRenderer.GridStyle.NONE );


        GraphView tempGraph = (GraphView) findViewById(R.id.temp);
        tempSeries = new LineGraphSeries<DataPoint>(new DataPoint[] {

        });
        tempGraph.addSeries(tempSeries);
        tempGraph.getViewport().setYAxisBoundsManual(true);
        tempGraph.getViewport().setMinY(90);
        tempGraph.getViewport().setMaxY(100);
        tempGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        super.onActivityResult(requestCode, resultCode, intent);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if(intentResult != null) {
            if(intentResult.getContents() == null) {
                Log.i("CAM", "Cancelled");
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
                startQRActivity();
            } else {
                Log.i("CAM", "Scanned");
                Toast.makeText(this, "Scanned: " + intentResult.getContents(), Toast.LENGTH_LONG).show();
                getPatientData(intentResult.getContents());
            }
        }
    }

    private void getPatientData(String qrcode){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Patients");

        Query qrQuery = myRef.child(qrcode);
        Log.i("CAM", qrcode);

        qrQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Patient patient = dataSnapshot.getValue(Patient.class);
                Log.i("CAM", patient.toString());
                TextView name = (TextView)findViewById(R.id.userName);
                TextView dob = (TextView)findViewById(R.id.userDOB);

                name.setText(patient.firstName + patient.lastName);
                dob.setText(patient.dob);
                DataPoint[] db = patient.heartRates();
//                Log.i("CAM", Arrays.toString(db));

                tempSeries.resetData(patient.temperature());
                heartRateSeries.resetData(db);

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("CAM", "onCancelled", databaseError.toException());
            }
        });
    }

    private void startQRActivity(){
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES );
        integrator.setPrompt("Scan Code");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }
}
