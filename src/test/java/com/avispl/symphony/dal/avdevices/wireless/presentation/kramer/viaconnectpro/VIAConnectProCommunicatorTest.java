/*
 * Copyright (c) 2022 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.avdevices.wireless.presentation.kramer.viaconnectpro;

import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;
import com.avispl.symphony.dal.avdevices.wireless.presentation.kramer.viaconnectpro.utils.VIAConnectProConstant;
import com.avispl.symphony.dal.avdevices.wireless.presentation.kramer.viaconnectpro.utils.VIAConnectProMonitoringMetric;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * VIAConnectProCommunicatorTest
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 3/23/2022
 * @since 1.0.0
 */
class VIAConnectProCommunicatorTest {

    private static final String PROTOCOL = "telnet";
    private static final String REAL_DEVICE_HOST = "172.31.254.224";
    private static final String USERNAME = "su";
    private static final String PASSWORD = "supass";
    private static final int PORT = 9982;

    static VIAConnectProCommunicator viaConnectProCommunicator;
    @BeforeEach
    public void init() throws Exception {
        viaConnectProCommunicator = new VIAConnectProCommunicator();
        viaConnectProCommunicator.setProtocol(PROTOCOL);
        viaConnectProCommunicator.setHost(REAL_DEVICE_HOST);
        viaConnectProCommunicator.setPort(PORT);
        viaConnectProCommunicator.setLogin(USERNAME);
        viaConnectProCommunicator.setPassword(PASSWORD);
        viaConnectProCommunicator.setUserRole("Moderator");
        viaConnectProCommunicator.internalInit();
    }

    @AfterEach
    public void destroy() {
        viaConnectProCommunicator.internalDestroy();
    }

    /**
     * Test get statistics:
     * - Get statistics and controls from the device.
     * @throws Exception When fail to getMultipleStatistics
     */
    @Test
    @Tag("RealDevice")
    void testGetMultipleStatistics() throws Exception {
        ExtendedStatistics extendedStatistics = (ExtendedStatistics) viaConnectProCommunicator.getMultipleStatistics().get(0);
        Map<String, String> stats = extendedStatistics.getStatistics();
        Assertions.assertNotNull(stats.get(VIAConnectProConstant.IP_ADDRESS));
        Assertions.assertNotNull(stats.get(VIAConnectProConstant.SUBNET_MASK));
        Assertions.assertNotNull(stats.get(VIAConnectProConstant.GATEWAY));
        Assertions.assertNotNull(stats.get(VIAConnectProConstant.DNS_SERVER));
        Assertions.assertNotNull(stats.get(VIAConnectProConstant.HOST_NAME));
        Assertions.assertNotNull(stats.get(VIAConnectProConstant.ROOM_CODE));
        Assertions.assertNotNull(stats.get(VIAConnectProConstant.VERSION));
        Assertions.assertNotNull(stats.get(VIAConnectProConstant.MAC_ADDRESS));
        Assertions.assertNotNull(stats.get(VIAConnectProConstant.SERIAL_NUMBER));

        String deviceSettingsGroup = VIAConnectProMonitoringMetric.ACTIVE_SYSTEM_LOG_GET.getGroupName();
        Assertions.assertNotNull(stats.get(String.format("%s#%s", deviceSettingsGroup, VIAConnectProConstant.ACTIVATE_SYSTEM_LOG)));
        Assertions.assertNotNull(stats.get(String.format("%s#%s", deviceSettingsGroup, VIAConnectProConstant.JOIN_THROUGH_BROWSER)));
        Assertions.assertNotNull(stats.get(String.format("%s#%s", deviceSettingsGroup, VIAConnectProConstant.API_SETTINGS_COMMAND)));
        Assertions.assertNotNull(stats.get(String.format("%s#%s", deviceSettingsGroup, VIAConnectProConstant.AUDIO_OUTPUT)));
        Assertions.assertNotNull(stats.get(String.format("%s#%s", deviceSettingsGroup, VIAConnectProConstant.QUICK_CLIENT_ACCESS)));
        Assertions.assertNotNull(stats.get(String.format("%s#%s", deviceSettingsGroup, VIAConnectProConstant.VOLUME)));

        String deviceSettingsModeratorGroup = VIAConnectProMonitoringMetric.PART_PRESENT_CONFIRM_GET.getGroupName();
        Assertions.assertNotNull(stats.get(String.format("%s#%s", deviceSettingsModeratorGroup, VIAConnectProConstant.STATUS)));
        Assertions.assertNotNull(stats.get(String.format("%s#%s", deviceSettingsModeratorGroup, VIAConnectProConstant.PARTICIPANT_PRESENT_CONFIRM)));

        Assertions.assertNotNull(stats.get(String.format("%s#%s", VIAConnectProMonitoringMetric.ROOM_OVERLAY_STATUS_GET.getGroupName(), VIAConnectProConstant.STATUS)));
    }

    /**
     * Test get statistics:
     * - Get statistics and controls from the device with user role
     * @throws Exception When fail to getMultipleStatistics
     */
    @Test
    @Tag("RealDevice")
    void testGetMultipleStatisticsWithUserRole() throws Exception {
        viaConnectProCommunicator.internalDestroy();
        viaConnectProCommunicator.setUserRole("User");
        viaConnectProCommunicator.internalInit();
        ExtendedStatistics extendedStatistics = (ExtendedStatistics) viaConnectProCommunicator.getMultipleStatistics().get(0);
        Map<String, String> stats = extendedStatistics.getStatistics();
        String groupName = VIAConnectProMonitoringMetric.ROOM_OVERLAY_STATUS_GET.getGroupName();
        Assertions.assertNull(stats.get(String.format("%s#%s", groupName, VIAConnectProConstant.STATUS)));
    }

    /**
     * Test get statistics:
     * - Get statistics and controls from the device with invalid user role.
     * @throws Exception When fail to getMultipleStatistics
     */
    @Test
    @Tag("RealDevice")
    void testGetMultipleStatisticsWithUserRoleInvalid() throws Exception {
        viaConnectProCommunicator.internalDestroy();
        // Invalid userRole => Role will be set to User
        viaConnectProCommunicator.setUserRole("@@##");
        viaConnectProCommunicator.internalInit();
        ExtendedStatistics extendedStatistics = (ExtendedStatistics) viaConnectProCommunicator.getMultipleStatistics().get(0);
        Map<String, String> stats = extendedStatistics.getStatistics();
        String groupName = VIAConnectProMonitoringMetric.ROOM_OVERLAY_STATUS_GET.getGroupName();
        Assertions.assertNull(stats.get(String.format("%s#%s", groupName, VIAConnectProConstant.STATUS)));
    }

    /**
     * Test get statistics:
     * - Get statistics and controls from the device with userRole = Empty
     * @throws Exception When fail to getMultipleStatistics
     */
    @Test
    @Tag("RealDevice")
    void testGetMultipleStatisticsWithUserRoleEmpty() throws Exception {
        viaConnectProCommunicator.internalDestroy();
        viaConnectProCommunicator.setUserRole("");
        // When userRole is empty, role will be set to User
        viaConnectProCommunicator.internalInit();
        ExtendedStatistics extendedStatistics = (ExtendedStatistics) viaConnectProCommunicator.getMultipleStatistics().get(0);
        Map<String, String> stats = extendedStatistics.getStatistics();
        String groupName = VIAConnectProMonitoringMetric.ROOM_OVERLAY_STATUS_GET.getGroupName();
        Assertions.assertNull(stats.get(String.format("%s#%s", groupName, VIAConnectProConstant.STATUS)));
    }

    /**
     * Test get statistics:
     * - Get statistics and controls from the device with moderator role
     * @throws Exception When fail to getMultipleStatistics
     */
    @Test
    @Tag("RealDevice")
    void testGetMultipleStatisticsWithModRole() throws Exception {
        viaConnectProCommunicator.internalDestroy();
        viaConnectProCommunicator.setUserRole("Moderator");
        viaConnectProCommunicator.internalInit();
        ExtendedStatistics extendedStatistics = (ExtendedStatistics) viaConnectProCommunicator.getMultipleStatistics().get(0);
        Map<String, String> stats = extendedStatistics.getStatistics();
        String groupName = VIAConnectProMonitoringMetric.ROOM_OVERLAY_STATUS_GET.getGroupName();
        Assertions.assertNotNull(stats.get(String.format("%s#%s", groupName, VIAConnectProConstant.STATUS)));
    }

    /**
     * Test get statistics:
     * - Test when no one is logged-in
     * @throws Exception When fail to control or get statistics
     */
    @Test
    @Tag("RealDevice")
    void testNoUserLogin() throws Exception {
        ExtendedStatistics extendedStatistics = (ExtendedStatistics) viaConnectProCommunicator.getMultipleStatistics().get(0);
        Map<String, String> stats = extendedStatistics.getStatistics();
        Assertions.assertEquals("No one is logged in.",stats.get(String.format("%s#%s", VIAConnectProConstant.PARTICIPANT_LIST, VIAConnectProConstant.USERNAME)));
    }

    /**
     * Test control:
     * - Test set volume
     * @throws Exception When fail to control or get statistics
     */
    @Test
    @Tag("RealDevice")
    void testSetVolume() throws Exception {
        viaConnectProCommunicator.getMultipleStatistics();
        ControllableProperty controllableProperty = new ControllableProperty();
        controllableProperty.setProperty(String.format("%s#%s",VIAConnectProConstant.DEVICE_SETTINGS, VIAConnectProConstant.VOLUME));
        controllableProperty.setValue("20");
        viaConnectProCommunicator.controlProperty(controllableProperty);
        ExtendedStatistics extendedStatistics = (ExtendedStatistics) viaConnectProCommunicator.getMultipleStatistics().get(0);
        Assertions.assertEquals("20", extendedStatistics.getStatistics().get(String.format("%s#%s", VIAConnectProConstant.DEVICE_SETTINGS, VIAConnectProConstant.VOLUME)));
    }

    /**
     * Test control:
     * - Test set new stream url
     * @throws Exception When fail to control or get statistics
     */
    @Test
    @Tag("RealDevice")
    void testSetNewStreamURL() throws Exception {
        viaConnectProCommunicator.getMultipleStatistics();
        ControllableProperty controllableProperty = new ControllableProperty();
        controllableProperty.setProperty(String.format("%s#%s",VIAConnectProConstant.STREAMING_FROM_EXTERNAL_TO_DEVICE, VIAConnectProConstant.EXTERNAL_STREAM_URL));
        controllableProperty.setValue("https://newstream.com");
        viaConnectProCommunicator.controlProperty(controllableProperty);
        ExtendedStatistics extendedStatistics = (ExtendedStatistics) viaConnectProCommunicator.getMultipleStatistics().get(0);
        Assertions.assertEquals("https://newstream.com", extendedStatistics.getStatistics().get(String.format("%s#%s",VIAConnectProConstant.STREAMING_FROM_EXTERNAL_TO_DEVICE, VIAConnectProConstant.EXTERNAL_STREAM_URL)));
    }

    /**
     * Test control:
     * - Test set new stream url
     * - Next interval if StartStream is not clicked => that property will be populated/
     * @throws Exception When fail to control or get statistics
     */
    @Test
    @Tag("RealDevice")
    void testCachedControlProperty() throws Exception {
        viaConnectProCommunicator.getMultipleStatistics();
        ControllableProperty controllableProperty = new ControllableProperty();
        controllableProperty.setProperty(String.format("%s#%s",VIAConnectProConstant.STREAMING_FROM_EXTERNAL_TO_DEVICE, VIAConnectProConstant.EXTERNAL_STREAM_URL));
        controllableProperty.setValue("https://newstream.com");
        viaConnectProCommunicator.controlProperty(controllableProperty);
        ExtendedStatistics beforeControlGetMulti = (ExtendedStatistics) viaConnectProCommunicator.getMultipleStatistics().get(0);
        Assertions.assertEquals("https://newstream.com", beforeControlGetMulti.getStatistics().get(String.format("%s#%s",VIAConnectProConstant.STREAMING_FROM_EXTERNAL_TO_DEVICE, VIAConnectProConstant.EXTERNAL_STREAM_URL)));
        // getMultipleStatistics next interval will contain cached EXTERNAL_STREAM_URL
        ExtendedStatistics normalGetMulti = (ExtendedStatistics) viaConnectProCommunicator.getMultipleStatistics().get(0);
        Assertions.assertEquals("https://newstream.com", normalGetMulti.getStatistics().get(String.format("%s#%s",VIAConnectProConstant.STREAMING_FROM_EXTERNAL_TO_DEVICE, VIAConnectProConstant.EXTERNAL_STREAM_URL)));
    }
}