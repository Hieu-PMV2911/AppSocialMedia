package com.example.doanchuyennganh.notifications;

import java.util.HashMap;

public class Contants {
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-type";
    public static final String REMOTE_MSG_TYPE = "type";
    public static final String REMOTE_MSG_INVITATION = "invitation";
    public static final String REMOTE_MSG_MEETING_TYPE = "meetingType";
    public static final String REMOTE_MSG_INVITATION_TOKEN = "invitationToken";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";

    public static HashMap<String, String> getRemoteMessageHeader(){
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(
                Contants.REMOTE_MSG_AUTHORIZATION,
                "key=AAAAEBtdsc0:APA91bFn3lKbHY_Mt5QQ-UKZI-D2W7vzBJA0Bs--DR4VsllHA298a9kpPh7dvn0p928aoWHrSPdb8ZC3rQjEaQTtVU2nUMctYu5qJCLacYLKq16XvwGwVFdE_Cd6l_u1PhM88qyP7lN5"
        );
        hashMap.put(Contants.REMOTE_MSG_CONTENT_TYPE, "application/json");
        return hashMap;
    }

}
