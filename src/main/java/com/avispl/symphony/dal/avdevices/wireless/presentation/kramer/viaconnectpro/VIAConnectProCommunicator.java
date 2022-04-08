/*
 * Copyright (c) 2022 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.avdevices.wireless.presentation.kramer.viaconnectpro;

import com.avispl.symphony.api.dal.control.Controller;
import com.avispl.symphony.api.dal.dto.control.AdvancedControllableProperty;
import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;
import com.avispl.symphony.api.dal.dto.monitor.Statistics;
import com.avispl.symphony.api.dal.error.CommandFailureException;
import com.avispl.symphony.api.dal.error.ResourceNotReachableException;
import com.avispl.symphony.api.dal.monitor.Monitorable;
import com.avispl.symphony.dal.avdevices.wireless.presentation.kramer.viaconnectpro.dto.ParticipantListDTO;
import com.avispl.symphony.dal.avdevices.wireless.presentation.kramer.viaconnectpro.utils.DisplayStatusModeEnum;
import com.avispl.symphony.dal.avdevices.wireless.presentation.kramer.viaconnectpro.utils.VIAConnectProConstant;
import com.avispl.symphony.dal.avdevices.wireless.presentation.kramer.viaconnectpro.utils.VIAConnectProControllingMetric;
import com.avispl.symphony.dal.avdevices.wireless.presentation.kramer.viaconnectpro.utils.VIAConnectProErrorMetric;
import com.avispl.symphony.dal.avdevices.wireless.presentation.kramer.viaconnectpro.utils.VIAConnectProMonitoringMetric;
import com.avispl.symphony.dal.communicator.TelnetCommunicator;
import com.avispl.symphony.dal.util.StringUtils;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;

/**
 * VIA Connect Pro Adapter
 * <p>
 * Monitoring:
 * <ol>
 * 	<li>Participant List</li>
 * 	<li>Display Status</li>
 * 	<li>Volume</li>
 * 	<li>IP Information</li>
 * 	<li>Presentation Mode Status</li>
 * 	<li>Log Mod Status</li>
 * 	<li>Quick Client Access Status</li>
 * 	<li>Gateway Serial Number</li>
 * 	<li>Gateway Mac Address</li>
 * 	<li>Gateway Version</li>
 * 	<li>Chrome Status</li>
 * 	<li>Room Overlay Status</li>
 * 	<li>Streaming</li>
 * 	<li>Part Present Confirm</li>
 * </ol>
 * <p>
 * Controlling:
 * <ol>
 * 	<li>Set Volume</li>
 * 	<li>Kick off user</li>
 * 	<li>Start/Stop UserPresentation</li>
 * 	<li>Streaming(start/stop/restart/change)</li>
 * 	<li>StreamingURL: open network stream</li>
 * 	<li>Wifi Guest Mode</li>.
 * 	</ol>
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 3/14/2022
 * @since 1.0.0
 */
public class VIAConnectProCommunicator extends TelnetCommunicator implements Monitorable, Controller {

	/**
	 * Store previous/current ExtendedStatistics
	 */
	private ExtendedStatistics localExtendedStatistics;

	/**
	 * cachedLocalExtendedStatistics.This variable will store cached stats/controls and will be updated in {@link VIAConnectProCommunicator#controlProperty(ControllableProperty)}
	 * And populate its values in {@link VIAConnectProCommunicator#getMultipleStatistics()}
	 */
	private final ExtendedStatistics cachedLocalExtendedStatistics;

	/**
	 * Check if {@link VIAConnectProCommunicator#controlProperty(ControllableProperty)} have just finished
	 * And {@link VIAConnectProCommunicator#getMultipleStatistics()} is about to be executed.
	 */
	private boolean isCachedControlling;

	/**
	 * Check if user is controlling StreamingControl
	 */
	private boolean isStreamingControl = false;

	/**
	 * Store previous username
	 */
	private String previousUserName;

	/**
	 * ReentrantLock to prevent telnet session is closed when adapter is retrieving statistics from the device.
	 */
	private final ReentrantLock reentrantLock = new ReentrantLock();

	/**
	 * Prevent case where {@link VIAConnectProCommunicator#controlProperty(ControllableProperty)} slow down -
	 * the getMultipleStatistics interval if it's fail to send the cmd
	 */
	private static final int controlTelnetTimeout = 3000;

	/**
	 * Set back to default timeout value in {@link TelnetCommunicator}
	 */
	private static final int statisticsTelnetTimeout = 30000;

	/**
	 * Adapter property: role of a user -
	 *  Adapter will base on this role to display/hide statistics for a specific role.
	 */
	private String configManagement;

	/**
	 * Retrieves {@code {@link #configManagement }}
	 *
	 * @return value of {@link #configManagement}
	 */
	public String getConfigManagement() {
		return configManagement;
	}

	/**
	 * Sets {@code configManagement}
	 *
	 * @param configManagement the {@code java.lang.String} field
	 */
	public void setConfigManagement(String configManagement) {
		this.configManagement = configManagement;
	}

	/**
	 * VIAConnectProCommunicator constructor
	 */
	public VIAConnectProCommunicator() {
		this.setLoginPrompt("Username:");
		this.setPasswordPrompt("Password:");
		this.setCommandSuccessList(
				Collections.singletonList(VIAConnectProConstant.END_COMMAND));
		this.setCommandErrorList(Collections.singletonList("A"));
		this.setLoginSuccessList(Collections.singletonList("Login\r\nLogin Successful.\nNow Please send a command:\r\n"));
		// Instantiate cachedLocalExtendedStatistics
		cachedLocalExtendedStatistics = new ExtendedStatistics();
		cachedLocalExtendedStatistics.setStatistics(new HashMap<>());
		cachedLocalExtendedStatistics.setControllableProperties(new ArrayList<>());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void internalInit() throws Exception {
		super.internalInit();
		if (logger.isDebugEnabled()) {
			logger.debug("VIAConnectProCommunicator-internalInit(): Creating telnet session");
		}
		// Check if device is reachable, then close the telnet session
		this.createChannel();
		if (logger.isDebugEnabled()) {
			logger.debug("VIAConnectProCommunicator-internalInit(): Closing telnet session");
		}
		this.destroyChannel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void internalDestroy() {
		cachedLocalExtendedStatistics.getStatistics().clear();
		cachedLocalExtendedStatistics.getControllableProperties().clear();
		this.destroyChannel();
		super.internalDestroy();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void controlProperty(ControllableProperty controllableProperty) throws Exception {
		String property = controllableProperty.getProperty();
		String propertyValue = String.valueOf(controllableProperty.getValue());
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Perform control operation with property: %s and value: %s", property, propertyValue));
		}

		reentrantLock.lock();
		try {
			this.timeout = controlTelnetTimeout;
			if(!isLogin()){
				return;
			}
			String groupName = property.substring(0, property.indexOf(VIAConnectProConstant.HASH));
			String propertyName = property.substring(property.indexOf(VIAConnectProConstant.HASH) + 1);
			switch (groupName) {
				case VIAConnectProConstant.DEVICE_SETTINGS:
					deviceSettingsControl(propertyValue, propertyName);
					break;
				case VIAConnectProConstant.USER_MODERATION:
					userModerationControl(propertyValue, propertyName);
					break;
				case VIAConnectProConstant.STREAMING_FROM_EXTERNAL_TO_DEVICE:
						cachedControlProperties(VIAConnectProControllingMetric.STREAMING_URL, propertyName, propertyValue, property, groupName);
					break;
				case VIAConnectProConstant.STREAMING_FROM_DEVICE_TO_EXTERNAL:
					if (propertyName.equals(VIAConnectProConstant.STREAMING_MODE)) {
						normalControlProperties(VIAConnectProControllingMetric.STREAMING_STATUS_SET, propertyName, propertyValue);
					} else if (propertyName.equals(VIAConnectProConstant.APPLY)) {
						normalControlProperties(VIAConnectProControllingMetric.STREAMING_START, propertyName, propertyValue);
					} else {
						cachedControlProperties(VIAConnectProControllingMetric.STREAMING_START, propertyName, propertyValue, property, groupName);
					}
					break;
				default:
					if (logger.isWarnEnabled()) {
						logger.warn(String.format("Operation %s with value %s is not supported.", property, propertyValue));
					}
					throw new IllegalArgumentException(String.format("Operation %s with value %s is not supported.", property, propertyValue));
			}
		} finally {
			this.timeout = statisticsTelnetTimeout;
			reentrantLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void controlProperties(List<ControllableProperty> list) throws Exception {
		if (CollectionUtils.isEmpty(list)) {
			throw new IllegalArgumentException("Controllable properties cannot be null or empty");
		}
		for (ControllableProperty controllableProperty : list) {
			controlProperty(controllableProperty);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void login() throws Exception {
		List<String> param = new ArrayList<>();
		this.write(buildTelnetRequest(VIAConnectProControllingMetric.LOGIN.getCommand(), param, true));
		super.login();
	}

	@Override
	public List<Statistics> getMultipleStatistics() throws Exception {
		if (logger.isDebugEnabled()) {
			if (isCachedControlling) {
				logger.debug("VIAConnectProCommunicator: Perform getMultipleStatistics() after controlProperty()");
			} else {
				logger.debug("VIAConnectProCommunicator: Perform getMultipleStatistics()");
			}
		}
		ExtendedStatistics extendedStatistics = new ExtendedStatistics();
		reentrantLock.lock();
		try {
			// Populate statistics after controlProperty with cached statistics
			if (isCachedControlling && cachedLocalExtendedStatistics != null && localExtendedStatistics != null) {
				Map<String, String> cachedStats = cachedLocalExtendedStatistics.getStatistics();
				List<AdvancedControllableProperty> cachedControls = cachedLocalExtendedStatistics.getControllableProperties();
				Map<String, String> currentStats = localExtendedStatistics.getStatistics();
				List<AdvancedControllableProperty> currentControls = localExtendedStatistics.getControllableProperties();
				if (isStreamingControl) {
					isStreamingControl = false;
					populateCachedStreamingControl(currentStats, currentControls, cachedControls);
				}
				currentStats.putAll(cachedStats);
				populateCachedControlProperties(currentControls, cachedControls);
				isCachedControlling = false;
				return Collections.singletonList(localExtendedStatistics);
			}
			if(!isLogin()){
				throw new RuntimeException("Unable to establish a telnet communication session");
			}
			// Populate new statistics
			Map<String, String> newStats = new HashMap<>();
			List<AdvancedControllableProperty> newControls = new ArrayList<>();
			populateStatistics(newStats, newControls);
			extendedStatistics.setStatistics(newStats);
			extendedStatistics.setControllableProperties(newControls);
			// Populate cached stats and controls to new statistics
			// This cached stats and controls will be removed if internalDestroy() is called -
			// or button is clicked.
			if (cachedLocalExtendedStatistics != null) {
				Map<String, String> cachedStats = cachedLocalExtendedStatistics.getStatistics();
				List<AdvancedControllableProperty> cachedControls = cachedLocalExtendedStatistics.getControllableProperties();
				if (getListParticipant().getUserAndStatusMap().size() == 0) {
					removeCachedStatisticAndControl(cachedStats, cachedControls, VIAConnectProMonitoringMetric.PLIST_CNT.getGroupName());
					removeCachedStatisticAndControl(cachedStats, cachedControls, VIAConnectProControllingMetric.STREAMING_START.getGroupName());
				}
				populateCachedStreamingControl(newStats, newControls, cachedControls);
				newStats.putAll(cachedStats);
				populateCachedControlProperties(newControls, cachedControls);
			}
		} finally {
			try{
				if (logger.isDebugEnabled()) {
					logger.debug("VIAConnectProCommunicator: Closing session");
				}
				this.destroyChannel();
			}
			finally {
				reentrantLock.unlock();
			}
		}
		localExtendedStatistics = extendedStatistics;
		return Collections.singletonList(localExtendedStatistics);
	}

	/**
	 * Populate statistics(get from the device or default statistics to control) and controlling properties
	 *
	 * @param statistics Map of statistics
	 * @param controls List of AdvancedControllableProperty
	 */
	private void populateStatistics(Map<String, String> statistics, List<AdvancedControllableProperty> controls) {
		populateNonGroupProperties(statistics);
		populateDeviceSettingsGroup(statistics, controls);
		populateDeviceSettingsModeratorGroup(statistics);
		populateDeviceSettingsRoomOverlayGroup(statistics);
		populateParticipantGroup(statistics, controls);
		populateUserModeration(statistics, controls);
		populateStreamingFromExternalToDevice(statistics, controls);
		populateStreamingFromDeviceToExternal(statistics, controls);
	}

	/**
	 * Get list of participant (number of logged-in user, username-status)
	 * This command might be used multiple time to make sure the list of usernames is always up-to-date
	 *
	 * @return ParticipantListDTO DTO of participant list
	 */
	private ParticipantListDTO getListParticipant() {
		ParticipantListDTO participantListDTO = new ParticipantListDTO();
		List<String> param2 = Arrays.asList(VIAConnectProMonitoringMetric.PLIST_All_STATUS.getParam().split(VIAConnectProConstant.COMMA));
		String rawUserNames = sendTelnetCommand(VIAConnectProMonitoringMetric.PLIST_All_STATUS.getCommand(), param2, false);
		String[] rawUsernameAndStatus = rawUserNames.split(VIAConnectProConstant.REGEX_VERTICAL_LINE);
		String rawResponse = rawUsernameAndStatus[rawUsernameAndStatus.length - 1];
		if (rawResponse.equals(VIAConnectProErrorMetric.ERROR_14.getErrorCode())) {
			participantListDTO.setLoggedInUsers(0);
			participantListDTO.setUserAndStatusMap(new HashMap<>());
			return participantListDTO;
		}
		String[] usernamesAndStatus = rawResponse.split(VIAConnectProConstant.HASH);
		int numberOfUsers = usernamesAndStatus.length;
		participantListDTO.setLoggedInUsers(numberOfUsers);
		if (numberOfUsers == 0) {
			participantListDTO.setUserAndStatusMap(new HashMap<>());
			return participantListDTO;
		}
		Map<String, String> userNameAndStatusMap = new HashMap<>();
		for (String usernameAndStatus : usernamesAndStatus) {
			if (usernameAndStatus.contains(VIAConnectProConstant.UNDER_SCORE)) {
				String username = usernameAndStatus.split(VIAConnectProConstant.UNDER_SCORE)[0];
				String status = usernameAndStatus.split(VIAConnectProConstant.UNDER_SCORE)[1];
				userNameAndStatusMap.put(username, status);
			}
		}

		participantListDTO.setUserAndStatusMap(userNameAndStatusMap);
		return participantListDTO;
	}

	/**
	 * Populate Statistics for properties not in any group
	 *
	 * @param statistics Map of statistics
	 */
	private void populateNonGroupProperties(Map<String, String> statistics) {
		// IP Information
		String rawIpInformation = sendTelnetCommand(VIAConnectProMonitoringMetric.IP_INFORMATION.getCommand(), Collections.singletonList(VIAConnectProMonitoringMetric.IP_INFORMATION.getParam()), false);
		String[] ipInformation = rawIpInformation.split(VIAConnectProConstant.REGEX_VERTICAL_LINE);
		statistics.put(VIAConnectProConstant.IP_ADDRESS, ipInformation[0].split(VIAConnectProConstant.COLON)[1]);
		statistics.put(VIAConnectProConstant.SUBNET_MASK, ipInformation[1].split(VIAConnectProConstant.COLON)[1]);
		statistics.put(VIAConnectProConstant.GATEWAY, ipInformation[2].split(VIAConnectProConstant.COLON)[1]);
		statistics.put(VIAConnectProConstant.DNS_SERVER, ipInformation[3].split(VIAConnectProConstant.COLON)[1]);
		statistics.put(VIAConnectProConstant.HOST_NAME, ipInformation[4].split(VIAConnectProConstant.COLON)[1]);
		// Room code
		String rawRoomCode = sendTelnetCommand(VIAConnectProMonitoringMetric.ROOM_CODE.getCommand(), Arrays.asList(VIAConnectProMonitoringMetric.ROOM_CODE.getParam().split(VIAConnectProConstant.COMMA)), false);
		if (rawRoomCode.contains(VIAConnectProErrorMetric.ERROR_21.getErrorCode())) {
			statistics.put(VIAConnectProConstant.ROOM_CODE, VIAConnectProConstant.NONE);
			logger.error(String.format("Populate failed - Response error code: %s, error description: %s", VIAConnectProErrorMetric.ERROR_21.getErrorCode(), VIAConnectProErrorMetric.ERROR_21.getErrorDescription()));
		} else {
			String roomCode = rawResponseHandling(rawRoomCode);
			statistics.put(VIAConnectProConstant.ROOM_CODE, roomCode);
		}

		// Version
		List<String> param = Collections.singletonList(VIAConnectProMonitoringMetric.VERSION_GET.getParam());
		String rawGatewayVersion = sendTelnetCommand(VIAConnectProMonitoringMetric.VERSION_GET.getCommand(), param, false);
		if (rawGatewayVersion.contains(VIAConnectProErrorMetric.ERROR_703.getErrorCode())) {
			statistics.put(VIAConnectProConstant.VERSION, VIAConnectProConstant.NONE);
			logger.error(String.format("Populate failed - Response error code: %s, error description: %s", VIAConnectProErrorMetric.ERROR_703.getErrorCode(), VIAConnectProErrorMetric.ERROR_703.getErrorDescription()));
		} else {
			String gatewayVersion = rawResponseHandling(rawGatewayVersion);
			statistics.put(VIAConnectProConstant.VERSION, gatewayVersion);
		}
		// MacAddress
		param = Collections.singletonList(VIAConnectProMonitoringMetric.MAC_ADDRESS_GET.getParam());
		String rawGatewayMacAddress = sendTelnetCommand(VIAConnectProMonitoringMetric.MAC_ADDRESS_GET.getCommand(), param, false);
		if (rawGatewayMacAddress.contains(VIAConnectProErrorMetric.ERROR_702.getErrorCode())) {
			statistics.put(VIAConnectProConstant.MAC_ADDRESS, VIAConnectProConstant.NONE);
			logger.error(String.format("Populate failed - Response error code: %s, error description: %s", VIAConnectProErrorMetric.ERROR_702.getErrorCode(), VIAConnectProErrorMetric.ERROR_702.getErrorDescription()));
		} else {
			String gatewayMacAddress = rawResponseHandling(rawGatewayMacAddress);
			statistics.put(VIAConnectProConstant.MAC_ADDRESS, gatewayMacAddress);
		}
		// Serial number
		param = Collections.singletonList(VIAConnectProMonitoringMetric.SERIAL_NUMBER_GET.getParam());
		String rawGatewaySerialNumber = sendTelnetCommand(VIAConnectProMonitoringMetric.SERIAL_NUMBER_GET.getCommand(), param, false);
		if (rawGatewaySerialNumber.contains(VIAConnectProErrorMetric.ERROR_701.getErrorCode())) {
			statistics.put(VIAConnectProConstant.SERIAL_NUMBER, VIAConnectProConstant.NONE);
			logger.error(String.format("Populate failed - Response error code: %s, error description: %s", VIAConnectProErrorMetric.ERROR_701.getErrorCode(), VIAConnectProErrorMetric.ERROR_701.getErrorDescription()));
		} else {
			String gatewaySerialNumber = rawResponseHandling(rawGatewaySerialNumber);
			statistics.put(VIAConnectProConstant.SERIAL_NUMBER, gatewaySerialNumber);
		}
	}

	/**
	 * Populate statistics and controls for DeviceSettings group
	 *
	 * @param statistics Map of statistics
	 * @param controls List of AdvancedControllableProperty
	 */
	private void populateDeviceSettingsGroup(Map<String, String> statistics, List<AdvancedControllableProperty> controls) {
		if (!isConfigManagement()) {
			return;
		}
		// Activate system log
		String groupName = VIAConnectProMonitoringMetric.ACTIVE_SYSTEM_LOG_GET.getGroupName();
		List<String> param = Collections.singletonList(VIAConnectProMonitoringMetric.ACTIVE_SYSTEM_LOG_GET.getParam());
		String rawLogModeStatus = sendTelnetCommand(VIAConnectProMonitoringMetric.ACTIVE_SYSTEM_LOG_GET.getCommand(), param, false);
		String logModeStatus = rawResponseHandling(rawLogModeStatus);
		String logModeString = VIAConnectProConstant.ZERO.equals(logModeStatus) ? VIAConnectProConstant.DISABLED : VIAConnectProConstant.ENABLED;
		statistics.put(String.format("%s#%s", groupName, VIAConnectProConstant.ACTIVATE_SYSTEM_LOG), logModeString);
		// Chrome join through browser
		param = Collections.singletonList(VIAConnectProMonitoringMetric.CHROME_JOIN_THROUGH_BROWSER_GET.getParam());
		String rawChromeStatus = sendTelnetCommand(VIAConnectProMonitoringMetric.CHROME_JOIN_THROUGH_BROWSER_GET.getCommand(), param, false);
		String chromeStatus = rawResponseHandling(rawChromeStatus);
		String chromeStatusString = VIAConnectProConstant.ZERO.equals(chromeStatus) ? VIAConnectProConstant.DISABLED : VIAConnectProConstant.ENABLED;
		statistics.put(String.format("%s#%s", groupName, VIAConnectProConstant.JOIN_THROUGH_BROWSER), chromeStatusString);
		// Chrome API Mode
		param = Collections.singletonList(VIAConnectProMonitoringMetric.CHROME_API_MODE_GET.getParam());
		String rawChromeAPIModeStatus = sendTelnetCommand(VIAConnectProMonitoringMetric.CHROME_API_MODE_GET.getCommand(), param, false);
		String chromeAPIModeStatus = rawResponseHandling(rawChromeAPIModeStatus);
		String chromeAPIModeStatusString = VIAConnectProConstant.ZERO.equals(chromeAPIModeStatus) ? VIAConnectProConstant.DISABLED : VIAConnectProConstant.ENABLED;
		statistics.put(String.format("%s#%s", groupName, VIAConnectProConstant.API_SETTINGS_COMMAND), chromeAPIModeStatusString);
		// Quick client access
		param = Collections.singletonList(VIAConnectProMonitoringMetric.QUICK_CLIENT_ACCESS_GET.getParam());
		String rawQuickClientAccessStatus = sendTelnetCommand(VIAConnectProMonitoringMetric.QUICK_CLIENT_ACCESS_GET.getCommand(), param, false);
		String quickClientAccessStatusInt = rawResponseHandling(rawQuickClientAccessStatus);

		String quickClientAccessStatus = VIAConnectProConstant.ONE.equals(quickClientAccessStatusInt) ? VIAConnectProConstant.ENABLED : VIAConnectProConstant.DISABLED;
		statistics.put(String.format("%s#%s", groupName, VIAConnectProConstant.QUICK_CLIENT_ACCESS), quickClientAccessStatus);
		// Volume
		String rawVolume = sendTelnetCommand(VIAConnectProMonitoringMetric.VOLUME.getCommand(), Collections.singletonList(VIAConnectProMonitoringMetric.VOLUME.getParam()), false);
		String[] splitVolume = rawVolume.split(VIAConnectProConstant.REGEX_VERTICAL_LINE);
		String volume = splitVolume[2];
		statistics.put(String.format("%s#%s", groupName, VIAConnectProConstant.VOLUME), volume);
		controls.add(createSlider(String.format("%s#%s", groupName, VIAConnectProConstant.VOLUME), "0%", "100%", 0f, 100f, Float.valueOf(volume)));
		// Wifi guest mode
		String rawWifiGuestMode = sendTelnetCommand(VIAConnectProMonitoringMetric.WIFI_GUEST_MODE.getCommand(), Collections.singletonList(VIAConnectProMonitoringMetric.WIFI_GUEST_MODE.getParam()), false);
		String wifiGuestMode = rawResponseHandling(rawWifiGuestMode);
		if (wifiGuestMode.equals(VIAConnectProConstant.ERROR_20057)) {
			statistics.put(String.format("%s#%s", groupName, VIAConnectProConstant.WIFI_GUEST_MODE), VIAConnectProConstant.NONE);
			logger.error(String.format("Populate failed - Response error code: %s, error description: %s", VIAConnectProErrorMetric.ERROR_20057.getErrorCode(), VIAConnectProErrorMetric.ERROR_20057.getErrorDescription()));
		} else {
			statistics.put(String.format("%s#%s", groupName, VIAConnectProConstant.WIFI_GUEST_MODE), wifiGuestMode);
			controls.add(createSwitch(String.format("%s#%s", groupName, VIAConnectProConstant.WIFI_GUEST_MODE), Integer.parseInt(wifiGuestMode),
					VIAConnectProConstant.DISABLE,
					VIAConnectProConstant.ENABLE));
		}
	}

	/**
	 * Populate statistics and controls for DeviceSettings-Moderator group
	 *
	 * @param statistics Map of statistics
	 */
	private void populateDeviceSettingsModeratorGroup(Map<String, String> statistics) {
		if (!isConfigManagement()) {
			return;
		}
		// Moderator-Status
		String groupName = VIAConnectProMonitoringMetric.PART_PRESENT_CONFIRM_GET.getGroupName();
		List<String> param = Collections.singletonList(VIAConnectProMonitoringMetric.MODERATOR_MODE_STATUS_GET.getParam());
		String rawPresentationModeStatus = sendTelnetCommand(VIAConnectProMonitoringMetric.MODERATOR_MODE_STATUS_GET.getCommand(), param, false);
		String presentationModeStatus = rawResponseHandling(rawPresentationModeStatus);
		String presentationModeStatusString = VIAConnectProConstant.ZERO.equals(presentationModeStatus) ? VIAConnectProConstant.DISABLED : VIAConnectProConstant.ENABLED;
		statistics.put(String.format("%s#%s", groupName, VIAConnectProConstant.MODERATOR_MODE_STATUS), presentationModeStatusString);
		// Moderator-ParticipantPresentConfirm
		param = Collections.singletonList(VIAConnectProMonitoringMetric.PART_PRESENT_CONFIRM_GET.getParam());
		String rawPartPresentConfirm = sendTelnetCommand(VIAConnectProMonitoringMetric.PART_PRESENT_CONFIRM_GET.getCommand(), param, false);
		String partPresentConfirm = rawResponseHandling(rawPartPresentConfirm);
		String partPresentConfirmString = VIAConnectProConstant.ZERO.equals(partPresentConfirm) ? VIAConnectProConstant.DISABLED : VIAConnectProConstant.ENABLED;
		if (partPresentConfirm.equals(VIAConnectProConstant.ERROR_1008)) {
			return;
		}
		statistics.put(String.format("%s#%s", groupName, VIAConnectProConstant.PARTICIPANT_PRESENTATION_START_CONFIRM), partPresentConfirmString);
	}

	/**
	 * Populate statistics and controls for DeviceSettings-RoomOverlay
	 *
	 * @param statistics Map of statistics
	 */
	private void populateDeviceSettingsRoomOverlayGroup(Map<String, String> statistics) {
		if (!isConfigManagement()) {
			return;
		}
		String groupName = VIAConnectProMonitoringMetric.ROOM_OVERLAY_STATUS_GET.getGroupName();
		List<String> param = Collections.singletonList(VIAConnectProMonitoringMetric.ROOM_OVERLAY_STATUS_GET.getParam());
		String rawRoomOverlayStatus = sendTelnetCommand(VIAConnectProMonitoringMetric.ROOM_OVERLAY_STATUS_GET.getCommand(), param, false);
		String[] roomOverlayResponse = rawRoomOverlayStatus.split(VIAConnectProConstant.REGEX_VERTICAL_LINE);
		String roomOverlayStatus = roomOverlayResponse[2];
		String roomOverlayStatusString = VIAConnectProConstant.ZERO.equals(roomOverlayStatus) ? VIAConnectProConstant.DISABLED : VIAConnectProConstant.ENABLED;
		statistics.put(String.format("%s#%s", groupName, VIAConnectProConstant.ROOM_OVERLAY_ACTIVE_STATUS), roomOverlayStatusString);
		if (VIAConnectProConstant.ONE.equals(roomOverlayStatus)) {
			statistics.put(String.format("%s#%s", groupName, VIAConnectProConstant.AUTO_HIDE_TIME), roomOverlayResponse[3]);
		}
	}

	/**
	 * Populate ParticipantList group
	 *
	 * @param statistics Map of statistics
	 */
	private void populateParticipantGroup(Map<String, String> statistics, List<AdvancedControllableProperty> controls) {
		String groupName = VIAConnectProMonitoringMetric.PLIST_All_STATUS.getGroupName();
		List<String> usernames = new ArrayList<>();
		if (!isValidUsernameListAndPopulateList(statistics, controls, groupName, usernames, true, null)) {
			return;
		}
		ParticipantListDTO participantListDTO = getListParticipant();
		statistics.put(String.format("%s#%s", groupName, VIAConnectProConstant.CURRENT_LOGGED_IN_USERS), String.valueOf(participantListDTO.getLoggedInUsers()));
		int i = 0;
		for (Map.Entry<String, String> entry : participantListDTO.getUserAndStatusMap().entrySet()) {
			statistics.put(String.format("%s#%s%s", groupName, VIAConnectProConstant.PARTICIPANT, (i + 1)), entry.getKey());
			String plistStatus;
			if (VIAConnectProConstant.ZERO.equals(entry.getValue())) {
				plistStatus = VIAConnectProConstant.NOT_PRESENTING;
			} else if (VIAConnectProConstant.ONE.equals(entry.getValue())) {
				plistStatus = VIAConnectProConstant.PRESENTING;
			} else {
				plistStatus = VIAConnectProConstant.WAITING_FOR_PERMISSION;
			}
			statistics.put(String.format("%s#Participant%sStatus", groupName, (i + 1)), plistStatus);
			i++;
		}
	}

	/**
	 * Populate UserModeration group
	 *
	 * @param statistics Map of statistics
	 */
	private void populateUserModeration(Map<String, String> statistics, List<AdvancedControllableProperty> controls) {
		if (!isConfigManagement()) {
			return;
		}
		String groupName = VIAConnectProConstant.USER_MODERATION;
		List<String> usernames = new ArrayList<>();
		if (!isValidUsernameListAndPopulateList(statistics, controls, groupName, usernames, false, previousUserName)) {
			return;
		}
		// DISPLAY STATUS
		List<String> param = new ArrayList<>();
		param.add(VIAConnectProMonitoringMetric.DISPLAY_STATUS_GET.getParam());
		if (previousUserName == null || !usernames.contains(previousUserName)) {
			previousUserName = usernames.get(0);
		}
		param.add(previousUserName);
		// the output should be: DisplayStatus|Get|Presenting/NotPresenting/Waiting
		String rawDisplayStatus = sendTelnetCommand(VIAConnectProMonitoringMetric.DISPLAY_STATUS_GET.getCommand(), param, false);
		String displayStatus = rawResponseHandling(rawDisplayStatus);
		if (VIAConnectProConstant.WAITING.equals(displayStatus)) {
			displayStatus = VIAConnectProConstant.NOT_PRESENTING;
		}
		statistics.put(String.format("%s#%s", groupName, VIAConnectProConstant.USER_DISPLAY_STATUS), displayStatus);
		if (isConfigManagement()) {
			if (VIAConnectProConstant.PRESENTING.equals(displayStatus)) {
				statistics.put(String.format("%s#%s", groupName, VIAConnectProConstant.USER_PRESENTATION), DisplayStatusModeEnum.STOP.getName());
				controls.add(createButton(String.format("%s#%s", groupName, VIAConnectProConstant.USER_PRESENTATION), DisplayStatusModeEnum.STOP.getName(), VIAConnectProConstant.IN_PROGRESS));
			} else {
				statistics.put(String.format("%s#%s", groupName, VIAConnectProConstant.USER_PRESENTATION), DisplayStatusModeEnum.START.getName());
				controls.add(createButton(String.format("%s#%s", groupName, VIAConnectProConstant.USER_PRESENTATION), DisplayStatusModeEnum.START.getName(), VIAConnectProConstant.IN_PROGRESS));
			}
		}
		// Kick user:
		if (isConfigManagement()) {
			statistics.put(String.format("%s#%s", groupName, VIAConnectProConstant.USER_KICK_OFF), VIAConnectProConstant.EMPTY);
			controls.add(createButton(String.format("%s#%s", groupName, VIAConnectProConstant.USER_KICK_OFF), VIAConnectProConstant.KICK_OFF, "Kicking user ..."));
		}
	}

	/**
	 * Populate streaming control statistics and controls
	 *
	 * @param statistics Map of statistics
	 * @param controls List of AdvancedControllableProperty
	 */
	private void populateStreamingFromDeviceToExternal(Map<String, String> statistics, List<AdvancedControllableProperty> controls) {
		if (!isConfigManagement()) {
			return;
		}
		// Check if streaming is activated
		String groupName = VIAConnectProControllingMetric.STREAMING_START.getGroupName();
		List<String> param = Collections.singletonList(VIAConnectProMonitoringMetric.STREAMING_STATUS_GET.getParam());
		String rawStreamingGetResponse = sendTelnetCommand(VIAConnectProMonitoringMetric.STREAMING_STATUS_GET.getCommand(), param, false);
		String[] streamingGetResponse = rawStreamingGetResponse.split(VIAConnectProConstant.REGEX_VERTICAL_LINE);
		String streamingStatus = streamingGetResponse[2];
		statistics.put(String.format("%s#%s", groupName, VIAConnectProConstant.STREAMING_MODE), streamingStatus);
		controls.add(createSwitch(String.format("%s#%s", groupName, VIAConnectProConstant.STREAMING_MODE), Integer.parseInt(streamingStatus),
				VIAConnectProConstant.DEACTIVATE,
				VIAConnectProConstant.ACTIVATE));
		if (VIAConnectProConstant.ZERO.equals(streamingStatus)) {
			statistics.put(String.format("%s#%s", groupName, VIAConnectProConstant.URL), VIAConnectProConstant.UDP);
			controls.add(createText(String.format("%s#%s", groupName, VIAConnectProConstant.URL), VIAConnectProConstant.UDP));
			return;
		}
		populateStreamingFromDeviceToExternalStatus(statistics, groupName, streamingGetResponse);
		statistics.put(String.format("%s#%s", groupName, VIAConnectProConstant.ACTION), VIAConnectProConstant.START);
		List<String> streamModes = new ArrayList<>();
		streamModes.add(VIAConnectProConstant.START);
		streamModes.add(VIAConnectProConstant.STOP);
		streamModes.add(VIAConnectProConstant.RESTART);
		streamModes.add(VIAConnectProConstant.CHANGE);
		controls.add(createDropdown(String.format("%s#%s", groupName, VIAConnectProConstant.ACTION), streamModes, VIAConnectProConstant.START));

		statistics.put(String.format("%s#%s", groupName, VIAConnectProConstant.APPLY), VIAConnectProConstant.EMPTY);
		controls.add(createButton(String.format("%s#%s", groupName, VIAConnectProConstant.APPLY), VIAConnectProConstant.APPLY, "Applying the stream..."));
	}

	/**
	 * Populate streaming statuses for StreamingFromDeviceToExternal
	 *
	 * @param statistics Map of statistics
	 * @param groupName Group name
	 * @param streamingGetResponse Array of responses from the device
	 */
	private void populateStreamingFromDeviceToExternalStatus(Map<String, String> statistics, String groupName, String[] streamingGetResponse) {
		String rawSStatus = sendTelnetCommand(VIAConnectProMonitoringMetric.STREAMING_STATUS_SSTATUS_GET.getCommand(), Collections.singletonList(VIAConnectProMonitoringMetric.STREAMING_STATUS_SSTATUS_GET.getParam()), false);
		String[] splitRawSStatus = rawSStatus.split(VIAConnectProConstant.REGEX_VERTICAL_LINE);
		String sstatus = splitRawSStatus[1];
		int intStreamingGetResponse = Integer.parseInt(sstatus);
		String statusValue;
		switch (intStreamingGetResponse) {
			case 0:
				statusValue = VIAConnectProConstant.NO_URL_IS_BEING_STREAMED;
				break;
			case 1:
				statusValue = VIAConnectProConstant.RECORDING_IS_ON;
				break;
			case 6:
				statusValue = VIAConnectProConstant.STREAMING_IS_ON;
				break;
			default:
				statusValue = VIAConnectProConstant.EMPTY;
		}
		statistics.put(String.format("%s#%s", groupName, VIAConnectProConstant.STATUS), statusValue);
		if (VIAConnectProConstant.ZERO.equals(streamingGetResponse[2])) {
			statistics.put(String.format("%s#%s", groupName, VIAConnectProConstant.URL), VIAConnectProConstant.NO_URL);
		} else {
			if (streamingGetResponse.length == 4) {
				// Single display
				statistics.put(String.format("%s#%s", groupName, VIAConnectProConstant.URL), streamingGetResponse[3]);
			} else if (streamingGetResponse.length == 5) {
				// Dual display
				statistics.put(String.format("%s#%s", groupName, VIAConnectProConstant.URL_1), streamingGetResponse[3]);
				statistics.put(String.format("%s#%s", groupName, VIAConnectProConstant.URL_2), streamingGetResponse[4]);
			}
		}
	}

	/**
	 * Populate streaming status statistics and controls
	 *
	 * @param stats Map of statistics
	 * @param controls List of AdvancedControllableProperty
	 */
	private void populateStreamingFromExternalToDevice(Map<String, String> stats, List<AdvancedControllableProperty> controls) {
		String groupName = VIAConnectProControllingMetric.STREAMING_URL.getGroupName();
		stats.put(String.format("%s#%s", groupName, VIAConnectProConstant.EXTERNAL_STREAM_URL), VIAConnectProConstant.RTSP);
		controls.add(createText(String.format("%s#%s", groupName, VIAConnectProConstant.EXTERNAL_STREAM_URL), VIAConnectProConstant.RTSP));

		stats.put(String.format("%s#%s", groupName, VIAConnectProConstant.START_STREAMING), VIAConnectProConstant.EMPTY);
		controls.add(createButton(String.format("%s#%s", groupName, VIAConnectProConstant.START_STREAMING), VIAConnectProConstant.START, "Starting..."));

		stats.put(String.format("%s#%s", groupName, VIAConnectProConstant.STOP_STREAMING), VIAConnectProConstant.EMPTY);
		controls.add(createButton(String.format("%s#%s", groupName, VIAConnectProConstant.STOP_STREAMING), VIAConnectProConstant.STOP, "Stopping..."));
	}

	/**
	 * Check if it is a dual display or not
	 *
	 * @return true if it is dual display and vice versa.
	 */
	private boolean isDualDisplayStreaming() {
		List<String> param = Collections.singletonList(VIAConnectProMonitoringMetric.STREAMING_STATUS_GET.getParam());
		String rawStreamingGetResponse = sendTelnetCommand(VIAConnectProMonitoringMetric.STREAMING_STATUS_GET.getCommand(), param, false);
		String[] streamingGetResponse = rawStreamingGetResponse.split(VIAConnectProConstant.REGEX_VERTICAL_LINE);
		return streamingGetResponse.length == 5;
	}

	/**
	 * Send telnet command to the device.
	 *
	 * @param command Name of the command
	 * @param params Params of the command. Example: <P1>{param 1}</P1> <P2>{param 2}</P2>
	 * @param isControlCommand Check if is control to throw the proper exception.
	 * @return String of raw response
	 */
	private String sendTelnetCommand(String command, List<String> params, boolean isControlCommand) {
		try {
			String response = this.internalSend(buildTelnetRequest(command, params, false));
			String inputCommand = command;
			// Handle special cases
			if (inputCommand.equals(VIAConnectProControllingMetric.STREAMING_START.getCommand()) && isControlCommand) {
				if (params.get(0).equals(VIAConnectProControllingMetric.STREAMING_START.getParam())) {
					inputCommand = VIAConnectProConstant.SSTART_SPECIAL_CASE;
				} else if (params.get(0).equals(VIAConnectProControllingMetric.STREAMING_STOP.getParam())) {
					inputCommand = VIAConnectProConstant.SSTOP_SPECIAL_CASE;
				} else if (params.get(0).equals(VIAConnectProControllingMetric.STREAMING_RESTART.getParam())) {
					inputCommand = VIAConnectProConstant.SRESTART_SPECIAL_CASE;
				} else if (params.get(0).equals(VIAConnectProControllingMetric.STREAMING_CHANGE.getParam())) {
					inputCommand = VIAConnectProConstant.SCHANGE_SPECIAL_CASE;
				}
			} else if (params.get(0).equals(VIAConnectProMonitoringMetric.STREAMING_STATUS_SSTATUS_GET.getParam())) {
				inputCommand = VIAConnectProConstant.RSSTATUS_SPECIAL_CASE;
			}
			else if (inputCommand.equals(VIAConnectProMonitoringMetric.IP_INFORMATION.getCommand())) {
				inputCommand = VIAConnectProConstant.IP_SPECIAL_CASE;
			}
			// Resend the command one more time only for get commands
			if (!response.contains(inputCommand) && !isControlCommand) {
				// retry one more time. This is because if user control the device (properly change some properties) -
				//  so we request one more time to make sure we get the correct response. If it's failed again => It's an error.
				response = this.internalSend(buildTelnetRequest(command, params, false));
				if (!response.contains(inputCommand)) {
					throw new ResourceNotReachableException("Fail to monitor properties");
				}
			}
			String[] strings = response.split(VIAConnectProConstant.END_COMMAND);
			// Handle case where response contains more than 2 responses (where one of them is not correct, other is correct)
			if (strings.length > 1) {
				for (String string : strings) {
					if (string.contains(inputCommand)) {
						response = string;
						break;
					}
				}
			} else {
				response = strings[0];
			}
			return response;
		} catch (Exception exception) {
			if (isControlCommand) {
				throw new CommandFailureException(this.getAddress(), command, "Fail to send control command", exception);
			} else {
				throw new ResourceNotReachableException("Fail to monitor properties", exception);
			}
		}
	}

	/**
	 * Build telnet request before sending it to the device
	 * Example request: <P><UN>su</UN><Pwd>supass</Pwd><Cmd>Login</Cmd></P>
	 *
	 * @param command Name of the command
	 * @param params List of params
	 * @param isLoginCommand is login command
	 * @return String of built telnet request
	 */
	private String buildTelnetRequest(String command, List<String> params, boolean isLoginCommand) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<P>");
		stringBuilder.append(String.format("<UN>%s</UN>", this.getLogin()));
		if (isLoginCommand) {
			stringBuilder.append(String.format("<Pwd>%s</Pwd>", this.getPassword()));
		} else {
			stringBuilder.append("<Pwd></Pwd>");
		}
		stringBuilder.append(String.format("<Cmd>%s</Cmd>", command));
		for (int i = 0; i < params.size(); i++) {
			stringBuilder.append(String.format("<P%s>%s</P%s>", i + 1, params.get(i), i + 1));
		}
		stringBuilder.append("</P>");
		return String.valueOf(stringBuilder);
	}

	/**
	 * Update value of cached stats and controls
	 *  @param property Group name
	 * @param propertyValue new value to be set
	 * @param cachedStats Map of statistics
	 * @param cachedControls List of cached AdvancedControllableProperty
	 * @param localControls List of {@link VIAConnectProCommunicator#localExtendedStatistics#controlProperty(ControllableProperty)}
	 */
	private void updateLatestPropertyValue(String property, String propertyValue, Map<String, String> cachedStats, List<AdvancedControllableProperty> cachedControls,
			List<AdvancedControllableProperty> localControls) {
		cachedStats.put(property, propertyValue);
		// Remove duplicate AdvancedControllableProperty out of cachedControls
		for (AdvancedControllableProperty cachedControl : cachedControls) {
			if (cachedControl.getName().equals(property)) {
				cachedControls.remove(cachedControl);
				break;
			}
		}

		// Populate new cached AdvancedControllableProperty
		for (AdvancedControllableProperty control : localControls) {
			if (control.getName().equals(property)) {
					AdvancedControllableProperty newControl = new AdvancedControllableProperty();
					newControl.setName(control.getName());
					newControl.setValue(propertyValue);
					newControl.setTimestamp(control.getTimestamp());
					newControl.setType(control.getType());
					cachedControls.add(newControl);
				break;
			}
		}
	}

	/**
	 * Handle response from the device
	 *
	 * @param rawResponse raw response from the device
	 * @return proper value
	 */
	private String rawResponseHandling(String rawResponse) {
		String[] rawResponseArray = rawResponse.split(VIAConnectProConstant.REGEX_VERTICAL_LINE);
		return rawResponseArray[rawResponseArray.length - 1];
	}

	/**
	 * Remove cached statistics and controls
	 *
	 * @param cachedStats Map of cached statistics
	 * @param cachedControls List of cached controls
	 * @param metricName Name of the metric to be removed
	 */
	private void removeCachedStatisticAndControl(Map<String, String> cachedStats, List<AdvancedControllableProperty> cachedControls, String metricName) {
		List<String> listStatsRemove = new ArrayList<>();
		List<AdvancedControllableProperty> listControlsRemove = new ArrayList<>();
		for (Map.Entry<String, String> entry : cachedStats.entrySet()) {
			String propertyName = entry.getKey();
			String[] groupNames = propertyName.split(VIAConnectProConstant.HASH);
			if (groupNames[0].equals(metricName)) {
				listStatsRemove.add(propertyName);
			}
		}
		for (String s : listStatsRemove) {
			cachedStats.remove(s);
		}
		for (AdvancedControllableProperty control : cachedControls) {
			String propertyName = control.getName();
			String[] groupNames = propertyName.split(VIAConnectProConstant.HASH);
			if (groupNames[0].equals(metricName)) {
				listControlsRemove.add(control);
			}
		}
		for (AdvancedControllableProperty advancedControllableProperty : listControlsRemove) {
			cachedControls.remove(advancedControllableProperty);
		}
	}

	/**
	 * Check if there are any usernames in the username list.
	 * And populate statistics and controls if valid.
	 *
	 * @param stats Map of statistics
	 * @param controls List of AdvancedControllableProperty
	 * @param groupName Name of the group
	 * @param usernames List of usernames
	 * @param onlyValidate Only validate if user is logged in, not populate stats and controls
	 * @return boolean
	 */
	private boolean isValidUsernameListAndPopulateList(Map<String, String> stats, List<AdvancedControllableProperty> controls, String groupName, List<String> usernames, boolean onlyValidate, String defaultValue) {
		ParticipantListDTO participantListDTO = getListParticipant();
		if (participantListDTO.getUserAndStatusMap().size() == 0) {
			stats.put(String.format("%s#%s", groupName, VIAConnectProConstant.USER), "No one is logged in.");
			return false;
		}
		if (onlyValidate) {
			return true;
		}
		for (Map.Entry<String, String> entry : participantListDTO.getUserAndStatusMap().entrySet()) {
			usernames.add(entry.getKey());
		}
		if (defaultValue == null) {
			defaultValue = usernames.get(0);
		}
		stats.put(String.format("%s#%s", groupName, VIAConnectProConstant.USER), defaultValue);
		controls.add(createDropdown(String.format("%s#%s", groupName, VIAConnectProConstant.USER), usernames, defaultValue));
		return true;
	}

	/**
	 * Populate cached AdvancedControllableProperty to current AdvancedControllableProperty
	 *
	 * @param currentControls List of current AdvancedControllableProperty
	 * @param cachedControls List of cached AdvancedControllableProperty
	 */
	private void populateCachedControlProperties(List<AdvancedControllableProperty> currentControls, List<AdvancedControllableProperty> cachedControls) {
		if (cachedControls.size() == 0) {
			return;
		}
		List<AdvancedControllableProperty> listControlToBeRemoved = new ArrayList<>();
		Map<String, Object> nameToValue = cachedControls.stream()
				.collect(Collectors.toMap(AdvancedControllableProperty::getName, AdvancedControllableProperty::getValue));
			for (AdvancedControllableProperty currentControl : currentControls) {
				Object currentCachedControlValue = nameToValue.get(currentControl.getName());
				if (currentCachedControlValue != null) {
					listControlToBeRemoved.add(currentControl);
				}
			}
		currentControls.removeAll(listControlToBeRemoved);
		currentControls.addAll(cachedControls);
	}

	/**
	 * Populate cached AdvancedControllableProperty for streaming control
	 *
	 * @param currentStats Map of current statistics
	 * @param currentControls List of current AdvancedControllableProperty
	 * @param cachedControls List of cached AdvancedControllableProperty
	 */
	private void populateCachedStreamingControl(Map<String, String> currentStats, List<AdvancedControllableProperty> currentControls, List<AdvancedControllableProperty> cachedControls) {
		String groupName = VIAConnectProControllingMetric.STREAMING_START.getGroupName();
		String actionProperty = String.format("%s#%s", groupName, VIAConnectProConstant.ACTION);
		if (currentStats.get(actionProperty) == null) {
			return;
		}
		for (AdvancedControllableProperty cachedControl : cachedControls) {
			if (cachedControl.getName().equals(actionProperty)) {
				String cachedControlValue = (String) cachedControl.getValue();
				currentControls.removeIf(advancedControllableProperty -> advancedControllableProperty.getName().equals(String.format("%s#%s", groupName, VIAConnectProConstant.NEW_URL)));
				currentControls.removeIf(advancedControllableProperty -> advancedControllableProperty.getName().equals(String.format("%s#%s", groupName, VIAConnectProConstant.NEW_URL_1)));
				currentControls.removeIf(advancedControllableProperty -> advancedControllableProperty.getName().equals(String.format("%s#%s", groupName, VIAConnectProConstant.NEW_URL_2)));
				List<String> param = Collections.singletonList(VIAConnectProMonitoringMetric.STREAMING_STATUS_GET.getParam());
				String rawStreamingGetResponse = sendTelnetCommand(VIAConnectProMonitoringMetric.STREAMING_STATUS_GET.getCommand(), param, false);
				String[] streamingGetResponse = rawStreamingGetResponse.split(VIAConnectProConstant.REGEX_VERTICAL_LINE);
				// Populate new status≡
				populateStreamingFromDeviceToExternalStatus(currentStats, groupName, streamingGetResponse);
				if (VIAConnectProConstant.START.equals(cachedControlValue) || VIAConnectProConstant.STOP.equals(cachedControlValue)) {
					currentStats.remove(String.format("%s#%s", groupName, VIAConnectProConstant.NEW_URL));
					currentStats.remove(String.format("%s#%s", groupName, VIAConnectProConstant.NEW_URL_1));
					currentStats.remove(String.format("%s#%s", groupName, VIAConnectProConstant.NEW_URL_2));
				} else {
					if (streamingGetResponse.length == 5) {
						currentStats.put(String.format("%s#%s", groupName, VIAConnectProConstant.NEW_URL_1), streamingGetResponse[3]);
						currentControls.add(createText(String.format("%s#%s", groupName, VIAConnectProConstant.NEW_URL_1), streamingGetResponse[3]));
						currentStats.put(String.format("%s#%s", groupName, VIAConnectProConstant.NEW_URL_2), streamingGetResponse[4]);
						currentControls.add(createText(String.format("%s#%s", groupName, VIAConnectProConstant.NEW_URL_2), streamingGetResponse[4]));
					} else {
						currentStats.put(String.format("%s#%s", groupName, VIAConnectProConstant.NEW_URL), streamingGetResponse[3]);
						currentControls.add(createText(String.format("%s#%s", groupName, VIAConnectProConstant.NEW_URL), streamingGetResponse[3]));
					}
				}
				break;
			}
		}
	}

	/**
	 * Control Property: User Moderation
	 *
	 * @param propertyValue Property value
	 * @param propertyName Property name
	 */
	private void userModerationControl(String propertyValue, String propertyName) {
		switch (propertyName) {
			case VIAConnectProConstant.USER:
				previousUserName = propertyValue;
				break;
			case VIAConnectProConstant.USER_PRESENTATION:
			case VIAConnectProConstant.USER_KICK_OFF:
				normalControlProperties(VIAConnectProControllingMetric.DISPLAY_STATUS_SET, propertyName, propertyValue);
				break;
			default:
				throw new IllegalArgumentException("Unexpected value: " + propertyName);
		}
	}

	/**
	 * Control Property: DeviceSettings
	 *
	 * @param propertyValue Value of property
	 * @param propertyName Name of the property
	 */
	private void deviceSettingsControl(String propertyValue, String propertyName) {
		switch (propertyName) {
			case VIAConnectProConstant.VOLUME:
				normalControlProperties(VIAConnectProControllingMetric.VOLUME_SET, propertyName, propertyValue);
				break;
			case VIAConnectProConstant.WIFI_GUEST_MODE:
				normalControlProperties(VIAConnectProControllingMetric.WIFI_GUEST_MODE_SET, propertyName, propertyValue);
				break;
			default:
				throw new IllegalArgumentException("Unexpected value: " + propertyName);
		}
	}

	/**
	 * controlProperty for cached case - require more than one value then send the command to the device.
	 *
	 * @param viaConnectProControllingMetric control metric
	 * @param propertyName property name
	 * @param propertyValue property value
	 * @param property group name and property name (GrName#PropName)
	 * @param groupName group name
	 */
	private void cachedControlProperties(VIAConnectProControllingMetric viaConnectProControllingMetric, String propertyName, String propertyValue, String property, String groupName) {
		isCachedControlling = true;
		if (localExtendedStatistics == null || cachedLocalExtendedStatistics == null) {
			return;
		}
		Map<String, String> cachedStats = cachedLocalExtendedStatistics.getStatistics();
		List<AdvancedControllableProperty> cachedControls = cachedLocalExtendedStatistics.getControllableProperties();

		Map<String, String> localStats = localExtendedStatistics.getStatistics();
		List<AdvancedControllableProperty> localControls = localExtendedStatistics.getControllableProperties();
		List<String> param = new ArrayList<>();

		switch (viaConnectProControllingMetric) {
			case STREAMING_START:
				isStreamingControl = true;
				switch (propertyName) {
					case VIAConnectProConstant.ACTION:
						switch (propertyValue) {
							case VIAConnectProConstant.START:
							case VIAConnectProConstant.STOP:
							case VIAConnectProConstant.RESTART:
							case VIAConnectProConstant.CHANGE:
								updateLatestPropertyValue(property, propertyValue, cachedStats, cachedControls, localControls);
								break;
							default:
								throw new IllegalArgumentException("Unexpected value: " + propertyName);
						}
						break;
					case VIAConnectProConstant.URL:
					case VIAConnectProConstant.URL_1:
					case VIAConnectProConstant.URL_2:
					case VIAConnectProConstant.NEW_URL:
					case VIAConnectProConstant.NEW_URL_1:
					case VIAConnectProConstant.NEW_URL_2:
						updateLatestPropertyValue(property, propertyValue, cachedStats, cachedControls, localControls);
						break;
					default:
						throw new IllegalArgumentException("Unexpected value: " + propertyName);
				}
				break;
			case STREAMING_URL:
				switch (propertyName) {
					case VIAConnectProConstant.EXTERNAL_STREAM_URL:
						updateLatestPropertyValue(property, propertyValue, cachedStats, cachedControls, localControls);
						break;
					case VIAConnectProConstant.START_STREAMING:
						String newStreamURL = localStats.get(String.format("%s#%s", groupName, VIAConnectProConstant.EXTERNAL_STREAM_URL));
						if (!newStreamURL.contains(VIAConnectProConstant.UDP) && !newStreamURL.contains(VIAConnectProConstant.RTSP) && !newStreamURL.contains(VIAConnectProConstant.HTTPS)) {
								throw new CommandFailureException(this.getAddress(), VIAConnectProControllingMetric.STREAMING_URL.getCommand(),
										String.format("Fail to start new stream with URL: %s. Only accept UDP, RTSP or youtube link.", newStreamURL));
						}
						param.add(VIAConnectProControllingMetric.STREAMING_URL.getParam());
						param.add(newStreamURL);
						String rawNewStream = sendTelnetCommand(VIAConnectProControllingMetric.STREAMING_URL.getCommand(), param, true);
						String[] splitRawNewStream = rawNewStream.split(VIAConnectProConstant.REGEX_VERTICAL_LINE);
						if (splitRawNewStream[splitRawNewStream.length - 1].equals(VIAConnectProConstant.ERROR_504)) {
							logger.error("VIAConnectProCommunicator: Stream already start");
							// Cannot get status of streamingURL so suppress this error.
							break;
						}
						// Example response: : StreamingURL|1|1. 1 is start streaming successfully.
						if (!VIAConnectProConstant.ONE.equals(splitRawNewStream[splitRawNewStream.length - 1])) {
							throw new CommandFailureException(this.getAddress(), VIAConnectProControllingMetric.STREAMING_URL.getCommand(),
									String.format("Fail to start new stream with URL: %s", newStreamURL));
						}
						removeCachedStatisticAndControl(cachedStats, cachedControls, groupName);
						break;
					case VIAConnectProConstant.STOP_STREAMING:
						String stopStreamURL = localStats.get(String.format("%s#%s", groupName, VIAConnectProConstant.EXTERNAL_STREAM_URL));
						if (!stopStreamURL.contains(VIAConnectProConstant.UDP) && !stopStreamURL.contains(VIAConnectProConstant.RTSP) && !stopStreamURL.contains(VIAConnectProConstant.HTTPS)) {
							throw new CommandFailureException(this.getAddress(), VIAConnectProControllingMetric.STREAMING_URL.getCommand(),
									String.format("Fail to stop stream with URL: %s. Only accept UDP, RTSP or youtube link.", stopStreamURL));
						}
						param.add(VIAConnectProConstant.ZERO);
						param.add(stopStreamURL);
						String rawStopStream = sendTelnetCommand(VIAConnectProControllingMetric.STREAMING_URL.getCommand(), param, true);
						String[] splitRawStopStream = rawStopStream.split(VIAConnectProConstant.REGEX_VERTICAL_LINE);
						if (splitRawStopStream[splitRawStopStream.length - 1].equals(VIAConnectProConstant.ERROR_504)) {
							logger.error("VIAConnectProCommunicator: Stream already stop");
							// Cannot get status of streamingURL so suppress this error.
							break;
						}
						// Example response: : StreamingURL|0|1. 1 is stop streaming successfully.
						if (!VIAConnectProConstant.ONE.equals(splitRawStopStream[splitRawStopStream.length - 1])) {
							throw new CommandFailureException(this.getAddress(), VIAConnectProControllingMetric.STREAMING_URL.getCommand(),
									String.format("Fail to stop stream with URL: %s", stopStreamURL));
						}
						removeCachedStatisticAndControl(cachedStats, cachedControls, groupName);
						break;
					default:
						throw new IllegalArgumentException("Unexpected value: " + propertyName);
				}
				break;
			default:
				throw new IllegalArgumentException("Unexpected value: " + propertyName);
		}
	}

	/**
	 * controlProperty for normal case - require only one value then send the command to the device.
	 *
	 * @param viaConnectProMetric Control metric
	 * @param propertyName property name
	 * @param propertyValue value of that property
	 */
	private void normalControlProperties(VIAConnectProControllingMetric viaConnectProMetric, String propertyName, String propertyValue) {
		switch (viaConnectProMetric) {
			case VOLUME_SET:
				List<String> volumeParams = new ArrayList<>();
				volumeParams.add(VIAConnectProControllingMetric.VOLUME_SET.getParam());
				String stringVolume = String.valueOf(Float.valueOf(propertyValue).intValue());
				if (VIAConnectProConstant.ZERO.equals(stringVolume)) {
					break;
				}
				volumeParams.add(stringVolume); // param must have type integer.
				String rawVolumeSet = sendTelnetCommand(VIAConnectProControllingMetric.VOLUME_SET.getCommand(), volumeParams, true);
				String[] rawVolumeSetResponse = rawVolumeSet.split(VIAConnectProConstant.REGEX_VERTICAL_LINE);
				// Expect response is Vol|Get|<value of Volume>|0
				String volumeResponse = rawVolumeSetResponse[2];
				if (!volumeResponse.equals(stringVolume)) {
					throw new CommandFailureException(this.getAddress(), VIAConnectProControllingMetric.VOLUME_SET.getCommand(),
							String.format("Fail to set volume to %s", propertyValue));
				}
				break;
			case WIFI_GUEST_MODE_SET:
				List<String> wifiGuestModeParams = new ArrayList<>();
				wifiGuestModeParams.add(propertyValue);
				String rawWifiGuestModeSet = sendTelnetCommand(VIAConnectProControllingMetric.WIFI_GUEST_MODE_SET.getCommand(), wifiGuestModeParams, true);
				String[] rawWifiGuestModeSetResponse = rawWifiGuestModeSet.split(VIAConnectProConstant.REGEX_VERTICAL_LINE);
				// Expect response is WifiGuestMode|0/1 Stop/Start|0/1. 1 is set successfully, 0 is fail to set.
				String wifiGuestModeSetResponse = rawWifiGuestModeSetResponse[rawWifiGuestModeSetResponse.length - 1];
				if (!VIAConnectProConstant.ONE.equals(wifiGuestModeSetResponse)) {
					throw new CommandFailureException(this.getAddress(), VIAConnectProControllingMetric.WIFI_GUEST_MODE_SET.getCommand(),
							String.format("Fail to set wifi guest mode status to %s", propertyValue));
				}
				break;
			case DISPLAY_STATUS_SET:
				String groupName = VIAConnectProConstant.USER_MODERATION;
				if (localExtendedStatistics == null || cachedLocalExtendedStatistics == null ) {
					break;
				}
				Map<String, String> localStats = localExtendedStatistics.getStatistics();
				Map<String, String> cachedStats = cachedLocalExtendedStatistics.getStatistics();
				List<AdvancedControllableProperty> cachedControls = cachedLocalExtendedStatistics.getControllableProperties();
				List<String> displayStatusParams = new ArrayList<>();
				switch (propertyName) {
					case VIAConnectProConstant.USER_PRESENTATION:

						String userName = localStats.get(String.format("%s#%s", groupName, VIAConnectProConstant.USER));
						String currentButtonLabel = localStats.get(String.format("%s#%s", groupName, VIAConnectProConstant.USER_PRESENTATION));
						String displayStatusMode;
						if (currentButtonLabel.equals(DisplayStatusModeEnum.START.getName())) {
							displayStatusMode = DisplayStatusModeEnum.START.getCode();
						} else {
							displayStatusMode = DisplayStatusModeEnum.STOP.getCode();
						}
						displayStatusParams.add(VIAConnectProControllingMetric.DISPLAY_STATUS_SET.getParam());
						displayStatusParams.add(userName);
						displayStatusParams.add(displayStatusMode);
						String rawApplyDisplayStatus = sendTelnetCommand(VIAConnectProControllingMetric.DISPLAY_STATUS_SET.getCommand(), displayStatusParams, true);
						String applyDisplayStatus = rawResponseHandling(rawApplyDisplayStatus);
						if (VIAConnectProConstant.USR_NOT_EXIST.equals(applyDisplayStatus)) {
							throw new CommandFailureException(this.getAddress(), VIAConnectProControllingMetric.DISPLAY_STATUS_SET.getCommand(),
									String.format("User %s is not exist/online at the moment.", userName));
						}
						removeCachedStatisticAndControl(cachedStats, cachedControls, groupName);
						break;
					case VIAConnectProConstant.USER_KICK_OFF:
						String kickUserName = localStats.get(String.format("%s#%s", groupName, VIAConnectProConstant.USER));
						displayStatusParams = new ArrayList<>();
						displayStatusParams.add(kickUserName);
						String rawKickOffUser = sendTelnetCommand(VIAConnectProControllingMetric.KICK_OFF.getCommand(), displayStatusParams, true);
						String[] splitRawKickOffUser = rawKickOffUser.split(VIAConnectProConstant.REGEX_VERTICAL_LINE);
						// Example response: KickOff|0/1|<username>. 0 is fail, 1 is success
						if (!VIAConnectProConstant.ONE.equals(splitRawKickOffUser[1])) {
							throw new CommandFailureException(this.getAddress(), VIAConnectProControllingMetric.KICK_OFF.getCommand(),
									String.format("Fail to kick user: %s", kickUserName));
						}
						removeCachedStatisticAndControl(cachedStats, cachedControls, groupName);
						break;
					default:
						throw new IllegalArgumentException("Unexpected value: " + propertyName);
				}
				break;
			case STREAMING_STATUS_SET:
				Map<String, String> localStats1 = localExtendedStatistics.getStatistics();
				Map<String, String> cachedStats1 = cachedLocalExtendedStatistics.getStatistics();
				List<AdvancedControllableProperty> cachedControls1 = cachedLocalExtendedStatistics.getControllableProperties();
				List<String> streamingStatusSetParams = new ArrayList<>();
				streamingStatusSetParams.add(VIAConnectProControllingMetric.STREAMING_STATUS_SET.getParam());
				streamingStatusSetParams.add(propertyValue);
				if (!isDualDisplayStreaming()) {
					String currentURL = localStats1.get(String.format("%s#%s", VIAConnectProControllingMetric.STREAMING_STATUS_SET.getGroupName(), VIAConnectProConstant.URL));
					streamingStatusSetParams.add(currentURL);
				} else {
					String urlOne = localStats1.get(String.format("%s#%s", VIAConnectProControllingMetric.STREAMING_STATUS_SET.getGroupName(), VIAConnectProConstant.URL_1));
					String urlTwo = localStats1.get(String.format("%s#%s", VIAConnectProControllingMetric.STREAMING_STATUS_SET.getGroupName(), VIAConnectProConstant.URL_2));
					streamingStatusSetParams.add(urlOne);
					streamingStatusSetParams.add(urlTwo);
				}
				String rawSetStreamingMode = sendTelnetCommand(VIAConnectProControllingMetric.STREAMING_STATUS_SET.getCommand(), streamingStatusSetParams, true);
				String[] splitRawSetStreamingMode = rawSetStreamingMode.split(VIAConnectProConstant.REGEX_VERTICAL_LINE);
				// Example response: Streaming|Set|P2|0/1. 0 is fail, 1 is success
				if (!VIAConnectProConstant.ONE.equals(splitRawSetStreamingMode[splitRawSetStreamingMode.length - 1])) {
					String errorStatus = propertyValue.equals(VIAConnectProConstant.ONE) ? VIAConnectProConstant.ACTIVATE : VIAConnectProConstant.DEACTIVATE;
					String errorMessage = String.format("Fail to set streaming mode to %s.", errorStatus);
					if (rawSetStreamingMode.contains(VIAConnectProConstant.ERROR)) {
						VIAConnectProErrorMetric viaConnectProErrorMetric = VIAConnectProErrorMetric.getByCode(splitRawSetStreamingMode[splitRawSetStreamingMode.length - 1]);
						if (viaConnectProErrorMetric != null) {
							errorMessage += String.format("Error code: %s, description: %s", viaConnectProErrorMetric.getErrorCode(), viaConnectProErrorMetric.getErrorDescription());
						}
					}
					throw new CommandFailureException(this.getAddress(), VIAConnectProControllingMetric.STREAMING_STATUS_SET.getCommand(),
							errorMessage);
				}
				removeCachedStatisticAndControl(cachedStats1, cachedControls1, VIAConnectProControllingMetric.STREAMING_STATUS_SET.getGroupName());
				break;
			case STREAMING_START:
				String streamGroupName = VIAConnectProControllingMetric.STREAMING_START.getGroupName();
				Map<String, String> localStats2 = localExtendedStatistics.getStatistics();
				Map<String, String> cachedStats2 = cachedLocalExtendedStatistics.getStatistics();
				List<AdvancedControllableProperty> cachedControls2 = cachedLocalExtendedStatistics.getControllableProperties();
				String currentAction = localStats2.get(String.format("%s#%s", streamGroupName, VIAConnectProConstant.ACTION));
				List<String> param = new ArrayList<>();
				if (currentAction.equals(VIAConnectProConstant.START) || currentAction.equals(VIAConnectProConstant.STOP)) {
					String command = currentAction.equals(VIAConnectProConstant.START) ? VIAConnectProControllingMetric.STREAMING_START.getCommand() : VIAConnectProControllingMetric.STREAMING_STOP.getCommand();
					String firstParam = currentAction.equals(VIAConnectProConstant.START) ? VIAConnectProControllingMetric.STREAMING_START.getParam() : VIAConnectProControllingMetric.STREAMING_STOP.getParam();
					param.add(firstParam);
					param.add(this.getLogin());
					String rawStartOrStopStream = sendTelnetCommand(command, param, true);
					String[] splitRawStartOrStopStream = rawStartOrStopStream.split(VIAConnectProConstant.REGEX_VERTICAL_LINE);
					// Example response: SStart/SStop|0/1|ID. 0 is fail, 1 is success
					if (!VIAConnectProConstant.ONE.equals(splitRawStartOrStopStream[1])) {
						populateErrorMessageForStreaming(currentAction, this.getLogin(), command, rawStartOrStopStream, splitRawStartOrStopStream);
					}
				} else {
					String command = currentAction.equals(VIAConnectProConstant.RESTART) ? VIAConnectProControllingMetric.STREAMING_RESTART.getCommand() : VIAConnectProControllingMetric.STREAMING_CHANGE.getCommand();
					String firstParam = currentAction.equals(VIAConnectProConstant.RESTART) ? VIAConnectProControllingMetric.STREAMING_RESTART.getParam() : VIAConnectProControllingMetric.STREAMING_CHANGE.getParam();

					param.add(firstParam);
					param.add(this.getLogin());

					if (isDualDisplayStreaming()) {
						String urlName1 = localStats2.get(String.format("%s#%s", streamGroupName, VIAConnectProConstant.NEW_URL_1));
						String urlName2 = localStats2.get(String.format("%s#%s", streamGroupName, VIAConnectProConstant.NEW_URL_2));
						param.add(urlName1);
						param.add(urlName2);
					} else {
						String urlName = localStats2.get(String.format("%s#%s", streamGroupName, VIAConnectProConstant.NEW_URL));
						param.add(urlName);
					}

					if (isDualDisplayStreaming()) {
						String urlName2 = localStats2.get(String.format("%s#%s", streamGroupName, VIAConnectProConstant.NEW_URL_2));
						param.add(urlName2);
					}
					String rawRestartOrChangeStream = sendTelnetCommand(command, param, true);
					String[] splitRawRestartOrChangeStream = rawRestartOrChangeStream.split(VIAConnectProConstant.REGEX_VERTICAL_LINE);
					// Example response: Streaming|SRestart/SChange|0/1. 0 is fail, 1 is success
					if (!VIAConnectProConstant.ONE.equals(splitRawRestartOrChangeStream[2])) {
						populateErrorMessageForStreaming(currentAction, this.getLogin(), command, rawRestartOrChangeStream, splitRawRestartOrChangeStream);
						return;
					}
				}
				removeCachedStatisticAndControl(cachedStats2, cachedControls2, streamGroupName);
				break;
			default:
				throw new IllegalArgumentException("Unexpected value: " + propertyName);
		}
	}

	/**
	 * Populate error message for StreamingControl
	 *
	 * @param currentAction Start/Stop/Change/Restart the stream
	 * @param userName username
	 * @param command  Start/Stop/Change/Restart the stream command
	 * @param rawResponse Raw response
	 * @param splitResponse Split response
	 */
	private void populateErrorMessageForStreaming(String currentAction, String userName, String command, String rawResponse, String[] splitResponse) {
		String errorMessage = String.format("Fail to %s the stream for username: %s.", currentAction, userName);
		if (rawResponse.contains(VIAConnectProConstant.ERROR)) {
			VIAConnectProErrorMetric viaConnectProErrorMetric = VIAConnectProErrorMetric.getByCode(splitResponse[splitResponse.length - 1]);
			if (viaConnectProErrorMetric != null) {
				errorMessage += String.format("Error code: %s, description: %s", viaConnectProErrorMetric.getErrorCode(), viaConnectProErrorMetric.getErrorDescription());
			}
		}
		throw new CommandFailureException(this.getAddress(), command,
				errorMessage);
	}

	/**
	 * Check if configManagement is moderator, if it is invalid string => treat as moderator
	 *
	 * @return boolean value.
	 */
	private boolean isConfigManagement() {
		// If configManagement is null or empty => treat it as Moderator
		if (StringUtils.isNullOrEmpty(this.getConfigManagement())) {
			return false;
		} else
			return this.getConfigManagement().toLowerCase(Locale.ROOT).trim().equals(VIAConnectProConstant.TRUE);
	}

	/**
	 * Check if the adapter is login successfully by sending a command to the device
	 * Command to be sent: Get-Volume
	 *
	 * @return boolean is login or not.
	 */
	private boolean isLogin() throws Exception {
		if(!isChannelConnected()){
			createChannel();
		}
		String response = this.internalSend(buildTelnetRequest(VIAConnectProMonitoringMetric.VOLUME.getCommand(), Collections.singletonList(VIAConnectProMonitoringMetric.VOLUME.getParam()), false));
		boolean isLoginSuccess = response.endsWith(VIAConnectProConstant.END_COMMAND);

		if(!isLoginSuccess){
			logger.error("VIAConnectProCommunicator: Telnet connection to " + host + " cannot be established");
		}
		return isLoginSuccess;
	}

	/**
	 * Create switch
	 *
	 * @param name String name of the switch
	 * @param status status of the switch (On/Off)
	 * @param labelOff String - Off label
	 * @param labelOn String - On label
	 * @return Instance of AdvancedControllableProperty
	 */
	private AdvancedControllableProperty createSwitch(String name, int status, String labelOff, String labelOn) {
		AdvancedControllableProperty.Switch toggle = new AdvancedControllableProperty.Switch();
		toggle.setLabelOff(labelOff);
		toggle.setLabelOn(labelOn);

		return new AdvancedControllableProperty(name, new Date(), toggle, status);
	}

	/**
	 * Create drop-down
	 *
	 * @param name String name of the drop-down
	 * @param values list of values
	 * @param initialValue String initial value
	 * @return Instance of AdvancedControllableProperty
	 */
	private AdvancedControllableProperty createDropdown(String name, List<String> values, String initialValue) {
		AdvancedControllableProperty.DropDown dropDown = new AdvancedControllableProperty.DropDown();
		dropDown.setOptions(values.toArray(new String[0]));
		dropDown.setLabels(values.toArray(new String[0]));
		return new AdvancedControllableProperty(name, new Date(), dropDown, initialValue);
	}

	/**
	 * Create a controllable property Text
	 *
	 * @param name the name of property
	 * @param stringValue character string
	 * @return Instance of AdvancedControllableProperty Text instance
	 */
	private AdvancedControllableProperty createText(String name, String stringValue) {
		AdvancedControllableProperty.Text text = new AdvancedControllableProperty.Text();
		return new AdvancedControllableProperty(name, new Date(), text, stringValue);
	}

	/**
	 * Instantiate Text controllable property
	 *
	 * @param name name of the property
	 * @param label default button label
	 * @return instance of AdvancedControllableProperty with AdvancedControllableProperty.Button as type
	 */
	private AdvancedControllableProperty createButton(String name, String label, String labelPressed) {
		AdvancedControllableProperty.Button button = new AdvancedControllableProperty.Button();
		button.setLabel(label);
		button.setLabelPressed(labelPressed);
		button.setGracePeriod(0L);
		return new AdvancedControllableProperty(name, new Date(), button, VIAConnectProConstant.EMPTY);
	}

	/**
	 * Create slider
	 *
	 * @param name String Name of the slider
	 * @param labelStart String start label of the slider
	 * @param labelEnd String end label of the slider
	 * @param rangeStart Float range start
	 * @param rangeEnd Float range end
	 * @param initialValue Float initial value
	 * @return Instance of AdvancedControllableProperty
	 */
	private AdvancedControllableProperty createSlider(String name, String labelStart, String labelEnd, Float rangeStart, Float rangeEnd, Float initialValue) {
		AdvancedControllableProperty.Slider slider = new AdvancedControllableProperty.Slider();
		slider.setLabelStart(labelStart);
		slider.setLabelEnd(labelEnd);
		slider.setRangeStart(rangeStart);
		slider.setRangeEnd(rangeEnd);

		return new AdvancedControllableProperty(name, new Date(), slider, initialValue);
	}
}