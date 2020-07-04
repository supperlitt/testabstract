package com.android.testxpabs;


import android.util.Log;

import com.android.testabstract.TestAb;
import com.android.testabstract.TestModel;

public class TestTestAb extends TestAb<TestModel> {

    public TestTestAb(){
    }

    @Override
    protected void onSuccess(TestModel paramT) {
        Log.i("tt===tt", "hooked msg " + paramT.msg);
    }
}