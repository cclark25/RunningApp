package com.example.runningapp.shared;

import androidx.annotation.NonNull;

import org.xml.sax.Attributes;

import java.util.Date;

public class TRKPT_P extends ElementParser {
    interface Callback {
        void call(ActivityRecord r);
    }

    static ChildParserFactory getFactory(Callback onEachActivityRecord) {
        return new ChildParserFactory() {
            @NonNull
            @Override
            ElementParser _build(String uri, String localName, String qName, Attributes attributes) {
                return new TRKPT_P(uri, localName, qName, attributes, onEachActivityRecord);
            };
        };
    };
    public ActivityRecord record = new ActivityRecord();
    Callback cb;

    public TRKPT_P(String uri, String localName, String qName, Attributes attributes, Callback cb) {
        super(uri, localName, qName, attributes);
        this.cb = cb;

        this.record.longitude = Double.parseDouble(attributes.getValue("lon"));
        this.record.latitude = Double.parseDouble(attributes.getValue("lat"));

        ChildParserFactory ele = new DefaultParserFactory((String _uri, String _localName, String _qName, Attributes _attributes) -> {
            return new Double_P(_uri, _localName, _qName, _attributes,
                    (double d) -> {
                        // TODO: find conversion between WGS84 Altitude and MSL elevation
                        this.record.altitude = d;
                    }
            );
        });
        this.childParsers.put("ele", ele);
        ChildParserFactory time = new DefaultParserFactory((String _uri, String _localName, String _qName, Attributes _attributes) -> {
            return new Timestamp_P(_uri, _localName, _qName, _attributes,
                    (Date d) -> {
                        this.record.timestamp = d;
                    }
            );
        });
        this.childParsers.put("time", time);
        ChildParserFactory extensions = new VoidParserFactory();
        this.childParsers.put("extensions", extensions);

        ChildParserFactory trackPointExtension = extensions.addChildParserFactory("gpxtpx:TrackPointExtension", new VoidParserFactory());

        ChildParserFactory hr = new DefaultParserFactory((String _uri, String _localName, String _qName, Attributes _attributes) -> {
            return new Double_P(_uri, _localName, _qName, _attributes,
                    (double d) -> {
                        this.record.heartRate = d;
                    }
            );
        });
        trackPointExtension.addChildParserFactory("gpxtpx:hr", hr);
        trackPointExtension.addChildParserFactory("gpxtpx:cad", new VoidParserFactory());

    }

    @Override
    public void onEnd() {
        super.onEnd();
        this.cb.call(this.record);
    }

}
