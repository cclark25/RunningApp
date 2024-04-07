package com.example.runningapp.shared;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Vector;

class TestParser extends ElementParser {
    public TestParser(String uri, String localName, String qName, Attributes attributes, OnIngest cb) {
        super(uri, localName, qName, attributes);
        this.childParsers.put("gpx", GPX_P.getFactory((ActivitySet s)->{
            Vector<ActivitySet> v = new Vector<ActivitySet>();
            v.add(s);
            cb.run(v);
            int activityCount = 0;
            for(ActivitySet.SegmentSet seg : s.segments){
                activityCount += seg.activityRecords.size();
            }

        }));
    }
}

public class TopParser extends DefaultHandler {
    private Deque<ElementParser> elementStack = new ArrayDeque<>();

    public TopParser(String uri, String localName, String qName, Attributes attributes, OnIngest cb){
        this.elementStack.push(new TestParser(uri, localName, qName, attributes, cb));
    }

    @Override
    public void startElement(
            String uri,
            String localName,
            String qName,
            Attributes attributes) throws SAXException {

        ElementParser p = this.elementStack.peek();
        if(p != null) {
            ElementParser newTop = p.getChildParser(uri, localName, qName, attributes);
            this.elementStack.push(
                newTop
            );
        }
        else {
            this.elementStack.push(new VoidParser(uri, localName, qName, attributes));
        }

//        String s = "";
//        for(ElementParser e : this.elementStack){
//            if(e == null){
//                s = "{null}->" + s;
//                continue;
//            }
//            s = e.debugName + "->" + s;
//        }
//        System.out.println("Key: " + s);
    }

    @Override
    public void endElement(
            String uri,
            String localName,
            String qName) throws SAXException {

        this.elementStack.pop().onEnd();
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {

//                    if(this.qName != null && this.qName.equals("time") && this.currentActivity != null){
//                        System.out.println("Time: " + new String(ch));
////                        this.currentActivity.timestamp
////                                = Date.from(Instant.parse());
//                    }
//                    System.out.printf("%s: \"%s\"\n", this.qName, new String(ch));
        ElementParser p = this.elementStack.peek();
        if(p != null){
            p.content += new String(ch, start, length);
        }
    }
}
