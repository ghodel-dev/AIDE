package com.miao.aide;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.android.apksig.ApkSigner.Builder;
import com.android.apksig.ApkSigner.SignerConfig;
import com.blankj.utilcode.util.CloseUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ReflectUtils;
import com.blankj.utilcode.util.ThrowableUtils;
import com.google.common.collect.ImmutableList;
import com.miao.aide.api.ApiManager;
import com.miao.aide.application.App;
import com.miao.aide.builder.ArscObfuscator;
import com.miao.aide.filebrowser.FileOptions;
import com.miao.aide.util.ZipOut;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipFile;
import ru.maximoff.zipalign.ZipAligner;
import sun1.security.pkcs.PKCS8Key;

public class ApkBuilderManager {
	
	
    private static File getZipAlignFile() throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(App.getApp().getApplicationInfo().nativeLibraryDir);
        stringBuilder.append("/libzipalign.so");
        File file = new File(stringBuilder.toString());
        if (file.exists()) {
            return file;
        }
        throw new Exception("ZipAlign binary not found");
    }

    public static void proxyBuildApking(Object obj, List<String> list) {
        try {
            LogUtils.i(new Object[]{"### BuildApking Start ###"});
            ReflectUtils reflect = ReflectUtils.reflect(obj);
            File fileByPath = FileUtils.getFileByPath((String) reflect.field("EQ").get());
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(fileByPath.getAbsolutePath());
            stringBuffer.append("-unsigned");
            File file = new File(stringBuffer.toString());
            LogUtils.iTag("apkFile", new Object[]{fileByPath.getAbsolutePath()});
            LogUtils.iTag("unsignedApkFile", new Object[]{file.getAbsolutePath()});
            LogUtils.iTag("Start building", new Object[]{list});
            reflect.method("j6_", new Object[]{list}).get();
            Boolean bool = new Boolean(file.exists());
            LogUtils.eTag("unsignedApkFile exists", new Object[]{bool});
            LogUtils.i(new Object[]{"### BuildApking End ###"});
            ApiManager.getIMainActivity().onBeforeSigningAPK(file, obj);
        } catch (Throwable th) {
            LogUtils.eTag("BuildApking Error", new Object[]{th});
            RuntimeException runtimeException = new RuntimeException(ThrowableUtils.getFullStackTrace(th));
        }
    }

    private static void proxyD8(List list, Object obj) throws Exception {
        LogUtils.iTag("### D8 Dexing Start ###", new Object[0]);
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getApp());
        File filesDir = App.getApp().getFilesDir();
        File dataDir = App.getApp().getDataDir();
        boolean z = defaultSharedPreferences.getBoolean("adset_use_d8_18", false);
        String[] strArr = (String[]) ReflectUtils.reflect(obj).field("Hw").get();
        for (Object obj2 : strArr) {
            Object obj22 = obj2;
            String stringBuilder;
            LogUtils.iTag("Dexing Jar: ", new Object[]{obj22});
            StringBuilder stringBuilder2 = new StringBuilder();
            StringBuilder stringBuilder3 = new StringBuilder();
            stringBuilder3.append("dalvikvm -Xcompiler-option --compiler-filter=speed -Xmx256m -cp ");
            stringBuilder3.append(filesDir.getAbsolutePath());
            stringBuilder3.append("/d8.jar com.android.tools.r8.D8");
            stringBuilder2.append(stringBuilder3.toString());
            stringBuilder2.append(" --release");
            stringBuilder3 = new StringBuilder();
            stringBuilder3.append(" --lib ");
            stringBuilder3.append(new File(dataDir, "no_backup/.aide/android.jar").getAbsolutePath());
            stringBuilder2.append(stringBuilder3.toString());
            stringBuilder2.append(" --min-api 26");
            String str = " --classpath ";
            if (z) {
                StringBuilder stringBuilder4 = new StringBuilder();
                stringBuilder4.append(str);
                stringBuilder4.append(filesDir.getAbsolutePath());
                stringBuilder4.append("/rt.jar");
                stringBuilder = stringBuilder4.toString();
            } else {
                stringBuilder = " --no-desugaring";
            }
            stringBuilder2.append(stringBuilder);
            for (String file : strArr) {
                File file2 = new File(file);
                if (file2.exists()) {
                    StringBuilder stringBuilder5 = new StringBuilder();
                    stringBuilder5.append(str);
                    stringBuilder5.append(file2.getAbsolutePath());
                    stringBuilder2.append(stringBuilder5.toString());
                }
            }
            stringBuilder3 = new StringBuilder();
            stringBuilder3.append(" --output ");
            stringBuilder3.append(new File((String)obj22).getAbsolutePath());
            stringBuilder2.append(stringBuilder3.toString());
            stringBuilder2.append(" --intermediate");
            stringBuilder2.append(" ");
            CharSequence charSequence = "build/bin";
            CharSequence charSequence2 = "";
            CharSequence charSequence3 = "/classesrelease";
            if (((String)obj22).contains(charSequence)) {
                stringBuilder2.append(obj22);
            } else {
                stringBuilder2.append(((String)obj22).replace(charSequence3, charSequence2));
            }
            if (readInputStreem(Runtime.getRuntime().exec(stringBuilder2.toString()).getErrorStream()).isEmpty()) {
                if (!((String)obj22).contains(charSequence)) {
                    obj22 = ((String)obj22).replace(charSequence3, charSequence2);
                }
                list.add(obj22);
            } else {
                LogUtils.iTag("D8 Dexing Error: ", new Object[]{readInputStreem(Runtime.getRuntime().exec(stringBuilder2.toString()).getErrorStream())});
            }
            LogUtils.iTag("### D8 Dexing End ###", new Object[0]);
        }
    }

    public static List<String> proxyDexing(Object obj) {
        List<String> list;
        Throwable th;
        RuntimeException runtimeException;
        List<String> arrayList;
        Exception e;
        ArrayList arrayList2 = new ArrayList();
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getApp());
        ReflectUtils reflect = ReflectUtils.reflect(obj);
        try {
            LogUtils.iTag("### Dexing Start ###", new Object[0]);
            list = (List) ReflectUtils.reflect(obj).method("FH_").get();
            try {
                LogUtils.iTag("### Dexing End ###", new Object[0]);
            } catch (Throwable th2) {
                th = th2;
                LogUtils.eTag("Dexing Error", new Object[]{th});
                runtimeException = new RuntimeException(ThrowableUtils.getFullStackTrace(th));
                if (defaultSharedPreferences.getBoolean("adset_use_d8", false)) {
                    return list;
                }
                reflect.method("j6", new Object[]{"D8 Dexing..", Integer.valueOf(70)}).get();
                arrayList = new ArrayList();
                try {
                    proxyD8(arrayList, obj);
                    return arrayList;
                } catch (Exception e2) {
                    e = e2;
                    list = arrayList;
                    LogUtils.eTag("D8 Dexing Error", new Object[]{e.getMessage()});
                    return list;
                }
            }
        } catch (Throwable th3) {
            Throwable th4 = th3;
            list = arrayList2;
            th = th4;
            LogUtils.eTag("Dexing Error", new Object[]{th});
            runtimeException = new RuntimeException(ThrowableUtils.getFullStackTrace(th));
            if (defaultSharedPreferences.getBoolean("adset_use_d8", false)) {
                return list;
            }
            reflect.method("j6", new Object[]{"D8 Dexing..", Integer.valueOf(70)}).get();
            arrayList = new ArrayList();
            try {
				proxyD8(arrayList, obj);
				} catch(Exception ex){
				
			}
            return arrayList;
        }
        try {
            if (defaultSharedPreferences.getBoolean("adset_use_d8", false)) {
                return list;
            }
            reflect.method("j6", new Object[]{"D8 Dexing..", Integer.valueOf(70)}).get();
            arrayList = new ArrayList();
			try {
            proxyD8(arrayList, obj);
			} catch(Exception ex){
				
			}
            return arrayList;
        } catch (Exception e3) {
            e = e3;
            LogUtils.eTag("D8 Dexing Error", new Object[]{e.getMessage()});
            return list;
        }
    }

    public static void proxyRes(Object obj) throws Exception {
        File file = new File((String) ReflectUtils.reflect(obj).field("EQ").get());
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(file.getAbsolutePath());
        stringBuffer.append("-unsigned");
        File file2 = new File(stringBuffer.toString());
        file2.renameTo(file);
        ZipFile zipFile = new ZipFile(file);
        ZipOut input = new ZipOut(file2.getAbsolutePath()).setInput(zipFile);
        String str = "resources.arsc";
        input.removeFile(str);
        ArscObfuscator arscObfuscator = new ArscObfuscator(ZipOut.getZipInputStream(zipFile, str));
        input.addFile(str, arscObfuscator.getData());
        HashMap<String, String> map = arscObfuscator.getMap();
        for (String str2 : map.keySet()) {
            input.removeFile(str2);
            input.addFile((String) map.get(str2), ZipOut.toByteArray(ZipOut.getZipInputStream(zipFile, str2)));
            PrintStream printStream = System.out;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(str2);
            stringBuilder.append("->");
            stringBuilder.append((String) map.get(str2));
            printStream.println(stringBuilder.toString());
        }
        input.save();
    }

    public static void proxySign(Object obj) {
        String str = ".unsigned.apk";
        String str2 = "### AabtoApks End ###";
        String str3 = ".unsigned";
        String str4 = "com.miao.aide.ApkBuilderManager";
        String str5 = ".apk";
        String str6 = "CERT";
        String str7 = "";
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getApp());
        ReflectUtils reflect = ReflectUtils.reflect(obj);
        File file = new File((String) reflect.field("EQ").get());
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(file.getAbsolutePath());
        stringBuffer.append("-unsigned");
        File file2 = new File(stringBuffer.toString());
        if (defaultSharedPreferences.getBoolean("adset_use_resguard", false)) {
            try {
                proxyRes(obj);
            } catch (Exception e) {
                Exception exception = e;
                LogUtils.eTag("Resguard failed", new Object[]{exception});
            }
        }
        String str8 = "j6";
        if (defaultSharedPreferences.getBoolean("adset_use_zipalign", false)) {
            reflect.method(str8, new Object[]{"Zip Aligning...", Integer.valueOf(80)}).get();
            proxyZipAlign(obj);
            stringBuffer = new StringBuffer();
            stringBuffer.append(file.getAbsolutePath());
            stringBuffer.append("-aligned");
            file2 = new File(stringBuffer.toString());
        }
        reflect.method(str8, new Object[]{"Apk Signing...", Integer.valueOf(90)}).get();
        LogUtils.i(new Object[]{"### ApkSigner Start ###"});
        if (file.exists()) {
            file.delete();
        }
        try {
            String str9;
            Object obj2;
            String str10;
            String str11;
            PrivateKey privateKey;
            Object obj3;
            String str12;
            String string = defaultSharedPreferences.getString("user_keystore", str7);
            if (defaultSharedPreferences.getBoolean("use_user_ks", false)) {
                str4 = defaultSharedPreferences.getString("user_ks_type", str7);
                String str13 = "keyPass=";
                String str14 = "alias=";
                str9 = str;
                str = "password=";
                obj2 = str2;
                str2 = "keystore=";
                String str15;
                StringBuffer stringBuffer2;
                InputStream fileInputStream;
                KeyStore keyStore;
                if (str4.equals("default")) {
                    str4 = (String) reflect.field("J0").get();
                    str15 = (String) reflect.field("J8").get();
                    str10 = str3;
                    str3 = (String) reflect.field("Ws").get();
                    str11 = str5;
                    StringBuffer stringBuffer3 = new StringBuffer();
                    stringBuffer3.append(str2);
                    stringBuffer3.append(string);
                    str2 = stringBuffer3.toString();
                    stringBuffer3 = new StringBuffer();
                    stringBuffer3.append(str);
                    stringBuffer3.append(str4);
                    str = stringBuffer3.toString();
                    stringBuffer3 = new StringBuffer();
                    stringBuffer3.append(str14);
                    stringBuffer3.append(str15);
                    str5 = stringBuffer3.toString();
                    stringBuffer2 = new StringBuffer();
                    stringBuffer2.append(str13);
                    stringBuffer2.append(str3);
                    LogUtils.i(new Object[]{str2, str, str5, stringBuffer2.toString()});
                    fileInputStream = new FileInputStream(string);
                    keyStore = (KeyStore) ReflectUtils.reflect("com.aide.ui.build.android.I").newInstance().get();
                    keyStore.load(fileInputStream, str4.toCharArray());
                    privateKey = (PrivateKey) keyStore.getKey(str15, str3.toCharArray());
                    obj3 = (X509Certificate) keyStore.getCertificateChain(str15)[0];
                    CloseUtils.closeIOQuietly(new Closeable[]{fileInputStream});
                    str12 = str7;
                } else {
                    str10 = str3;
                    str11 = str5;
                    str3 = defaultSharedPreferences.getString("user_ks_cert_pass", str7);
                    str5 = defaultSharedPreferences.getString("user_ks_alias", str7);
                    str15 = defaultSharedPreferences.getString("user_ks_pass", str7);
                    str12 = str7;
                    StringBuffer stringBuffer4 = new StringBuffer();
                    stringBuffer4.append(str2);
                    stringBuffer4.append(string);
                    str2 = stringBuffer4.toString();
                    stringBuffer4 = new StringBuffer();
                    stringBuffer4.append(str);
                    stringBuffer4.append(str3);
                    str = stringBuffer4.toString();
                    stringBuffer4 = new StringBuffer();
                    stringBuffer4.append(str14);
                    stringBuffer4.append(str5);
                    str7 = stringBuffer4.toString();
                    stringBuffer2 = new StringBuffer();
                    stringBuffer2.append(str13);
                    stringBuffer2.append(str15);
                    LogUtils.i(new Object[]{str2, str, str7, stringBuffer2.toString()});
                    fileInputStream = new FileInputStream(string);
                    keyStore = KeyStore.getInstance(str4.toUpperCase());
                    keyStore.load(fileInputStream, str3.toCharArray());
                    privateKey = (PrivateKey) keyStore.getKey(str5, str15.toCharArray());
                    obj3 = (X509Certificate) keyStore.getCertificateChain(str5)[0];
                    CloseUtils.closeIOQuietly(new Closeable[]{fileInputStream});
                }
            } else {
                str9 = str;
                obj2 = str2;
                str10 = str3;
                str11 = str5;
                str12 = str7;
                try {
                    X509Certificate x509Certificate = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(Class.forName(str4).getResourceAsStream("/keys/testkey.x509.pem"));
                    CloseUtils.closeIOQuietly(new Closeable[]{Class.forName(str4).getResourceAsStream("/keys/testkey.x509.pem")});
                    InputStream resourceAsStream = Class.forName(str4).getResourceAsStream("/keys/testkey.pk8");
                    privateKey = new PKCS8Key();
                    PKCS8Key pKCS8Key = (PKCS8Key) privateKey;
                    ((PKCS8Key)privateKey).decode(resourceAsStream);
					
                    CloseUtils.closeIOQuietly(new Closeable[]{resourceAsStream});
                    obj3 = x509Certificate;
                } catch (ClassNotFoundException e2) {
                    throw new NoClassDefFoundError(e2.getMessage());
                }
            }
            Builder builder = new Builder(ImmutableList.of(new SignerConfig.Builder(str6, (PrivateKey)privateKey, (List)ImmutableList.of(obj3)).build()));
            builder.setInputApk(file2);
            builder.setOutputApk(file);
            builder.setMinSdkVersion(FileOptions.getMinSdkVersion(file2.getAbsolutePath()));
            builder.setV1SigningEnabled(true);
            builder.setV2SigningEnabled(true);
            builder.setV3SigningEnabled(true);
            builder.build().sign();
            file2.delete();
            LogUtils.i(new Object[]{"### ApkSigner End ###"});
            if (defaultSharedPreferences.getBoolean("adset_use_apktoaab", false)) {
                reflect.method(str8, new Object[]{"Generating AAB...", Integer.valueOf(95)}).get();
                LogUtils.i(new Object[]{"### ApktoAab Start ###"});
                StringBuffer stringBuffer5 = new StringBuffer();
                str7 = str11;
                str5 = str12;
                stringBuffer5.append(file.getAbsolutePath().replace(str7, str5));
                str4 = str10;
                stringBuffer5.append(str4);
                File file3 = new File(stringBuffer5.toString());
                StringBuffer stringBuffer6 = new StringBuffer();
                stringBuffer6.append(file.getAbsolutePath().replace(str4, str5));
                stringBuffer6.append(".aab");
                File file4 = new File(stringBuffer6.toString());
                if (file4.exists()) {
                    file4.delete();
                }
                ApkToAaabConverter apkToAaabConverter = new ApkToAaabConverter(App.getApp(), file, file3);
                apkToAaabConverter.start();
                reflect.method(str8, new Object[]{"AAB Signing...", Integer.valueOf(100)}).get();
                Builder builder2 = new Builder(ImmutableList.of(new SignerConfig.Builder(str6, (PrivateKey)privateKey, (List)ImmutableList.of(obj3)).build()));
                builder2.setInputApk(file3);
                builder2.setOutputApk(file4);
                builder2.setMinSdkVersion(FileOptions.getMinSdkVersion(file2.getAbsolutePath()));
                builder2.setV1SigningEnabled(true);
                builder2.setV2SigningEnabled(true);
                builder2.build().sign();
                file3.delete();
                LogUtils.i(new Object[]{"### ApktoAab End ###"});
                if (defaultSharedPreferences.getBoolean("adset_use_aabtoapks", false)) {
                    reflect.method(str8, new Object[]{"AabtoApks...", Integer.valueOf(100)}).get();
                    LogUtils.i(new Object[]{obj2});
                    String str16 = str9;
                    file3 = new File(file.getAbsolutePath().replace(str7, str16));
                    apkToAaabConverter.buildApkSet(file4, new File(file.getAbsolutePath().replace(str7, str16)));
                    Builder builder3 = new Builder(ImmutableList.of(new SignerConfig.Builder(str6, (PrivateKey)privateKey, (List)ImmutableList.of(obj3)).build()));
                    builder3.setInputApk(file3);
                    builder3.setOutputApk(file);
                    builder3.setMinSdkVersion(FileOptions.getMinSdkVersion(file2.getAbsolutePath()));
                    builder3.setV1SigningEnabled(true);
                    builder3.setV2SigningEnabled(true);
                    builder3.setV3SigningEnabled(true);
                    builder3.build().sign();
                    file3.delete();
                    LogUtils.i(new Object[]{obj2});
                }
            }
        } catch (Exception e3) {
            LogUtils.eTag("ApktoAab failed", new Object[]{e3});
        } catch (Throwable th) {
            LogUtils.eTag("Signature failed", new Object[]{th});
        }
    }

    public static void proxyZipAlign(Object obj) {
        LogUtils.i(new Object[]{"### ZipAlign Start ###"});
        File file = new File((String) ReflectUtils.reflect(obj).field("EQ").get());
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(file.getAbsolutePath());
        stringBuffer.append("-unsigned");
        File file2 = new File(stringBuffer.toString());
        stringBuffer = new StringBuffer();
        stringBuffer.append(file.getAbsolutePath());
        stringBuffer.append("-aligned");
        file = new File(stringBuffer.toString());
        if (file.exists()) {
            file.delete();
        }
        try {
            ZipAligner.align(file2.getAbsolutePath(), file.getAbsolutePath(), 2, false);
            file2.delete();
        } catch (Exception e) {
            LogUtils.eTag("Aligner failed", new Object[]{e});
        }
    }

    private static String readInputStreem(InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            while (true) {
                String readLine = bufferedReader.readLine();
                if (readLine == null) {
                    break;
                }
                stringBuilder.append(readLine);
                stringBuilder.append('\n');
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuilder.toString().trim();
    }
}
