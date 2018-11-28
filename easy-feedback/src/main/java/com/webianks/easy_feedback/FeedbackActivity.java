package com.webianks.easy_feedback;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.webianks.easy_feedback.components.DeviceInfo;
import com.webianks.easy_feedback.components.SystemLog;
import com.webianks.easy_feedback.components.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by R Ankit on 28-10-2016.
 */

public class FeedbackActivity extends AppCompatActivity implements View.OnClickListener {

	public static final String KEY_WITH_INFO = "with_info";
	public static final String KEY_EMAIL = "email";
	public String LOG_TO_STRING = SystemLog.extractLogToString();
	private EditText editText;
	private String emailId;
	private String deviceInfo;
	private boolean withInfo;
	private LinearLayout selectContainer;


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.feedback_layout);

		if (getSupportActionBar() != null)
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		init();
	}

	private void init() {

		editText = (EditText) findViewById(R.id.editText);

		TextView info = (TextView) findViewById(R.id.info_legal);
		Button submitSuggestion = (Button) findViewById(R.id.submitSuggestion);

		submitSuggestion.setOnClickListener(this);

		emailId = getIntent().getStringExtra(KEY_EMAIL);
		withInfo = getIntent().getBooleanExtra(KEY_WITH_INFO, false);

		deviceInfo = DeviceInfo.getAllDeviceInfo(this, false);

		if (withInfo) {

			CharSequence infoFeedbackStart = getResources().getString(R.string.info_fedback_legal_start);
			SpannableString deviceInfo = new SpannableString(getResources().getString(R.string.info_fedback_legal_system_info));
			deviceInfo.setSpan(new ClickableSpan() {
				@Override
				public void onClick(View widget) {
					new AlertDialog.Builder(FeedbackActivity.this)
							.setTitle(R.string.info_fedback_legal_system_info)
							.setMessage(FeedbackActivity.this.deviceInfo)
							.setPositiveButton(R.string.Ok, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							})
							.show();
				}
			}, 0, deviceInfo.length(), 0);
			CharSequence infoFeedbackAnd = getResources().getString(R.string.info_fedback_legal_and);
			SpannableString systemLog = new SpannableString(getResources().getString(R.string.info_fedback_legal_log_data));
			systemLog.setSpan(new ClickableSpan() {
				@Override
				public void onClick(View widget) {
					new AlertDialog.Builder(FeedbackActivity.this)
							.setTitle(R.string.info_fedback_legal_log_data)
							.setMessage(FeedbackActivity.this.LOG_TO_STRING)
							.setPositiveButton(R.string.Ok, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							})
							.show();
				}
			}, 0, systemLog.length(), 0);
			CharSequence infoFeedbackEnd = getResources().getString(R.string.info_fedback_legal_will_be_sent, getAppLabel());
			Spanned finalLegal = (Spanned) TextUtils.concat(infoFeedbackStart, deviceInfo, infoFeedbackAnd, systemLog, infoFeedbackEnd);


			info.setText(finalLegal);
			info.setMovementMethod(LinkMovementMethod.getInstance());

		} else
			info.setVisibility(View.GONE);

	}

	public void sendEmail(String body) {

		Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
		emailIntent.setType("text/plain");
		emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailId});
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_mail_subject, getAppLabel()));
		emailIntent.putExtra(Intent.EXTRA_TEXT, body);

		ArrayList<Uri> uris = new ArrayList<>();


		if (withInfo) {
			Uri deviceInfoUri = createFileFromString(deviceInfo, getString(R.string.file_name_device_info));
			uris.add(deviceInfoUri);

			Uri logUri = createFileFromString(LOG_TO_STRING, getString(R.string.file_name_device_log));
			uris.add(logUri);
		}

		emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
		startActivity(Utils.createEmailOnlyChooserIntent(this, emailIntent, getString(R.string.send_feedback_two)));
	}

	private Uri createFileFromString(String text, String name) {
		File file = new File(getExternalCacheDir(), name);
		//create the file if it didn't exist
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			//BufferedWriter for performance, false to overrite to file flag
			BufferedWriter buf = new BufferedWriter(new FileWriter(file, false));
			buf.write(text);
			buf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".fileprovider", file);
	}


	@Override
	public void onClick(View view) {

		if (view.getId() == R.id.submitSuggestion) {

			String suggestion = editText.getText().toString();
			if (suggestion.trim().length() > 0) {
				sendEmail(suggestion);
				finish();
			} else
				editText.setError(getString(R.string.please_write));
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == android.R.id.home)
			finish();

		return super.onOptionsItemSelected(item);
	}

	public String getAppLabel() {
		return getResources().getString(R.string.app_name);
	}


}
