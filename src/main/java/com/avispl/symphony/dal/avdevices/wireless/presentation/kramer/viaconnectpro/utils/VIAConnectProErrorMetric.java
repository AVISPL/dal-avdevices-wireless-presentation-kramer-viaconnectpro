/*
 * Copyright (c) 2022 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.avdevices.wireless.presentation.kramer.viaconnectpro.utils;

/**
 * VIAConnectProErrorMetrics enum class
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 4/4/2022
 * @since 1.0.0
 */
public enum VIAConnectProErrorMetric {
	ERROR_14("Error14", "No such user exists"),
	ERROR_21("Error21","Room code is not enabled."),

	ERROR_701("Error701", "Serial number could not be retrieved"),
	ERROR_702("Error702", "MAC address could not be retrieved"),
	ERROR_703("Error703", "Version number could not be retrieved"),
	ERROR_704("Error704", "System audio device name list is empty."),

	/**
	 * Wifi guest mode error response:
	 */
	ERROR_20057("Error20057","Guest mode is not activated"),

	/**
	 * Streaming error responses:
	 */
	ERROR_20053("Error20062","Path does not begin with udp or tcp (Single display)"),
	ERROR_20054("Error20063","Empty P3 or P4 values when dual display is set."),
	ERROR_20055("Error20065","Path Length incorrect"),
	ERROR_20058("Error20066","Path does not begin with udp or tcp (Dual display)"),
	ERROR_20059("Error20062","Path does not begin with udp or tcp (Single display)"),
	ERROR_20060("Error20063","Empty P3 or P4 values when dual display is set."),
	ERROR_20061("Error20065","Path Length incorrect"),
	ERROR_20062("Error20066","Path does not begin with udp or tcp (Dual display)"),
	ERROR_20063("Error20066","Path does not begin with udp or tcp (Dual display)"),
	ERROR_20065("Error20062","Path does not begin with udp or tcp (Single display)"),
	ERROR_20066("Error20063","Empty P3 or P4 values when dual display is set.");

	VIAConnectProErrorMetric(String errorCode, String errorDescription) {
		this.errorCode = errorCode;
		this.errorDescription = errorDescription;
	}

	private String errorCode;
	private String errorDescription;

	/**
	 * Retrieves {@code {@link #errorCode}}
	 *
	 * @return value of {@link #errorCode}
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Sets {@code errorCode}
	 *
	 * @param errorCode the {@code java.lang.String} field
	 */
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * Retrieves {@code {@link #errorDescription}}
	 *
	 * @return value of {@link #errorDescription}
	 */
	public String getErrorDescription() {
		return errorDescription;
	}

	/**
	 * Sets {@code errorDescription}
	 *
	 * @param errorDescription the {@code java.lang.String} field
	 */
	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}

	/**
	 * Get name of metric from VIAConnectProErrorMetric
	 *
	 * @param code error code of metric
	 * @return Enum of VIAConnectProErrorMetric
	 */
	public static VIAConnectProErrorMetric getByCode(String code) {
		for (VIAConnectProErrorMetric metric: VIAConnectProErrorMetric.values()) {
			if (metric.getErrorCode().equals(code)) {
				return metric;
			}
		}
		return null;
	}
}
