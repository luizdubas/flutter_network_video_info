package com.example.flutter_video_info;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * FlutterVideoInfoPlugin
 */
public class FlutterVideoInfoPlugin implements FlutterPlugin, MethodCallHandler {

    private String chName = "flutter_video_info";
    private Context context;
    private ExecutorService executor;
    private MethodChannel channel;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        context = binding.getApplicationContext();
        executor = Executors.newCachedThreadPool();
        channel = new MethodChannel(binding.getBinaryMessenger(), "flutter_video_info");
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        channel = null;
        executor.shutdown();
        executor = null;
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (call.method.equals("getVidInfo")) {
            String path = call.argument("path");
            result.success(getVidInfo(path));
        } else {
            result.notImplemented();
        }
    }

    String getVidInfo(String path) {
        String author,dateString,mimeType,location,frameRateStr,widthStr,heightStr,durationStr,orientation;
        double filesize;
        MediaMetadataRetriever mediaRetriever = new MediaMetadataRetriever();
        JSONObject json = new JSONObject();
        try {
            mediaRetriever.setDataSource(path, new HashMap<>());
            author = getData(MediaMetadataRetriever.METADATA_KEY_AUTHOR, mediaRetriever);
            dateString = getData(MediaMetadataRetriever.METADATA_KEY_DATE, mediaRetriever);
             try {
                SimpleDateFormat readFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss.SSS", Locale.getDefault());
                Date date = readFormat.parse(dateString);
                SimpleDateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                dateString = outFormat.format(date);
            } catch (Exception e) {
                e.printStackTrace();
            }

            mimeType = getData(MediaMetadataRetriever.METADATA_KEY_MIMETYPE, mediaRetriever);
            location = getData(MediaMetadataRetriever.METADATA_KEY_LOCATION, mediaRetriever);
            frameRateStr = getData(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE, mediaRetriever);
            durationStr = getData(MediaMetadataRetriever.METADATA_KEY_DURATION, mediaRetriever);
            widthStr = getData(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH, mediaRetriever);
            heightStr = getData(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT, mediaRetriever);
            filesize = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                orientation = getData(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION, mediaRetriever);
            } else {
                orientation = null;
            }

            try {
                 mediaRetriever.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                json.put("path", path);
                json.put("mimetype", mimeType);
                json.put("author", author);
                json.put("date", dateString);
                json.put("width", widthStr);
                json.put("height", heightStr);
                json.put("location", location);
                json.put("framerate", frameRateStr);
                json.put("duration", durationStr);
                json.put("filesize", filesize);
                json.put("orientation", orientation);
                json.put("isfileexist",true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return json.toString();
    }

    String getData(int key, MediaMetadataRetriever mediaRetriever) {
        try {
            return mediaRetriever.extractMetadata(key);
        } catch (Exception e) {
            return null;
        }
    }

}
