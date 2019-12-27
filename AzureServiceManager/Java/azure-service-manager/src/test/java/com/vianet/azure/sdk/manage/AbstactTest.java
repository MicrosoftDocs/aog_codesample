package com.vianet.azure.sdk.manage;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.management.configuration.PublishSettingsLoader;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chen.rui on 6/7/2016.
 */
public class AbstactTest {

    public static final String publishsetting = "C:\\Users\\kevin\\Documents\\CIETest03.publishsettings";

    Configuration config = null;

    public static final String TENTANT = "your tenlent id";
    public static final String CLIENT_ID = "your client id";
    public static final String CLIENT_SECRET = "123456";
    public static final String SUB_ID = "e0fbea86-6cf2-4b2d-81e2-9c59f4f96bcb";
    public final static String MANAGEMENT_EBDPOINT = "https://management.chinacloudapi.cn/";
    public final static String CLASSIC_MANAGEMENT_EBDPOINT = "https://management.core.chinacloudapi.cn/";

    public Configuration getConfig() {
        try {
            if(config == null) {
                config = PublishSettingsLoader.createManagementConfiguration(publishsetting, SUB_ID);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }


}
