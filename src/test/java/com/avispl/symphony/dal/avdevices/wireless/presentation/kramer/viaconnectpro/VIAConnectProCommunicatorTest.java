package com.avispl.symphony.dal.avdevices.wireless.presentation.kramer.viaconnectpro;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VIAConnectProCommunicatorTest {
    static VIAConnectProCommunicator viaConnectProCommunicator;
    @BeforeEach
    public void init() throws Exception {
        viaConnectProCommunicator = new VIAConnectProCommunicator();
        viaConnectProCommunicator.setProtocol("telnet");
        viaConnectProCommunicator.setHost("172.31.254.224");
        viaConnectProCommunicator.setPort(9982);
        viaConnectProCommunicator.setLogin("su");
        viaConnectProCommunicator.setPassword("supass");
        viaConnectProCommunicator.init();
//        <P><UN>su</UN><Pwd></Pwd><Cmd>PartPresentConfirm</Cmd><P1>Get<P1></P>
    }


    @Test
    void getMultipleStatistics() throws Exception {
                 viaConnectProCommunicator.getMultipleStatistics();
    }
}