package com.example.runningapp.shared;

import androidx.annotation.NonNull;

import org.xml.sax.Attributes;

public class TRKSEG_P extends ElementParser {
    interface Callback {
        void call(ActivitySet.SegmentSet s);
    }

    static ChildParserFactory getFactory(Callback cb) {
        return new ChildParserFactory() {
            @NonNull
            @Override
            public ElementParser _build(String uri, String localName, String qName, Attributes attributes) {
                return new TRKSEG_P(uri, localName, qName, attributes, cb);
            }
        };
    }

    ;

    public ActivitySet.SegmentSet segmentSet = new ActivitySet.SegmentSet(new Segment());

    public Callback cb;

    public TRKSEG_P(String uri, String localName, String qName, Attributes attributes, Callback cb) {
        super(uri, localName, qName, attributes);
        this.cb = cb;
        ChildParserFactory trkpt = this.childParsers.put("trkpt", TRKPT_P.getFactory(
                (ActivityRecord d) -> {
                    long seconds = d.timestamp.getTime() / 1000;
                    int nanoSeconds = Long.valueOf((d.timestamp.getTime() % 1000) * 1_000_000).intValue();
                    segmentSet.activityRecords.add(d);
                }
        ));
    }

    @Override
    public void onEnd() {
        super.onEnd();
        this.cb.call(this.segmentSet);
    }
}
