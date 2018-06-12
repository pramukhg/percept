package io.percept.percept;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.view.WindowUtils;
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
import android.renderscript.Sampler;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
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
    //points for heart rate graph
    LineGraphSeries<DataPoint> heartRateSeries;

    //keeps track of the current patient
    Patient state;

    //current patient firebase listener
    ValueEventListener listener;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);

//        startQRActivity();
        setContentView(R.layout.user);
        configureUI();

        detectAbnormalTemp();
    }

    //if anyones temperature goes over 100, trigger an alert.
    private void detectAbnormalTemp(){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("Patients");
        final Activity _this = this;

        myRef.orderByChild("temp").startAt(100).addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChild("dob")){
                    if (dataSnapshot.getChildrenCount() != 0)
                        dataSnapshot = dataSnapshot.getChildren().iterator().next();
                    else
                        //first call
                        return;
                }


                Patient patient = dataSnapshot.getValue(Patient.class);
                //the alert
                Toast.makeText(_this, "URGENT: " + patient.firstName + " " + patient.lastName +
                       " has temperature of " + patient.temp + " F", Toast.LENGTH_LONG).show();
                myRef.removeEventListener(this);

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("CAM", "onCancelled", databaseError.toException());
            }
        });
    }

    //configure the UI of the graphs
    private void configureUI(){
        GraphView heartRateGraph = (GraphView) findViewById(R.id.heartRate);
        heartRateSeries = new LineGraphSeries<DataPoint>(new DataPoint[] {

        });
        heartRateGraph.addSeries(heartRateSeries);
        heartRateGraph.getViewport().setYAxisBoundsManual(true);
        heartRateGraph.getViewport().setMinY(65);
        heartRateGraph.getViewport().setMaxY(90);
        heartRateGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        heartRateGraph.getGridLabelRenderer().setGridStyle( GridLabelRenderer.GridStyle.NONE );
    }

    //after any camera or voice activity finishes it heads here.
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "Bad Result", Toast.LENGTH_LONG).show();
            return;
        }
        switch (requestCode) {
            case IntentIntegrator.REQUEST_CODE:
                IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
                if (intentResult == null) {
                    assert false;
                }
                Log.i("CAM", "Scanned");
                Toast.makeText(this, "Scanned: " + intentResult.getContents(), Toast.LENGTH_LONG).show();
                getPatientDataFromQR(intentResult.getContents());
                break;
            case R.id.voicepatient:
                getPatientDataFromString(intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0));
                break;
            case R.id.voicenote:
                setPatientNote(intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0));
                break;
            default:
                assert false;

        }
    }

    //doctor wants to take a note of the patient
    private void setPatientNote(String note){
        Log.i("CAM", "note " + note);
        if (state == null){
            Toast.makeText(this, "No Patient Selected", Toast.LENGTH_LONG).show();
            return;
        }

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Patients");
        myRef.child(state.key).child("notes").setValue(note);

        populatePatientUI(state);

    }

    //doctor has read name aloud, saerching for name from database to populate Patient ui
    private void getPatientDataFromString(String name){
        Log.i("CAM", "patient name " + name);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Patients");
        if (listener != null)
            myRef.removeEventListener(listener);
        listener = myRef.orderByChild("firstName").startAt(name).endAt(name).addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild("dob")){
                    dataSnapshot = dataSnapshot.getChildren().iterator().next();
                }
                Log.i("CAM", "patient result " + dataSnapshot.toString());
                Patient patient = dataSnapshot.getValue(Patient.class);
                patient.key = dataSnapshot.getKey();
                populatePatientUI(patient);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("CAM", "onCancelled", databaseError.toException());
            }
        });

    }

    //search for QR code from DB to populate patient UI
    private void getPatientDataFromQR(String qrcode){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Patients");

        Query qrQuery = myRef.child(qrcode);
        Log.i("CAM", qrcode);

        if (listener != null)
            myRef.removeEventListener(listener);
        listener = qrQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Patient patient = dataSnapshot.getValue(Patient.class);
                patient.key = dataSnapshot.getKey();
                populatePatientUI(patient);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("CAM", "onCancelled", databaseError.toException());
            }
        });
    }


    //fill out UI elements defined in user.xml
    private void populatePatientUI(Patient patient){
        state = patient;
        Log.i("CAM", patient.toString());
        TextView name = (TextView)findViewById(R.id.userName);
        TextView dob = (TextView)findViewById(R.id.userDOB);

        name.setText(patient.firstName + " " + patient.lastName);
        dob.setText(patient.dob);
        DataPoint[] db = patient.heartRates();

        ((TextView) findViewById(R.id.temp)).setText(patient.temp + " F");
        ((TextView) findViewById(R.id.notesmain)).setText(patient.notes);
        ((TextView) findViewById(R.id.notestriage)).setText(patient.triage);
        heartRateSeries.resetData(db);
    }

    //start the QR code reader
    private void startQRActivity(){
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES );
        integrator.setPrompt("Scan Code");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    /* Menu code
     * refer to menu.xml for menu values
     */
    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS){
            getMenuInflater().inflate(R.menu.menu, menu);
            return true;
        }
        return super.onCreatePanelMenu(featureId, menu);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        // check to make sure feature ID is for voice
        if (item.getItemId() == R.id.qr_activity) {
            startQRActivity();
            return true;
        }

        // all voice commands trigger an voice command intent. Will be able to use
        //transcribed text in app
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        // will create the microphone bubble to start activity for result
        startActivityForResult(intent, item.getItemId());

        return true;
    }
    //open options on key down
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            openOptionsMenu();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}


