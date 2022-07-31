package com.miao.aide.builder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import com.miao.aide.builder.model.Project;
import com.miao.aide.builder.model.Task;
import com.miao.aide.builder.task.AabTask;
import com.miao.aide.builder.task.Aapt2Task;
import com.miao.aide.builder.task.AidlTask;
import com.miao.aide.builder.task.BuildTask;
import com.miao.aide.builder.task.CheckLibrariesTask;
import com.miao.aide.builder.task.D8Task;
import com.miao.aide.builder.task.DXTask;
import com.miao.aide.builder.task.GenerateFirebaseTask;
import com.miao.aide.builder.task.JavaTask;
import com.miao.aide.builder.task.PackageTask;
import com.miao.aide.builder.task.SignTask;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class ApkMaker extends AsyncTask<String, String, String> {
	private Context c;
	private BuildCallback callback;
	private boolean isRunning = false;
	private File project;
	SharedPreferences sp;
	private ArrayList<Task> tasks;

	public interface BuildCallback {
		void aaptERR(String str);

		void aaptOK(String str);

		void javaERR(String str);

		void javaOK(String str);

		void onFailure(String str);

		void onProgress(String str);

		void onStart();

		void onSuccess(File file);
	}

	public ApkMaker(Context context) {
		this.c = context;
		this.sp = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public void build() {
		execute(new String[0]);
	}

	protected String doInBackground(String[] strArr) {
		this.isRunning = true;
		Project project = new Project();
		project.setProjectPath(this.project.getAbsolutePath());
		ArrayList arrayList = new ArrayList();
		arrayList.add(new BuildTask(this.c, project));
		arrayList.add(new CheckLibrariesTask(this.c, project));
		arrayList.add(new GenerateFirebaseTask(this.c, project));
		arrayList.add(new Aapt2Task(this.c, project));
		arrayList.add(new AidlTask(this.c, project));
		arrayList.add(new JavaTask(this.c, project));
		if (this.sp.getBoolean("adset_use_d8", false)) {
			arrayList.add(new D8Task(this.c, project));
		} else {
			arrayList.add(new DXTask(this.c, project));
		}

		arrayList.add(new PackageTask(this.c, project));
		arrayList.add(new SignTask(this.c, project));
		if (this.sp.getBoolean("adset_use_apktoaab", false)) {
			arrayList.add(new AabTask(this.c, project));
		}
		try {
			runTasks(arrayList);
			return "";
		} catch (NullPointerException npe) {
			return npe.getMessage();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	public boolean isRunning() {
		return this.isRunning;
	}

	protected void onCancelled(String str) {
		this.isRunning = false;
		if (str != null) {
			this.callback.onFailure(str);
		}
		super.onCancelled(str);
	}

	protected void onPostExecute(String str) {
		super.onPostExecute(str);
		this.isRunning = false;

		if (str != null) {
			if (str.isEmpty()) {
				this.callback.onSuccess(new File(this.project, "build/bin/signed.apk"));
			} else {
				this.callback.onFailure(str);
			}
		} else {
			this.callback.onFailure("Null Pointer Exception");
		}

	}
	
	protected void onPreExecute() {
		this.callback.onStart();
		this.isRunning = true;
		super.onPreExecute();
	}

	protected void onProgressUpdate(String[] strArr) {
		this.callback.onProgress(strArr[0]);
		super.onProgressUpdate(strArr);
	}

	protected boolean runTasks(ArrayList<Task> arrayList) throws Exception {
		Iterator it = arrayList.iterator();
		while (it.hasNext()) {
			Task task = (Task) it.next();
			PrintStream printStream = System.out;
			StringBuilder stringBuilder = new StringBuilder();
			String str = "Running ";
			stringBuilder.append(str);
			stringBuilder.append(task.getTaskName());
			printStream.println(stringBuilder.toString());
			StringBuilder stringBuilder2 = new StringBuilder();
			stringBuilder2.append(str);
			stringBuilder2.append(task.getTaskName());
			publishProgress(new String[] { stringBuilder2.toString() });
			if (!task.doFullTaskAction()) {
				StringBuilder stringBuilder3 = new StringBuilder();
				stringBuilder3.append(task.getTaskName());
				stringBuilder3.append(" failed");
				throw new Exception(stringBuilder3.toString());
			}
		}
		return true;
	}

	public void setBuildListener(BuildCallback buildCallback) {
		this.callback = buildCallback;
	}

	public void setProjectDir(String str) {
		this.project = new File(str);
	}

	public void setTasks(ArrayList<Task> arrayList) {
		this.tasks = arrayList;
	}
}
