package com.example.multi.view;

import android.content.Context;
import android.util.AttributeSet;

public interface IComponentView {

    void init(Context context);

    void parseXmlAttributes(Context context, AttributeSet attributeSet);

    int getDefaultInputType();
}
