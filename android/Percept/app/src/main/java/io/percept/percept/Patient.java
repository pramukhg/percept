package io.percept.percept;

import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

public class Patient {

    @Override
    public String toString() {
        return "Patient{" +
                "dob='" + dob + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", vitals_0=" + vitals_0 +
                '}';
    }

    public String dob;
    public String firstName;
    public String lastName;
    public String qrcode;
    public Map<String, String> vitals_0;
    public Map<String, String> vitals_1;
    public Map<String, String> vitals_2;
    public Map<String, String> vitals_3;
    public Map<String, String> vitals_4;
    public Map<String, String> vitals_5;
    public Map<String, String> vitals_6;
    public Map<String, String> vitals_7;
    public Map<String, String> vitals_8;
    public Map<String, String> vitals_9;



    public Patient(String dob, String firstName, String lastName, String qrcode) {
        this.dob = dob;
        this.firstName = firstName;
        this.lastName = lastName;
        this.qrcode = qrcode;
    }

    //keep this, essential
    public Patient(){}

    private String[] getValues(String query){
        String[] values = new String[10];
        values[0] = vitals_0.get(query);
        values[1] = vitals_1.get(query);
        values[2] = vitals_2.get(query);
        values[3] = vitals_3.get(query);
        values[4] = vitals_4.get(query);
        values[5] = vitals_5.get(query);
        values[6] = vitals_6.get(query);
        values[7] = vitals_7.get(query);
        values[8] = vitals_8.get(query);
        values[9] = vitals_8.get(query);
        return values;
    }

    private int [] getNormalizedTimestamps(){
        String [] timestamp = getValues("timestamp");

        //convert timestamp to doubles
        Long [] tme = new Long[timestamp.length];
        for (int i = 0; i < timestamp.length; i ++){
            tme[i] = Long.parseLong(timestamp[i]);
        }
        Long min = Collections.min(Arrays.asList(tme));

        int [] normalizedTimestamps = new int[timestamp.length];
        for (int i = 0; i < timestamp.length; i ++){
            normalizedTimestamps[i] = (int)(tme[i] - min);
        }
        return normalizedTimestamps;
    }

    public DataPoint[] heartRates() {
        String [] heartRates = getValues("heartRate");
        int [] normalizedTimestamps = getNormalizedTimestamps();

        ArrayList<DataPoint> retVal = new ArrayList<DataPoint>();
        for (int i = 0; i < normalizedTimestamps.length; i ++){
            retVal.add(new DataPoint((double)normalizedTimestamps[i], Double.parseDouble(heartRates[i])));
        }
        Collections.sort(retVal, new Comparator<DataPoint>() {
            @Override
            public int compare(DataPoint o1, DataPoint o2) {
                return (int)(o1.getX() - o2.getX());
            }
        });
        return retVal.toArray(new DataPoint[retVal.size()]);
    }

    public DataPoint[] temperature() {
        String [] heartRates = getValues("temp");
        int [] normalizedTimestamps = getNormalizedTimestamps();

        ArrayList<DataPoint> retVal = new ArrayList<DataPoint>();
        for (int i = 0; i < normalizedTimestamps.length; i ++){
            retVal.add(new DataPoint((double)normalizedTimestamps[i], Double.parseDouble(heartRates[i])));
        }
        Collections.sort(retVal, new Comparator<DataPoint>() {
            @Override
            public int compare(DataPoint o1, DataPoint o2) {
                return (int)(o1.getX() - o2.getX());
            }
        });
        return retVal.toArray(new DataPoint[retVal.size()]);
    }



}

