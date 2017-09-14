package com.eypcnn.reboot;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

import org.markdownj.MarkdownProcessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;


/**
 * EypCnn
 */
public class WebViewActivity extends Activity {
	public static final String TAG = "WebViewActivity";

	public static final String FILE_RES_ID = "FILE_RES_ID";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView( R.layout.webview);
    }

	private String readText(int fileResId) {
		String result = "";
		try {
			String str;
			BufferedReader in = new BufferedReader(
					new InputStreamReader(getResources().openRawResource(fileResId), "UTF-8"));
			while ((str = in.readLine()) != null) {
				result += str + "\n";
			}
			in.close();
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Error when reading text file", e);
		} catch (IOException e) {
			Log.e(TAG, "Error when reading text file", e);
		} catch (Exception e) {
			Log.e(TAG, "Error when reading text file", e);
		}
		return result;
	}
}
