package com.miao.aide.builder.task;

import android.content.Context;
import com.android.dx.command.dexer.DxContext;
import com.android.dx.merge.CollisionPolicy;
import com.android.dex.Dex;
import com.android.dx.merge.DexMerger;
import com.android.dx.command.dexer.Main;
import com.miao.aide.builder.model.Project;
import com.miao.aide.builder.model.Task;
import com.miao.aide.builder.util.Utils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Filter;
import java.util.regex.Pattern;
import org.codehaus.groovy.reflection.stdclasses.IntegerCachedClass;
import android.util.Log;

public class DXTask extends Task {

	private String task = "Dx";

	public DXTask(Context context, Project project) {
		super(context, project);
	}

	@Override
	public boolean doFullTaskAction() throws Exception {

		if (!dexLibs()) {

			return false;
		}
		if (!dexBuildClasses()) {

			return false;
		}
		if (!dexMerge()) {

			return false;
		}

		return true;
	}

	private boolean dexLibs() throws Exception {

		ArrayList<File> javaLibraries = mProject.getLibraries();

		File dexesLib = new File(mProject.getProjectPath(), "build/bin/dexes");
		if (!dexesLib.exists()) {
			dexesLib.mkdirs();
		}

		for (int i = 0; i < javaLibraries.size(); i++) {

			//String MD5 = Utils.calculateMD5(javaLibraries.get(i));
			String fileName = javaLibraries.get(i).getName();

			File output = new File(dexesLib, fileName + ".dex");

			if (!output.exists()) {

				List<String> args = new ArrayList<>();
				args.add("--verbose");
				args.add("--num-threads");
				args.add(String.valueOf(getNumCores()));

				//args.add("--no-strict");
				//args.add("--no-files");
				//args.add("--min-sdk-version");
				//args.add(mProject.getMinSdk());
				args.add("--output");
				args.add(output.getAbsolutePath());
				args.add(javaLibraries.get(i).getAbsolutePath());

				Log.d("DXTask", "Runinng Dx with arguments : " + args);
				Log.d("DXTask", "Dexing lib : " + javaLibraries.get(i).getPath() + " => " + output.getAbsolutePath());

				Main.clearInternTables();
				Main.Arguments arguments = new Main.Arguments();
				Method parseMethod = Main.Arguments.class.getDeclaredMethod("parse", String[].class);
				parseMethod.setAccessible(true);
				parseMethod.invoke(arguments, (Object) args.toArray(new String[0]));
				int resultCode = Main.run(arguments);

				if (resultCode != 0) {
					return false;
				}
			}

		}
		task = "Dexed libraries";
		return true;
	}

	private boolean dexBuildClasses() throws Exception {

		File buildDir = new File(mProject.getProjectPath(), "build");
		File inputDirClasses = new File(buildDir, "bin/classes");

		File outputDexClassesDir = new File(buildDir, "bin/classes.dex");

		List<String> args = new ArrayList<>();
		args.add("--verbose");
		args.add("--num-threads");
		args.add(String.valueOf(getNumCores()));

		//args.add("--min-sdk-version");
		//args.add(mProject.getMinSdk());
		args.add("--output");
		args.add(outputDexClassesDir.getAbsolutePath());
		args.add(inputDirClasses.getAbsolutePath());

		Log.d("DXTask", "Runinng Dx with arguments : " + args);
		Log.d("DXTask",
				"Dexing classes : " + inputDirClasses.getPath() + " => " + outputDexClassesDir.getAbsolutePath());

		//int result = Main.main(args.toArray(new String[0]));

		Main.clearInternTables();
		Main.Arguments arguments = new Main.Arguments();
		Method parseMethod = Main.Arguments.class.getDeclaredMethod("parse", String[].class);
		parseMethod.setAccessible(true);
		parseMethod.invoke(arguments, (Object) args.toArray(new String[0]));
		int resultCode = Main.run(arguments);

		if (resultCode != 0) {
			return false;
		}

		task = "Dexed classes";

		return true;

	}

	private boolean dexMerge() throws Exception {

		//List<String> args = new ArrayList<>();
		List<Dex> args = new ArrayList<>();

		List<File> dexedLibs = getAllDexFiles(new File(mProject.getProjectPath(), "build/bin/dexes"));
		File outputClassesDex = new File(mProject.getProjectPath(), "build/bin/classes.dex");
		//File appDexFile = new File(mProject.getProjectPath(), "build/bin/app-classes.dex");

		args.add(new Dex(outputClassesDex));

		if (dexedLibs.size() >= 1) {

			for (File dexedLib : dexedLibs) {
				args.add(new Dex(dexedLib));

				DexMerger dexMerger = new DexMerger(args.toArray(new Dex[0]), CollisionPolicy.KEEP_FIRST,
						new DxContext());
				Dex merged = dexMerger.merge();

				if (merged != null) {
					merged.writeTo(outputClassesDex);
					return true;
				}

				/*Dex[] toBeMerge = {new Dex(dexFile), new Dex(dexedLib)};
				DexMerger dexMerger = new DexMerger(toBeMerge, CollisionPolicy.FAIL, new DxContext());
				Dex merged = dexMerger.merge();
				
				if (merged != null) {
					merged.writeTo(dexFile);
					Log.d("DXTask", "Merge Dex : " + dexedLib.getPath() + " => " + dexFile.getAbsolutePath());
				} else {
					Log.d("DXTask", "Failed merge dex...");
				}*/

				//args.add(dexedLib.getAbsolutePath());
			}
		}
		//DexMerger.main(args.toArray(new String[]{}));

		task = "Merger dex";
		return true;

	}

	private List<File> getAllDexFiles(File file) {
		List<File> list = new ArrayList<>();
		File[] listFiles = file.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isFile() && pathname.getName().endsWith(".dex");
			}
		});

		if (listFiles != null) {
			for (File f : listFiles) {
				if (f.isDirectory()) {
					list.addAll(getAllDexFiles(f));
				} else {
					list.add(f);
				}

			}
		}

		return list;
	}

	private int getNumCores() {
		class CpuFilter implements FileFilter {
			@Override
			public boolean accept(File pathName) {

				if (Pattern.matches("cpu[0-9]+", pathName.getName())) {
					return true;
				}

				return false;
			}
		}

		try {
			File file = new File("/sys/devices/systems/cpu/");
			File[] files = file.listFiles(new CpuFilter());
			return files.length;
		} catch (Exception e) {
			return Runtime.getRuntime().availableProcessors();
		}

	}

	@Override
	public String getTaskName() {
		return task;
	}

}