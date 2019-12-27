package com.vianet.azure.sdk.manage.utils;

import org.junit.*;


/**
 * Created by chen.rui on 6/23/2016.
 */
public class TestInstallCell {

    @Test
    public void testInstallCell() throws Exception {
        InstallCert.doInstall("jymtesthbase.azurehdinsight.cn", "changeit".toCharArray());
    }


}
