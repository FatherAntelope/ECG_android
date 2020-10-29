package com.example.ecgdemo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;


class Point{
    int x, y1, y2;
    Point(int x, int y1, int y2) {
        this.x = x;
        this.y1 = y1;
        this.y2 = y2;
    }

}

public class MainActivity extends AppCompatActivity {
    List<Point> points;
    Point point;
    Intent fileIntent;
    String name1, name2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        Button buttonOpenFile = (Button) findViewById(R.id.openFile);



        points = new ArrayList<>();
        buttonOpenFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                createCharts();
            }
        });


    }



    //рисует графики
    public void createCharts() {
        getData();

        //задаем параметры полотна
        GraphView graphView = (GraphView) findViewById(R.id.graph);
        graphView.getViewport().setScalable(true);  // activate horizontal zooming and scrolling
        graphView.getViewport().setScrollable(true);  // activate horizontal scrolling
        graphView.getLegendRenderer().setVisible(true);
        graphView.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graphView.getViewport().setMinX(4);
        graphView.getViewport().setMaxX(200);

        //достаем данные из points
        DataPoint[] dataPoints1 = new DataPoint[points.size()];
        DataPoint[] dataPoints2 = new DataPoint[points.size()];
        for (int i = 0; i < points.size(); i++) {
            dataPoints1[i] = new DataPoint(points.get(i).x, points.get(i).y1);
            dataPoints2[i] = new DataPoint(points.get(i).x, points.get(i).y2);
        }

        LineGraphSeries<DataPoint> series1 = new LineGraphSeries<DataPoint>(dataPoints1);
        LineGraphSeries<DataPoint> series2 = new LineGraphSeries<DataPoint>(dataPoints2);

        //задаем параметры для графиков
        series1.setColor(Color.RED);
        series2.setColor(Color.CYAN);
        series1.setTitle(name1);
        series2.setTitle(name2);

        //отображаем графики
        graphView.addSeries(series1);
        graphView.addSeries(series2);

    }

    //не используется
    public boolean isStoragePermissionReadGranted() {
        String TAG = "Storage Permission";
        if (Build.VERSION.SDK_INT >= 23) {
            if (this.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    //получение данных из csv файла
    public void getData() {

            try {
                AssetManager assetManager = this.getAssets();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(assetManager.open("104.csv")));
                String line;
                int col1, col2;
                int step = 0;
                int x, y1, y2;

                line = bufferedReader.readLine();
                col1 = line.indexOf(",");
                col2 = line.indexOf(",", col1 + 1);
                name1 = line.substring(col1 + 1, col2);
                col1 = col2 + 1;
                name2 = line.substring(col1);

                while ((line = bufferedReader.readLine()) != null) {
                    if(step == 2000)
                        break;
                    col1 = line.indexOf(",");
                    x = step;
                    col2 = line.indexOf(",", col1 + 1);
                    y1 = Integer.valueOf(line.substring(col1 + 1, col2));
                    col1 = col2 + 1;
                    y2 = Integer.valueOf(line.substring(col1));
                    points.add(new Point(x, y1, y2));
                    step++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
    }


    //не используется
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case 100:
                if(resultCode == RESULT_OK){
                    try {

                        Uri uri= data.getData();
                        File file= new File(uri.getPath());



                        point = new Point(0, 0, 0);
                        String str = file.getName();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.getAssets().open("103.csv")));
                        String line;
                        int col1, col2;
                        line = bufferedReader.readLine();
                        int step = 0;
                        while ((line = bufferedReader.readLine()) != null) {
                            if(step == 2000)
                                break;
                            col1 = line.indexOf(",");
                            point.x = Integer.valueOf(line.substring(0, col1));
                            System.out.println(line.substring(0,col1));
                            col2 = line.indexOf(",", col1 + 1);
                            point.y1 = Integer.valueOf(line.substring(col1 + 1, col2));
                            System.out.println(line.substring(col1+1,col2));
                            col1 = col2 + 1;
                            point.y2 = Integer.valueOf(line.substring(col1));
                            System.out.println(line.substring(col1));
                            points.add(point);
                            step++;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //Флаги, чтобы убрать все лишнее с экрана
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
}