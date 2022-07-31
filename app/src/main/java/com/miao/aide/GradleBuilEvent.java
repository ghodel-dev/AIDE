package com.miao.aide;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.aide.ui.U;
import com.blankj.utilcode.util.ClipboardUtils;
import com.blankj.utilcode.util.KeyboardUtils;
import com.miao.aide.api.SimpleIMainActivity;
import com.miao.aide.builder.ApkMaker;
import com.miao.aide.builder.ApkMaker.BuildCallback;
import com.miao.aide.builder.GradleBuilder;
import com.miao.aide.filebrowser.FileOptions;
import com.miao.aide.util.AIDEUtils;
import com.miao.aide.util.ProjectUtils;
import com.miao.util.KeyText;
import com.miao.util.Toasty;
import id.ghodel.mod.util.LogUtils;
import java.io.File;

public class GradleBuilEvent extends SimpleIMainActivity {
    private AideMainActivity mActivity;
    private GradleBuilder mGradleBuilder;
    private Menu mOptionsMenu;

    private class Option extends KeyText {
        private final GradleBuilEvent this$0;
        final GradleBuilEvent this$0$;

        public Option(GradleBuilEvent gradleBuilEvent, GradleBuilEvent gradleBuilEvent2, String str, CharSequence charSequence) {
            super(str, charSequence);
			this.this$0$ = gradleBuilEvent;
            this.this$0 = gradleBuilEvent2;
        }
    }

    private class RunCmdOption extends KeyText {
        private String[] cmd;
        private final GradleBuilEvent this$0;
        final GradleBuilEvent this$0$;

        public RunCmdOption(GradleBuilEvent gradleBuilEvent, GradleBuilEvent gradleBuilEvent2, String str, CharSequence charSequence, String... strArr) {
            super(str, charSequence);
			this.this$0$ = gradleBuilEvent;
            this.this$0 = gradleBuilEvent2;
            this.cmd = strArr;
        }
    }

    public GradleBuilder getGradleBuilder() {
        return this.mGradleBuilder;
    }

    public void onCreate(Activity activity, Bundle bundle) {
        super.onCreate(activity, bundle);
        this.mActivity = (AideMainActivity) activity;
        this.mGradleBuilder = new GradleBuilder(activity);
    }

    public void onCreateOptionsMenu(Menu menu) {
        this.mOptionsMenu = menu;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        File currentProject = ProjectUtils.getCurrentProject();
        if (currentProject != null && ProjectUtils.isGradleProject(currentProject)) {
            currentProject.getAbsolutePath();
            if (itemId == 2131230989 || itemId == 2131230991) {
                KeyboardUtils.hideSoftInput(mActivity);
                ApkMaker apkMaker = new ApkMaker(mActivity);
                apkMaker.setProjectDir(AIDEUtils.getCurrentAppHome());
                apkMaker.setBuildListener(new BuildCallback() {
                    ProgressDialog pd;
                    
                    public void aaptERR(String str) {
                    }

                    public void aaptOK(String str) {
                    }

                    public void javaERR(String str) {
                    }

                    public void javaOK(String str) {
                    }

                    public void onFailure(String str) {
                        this.pd.dismiss();
                        new Builder(mActivity)
								.setTitle("Build Failed!")
								.setMessage(LogUtils.colorErrsAndWarnings(str))
								.setPositiveButton(android.R.string.copy, new DialogInterface.OnClickListener(){
									@Override
									public void onClick(DialogInterface dialog, int arg1) {
										
										ClipboardUtils.copyText(str);
										Toasty.success("Error was successfully copied to Clipboard").show();
									}
									
								})
								.setNegativeButton(android.R.string.ok, null)
								.create()
								.show();
                    }

                    public void onProgress(String str) {
                        this.pd.setMessage(str);
                    }

                    public void onStart() {
                        
                        this.pd = new ProgressDialog(mActivity);
                        //this.pd.setCancelable(true);
                        this.pd.setTitle("Building app");
                        this.pd.setMessage("Please wait...");
                        this.pd.setProgressStyle(0);
                        this.pd.show();
                    }

                    public void onSuccess(File file) {
                        this.pd.dismiss
						();
                        FileOptions.installApk(mActivity, file);
                    }
                });
                apkMaker.build();
                return true;
            }
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public void saveFile() {
        U.er().j6(false, false);
    }
}
