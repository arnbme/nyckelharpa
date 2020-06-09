/**
 *  Update Url:   https://raw.githubusercontent.com/arnbme/nyckelharpa/master/Centralite-Keypad.groovy
 *
 *  Centralite Keypad
 *
 *  Copyright 2015-2016 Mitch Pond, Zack Cornelius
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *	Jun 08, 2020 v1.0.3	allow temperature decimal digits 0, 1, 2 and adjust Celcius and Farenheit accordingly
 *	Apr 25, 2020 v1.0.2	add missing routine getArmCmd used only with logtrace and bad LCM pin
 * 	Apr 25, 2020 v1.0.2 fix for UEI keypad
 *							Use hardware command vs beep(0) for Off() command
 * 	Apr 02, 2020 v1.0.1 Error missing SetExitDelay routine. Add setExitDelay routine for compatability with HE HSM not used by this module.
 * 	Mar 25, 2020 v1.0.0 Add support for HE pins and pin processing
 * 	Mar 24, 2020 v0.2.9 Symptom Iris V3 device goes into hardware motion loop during panic or siren sounding
 *							Fixed in sendPanelResponse(): sends a response that does not stop siren, kill keys. or create hardware motion messages
 *						Add support for new beep sound in Iris V2 and V3 Depending on firmware my need to use old beep sound
 *						Add support for battery type on Iris V3 and XHK1-UE keypads
 * 	Mar 23, 2020 v0.2.8 Symptom Iris V3 Siren stops when motion occurs on device and sendPanelResponse responds
 *							Fixed in set and unset active siren boolean, then test for active siren in sendPanelResponse
 * 	Mar 21, 2020 v0.2.7 Symptom Iris V3 light status did not match HSM status.
 * 							Fixed in routine sendPanelResponse. Use hsmStatus for Iris V3 motion response
 *						Symptom Erratic or missing entry and exit delay tones.
 *							Fixed in routine sendPanelResponse and setEntryDelay
 *						Change Refresh routine: Set device's Current States motion to inactive
 * 	Mar 19, 2020 v0.2.6 Symptom Iris V3 battery short life, V3 loses system arm state.
 *										  Cause Iris V3 motion message loop
 *										  Fix: add logic from HE Iris V3 DH
 *										  Decided not to support alternate Beep tones for now.
 *										  Exit, Entry and Beep sounds are erratic on the V3, Siren works
 * 	Feb 03, 2020 v0.2.5 When device not removed from HSM, causes HSM Command "entry" error and app fails to function.
 *											Add command "entry" statement and issue log warning
 * 	Jan 04, 2020 v0.2.4 Set flag for Iris 2 & 3 allowing Partial Key to set Home or Night arming state
 * 	Jan 04, 2020 v0.2.4 Disable webSocket for live production module
 * 	Dec 30, 2019 v0.2.3 Add support and fingerprint for Iris V3 keypad. Does not send pin on ON(Away) or Partial (Night)
 * 	Jun 17, 2019 v0.2.2 need to handle ping from this client to server in the socketChat.php module.
 *						for now disabled the ping from this module by setting pingInterval to -1
 * 	Jun 14, 2019 v0.2.2 add webSocket client logic
 * 	Jun 07, 2019 v0.2.1 add ssekey command used to pass javascript communication ssekey from keypad simulator
 * 	Jun 02, 2019 v0.2.1 add attribue pinStatus to return pin status on Maker API call
 * 	May 23, 2019 v0.2.1 allow Maker API for internet keypad.
 * 	May 21, 2019 v0.2.0 use updateDataValue to store deviceVersion in device Data. Used with Nyckelharpa version testing.
 *							calling version from an external app returns null (sigh) known issue not going to be fixed
 *							add Command "version" allowing external call
 *							version() return works when called from device code
 * 	May 18, 2019 v0.1.9 Make existing routine panicCommand useable from external modules
 *							Used by Nyckelharpa for Panic Pin procssing
 *							Add Enable Panic setting similar HE Device Handlers, unable to get setting name
 * 	May 15, 2019 v0.1.8 For 3400 and UEI, change off command to issue beep(0)
 *						Show Version as an unused setting. No other way to get it to show AFAIK
 * 	May 11, 2019 v0.1.7 Add support for UEI keypad device
 *					added UEI XHK1 fingerprint but did not test if DTH is assigned correctly
 *					added Steve Jackson's changed battery reference voltages to accommodate the higher voltage of
 *					of the UEI XHK1 keypad.  Changed voltages from 3.5 to 7.2 (voltage too high),
 *					3.5 to 5.2 (MinVolts), 3.0 to 6.8 (MaxVolts).
 *	May 11, 2019 added version number, initially set to 0.1.7 17 changes from initial porting
 *	May 11, 2019 Generate beeps for 3400-G Keypad, Centralite does not respond to Siren Command
 *	May 05, 2019 Restore button capability as pushableButton, capability ContactSensor add update Url
 *	Apr 30, 2019 HSM hijacked command setExitDelay to send all HSM delay to keypad
 *								avoid confusion by changing ours to setExitAway
 *  Apr 29, 2019 Arn Burkhoff Updated siren and off commands
 *  Apr 29, 2019 Arn Burkhoff added commands setExitNight setExitStay, capability Alarm.
 *							When Panic entered, internally issue siren command
 *  Apr 24, 2019 Mitch Pond fixed Temperature and converted module to HE structure
 *  Apr 22, 2019 changed battery back to % from volts for users. Temperature is still very wrong
 *  Mar 31, 2019 routine disarm and others issued multiple times. fixed in other modules
 *  Mar 31, 2019 routine disarm threw an error caused by HE sending a delay value, add delay parm that is ignored
 *  Mar 31, 2019 deprecate Sep 20, 2018 change, HE should be fast enough for proper acknowledgements
 *  Feb 26, 2019 in sendRawStatus set seconds to Integer or it fails
 *	Feb 26, 2019 HE device.currentValue gives value at start of event, use true as second parameter to get live actual value
 *  Feb 25, 2019 kill setmodehelper on detentrydelay and setexitdelay commands, mode help sets mode icon lights
 *					error found in Hubitat with entry delay
 *  Feb 22, 2019 convertToHexString default in ST not available in HE, change width to 2
 *  Feb 21, 2019 V1.0.1 Hubitat command names vary from Smartthings, add additional commands
 *  -------------------------Porting to Hubitat starts around here----------------------------------------
 *  Sep 20, 2018 per ST tech support. Issue acknowlegement in HandleArmRequest
 *               disable routines: acknowledgeArmRequest and sendInvalidKeycodeResponse allowing SHM Delay to have no code changes
 *
 *  Sep 18, 2018 comment out health check in an attempt to fix timout issue  (no improvement)
 *  Sep 04, 2018 add health check and vid for new phone app.
 *  Mar 25, 2018 add volts to battery message
 *  Aug 25, 2017 deprecate change of Jul 12, 2017, change from Jul 25 & 26, 2017 remains but is no longer needed or used
 *  Jul 26, 2017 Stop entryDelay from updating field lastUpdate or alarm is not triggered in CoRE
 *			pistons that assume a time change means alarm mode(off or on) was reset
 *  Jul 25, 2017 in formatLocalTime add seconds to field lastUpdate.
 * 			need seconds to catch a rearm within the open time delay in Core Front Door Opens piston
 * 			otherwise alarm sounds after rearm
 *  Jul 12, 2017 in sendStatustoDevice light Night button not HomeStay button (no such mode in SmartHome)
 */
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
metadata {
	definition (name: "Centralitex Keypad", namespace: "mitchpond", author: "Mitch Pond", vid: "generic-motion") {
		capability "SecurityKeypad"
		capability "Alarm"
		capability "Battery"
		capability "Configuration"
        capability "Motion Sensor"
		capability "Sensor"
		capability "Temperature Measurement"
		capability "Refresh"
		capability "Lock Codes"
		capability "Tamper Alert"
		capability "Tone"
		capability "PushableButton"
//      capability "polling"
		capability "ContactSensor"

		attribute "armMode", "String"
        attribute "lastUpdate", "String"
        attribute "pinStatus", "String"

		command "setDisarmed"
		command "setArmedAway"
		command "setArmedStay"
		command "setArmedNight"
		command "setExitAway", ['number']		//this was setExitDelay in ST
		command "setExitStay", ['number']
		command "setExitNight", ['number']		//defined by Hubitat capability
//		command "setEntryDelay", ['number']		//issue same hardware command as Beep command defined by Hubitat capability
		command "testCmd", ['number']
		command "sendInvalidKeycodeResponse"
		command "acknowledgeArmRequest",['number']
		command "panicContact"
		command "version"
		command "ssekey",["string"]
		command "armCode",["string"]
		command "pinStatusSet",["string"]		//called by Nyckelharpa module
//		command "discoverCmds"
//		HSM commands
		command "armNight"						//not set as part of device capabilities
		command "entry"								//Feb 03, 2020 V0.2.5

		fingerprint endpointId: "01", profileId: "0104", deviceId: "0401", inClusters: "0000,0001,0003,0020,0402,0500,0B05", outClusters: "0019,0501", manufacturer: "CentraLite", model: "3400", deviceJoinName: "Xfinity 3400-X Keypad"
		fingerprint endpointId: "01", profileId: "0104", deviceId: "0401", inClusters: "0000,0001,0003,0020,0402,0500,0501,0B05,FC04", outClusters: "0019,0501", manufacturer: "CentraLite", model: "3405-L", deviceJoinName: "Iris 3405-L Keypad"
 		fingerprint endpointId: "01", profileId: "0104", deviceId: "0401", inClusters: "0000,0001,0003,0020,0402,0500,0B05", outClusters: "0003,0019,0501", manufacturer: "Universal Electronics Inc", model: "URC4450BC0-X-R", deviceJoinName: "Xfinity XHK1-UE Keypad"
 		fingerprint endpointId: "01", profileId: "0104", deviceId: "0401", inClusters: "0000,0001,0003,0020,0402,0405,0500,0501,0B05,FC01,FC02", outClusters: " 0003,0019,0501", manufacturer: "iMagic by GreatStar", model: "1112-S", deviceJoinName: "Iris V3 1112-S Keypad"
	}

	preferences{
		input ("version_donotuse", "text", title: "Version: ${version()}<br />(Do not set display only)", required: false )
	    input ("lockManagerPins", "bool", title: "When Off/False<br />Use Nyckelharpa user pin manager. (Default)<br /><br />When On/True<br />Use Lock Manager Pins.", defaultValue: false)
        input name: "optEncrypt", type: "bool", title: "Enable Lock Manager Code encryption", defaultValue: false, description: ""
//		if (device?.data?.model.substring(0,3) !='340" throws error Cannot invoke method substring() on null object
        if (device?.data?.model == '1112-S' || device?.data?.model== 'URC4450BC0-X-R')
        	input ("BatteryType", "enum", title: "Battery Type", required: true, options:["Alkaline", "Lithium", "Rechargeable"])
        input ("panicEnabled", "bool", title: "Enable Panic Key (when available) and Panic Pins. Default (True)", defaultValue: true)
		input ("tempOffset", "decimal", title: "Enter an offset (decimals accepted) to adjust the reported temperature",
				defaultValue: 0, displayDuringSetup: false)
 		input "tempDecimals", "number", required: false, range: "0..2", defaultValue: 1,
 						title: "Temperature: number of decimals from 0 to 2. Default: 0"
		if (device?.data?.model == '1112-S' || device?.data?.model== '3405-L')
			{
	        input ("altBeepEnable", "bool", title: "Enable old style beep sound Default (False). If no beep sound set on", defaultValue: false)
			input ("beepLength", "number", title: "Enter length of old style beep in seconds. Iris V3 firmware locked at 1 beep.", defaultValue: 1, displayDuringSetup: false)
			if (device?.data?.model== '3405-L')
				{
				input name: "pinOnArmAway", type: "bool", title: "Verify pin to arm away", defaultValue: false, description: ""
				input name: "pinOnArmHome", type: "bool", title: "Verify pin to arm home/night", defaultValue: false, description: ""
				}
			}
		else
			input ("beepLength", "number", title: "Enter length of beep in seconds", defaultValue: 1, displayDuringSetup: false)
		if (device?.data?.model != '1112-S')
        	input ("motionTime", "number", title: "Time in seconds for Motion to become Inactive (Default:10, 0=disabled)",	defaultValue: 10, displayDuringSetup: false)
        input ("showVolts", "bool", title: "Turn on to show actual battery voltage x 10 as %. Default (Off) is calculated percentage", defaultValue: false, displayDuringSetup: false)
		if (device?.data?.model == '1112-S' || device?.data?.model == '3405-L')
	        input ("irisPartialSwitch", "bool", title: "When On/True Partial key arms Night, when Off/false: arms Home. Default (Off)", defaultValue: false, displayDuringSetup: false)
        input ("logEnable", "bool", title: "Log debugging messages", defaultValue: false, displayDuringSetup: false)
        input ("txtEnable", "bool", title: "Log trace messages", defaultValue: false, displayDuringSetup: false)
//		paragraph "Centralitex Keypad Plus UEI Version ${version()}" Does not work in HE
	}

}

//	Needed for Web Socket client
// import hubitat.helper.InterfaceUtils		//disabled for live beta, enable for socket testing

//called by client Keypad html/javascript module
def ssekey(ssekey)
	{
	log.debug "sskey entered $ssekey"
	updateDataValue("sseKey","$ssekey")
//	updateDataValue("sseKeyTime",now())			//unix time since Jan 1, 1970 in milliseconds
	}

def version()
	{
	updateDataValue("driverVersion", "1.0.3")	//Stores in device Data
	return "1.0.3";
	}

def installed() {
    log.info "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.info "Updated with settings: ${settings}"
    //Unschedule any existing, mainly socket runin scheduled executions
    unschedule()

//Create a 30 minute timer for debug logging
//  if (logEnable) runIn(1800,logsOff)
	initialize()
}

def initialize()
	{
    logdebug "initialize() entered"
	sendEvent(name: "motion", value: "inactive", displayed: false)	//reset motion value
	//Connect the webSocket
/*	// disabled for live, enable for testing Socket connections
    try {
        InterfaceUtils.webSocketConnect(device, "ws://192.168.0.101:9000/cz/shmdelay/SocketChat.php?o=035c625e481", pingInterval:-1)
    	}
    catch(e)
    	{
        log.error "WebSocket connect failed initialize error: ${e.message}"
    	}
*/
	}

def socket_sendMsg(s) {
	logdebug "socket_sendMsg entered "+s		//convert whatever was passed to Json, then send
    InterfaceUtils.sendWebSocketMessage(device, new groovy.json.JsonOutput().toJson(s))

}

def webSocketStatus(String status)
	{
    logdebug "webSocketStatus entered: ${status}"

    if(status.startsWith('failure: '))
    	{
        log.warn("failure message from web socket ${status}")
		reconnectWebSocket()
    	}
    else if(status == 'status: open')
    	{
        log.info "websocket is open"
        if (device?.data.sseKey)
        	{
        	socket_sendMsg([target: 'loginDvc', sseKey: device.data.sseKey]) //create then send login object
			}
        pauseExecution(1000)
        state.reconnectDelay = 1	//reset reconnect delay
        if (device?.data.sseKey)
        	{
        	socket_sendMsg([target: 'simKeypad', sseKey: device.data.sseKey, alert: 'big issue']) //create test message
			}

    	}
    else if (status == "status: closing")
    	{
        log.warn "WebSocket connection closing."
    	}
    else
    	{
        log.warn "WebSocket error, reconnecting."
		reconnectWebSocket()
    	}
	}

def reconnectWebSocket() {
    // first delay is 2 seconds, doubles every time
    state.reconnectDelay = (state.reconnectDelay ?: 1) * 2
    // don't let delay get too crazy, max it out at 10 minutes
    if(state.reconnectDelay > 600) state.reconnectDelay = 600
    //When the Server Socket is offline, give it some time before trying to reconnect
    runIn(state.reconnectDelay, initialize)
}

// Statuses:
// 00 - Command: setDisarmed   Centralite all icons off / Iris Off button on, Ready to Arm
// 01 - Command: setArmedStay  lights Centralite Stay button / Iris Partial
// 02 - Command: setArmedNight lights Centralite Night button / Iris V2 does nothing / Iris V3 lights Partial
// 03 - Command: setArmedAway  lights Centralite Away button / Iris ON
// 04 - Panic Sound, uses seconds for duration (siren on everything but 3400 use beep instead, max 255)
// 05 - Command: Beep and SetEntryDelay Fast beep (1 per second, uses seconds for duration, max 255) Appears to keep the status lights as it was, used for entry delay command
// 06 - Not ready to Arm Centralite - Amber status blink (Runs forever until Off or some command issued on Centralite, Iris V3 wont Arm)
// 07 - Zigbee In Alarm: sounds Siren on Iris V2/V3
// 08 - Command: setExitStay  Blink Stay Icon/Partial light all devices, Slow beep on Iris only (1 per second, accelerating to 2 beep per second for the last 10 seconds) - With red flashing status - lights Stay icon/Iris Partial Uses seconds
// 09 - Command: setExitNight Blink Night Icon on Centralite and UEI devices no beeps, with red flashing status - lights Night icon/ Uses seconds  (does nothing on Iris)
// 10 - Command: setExitAway  Blink Away Icon / ON light on all devices (1 per second, accelerating to 2 beep per second for the last 10 seconds) - With red flashing status - lights Away Uses/Iris ON seconds
// 11 - ?
// 12 - ?
// 13 - ?

// parse hardware events into attributes,and process websocket messages
def parse(String description)
	{
	logdebug "Parse entered ${description}";
	if (description?.startsWith('{"type":'))		//intercept websocket messages
		{
		log.debug "WebSocket message: ${description}"
		}
	else
		{
		def results = [];

		//------Miscellaneous Zigbee message------//
		if (description?.startsWith('catchall:')) {
			def message = zigbee.parseDescriptionAsMap(description);
			//------ZDO packets - drop ------//
			if (message.profileId == '0000') return []
			//------Profile-wide command (rattr responses, errors, etc.)------//
			else if (message?.isClusterSpecific == false) {
				//------Default response------//
				if (message?.command == '0B') {
					if (message?.data[1] == '81')
						log.error "Device: unrecognized command: "+description;
					else if (message?.data[1] == '80')
						log.error "Device: malformed command: "+description;
				}
				//------Read attributes responses------//
				else if (message?.command == '01') {
					if (message?.clusterId == '0402') {
						logdebug "Device: read attribute response: "+description;

						results = parseTempAttributeMsg(message)
					}}
				else
					log.info "Unhandled profile-wide command: "+description;
			}
			//------Cluster specific commands------//
			else if (message?.isClusterSpecific) {
				//------Poll Control - drop------//
				if (message?.clusterId == '0020') return []
				//------IAS ACE------//
				else if (message?.clusterId == '0501')
					{
					if (message?.command == '07')
						{
						if (device?.data?.model == '1112-S' )
							getMotionResult('active')
						else
							motionON()
						}
					else if (message?.command == '04')
						{
						if (panicEnabled)
							{
							results = createEvent(name: "button", value: "pushed", data: [buttonNumber: 1], descriptionText: "$device.displayName panic button was pushed", isStateChange: true)
							siren()
	//                	    panicContact()		do not use for real keypad
							if (panicEnabled)
								{
								sendEvent(name: "contact", value: "open", displayed: true, isStateChange: true)
								runIn(3, "panicContactClose")
								}
							}
						}
					else if (message?.command == '00')
						{
						if (lockManagerPins)		//use HE lock manager pin codes
							{
							lmPins(message)
							}
						else
							{
							results = handleArmRequest(message)
							logtrace results
							}
						}
				}
				else log.warn "Unhandled cluster-specific command: "+message
			}
		}
		//------IAS Zone Enroll request------//
		else if (description?.startsWith('enroll request')) {
			logtrace "Sending IAS enroll response..."
			results = zigbee.enrollResponse()
		}
		//------Read Attribute response------//
		else if (description?.startsWith('read attr -')) {
			results = parseReportAttributeMessage(description)
		}
		//------Temperature Report------//
		else if (description?.startsWith('temperature: ')) {
			logdebug "Got ST-style temperature report.."
			results = createEvent(getTemperatureResult(zigbee.parseHATemperatureValue(description, "temperature: ", getTemperatureScale())))
			logdebug results
		}
		else if (description?.startsWith('zone status ')) {
			results = parseIasMessage(description)
		}
		return results
		}
	}


def configure() {
    logtrace "--- Configure Called"
    String hubZigbeeId = swapEndianHex(device.hub.zigbeeEui)
    def cmd = [
        //------IAS Zone/CIE setup------//
        "zcl global write 0x500 0x10 0xf0 {${hubZigbeeId}}", "delay 100",
        "send 0x${device.deviceNetworkId} 1 1", "delay 200",

        //------Set up binding------//
        "zdo bind 0x${device.deviceNetworkId} 1 1 0x500 {${device.zigbeeId}} {}", "delay 200",
        "zdo bind 0x${device.deviceNetworkId} 1 1 0x501 {${device.zigbeeId}} {}", "delay 200",

    ] +
    zigbee.configureReporting(1,0x20,0x20,3600,43200,0x01) +
    zigbee.configureReporting(0x0402,0x00,0x29,30,3600,0x0064)

    return cmd + refresh()
}

def poll() {
	refresh()
}

def refresh() {
	 return sendStatusToDevice() +
		zigbee.readAttribute(0x0001,0x20) +
		zigbee.readAttribute(0x0402,0x00)
}

private formatLocalTime(time, format = "EEE, MMM d yyyy @ h:mm:ss.SSS a z") {
	if (time instanceof Long) {
    	time = new Date(time)
    }
	if (time instanceof String) {
    	//get UTC time
    	time = timeToday(time, location.timeZone)
    }
    if (!(time instanceof Date)) {
    	return null
    }
	def formatter = new java.text.SimpleDateFormat(format)
	formatter.setTimeZone(location.timeZone)
	return formatter.format(time)
}

private parseReportAttributeMessage(String description) {
	descMap = zigbee.parseDescriptionAsMap(description)
	//logdebug "Desc Map: $descMap"

	def results = []

	if (descMap.cluster == "0001" && descMap.attrId == "0020") {
		logdebug "Received battery level report"
//		sendNotificationEvent ("Received battery level report descMap.value")
		results = createEvent(getBatteryResult(Integer.parseInt(descMap.value, 16)))
	}
    else if (descMap.cluster == "0001" && descMap.attrId == "0034")
    {
    	logdebug "Received Battery Rated Voltage: ${descMap.value}"
//		sendNotificationEvent ("Received Battery Rated Voltage: descMap.value")
    }
    else if (descMap.cluster == "0001" && descMap.attrId == "0036")
    {
    	logdebug "Received Battery Alarm Voltage: ${descMap.value}"
//		sendNotificationEvent ("Received Battery Alarm Voltage: descMap.value")
    }
	else if (descMap.cluster == "0402" && descMap.attrId == "0000") {
		def value = getTemperature(descMap.value)
		results = createEvent(getTemperatureResult(value))
	}

	return results
}

private parseTempAttributeMsg(message) {

	byte[] temp = message.data[-2..-1].reverse()
	createEvent(getTemperatureResult(getTemperature(temp.encodeHex() as String)))
}

private Map parseIasMessage(String description) {
    List parsedMsg = description.split(' ')
    String msgCode = parsedMsg[2]

    Map resultMap = [:]
    switch(msgCode) {
        case '0x0020': // Closed/No Motion/Dry
        	resultMap = getContactResult('closed')
            break

        case '0x0021': // Open/Motion/Wet
        	resultMap = getContactResult('open')
            break

        case '0x0022': // Tamper Alarm
            break

        case '0x0023': // Battery Alarm
            break

        case '0x0024': // Supervision Report
        	resultMap = getContactResult('closed')
            break

        case '0x0025': // Restore Report
        	resultMap = getContactResult('open')
            break

        case '0x0026': // Trouble/Failure
            break

        case '0x0028': // Test Mode
            break
        case '0x0000':
			resultMap = createEvent(name: "tamper", value: "clear", isStateChange: true, displayed: false)
            break
        case '0x0004':
			resultMap = createEvent(name: "tamper", value: "detected", isStateChange: true, displayed: false)
            break;
        default:
        	log.warn "Invalid message code in IAS message: ${msgCode}"
    }
    return resultMap
}

/*	deprecated V026 Mar 19, 2020
private Map getMotionResult(value) {
	String linkText = getLinkText(device)
	String descriptionText = value == 'active' ? "${linkText} detected motion" : "${linkText} motion has stopped"
	return [
		name: 'motion',
		value: value,
		descriptionText: descriptionText
	]
}
*/
def motionON() {
  logdebug "--- Motion Detected"

	//-- Calculate Inactive timeout value
	def motionTimeRun = (settings.motionTime?:0).toInteger()

	//-- If Inactive timeout was configured
	if (motionTimeRun > 0) {
    	sendEvent(name: "motion", value: "active", displayed:true, isStateChange: true)
		logdebug "--- Will become inactive in $motionTimeRun seconds"
		runIn(motionTimeRun, "motionOFF")
	}
}

def motionOFF() {
//	logdebug "--- Motion Inactive (OFF)"
    sendEvent(name: "motion", value: "inactive", displayed:true, isStateChange: true)
}

/*
 * Used for simulated keypad, Maker API commands, and device command Panic events
 * Do not use this routine with a real device (for now)
 */
def panicContact() {
	logdebug "PanicContact routine entered, Panic enabled: $panicEnabled"
	if (panicEnabled)
		{
    	sendEvent(name: "contact", value: "open", displayed: true, isStateChange: true)
   		runIn(3, "panicContactClose")
    	def currentArmMode = device.currentState("armMode")
    	if (currentArmMode.value.substring(0,3) !='***')		//handle multiple panic button hits
     		sendEvent(name: "armMode", value: "***Panic*** ${currentArmMode.value} ***Panic***", displayed:true, isStateChange: true)
		pauseExecution(1000)									//allows async send events to execute
		currentArmMode = device.currentState("armMode",true)	//somehow forces returned Maker API attribute armMode to update
		}
}

def panicContactClose()
{
	sendEvent(name: "contact", value: "closed", displayed: true, isStateChange: true)
}

//Converts the battery level response into a percentage to display in ST
//and creates appropriate message for given level

private getBatteryResult(rawValue) {
	def linkText = getLinkText(device)
	def result = [name: 'battery']
	def volts = rawValue / 10
	def excessVolts=3.5
	def maxVolts=3.0
	def minVolts=2.5
	if (device.data.model.substring(0,3)!='340')	//UEI and Iris V3 use 4AA batteries, 6volts
		{
		switch (BatteryType)
			{
			case "Rechargeable":
				excessVolts = (1.35 * 4)
				minVolts = (1.0 * 4)
				maxVolts = (1.2 * 4)
			break
			case "Lithium":
				excessVolts = (1.8 * 4)
				minVolts = (1.1 * 4)
				maxVolts = (1.7 * 4)
			break
			default:					//assumes alkaline
				excessVolts=6.8
				maxVolts=6.0
				minVolts=4.6
			break
			}
		}

	if (volts > excessVolts)
		{
		result.descriptionText = "${linkText} battery voltage: $volts, exceeds max voltage: $excessVolts"
		result.value = Math.round(((volts * 100) / maxVolts))
		}
	else
		{
		def pct = (volts - minVolts) / (maxVolts - minVolts)
		result.value = Math.min(100, Math.round(pct * 100))
		result.descriptionText = "${linkText} battery was ${result.value}% $volts volts"
		}
	if (showVolts)			//test if voltage setting is true
	    result.value=rawValue
	return result
}

private getTemperature(value) {
	def celcius = Integer.parseInt(value, 16).shortValue() / 100
//	logdebug "Celcius: $celcius Farenheit: ${celsiusToFahrenheit(celcius)} RawHex: $value"
	if(getTemperatureScale() == "C"){
		return celcius
	} else {
		return celsiusToFahrenheit(celcius)
	}
}

private Map getTemperatureResult(value) {
//	log.debug "getTemperatureResult $value"
	def deg=value
	def dec=1	//default number of decimals
	def linkText = getLinkText(device)
	if (tempOffset)
		deg+=tempOffset
	if (tempDecimals)
		dec=tempDecimals
	if (dec == 1)
		deg = Math.round(deg * 10 ) /10
	else
	if (dec == 2)
		deg = Math.round(deg * 100 ) /100
	else
		deg = Math.round(deg)
	def descriptionText = "${linkText} was ${deg}°${temperatureScale}"
	return [
		name: 'temperature',
		value: deg,
		descriptionText: descriptionText,
		unit: "°${temperatureScale}"
	]
}

//------Command handlers------//
private handleArmRequest(message){
	def keycode
	def reqArmMode = message.data[0].substring(1)
	if (device.data.model == '1112-S' && reqArmMode != '0')		//Iris V3 sends pin on disarm only, otherwise set to 0000
		keycode = '0000'
	else
		keycode = new String(message.data[2..-2].join().decodeHex(),"UTF-8")
	if (reqArmMode == '1' && irisPartialSwitch)			//Iris keypad partial set for Night mode?
		{
		reqArmMode = '2'
		}
	//state.lastKeycode = keycode
	logdebug "Received arm command with keycode/armMode: ${keycode}/${reqArmMode}"

	//Acknowledge the command. This may not be *technically* correct, but it works
	/*List cmds = [
				 "raw 0x501 {09 01 00 0${reqArmMode}}", "delay 200",
				 "send 0x${device.deviceNetworkId} 1 1", "delay 500"
				]
	def results = cmds?.collect{ new hubitat.device.HubAction(it,, hubitat.device.Protocol.ZIGBEE) } + createCodeEntryEvent(keycode, reqArmMode)
	*/
//	def results = createCodeEntryEvent(keycode, reqArmMode)
//	List cmds = [
//				 "raw 0x501 {09 01 00 0${reqArmMode}}",
//				 "send 0x${device.deviceNetworkId} 1 1", "delay 100"
//				]
//	def results = cmds?.collect{ new hubitat.device.HubAction(it, hubitat.device.Protocol.ZIGBEE) } + createCodeEntryEvent(keycode, reqArmMode)
//	log.trace "Method: handleArmRequest(message): "+results
//	return results
//	cmds
	createCodeEntryEvent(keycode, reqArmMode)
}


def armCode(message)
	{
	logtrace "armMode entered: message $message"
	def keycode = message.substring(1,5) as String
	def armMode = message.substring(0,1)
//	sendEvent used not in parse routine Nyckelharpa gets both events
	sendEvent([name: "codeEntered", value: keycode as String, data: armMode as String,
				isStateChange: true, displayed: false])
	pauseExecution(1000)
	def currentArmMode = device.currentState("armMode",true)	//forces returned Maker API attribute armMode to update
	}

def pinStatusSet(pinStatus)
	{
//	set pinstatus for Maker API returned attributes used by simulated keypad
	logtrace "pinStatusSet entered: message $message"
	sendEvent([name: "pinStatus", value: pinStatus, data: pinStatus,
				isStateChange: true, displayed: false])
	}

def createCodeEntryEvent(keycode, armMode) {
	logtrace "createCodeEntryEvent entered keycode: $keycode armMode: $armMode"
	createEvent(name: "codeEntered", value: keycode as String, data: armMode as String,
				isStateChange: true, displayed: false)
	}

private sendStatusToDevice(armModex='') {
	logdebug 'Entering sendStatusToDevice armModex: '+armModex+', Device.armMode: '+device.currentValue('armMode',true)
	def armMode=null
	if (armModex=='')
		{
//		logdebug "using device armMode"
		armMode = device.currentValue("armMode",true)
		}
	else
		{
//		logdebug "using passed armModex"
		armMode = armModex
		}
	def status = ''
	if (armMode == null || armMode == 'disarmed') status = 0
	else if (armMode == 'armedAway') status = 3
	else if (armMode == 'armedHome') status = 1
	else if (armMode == 'armedNight') status = 2
	else logdebug 'Invalid Arm Mode in sendStatusToDevice: '+armMode

	// If we're not in one of the 4 basic modes, don't update the status, don't want to override beep timings, exit delay is dependent on it being correct
	if (status != '')
	{
		return sendRawStatus(status)
	}
    else
    {
    	return []
    }
}


private sendRawStatus(status, secs = 00) {
	def seconds=secs as Integer
	logdebug "sendRawStatus info ${zigbee.convertToHexString(status,2)}${zigbee.convertToHexString(seconds,2)} to device..."


    // Seems to require frame control 9, which indicates a "Server to client" cluster specific command (which seems backward? I thought the keypad was the server)
    List cmds = ["raw 0x501 {09 01 04 ${zigbee.convertToHexString(status,2)}${zigbee.convertToHexString(seconds,2)}}",
    			 "send 0x${device.deviceNetworkId} 1 1", 'delay 100']

	cmds
//  def results = cmds?.collect{ new hubitat.device.HubAction(it,hubitat.device.Protocol.ZIGBEE) };
//	logdebug "sendRawStatus results"+results
//  return results
}

def notifyPanelStatusChanged(status) {
	//TODO: not yet implemented. May not be needed.
}
//------------------------//

def setDisarmed() {
	logdebug ('setDisarm entered')
	state.alert=false
	setModeHelper("disarmed",0)
	}
def setArmedAway(def delay=0) { setModeHelper("armedAway",delay) }
def setArmedStay(def delay=0) { setModeHelper("armedHome",delay) }		//was Stay in SmartThings
def setArmedNight(def delay=0) { setModeHelper("armedNight",delay) }
//	Hubitat Command set V1.0.1 Feb 21, 2019,
//on Mar 31, 2019 HE sent disarm 3 times, ignore when mode is correct on device
//on Apr 19, 2019 Not using HSM commands
def disarm(delay=0)
	{
	logdebug ('disarm entered')
//	if (device.currentValue('armMode',true) != 'disarmed')
//		setModeHelper("disarmed",0)
	}
def armAway(def delay=0)
	{
	logdebug ('armAway entered')
//	if (device.currentValue('armMode',true) != 'armedAway')
//		setModeHelper("armedAway",delay)
	}
def armHome(def delay=0)
	{
	logdebug ('armHome entered')
//	if (device.currentValue('armMode',true) != 'armedHome')
//		setModeHelper("armedStay",delay)
	}
def armNight(def delay=0)
	{
	logdebug ('armNight entered')
//	if (device.currentValue('armMode',true) != 'armedNight')
//		setModeHelper("armedNight",delay)
	}

def entry(delay=0)
	{
//	logdebug "entry entered delay: ${delay}"
//	setEntryDelay(delay)	//disabled until I understand why this is issued when setting away from actiontiles
//	v.0.2.5 not used by Nyckelharpa, but device should be removed from HSM, issue warning message
//	log.warn "Centralitex DH says: Remove $device.displayName from HSM Configure Arming/Disarming/Cancel Options --> Use keypad(s) to arm/disarm"
	}

def setEntryDelay(delay=0) {
//	setModeHelper("entryDelay", delay)
	if (device?.data?.model == '1112-S')
		state.delayExpire = now() + (delay * 1000)		//Unix time in milliseconds, Used by Iris V3 only
	sendRawStatus(5, delay) // Entry delay beeps
}

def setExitAway(delay) {
//	setModeHelper("exitDelay", delay)
//	setModeHelper("exitDelay", 0)
	sendRawStatus(10, delay)  // Exit delay
}

def setExitNight(delay) {
	sendRawStatus(9, delay)		//Night delay
	}

def setExitStay(delay) {
	sendRawStatus(8, delay)		//Stay Delay
	}

/*
 *	Alarm Capability Commands
 */

def both()
	{
	siren()
	}
def off()
	{
	state.alert=false
	if (device.data.model.contains ('3400'))
		beep(0)
	else
		{
    	List cmds = ["raw 0x501 {19 01 04 00 00 01 01}",
    			 "send 0x${device.deviceNetworkId} 1 1", 'delay 100']
		cmds
		}
	}
def siren()
	{
/*	device.data.model not available in ST
 *  siren command does not work on Centralite 3400 V2 and 3400-G (V3) or UEI
 */
	state.alert = true					//used only by Iris V3 in sendPanelResponse
	if (device.data.model.contains ('3400') || device.data.model.substring(0,3)=='URC')
		beep(255)
	else
		{
    	List cmds = ["raw 0x501 {19 01 04 07 00 01 01}",
    	 "send 0x${device.deviceNetworkId} 1 1", 'delay 100']
		cmds
		}
	}
def strobe()
	{
	sendRawStatus(6)			//blinks Iris light, not sure on Centralite
	}

private setModeHelper(String armMode, delay) {
	logdebug "In setmodehelper armMode: $armMode delay: $delay"
	sendEvent(name: "armMode", value: armMode)
	if (armMode != 'entryDelay')
		{
		def lastUpdate = formatLocalTime(now())
		sendEvent(name: "lastUpdate", value: lastUpdate, displayed: false)
		}
	sendStatusToDevice(armMode)
}

private setKeypadArmMode(armMode){
	Map mode = [disarmed: '00', armedAway: '03', armedHome: '01', armedNight: '02', entryDelay: '', exitDelay: '']
    if (mode[armMode] != '')
    {
		return ["raw 0x501 {09 01 04 ${mode[armMode]}00}",
				 "send 0x${device.deviceNetworkId} 1 1", 'delay 100']
    }
}

def acknowledgeArmRequest(armMode='0'){
	logtrace "entered acknowledgeArmRequest armMode: ${armMode}"
	List cmds = [
				 "raw 0x501 {09 01 00 0${armMode}}",
				 "send 0x${device.deviceNetworkId} 1 1", "delay 100"
				]
//	def results = cmds?.collect{ new hubitat.device.HubAction(it, hubitat.device.Protocol.ZIGBEE) }
//	logtrace "Method: acknowledgeArmRequest(armMode): "+results
//	return results
	cmds

}

def sendInvalidKeycodeResponse(){
	List cmds = [
				 "raw 0x501 {09 01 00 04}",
				 "send 0x${device.deviceNetworkId} 1 1", "delay 100"
				]

	logtrace 'Method: sendInvalidKeycodeResponse(): '+cmds
//	return (collect{ new hubitat.device.HubAction(it, hubitat.device.Protocol.ZIGBEE) }) + sendStatusToDevice()
	cmds
	sendStatusToDevice()
}

/*
 * Iris V3 may not sound default new beep if it has old firmware
 * Iris V3 generates 1 (old) beep no matter what number is given
 */
def beep(def beepLength = settings.beepLength as Integer)
	{
	logdebug "beep entered: ${beepLength} ${altBeepEnable}"
	if ( beepLength == null )
		{
		beepLength = 1
		}
	def len = zigbee.convertToHexString(beepLength, 2)
	if (device.data.model == '1112-S' || device.data.model== '3405-L')
		{
//		logdebug "its an Iris altBeepEnable: ${altBeepEnable}"
		if (!altBeepEnable)
			{
//			log.debug 'Using FC04 cluster beep'
        	return ["he raw 0x${device.deviceNetworkId} 1 1 0xFC04 {15 4E 10 00 00 00}"]
        	}
		}
//	log.debug "old beep used: ${beepLength} ${len} ${altBeepEnable}"
    return ["he raw 0x${device.deviceNetworkId} 1 1 0x0501 {09 01 04 05 ${len} 01 01}"]
	}

//------Utility methods------//

private String swapEndianHex(String hex) {
	reverseArray(hex.decodeHex()).encodeHex()
}

private byte[] reverseArray(byte[] array) {
	int i = 0;
	int j = array.length - 1;
	byte tmp;
	while (j > i) {
		tmp = array[j];
		array[j] = array[i];
		array[i] = tmp;
		j--;
		i++;
	}
	return array
}
//------------------------//

private testCmd(cmd=5,time=15){
	//logtrace zigbee.parse('catchall: 0104 0501 01 01 0140 00 4F2D 01 00 0000 07 00 ')
	//beep(10)
	//test exit delay
	//logdebug device.zigbeeId
	//testingTesting()
	//discoverCmds()
	//zigbee.configureReporting(1,0x20,0x20,3600,43200,0x01)		//battery reporting
	//["raw 0x0001 {00 00 06 00 2000 20 100E FEFF 01}",
	//"send 0x${device.deviceNetworkId} 1 1"]
	//zigbee.command(0x0003, 0x00, "0500") //Identify: blinks connection light

	//logdebug 		//temperature reporting

//	return zigbee.readAttribute(0x0020,0x01) +
//		    zigbee.readAttribute(0x0020,0x02) +
//		    zigbee.readAttribute(0x0020,0x03)
//	if (cmd < 12)
//		sendRawStatus(cmd as Integer, time as Integer)
//    List cmds = ["raw 0x501 {09 01 03 ${zigbee.convertToHexString(cmd as Integer,2)}${zigbee.convertToHexString(time as Integer,2)}}",
//    			 "send 0x${device.deviceNetworkId} 1 1", 'delay 100']
//	cmds
    List cmds = ["raw 0x501 {19 01 04 07 00 01 01}",
    			 "send 0x${device.deviceNetworkId} 1 1", 'delay 100']
	cmds
}

private discoverCmds(){
	List cmds = ["raw 0x0501 {08 01 11 0011}", 'delay 200',
				 "send 0x${device.deviceNetworkId} 1 1", 'delay 500']
	cmds
}

private testingTesting() {
	logdebug "Delay: "+device.currentState("armMode").toString()
	List cmds = ["raw 0x501 {09 01 04 050A}", 'delay 200',
				 "send 0x${device.deviceNetworkId} 1 1", 'delay 500']
	cmds
}

def logdebug(txt)
	{
   	if (logEnable)
   		log.debug ("${txt}")
    }
def logtrace(txt)
	{
   	if (txtEnable)
   		log.trace ("${txt}")
    }

/*		V026 logic used with Iris V3 to fix hardware motion message send loop
		Copied from HE's Iris V3 DH. This device needs a response on motion
*/

private getMotionResult(value)
	{
    logdebug "getMotionResult: ${value} current: ${device?.currentValue('motion')}"
    if (device?.currentValue("motion") != "active")
    	{
        runIn(30,motionOFF)
        def descriptionText = "${device?.displayName} is ${value}"
        sendEvent(name: "motion",value: value,descriptionText: "${descriptionText}")
    	}
	sendPanelResponse()		//Iris V3 needs a response on normal motion messages or it goes beserk and falls offline
	}

/*
This is a weird device be careful here, it sends unsolicited motion messages.
Iris V3 specific routine added Mar 22, 2020 V0.2.7
Iris V3 when arming (exit delays), hardware sends a motion message every second that is ignored. These messages stop when arming completes or is cancelled
Iris V3 when armed with an entry delay, hardware sends a motion message every second that is ignored.
		These messages stop when disarming completes or entry delay time expires (intrusion)
Iris V3 lights the Partial light on armedNight or armedHome, blinks it on armingNight or armingHome
Iris V3	When system is fully armed or disarmed, not responding to a motion response eventually throws the device offline
Note: delayExpire is set when setEntrydelay is entered, so in this module it's used for entry delay only. This varies from HE version
*/
private sendPanelResponse()
	{
	def resp = []
	if (state?.alert)			//check if siren active, requires disarm to stop it
		{
		logdebug "sending Alert Response, Alert: ${state.alert}"
		resp.add("he raw 0x${device.deviceNetworkId} 1 1 0x0501 {19 01 05 04 00 00 00}")	//Siren stays on, no hardware message loop
		sendHubCommand(new hubitat.device.HubMultiAction(resp, hubitat.device.Protocol.ZIGBEE))
		return
		}
	def hsmstatus = location.hsmStatus
	def status = [disarmed : 0, armedHome: 1, armedNight: 2, armedAway: 3, allDisarmed: 0, armingHome: 8, armingNight: 9, armingAway: 10][hsmstatus] ?: 0
	if (status < 4)
		{
		if (status > 0 && now() < state.delayExpire?: 0)		//active entry delay and not disarmed?
			logdebug "no response1: ${hsmstatus} status: ${status} ${intToHexStr(status)} state.delayExpire: ${state.delayExpire?: 0}"
		else
			{
			logdebug "sending response: ${hsmstatus} status: ${status} ${intToHexStr(status)} state.delayExpire: ${state.delayExpire?: 0}"
			state.delayExpire=0
			resp.add("he raw 0x${device.deviceNetworkId} 1 1 0x0501 {19 01 05 ${intToHexStr(status)} 00 00 00}")
			sendHubCommand(new hubitat.device.HubMultiAction(resp, hubitat.device.Protocol.ZIGBEE))
			}
		}
	else
		{
		state.delayExpire=0
		logdebug "no response2: ${hsmstatus} status: ${status} ${intToHexStr(status)} state.delayExpire: ${state.delayExpire?: 0}"
		}
	}

/*
		The following code is used for compatabilitty with HE pins and pin processing
*/
void setExitDelay(Map delays){
//    state.exitDelay = (delays?.awayDelay ?: 0).toInteger()
//    state.armNightDelay = (delays?.nightDelay ?: 0).toInteger()
//    state.armHomeDelay = (delays?.homeDelay ?: 0).toInteger()
}

void setExitDelay(delay){
//    state.exitDelay = delay != null ? delay.toInteger() : 0
}

private changeIsValid(codeMap,codeNumber,code,name){
    def result = true
    def codeLength = device.currentValue("codeLength")?.toInteger() ?: 4
    def maxCodes = device.currentValue("maxCodes")?.toInteger() ?: 20
    def isBadLength = codeLength != code.size()
    def isBadCodeNum = maxCodes < codeNumber
    if (lockCodes) {
        def nameSet = lockCodes.collect{ it.value.name }
        def codeSet = lockCodes.collect{ it.value.code }
        if (codeMap) {
            nameSet = nameSet.findAll{ it != codeMap.name }
            codeSet = codeSet.findAll{ it != codeMap.code }
        }
        def nameInUse = name in nameSet
        def codeInUse = code in codeSet
        if (nameInUse || codeInUse) {
            if (logEnable && nameInUse) { log.warn "changeIsValid:false, name:${name} is in use:${ lockCodes.find{ it.value.name == "${name}" } }" }
            if (logEnable && codeInUse) { log.warn "changeIsValid:false, code:${code} is in use:${ lockCodes.find{ it.value.code == "${code}" } }" }
            result = false
        }
    }
    if (isBadLength || isBadCodeNum) {
        if (logEnable && isBadLength) { log.warn "changeIsValid:false, length of code ${code} does not match codeLength of ${codeLength}" }
        if (logEnable && isBadCodeNum) { log.warn "changeIsValid:false, codeNumber ${codeNumber} is larger than maxCodes of ${maxCodes}" }
        result = false
    }
    return result
}

private getCodeMap(lockCodes,codeNumber){
    if (logEnable) log.debug "getCodeMap- lockCodes:${lockCodes}, codeNumber:${codeNumber}"
    def codeMap = [:]
    def lockCode = lockCodes?."${codeNumber}"
    if (lockCode) {
        codeMap = ["name":"${lockCode.name}", "code":"${lockCode.code}"]
    }
    return codeMap
}

private getLockCodes() {
    def lockCodes = device.currentValue("lockCodes")
    def result = [:]
    if (lockCodes) {
        if (lockCodes[0] == "{") result = new JsonSlurper().parseText(lockCodes)
        else result = new JsonSlurper().parseText(decrypt(lockCodes))
    }
    return result
}

private updateLockCodes(lockCodes){
    if (logEnable) log.debug "updateLockCodes: ${lockCodes}"
    def data = new groovy.json.JsonBuilder(lockCodes)
    if (optEncrypt) data = encrypt(data.toString())
    sendEvent(name:"lockCodes",value:data,isStateChange:true)
}

private updateEncryption(){
    def lockCodes = device.currentValue("lockCodes") //encrypted or decrypted
    if (lockCodes){
        if (optEncrypt && lockCodes[0] == "{") {	//resend encrypted
            sendEvent(name:"lockCodes",value: encrypt(lockCodes), isStateChange:true)
        } else if (!optEncrypt && lockCodes[0] != "{") {	//resend decrypted
            sendEvent(name:"lockCodes",value: decrypt(lockCodes), isStateChange:true)
        } else {
            sendEvent(name:"lockCodes",value: lockCodes, isStateChange:true)
        }
    }
}

void setCodeLength(length){
    String descriptionText = "${device.displayName} codeLength set to 4"
    if (txtEnable) log.info "${descriptionText}"
    sendEvent(name:"codeLength",value:"${4}",descriptionText:descriptionText)
}

void setCode(codeNumber, code, name = null) {
    if (!name) name = "code #${codeNumber}"

    def lockCodes = getLockCodes()
    def codeMap = getCodeMap(lockCodes,codeNumber)
    def data = [:]
    def value
    //verify proposed changes
    if (!changeIsValid(codeMap,codeNumber,code,name)) return

    if (logEnable) log.debug "setting code ${codeNumber} to ${code} for lock code name ${name}"

    if (codeMap) {
        if (codeMap.name != name || codeMap.code != code) {
            codeMap = ["name":"${name}", "code":"${code}"]
            lockCodes."${codeNumber}" = codeMap
            data = ["${codeNumber}":codeMap]
            if (optEncrypt) data = encrypt(JsonOutput.toJson(data))
            value = "changed"
        }
    } else {
        codeMap = ["name":"${name}", "code":"${code}"]
        data = ["${codeNumber}":codeMap]
        lockCodes << data
        if (optEncrypt) data = encrypt(JsonOutput.toJson(data))
        value = "added"
    }
    updateLockCodes(lockCodes)
    sendEvent(name:"codeChanged",value:value,data:data, isStateChange: true)
}

def deleteCode(codeNumber) {
    def codeMap = getCodeMap(lockCodes,"${codeNumber}")
    def result = [:]
    if (codeMap) {
        lockCodes.each{
            if (it.key != "${codeNumber}"){
                result << it
            }
        }
        updateLockCodes(result)
        def data =  ["${codeNumber}":codeMap]
        if (optEncrypt) data = encrypt(JsonOutput.toJson(data))
        sendEvent(name:"codeChanged",value:"deleted",data:data, isStateChange: true)
    }
}

def getCodes(){
    updateEncryption()
}

private getDefaultLCdata(){
    return [
            isValid:true
            ,isInitiator:false
            ,code:"0000"
            ,name:"not required"
            ,codeNumber: -1
    ]
}

//	This code handles the HE Pin processing
def lmPins(descMap)
	{
	logdebug "lmPins entered ${device?.data?.model} ${descMap}"
	def armRequest = descMap.data[0]
	def nyckelArmRequest=armRequest
	def asciiPin = "0000"
	switch (device?.data?.model)
		{
		case '1112-S' :
			if (armRequest == "00")
				asciiPin = descMap.data[2..5].collect{ (char)Integer.parseInt(it, 16) }.join()
			else
			if (armRequest== '01' && irisPartialSwitch)
				nyckelArmRequest='02'
			createLmCodeEntryEvent(asciiPin,nyckelArmRequest.substring(1),isValidPinV3(asciiPin, armRequest))
		break
		case '3405-L' :
			asciiPin = descMap.data[2..5].collect{ (char)Integer.parseInt(it, 16) }.join()
			if (armRequest== '01' && irisPartialSwitch)
				nyckelArmRequest='02'
			createLmCodeEntryEvent(asciiPin,nyckelArmRequest.substring(1),isValidPinV2(asciiPin, armRequest))
		break
		default:
			asciiPin = descMap.data[2..5].collect{ (char)Integer.parseInt(it, 16) }.join()
			createLmCodeEntryEvent(asciiPin,armRequest.substring(1),isValidPin(asciiPin, armRequest))
		break
		}
	}

private isValidPinV3(code, armRequest)
	{
    def data = getDefaultLCdata()
    if (armRequest == "00")
    	{
        //verify pin
        def lockCode = lockCodes.find{ it.value.code == "${code}" }
        if (lockCode)
        	{
            data.codeNumber = lockCode.key
            data.name = lockCode.value.name
            data.code = code
//          descriptionText = "${device.displayName} was disarmed by ${data.name}"
//          sendEvent(name: "lastCodeName", value: data.name, descriptionText: descriptionText, isStateChange: true)
        	}
        else
        	{
            data.isValid = false
            if (txtEnable) log.warn "Invalid pin entered [${code}] for arm command [${getArmCmd(armRequest)}]"
        	}
	    }
    return data
	}

private isValidPinV2(code, armRequest)
	{
    def data = getDefaultLCdata()
    if (code=='0000' && (armRequest =='03' || armRequest== '01'))		//allow no pin used for arming
    	{}
    else
    if ((pinOnArmAway && armRequest == "03") || (pinOnArmHome && armRequest == "01") || armRequest == "00")
    	{
		//verify pin
		def lockCode = lockCodes.find{ it.value.code == "${code}" }
		if (lockCode)
			{
			data.codeNumber = lockCode.key
			data.name = lockCode.value.name
			data.code = code
			}
		else
			{
			data.isValid = false
			if (txtEnable) log.warn "Invalid pin entered [${code}] for arm command [${getArmCmd(armRequest)}]"
			}
		}
    return data
  	}

// Centralite 3400s and UEI
private isValidPin(code, armRequest)
	{
    def data = getDefaultLCdata()
	def lockCode = lockCodes.find{ it.value.code == "${code}" }
	if (lockCode)
		{
		data.codeNumber = lockCode.key
		data.name = lockCode.value.name
		data.code = code
		}
	else
		{
		data.isValid = false
		if (txtEnable) log.warn "Invalid pin entered [${code}] for arm command [${getArmCmd(armRequest)}]"
		}
    return data
  	}

def getArmCmd(armMode){
    switch (armMode){
        case "00": return "disarm"
        case "01": return "armHome"
        case "02": return "armNight" //arm sleep on Xfinity keypad
        case "03": return "armAway"
    }
}
def createLmCodeEntryEvent(keycode, armMode, lmPinMap) {
//	Map data is sent, but it returns in Nyckelharpa as a JSON string that must be reformatted with Jsonslurper into a Map
	def lmPinMapx=lmPinMap
	lmPinMapx.armMode=armMode as String
	logtrace "createLmCodeEntryEvent entered keycode: $keycode armMode: $armMode lmMap: $lmPinMap"
	sendEvent(name: "codeEntered", value: keycode as String, data: lmPinMapx,
				isStateChange: true, displayed: false)
	}


