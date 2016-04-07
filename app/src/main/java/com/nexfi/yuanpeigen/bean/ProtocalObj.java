package com.nexfi.yuanpeigen.bean;

import com.thoughtworks.xstream.XStream;

public class ProtocalObj {
    public String toXml() {
        XStream x = new XStream();
        x.alias(this.getClass().getSimpleName(), this.getClass());
        return x.toXML(this);
    }

    public Object fromXml(String xml) {
        XStream x = new XStream();
        x.alias(this.getClass().getSimpleName(), this.getClass());
        Object obj=x.fromXML(xml);
        return obj;
    }
}
