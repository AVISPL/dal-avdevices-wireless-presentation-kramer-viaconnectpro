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
import com.avispl.symphony.dal.avdevices.wireless.presentation.kramer.viaconnectpro.utils.VIAConnectProMonitoringMetric;
import com.avispl.symphony.dal.communicator.TelnetCommunicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		populateParticipantGroup(statistics);
		populateDisplayStatus(statistics, controls);
		populateOtherProperties(statistics, controls);

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
		List<String> param1 = Arrays.asList(VIAConnectProMonitoringMetric.PLIST_CNT.getParam().split(","));
		String rawNumberOfUsers = sendTelnetCommand(VIAConnectProMonitoringMetric.PLIST_CNT.getCommand(), param1, false);
		int numberOfUsers = Integer.parseInt(rawResponseHandling(rawNumberOfUsers));
		participantListDTO.setLoggedInUsers(numberOfUsers);
		if (numberOfUsers == 0) {
			participantListDTO.setUserAndStatusMap(new HashMap<>());
			return participantListDTO;
		}
		List<String> param2 = Arrays.asList(VIAConnectProMonitoringMetric.PLIST_All_STATUS.getParam().split(","));
		// the output should be: Plist|all|4|jolly_0#mike_1#Smith_2
		String rawUserNames = sendTelnetCommand(VIAConnectProMonitoringMetric.PLIST_All_STATUS.getCommand(), param2, false);
		String[] usernamesAndStatus = rawResponseHandling(rawUserNames).split("#");
		Map<String, String> userNameAndStatusMap = new HashMap<>();
		for (String usernameAndStatus: usernamesAndStatus) {
			String username = usernameAndStatus.split("_")[0];
			String status = usernameAndStatus.split("_")[1];
			userNameAndStatusMap.put(username,status);
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
		ParticipantListDTO participantListDTO = getListParticipant();
		stats.put(String.format("%s#CurrentLoggedInUsers", VIAConnectProMonitoringMetric.PLIST_All_STATUS.getName()), String.valueOf(participantListDTO.getLoggedInUsers()));
		int i = 0;
		for (Map.Entry<String,String> entry : participantListDTO.getUserAndStatusMap().entrySet()) {
			i++;
			stats.put(String.format("%s#Participant%s", VIAConnectProMonitoringMetric.PLIST_All_STATUS.getName(),(i+1)), entry.getKey());
			stats.put(String.format("%s#Participant%sStatus",VIAConnectProMonitoringMetric.PLIST_All_STATUS.getName(),(i+1)), entry.getValue());
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
		ParticipantListDTO participantListDTO = getListParticipant();
		List<String> values = new ArrayList<>();
		if (participantListDTO.getUserAndStatusMap().size() == 0) {
			stats.put(String.format("%s#UserDisplayStatus",VIAConnectProMonitoringMetric.DISPLAY_STATUS_GET.getName()), "Need at least 1 username to get the status.");
			return;
		}
		for (Map.Entry<String,String> entry : participantListDTO.getUserAndStatusMap().entrySet()) {
			values.add(entry.getKey());
		}
		stats.put(String.format("%s#Username", VIAConnectProMonitoringMetric.DISPLAY_STATUS_GET.getName()), "");
		controls.add(createDropdown(String.format("%s#Username", VIAConnectProMonitoringMetric.DISPLAY_STATUS_GET.getName()), values, values.get(0)));

		List<String> param = new ArrayList<>();
		param.add(VIAConnectProMonitoringMetric.DISPLAY_STATUS_GET.getParam());
		param.add(values.get(0));
		// the output should be: Plist|all|4|jolly_0#mike_1#Smith_2
		String rawDisplayStatus = sendTelnetCommand(VIAConnectProMonitoringMetric.DISPLAY_STATUS_GET.getCommand(), param, false);
		String displayStatus = rawResponseHandling(rawDisplayStatus);
		stats.put(String.format("%s#UserDisplayStatus",VIAConnectProMonitoringMetric.DISPLAY_STATUS_GET.getName()), displayStatus);
		stats.put(String.format("%s#UserPresentation",VIAConnectProMonitoringMetric.DISPLAY_STATUS_GET.getName()), "");

		List<String> statuses = new ArrayList<>();
		statuses.add("On");
		statuses.add("Off");
		statuses.add("Deny");
		controls.add(createDropdown(String.format("%s#UserPresentation",VIAConnectProMonitoringMetric.DISPLAY_STATUS_GET.getName()), statuses, "On"));
		stats.put(String.format("%s#Apply",VIAConnectProMonitoringMetric.DISPLAY_STATUS_GET.getName()), "");
		controls.add(createButton(String.format("%s#Apply",VIAConnectProMonitoringMetric.DISPLAY_STATUS_GET.getName()), VIAConnectProConstant.APPLY, "Applying..."));
	}

	private void populateOtherProperties(Map<String, String> stats, List<AdvancedControllableProperty> controls) throws Exception {
		// 3. Volume + Control
		String rawVolume = sendTelnetCommand(VIAConnectProMonitoringMetric.VOLUME.getCommand(), Collections.singletonList(VIAConnectProMonitoringMetric.VOLUME.getParam()), false);
		String volume = rawResponseHandling(rawVolume);
		stats.put("Volume", volume);
		controls.add(createSlider("Volume", "0%", "100%", 0f, 100f, Float.valueOf(volume)));
		// 4.IP Information
		String rawIpInformation = sendTelnetCommand(VIAConnectProMonitoringMetric.IP_INFORMATION.getCommand(), Collections.singletonList(VIAConnectProMonitoringMetric.IP_INFORMATION.getCommand()), false);
		String[] ipInformation = rawIpInformation.split("\\|");
		stats.put("IpInformation#IpAddress", ipInformation[0]);
		stats.put("IpInformation#SubnetMask", ipInformation[1]);
		stats.put("IpInformation#Gateway", ipInformation[2]);
		stats.put("IpInformation#DnsServer", ipInformation[3]);
		stats.put("IpInformation#HostName", ipInformation[4]);
	}

	/**
	 * Send telnet command to the device.
	 *
	 * @param command Name of the command
	 * @param params Params of the command. Example: <P1>{param 1}</P1> <P2>{param 2}</P2>
	 * @param isControlCommand  Check if is control to throw the proper exception.
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
}