// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.flipper.plugins.litho;

import android.view.View;
import android.view.ViewGroup;
import com.facebook.litho.LithoView;
import com.facebook.flipper.core.FlipperConnection;
import com.facebook.flipper.core.FlipperObject;
import com.facebook.flipper.core.FlipperReceiver;
import com.facebook.flipper.core.FlipperResponder;
import com.facebook.flipper.plugins.common.MainThreadFlipperReceiver;
import com.facebook.flipper.plugins.inspector.ApplicationWrapper;
import com.facebook.flipper.plugins.inspector.InspectorSonarPlugin;
import com.facebook.flipper.plugins.inspector.ObjectTracker;

import java.util.Stack;

public final class GenerateLithoAccessibilityRenderExtensionCommand implements InspectorSonarPlugin.ExtensionCommand {

    @Override
    public String command() {
        return "forceLithoAXRender";
    }

    @Override
    public FlipperReceiver receiver(final ObjectTracker tracker, final FlipperConnection connection) {
        return new MainThreadFlipperReceiver(connection) {
            @Override
            public void onReceiveOnMainThread(final FlipperObject params, final FlipperResponder responder)
                    throws Exception {
                final String applicationId = params.getString("applicationId");

                // check that the application is valid
                if (applicationId == null) {
                    return;
                }
                final Object obj = tracker.get(applicationId);
                if (obj != null && !(obj instanceof ApplicationWrapper)) {
                    return;
                }

                final ApplicationWrapper applicationWrapper = ((ApplicationWrapper) obj);
                final boolean forceLithoAXRender = params.getBoolean("forceLithoAXRender");
                final boolean prevForceLithoAXRender = Boolean.getBoolean("is_accessibility_enabled");

                // nothing has changed, so return
                if (forceLithoAXRender == prevForceLithoAXRender) {
                    return;
                }

                // change property and rerender
                System.setProperty("is_accessibility_enabled", forceLithoAXRender + "");
                forceRerenderAllLithoViews(forceLithoAXRender, applicationWrapper);
            }
        };
    }

    private void forceRerenderAllLithoViews(boolean forceLithoAXRender, ApplicationWrapper applicationWrapper) {

        // iterate through tree and rerender all litho views
        Stack<ViewGroup> lithoViewSearchStack = new Stack<>();
        for (View root : applicationWrapper.getViewRoots()) {
            if (root instanceof ViewGroup) {
                lithoViewSearchStack.push((ViewGroup) root);
            }
        }

        while (!lithoViewSearchStack.isEmpty()) {
            ViewGroup v = lithoViewSearchStack.pop();
            if (v instanceof LithoView) {
                // TODO: uncomment once Litho open source updates
//                ((LithoView) v).rerenderForAccessibility(forceLithoAXRender);
            } else {
                for (int i = 0; i < v.getChildCount(); i++) {
                    View child = v.getChildAt(i);
                    if (child instanceof ViewGroup) {
                        lithoViewSearchStack.push((ViewGroup) child);
                    }
                }
            }
        }
    }
}