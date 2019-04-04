/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.flipper.sample;

import android.app.Application;
import android.content.Context;
import androidx.annotation.Nullable;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.flipper.android.AndroidFlipperClient;
import com.facebook.flipper.core.FlipperClient;
import com.facebook.flipper.plugins.crashreporter.CrashReporterPlugin;
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin;
import com.facebook.flipper.plugins.example.ExampleFlipperPlugin;
import com.facebook.flipper.plugins.fresco.FrescoFlipperPlugin;
import com.facebook.flipper.plugins.inspector.DescriptorMapping;
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin;
import com.facebook.flipper.plugins.leakcanary.LeakCanaryFlipperPlugin;
import com.facebook.flipper.plugins.litho.LithoFlipperDescriptors;
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor;
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin;
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin;
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin.SharedPreferencesDescriptor;
import com.facebook.litho.config.ComponentsConfiguration;
import com.facebook.soloader.SoLoader;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;

public class FlipperSampleApplication extends Application {

  @Nullable public static OkHttpClient sOkHttpClient = null;

  @Override
  public void onCreate() {
    super.onCreate();
    SoLoader.init(this, false);
    Fresco.initialize(this);

    final FlipperClient client = AndroidFlipperClient.getInstance(this);
    final DescriptorMapping descriptorMapping = DescriptorMapping.withDefaults();

    final NetworkFlipperPlugin networkPlugin = new NetworkFlipperPlugin();
    final FlipperOkhttpInterceptor interceptor = new FlipperOkhttpInterceptor(networkPlugin);

    sOkHttpClient =
        new OkHttpClient.Builder()
            .addNetworkInterceptor(interceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.MINUTES)
            .build();

    // Normally, you would want to make this dependent on a BuildConfig flag, but
    // for this demo application we can safely assume that you always want to debug.
    ComponentsConfiguration.isDebugModeEnabled = true;
    LithoFlipperDescriptors.add(descriptorMapping);
    client.addPlugin(new InspectorFlipperPlugin(this, descriptorMapping));
    client.addPlugin(networkPlugin);
    client.addPlugin(
        new SharedPreferencesFlipperPlugin(
            this,
            Arrays.asList(
                new SharedPreferencesDescriptor("sample", Context.MODE_PRIVATE),
                new SharedPreferencesDescriptor("other_sample", Context.MODE_PRIVATE))));
    client.addPlugin(new LeakCanaryFlipperPlugin());
    client.addPlugin(new FrescoFlipperPlugin());
    client.addPlugin(new ExampleFlipperPlugin());
    client.addPlugin(CrashReporterPlugin.getInstance());
    client.addPlugin(new DatabasesFlipperPlugin());
    client.start();

    getSharedPreferences("sample", Context.MODE_PRIVATE).edit().putString("Hello", "world").apply();
    getSharedPreferences("other_sample", Context.MODE_PRIVATE)
        .edit()
        .putInt("SomeKey", 1337)
        .apply();
  }
}
