/*
 * Copyright (c) 2022 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.avdevices.wireless.presentation.kramer.viaconnectpro.utils;

/**
 * VIAConnectProMonitoringMetric enum class
 *
 * @author Kevin / Symphony Dev Team <br>
 * Created on 3/14/2022
 * @since 1.0.0
 */
public enum VIAConnectProMonitoringMetric {

	/**
	 * Participant list,CNT require 2 params: cnt and 3
	 * ALL_STATUS: require 2 params: all and 4 (this command contains the username and its status)
	 */
	PLIST_CNT("ParticipantList", "PList", "cnt,3"),
	PLIST_All_STATUS("ParticipantList", "PList", "all,4"),

	/**
	 * Display status: Get-require 2 params (Get and Username)
	 */
	DISPLAY_STATUS_GET("ParticipantList", "DisplayStatus", "Get"),

	/**
	 * Volume: Get-require 2 params (Vol and Get)
	 */
	VOLUME("DeviceSettings", "Vol", "Get"),

	/**
	 * Ip information
	 */
	IP_INFORMATION("", "IpInfo", ""),

	/**
	 * Presentation mode
	 * 1. Get-PrsMode: 1 param (Get)
	 */
	MODERATOR_MODE_STATUS_GET("DeviceSettings-Moderator", "PrsMode", "Get"),

	/**
	 * Part preset confirm
	 * 1. Get-PartPresentConfirm: 1 param (Get)
	 */
	PART_PRESENT_CONFIRM_GET("DeviceSettings-Moderator", "PartPresentConfirm", "Get"),

	/**
	 * Log mode status
	 * 1. Get-Log: 1 param (Get)
	 */
	ACTIVE_SYSTEM_LOG_GET("DeviceSettings", "Log", "Get"),

	/**
	 * Quick client access status
	 * 1. Get-QuickLaunch: 1 param (Get)
	 */
	QUICK_CLIENT_ACCESS_GET("DeviceSettings", "QuickLaunch", "Get"),

	/**
	 * Gateway: serial number, mac address, version
	 */
	SERIAL_NUMBER_GET("", "GetSerialNo", ""),
	MAC_ADDRESS_GET("", "GetMacAdd", ""),
	VERSION_GET("", "GetVersion", ""),

	/**
	 * Chrome connectivity status and api mode status
	 * 1. Get-Chrome : 1 param (Get)
	 * 2. Get-APIMode: 1 param (Get)
	 */
	CHROME_JOIN_THROUGH_BROWSER_GET("DeviceSettings", "Chrome", "Get"),
	CHROME_API_MODE_GET("DeviceSettings", "APIMode", "Get"),

	/**
	 * Room overlay status
	 * 1. Get-RoomOverlay: 1 param (Get)
	 * 2. Set-RoomOverlay: 3 param (Set,  0/1 (off/on) room overlay, 0/1 (off/on) auto hide time)
	 */
	ROOM_OVERLAY_STATUS_GET("DeviceSettings-RoomOverlay", "RoomOverlay", "Get"),

	/**
	 * Audio devices
	 */
	AUDIO_DEVICES_GET("DeviceSettings", "GetAudioDevice", ""), // Number of devices ?

	/**
	 * Streaming status
	 * 1. Get-Streaming: 1 param (Get)
	 * 2. Get-Streaming-SStatus: 1 param (SStatus)
	 */
	STREAMING_STATUS_GET("StreamingFromDeviceToExternal", "Streaming", "Get"),
	STREAMING_STATUS_SSTATUS_GET("StreamingFromDeviceToExternal", "Streaming", "SStatus"),

	/**
	 * Wi-Fi Guest Mode
	 * 1. Get-Status: 1 param (Status)
	 * 3. Set-WifiGuestMode-Status: 1 param Status
	 */
	WIFI_GUEST_MODE("DeviceSettings", "WifiGuestMode", "Status"),
	WIFI_GUEST_MODE_STATUS("DeviceSettings", "WifiGuestMode", "Status"),

	ROOM_CODE("","RCode","Get,Code");

	/**
	 * VIAConnectProMonitoringMetric with arguments constructor
	 *
	 * @param groupName Group name of the metric
	 * @param command VIA Connect PRO Command
	 * @param param parameter of the command.
	 */
	VIAConnectProMonitoringMetric(String groupName, String command, String param) {
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
}