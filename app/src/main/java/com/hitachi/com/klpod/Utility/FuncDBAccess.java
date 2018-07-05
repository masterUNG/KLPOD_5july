package com.hitachi.com.klpod.Utility;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class FuncDBAccess extends AsyncTask<String,Void,String>{

    private Context context;

    public FuncDBAccess(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(String... strings) {
        try {

            OkHttpClient okHttpClient = new OkHttpClient();
            Request.Builder builder = new Request.Builder();

            Request request = builder
                    .url(strings[0])
                    .build();


            Response response = okHttpClient.newCall(request).execute();
            Log.d("KLTag","response ==> " + response);
            return response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public String SetJSONResult(String resultJSON)
    {
        resultJSON = resultJSON.replace("\\\"","\"");
        return resultJSON.substring(1,resultJSON.length()-1);

    }
}
