package com.android.testabstract;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((Button)findViewById(R.id.btnTest)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TestAb<TestModel> testAb = new TestAb<TestModel>() {
                    @Override
                    protected void onSuccess(TestModel paramT) {
                        Toast.makeText(getBaseContext(), paramT.msg, Toast.LENGTH_LONG).show();
                    }
                };

                Test(testAb);
            }
        });
    }
    public void Test(TestAb testAb){
        TestModel model = new TestModel();
        model.msg = "success";
        testAb.onExecuted(model, null);
    }
}
/*


    public void Test2(ITest test){
        TestModel model = new TestModel();
        model.msg = "success";
        test.onExecuted(model);
    }



        ((Button)findViewById(R.id.btnTest2)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ITest testInter = new ITest() {
                    @Override
                    public void onExecuted(TestModel paramT) {
                        Toast.makeText(getBaseContext(), paramT.msg, Toast.LENGTH_LONG).show();
                    }
                };

                Test2(testInter);
            }
        });

 */