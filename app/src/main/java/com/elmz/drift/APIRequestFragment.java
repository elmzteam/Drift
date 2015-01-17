package com.elmz.drift;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;

public class APIRequestFragment extends AsyncTask<String, Void, String>{
	public static final String base = "http://104.131.87.106/";

	private ICallback callback;

	public static String LOG_TAG = "ELMZ-DRFT";

	public APIRequestFragment(ICallback cb){
		callback = cb;
	}

	@Override
	protected String doInBackground(String... params){
		Log.d(LOG_TAG, "Received request: " + params[0] + " " + params[1]);

		HttpRequestBase request = null;
		String method = params[0];
		String url = base + params[1];

		if(method.equals("GET")){
			request = new HttpGet(url);
		} else if(method.equals("POST")){
			request = new HttpPost(url);
			request.setHeader("Content-Type", "application/x-www-form-urlencoded");

			StringBuilder formDataBuilder = new StringBuilder();

			try{
				for(int i = 1; i < params.length - 1; i++){
					formDataBuilder.append(Integer.toString(i));
					formDataBuilder.append("=");
					formDataBuilder.append(URLEncoder.encode(params[i + 1], "utf-8"));
					if(i < params.length - 2){
						formDataBuilder.append("&");
					}
				}
				((HttpPost) (request)).setEntity(new StringEntity(formDataBuilder.toString()));
			} catch(UnsupportedEncodingException e){
				Log.e(LOG_TAG, "Unsupported encoding :(");
			}
		}

		return getRequest(request);
	}

	public String getRequest(HttpRequestBase req){
		String response = "";
		DefaultHttpClient client = new DefaultHttpClient();
		try{
			HttpResponse execute = client.execute(req);
			InputStream content = execute.getEntity().getContent();

			BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
			String s = "";
			while((s = buffer.readLine()) != null){
				response += s;
			}

		} catch(Exception e){
			e.printStackTrace();
		}
		return response;
	}

	@Override
	protected void onPostExecute(String result){
		JsonElement json = new JsonParser().parse(result);
		callback.callback(json);
	}
}