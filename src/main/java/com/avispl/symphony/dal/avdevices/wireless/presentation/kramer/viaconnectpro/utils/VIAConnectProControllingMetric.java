/*
 * Copyright (c) 2022 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.avdevices.wireless.presentation.kramer.viaconnectpro.utils;

/**
 * VIAConnectProControllingMetric enum class
 *
 * @author Kevin / Symphony Dev Team <br>
 * Created on 3/14/2022
 * @since 1.0.0
 */
public enum VIAConnectProControllingMetric {

	/**
	 * Login command
	 */
	LOGIN("", "Login", ""),

	/**
	 * Display status: Set-require 2 params (Set and username)
	 */
	DISPLAY_STATUS_SET("UserModeration", "DisplayStatus", "Set"),

	/**
	 * Volume: Set-require 2 params (Vol and Set)
	 */
	VOLUME_SET("DeviceSettings", "Vol", "Set"),

	STREAMING_STATUS_SET("StreamingFromDeviceToExternal", "Streaming", "Set"),

	/**
	 * Start streaming
	 * Set status: require 2 params SStart and username
	 * Stop streaming
	 * Set status: require 2 params SStop and username
	 * Restart streaming:
	 * require 3 params: SRestart, username, URL name
	 * Change streaming:
	 * require 3 params: SChange, username, URL name
	 * Streaming:
	 * require 2 params: Set, 0/1 (off/on)
	 */
	STREAMING_START("StreamingFromDeviceToExternal", "Streaming", "SStart"),
	STREAMING_STOP("StreamingFromDeviceToExternal", "Streaming", "SStop"),
	STREAMING_RESTART("StreamingFromDeviceToExternal", "Streaming", "SRestart"),
	STREAMING_CHANGE("StreamingFromDeviceToExternal", "Streaming", "SChange"),

	/**
	 * Streaming URL: require 2 params 1,  URL path
	 */
	STREAMING_URL("StreamingFromExternalToDevice", "StreamingURL", "1");

	/**
	 * VIAConnectProControllingMetric with arguments constructor
	 *
	 * @param groupName Group name of the metric
	 * @param command VIA Connect PRO Command
	 * @param param parameter of the command.
	 */
	VIAConnectProControllingMetric(String groupName, String command, String param) {
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
