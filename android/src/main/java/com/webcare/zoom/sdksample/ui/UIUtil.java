package com.webcare.zoom.sdksample.ui;

import android.content.Context;
import android.content.Intent;

import java.util.List;

import us.zoom.sdk.IBOData;
import us.zoom.sdk.IBOMeeting;
import us.zoom.sdk.InMeetingBOController;
import us.zoom.sdk.ZoomSDK;
import com.webcare.zoom.sdksample.inmeetingfunction.customizedmeetingui.MyMeetingActivity;
import com.webcare.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.MeetingWindowHelper;

public class UIUtil {
    public static void returnToMeeting(Context context) {
        if(context == null)
            return;
        if(ZoomSDK.getInstance().getMeetingSettingsHelper().isCustomizedMeetingUIEnabled()) {
            MeetingWindowHelper.getInstance().hiddenMeetingWindow(true);
            Intent intent = new Intent(context, MyMeetingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            context.startActivity(intent);
        } else {
            ZoomSDK.getInstance().getZoomUIService().hideMiniMeetingWindow();
            ZoomSDK.getInstance().getMeetingService().returnToMeeting(context);
        }

    }

    public static String[] getBoNameUserNameByUserId(InMeetingBOController boController, String userId) {
        String[] res = new String[]{"", ""};
        if (boController == null || userId == null || userId.length() == 0)
            return res;
        IBOData iboData = boController.getBODataHelper();
        if (iboData != null) {
            List<String> bIds = iboData.getBOMeetingIDList();
            if (bIds != null && bIds.size() > 0) {
                for (String bId : bIds) {
                    IBOMeeting iboMeeting = iboData.getBOMeetingByID(bId);
                    if (iboMeeting != null) {
                        List<String> users = iboMeeting.getBoUserList();
                        if(users != null && users.contains(userId)) {
                            res[0] = iboMeeting.getBoName();
                            res[1] = iboData.getBOUserName(userId);
                            break;
                        }
                    }
                }
            }
        }
        return res;
    }
}
