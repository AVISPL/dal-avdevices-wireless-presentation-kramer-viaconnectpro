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
	 *
	 */
	PLIST_CNT("ParticipantList","PList", "cnt,3"),
	PLIST_All_STATUS("ParticipantList","PList", "all,4"),

	/**
	 * Display status, require 2 params (Get and Username)
	 */
	DISPLAY_STATUS_GET("DisplayStatus","DisplayStatus","Get"),

	/**
	 * Volume
	 */
	VOLUME("Volume","Vol", "Get"),

	/**
	 * Ip information
	 */
	IP_INFORMATION("IpInformation", "IpInfo",""),

	/**
	 * Room code settings
	 */
	ROOM_CODE_SETTINGS_GET_ACTIVE_STATUS("RoomCodeSettings","RCode","Get,ActiveStatus"),
	ROOM_CODE_SETTINGS_GET_APPEAR_STATUS("RoomCodeSettings","RCode","Get,AppearStatus"),
	ROOM_CODE_SETTINGS_GET_CODE("RoomCodeSettings","RCode","Get,Code"),
	ROOM_CODE_SETTINGS_GET_RTIME("RoomCodeSettings","RCode","Get,RTime"),

	/**
	 * Room name settings
	 */
	ROOM_NAME_SETTINGS_GET("RoomNameSettings","RName","Get,Status" ),

	/**
	 * Datetime display status
	 */
	DATETIME_DISPLAY_STATUS_GET("DatetimeDisplayStatus","DateTime","GetVisible"),

	/**
	 * Presentation mode
	 */
	PRESENTATION_MODE_STATUS_GET("PresentationModeStatus","PrsMode","Get"),

	/**
	 * Log mode status
	 */
	LOG_MODE_STATUS("LogModeStatus","Log","Get"),

	/**
	 * Quick client access status
	 */
	QUICK_CLIENT_ACCESS_STATUS_GET("QuickClientAccessStatus","QuickLaunch","Get"),

	/**
	 * Gateway: serial number, mac address, version
	 */
	GATEWAY_SERIAL_NUMBER_GET("GatewaySerialNumber","GetSerialNo",""),
	GATEWAY_MAC_ADDRESS_GET("GatewayMacAddress","GetMacAdd",""),
	GATEWAY_VERSION_GET("GatewayVersion","GetVersion",""),

	/**
	 * Chrome connectivity status and api mode status
	 */
	CHROME_STATUS_GET("Chrome","Chrome","Get"),
	CHROME_STATUS_API_MODE_GET("Chrome","APIMode","Get"),

	/**
	 * Room overlay status
	 */
	ROOM_OVERLAY_STATUS_GET("RoomOverlay","RoomOverlay","Get"),

	/**
	 * Audio devices
	 */
	AUDIO_DEVICES_GET("AudioDevices","GetAudioDevice","5"), // Number of devices ?

	/**
	 * Streaming status
	 */
	STREAMING_STATUS_GET("Streaming","Streaming","Get"),
	STREAMING_STATUS_SSTATUS_GET("Streaming","Streaming","SStatus"),

	/**
	 * Part preset confirm
	 */
	PART_PRESET_CONFIRM_GET("PartPresentConfirmStatus","PartPresentConfirm", "Get");

	/**
	 * VIAConnectProControllingMetric with arguments constructor
	 *
	 * @param name Name of the metric
	 * @param command VIA Connect PRO Command
	 * @param param parameter of the command.
	 */
	VIAConnectProMonitoringMetric(String name, String command, String param) {
		this.name = name;
		this.command = command;
		this.param = param;
	}

	private String name;
	private String command;
	private String param;

	/**
	 * Retrieves {@code {@link #name}}
	 *
	 * @return value of {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets {@code name}
	 *
	 * @param name the {@code java.lang.String} field
	 */
	public void setName(String name) {
		this.name = name;
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
	 * Sets {@code command}
	 *
	 * @param command the {@code java.lang.String} field
	 */
	public void setCommand(String command) {
		this.command = command;
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
	 * Sets {@code param}
	 *
	 * @param param the {@code java.lang.String} field
	 */
	public void setParam(String param) {
		this.param = param;
	}
}
