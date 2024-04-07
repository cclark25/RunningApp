package com.example.runningapp.shared;

import org.xml.sax.Attributes;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Timestamp_P extends ElementParser {
    interface Callback {
        void call(Date d);
    }

    Callback cb;

    public Timestamp_P(
            String uri, String localName, String qName, Attributes attributes,
            Callback cb
    ) {
        super(uri, localName, qName, attributes);
        this.cb = cb;
    }


    @Override
    public void onEnd() {

        this.cb.call(
                Date.from(Instant.from
                        (DateTimeFormatter.ISO_DATE_TIME.parse(
                                this.content.trim()
                        ))
                )
        );
    }
}
