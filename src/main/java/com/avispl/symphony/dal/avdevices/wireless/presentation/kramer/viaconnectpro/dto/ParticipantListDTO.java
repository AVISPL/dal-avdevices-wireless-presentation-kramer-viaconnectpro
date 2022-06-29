/*
 * Copyright (c) 2022 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.avdevices.wireless.presentation.kramer.viaconnectpro.dto;

import java.util.Map;

/**
 * ParticipantDTO
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 3/14/2022
 * @since 1.0.0
 */
public class ParticipantListDTO {

	private int loggedInUsers;
	private Map<String, String> userAndStatusMap;

	/**
	 * Retrieves {@code {@link #loggedInUsers}}
	 *
	 * @return value of {@link #loggedInUsers}
	 */
	public int getLoggedInUsers() {
		return loggedInUsers;
	}

	/**
	 * Sets {@code loggedInUsers}
	 *
	 * @param loggedInUsers the {@code int} field
	 */
	public void setLoggedInUsers(int loggedInUsers) {
		this.loggedInUsers = loggedInUsers;
	}

	/**
	 * Retrieves {@code {@link #userAndStatusMap}}
	 *
	 * @return value of {@link #userAndStatusMap}
	 */
	public Map<String, String> getUserAndStatusMap() {
		return userAndStatusMap;
	}

	/**
	 * Sets {@code userAndStatusMap}
	 *
	 * @param userAndStatusMap the {@code java.util.Map<java.lang.String,java.lang.String>} field
	 */
	public void setUserAndStatusMap(Map<String, String> userAndStatusMap) {
		this.userAndStatusMap = userAndStatusMap;
	}
}
