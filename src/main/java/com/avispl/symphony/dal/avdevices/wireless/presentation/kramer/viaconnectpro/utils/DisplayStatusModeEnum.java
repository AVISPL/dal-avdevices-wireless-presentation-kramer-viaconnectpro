/*
 * Copyright (c) 2022 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.avdevices.wireless.presentation.kramer.viaconnectpro.utils;

/**
 * DisplayStatusModeEnum enum class
 *
 * @author Kevin / Symphony Dev Team <br>
 * Created on 4/4/2022
 * @since 1.0.0
 */
public enum DisplayStatusModeEnum {

	START("Start", "1"),
	STOP("Stop", "0"),
	DENY("Deny", "2");

	/**
	 * DisplayStatusModeEnum with arguments constructor
	 *
	 * @param name Name of the display status
	 * @param code Code of the display status
	 */
	DisplayStatusModeEnum(String name, String code) {
		this.name = name;
		this.code = code;
	}

	private final String name;
	private final String code;

	/**
	 * Retrieves {@code {@link #name}}
	 *
	 * @return value of {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Retrieves {@code {@link #code}}
	 *
	 * @return value of {@link #code}
	 */
	public String getCode() {
		return code;
	}
}
