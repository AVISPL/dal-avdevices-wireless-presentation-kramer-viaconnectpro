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
import com.avispl.symphony.dal.avdevices.wireless.presentation.kramer.viaconnectpro.utils.VIAConnectProConstant;
import com.avispl.symphony.dal.avdevices.wireless.presentation.kramer.viaconnectpro.utils.VIAConnectProMetric;
import com.avispl.symphony.dal.communicator.TelnetCommunicator;

import java.util.*;

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
 * 	<li>Room Code Settings</li>
 * 	<li>Room Name Settings</li>
 * 	<li>Datetime Display Status</li>
 * 	<li>Presentation Mode Status</li>
 * 	<li>Log Mod Status</li>
 * 	<li>Quick Client Access Status</li>
 * 	<li>Gateway Serial Number</li>
 * 	<li>Gateway Mac Address</li>
 * 	<li>Gateway Version</li>
 * 	<li>Chrome Status</li>
 * 	<li>Room Overlay Status</li>
 * 	<li>Audio Devices</li>
 * 	<li>Streaming</li>
 * 	<li>Part Present Confirm</li>
 * </ol>
 * <p>
 * Controlling:
 * <ol>
 * 	<li>Set Display Status</li>
 * 	<li>Set Volume</li>
 * 	<li>Change Room Code Settings</li>
 * 	<li>Change Room Name Settings</li>
 * 	<li>Set Datetime Status</li>
 * 	<li>Set Presentation Mode Status</li>
 * 	<li>Set Log Mode Status</li>
 * 	<li>Set Quick Client Access Status</li>
 * 	<li>User Computer: Option to control user's computer from gateway</li>
 * 	<li>Set DND Mode</li>
 * 	<li>Kick off user</li>
 * 	<li>Set Screen Share Status</li>
 * 	<li>Set Chrome Status</li>
 * 	<li>Set Room Overlay Status</li>
 * 	<li>Streaming(start/stop/restart/change)</li>
 * 	<li>StreamingURL: open network stream</li>
 * 	<li>Change whiteboard settings</li>
 * 	<li>Set part present confirm status</li>
 * 	<li>Wifi Guest Mode</li>
 * 	</ol>
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 3/14/2022
 * @since 1.0.0
 */
public class VIAConnectProCommunicator extends TelnetCommunicator implements Monitorable, Controller {

	private ExtendedStatistics localExtendedStatistics;

	/**
	 * VIAConnectProCommunicator constructor
	 */
	public VIAConnectProCommunicator() {
		this.setCommandSuccessList(
				Collections.singletonList("A"));
		this.setCommandErrorList(Collections.singletonList("A"));
		this.setLoginSuccessList(Collections.singletonList(">"));
	}


	@Override
	public void controlProperty(ControllableProperty controllableProperty) throws Exception {
	}

	@Override
	public void controlProperties(List<ControllableProperty> list) throws Exception {
		if (CollectionUtils.isEmpty(list)) {
			throw new IllegalArgumentException("Controllable properties cannot be null or empty");
		}
		for (ControllableProperty controllableProperty : list) {
			controlProperty(controllableProperty);
		}
	}

	@Override
	public List<Statistics> getMultipleStatistics() throws Exception {
		ExtendedStatistics extendedStatistics = new ExtendedStatistics();
		Map<String, String> statistics = new HashMap<>();
		List<AdvancedControllableProperty> advancedControllablePropertyList = new ArrayList<>();
		populateStatistics(statistics, advancedControllablePropertyList);
		extendedStatistics.setStatistics(statistics);
		extendedStatistics.setControllableProperties(advancedControllablePropertyList);
		localExtendedStatistics = extendedStatistics;
		return Collections.singletonList(localExtendedStatistics);
	}

	/**
	 * Populate statistics(get from the device or default statistics to control) and controlling properties
	 *
	 * @param statistics Map of statistics
	 * @param controls List of AdvancedControllableProperty
	 */
	private void populateStatistics(Map<String, String> statistics, List<AdvancedControllableProperty> controls) throws Exception {
		Set<String> groupNames = VIAConnectProMetric.getGroupNames();
		for (String groupName : groupNames) {
			switch (groupName) {
				case "ParticipantList":
					populateParticipantGroup(statistics);
					break;
				case "DisplayStatus":
					populateDisplayStatus(statistics, controls);
					break;
				case "Volume":
					populateVolume(statistics, controls);
					break;
				case "IpInformation":
					populateIpInformation(statistics);
					break;
				case "RoomCodeSettings":
					populateRoomCode(statistics, controls);
					break;
				case "RoomNameSettings":
					populateRoomName(statistics, controls);
					break;
				case "DatetimeDisplayStatus":
					populateDatetime(statistics, controls);
					break;
				case "PresentationModeStatus":
					populatePresentationMode(statistics, controls);
					break;
				case "LogModeStatus":
					populateLogMode(statistics, controls);
					break;
				case "QuickClientAccessStatus":
					populateQuickClientAccess(statistics, controls);
					break;
				case "UserComputer":
					populateUserComputerControl(statistics, controls);
					break;
				case "DND":
					populateDND(statistics, controls);
					break;
				case "KickOff":
					populateKickOff(statistics, controls);
					break;
				case "ScreenShareStatus":
					populateScreenShare(statistics, controls);
					break;
				case "GatewaySerialNumber":
					populateGatewaySerialNumber(statistics);
					break;
				case "GatewayMacAddress":
					populateMacAddress(statistics);
					break;
				case "GatewayVersion":
					populateGatewayVersion(statistics);
					break;
				case "Chrome":
					populateChromeStatus(statistics, controls);
					break;
				case "RoomOverlay":
					populateRoomOverlay(statistics, controls);
					break;
				case "AudioDevices":
					populateAudioDevices(statistics);
					break;
				case "Streaming":
				case "StreamingURL":
					populateStreamingStatus(statistics, controls);
					break;
				case "StreamingControl":
					populateStreamingControl(statistics, controls);
					break;
				case "WhiteBoard":
					populateWhiteBoard(statistics, controls);
					break;
				case "PartPresentConfirmStatus":
					populatePartPresentConfirm(statistics, controls);
					break;
				case "WifiGuestMode":
					populateWifiGuestMode(statistics, controls);
					break;
				default:
					//
			}
		}
	}

	/**
	 * Get list of participant (number of logged-in user, username-status)
	 * This command might be used multiple time to make sure the list of usernames is always up-to-date
	 *
	 * @return ParticipantListDTO DTO of participant list
	 * @throws Exception When fail to get telnet response
	 */
	private ParticipantListDTO getListParticipant() throws Exception {
		ParticipantListDTO participantListDTO = new ParticipantListDTO();
		List<String> param1 = Arrays.asList(VIAConnectProMetric.PLIST_CNT.getParam().split(","));
		String rawNumberOfUsers = sendTelnetCommand(VIAConnectProMetric.PLIST_CNT.getCommand(), param1, false);
		int numberOfUsers = Integer.parseInt(rawResponseHandling(rawNumberOfUsers));
		participantListDTO.setLoggedInUsers(numberOfUsers);
		if (numberOfUsers == 0) {
			participantListDTO.setUserAndStatusMap(new HashMap<>());
			return participantListDTO;
		}
		List<String> param2 = Arrays.asList(VIAConnectProMetric.PLIST_All_STATUS.getParam().split(","));
		// the output should be: Plist|all|4|jolly_0#mike_1#Smith_2
		String rawUserNames = sendTelnetCommand(VIAConnectProMetric.PLIST_All_STATUS.getCommand(), param2, false);
		String[] usernamesAndStatus = rawResponseHandling(rawUserNames).split("#");
		Map<String, String> userNameAndStatusMap = new HashMap<>();
		for (String usernameAndStatus : usernamesAndStatus) {
			String username = usernameAndStatus.split("_")[0];
			String status = usernameAndStatus.split("_")[1];
			userNameAndStatusMap.put(username, status);
		}
		participantListDTO.setUserAndStatusMap(userNameAndStatusMap);
		return participantListDTO;
	}

	/**
	 * Populate ParticipantList group
	 *
	 * @param stats Map of statistics
	 * @throws Exception When fail to get participant list
	 */
	private void populateParticipantGroup(Map<String, String> stats) throws Exception {
		String groupName = VIAConnectProMetric.PLIST_All_STATUS.getGroupName();
		ParticipantListDTO participantListDTO = getListParticipant();
		stats.put(String.format("%s#CurrentLoggedInUsers", groupName), String.valueOf(participantListDTO.getLoggedInUsers()));
		int i = 0;
		for (Map.Entry<String, String> entry : participantListDTO.getUserAndStatusMap().entrySet()) {
			i++;
			stats.put(String.format("%s#Participant%s", groupName, (i + 1)), entry.getKey());
			stats.put(String.format("%s#Participant%sStatus", groupName, (i + 1)), entry.getValue());
		}
	}

	/**
	 * Populate statistics and controls for DisplayStatus group
	 *
	 * @param stats Map of statistics
	 * @param controls List of AdvancedControllableProperty
	 * @throws Exception When fail to get Participant DTO and display status
	 */
	private void populateDisplayStatus(Map<String, String> stats, List<AdvancedControllableProperty> controls) throws Exception {
		String groupName = VIAConnectProMetric.DISPLAY_STATUS_GET.getGroupName();
		List<String> usernames = new ArrayList<>();
		if (!isValidUsernameListAndPopulateList(stats, controls, groupName, usernames)) {
			return;
		}

		List<String> param = new ArrayList<>();
		param.add(VIAConnectProMetric.DISPLAY_STATUS_GET.getParam());
		param.add(usernames.get(0));
		// the output should be: Plist|all|4|jolly_0#mike_1#Smith_2
		String rawDisplayStatus = sendTelnetCommand(VIAConnectProMetric.DISPLAY_STATUS_GET.getCommand(), param, false);
		String displayStatus = rawResponseHandling(rawDisplayStatus);
		stats.put(String.format("%s#UserDisplayStatus", groupName), displayStatus);
		stats.put(String.format("%s#UserPresentation", groupName), "");

		List<String> statuses = new ArrayList<>();
		statuses.add("On");
		statuses.add("Off");
		statuses.add("Deny");
		controls.add(createDropdown(String.format("%s#UserPresentation", groupName), statuses, "On"));
		stats.put(String.format("%s#Apply", groupName), "");
		controls.add(createButton(String.format("%s#Apply", groupName), VIAConnectProConstant.APPLY, "Applying..."));
	}

	/**
	 * Populate monitoring and controlling properties for RoomCodeSettings (RCOnDE)
	 *
	 * @param stats Map of statistics
	 * @param controls List of AdvancedControllableProperty
	 * @throws Exception When fail to get active status, appear status, room code, refresh time
	 */
	private void populateRoomCode(Map<String, String> stats, List<AdvancedControllableProperty> controls) throws Exception {
		String groupName = VIAConnectProMetric.ROOM_CODE_SETTINGS_GET_ACTIVE_STATUS.getGroupName();
		List<String> param = Arrays.asList(VIAConnectProMetric.ROOM_CODE_SETTINGS_GET_ACTIVE_STATUS.getParam().split(","));
		// the output should be: RCode|Get|ActiveStatus|0/1
		String rawActiveStatus = sendTelnetCommand(VIAConnectProMetric.ROOM_CODE_SETTINGS_GET_ACTIVE_STATUS.getCommand(), param, false);
		String activeStatus = rawResponseHandling(rawActiveStatus);
		stats.put(String.format("%s#ActiveStatus", groupName), "");
		controls.add(createSwitch(String.format("%s#ActiveStatus", groupName), Integer.parseInt(activeStatus), "Disable", "Enable"));

		// the output should be: RCode|Get|AppearStatus|0/1
		List<String> param2 = Arrays.asList(VIAConnectProMetric.ROOM_CODE_SETTINGS_GET_APPEAR_STATUS.getParam().split(","));
		String rawAppearStatus = sendTelnetCommand(VIAConnectProMetric.ROOM_CODE_SETTINGS_GET_APPEAR_STATUS.getCommand(), param2, false);
		String appearStatus = rawResponseHandling(rawAppearStatus);
		stats.put(String.format("%s#AppearStatus", groupName), "");
		controls.add(createSwitch(String.format("%s#AppearStatus", groupName), Integer.parseInt(appearStatus), "Disable", "Enable"));

		List<String> param3 = Arrays.asList(VIAConnectProMetric.ROOM_CODE_SETTINGS_GET_CODE.getParam().split(","));
		String rawCode = sendTelnetCommand(VIAConnectProMetric.ROOM_CODE_SETTINGS_GET_CODE.getCommand(), param3, false);
		String code = rawResponseHandling(rawCode);
		stats.put(String.format("%s#Code", groupName), code);

		List<String> param4 = Arrays.asList(VIAConnectProMetric.ROOM_CODE_SETTINGS_GET_RTIME.getParam().split(","));
		String rawRefreshTime = sendTelnetCommand(VIAConnectProMetric.ROOM_CODE_SETTINGS_GET_RTIME.getCommand(), param4, false);
		String refreshTime = rawResponseHandling(rawRefreshTime);
		stats.put(String.format("%s#RefreshTime(minutes)", groupName), "");
		controls.add(createText(String.format("%s#RefreshTime(minutes)", groupName), refreshTime));
	}

	private void populateRoomName(Map<String, String> stats, List<AdvancedControllableProperty> controls) throws Exception {
		String groupName = VIAConnectProMetric.ROOM_NAME_SETTINGS_GET.getGroupName();
		List<String> param = Arrays.asList(VIAConnectProMetric.ROOM_NAME_SETTINGS_GET.getParam().split(","));
		String rawRoomNameStatus = sendTelnetCommand(VIAConnectProMetric.ROOM_NAME_SETTINGS_GET.getCommand(), param, false);
		String roomNameStatus = rawResponseHandling(rawRoomNameStatus);
		stats.put(String.format("%s#Status", groupName), "");
		controls.add(createSwitch(String.format("%s#Status", groupName), Integer.parseInt(roomNameStatus), "Disable", "Enable"));

		List<String> param2 = Arrays.asList(VIAConnectProMetric.ROOM_NAME_SETTINGS_GET.getParam().split(","));
		String rawRoomName = sendTelnetCommand(VIAConnectProMetric.ROOM_NAME_SETTINGS_NAME.getCommand(), param2, false);
		String roomName = rawResponseHandling(rawRoomName);
		stats.put(String.format("%s#Name", groupName), "");
		controls.add(createText(String.format("%s#Name", groupName), roomName));
	}

	/**
	 * Populate monitoring and controlling properties for DND group
	 * - The value of DNDMode's switch is not the default value.
	 *
	 * @param stats Map of statistics
	 * @param controls List of AdvancedControllableProperty
	 * @throws Exception When fail to get participant list.
	 */
	private void populateDND(Map<String, String> stats, List<AdvancedControllableProperty> controls) throws Exception {
		String groupName = VIAConnectProMetric.DND_SET.getGroupName();
		List<String> usernames = new ArrayList<>();
		if (!isValidUsernameListAndPopulateList(stats, controls, groupName, usernames)) {
			return;
		}
		stats.put(String.format("%s#DNDMode", groupName), "");
		controls.add(createSwitch(String.format("%s#DNDMode", groupName), 0, "Disable", "Enable"));
		stats.put(String.format("%s#Apply", groupName), "");
		controls.add(createButton(String.format("%s#Apply", groupName), VIAConnectProConstant.APPLY, "Applying..."));
	}

	private void populateUserComputerControl(Map<String, String> stats, List<AdvancedControllableProperty> controls) throws Exception {
		String groupName = VIAConnectProMetric.USER_COMPUTER_CONTROL.getGroupName();
		List<String> usernames = new ArrayList<>();
		if (!isValidUsernameListAndPopulateList(stats, controls, groupName, usernames)) {
			return;
		}
		stats.put(String.format("%s#UserControl", groupName), "");
		controls.add(createSwitch(String.format("%s#UserControl", groupName), 1, "Disable", "Enable"));
		stats.put(String.format("%s#%s", groupName, VIAConnectProConstant.APPLY), "");
		controls.add(createButton(String.format("%s#%s", groupName, VIAConnectProConstant.APPLY), VIAConnectProConstant.APPLY, "Applying..."));
	}

	private void populateKickOff(Map<String, String> stats, List<AdvancedControllableProperty> controls) throws Exception {
		String groupName = VIAConnectProMetric.KICK_OFF.getGroupName();
		List<String> usernames = new ArrayList<>();
		if (!isValidUsernameListAndPopulateList(stats, controls, groupName, usernames)) {
			return;
		}
		stats.put(String.format("%s#%s", groupName, VIAConnectProConstant.KICK), "");
		controls.add(createButton(String.format("%s#%s", groupName, VIAConnectProConstant.KICK), VIAConnectProConstant.KICK, "Kicking user ..."));
	}

	private void populateStreamingControl(Map<String, String> stats, List<AdvancedControllableProperty> controls) throws Exception {
		String groupName = VIAConnectProMetric.STREAMING_START.getGroupName();
		List<String> usernames = new ArrayList<>();
		if (!isValidUsernameListAndPopulateList(stats, controls, groupName, usernames)) {
			return;
		}
		stats.put(String.format("%s#Action", groupName), "");
		List<String> streamModes = new ArrayList<>();
		streamModes.add(VIAConnectProConstant.START);
		streamModes.add(VIAConnectProConstant.STOP);
		streamModes.add(VIAConnectProConstant.RESTART);
		streamModes.add(VIAConnectProConstant.CHANGE);
		controls.add(createDropdown(String.format("%s#Action", groupName), streamModes, VIAConnectProConstant.START));

		stats.put(String.format("%s#Apply", groupName), "");
		controls.add(createButton(String.format("%s#Apply", groupName), VIAConnectProConstant.APPLY, "Applying the stream..."));
	}

	private void populateWifiGuestMode(Map<String, String> stats, List<AdvancedControllableProperty> controls) throws Exception {
		List<String> param11 = Collections.singletonList(VIAConnectProMetric.PART_PRESET_CONFIRM_GET.getParam());
		String rawWifiGuestMode = sendTelnetCommand(VIAConnectProMetric.PART_PRESET_CONFIRM_GET.getCommand(), param11, false);
		String wifiGuestMode = rawResponseHandling(rawWifiGuestMode);
		stats.put(VIAConnectProMetric.WIFI_GUEST_MODE.getGroupName(), "");
		controls.add(createSwitch(VIAConnectProMetric.WIFI_GUEST_MODE.getGroupName(), Integer.parseInt(wifiGuestMode), VIAConnectProConstant.STOP, VIAConnectProConstant.START));
	}

	private void populatePartPresentConfirm(Map<String, String> stats, List<AdvancedControllableProperty> controls) throws Exception {
		List<String> param10 = Collections.singletonList(VIAConnectProMetric.PART_PRESET_CONFIRM_GET.getParam());
		String rawPartPresetConfirm = sendTelnetCommand(VIAConnectProMetric.PART_PRESET_CONFIRM_GET.getCommand(), param10, false);
		String partPresetConfirm = rawResponseHandling(rawPartPresetConfirm);
		stats.put(VIAConnectProMetric.PART_PRESET_CONFIRM_GET.getGroupName(), "");
		controls.add(createSwitch(VIAConnectProMetric.PART_PRESET_CONFIRM_GET.getGroupName(), Integer.parseInt(partPresetConfirm), "Off", "On"));
	}

	private void populateWhiteBoard(Map<String, String> stats, List<AdvancedControllableProperty> controls) {
		stats.put(String.format("%s#OffModeAnnotation", VIAConnectProMetric.WHITE_BOARD_ON.getGroupName()), "");
		controls.add(createSwitch(String.format("%s#OffModeAnnotation", VIAConnectProMetric.WHITE_BOARD_ON.getGroupName()), 1, "AutoSave", "Discard"));
		stats.put(String.format("%s#Status", VIAConnectProMetric.WHITE_BOARD_ON.getGroupName()), "");
		controls.add(createSwitch(String.format("%s#Status", VIAConnectProMetric.WHITE_BOARD_ON.getGroupName()), 0, "Off", "On"));
		stats.put(String.format("%s#Switch", VIAConnectProMetric.WHITE_BOARD_ON.getGroupName()), "");
		controls.add(createButton(String.format("%s#Switch", VIAConnectProMetric.WHITE_BOARD_ON.getGroupName()), "Switch", "Switching mode..."));
	}

	private void populateAudioDevices(Map<String, String> stats) {
		stats.put(VIAConnectProMetric.AUDIO_DEVICES_GET.getGroupName(), "");
	}

	private void populateChromeStatus(Map<String, String> stats, List<AdvancedControllableProperty> controls) throws Exception {
		List<String> param8 = Collections.singletonList(VIAConnectProMetric.CHROME_STATUS_SET.getParam());
		String rawChromeStatus = sendTelnetCommand(VIAConnectProMetric.CHROME_STATUS_SET.getCommand(), param8, false);
		String chromeStatus = rawResponseHandling(rawChromeStatus);
		stats.put(String.format("%s#ConnectionStatus", VIAConnectProMetric.CHROME_STATUS_SET.getGroupName()), "");
		controls.add(createSwitch(String.format("%s#ConnectionStatus", VIAConnectProMetric.CHROME_STATUS_SET.getGroupName()), Integer.parseInt(chromeStatus), "Off", "On"));

		List<String> param9 = Collections.singletonList(VIAConnectProMetric.CHROME_STATUS_SET.getParam());
		String rawChromeAPIModeStatus = sendTelnetCommand(VIAConnectProMetric.CHROME_STATUS_SET.getCommand(), param9, false);
		String chromeAPIModeStatus = rawResponseHandling(rawChromeAPIModeStatus);
		stats.put(String.format("%s#APIMode", VIAConnectProMetric.CHROME_API_MODE_SET.getGroupName()), "");
		controls.add(createSwitch(String.format("%s#APIMode", VIAConnectProMetric.CHROME_API_MODE_SET.getGroupName()), Integer.parseInt(chromeAPIModeStatus), "non-SSL", "SSL"));
	}

	private void populateGatewayVersion(Map<String, String> stats) throws Exception {
		List<String> param7 = Collections.singletonList(VIAConnectProMetric.GATEWAY_VERSION_GET.getParam());
		String rawGatewayVersion = sendTelnetCommand(VIAConnectProMetric.GATEWAY_VERSION_GET.getCommand(), param7, false);
		String gatewayVersion = rawResponseHandling(rawGatewayVersion);
		stats.put(VIAConnectProMetric.GATEWAY_VERSION_GET.getGroupName(), gatewayVersion);
	}

	private void populateMacAddress(Map<String, String> stats) throws Exception {
		List<String> param6 = Collections.singletonList(VIAConnectProMetric.GATEWAY_MAC_ADDRESS_GET.getParam());
		String rawGatewayMacAddress = sendTelnetCommand(VIAConnectProMetric.GATEWAY_MAC_ADDRESS_GET.getCommand(), param6, false);
		String gatewayMacAddress = rawResponseHandling(rawGatewayMacAddress);
		stats.put(VIAConnectProMetric.GATEWAY_MAC_ADDRESS_GET.getGroupName(), gatewayMacAddress);
	}

	private void populateGatewaySerialNumber(Map<String, String> stats) throws Exception {
		List<String> param5 = Collections.singletonList(VIAConnectProMetric.GATEWAY_SERIAL_NUMBER_GET.getParam());
		String rawGatewaySerialNumber = sendTelnetCommand(VIAConnectProMetric.GATEWAY_SERIAL_NUMBER_GET.getCommand(), param5, false);
		String gatewaySerialNumber = rawResponseHandling(rawGatewaySerialNumber);
		stats.put(VIAConnectProMetric.GATEWAY_SERIAL_NUMBER_GET.getGroupName(), gatewaySerialNumber);
	}

	private void populateScreenShare(Map<String, String> stats, List<AdvancedControllableProperty> controls) {
		stats.put(VIAConnectProMetric.SCREEN_SHARE_STATUS_SET.getGroupName(), "");
		controls.add(createSwitch(VIAConnectProMetric.SCREEN_SHARE_STATUS_SET.getGroupName(), 0, "Off", "On"));
	}

	private void populateQuickClientAccess(Map<String, String> stats, List<AdvancedControllableProperty> controls) throws Exception {
		List<String> param4 = Collections.singletonList(VIAConnectProMetric.QUICK_CLIENT_ACCESS_STATUS_GET.getParam());
		String rawQuickClientAccessStatus = sendTelnetCommand(VIAConnectProMetric.QUICK_CLIENT_ACCESS_STATUS_GET.getCommand(), param4, false);
		String quickClientAccessStatus = rawResponseHandling(rawQuickClientAccessStatus);
		stats.put(VIAConnectProMetric.QUICK_CLIENT_ACCESS_STATUS_GET.getGroupName(), "");
		controls.add(createSwitch(VIAConnectProMetric.QUICK_CLIENT_ACCESS_STATUS_GET.getGroupName(), Integer.parseInt(quickClientAccessStatus), "Disable", "Enable"));
	}

	private void populateLogMode(Map<String, String> stats, List<AdvancedControllableProperty> controls) throws Exception {
		List<String> param3 = Collections.singletonList(VIAConnectProMetric.LOG_MODE_STATUS_GET.getParam());
		String rawLogModeStatus = sendTelnetCommand(VIAConnectProMetric.LOG_MODE_STATUS_GET.getCommand(), param3, false);
		String logModeStatus = rawResponseHandling(rawLogModeStatus);
		stats.put(VIAConnectProMetric.LOG_MODE_STATUS_GET.getGroupName(), "");
		controls.add(createSwitch(VIAConnectProMetric.LOG_MODE_STATUS_GET.getGroupName(), Integer.parseInt(logModeStatus), "Disable", "Enable"));
	}

	private void populatePresentationMode(Map<String, String> stats, List<AdvancedControllableProperty> controls) throws Exception {
		List<String> param2 = Collections.singletonList(VIAConnectProMetric.PRESENTATION_MODE_STATUS_GET.getParam());
		String rawPresentationModeStatus = sendTelnetCommand(VIAConnectProMetric.PRESENTATION_MODE_STATUS_GET.getCommand(), param2, false);
		String presentationModeStatus = rawResponseHandling(rawPresentationModeStatus);
		stats.put(VIAConnectProMetric.PRESENTATION_MODE_STATUS_GET.getGroupName(), "");
		controls.add(createSwitch(VIAConnectProMetric.PRESENTATION_MODE_STATUS_GET.getGroupName(), Integer.parseInt(presentationModeStatus), "Disable", "Enable"));
	}

	private void populateDatetime(Map<String, String> stats, List<AdvancedControllableProperty> controls) throws Exception {
		List<String> param = Collections.singletonList(VIAConnectProMetric.DATETIME_DISPLAY_STATUS_GET.getParam());
		String rawDatetime = sendTelnetCommand(VIAConnectProMetric.DATETIME_DISPLAY_STATUS_GET.getCommand(), param, false);
		String datetime = rawResponseHandling(rawDatetime);
		stats.put(VIAConnectProMetric.DATETIME_DISPLAY_STATUS_GET.getGroupName(), "");
		controls.add(createSwitch(VIAConnectProMetric.DATETIME_DISPLAY_STATUS_GET.getGroupName(), Integer.parseInt(datetime), "Invisible", "Visible"));
	}

	private void populateVolume(Map<String, String> stats, List<AdvancedControllableProperty> controls) throws Exception {
		String rawVolume = sendTelnetCommand(VIAConnectProMetric.VOLUME.getCommand(), Collections.singletonList(VIAConnectProMetric.VOLUME.getParam()), false);
		String volume = rawResponseHandling(rawVolume);
		stats.put(VIAConnectProMetric.VOLUME.getGroupName(), volume);
		controls.add(createSlider("Volume", "0%", "100%", 0f, 100f, Float.valueOf(volume)));
	}

	private void populateIpInformation(Map<String, String> stats) throws Exception {
		String rawIpInformation = sendTelnetCommand(VIAConnectProMetric.IP_INFORMATION.getCommand(), Collections.singletonList(VIAConnectProMetric.IP_INFORMATION.getCommand()), false);
		String[] ipInformation = rawIpInformation.split("\\|");
		stats.put(String.format("%s#IpAddress", VIAConnectProMetric.IP_INFORMATION.getGroupName()), ipInformation[0]);
		stats.put(String.format("%s#SubnetMask", VIAConnectProMetric.IP_INFORMATION.getGroupName()), ipInformation[1]);
		stats.put(String.format("%s#Gateway", VIAConnectProMetric.IP_INFORMATION.getGroupName()), ipInformation[2]);
		stats.put(String.format("%s#DnsServer", VIAConnectProMetric.IP_INFORMATION.getGroupName()), ipInformation[3]);
		stats.put(String.format("%s#HostName", VIAConnectProMetric.IP_INFORMATION.getGroupName()), ipInformation[4]);
	}

	private void populateRoomOverlay(Map<String, String> stats, List<AdvancedControllableProperty> controls) throws Exception {
		List<String> param10 = Collections.singletonList(VIAConnectProMetric.ROOM_OVERLAY_STATUS_GET.getParam());
		String rawRoomOverlayStatus = sendTelnetCommand(VIAConnectProMetric.ROOM_OVERLAY_STATUS_GET.getCommand(), param10, false);
		String roomOverlayStatus = rawResponseHandling(rawRoomOverlayStatus);
		stats.put(String.format("%s#Status", VIAConnectProMetric.ROOM_OVERLAY_STATUS_GET.getGroupName()), "");
		controls.add(createSwitch(String.format("%s#Status", VIAConnectProMetric.ROOM_OVERLAY_STATUS_GET.getGroupName()), Integer.parseInt(roomOverlayStatus), "Off", "On"));

		List<String> param11 = Collections.singletonList(VIAConnectProMetric.ROOM_OVERLAY_STATUS_GET.getParam());
		String rawRoomOverlayAutoHideTime = sendTelnetCommand(VIAConnectProMetric.ROOM_OVERLAY_STATUS_GET.getCommand(), param11, false);
		String roomOverlayAutoHideTime = rawResponseHandling(rawRoomOverlayAutoHideTime);
		stats.put(String.format("%s#AutoHideTime", VIAConnectProMetric.ROOM_OVERLAY_STATUS_GET.getGroupName()), "");
		controls.add(createSwitch(String.format("%s#AutoHideTime", VIAConnectProMetric.ROOM_OVERLAY_STATUS_GET.getGroupName()), Integer.parseInt(roomOverlayAutoHideTime), "Off", "On"));
		stats.put(String.format("%s#Apply", VIAConnectProMetric.ROOM_OVERLAY_STATUS_GET.getGroupName()), "");
		controls.add(createButton(String.format("%s#Apply", VIAConnectProMetric.ROOM_OVERLAY_STATUS_GET.getGroupName()), VIAConnectProConstant.APPLY, "Applying the stream..."));
	}

	private void populateStreamingStatus(Map<String, String> stats, List<AdvancedControllableProperty> controls) throws Exception {
		List<String> param = Collections.singletonList(VIAConnectProMetric.STREAMING_STATUS_GET.getParam());
		String rawStreamingGetResponse = sendTelnetCommand(VIAConnectProMetric.STREAMING_STATUS_GET.getCommand(), param, false);
		String[] streamingGetResponse = rawStreamingGetResponse.split("\\|");

		List<String> param1 = Collections.singletonList(VIAConnectProMetric.STREAMING_STATUS_SSTATUS_GET.getParam());
		String rawStreamingSStatusResponse = sendTelnetCommand(VIAConnectProMetric.STREAMING_STATUS_SSTATUS_GET.getCommand(), param1, false);
		String[] streamingSStatusResponse = rawStreamingSStatusResponse.split("\\|");

		stats.put(String.format("%s#Datetime", VIAConnectProMetric.STREAMING_STATUS_GET.getGroupName()), streamingSStatusResponse[2]);
		stats.put(String.format("%s#URLStatus", VIAConnectProMetric.STREAMING_STATUS_GET.getGroupName()), streamingSStatusResponse[1]);

		int intStreamingGetResponse = Integer.parseInt(streamingGetResponse[2]);
		String statusValue;
		switch (intStreamingGetResponse) {
			case 0:
				statusValue = "No url is being streamed";
				break;
			case 1:
				statusValue = "A URL is being streamed";
				break;
			default:
				statusValue = "";
		}
		stats.put(String.format("%s#Status", VIAConnectProMetric.STREAMING_STATUS_GET.getGroupName()), statusValue);
		if (intStreamingGetResponse == 0) {
			stats.put(String.format("%s#URL", VIAConnectProMetric.STREAMING_STATUS_GET.getGroupName()), "No URL");
		} else {
			if (streamingGetResponse.length == 4) {
				// Single display
				stats.put(String.format("%s#URL", VIAConnectProMetric.STREAMING_STATUS_GET.getGroupName()), streamingGetResponse[3]);
			} else if (streamingGetResponse.length == 5) {
				// Dual display
				stats.put(String.format("%s#URL1", VIAConnectProMetric.STREAMING_STATUS_GET.getGroupName()), streamingGetResponse[3]);
				stats.put(String.format("%s#URL2", VIAConnectProMetric.STREAMING_STATUS_GET.getGroupName()), streamingGetResponse[4]);
			}
		}

		stats.put(String.format("%s#NewStreamURL", VIAConnectProMetric.STREAMING_URL.getGroupName()), "");
		controls.add(createText(String.format("%s#NewStreamURL", VIAConnectProMetric.STREAMING_URL.getGroupName()), "newstreamurl.com"));

		stats.put(String.format("%s#StartNewStream", VIAConnectProMetric.STREAMING_URL.getGroupName()), "");
		controls.add(createButton(String.format("%s#StartNewStream", VIAConnectProMetric.STREAMING_URL.getGroupName()), VIAConnectProConstant.START, "Starting..."));
	}

	/**
	 * Send telnet command to the device.
	 *
	 * @param command Name of the command
	 * @param params Params of the command. Example: <P1>{param 1}</P1> <P2>{param 2}</P2>
	 * @param isControlCommand Check if is control to throw the proper exception.
	 * @return String of raw response
	 * @throws Exception When fail to send the telnet command.
	 */
	private String sendTelnetCommand(String command, List<String> params, boolean isControlCommand) throws Exception {
		try {
			return this.send(buildTelnetRequest(command, params));
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
	 *
	 * @param command Name of the command
	 * @param params List of params
	 * @return String of built telnet request
	 */
	private String buildTelnetRequest(String command, List<String> params) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<P>");
		stringBuilder.append(String.format("<UN>%s</UN>", this.getLogin()));
		stringBuilder.append("<Pwd></Pwd>");
		stringBuilder.append(String.format("<Cmd>%s</Cmd>", command));
		for (int i = 0; i < params.size(); i++) {
			stringBuilder.append(String.format("<P%s>%s<P%s>", i + 1, params.get(i), i + 1));
		}
		stringBuilder.append("</P>");
		return String.valueOf(stringBuilder);
	}

	private String rawResponseHandling(String rawResponse) {
		String[] rawResponseArray = rawResponse.split("\\|");
		return rawResponseArray[rawResponseArray.length - 1];
	}

	/**
	 * Check if there are any usernames in the username list.
	 * And populate statistics and controls if valid.
	 *
	 * @param stats Map of statistics
	 * @param controls List of AdvancedControllableProperty
	 * @param groupName Name of the group
	 * @return boolean
	 * @throws Exception When fail to get participant list
	 */
	private boolean isValidUsernameListAndPopulateList(Map<String, String> stats, List<AdvancedControllableProperty> controls, String groupName, List<String> usernames) throws Exception {
		ParticipantListDTO participantListDTO = getListParticipant();
		if (participantListDTO.getUserAndStatusMap().size() == 0) {
			stats.put(String.format("%s#Username", groupName), "Need at least 1 username.");
			return false;
		}
		for (Map.Entry<String, String> entry : participantListDTO.getUserAndStatusMap().entrySet()) {
			usernames.add(entry.getKey());
		}
		stats.put(String.format("%s#Username", groupName), "");
		controls.add(createDropdown(String.format("%s#Username", groupName), usernames, usernames.get(0)));
		return true;
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
		return new AdvancedControllableProperty(name, new Date(), button, "");
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