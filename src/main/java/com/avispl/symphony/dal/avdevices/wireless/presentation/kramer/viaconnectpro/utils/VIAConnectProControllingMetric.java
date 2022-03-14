/*
 * Copyright (c) 2022 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.avdevices.wireless.presentation.kramer.viaconnectpro.utils;

/**
 * VIAConnectProControllingMetric enum class
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 3/14/2022
 * @since 1.0.0
 */
public enum VIAConnectProControllingMetric {

	/**
	 * Require 2 params: Set and username
	 */
	DISPLAY_STATUS_SET("DisplayStatus","DisplayStatus", "Set"),
	VOLUME_SET("Volume", "Vol", "Set"),

	ROOM_CODE_SETTINGS_SET_ACTIVE_STATUS("RoomCodeSettings", "RCode","Set,ActiveStatus"),
	ROOM_CODE_SETTINGS_SET_APPEAR_STATUS("RoomCodeSettings","RCode", "Set,AppearStatus"),
	ROOM_CODE_SETTINGS_SET_REFRESH_TIME("RoomCodeSettings","RCode", "Set,RTime"),

	/**
	 * Room name settings:
	 * 1. Set status: require 3 params Set,Status, {0/1} (off/on)
	 * 2. Set name: require 3 params Set, Name, {new name}
	 */
	ROOM_NAME_SETTINGS_SET_STATUS("RoomNameSettings","RName","Set,Status,1"),
	ROOM_NAME_SETTINGS_SET_NAME("RoomNameSettings","RName","Set,Name"),

	/**
	 * Datetime status:
	 * 	Set status: require 2 params SetVisible, {0/1} (off/on)
	 */
	DATE_TIME_STATUS_SET("DateTimeStatus","DateTime","SetVisible,1"),

	/**
	 * Presentation Mode Status:
	 * 	Set status: require 2 params PrsMode, {0/1} (off/on)
	 */
	PRESENTATION_MODE_STATUS_SET("PresentationModeStatus","PrsMode","Set,1"),

	/**
	 * Log Mode Status:
	 * 	Set status: require 2 params Log, {0/1} (off/on)
	 */
	LOG_MODE_STATUS_SET("LogModeStatus","Log","Set,1"),

	/**
	 * Quick Client Access Mode Status:
	 * 	Set status: require 2 params QuickLaunch, {0/1} (off/on)
	 */
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
	 * Set chrome status:
	 * 	Set status: require 2 params Set and 0/1 (off/on)
	 */
	CHROME_SET_STATUS("Chrome","Chrome", "Set" ),
	/**
	 * Set API mode:
	 * 	Set status: require 2 params Set and 0/1 (off/on)
	 */
	CHROME_SET_API_MODE("Chrome","APIMode", "Set" ),

	/**
	 * Set room overlay
	 * - 3 params:Set,  0/1 (off/on) room overlay, 0/1 (off/on) auto hide time
	 * Set Auto Hide Time
	 * 	TODO Miss auto hide time
	 */
	ROOM_OVERLAY_STATUS_SET("RoomOverlay","RoomOverlay","Set"),

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
	STREAMING("Streaming","Streaming","Set"),

	/**
	 * Streaming URL: require 2 params 0/1 (off/on), URL path
	 */
	STREAMING_URL("StreamingURL","StreamingURL",""),

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
	 * Part preset confirm: 2 params Set, 0/1 (off/on)
	 */
	PART_PRESET_CONFIRM_SET("PartPresentConfirm","PartPresentConfirm","Set"),

	/**
	 * WifiGuestMode:
	 * 	- Start/Stop: 0/1
	 * 	- Status: 1 param Status
	 */
	WIFI_GUEST_MODE_START("WifiGuestMode","WifiGuestMode","1"),
	WIFI_GUEST_MODE_STOP("WifiGuestMode","WifiGuestMode","0"),
	WIFI_GUEST_MODE_STATUS("WifiGuestMode","WifiGuestMode","Status");
	;

	/**
	 * VIAConnectProControllingMetric with arguments constructor
	 *
	 * @param name Name of the metric
	 * @param command VIA Connect PRO Command
	 * @param param parameter of the command.
	 */
	VIAConnectProControllingMetric(String name, String command, String param) {
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

	/**
	 * Get name of metric from VIAConnectProControllingMetric
	 *
	 * @param name name of metric
	 * @return Enum of VIAConnectProControllingMetric
	 */
	public static VIAConnectProControllingMetric getByName(String name) {
		for (VIAConnectProControllingMetric metric: VIAConnectProControllingMetric.values()) {
			if (metric.getName().equals(name)) {
				return metric;
			}
		}
		throw new IllegalArgumentException("Cannot find the enum with name: " + name);
	}
}
