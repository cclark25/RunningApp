package com.example.runningapp.shared;

import androidx.annotation.NonNull;

import org.xml.sax.Attributes;

public class GPX_P extends ElementParser {
    static String key = "gpx";
    interface Callback {
        void call(ActivitySet s);
    };
    static ChildParserFactory getFactory(Callback cb) {
        return new ChildParserFactory() {
            @NonNull
            @Override
            public ElementParser _build(String uri, String localName, String qName, Attributes attributes) {
                return new GPX_P(uri, localName, qName, attributes, cb);
            }
        };
    };

    public ActivitySet activity = new ActivitySet(new Activity());
    public Callback cb;

    public GPX_P(String uri, String localName, String qName, Attributes attributes, Callback cb) {
        super(uri, localName, qName, attributes);
        this.cb = cb;
        ChildParserFactory trk = new VoidParserFactory();

        trk.addChildParserFactory(
                "trkseg",
                TRKSEG_P.getFactory((ActivitySet.SegmentSet s)->{
                    activity.segments.add(s);
                })
        );


        this.childParsers.put("trk", trk);
    }

    @Override
    public void onEnd() {
        super.onEnd();
        this.cb.call(this.activity);
    }
}

