package com.example.runningapp.shared;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


public class GPXInOut  {
    public void ingest(AppCompatActivity context, OnIngest callback){
        try {
            GPXInOut self = this;

            class ResultCallback implements ActivityResultCallback<Uri> {
                public String fileToOpen;
                @Override
                public void onActivityResult(Uri o) {
                    self.loadFile(context, o, callback);
//                    Vector<ActivitySet> activities = new Vector<>();
//                    ActivitySet set = new ActivitySet(new Activity());
//                    ActivitySet.SegmentSet s = new ActivitySet.SegmentSet(
//                            new Segment()
//                    );
//
//                    s.activityRecords.add(new ActivityRecord());
//                    set.segments.add(s);
//                    activities.add(set);
//                    callback.run(activities);
                }
            };

            ResultCallback resultCallback = new ResultCallback();

            ActivityResultContract<Object, Uri> contract = new ActivityResultContract<Object, Uri>() {
                @Override
                public Uri parseResult(int i, @Nullable Intent intent) {
                    return intent.getData();
//                    return intent.toUri(0);
                }

                @NonNull
                @Override
                public Intent createIntent(@NonNull Context context, Object o) {
                    return new Intent()
                            .setType("*/*")
                            .setAction(Intent.ACTION_GET_CONTENT);
                }

            };

            ActivityResultLauncher launcher = context.registerForActivityResult(
                    contract,
                    resultCallback
            );
            launcher.launch(null);
        }
        catch (Exception e){
//            System.out.println(e.stackTrac());
            e.printStackTrace();
//            throw e;
        }
    }

    public void loadFile(Context context, Uri uri, OnIngest cb){


        // Figure out extension
        ContentResolver contentResolver = context.getContentResolver();

        try {
            ParcelFileDescriptor pfd = contentResolver.openFileDescriptor(uri, "r");
            InputStream in = new ParcelFileDescriptor.AutoCloseInputStream(pfd);


            parseFile(in, cb);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void parseFile(InputStream in, OnIngest cb){
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

        try {
            SAXParser parser = saxParserFactory.newSAXParser();

            DefaultHandler h= new DefaultHandler(){
                private ActivityRecord currentActivity = null;
                private ActivitySet.SegmentSet currentSegment = null;

                private String uri = null;
                private String localName = null;
                private String qName = null;
                private Attributes attributes = null;

                private String content = "";

                @Override
                public void endElement(
                        String uri,
                        String localName,
                        String qName) throws SAXException {
                    if(this.qName != null && this.qName.equals("time")){
                        Date t = Date.from(Instant.parse(this.content.trim()));
                        if(this.currentActivity != null){
                            this.currentActivity.timestamp = t;
                        }
                        //                        this.currentActivity.timestamp = t;
                        System.out.println("Time: " + t);
                    }

                    this.uri = null;
                    this.localName = null;
                    this.qName = null;
                    this.attributes = null;

                    if(qName.equals("trkpt")){
                        currentActivity = null;
                    }
                    else if(qName.equals("trkseg")){
                        currentSegment = null;
                    }

                    this.content = "";
                }

                @Override
                public void startElement(
                        String uri,
                        String localName,
                        String qName,
                        Attributes attributes) throws SAXException {
                    super.startElement(uri, localName, qName, attributes);
                    this.uri = uri;
                    this.localName = localName;
                    this.qName = qName;
                    this.attributes = attributes;

                    if(qName.equals("trkpt")){
                        ActivityRecord newRec = new ActivityRecord();

                        String lat = attributes.getValue("lat");
                        String lon = attributes.getValue("lon");

                        if(lat != null && lon != null){
                            newRec.latitude = Double.parseDouble(lat);
                            newRec.longitude = Double.parseDouble(lon);

                        }
                    } else if(qName.equals("trkseg")){
                        currentSegment = new ActivitySet.SegmentSet(new Segment());

                    }
                    else if(localName.equals("time")){

                        System.out.printf("time: %s=%d\n" , qName, attributes.getLength());
                    }
                }

                @Override
                public void characters(char ch[], int start, int length) throws SAXException {

//                    if(this.qName != null && this.qName.equals("time") && this.currentActivity != null){
//                        System.out.println("Time: " + new String(ch));
////                        this.currentActivity.timestamp
////                                = Date.from(Instant.parse());
//                    }
//                    System.out.printf("%s: \"%s\"\n", this.qName, new String(ch));
                    this.content += new String(ch, start, length);
                }

            };
            parser.parse(in, new TopParser(null, null, null, null, cb));
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXParseException e){
            throw new RuntimeException(e);
        }
        catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
