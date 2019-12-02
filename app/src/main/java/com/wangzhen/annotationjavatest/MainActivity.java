package com.wangzhen.annotationjavatest;

import android.os.Bundle;
import android.widget.TextView;

import com.wangzhen.annotation_lib.BindView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @BindView(value = R.id.tv_test)
    TextView tvTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bind();
        tvTest.setText("bind success");
    }
    // todo kapt 的用法有待研究
    private void bind() {
        try {
            Class bindViewClazz = Class.forName(this.getClass().getName() + "_ViewBinding");
            Method method = bindViewClazz.getMethod("bind", this.getClass());
            method.invoke(bindViewClazz.newInstance(), this);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }
}
