package com.lau.technewsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    ArrayList<String> newsHeading = new ArrayList<>();
    ArrayList<String> news = new ArrayList<>();
    ArrayAdapter arrayAdapter;

    SQLiteDatabase newsDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        newsDB = this.openOrCreateDatabase("Articles", MODE_PRIVATE, null);
        newsDB.execSQL("CREATE TABLE IF NOT EXISTS articles (id INTEGER PRIMARY KEY, articleId, INTEGER, title VARCHAR, content VARCHAR)");

        DownloadTask task = new DownloadTask();

        try {
            task.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        ListView list = findViewById(R.id.list);

        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, newsHeading);

        list.setAdapter(arrayAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adView, View view, int i, long l) {

                Intent webPage = new Intent(getApplicationContext(), Activity2.class);
                //load the other intent containing the web view

                webPage.putExtra("content", news.get(i));
                startActivity(webPage);
            }
        });

        DisplayNews();
    }


    public void DisplayNews() {

        Cursor cursor = newsDB.rawQuery("SELECT * FROM articles", null);

        int content = cursor.getColumnIndex("content");
        int title = cursor.getColumnIndex("title");

        if (cursor.moveToFirst()) {
            newsHeading.clear();
            news.clear();

            do {
                newsHeading.add(cursor.getString(title));
                news.add(cursor.getString(content));

            } while (cursor.moveToNext());
            arrayAdapter.notifyDataSetChanged();
        }
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {

                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                int data = inputStreamReader.read();

                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = inputStreamReader.read();
                }

                JSONArray jsonArray = new JSONArray(result);

                int items = 20;

                if (jsonArray.length() < 20) {
                    items = jsonArray.length();
                }

                newsDB.execSQL("DELETE FROM articles");

                for (int i=0;i < items; i++) {

                    String articleID = jsonArray.getString(i);

                    url = new URL("https://hacker-news.firebaseio.com/v0/item/" + articleID + ".json?print=pretty");
                    urlConnection = (HttpURLConnection) url.openConnection();

                    inputStream = urlConnection.getInputStream();
                    inputStreamReader = new InputStreamReader(inputStream);

                    data = inputStreamReader.read();

                    String info = "";

                    while (data != -1) {
                        char current = (char) data;
                        info += current;
                        data = inputStreamReader.read();
                    }
                    JSONObject jsonObject = new JSONObject(info);

                    if (!jsonObject.isNull("title") && !jsonObject.isNull("url")) {

                        String articleTitle = jsonObject.getString("title");
                        String newsUrl = jsonObject.getString("url");

                        url = new URL(newsUrl);
                        urlConnection = (HttpURLConnection) url.openConnection();

                        inputStream = urlConnection.getInputStream();
                        inputStreamReader = new InputStreamReader(inputStream);

                        data = inputStreamReader.read();

                        String newsDetails = "";
                        while (data != -1) {

                            char current = (char) data;
                            newsDetails += current;
                            data = inputStreamReader.read();
                        }
                        String sql = "INSERT INTO articles (articleID, title, content) VALUES (?, ?, ?)";
                        SQLiteStatement statement = newsDB.compileStatement(sql);

                        statement.bindString(1,articleID);
                        statement.bindString(2,articleTitle);
                        statement.bindString(3,newsDetails);

                        statement.execute();
                    }
                }
                return result;

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            DisplayNews();
        }
    }
}