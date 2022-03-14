/*
 * Copyright (c) 2022 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.avdevices.wireless.presentation.kramer.viaconnectpro.utils;

/**
 * VIAConnectProErrorMetrics enum class
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 3/14/2022
 * @since 1.0.0
 */
public enum VIAConnectProErrorMetrics {

	ERROR_01("Error01","Invalid value passed in command to set volume."),
	ERROR_02("Error02", "Blank value passed to set volume."),

	ERROR_11("Error11", "XML does not contain User Name. Username blank"),
	ERROR_12("Error12","User is not authorized."),
	ERROR_13("Error13","Incorrect User name and password."),
	ERROR_14("Error14", "No such user exists."),
	ERROR_16("Error16","Whiteboard is not open"),
	ERROR_17("Error17", "Switch was used to set to an already active mode."),
	ERROR_21("Error21","Room code is not enabled."),

	ERROR_22("Error22","Unable to set active status."),
	ERROR_23("Error23", "Room code active status already On."),
	ERROR_24("Error24","Unable to set active status."),
	ERROR_25("Error25", "Unable to set active status."),
	ERROR_26("Error26","Room code active status already Off."),

	ERROR_27("Error27","Room code is not active."),
	ERROR_28("Error28", "Unable to set appear status."),
	ERROR_29("Error29","Room code appears status already On."),
	ERROR_30("Error30", "Unable to set appear status."),
	ERROR_31("Error31","Room code appear status already Off."),

	ERROR_32("Error32","Unable to set refresh time"),
	ERROR_33("Error33", "Wrong refresh time value"),
	ERROR_34("Error34","Blank refresh time value"),

	ERROR_41("Error41","Unable to get room name value."),
	ERROR_42("Error42","Unable to get room name value."),
	ERROR_43("Error43","Unable to set room name status."),
	ERROR_44("Error44","Room name status already on."),
	ERROR_45("Error45", "Unable to set room name status."),
	ERROR_46("Error46","Unable to set room name status."),
	ERROR_47("Error47","Room name is not active."),
	ERROR_48("Error48", "Unable to set room name value."),
	ERROR_49("Error49","Blank room name value."),

	ERROR_56("Error56","Unable to set log mode."),
	ERROR_57("Error57","Log mode already on."),
	ERROR_58("Error58", "Unable to set log mode."),
	ERROR_59("Error59","Log mode already off"),

	ERROR_61("Error61","Unable to set Presentation mode."),
	ERROR_62("Error62","Presentation mode already on."),
	ERROR_63("Error63", "Unable to set Presentation mode."),
	ERROR_64("Error64","Presentation mode already off."),

	ERROR_71("Error71","Unable to set Date & Time visibility."),
	ERROR_72("Error72","Date & Time visibility already on."),
	ERROR_73("Error73", "Unable to set Date & Time visibility."),
	ERROR_74("Error74","Date & Time visibility already off."),

	ERROR_76("Error76","Unable to set Quick Launch mode."),
	ERROR_77("Error77","Quick Launch mode already on."),
	ERROR_78("Error78", "Unable to set Quick Launch mode."),
	ERROR_79("Error79","Quick Launch mode already off."),

	ERROR_111("Error111","Control permission has already been revoked."),
	ERROR_112("Error112","Control permission has already been granted"),
	ERROR_113("Error113", "Participant on iPad or a Tablet device"),
	ERROR_114("Error114","Participant not in display (not stepped-in)"),

	ERROR_123("Error123","System is already in DND mode"),
	ERROR_124("Error124", "User count greater than one. User not presenting"),
	ERROR_125("Error125","System is already in non-DND mode"),
	ERROR_126("Error126", "User count greater than one. User not in presenting state. "),
	ERROR_127("Error127","DND mode is not enabled"),
	USR_NOT_EXIST("UsrNotExist", "User does not exist"),

	ERROR_701("Error701", "Serial number could not be retrieved"),
	ERROR_702("Error702", "MAC address could not be retrieved"),
	ERROR_703("Error703", "Version number could not be retrieved"),
	ERROR_704("Error704", "System audio device name list is empty."),
	ERROR_1001("Error1001","Wrong P1 Value"),
	ERROR_1002("Error1002","Wrong P2 Value"),
	ERROR_1003("Error1003","Empty P2 Value"),
	ERROR_1007("Error1007","P2 is 1 and P3 is null"),
	ERROR_1008("Error1008","P2 is 0 and P3 is not null"),
	ERROR_1008_PART_PRESENT_CONFIRM("Error1008","Presentation mode is not set"),

	ERROR_20051("Error20051", "Wrong or blank P1 Value"),
	ERROR_20052("Error20052", "Blank P2 Value"),
	ERROR_20053("Error20053", "Recording is started"),
	ERROR_20054("Error20054", "Streaming is not activated"),
	ERROR_20055("Error20055","Blank or Wrong URLs"),
	ERROR_20056("Error20056", "Wrong or blank P1 Value"),
	ERROR_20057("Error20057","Guest mode is not activated"),

	ERROR_20058("Error20058","Streaming is started"),
	ERROR_20059("Error20059","Empty P2 value"),
	ERROR_20060("Error20060","Empty P3 value when P2 is 1."),
	ERROR_20061("Error20061","Wrong P2 value"),
	ERROR_20062("Error20062","Path does not begin with udp or tcp (Single display)"),
	ERROR_20063("Error20063","Empty P3 or P4 values when dual display is set."),
	ERROR_20065("Error20065","Path Length incorrect"),
	ERROR_20066("Error20066","Path does not begin with udp or tcp (Dual display)"),
//Error20051 Wrong or blank P1 Value
//Error20052 Blank P2 Value
//Error20053 Recording is started
//Error20054 Streaming is not activated
//Error20055 Blank or Wrong URLs
//Error20058 Streaming is started
//Error20059 Empty P2 value
//Error20060 Empty P3 value when P2 is 1.
//Error20061 Wrong P2 value
//Error20062 Path does not begin with udp or tcp (Single display)
//Error20063 Empty P3 or P4 values when dual display is set.
//Error20065 Path Length incorrect
//Error20066 Path does not begin with udp or tcp (Dual display)

	;

	VIAConnectProErrorMetrics(String errorCode, String errorDescription) {
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
}
