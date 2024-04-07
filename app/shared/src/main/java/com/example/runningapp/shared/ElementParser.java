package com.example.runningapp.shared;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;
import org.xml.sax.Attributes;

import java.util.Map;
import java.util.TreeMap;

class VoidParser extends ElementParser {

    public VoidParser(String uri, String localName, String qName, Attributes attributes) {
        super(uri, localName, qName, attributes);
    }
}

abstract class ChildParserFactory {
    @NonNull abstract ElementParser _build(
            String uri,
            String localName,
            String qName,
            Attributes attributes);
    public Map<String, ChildParserFactory> childParsers = new TreeMap<>();
    public @NotNull ChildParserFactory addChildParserFactory(String key, ChildParserFactory fac){
        this.childParsers.put(key, fac);
        return fac;
    }

    public @NotNull ElementParser build(
            String uri,
            String localName,
            String qName,
            Attributes attributes){
        ElementParser result = this._build(uri, localName, qName, attributes);

        this.childParsers.forEach((String key, ChildParserFactory fac)->{
            result.childParsers.put(key, fac);
        });

        return result;
    }
}

class VoidParserFactory extends ChildParserFactory {
    @NonNull
    @Override
    public ElementParser _build(String uri, String localName, String qName, Attributes attributes) {
        VoidParser result = new VoidParser(uri, localName, qName, attributes);
        result.debugName = "{void}";
        return result;
    }
}

interface DefaultParserFactoryLambda{
    public ElementParser build(String uri, String localName, String qName, Attributes attributes);
}
class DefaultParserFactory extends ChildParserFactory{
    DefaultParserFactoryLambda l;
    public DefaultParserFactory(DefaultParserFactoryLambda l) {
        this.l = l;
    }

    @NonNull
    @Override
    ElementParser _build(String uri, String localName, String qName, Attributes attributes) {
        return this.l.build(uri, localName, qName, attributes);
    }
}

public abstract class ElementParser {
    public Map<String, ChildParserFactory> childParsers = new TreeMap<>();
    public String uri;
    public String localName;
    public String qName;
    public  Attributes attributes;

    public String content = "";

    public ElementParser(
            String uri,
            String localName,
            String qName,
            Attributes attributes
    ){
        this.uri = uri;
        this.localName = localName;
        this.qName = qName;
        this.attributes = attributes;
    }

    public @NonNull ElementParser getChildParser(
            String uri,
            String localName,
            String qName,
            Attributes attributes){

        ChildParserFactory p = this.childParsers.get(qName);
        if( p != null ){
            return p.build(uri, localName, qName, attributes);
        } else {
            return new VoidParser(uri, localName, qName, attributes);
        }
    }

    public void onEnd(){
    }

    public String debugName = "{}";
}
