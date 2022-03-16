/*
 * Copyright (c) 2022 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.avdevices.wireless.presentation.kramer.viaconnectpro.utils;

import java.util.HashSet;
import java.util.Set;

/**
 * VIAConnectProMetric enum class
 *
 * @author Kevin / Symphony Dev Team <br>
 * Created on 3/14/2022
 * @since 1.0.0
 */
public enum VIAConnectProMetric {

	/**
	 * Participant list,CNT require 2 params: cnt and 3
	 * ALL_STATUS: require 2 params: all and 4 (this command contains the username and its status)
	 *
	 */
	PLIST_CNT("ParticipantList","PList", "cnt,3"),
	PLIST_All_STATUS("ParticipantList","PList", "all,4"),

	/**
	 * Display status: Get-require 2 params (Get and Username)
	 * Display status: Set-require 2 params (Set and username)
	 */
	DISPLAY_STATUS_GET("DisplayStatus","DisplayStatus","Get"),
	DISPLAY_STATUS_SET("DisplayStatus","DisplayStatus", "Set"),

	/**
	 * Volume: Get-require 2 params (Vol and Get)
	 * Volume: Set-require 2 params (Vol and Set)
	 */
	VOLUME("Volume","Vol", "Get"),
	VOLUME_SET("Volume", "Vol", "Set"),

	/**
	 * Ip information
	 */
	IP_INFORMATION("IpInformation", "IpInfo",""),

	/**
	 * Room code settings:
	 * 	1. Get-ActiveStatus: 2 params (Get, ActiveStatus)
	 * 	2. Get-AppearStatus: 2 params (Get, AppearStatus)
	 * 	3. Get-Code				 : 2 params (Get, Code)
	 * 	4. Get-RTime			 : 2 params (Get, RTime)
	 * 	5. Set-ActiveStatus: 2 params (Set, ActiveStatus)
	 * 	6. Set-AppearStatus: 2 params (Set, AppearStatus)
	 * 	7. Set-RTime			 : 2 params (Set, RTime)
	 */
	ROOM_CODE_SETTINGS_GET_ACTIVE_STATUS("RoomCodeSettings","RCode","Get,ActiveStatus"),
	ROOM_CODE_SETTINGS_GET_APPEAR_STATUS("RoomCodeSettings","RCode","Get,AppearStatus"),
	ROOM_CODE_SETTINGS_GET_CODE("RoomCodeSettings","RCode","Get,Code"),
	ROOM_CODE_SETTINGS_GET_RTIME("RoomCodeSettings","RCode","Get,RTime"),

	ROOM_CODE_SETTINGS_SET_ACTIVE_STATUS("RoomCodeSettings", "RCode","Set,ActiveStatus"),
	ROOM_CODE_SETTINGS_SET_APPEAR_STATUS("RoomCodeSettings","RCode", "Set,AppearStatus"),
	ROOM_CODE_SETTINGS_SET_REFRESH_TIME("RoomCodeSettings","RCode", "Set,RTime"),

	/**
	 * Room name settings:
	 *  1. Get-Status: 2 params (Get, Status)
	 *  2. Get-Name  : 2 params (Get, Name)
	 *  3. Set-Status: 3 params (Set,Status, {0/1} (off/on))
	 *  4. Set-Name  : 3 params (Set, Name, {new name})
	 */
	ROOM_NAME_SETTINGS_GET("RoomNameSettings","RName","Get,Status" ),
	ROOM_NAME_SETTINGS_NAME("RoomNameSettings", "RName", "Get,Name"),

	ROOM_NAME_SETTINGS_SET_STATUS("RoomNameSettings","RName","Set,Status,1"),
	ROOM_NAME_SETTINGS_SET_NAME("RoomNameSettings","RName","Set,Name"),

	/**
	 * Datetime display status
	 *  1. Get-GetVisible: 1 param (GetVisible)
	 *  2. Set-SetVisible: 2 params (SetVisible, {0/1} (off/on))
	 */
	DATETIME_DISPLAY_STATUS_GET("DatetimeDisplayStatus","DateTime","GetVisible"),
	DATETIME_DISPLAY_STATUS_SET("DatetimeDisplayStatus","DateTime","SetVisible"),

	/**
	 * Presentation mode
	 *  1. Get-PrsMode: 1 param (Get)
	 *  2. Set-PrsMode: 2 params (Set, {0/1} (off/on))
	 */
	PRESENTATION_MODE_STATUS_GET("PresentationModeStatus","PrsMode","Get"),
	PRESENTATION_MODE_STATUS_SET("PresentationModeStatus","PrsMode","Set,1"),

	/**
	 * Log mode status
	 * 	1. Get-Log: 1 param (Get)
	 * 	2. Set-Log: 2 params (Set, {0/1} (off/on))
	 */
	LOG_MODE_STATUS_GET("LogModeStatus","Log","Get"),
	LOG_MODE_STATUS_SET("LogModeStatus","Log","Set,1"),

	/**
	 * Quick client access status
	 *  1. Get-QuickLaunch: 1 param (Get)
	 *  2. Set-QuickLaunch: 2 params (Set, {0/1} (off/on))
	 */
	QUICK_CLIENT_ACCESS_STATUS_GET("QuickClientAccessStatus","QuickLaunch","Get"),
	QUICK_CLIENT_ACCESS_STATUS_SET("QuickClientAccessStatus","QuickLaunch","Set,1"),

	/**
	 * User computer enable/disable control:
	 * 	Set status: require 2 params {0/1} (disable/enable) and username
	 */
	USER_COMPUTER_CONTROL("UserComputer","Control","1"),

	/**
	 * DND set or unset:
	 * 	Set status: require 2 params Set and username
	 */
	DND_SET("DND ","DND","Set"),

	/**
	 * Kick off user:
	 * 	Set status: require 1 param username
	 */
	KICK_OFF("KickOff","KickOff",""),

	/**
	 * Set screen share status:
	 * 	Set status: require 2 params On/Off and username
	 */
	SCREEN_SHARE_STATUS_SET("ScreenShareStatus","ScreenShare",""),

	/**
	 * Gateway: serial number, mac address, version
	 */
	GATEWAY_SERIAL_NUMBER_GET("GatewaySerialNumber","GetSerialNo",""),
	GATEWAY_MAC_ADDRESS_GET("GatewayMacAddress","GetMacAdd",""),
	GATEWAY_VERSION_GET("GatewayVersion","GetVersion",""),

	/**
	 * Chrome connectivity status and api mode status
	 *  1. Get-Chrome : 1 param (Get)
	 *  2. Get-APIMode: 1 param (Get)
	 *  3. Set-Chrome : 2 params (Set and 0/1 (off/on))
	 *  4. Set-APIMode: 2 params (Set and 0/1 (off/on))
	 */
	CHROME_STATUS_GET("Chrome","Chrome","Get"),
	CHROME_STATUS_API_MODE_GET("Chrome","APIMode","Get"),
	CHROME_STATUS_SET("Chrome","Chrome", "Set" ),
	CHROME_API_MODE_SET("Chrome","APIMode", "Set" ),

	/**
	 * Room overlay status
	 *  1. Get-RoomOverlay: 1 param (Get)
	 *  2. Set-RoomOverlay: 3 param (Set,  0/1 (off/on) room overlay, 0/1 (off/on) auto hide time)
	 */
	ROOM_OVERLAY_STATUS_GET("RoomOverlay","RoomOverlay","Get"),
	ROOM_OVERLAY_STATUS_SET("RoomOverlay","RoomOverlay","Set"),

	/**
	 * Audio devices
	 */
	AUDIO_DEVICES_GET("AudioDevices","GetAudioDevice","5"), // Number of devices ?

	/**
	 * Streaming status
	 *  1. Get-Streaming: 1 param (Get)
	 *  2. Get-Streaming-SStatus: 1 param (SStatus)
	 */
	STREAMING_STATUS_GET("Streaming","Streaming","Get"),
	STREAMING_STATUS_SSTATUS_GET("Streaming","Streaming","SStatus"),

	/**
	 * Start streaming
	 * 	Set status: require 2 params SStart and username
	 * Stop streaming
	 *	Set status: require 2 params SStop and username
	 * Restart streaming:
	 * 	require 3 params: SRestart, username, URL name
	 * Change streaming:
	 * 	require 3 params: SChange, username, URL name
	 * Streaming:
	 * 	require 2 params: Set, 0/1 (off/on)
	 */
	STREAMING_START("StreamingControl","Streaming","SStart"),
	STREAMING_STOP("StreamingControl","Streaming","SStop"),
	STREAMING_RESTART("StreamingControl","Streaming","SRestart"),
	STREAMING_CHANGE("StreamingControl","Streaming","SChange"),

	/**
	 * Streaming URL: require 2 params 1,  URL path
	 */
	STREAMING_URL("Streaming","StreamingURL","1"),

	/**
	 * WhiteBoard:
	 * - On: 1 param On
	 * - Off-autosave: 2 params Off, 1
	 * - Off-discard: 2 params Off, 2
	 * - Switch: 2 params Switch, 2/1 (Desktop mode/ WhiteBoard mode)
	 */
	WHITE_BOARD_ON("WhiteBoard","WhiteBoard","On"),
	WHITE_BOARD_OFF_AUTOSAVE("WhiteBoard","WhiteBoard","Off,1"),
	WHITE_BOARD_OFF_DISCARD("WhiteBoard","WhiteBoard","Off,2"),
	WHITE_BOARD_SWITCH("WhiteBoard","WhiteBoard","Switch,2"),

	/**
	 * Part preset confirm
	 *  1. Get-PartPresentConfirm: 1 param (Get)
	 *  2. Set-PartPresentConfirm: 2 params (Set, 0/1 (off/on))
	 */
	PART_PRESET_CONFIRM_GET("PartPresentConfirmStatus","PartPresentConfirm", "Get"),
	PART_PRESET_CONFIRM_SET("PartPresentConfirmStatus","PartPresentConfirm","Set"),

	/**
	 * Wi-Fi Guest Mode
	 *  1. Get-Status: 1 param (Status)
	 *  2. Set-WifiGuestMode-Start/Stop: 0/1
	 *  3. Set-WifiGuestMode-Status: 1 param Status
	 */
	WIFI_GUEST_MODE("WifiGuestMode", "WifiGuestMode","Status"),
	WIFI_GUEST_MODE_START("WifiGuestMode","WifiGuestMode","1"),
	WIFI_GUEST_MODE_STOP("WifiGuestMode","WifiGuestMode","0"),
	WIFI_GUEST_MODE_STATUS("WifiGuestMode","WifiGuestMode","Status");

	/**
	 * VIAConnectProControllingMetric with arguments constructor
	 *
	 * @param groupName Name of the metric
	 * @param command VIA Connect PRO Command
	 * @param param parameter of the command.
	 */
	VIAConnectProMetric(String groupName, String command, String param) {
		this.groupName = groupName;
		this.command = command;
		this.param = param;
	}

	private final String groupName;
	private final String command;
	private final String param;

	/**
	 * Retrieves {@code {@link #groupName }}
	 *
	 * @return value of {@link #groupName}
	 */
	public String getGroupName() {
		return groupName;
	}

	/**
	 * Retrieves {@code {@link #command}}
	 *
	 * @return value of {@link #command}
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * Retrieves {@code {@link #param}}
	 *
	 * @return value of {@link #param}
	 */
	public String getParam() {
		return param;
	}

	/**
	 * Get groupName of metric from VIAConnectProMetric
	 *
	 * @param groupName groupName of metric
	 * @return Enum of VIAConnectProMetric
	 */
	public static VIAConnectProMetric getByName(String groupName) {
		for (VIAConnectProMetric metric: VIAConnectProMetric.values()) {
			if (metric.getGroupName().equals(groupName)) {
				return metric;
			}
		}
		throw new IllegalArgumentException("Cannot find the enum with groupName: " + groupName);
	}

	/**
	 * Get set of groupNames from VIAConnectProMetric
	 *
	 * @return Set of groupNames
	 */
	public static Set<String> getGroupNames() {
		Set<String> groupNamesSet = new HashSet<>();
		for (VIAConnectProMetric metric: VIAConnectProMetric.values()) {
			groupNamesSet.add(metric.getGroupName());
		}
		return groupNamesSet;
	}
}
