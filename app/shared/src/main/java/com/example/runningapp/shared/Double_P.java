package com.example.runningapp.shared;

import org.xml.sax.Attributes;

public class Double_P extends ElementParser {
    interface Callback {
        void call(double d);
    }

    Callback cb;

    public Double_P(
            String uri, String localName, String qName, Attributes attributes,
            Callback cb
    ) {
        super(uri, localName, qName, attributes);
        this.cb = cb;
    }


    @Override
    public void onEnd() {
        double d = Double.parseDouble(this.content);
        this.cb.call(d);
    }
}
