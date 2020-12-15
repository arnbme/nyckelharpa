/**
 *  Update Url:   https://raw.githubusercontent.com/arnbme/nyckelharpa/master/Nyckelharpa.groovy
 *
 *  Nyckelharpa Parent
 *  Functions:
 *		Acts as a container/controller for Child modules
 *		Process all Keypad activity
 *
 *  Copyright 2017-2020 Arn Burkhoff
 *
 * 	Changes to Apache License
 *	4. Redistribution. Add paragraph 4e.
 *	4e. This software is free for Private Use. All derivatives and copies of this software must be free of any charges,
 *	 	and cannot be used for commercial purposes.
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
 *  Aug 16, 2020 v1.2.0	Make force arming timing seconds window an external input field, globalForceSeconds, default 15
 *  Jul 16, 2020 v1.1.2	Revise Version check logic: Use Gitub Hubitat Package Manager file vs arnb.org file
 *  Jul 15, 2020 v1.1.1	Create simulation of Hubitat unsupported SmartThings device.command([delay: millis]), see delayCommand method
 *  					Restore ability to chirp a siren, or other device driver lacking a beep command
 *                      Restore 2 second delay for alarmDevices with beep command
 *  Jun 22, 2020 v1.1.0	add 'settings.' to many settings references hopefully reducing some Groovy overhead
 *  Jun 20, 2020 v1.0.9	Eliminte logdebugs routine to reduce overhead. Test flag and issue log.debug instead
 *  Jun 18, 2020 v1.0.8	Fix major issues with 1.0.7
 *							add subscribe to hsmSetArm to make arming from non-keypad sources function correctly
 *							eliminate and reduce updates to atmomicStates when possible
 *  Jun 10, 2020 v1.0.7	Forced arming fails from Dashboard HSM Status
 *							fixed by setting atomicState.doorsdtim=0 in two places
 *  Apr 25, 2020 v1.0.6	UEI when Forced arming failue is silent when sending acknowledgeArmRequest(4)
 *							use 0 when forced arming failure only. See routine checkOpenContacts.
 *  Apr 23, 2020 v1.0.6	UEI keypad never stops beeping when beeped to indicate arming failed for forced arming notification
 *							see routine solokeypad_delayOff. Applies only when using Centalitex driver
 *  Apr 23, 2020 v1.0.6	Adjust sound issued when pin is rejected due to open contact forced arming.
 *							see routine solokeypad_delayBeep. Applies only when using Centalitex driver
 *  Apr 23, 2020 v1.0.6	When using Centralitex driver and LCM pins:
 * 							Implement Panic Pin by testing for word Panic in LCM Pin name
 *  Apr 23, 2020 v1.0.6	Update text used with generating virtual contacts. Thanks to Peir Posthumous
 *  Apr 22, 2020 v1.0.5	Future: Prepare for a Panic pin when using Lock Code Manager pins with Centralitex driver, found following error
 *  Apr 22, 2020 v1.0.5	Error: LCM pins searching user pin data. Cause: typo Fix test setting lmPin, not lmpin
 *  Apr 22, 2020 v1.0.5	Eliminate all references to RBoyDTH from ST
 *  Apr 17, 2020 v1.0.4	Issue: System does not arm or incorrectly arms using Centralitex driver
 *  						Cause: System is disarmed, user sets child contact device to open while real device closed or vice versa
 *							Solution: When system is disarmed and a pin is entered, set child device status to real device status
 *  Apr 05, 2020 v1.0.3	When arming from a dashboard this app's alert message was not created
 *							Solution: in HeDoorsClose check doors when HSM is disarmed
 *  Apr 05, 2020 v1.0.3	Using He keypad driver when arming away with open door,	attempt to force arm sets arming off/disarmed
 *							Caused by Modefix executing checkOpenContacts
 *							Solution: in armingInHeCodeHandler set atomicState.doorsdtim=0.
 *  Apr 05, 2020 v1.0.3	Using HE keypad driver with forced arming, system was force armed, contact remains open,
 *								set alarm disarmed, intrusion alert occurs
 *							Caused by unknown timing issue in HSM that trips alarm when we reset the NYKL-contact
 *								to match the real contact, and the real contact is open even when system is disarmed
 *							Solution: Change subscribe from HeDoorsReset to HeDoorsResetDelay that runs HeDoorsReset in 1 second
 *  Apr 03, 2020 v1.0.2	Use HE driver event armingIn vs securityKeypad for requested mode
 *  Apr 02, 2020 v1.0.1	When any keypad uses the HE driver and panic is enabled, create nykl-panic device and subscribe to HSM panic
 *  Mar 31, 2020 v1.0.1	Add support for using native HE keypad drivers
 *  Mar 25, 2020 v1.0.0	Add support for Lock Manager Pin Codes and LM pin verificaion with Centralitex keypad driver
 *  Feb 03, 2020 v0.2.3	Correct error reported by @theroonie
 *										~ line 923 change	if(!checkOpenContacts(globalHomeCotacts, globalHomeNotify, keypad))
 *										to 							if(!checkOpenContacts(globalHomeContacts, globalHomeNotify, keypad))
 *  Jan 08, 2020 v0.2.3	Direct SMS deprecated by Hubitat. Replace with notification devices including Twilio SMS, and HE phone app
 *										a work in progress, needs thought and work, but functional
 *	Jan 06, 2020 v0.2.2	Fix non terminating error executing qssehe_alert(). Pre alpha code for software / internet keypad
 *	Jun 10, 2019 v0.2.1		When rule triggers an alert use rule name for sse alert name
 *	Jun 09, 2019 v0.2.1		When a pin is rejected for an open contact, execute keypad.pinStatusSet("OpenContact")
 *	Jun 02, 2019 v0.2.1		Set keypad attribute pinStatus (requires updated keypad driver)
 *	May 24, 2019 v0.2.0	Arming canceled and Arming forced not sent to push devices. Fixed
 *	May 23, 2019 v0.2.0	adjust text for Hubitat Phoneapp use as a notification device
 *	May 23, 2019 v0.2.0	version check logic from May 19, 2019 did not work for all module names, recoded
 *	May 21, 2019 v0.1.9	add logic to handle failure of Json file httpget and produce visible message
 *						get the Centralitex Keypad version from Device.getDaValue driverVersion field
 *						directly calling keypads version() routine returns null
 *	May 19, 2019 v0.1.9	add logic originally from CobraMax. verify all user modules and keypad driver are current
 *						compare current version to external JSON file
 *	May 18, 2019 v0.1.8	add support for using Panic Pin contact for HSM Custom Panic rule, HSM arming not required
 *						Use existing DH command panicContact changed to allow external call, when Panic pin is entered
 *						DH already supports using the device Panic pin for a Panic rule
 *						eliminate creation and setting of the NCKL-Panic-Contact no lonner needed
 *						eliminate globalPanic setting, Keypad Panic setting controls if Panic occurs
 *	May 17, 2019 v0.1.7	eliminate some odd code in keypadLighton for night arm state (also removed in SHM Delay)
 *	May 16, 2019 v0.1.6	add UEI model to keypads with 3 armed lighting modes
 *	May 12, 2019 v0.1.5	Show summary of settings on pageTwo prior to Done
 *						Insure non keypad device does not get a beep command with time, may create an error
 *	May 11, 2019 v0.1.5	Add settings for open door tone beep or last 10 seconds of setexitdelay
 *						Add setting to specify open doors tone here rather than in device
 *						moved talker door open event to end of open DoorHandler routines,
 *							otherwise it delayed beeps and chimes by over 2+ seconds
 *						Do some editing on settings and produce warning messages
 *	May 10, 2019 v0.1.4	Adjust Entry delay beeping devices text to warn against using keypad, it causes entry delay tones to stop
 *	May 06, 2019 v0.1.3	Issue in doorHandler: child device not to parent device state when system is armed,
 *						caused false alarm. Corrected doorHandler code.
 *	May 06, 2019 v0.1.2	Issue in CheckOpenContacts: When force closing child contact for alarm, it issues an
 *						unwanted door closed message. Solution: directly close the child contact vs using DoorHandler
 *	May 06, 2019 v0.1.1	Change subscibe from contact.open/close to contact reducing system overhead
 *	May 03, 2019 v0.1.0	Add Beepers and Sirens from deprecated contact module, move alarm state subscribe from talker to here
 *	May 03, 2019 v0.1.0	Rearrange sections, place globalSettings as  top profile
 *	May 02, 2019 v0.1.0	Add other devices to monitor for open / close to globalContacts
 *	May 02, 2019 v0.0.9	Cleanup and prepare for release
 *	May 02, 2019 v0.0.8	Fix missing spaces on valid and invalid pin messages
 *	Apr 30, 2019 v0.0.7	Do not create child devices when globalChildPrefix is null
 *	Apr 30, 2019 v0.0.7	Change method used to delete child devices to catch all of them
 *	Apr 28, 2019 v0.0.6	When arming Panic, close all child contacts, stop talker speaking panic is closed
 *	Apr 28, 2019 v0.0.5	use NCKL Panic Contact no longer necessary to specify simulated contact sensor in globals
 *	Apr 27, 2019 v0.0.5	Create and use simulated contacts in HSM arming, control here for arming override
 *	Apr 25, 2019 v0.0.4	Improve arming faild and forced arming messages
 *	Apr 24, 2019 v0.0.3	allow force rearm if second arm request within 30 minutes of last open doors failure
 *	Apr 23, 2019 v0.0.2	cleanup pin entry and panic logic
 *	Apr 22, 2019 v0.0.1	Add pushover support
 *	Apr 22, 2019 v0.0.0	Rename SHM Delay to Nyckelharpa change version to 0.0.0 prerelease
 *	Apr 20, 2019 v2.2.9H Cleanup some grunge
 *	Apr 01, 2019 v2.2.9H killed verify version link for now
 *	Apr 01, 2019 v2.2.9H Use true on device.currentvalue to insure live value, otherwise could be stale
 *	Mar 26, 2019 v2.2.8  Corrected keypad lights not properly see around statement 1034/5  fakeEvt = [value: theMode]
 *	Mar 14, 2019 v2.2.8  Change: Period not saved in Apple IOS, remove it as a phone number delimter
 *	Mar 12, 2019 v2.2.8  add phone number delimiters pound sign(#) and period(.) the semi colon no longer shows in android, nor is saved in IOS?
 *	Mar 03, 2019 v2.2.8  add flag to turn debug messages on, default is off
 *	Feb 19, 2019 v2.2.7  globalPinPush was miscoded should have been globalBadPinPush around line 359 Send Bad Pin Push Notification
 *	Jan 06, 2019 V2.2.6  Added: Support for 3400_G Centralite V3
 *	Jan 05, 2019 V2.2.5  Fixed: iPhone classic phone app crashes when attempting to set 3 character emergency number
 *								remove ,"" selection option
 *	Nov 30, 2018 V2.2.4  not in this version (add additional panic subscribe when using RBoy DTH
 *	Nov 30, 2018 V2.2.4  not in this version (Minor logic change for Iris V3 when testing for 3405-L
 *	Nov 24, 2018 V2.2.3H Convert to Hubitat, then did nothing until April 2019
 *	Aug 11, 2017 v1.0.0  Created as SHM Delay in SmartThings
 *
 */
import groovy.json.JsonSlurper
/* on Apr 02, 2020 this started to throw an error, not used so deprecated
if (location.hubs[0].id.toString().length() > 5)
 	{
 	state.hubType = 'SmartThings'
	include 'asynchttp_v1'
	}
else
 	state.hubType = 'Hubitat'
*/

definition(
    name: "Nyckelharpa",
    namespace: "arnbme",
    author: "Arn Burkhoff",
    description: "(${version()}) Nyckelharpa Keypad Control Center for HSM",
    category: "My Apps",
    iconUrl: "https://www.arnb.org/IMAGES/hourglass.png",
    iconX2Url: "https://www.arnb.org/IMAGES/hourglass@2x.png",
    iconX3Url: "https://www.arnb.org/IMAGES/hourglass@2x.png",
    singleInstance: true)

preferences {
    page(name: "main", nextPage: "globalsPage")
    page(name: "globalsPage", nextPage: "globalsVerify")
    page(name: "globalsVerify", nextPage: "globalsPage")
    page(name: "pageTwo")
	}

def version()
	{
	return "1.2.1";
	}
def main()
	{

	dynamicPage(name: "main", install: true, uninstall: true)
		{
		if (app.getInstallationState() == 'COMPLETE')	//note documentation shows as lower case, but returns upper
			{
			def appVersions=getAppVersions()
			def verMsg=genVersionMsg(appVersions)
			def modeFixChild="Update"
			def appVerString=appVersions as String
			if (!appVerString.contains("Nyckelharpa ModeFix"))
				{
				modeFixChild='Create'
				section
					{
					paragraph "<b>Warning: A Modefix profile must be created, otherwise keypad exit delay tones, and Talker open/close contact messages are not generated"
					}
				}
			def fixtitle = modeFixChild +" Mode Fix Settings"
			section
				{
				input "logDebugs", "bool", required: false, defaultValue:false,
					title: "Log debugging messages? Nyckelharpa module only. Automatic shutoff after 60 minutes. Default: Off/False"
				if (verMsg>"")
					paragraph verMsg.substring(1,)
				}
			section
    			{
  				href(name: 'toglobalsPage', page: 'globalsPage', title: 'Globals Settings')
				}
//			section
//				{
//				app(name: "EntryDelayProfile", appName: "Nyckelharpa Contact", namespace: "arnbme", title: "Create A New Delay Profile", multiple: true)
//				}
			section
				{
				app(name: "ModeFixProfile", appName: "Nyckelharpa ModeFix", namespace: "arnbme", title: "${fixtitle}", multiple: false)
				}
			section
				{
				app(name: "TalkerProfile", appName: "Nyckelharpa Talker", namespace: "arnbme",
					title: "Create A New Talker Profile", multiple: true)
				}
//			section
//				{
//				app(name: "SimKypdProfile", appName: "Nyckelharpa Simkeypad", namespace: "arnbme", title: "Create A New Sim Keypad Profile", multiple: true)
//				}
			if (globalKeypadDevices)
				{
				section
					{
					app(name: "UserProfile", appName: "Nyckelharpa User", namespace: "arnbme", title: "Create A New User Profile", multiple: true)
					}
				}
			}
		else
			{
			section
				{
				paragraph "Please read the documentation, review and set global settings, then complete the install by clicking 'Save' above. After the install completes, you may set Delay, User and Modefix profiles"
				}
			section
    			{
  				href(name: 'toglobalsPage', page: 'globalsPage', title: 'Globals Settings')
				}
			}
		section
			{
			href (url: "https://github.com/arnbme/nyckelharpa/blob/master/README.md",
			title: "Documentation",
			style: "external")
			}
		section
			{
			paragraph "Nyckelharpa Version ${version()}"
			}
//		remove("Uninstall Nyckelharpa","Warning!!","This will remove the ENTIRE SmartApp, including all profiles and settings.")
		}
	}

def globalsPage(error_msg)
	{
	dynamicPage(name: "globalsPage", title: "Global Settings")
		{
//		if (settings.logDebugs) log.debug  "dynamicPage: globalsPage"
		section
			{
			if (error_msg instanceof String )
				{
				paragraph "<b>"+error_msg+"</b>"
				}
			input "globalDisable", "bool", required: true, defaultValue: false,
				title: "Disable All Functions. Default: Off/False"
 			input "globalForceSeconds", "number", required: false, range: "10..40", defaultValue: 15,
 				title: "Number of seconds allowed for Forced Arming window. Default: 15"
//			input "globalKeypadControl", "bool", required: true, defaultValue: true, submitOnChange: true,
//				title: "A real or simulated Keypad is used to arm and disarm Home Security Monitor (HSM). Default: On/True"
			input "globalKeypadDevices", "capability.securityKeypad", required: false, multiple: true, submitOnChange: true,
				title: "Iris V2/V3, and Centralite V2/V3, UEI, real and simulated devices used to arm and disarm HSM"
			if (globalKeypadDevices)
				{
//				input "globalPanic", "bool", required: true, defaultValue: true,
//					title: "Keypad Panic Key when available is Monitored. No Panic key? Set this flag on, add a User Profile, Pin Usage: Panic. Default: On/True"
//				input "globalSimContact", "capability.contactSensor", required: true,
//					title: "Simulated Contact Sensor (Must Monitor in HSM Contacts)"
				input "globalPinMsgs", "bool", required: false, defaultValue: true, submitOnChange: true,
					title: "Log pin entries. Default: On/True"
				if (globalPinMsgs)
					{
					input "globalPinLog", "bool", required: false, defaultValue:true,
						title: "Log Pin to log.trace?"
					input "globalPinPush", "bool", required: false, defaultValue:true,
						title: "Send Pin Push Notification?"
					input "globalPinPhone", "capability.notification", required: false, multiple:true,
						title: "Send Pin text message notification to these devices"
					}
				input "globalBadPinMsgs", "bool", required: false, defaultValue: true, submitOnChange: true,
					title: "Log invalid keypad entries, pins not found in a User Profile Default: On/True"
				if (globalBadPinMsgs)
					{
					input "globalBadPinLog", "bool", required: false, defaultValue:true,
						title: "Log Bad Pins to log.trace?"
					input "globalBadPinPush", "bool", required: false, defaultValue:true,
						title: "Send Bad Pin Push Notification?"
					input "globalBadPinPhone", "capability.notification", required: false, multiple:true,
						title: "Send Bad Pin text message notification to these devices"
					}
				}
//			paragraph "<b>Allow the following contacts to remain open when Arming HSM. Each contact generates a Virtual Contact Sensor that must be set in HSM following directions in section 7 of the Github Readme.md file. These contacts also generate Open and Close messages</b>"
			paragraph "<b>(Optional!) Generate virtual contact sensors for the following devices: allows the device to remain open when arming HSM, then participate with this app's Forced Arming feature. Also requires changing HSM's defined devices. Refer to this app's <a href='https://github.com/arnbme/nyckelharpa/blob/beta/README.md#adjustHSM' target='_blank' >Github Readme, Section 7</a> for more information</b>"
			input "globalAwayContacts", "capability.contactSensor", required: false, submitOnChange: true, multiple: true,
				title: "(Optional!) When arming Away: Contact sensors that may be open, then participate in forced arming"
			if (globalAwayContacts)
				{
				input (name: "globalAwayNotify", type:"enum", required: false, options: ["Push", "SMS","Talk"],multiple:true,
					title: "How to notify contact is open when arming Away")
				}
			input "globalHomeContacts", "capability.contactSensor", required: false, submitOnChange: true, multiple:true,
				title: "(Optional!) When arming Home: Contact sensors that may be open, then participate in forced arming"
			if (globalHomeContacts)
				{
				input (name: "globalHomeNotify", type:"enum", required: false, options: ["Push", "SMS","Talk"],multiple:true,
					title: "How to notify contact is open arming Home")
				}
			input "globalNightContacts", "capability.contactSensor", required: false, submitOnChange: true, multiple:true,
				title: "(Optional!) When arming Night: Contact sensors that may be open, then participate it forced arming"
			if (globalNightContacts)
				{
				input (name: "globalNightNotify", type:"enum", required: false, options: ["Push", "SMS","Talk"],multiple:true,
					title: "How to notify contact is open arming Night")
				}
			paragraph "<b>Other Open/Close contacts not specified above to monitor for Open and Close Messages only. Do not select the Child Virtual Contact Sensors. These contacts do not generate a child device.</b>"
			input "globalOtherContacts", "capability.contactSensor", required: false, submitOnChange: true, multiple:true,
				title: "(Optional!) Other Contact sensors not selected above to monitor for Open and Close messages"
			if (globalOtherContacts)
				{
				input (name: "globalOtherNotify", type:"enum", required: false, options: ["Push", "SMS","Talk"],multiple:true,
					title: "How to notify when these contacts are open or closed")
				}
/*			This seems redundant and useless in HE, HSM cant arm with open contacts,
 *			and we already give a warning prior to forced arming.
 *			Leaving this here just in case, but if code is needed it is in SHM Delay Child
 *
 *			paragraph "<b>For all monitored contact sensors, when system is armed and 'Maximum number of open door warning messages' is greater than 0: controls how many times and how often, system issues message 'Warning %device is open'</b>"
 *			input "globalOpenCycles", "number", required: false, range: "0..99", submitOnChange: true,
 *				title: "Maximum number of open door warning messages from 0 to 99 when system is armed"
 *			if (globalOpenCycles && globalOpenCycles>0)
 *				{
 *				input "globalOpenMsgDelay", "number", required: false, range: "1..15", defaultValue: 1,
 *					title: "Number of minutes between open door messages from 1 to 15"
 *				input (name: "globalOpenNotify", type:"enum", required: false, options: ["Push", "SMS","Talk"],multiple:true,
 *					title: "How to notify when these contacts remain open")
 *				}
 */
			input "globalAlarmDevices", "capability.alarm", required: false, multiple: true,
				title: "(Optional!) Beep these alarm devices when entry delay begins. Originally designed to beep a siren as a warning. Warning! Do not select a keypad here, it kills entry delay tones"
			input "globalBeeperDevices", "capability.tone", required: false, multiple: true, submitOnChange: true,
				title: "(Optional!) Beep/Chime these devices when any monitored contact sensor opens, and arm state is disarmed"
			if (globalBeeperDevices)
 				{
 				input "globalBeeperSeconds", "number", required: false, range: "1..10", submitOnChange: true,defaultValue: 2,
 						title: "Keypad sound duration in seconds for open contact Beep/Chime from 1 to 10 when arm state is disarmed. Default: 2"
				input "globalBeeperExitSound", "bool", required: false, defaultValue:false,
					title: "When available use warning fast beep from the keypad's exit delay tone"
				}
			if (!state.configured)
				{
				input "globalChildPrefix", "text", title:"Simulated Device Prefix. Simulated device names used with Forced HSM Arming, and the simulaed Panic Contact, start with this prefix.\n\nThe prefix must start with a letter and the only supported characters are letters, numbers and hyphens.\n\nThis setting can't be changed once you leave this screen.",
					description: "Simulated Device Prefix",
					defaultValue: 'NCKL', required: true
				}
			else
				paragraph "Simulated Device Prefix: ${globalChildPrefix}. (Remove app to reset)"
			input "sendPushMessage", "capability.notification", title: "Notification Devices: example: HE PhoneApp, Pushover, Twilio", multiple: true, required: false
			}
		}
	}

def globalsVerify() 				//edit globals data
	{
	def error_msg=""
//	if (globalKeypadDevices)		//deprecated in V1.0.0
//		{
//		globalKeypadDevices.each
//			{
//			if (it.typeName != 'Centralitex Keypad')
//				error_msg+="Keypad ${it.label} must be changed to use Centralitex Device handler\n\n"
//			}
//		}

	if (globalAlarmDevices && globalKeypadDevices)
		{
		globalAlarmDevices.each
			{
			globalKeypadDevices.each
				{
				keypad ->
				if (it.id == keypad.id)
					error_msg+="Keypad ${it.label} cannot be used for an alarm device\n\n"
				}
			}
		}
	if (error_msg>"")
		globalsPage(error_msg)
	else
		pageTwo()
	}

def pageTwo()
	{
	dynamicPage(name: "pageTwo", title: "<b>Global settings verified, press 'Done' to update global settings</b>", install: true, uninstall: true)
		{
		def workMsg
		def childModeFixError = true
		getChildApps().each
			{
			if (it.getLabel()=='Nyckelharpa ModeFix')
				{
				childModeFixError=false
				}
			}
		if (childModeFixError)
			{
			section
				{
				paragraph "<b>Warning: A Modefix profile must be created, otherwise keypad exit delay tones, and Talker open/close contact messages are not generated</b>"
				}
			}


		section
			{
			def armWindow=15
			if (globalForceSeconds)
				armWindow=globalForceSeconds
			paragraph "<b>Forced Arming Window is ${armWindow} seconds</b>"
			if (globalKeypadDevices)
				{
				workMsg="Keypads used for arming and disarming ${globalKeypadDevices}"
//				if (globalPanic)
//					workMsg += "\nKeypad Panic Pins are processed"
//				else
//					workMsg += "\nKeypad Panic Pins are ignored"
				if (globalPinMsgs)
					{
					if (globalPinLog || globalPinPush || globalPinPhone)
						{
						workMsg += "\nValid Pin entries posted to ["
						if (globalPinLog)
							workMsg += "log.trace "
						if (globalPinPush)
							workMsg += "Push "
						if (globalPinPhone)
							workMsg += "Notify $globalPinPhone"
						workMsg +=']'
						}
					else
						workMsg+= "\nValid Pin entries are not logged"
					}
				else
					workMsg+= "\nValid Pin entries are not logged"
				if (globalBadPinMsgs)
					{
					if (globalBadPinLog || globalBadPinPush || globalBadPinPhone)
						{
						workMsg += "\nInvalid Pin entries posted to ["
						if (globalBadPinLog)
							workMsg += "log.trace "
						if (globalBadPinPush)
							workMsg += "Push "
						if (globalBadPinPhone)
							workMsg += "Notify $globalBadPinPhone"
						workMsg +=']'
						}
					else
						workMsg+= "\nInvalid Pin entries are not logged"
					}
				else
					workMsg+= "\nInvalid Pin entries are not logged"
				}
			else
				workMsg = "Keypads are not defined for arming and disarming"
			paragraph workMsg

			if (globalAwayContacts)
				{
				workMsg="Arming Away: these contacts allow HSM arming when open $globalAwayContacts"
				if (globalAwayNotify)
					workMsg+= "\nNotifiy when Away contact is open by $globalAwayNotify"
				else
					workMsg+= "\nNo notification when Away contact is open at arming"
				paragraph workMsg
				}
			else
				paragraph "Standard HSM function when arming Away"

			if (globalHomeContacts)
				{
				workMsg="Arming Home: these contacts allow HSM arming when open $globalHomeContacts"
				if (globalHomeNotify)
					workMsg+= "\nNotifiy when Home contact is open by $globalHomeNotify"
				else
					workMsg+= "\nNo notification when Home contact is open at arming"
				paragraph workMsg
				}
			else
				paragraph "Standard HSM function when arming Home"

			if (globalNightContacts)
				{
				workMsg="Arming Night: these contacts allow HSM arming when open $globalNightContacts"
				if (globalNightNotify)
					workMsg+= "\nNotify when Night contact is open by $globalNightNotify"
				else
					workMsg+= "\nNo notification when Night contact is open at arming"
				paragraph workMsg
				}
			else
				paragraph "Standard HSM function when arming Night"


			if (globalAlarmDevices)
				paragraph "In addition to keypads, these devices beep or sound tones at Entry Delay $globalAlarmDevices"
			if (globalBeeperDevices)
				{
				workMsg = "These devices beep or sound tones when system is disarmed and monitored contact opens $globalBeeperDevices"
				workMsg+="\nKeypad device sound duration is $globalBeeperSeconds seconds"
				if (globalBeeperExitSound)
					workMsg+="\nKeypad's Fast Beep sound generated when available"
				paragraph workMsg
				}

			if (globalChildPrefix)
				paragraph "Simulated device names begin with $globalChildPrefix"
			workMsg=""
			getChildDevices().each
				{
				if (workMsg=="")
					workMsg="Generated Child Devices (this may change after clicking Done)"
				workMsg+="\n$it.label"
				}
			if (workMsg>"")
				paragraph workMsg

			if (sendPushMessage)
				paragraph "Devices receiving Push notifications: $sendPushMessage"
			else
				paragraph "Push devices are not defined"
			}
		}
	}

def installed() {
    log.info "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.info "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
}

def initialize()
	{
	if(settings.logDebugs)
		runIn(3600,logsOff)			// turns off debug logging after 60 min
	else
		unschedule(logsOff)
	if (!settings.globalDisable)
		{
		subscribe(globalKeypadDevices, 'codeEntered',  keypadCodeHandler)		//issued by Centralitex Keypad driver
		subscribe(globalKeypadDevices, 'armingIn',  armingInHeCodeHandler)		//issued by HE Keypad drivers on pin entry
		subscribe(location, "hsmSetArm", armingNonKeypadHandler)		//Added for using Non Keypad arming suchas dashbaord HSM status
//		if (globalPanic)
//			{
//		    subscribe (globalKeypadDevices, "contact.open", qssehe_alert_panic)
//		    }
		globalKeypadDevices?.each
			{
			if (it.hasCommand("disableInvalidPinLogging"))
				it.disableInvalidPinLogging(true)
			}
		subscribe(location, "hsmAlert", alertHandler)
		}
	subscribe(location, "hsmStatus", HeDoorsResetDelay)	//Added for using Hubitat Keypad Drivers
//	subscribe(location, "hsmStatus", verify_version)	//kill for now
//	verify_version("dummy_evt")

 	if (settings.globalOtherContacts)
		{
		subscribe(globalOtherContacts,"contact", MonitorDoorHandler)
		globalHomeContacts?.each
			{
			addNewChildDevice(it, 'Virtual Contact Sensor')
			}
		}


/*
 *		Maintain Child Devices
 *			One for each unique contact device, plus a panic contact device
 */
	if (!settings.globalChildPrefix)
		{return}
	state.configured = true			//locks the device prefix value so it cant be changed
 	if (settings.globalAwayContacts)
		{
		subscribe(globalAwayContacts,"contact", DoorHandler)
		globalAwayContacts?.each
			{
			addNewChildDevice(it, 'Virtual Contact Sensor')
			}
		}
 	if (globalHomeContacts)
		{
		subscribe(globalHomeContacts,"contact", DoorHandler)
		globalHomeContacts?.each
			{
			addNewChildDevice(it, 'Virtual Contact Sensor')
			}
		}
 	if (settings.globalNightContacts)
		{
		subscribe(globalNightContacts,"contact", DoorHandler)
		globalNightContacts?.each
			{
			addNewChildDevice(it, 'Virtual Contact Sensor')
			}
		}
//	deprecated panic device May 18, 2019 V0.1.8
//	restored Apr 02, 2020 v1.0.1 when any keypad uses HE driver
	globalKeypadDevices.find
		{
		if (it.typeName == 'Centralitex Keypad')
			return false
		def dvc = [name: "Panic Contact", id: "Panic Id", label: "Panic Contact"]
		addNewChildDevice(dvc, 'Virtual Contact Sensor')
		subscribe (globalKeypadDevices, 'alarm', HePanicHandler)
		return true
		}
	}

void logsOff(){
//	stops debug logging
	log.info "Nyckelharpa: debug logging disabled"
	app.updateSetting("logDebugs",[value:"false",type:"bool"])
}

def uninstalled()
	{
	getChildDevices().each
		{deleteOldChildDevice(it)}

/*	globalAwayContacts?.each
		{
		deleteOldChildDevice(it)
		}
	globalHomeContacts?.each
		{
		deleteOldChildDevice(it)
		}
	globalNightContacts?.each
		{
		deleteOldChildDevice(it)
		}
//	deprecated panic device May 18, 2019 V0.1.8
//	def dvc = [name: "Panic Contact", id: "Panic Id", label: "Panic Contact"]
//	deleteOldChildDevice(dvc)
*/	}

//  --------------------------Keypad support added Mar 02, 2018 V2-------------------------------
/*				Basic location modes are Home, Night, Away. This can be very confusing
Xfinity			Default mode
Centralite	Iris		Location	Default			Triggers	Xfinity		Iris
Icon		Button		Mode 		AlarmStatus		Routine		Icon lit	key lit
(off)		Off			Home		off				I'm Home	(none)		Off??
Stay		Partial		Night		stay			GoodNight	Stay		Partial
Night					Night		stay			GoodNight	Stay		Partial, but night key should not occur
Away		On			Away		away			GoodBye!	Away		Away


Xfinity			When Location Stay mode is defined and SHM Stay routine defined for Xfinity only
Centralite				Location	Default			Triggers	Xfinity
Keypad					Mode 		AlarmStatus		Routine		Icon lit
(off)					Home		off				I'm Home	(none)
Stay					Stay		stay			Stay		Stay
Night					Night		stay			GoodNight	Night
Away					Away		away			GoodBye!	Away
*/

def armingInHeCodeHandler(evt)
	{
//	Note Invalid he pins are not sent here
//	User entered a VALID pin code on a keypad using an HE keypad driver. data unfortunately does not include arming mode
//	get arming mode from the keypad
//	HSM seems to propogate the keypad entry to each keypad defined in HSM as arming/disarm device, hense the test for same arm state
//  attempting to use type physical or digital failed to work since HSM seems to get this wrong
	if (settings.globalDisable)
		{return false}			//just in case
	def keypad = evt.getDevice();
	def lclMap=new JsonSlurper().parseText(evt.data)
//	if (settings.logDebugs) log.debug  "armingInCodeHandler entered Keypad: $lclmap]${keypad.displayName} Value: ${evt.value} Data: ${evt.data}"
	atomicState.HeKeypadStatus = lclMap.armMode		//save state for alert routine
	if (atomicState.HeKeypadStatus == 'disarmed')
		globalKeypadDevices.off()
	atomicState.doorsdtim=0		//When using HE driver stops Modefix from executing checkOpenContacts
	if (settings.logDebugs) log.debug  "armingInHeCodeHandler entered Keypad: ${keypad.displayName} Requested-state:${atomicState.HeKeypadStatus}"
	}

def armingNonKeypadHandler(evt)
	{
	if (settings.globalDisable)
		{return false}			//just in case
	def newMode= [disarm: 'disarmed', armAway: 'armed away', armHome: 'armed home', armNight: 'armed night',disarmAll: 'disarmed',][evt.value]
	if (settings.logDebugs) log.debug  "armingNonKeypadHandler entered Value: ${evt.value} newMode: $newMode"
	if (newMode != atomicState?.HeKeypadStatus)				//system issues duplicates
		{
		if (newMode >"")
			atomicState.HeKeypadStatus = newMode		//save state for alert routine if not null
//		if (evt.value == 'disarmed' && globalKeypadDevices)	//should not be needed from non-keypad source
//			globalKeypadDevices.off()
		atomicState.doorsdtim=0					//NonKeypad Arming issue stop Modefix from executing checkOpenContacts
		}
	}

def HePanicHandler(evt)
	{
//	Process panic alert from HE keypad driver allowing a full response when system is disarmed
	if (globalDisable)
		{return false}			//just in case
	if (settings.logDebugs) log.debug  "HePanicContact routine entered ${evt.value}"
	if (evt.value=='siren')
		{
		closePanicContact()
		openPanicContact()
		runIn(4,closePanicContact)
		}
	}

def keypadCodeHandler(evt)
	{
//	User entered a code on a keypad
	if (settings.logDebugs) log.debug  "keypadCodeHandler entered ${evt.value} ${evt.data}"
	if (settings.globalDisable)
		{return false}			//just in case
	def keypad = evt.getDevice();
//	log.debug "keypadCodeHandler called: $evt by device : ${keypad.displayName} ${keypad.getId('motionTime')}"
	def codeEntered = evt.value as String				//the entered pin
	def modeEntered
	def lclMap = [:]
	def lmPin=false
	def lmPinValid=false
	def lmPinName
	if (evt.data[0]=='{')		//this is a json map from lock manager pin processing, passed map on event is returned as JSON <sigh>
		{
//		log.debug "slurping json"
		lclMap=new JsonSlurper().parseText(evt.data)
		lmPin=true
		lmPinValid = lclMap.isValid
		lmPinName = lclMap.name
		modeEntered = lclMap.armMode as Integer
		}
	else
		modeEntered = evt.data as Integer				//the selected mode off(0), stay(1), night(2), away(3)

	def itext = [dummy: "dummy"]						//external find it data or dummy map to fake it when pin not found
	def fireBadPin=true									//flag to stop double band pin fire. Caused by timing issue
	def	pinStatus='Rejected'
//															with Routine, Piston and UserRoutinePiston processing
	if (modeEntered < 0 || modeEntered> 3)				//catch an unthinkable bad mode, this is catastrophic
		{
		log.error "${app.label}: Unexpected arm mode ${modeEntered} sent by keypad!"
		keypad.sendInvalidKeycodeResponse()
		return false
		}
//	def currentarmMode = keypad.currentValue('armMode',true)
//	if (settings.logDebugs) log.debug ("Delayv2 codeentryhandler searching user apps for keypad ${keypad.displayName} ${evt.data} ${evt.value}")
	def userName=false;
	def badPin=true;
	def badPin_message = keypad.displayName + " Invalid pin entered: " + codeEntered
	def error_message=""
	def info_message=""
	def pinKeypadsOK=false;
	def damap=[dummy: "dummy"]				//dummy return map for Routine and Piston processing
	def itthepinusage
	if (lmPin)
		{
		if (lmPinValid)
			{
			if (lmPinName.matches("(.*)(?i)panic(.*)"))		//LCM Panic added V1.0.6
				{
				error_message = keypad.displayName + " Panic Pin ${codeEntered} received"
				itthepinusage='Panic'
				pinStatus='Panic'
				}
			else
				{
				pinStatus='Accepted'
				badPin=false
				userName=lmPinName
				}
			}
		else
			error_message = badPin_message
		}
	else
		{
	//	Try to find a matching pin in the pin child apps
		def userApps = getChildApps()		//gets all completed child apps Sep 20, 2018
	//	def userApps = findAllChildAppsByName('Nyckelharpa User')
		userApps.find
			{
			if (it.getName()=="Nyckelharpa User" && it.theuserpin == codeEntered)
	//		if (it.getInstallationState()=='COMPLETE' && it.theuserpin == codeEntered)	fails in HE
				{
				if (settings.logDebugs) log.debug  ("found the pin ${it.getName()} ${it.theuserpin} ${it.theusername} ")
	//			verify burn cycles
				itext=it										//save for use outside of find loop
				if (it.themaxcycles > 0)						//check if pin is burned
					{
					def atomicUseId=it.getId()+'uses'			//build unique atomic id for uses
					if (atomicState."${atomicUseId}" < 0)		//initialize if never set
						{atomicState."${atomicUseId}" = 1}
					else
						{atomicState."${atomicUseId}" = atomicState."${atomicUseId}" + 1}
					if (atomicState."${atomicUseId}" > it.themaxcycles)
						{
						if (settings.logDebugs) log.debug  "pin $codeEntered burned"
						error_message = keypad.displayName + " Burned pin entered for " + it.theusername
						}
					}
				if (error_message == "" && codeEntered == '0000' && modeEntered == 0 &&
					(it.thepinusage=='User' || it.thepinusage=='UserRoutinePiston') && it?.thepinIgnoreOff)
					{
					badPin=true
					error_message=badPin_message
					}
				else
					{
					badPin=false
					badPin_message=""
					}
	//			if (settings.logDebugs) log.debug  "matched pin ${it.theuserpin} $it.pinScheduled"
	//			When pin is scheduled verify Dates, Weekday and Time Range
				if (error_message=="" && it.pinScheduled)
					{
	//				keep this code in sync with similar code in Nyckelharpa Users
					def df = new java.text.SimpleDateFormat("EEEE")	//formatter for current time
					df.setTimeZone(location.timeZone)
					def day = df.format(new Date())
					def df2 = new java.text.SimpleDateFormat("yyyyMMdd")
					df2.setTimeZone(location.timeZone)
					def nowymd = df2.format(new Date());		//	the yyyymmdd format for comparing and processing
					def dtbetween=true
					def num_dtstart
					def num_dtend
					if (it.pinStartDt > "")
						num_dtstart=it.dtEdit(it.pinStartDt)
					if (it.pinEndDt > "")
						num_dtend=it.dtEdit(it.pinEndDt)
	//				if (settings.logDebugs) log.debug  "pin found with schedule $nowymd $num_dtstart $num_dtend"
	//				verify the dates
					if (it.pinStartDt>"" && it.pinEndDt>"")
						{
						if (num_dtstart > nowymd || num_dtend < nowymd)
							error_message = keypad.displayName + " dates out of range with pin for " + it.theusername
						}
					else
					if (it.pinStartDt>"")
						{
						if (num_dtstart > nowymd)
							error_message = keypad.displayName + " start date error with pin for " + it.theusername
						}
					else
					if (it.pinEndDt>"")
						{
						if (num_dtend < nowymd)
							error_message = keypad.displayName + " end date expired with pin for " + it.theusername
						}

	//				verify the weekdays
					if (error_message=="" && it.pinDays)
						{
						if (!it.pinDays.contains(day))
							error_message = keypad.displayName + " not valid on $day with pin for " + it.theusername
						}

	//				verify the hours stored by system as 2018-03-13T11:30:00.000-0400
					if (error_message=="" && it.pinStartTime>"" && it.pinEndTime>"")
						{
						def between = timeOfDayIsBetween(it.pinStartTime.substring(11,16), it.pinEndTime.substring(11,16), new Date(), location.timeZone)
						if (!between)
							error_message = keypad.displayName + " time out of range with pin for " + it.theusername
						}
					}

	//	Process pin mode and device restrictions
				if (error_message=="" && it.pinRestricted)
					{
					if (it.pinModes)
						{
						if (!it.pinModes.contains(location.mode))
							error_message = keypad.displayName + " mode: "+ location.mode + " invalid with pin for " + it.theusername
						}
					if (error_message=="" && (it.pinRealKeypads || it.pinSimKeypads))
						{
	//					this wont work sigh if (it.pinSimKeypads.contains(keypad.displayName))
						it.pinRealKeypads.each
							{kp ->
							if (kp.displayName == keypad.displayName)
								pinKeypadsOK=true
							}
						it.pinSimKeypads.each
							{kp ->
							if (kp.displayName == keypad.displayName)
								pinKeypadsOK=true
							}
						if (!pinKeypadsOK)
							error_message = keypad.displayName + " is unauthorized keypad with pin for " + it.theusername
						}
					}

	//			Verify pin usage
				if (error_message=="")
					{
	//				if (settings.logDebugs) log.debug  "processing the pin for ${it.thepinusage}"
					switch (it.thepinusage)
						{
						case 'User':
						case 'UserRoutinePiston':		//process arming now or get a possible keypad timeout
							userName=it.theusername
							pinStatus='Accepted'
							break
						case 'Disabled':
							error_message = keypad.displayName + " disabled pin entered for " + it.theusername
							pinStatus='Disabled'
							break
						case 'Ignore':
							error_message = keypad.displayName + " ignored pin entered for " + it.theusername
							pinStatus='Ignored'
							break
						case 'Routine':
	//						forced to do acknowledgeArmRequest here due to a hardware timeout on keypad
							pinStatus='Exec Routine'
							keypad.acknowledgeArmRequest(4)
							acknowledgeArmRequest(4,keypad);
							fireBadPin=false
							damap=process_routine(it, modeEntered, keypad)
							if (settings.logDebugs) log.debug  "Routine created ${damap}"
							if (damap?.err)
								error_message=damap.err
							else
							if (damap?.info)
								info_message=damap.info
							break
						case 'Panic':
	//						if (globalPanic)
	//							{
								error_message = keypad.displayName + " Panic Pin entered for " + it.theusername
	//							keypadPanicHandler(evt)
								itthepinusage='Panic'
								pinStatus='Panic'
	//							}

	//						else
	//							{
	//							pinStatus='Panic Inactive'
	//							error_message = keypad.displayName + " Panic Pin entered but globalPanic flag disabled with pin for " + it.theusername
	//							}
							break
						case 'Piston':
	//						forced to do acknowledgeArmRequest here due to a possible hardware timeout on keypad
							pinStatus='Piston'
							keypad.acknowledgeArmRequest(4)
							acknowledgeArmRequest(4,keypad);
							fireBadPin=false
							damap=process_piston(it, modeEntered, keypad)
							if (damap?.err)
								error_message=damap.err
							else
							if (damap?.info)
								info_message=damap.info
							else
								error_message = "Process Piston returned bad data: ${dmap} "
							break
						default:
							pinStatus='Bad pin type'
							userName=it.theusername
							break
						}
					}
				return true				//this ends the ***find*** loop, not the function
				}
			else
				{return false}			//this continues the ***find*** loop, does not end function
			}
		}

//	Now done with find loop and editing the pin entered on the keypad

	if (error_message!="")									// put out any messages to notification log
		{
		badPin=true
//		if (settings.logDebugs) log.debug  "${error_message} info ${info_message}"
		doPinNotifications(error_message, itext)
		}
	else
	if (info_message!="")
		{
		doPinNotifications(info_message, itext)
		}

//	Was pin not found
/*  in theory acknowledgeArmRequest(4) and sendInvalidKeycodeResponse() send same command
	but not working that way. Look at this when I get some time
*/

    keypad.pinStatusSet(pinStatus)
	if (badPin)
		{
		if (fireBadPin)
			{
			keypad.acknowledgeArmRequest(4)				//always issue badpin very long beep
			acknowledgeArmRequest(4,keypad);
//			log.debug "about to set panic contact open ${itthepinusage}"
			if (itthepinusage=='Panic')
				keypad.panicContact()			//have dh open contact trigger panic with Custome HSM rule
			}
		else
			pinStatus='Rejected'
		if (globalBadPinMsgs && badPin_message !="")
			doBadPinNotifications (badPin_message, itext)
/*
**		Deprecated this logic on Mar 18, 2018 for better overall operation
		if (globalBadPins==1)
			{
			keypad.acknowledgeArmRequest(4)			//sounds a very long beep
			acknowledgeArmRequest(4,keypad);
			}
		else
			{
			if (atomicState.badpins < 0)			//initialize if never set
				{atomicState.badpins=0}
	    	atomicState.badpins = atomicState.badpins + 1
	    	if (atomicState.badpins >= globalBadpins)
	    		{
				keypad.acknowledgeArmRequest(4)		//sounds a very long beep
				acknowledgeArmRequest(4,keypad);
				atomicState.badpins = 0
    			}
			else
				{
				keypad.sendInvalidKeycodeResponse()	//sounds a medium duration beep
				acknowledgeArmRequest(4,keypad);
				}
    		}
*/

		return;
 		}


//	was this pin associated with a person
	if (!userName)									//if not a user pin, no further processing
		{
		keypad.sendInvalidKeycodeResponse()			//sounds a medium duration beep
		return
		}

//	Oct 21, 2018 verify contacts are closed prior to arming or exit delay
//	Message sensor, sensor open, Arming canceled
	if (modeEntered == 0)
		{}
	else
	if (modeEntered == 3 && globalAwayContacts)
		{
		if (!checkOpenContacts(globalAwayContacts, globalAwayNotify, keypad))
			{
		    keypad.pinStatusSet("OpenContact")
			return
			}
		}
	else
	if (modeEntered == 2 && globalHomeContacts)
		{
		if(!checkOpenContacts(globalHomeContacts, globalHomeNotify, keypad))
			{
		    keypad.pinStatusSet("OpenContact")
			return
			}
		}
	else
	if (modeEntered == 1 && globalNightContacts)
		{
		if(!checkOpenContacts(globalNightContacts, globalNightNotify, keypad))
			{
		    keypad.pinStatusSet("OpenContact")
			return
			}
		}
	keypad.acknowledgeArmRequest(modeEntered) 		//keypad demands a followup light setting or all lights blin
	keypad.nyckelharpaValidPin('0'+modeEntered, userName) //Post lastUserName field when using Nyckelharpa pins
//	acknowledgeArmRequest(modeEntered,keypad);			//Used with Internet keypad only
//	atomicState.badpins=0		//reset badpin count
	def HSMarmModes=['disarm','armHome','armNight','armAway']
	def armModes=['Home','Stay','Night','Away']
	def message = keypad.displayName + " set HSM State to " + HSMarmModes[modeEntered] + " with pin for " + userName
	def aMap = [data: [codeEntered: codeEntered, armMode: armModes[modeEntered]]]
	def mf
	def am
//	globalSimContact.close()
//	closePanicContact()
	execRoutine(aMap.data)
	sendLocationEvent(name: "hsmSetArm", value: HSMarmModes[modeEntered], descriptionText: "Nyckelharpa ${keypad.displayName}")
	doPinNotifications(message,itext)

//	Process remainder of UserRoutinePiston settings
/*	if (itext.thepinusage == 'UserRoutinePiston')
		{
		damap=process_routine(itext, modeEntered, keypad)
		if (damap?.err)
			{
			if (damap.err != "nodata")
				doPinNotifications(damap.err,itext)			//no message when no routines where coded
			}
		else
		if (damap?.info)
			doPinNotifications(damap.info,itext)
		else
			doPinNotifications("Process Routine returned bad data: ${damap}",itext)

		damap=process_piston(itext, modeEntered, keypad)
		if (damap?.err)
			{
			if (damap.err != "nodata")
				doPinNotifications(damap.err,itext)			//no message when no routines where coded
			}
		else
		if (damap?.info)
			doPinNotifications(damap.info,itext)
		else
			doPinNotifications("Process Piston returned bad data: ${damap}",itext)
		}
*/	}
void testnyckelharpaValidPin(armMode,userName)
	{
	log.debug "testnyckelharpaValidPin armMode: ${armMode}  userName:${userName}"
	}
	
def acknowledgeArmRequest(armMode,keypad)
//	Post the status of the pin to the shmdelay_oauth db table
	{
//	if (settings.logDebugs) log.debug  "acknowledgeArmRequest entererd ${keypad?.getTypeName()} ${keypad.name}"
	if (keypad?.getTypeName()!="Internet Keypad")
		{return false}
//	keypad.properties.each { k,v ->	if (settings.logDebugs) log.debug  "${k}: ${v}"}
	def pinstatus
	if (armMode <  0 || armMode > 3)
		pinstatus="Rejected"
	else
		pinstatus ="Accepted"
/*	def uri='https://www.arnb.org/shmdelay/qsse.php'

	def simKeypadDevices = getChildApps()		//gets all completed child apps Sep 20, 2018
//	def simKeypadDevices=findAllChildAppsByName('Nyckelharpa Simkypd')
	simKeypadDevices.each
		{
		if (it.getName()=="Nyckelharpa Simkypd" && it.simkeypad.name == keypad.name)
			{
			uri+='?i='+it.getAtomic('accessToken').substring(0,8)
			uri+='&p='+ pinstatus
//			if (settings.logDebugs) log.debug  "firing php ${uri} ${it.simkeypad.name} ${it.getAtomic('accessToken')}"
			try {
				asynchttp_v1.get('ackResponseHandler', [uri: uri])
				}
			catch (e)
				{
				if (settings.logDebugs) log.debug  "qsse.php Execution failed ${e}"
				}
			}
		}
*/	}

def qsse_status_mode(status,mode)
//	store the status of the ST status and mode to the shmdelay_oauth db table for all simulated keypads
	{
	return false				//bypass in HE until needed Apr 11, 2019
	def st_status=status
	if (!st_status)
		st_status = location.currentState("alarmSystemStatus").value
	if (st_status=="off")
		st_status="Disarmed"
	else
		st_status="Armed%20("+st_status+")"	//need to base64 to get this to send
	def st_mode=mode
	if (!st_mode)
		st_mode = location.currentMode
	def uri
	findAllChildAppsByName('Nyckelharpa Simkypd').each
		{
		if (it.getInstallationState()=='COMPLETE')
			{
			uri='https://www.arnb.org/shmdelay/qsse.php'
			uri+='?i='+it.getAtomic('accessToken').substring(0,8)
			uri+='&s='+ st_status
			uri+='&m='+ st_mode
//			if (settings.logDebugs) log.debug  "firing php ${uri} ${it.simkeypad.name} ${it.getAtomic('accessToken')}"
			try {
				asynchttp_v1.get('ackResponseHandler', [uri: uri])
				}
			catch (e)
				{
				if (settings.logDebugs) log.debug  "qsse.php Execution failed ${e}"
				}
			}
		}
	}

def ackResponseHandler(response, data)
	{
    if(response.getStatus() != 200)
    	sendNotificationEvent("Nyckelharpa qsse.php HTTP Error = ${response.getStatus()}")
	}

def qssehe_alert_panic(evt)			//executed when keypad's contact is opened for panic
	{
	qssehe_alert('PANIC')
	}
def qssehe_alert(alert)
//	store the HE hsmAlert value into Arnb.org db table shmdelay_oauthhe for all keypads with a sseKey
	{
	return
	if (settings.logDebugs) log.debug  "qssehe_alert entered ${alert}"
	def sseKey
	def uri
	if (globalKeypadDevices)
		{
		globalKeypadDevices.each
			{
			sseKey = it?.getDataValue('sseKey')
			if (sseKey >" ")
				{
				uri='http://192.168.0.101/cz/shmdelay/qssehe.php'
				uri+='?i='+ sseKey
				uri+='&a='+ alert
				log.debug "qssehe uri "+uri
				try {
					asynchttpGet('qssehe_AsyncHandler', [uri: uri])
					}
				catch (e)
					{
					if (settings.logDebugs) log.debug  "qssehe.php Execution failed ${e}"
					}
				}
			}
		}
	}

def qssehe_AsyncHandler(response, data)
	{
    if(response.getStatus() != 200)
    	log.error ("Nyckelharpa Modefix qssehe.php HTTP Error = ${response.getStatus()}")
	}
def execRoutine(aMap)
	{
	def armMode = aMap.armMode
	def kMap = [mode: armMode, dtim: now()]	//save mode dtim any keypad armed/disarmed the system for use with
//											  not ideal prefer alarmtime but its before new alarm time is set
	def kMode=false							//new keypad light setting, waiting for mode to change is a bit slow
	def kbMap = [value: armMode, source: "keypad"]
	if (settings.logDebugs) log.debug  "execRoutine aMap: ${aMap} kbMap: ${kbMap} armMode: ${armMode}"
	if (armMode == 'Home')
		{
		keypadLightHandler(kbMap)
		}
	else
	if (armMode == 'Away')
		{
		keypadLightHandler(kbMap)
		}
	else
	if (armMode == 'Stay')
		{
		keypadLightHandler(kbMap)
		}
	else
	if (armMode == 'Night')
		{
		keypadLightHandler(kbMap)
		}
	atomicState.kMap=kMap					//Nyckelharpa Contact DoorOpens and MotionSensor active functions
	}

def keypadLightHandler(evt)						//set the Keypad lights
	{
	def	theMode=evt.value						//This should be a valid SHM Mode
	def simkeypad
	if (settings.logDebugs) log.debug  "keypadLightHandler entered ${evt} ${theMode} source: ${evt.source}"
/*  Simulated keypad not implemnted in HE
	def simKeypadDevices=findAllChildAppsByName('Nyckelharpa Simkypd')
	simKeypadDevices.each
		{
		if (it?.getInstallationState()!='COMPLETE')
			{
			if (settings.logDebugs) log.debug  "${it.keypad} warning device not complete, please save the profile"
			}
		else
			{
			simkeypad=it.simkeypad		//get device
			keypadLighton(evt,theMode,simkeypad)
			}
		}
*/	globalKeypadDevices.each
		{ keypad ->
			if (keypad.typeName == 'Centralitex Keypad')
				keypadLighton(evt,theMode,keypad)
		}
	}

def	keypadLighton(evt,theMode,keypad)
	{
	if (settings.logDebugs) log.debug  "keypadLighton entered $evt $theMode $keypad ${keypad?.getTypeName()}"
	def currkeypadmode=""
	if (theMode == 'Home')					//Alarm is off
		{keypad.setDisarmed()}
	else
	if (theMode == 'Stay')
		{
		keypad.setArmedStay()				//lights Partial light on Iris, Stay Icon on Xfinity/Centralite
		}
	else
	if (theMode == 'Night')					//Iris has no Night light set Partial on
		{
		if (['3400','3400-G','URC4450BC0-X-R'].contains(keypad?.data.model) || 	keypad?.getTypeName()=="Internet Keypad")
			keypad?.setArmedNight()
		else
			keypad?.setArmedStay()
		}
	else
	if (theMode == 'Away')					//lights ON light on Iris
		{keypad.setArmedAway()}
	}
/* this routine is deprecated as of May 18, 2019
def keypadPanicHandler(evt)
	{
	if (settings.logDebugs) log.debug  "keypadPanicHandler entered, ${evt}"
	if (globalDisable || !globalPanic)
		{return false}			//just in case

	def alarmstatus = location.hsmStatus	//get HE alarm status
	def keypad=evt.getDevice()				//set the keypad name
	doPanicNotifications(keypad)			//get messages out
	def panic_map=[data:[cycles:5, keypad: keypad.name]]
	if (settings.logDebugs) log.debug  "keypadPanicHandler status alarmstatus: ${alarmstatus} panic map: ${panic_map} keypad: ${keypad.name}"
	if (alarmstatus.substring(0,5) != 'armed')
		{
//		unschedule(execRoutine)				//Kill any delayed arm/disarm requests
		globalNightContacts?.each
			{
			getChildDevice("$globalChildPrefix${it.id}").close()
			}
		setLocationMode('Night')
		sendLocationEvent(name: "hsmSetArm", value: "armNight")
		runIn(1, keypadPanicExecute,panic_map)
		}
	else
		{
		keypadPanicExecute(panic_map.data)		//Panic routine only uses the device name, should be ok
		}
	}
*/
/* this routine is deprecated May 18, 2019 */
/*def keypadPanicExecute(panic_map)						//Panic mode requested
	When system is armed: Open simulated sensor
**	When system is not armed: Wait for it to arm, open simulated sensor
**	Limit time to 5 cycles around 9 seconds of waiting maximum

	{
	def alarmstatus = location.hsmStatus	//get HE alarm status
	if (alarmstatus.substring(0,5) != 'armed')
		{
		if (settings.logDebugs) log.debug  "keypadPanicExecute entered $panic_map"
		if (panic_map.cycles > 1)
			{
			def cycles=panic_map.cycles-1
			def keypad=panic_map.keypad
			def newpanic_map=[data:[cycles: cycles, keypad: keypad]]
			runIn(2, keypadPanicExecute,newpanic_map)
			return false
			}
		else
			{
			log.error "System did not arm in 10 seconds, unable to create an intrusion"
			return false
			}
		}
	closePanicContact()
	openPanicContact()
	runIn(4,closePanicContact)
	qsse_status_mode(false,"**Panic**")
	}
*/

//	Process response from async execution of WebCore Piston
def getResponseHandler(response, data)
	{
    if(response.getStatus() != 200)
    	sendNotificationEvent("Nyckelharpa Piston HTTP Error = ${response.getStatus()}")
	}


//	this routine is currently not being executed killed the subscribe
def verify_version(evt)		//evt needed to stop error whne coming from subscribe to alarm change
	{
	if (settings.logDebugs) log.debug  "verify_version entered ${evt.getProperties().toString()}"
//	if (settings.logDebugs) log.debug  "evt data ${evt.getProperties().toString()}"
	def uri='https://www.arnb.org/shmdelay/'
//	uri+='?lat='+location.latitude					//Removed May 01, 2018 deemed obtrusive
//	uri+='&lon='+location.longitude
//	uri+='?hub='+location.hubs[0].encodeAsBase64()   //May have quotes and other stuff
//	uri+='&zip='+location.zipCode
//	uri+='&cnty='+location.country
//  uri+='&eui='+location.hubs[0].zigbeeEui
	def childApps = getChildApps()		//gets all completed child apps
	def vdelay=version()
	def vchild=''
	def vmodefix=''
	def vuser=''
	def vkpad=''
	def vtalk=''
	def vchildmindelay=9999
	def mf								//modefix module
	childApps.find 						//change from each to find to speed up the search
		{
//		if (settings.logDebugs) log.debug  "child ${it.getName()}"
//		if (vchild>'' && vmodefix>'' && vuser>''&& vkpad>''&& vtalk>'')		removed V2.1.9 Oct 16, 2018
//			return true														not getting minimum nonkeypad delay time
//		else
		if (it.getName()=="Nyckelharpa Contact")
			{
			if (vchild=='')
				vchild=it?.version()
			if (it?.theexitdelay < vchildmindelay)
				vchildmindelay=it.theexitdelay					//2.1.0 Oct 15, 2018 get delay profile exit delay time
			return false
			}
		else
		if (it.getName()=="Nyckelharpa ModeFix")				//should only have 1 profile
			{
			mf=it											//save app for later
			vmodefix=it?.version()
			return false
			}
		else
		if (it.getName()=="Nyckelharpa Simkypd Child")
			{
			if (vkpad=='')
				vkpad=it?.version()
			return false
			}
		else
		if (it.getName()=="Nyckelharpa Talker Child")
			{
			if (vtalk=='')
				vtalk=it?.version()
			return false
			}
		else
		if (it.getName()=="Nyckelharpa User")
			{
			if (vuser=='')
				vuser=it?.version()
			return false
			}
		}
/*	uri+="&p=${vdelay}"
    uri+="&c=${vchild}"
    uri+="&m=${vmodefix}"
    uri+="&u=${vuser}"
    uri+="&k=${vkpad}"
    uri+="&t=${vtalk}"
    if (settings.logDebugs) log.debug  "${uri}"

	try {
		asynchttp_v1.get('versiongetResponseHandler', [uri: uri])
		}
	catch (e)
		{
		if (settings.logDebugs) log.debug  "Execution failed ${e}"
		}
	qsse_status_mode(evt.value,false)
*/
//	Moved exitdelay non-keypad talk message to here from Nyckelharpa Contact, V2.1.9 Oct 15, 2018
	def vaway=evt?.value
//	if (settings.logDebugs) log.debug  "Talker setup1 $vchildmindelay $vtalk $vaway"

//	Nov 19, 2018 V2.2.2 User exit event not running in Nyckelharpa BuzzerSwitch
//	if (vtalk=='')			//talker profile not defined, return
//		return false

	if (vchildmindelay < 1)		//a nonkeypad time was set to 0
		return false;

	if (vchildmindelay == 9999)	//no non-keypad exit delay time?
		return false;

//	Nov 19, 2018 V2.2.3 Check Modefix data if State/Mode has an exit delay
	def daexitdelay=false

	def theMode = location.currentMode

	if (evt?.value == "stay" || evt?.value == "away")
		{
		if (vmodefix > '0.1.4')
			{
			def am="${evt?.value}Exit${theMode}"
			daexitdelay = mf."${am}"
			if (settings.logDebugs) log.debug  "Modefix Version ${vmodefix} the daeexitdelay is ${daexitdelay} amtext: ${am}"
			}
		else
		if (evt?.value == "away")
			daexitdelay=true
		}

	if (!daexitdelay)
		return false

	def locevent = [name:"Nyckelharpatalk", value: "exitDelayNkypd", isStateChange: true,
		displayed: true, descriptionText: "Issue exit delay talk event", linkText: "Issue exit delay talk event",
		data: vchildmindelay]

//	if (settings.logDebugs) log.debug  "Talker setup2 $vchildmindelay $vtalk"
//	def alarm = location.currentState("alarmSystemStatus")
//	def lastupdt = alarm?.date.time
	def myEvents = getLocationEventsSince("hsmStatus", new Date() - 30, [max:1])
	def alarmSecs = Math.round( myEvents[0].unixTime / 1000)

//	get current time in seconds
	def currSecs = Math.round(now() / 1000)	//round back to seconds
	def kSecs=0					//if defined in if statment it is lost after the if
	def kMap
	def kduration
	def globalKeypadControl=true
	if (globalKeypadControl)
		{
		kMap=atomicState['kMap']	//no data returns null
		if (kMap>null)
			{
			kSecs = Math.round(kMap.dtim / 1000)
			kduration=alarmSecs - kSecs
//			if (settings.logDebugs) log.debug  "Talker fields $kSecs $alarmSecs $kduration $vchildmindelay"
			if (kduration > 8)
				{
				sendLocationEvent(locevent)
//				if (settings.logDebugs) log.debug  "Away Talker from non keypad triggered"
				}
			}
		else	// no atomic map issue message
			{
			sendLocationEvent(locevent)
			}
		}
	else
		{
		sendLocationEvent(locevent)
		}

	}

//	Process response from async execution of version test to arnb.org
def versiongetResponseHandler(response, data)
	{
    if(response.getStatus() == 200)
    	{
		def results = response.getJson()
		if (settings.logDebugs) log.debug  "Nyckelharpa good response ${results.msg}"
		if (results.msg != 'OK')
    		sendNotificationEvent("${results.msg}")
        }
    else
    	sendNotificationEvent("Nyckelharpa Version Check, HTTP Error = ${response.getStatus()}")
    }


def	process_routine(it, modeEntered, keypad)
	{
//	the initial msg in rmap is the default error message
	def rmap = [err: "Process Routine " + keypad.displayName + " unknown keypad mode:" + modeEntered + " with pin for " + it.theusername]
//	modeEntered: off(0), stay(1), night(2), away(3)
	if (it?.thepinroutine)
		{
		rmap=fire_routine(it, modeEntered, keypad, it.thepinroutine[0], "All")
		}
	else
	if (modeEntered == 0 && it?.thepinroutineOff)
		{
		rmap=fire_routine(it, modeEntered, keypad, it.thepinroutineOff[0], "Off")
		}
	else
	if (modeEntered == 3 && it?.thepinroutineAway)
		{
		rmap=fire_routine(it, modeEntered, keypad, it.thepinroutineAway[0], "Away")
		}
	else
	if ((modeEntered == 1 || modeEntered == 2) && it?.thepinroutineStay)
		{
		rmap=fire_routine(it, modeEntered, keypad, it.thepinroutineStay[0], "Stay")
		}
	else
	if (it.pinuseage == "UserRoutinePiston" && modeEntered > -1 && modeEntered < 4 )	//nothing to process
		{
		rmap = [err: "nodata"]
		}
	return rmap		//return with an err or info map message
	}

def fire_routine(it, modeEntered, keypad, theroutine, textmode)
	{
	def rmsg = keypad.displayName + " Mode:" + textmode + " executed routine " + theroutine + " with pin for " + it.theusername
	def result
	location.helloHome?.execute(theroutine)
	if (it.thepinusage == "Routine")
		result = [err: rmsg]
	else
		result = [info: rmsg]
	return result
	}

def process_piston(it, modeEntered, keypad)
	{
	def rmap = [err: "Process Piston " + keypad.displayName + " unknown keypad mode:" + modeEntered + " with pin for " + it.theusername]
//	modeEntered: off(0), stay(1), night(2), away(3)
	if (it.thepinpiston)
		{
		rmap=fire_piston(it, modeEntered, keypad, it.thepinpiston, "All")
		}
	else
	if (modeEntered == 0 && it.thepinpistonOff)
		{
		rmap=fire_piston(it, modeEntered, keypad, it.thepinpistonOff, "Off")
		}
	else
	if (modeEntered == 3 && it.thepinpistonAway)
		{
		rmap=fire_piston(it, modeEntered, keypad, it.thepinpistonAway, "Away")
		}
	else
	if ((modeEntered == 1 || modeEntered == 2) && it.thepinpistonStay)
		{
		rmap=fire_piston(it, modeEntered, keypad, it.thepinpistonStay, "Stay")
		}
	else
	if (it.pinuseage == "UserRoutinePiston" && modeEntered > -1 && modeEntered < 4 )	//nothing to process
		{
		rmap = [err: "nodata"]
		}
	return rmap		//return with an err or info map message
	}

def fire_piston(it, modeEntered, keypad, thepiston, textmode)
	{
	def rmsg = keypad.displayName + " Mode:" + textmode + " executed piston with pin for " + it.theusername
	def result
	try {
		def params = [uri: thepiston]
//		def params = [uri: "https://www.google.com"]		//use to test
		asynchttp_v1.get('getResponseHandler', params)
		}
	catch (e)
		{
		rmsg = rmsg + " Piston Failed: " + e
		}
	if (it.thepinusage == "Piston")
		result = [err: rmsg]
	else
		result = [info: rmsg]
	return result

	}

// log, send notification, SMS message for pin entry, base code from Nyckelharpa Contact
def doPinNotifications(localmsg, it)
	{
	if (settings.logDebugs) log.debug  "doPinNotifications entered ${localmsg} ${it}"
	if (it?.pinMsgOverride)
		{
		if (it.UserPinLog)
			{
			sendNotificationEvent(localmsg)
			}
		if (it.UserPinPush)
			{
			if (sendPushMessage)
				sendPushMessage.deviceNotification(localmsg)
			}
		if (it.UserPinPhone)
			{
			globalPinPhone.deviceNotification(localmsg)
			}
		}
	else
	if (globalPinMsgs)
		{
		if (globalPinLog)
			{
			sendNotificationEvent(localmsg)
			}
		if (globalPinPush)
			{
			if (sendPushMessage)
				sendPushMessage.deviceNotification(localmsg)
			}
		if (globalPinPhone)
			{
			globalPinPhone.deviceNotification(localmsg)
			}
		}
	}

def doBadPinNotifications(localmsg, it)
	{
	if (settings.logDebugs) log.debug  "doBadPinNotifications entered ${localmsg} ${it}"
	if (globalBadPinLog)
		{
		sendNotificationEvent(localmsg)
		}
	if (globalBadPinPush)
		{
		if (sendPushMessage)
			sendPushMessage.deviceNotification(localmsg)
		}
	if (globalBadPinPhone)
		{
		globalBadPinPhone.deviceNotification(localmsg)
		}
	}

def doPanicNotifications(keypad)
	{
	if (settings.logDebugs) log.debug  "doPanicNotifications entered ${localmsg}"
	def message = "PANIC issued by $keypad "
	if (global911 > ""  || globalPolice)
		{
		def msg_emergency
		if (global911 > "")
			{
			msg_emergency= ", call Police at ${global911}"
			}
		if (globalPolice)
			{
			if (msg_emergency==null)
				{
				msg_emergency= ", call Police at ${globalPolice}"
				}
			else
				{
				msg_emergency+= " or ${globalPolice}"
				}
			}
		message+=msg_emergency
		}
	else
		{
		message+=" by (Nyckelharpa App)"
		}

	sendNotificationEvent(message)
	if (sendPushMessage)
		sendPushMessage.deviceNotification(message)
	if (globalPinPhone)
		{
		globalPinPhone.deviceNotification(message)
		}
	else
	if (globalBadPinPhone)
		{
		globalBadPinPhone.deviceNotification(message)
		}
	}

/*
 *  Sanity note: Routine checks the real contacts, but forces the monitored simulated contact closed
 *	There appears to be no need to change the status of the simulated contact to open
 */
def checkOpenContacts (contactList, notifyOptions, keypad)
	{
	def checkOpenReturn=false
	def lastDoorsDtim=0
	if (atomicState?.doorsdtim)
		lastDoorsDtim=atomicState?.doorsdtim	//last time doors failed
	def contactmsg=''
	def duration = now() -lastDoorsDtim
	def evt
	def armWindow=15
	if (settings.logDebugs) log.debug  "checkOpenContacts $contactList $notifyOptions $keypad lastDoorsDtim: $lastDoorsDtim Duration: $duration"
	contactList.each
		{
//		if (settings.logDebugs) log.debug  "${it} ${it.currentContact}"
		if (it.currentContact=="open")
			{
			if (location.hsmStatus=='disarmed' || location.hsmStatus == 'allDisarmed')
				getChildDevice("$globalChildPrefix${it.id}").open()		//sync child device to real V1.0.4
			if (contactmsg == '')
				{
				if (globalForceSeconds)
					armWindow=globalForceSeconds
				if (duration > (armWindow * 1000) || duration < 3000)
					{
					if (keypad)										//keypad is false when non keypad arming
						{
						if (keypad.data.model.substring(0,3)=='URC')	//V1.0.6 Apr 25, 2020
							keypad.acknowledgeArmRequest(0)				//UEI silent on code 4
						else
							keypad.acknowledgeArmRequest(4)				//always issue badpin very long beep, silence on UEI
						runIn(2,'solokeypad_delayBeep', [data:['keypad':keypad.getId(), 'beeps':2]])
//                      timing delay optimized for Iris V2
//						Iris V2 reject tone, pause, 2 beeps with old beep|3 or 4 chirps new beep
//						Iris V3 Beeps 3 times then gives reject tone (old beep) unable to test new beep
//						Centralite/xfinity reject tone, long pause, 2 beeps
//						UEI silence for 2 seconds, beep for 2 seconds
						}
					contactmsg = 'Arming Canceled. '+it.displayName
					atomicState.doorsdtim=now()
					}
				else
					{
//					atomicState.doorsdtim=0		//V1.0.7 allows arming away from non keypad source such as dashboard
//					unschedule(HeDoorsReset)	//V1.0.7 tried but not needed, leavie it just in case
					contactmsg = 'Arming Forced. '+it.displayName
					checkOpenReturn = true
					getChildDevice("$globalChildPrefix${it.id}").close()		//close the child device
//					evt = [value: "close", displayName: "${globalChildPrefix}-${it.name}", deviceId: "${it.id}"]
//					DoorHandler(evt)
					}
				}
			else
				{
				contactmsg += ', '+it.displayName
				if (checkOpenReturn)
					{
					getChildDevice("$globalChildPrefix${it.id}").close()		//close the child device
//					evt = [value: "close", displayName: "${globalChildPrefix}-${it.name}", deviceId: "${it.id}"]
//					DoorHandler(evt)
					}
				}
			}
		else
		if (location.hsmStatus=='disarmed' || location.hsmStatus == 'allDisarmed')
			getChildDevice("$globalChildPrefix${it.id}").close()		//sync child device to real v1.0.4
		}
	if (contactmsg>'')
		{
		contactmsg += ' is open.'
		if (checkOpenReturn==false)
			contactmsg += " Rearming within ${armWindow} seconds will force arming"
		notifyOptions.each
			{
			if (it=='Notification log')
				{
				sendNotificationEvent(contactmsg)
				}
			else
			if (it=='Push')
				{
				if (sendPushMessage)
					sendPushMessage.deviceNotification(contactmsg)
				}
			else
			if (it=='SMS' && globalPinPhone)
				{
				it.deviceNotification(contactmsg)
				}
			else
			if (it=='Talk')
				{
				def loceventcan = [name:"Nyckelharpatalk", value: "ArmCancel", isStateChange: true,
					displayed: true, descriptionText: "Issue exit delay talk event", linkText: "Issue exit delay talk event",
					data: contactmsg]
				sendLocationEvent(loceventcan)
				}
			}
		return checkOpenReturn
		}
	return true
	}

/*	used by Nyckelharpa Modefix to get atomicState.doorsdtim	*/
def getAtomicdoorsdtim()
	{
//	if (settings.logDebugs) log.debug  "getAtomicdoorsdtim was entered"
	def lastDoorsDtim=0
	if (atomicState?.doorsdtim)
		lastDoorsDtim=atomicState.doorsdtim	//last time doors failed
	return lastDoorsDtim
	}

/*	used by Nyckelharpa Modefix to kill forced rearm	*/
def killAtomicdoorsdtim()
	{
//	if (settings.logDebugs) log.debug  "killAtomicdoorsdtim was entered"
//	atomicState.doorsdtim=now()-600000			//now - 10minutes deprecated V1.1.0
	atomicState.doorsdtim=0
	}


def sendNotificationEvent(txt)				//ST sendNotificationEvent command not supported in HE
	{
	log.trace ("${txt}")
    }

/*
 *	This maintains the child contact devices
 *   ST addChildDevice(String typeName, String deviceNetworkId, hubId, Map properties)
 *   HE addChildDevice(String typeName, String deviceNetworkId, Map properties = [:]) this does not work needs null hubid!
 */
def addNewChildDevice(deviceData, deviceType) {
	if (getChildDevice("$globalChildPrefix${deviceData.id}"))
		return
	log.debug "addNewChildDevice for $globalChildPrefix${deviceData.name} as a ${deviceType}"
	try {
		addChildDevice("hubitat",
			"${deviceType}",
			"${globalChildPrefix}${deviceData.id}", null,
			[
				name: "$globalChildPrefix-${deviceData.name}",
				label: "$globalChildPrefix-${deviceData.label ?: deviceData.name}",
				completedSetup: true
			])
	}
	catch (e)
		{
		if ("$e".contains("UnknownDeviceTypeException"))
			log.warn "Device Type Handler Not Installed: ${deviceType}"
		else
			log.error "$e"
		}
	}

def deleteOldChildDevice(deviceData)
	{
	log.debug "deleteChildDevice for $globalChildPrefix ${deviceData.name}"
	if (getChildDevice("$globalChildPrefix${deviceData.id}"))
     	deleteChildDevice("$globalChildPrefix${deviceData.id}")
	}

def DoorHandler(evt)		//Should be real devices with child device
	{
//	if (settings.logDebugs) log.debug  log.debug  "DoorHandler entered ${evt?.displayName} ${evt?.value} ${evt?.deviceId}"  remove to speed up child open
	def resetKeypads=false				//do keypads have to be reset to OFF/Disarmed from running setExitAway
	if (evt.value=='open')
		{
		getChildDevice("$globalChildPrefix${evt.deviceId}").open()		//open the child device
		if (location.hsmStatus=='disarmed' || location.hsmStatus == 'allDisarmed')
			{
			if (globalBeeperDevices)
				{
				if (globalBeeperSeconds)
					{
					globalBeeperDevices.each
						{
						if (globalBeeperExitSound && it.hasCommand("setExitAway"))
							{
							it.setExitAway(globalBeeperSeconds)	//turns on the keypads status light and blinks
							resetKeypads=true
							}
						else
						if (it.typeName=='Centralitex Keypad')
							it.beep(globalBeeperSeconds as Integer)
						else
							it.beep()
						}
					if (resetKeypads)
						runInMillis((globalBeeperSeconds * 1000) + 1500, delaysetDisarmed)	//restore disarmed
					}
				else
					globalBeeperDevices.beep()
				}
//			moved location event here from top of routine or there is a 2 second delay producing the beep/chime
			def locevent = [name:"Nyckelharpatalk", value: "contactOpenMsg", isStateChange: true,
				displayed: true, descriptionText: "${evt.displayName} is open", linkText: "${evt.displayName} is open",
				data: "${evt.displayName}"]
			sendLocationEvent(locevent)
			}
		}
	else
		{
		getChildDevice("$globalChildPrefix${evt.deviceId}").close()
		if (location.hsmStatus=='disarmed' || location.hsmStatus == 'allDisarmed')
			{
			def locevent = [name:"Nyckelharpatalk", value: "contactClosedMsg", isStateChange: true,
				displayed: true, descriptionText: "${evt.displayName} is closed", linkText: "${evt.displayName} is closed",
				data: "${evt.displayName}"]
			sendLocationEvent(locevent)
			}
		}
	}

def MonitorDoorHandler(evt)		//monitored only no child device
	{
	if (settings.logDebugs) log.debug  "MonitorDoorHandler entered ${evt?.displayName} ${evt?.value} ${evt?.deviceId}"
	def resetKeypads=false				//do keypads have to be reset to OFF/Disarmed from running setExitAway
	if (location.hsmStatus=='disarmed' || location.hsmStatus == 'allDisarmed')
		{
		if (evt.value=='open')
			{
			if (globalBeeperDevices)
				{
				if (globalBeeperSeconds)
					{
					globalBeeperDevices.each
						{
						if (globalBeeperExitSound && it.hasCommand("setExitAway"))
							{
							it.setExitAway(globalBeeperSeconds)	//turns on the keypads status light and blinks
							resetKeypads=true
							}
						else
						if (it.typeName=='Centralitex Keypad')
							it.beep(globalBeeperSeconds as Integer)
						else
							it.beep()
						}
					if (resetKeypads)
						runInMillis((globalBeeperSeconds * 1000) + 1500, delaysetDisarmed)	//restore disarmed
					}
				else
					globalBeeperDevices.beep()
				}
//			moved location event here from top of routine or there is a 2 second delay producing the beep/chime
			def locevent = [name:"Nyckelharpatalk", value: "contactOpenMsg", isStateChange: true,
				displayed: true, descriptionText: "${evt.displayName} is open", linkText: "${evt.displayName} is open",
				data: "${evt.displayName}"]
			sendLocationEvent(locevent)
			}
		else
			{
			def locevent = [name:"Nyckelharpatalk", value: "contactClosedMsg", isStateChange: true,
				displayed: true, descriptionText: "${evt.displayName} is closed", linkText: "${evt.displayName} is closed",
				data: "${evt.displayName}"]
			sendLocationEvent(locevent)
			}
		}
	}

def closePanicContact()
	{
	getChildDevice("${globalChildPrefix}Panic Id").close()
	}
def openPanicContact()
	{
	getChildDevice("${globalChildPrefix}Panic Id").open()
	}


def alertHandler(evt)
	{
	if (settings.logDebugs) log.debug ("alertHandler entered, event: ${evt.value} ${evt.jsonData} ${evt.data}")
	def armWindow=15
	if (globalForceSeconds)
		armWindow=globalForceSeconds
	def i = new Integer("0")
	if (['intrusion-delay','intrusion-home-delay','intrusion-night-delay'].contains(evt.value))
		{
		if (globalKeypadDevices)
			globalKeypadDevices.setEntryDelay(evt.jsonData.seconds)
		def locevent = [name:"Nyckelharpatalk", value: "entryDelay", isStateChange: true,
			displayed: true, descriptionText: "Issue entry delay talk event", linkText: "Issue entry delay talk event",
			data: evt.jsonData.seconds]
		sendLocationEvent(locevent)
		if (globalAlarmDevices)
			{
			globalAlarmDevices.each
				{
				if (it.hasCommand("beep"))
					{
	//				it.beep([delay: 2000])			//This was how it was done in SmartThings, fails in Hubitat
					runIn (2,delayCommand,[data: [inputSetting: 'globalAlarmDevices', command: 'beep', i: $i], overwrite: false])
					}
				else
					{
					runInMillis (2000,'delayCommand', [data: [inputSetting: 'globalAlarmDevices', i: i, command: 'on'], overwrite: false])
					runInMillis (3000,'delayCommand', [data: [inputSetting: 'globalAlarmDevices', i: i, command: 'off'], overwrite: false])
//					second off is insurance just incase a hardware timing issue does not silence a siren (Issue with GoControl siren)
					runInMillis (4000,'delayCommand', [data: [inputSetting: 'globalAlarmDevices', i: i, command: 'off'], overwrite: false])
					}
				i++
				}
			}
		}
	else
	if (evt.value == 'arming')								//failed to arm due to open contact(S) hsm only speaks one device
		{														//does not occur using centalitex driver with keypads not coded in HSM
		HeDoorsClose()											//triggers from any HSM arming other than Centralitex that
		runInMillis(500, delaysetDisarmed)						//checks prior to arming
//		runInMillis(1200, delayBeep)
		runIn(armWindow,HeDoorsReset)
		}
/*	if (evt.value != 'rule')									//if rule use rule name minus red alert text
		qssehe_alert(evt.value)									//update remote sse data
	else
		{
		def matcher
		def mask=/([^<]*)</					//format descriptionText=Panic<span style="color:red"> ALERT!</span>
		if ((matcher = evt?.descriptionText =~ mask))
			qssehe_alert(matcher[0][1])
		} */
	}

/*
 *  V1.0.0 Modified version for HE drivers and arming from non Centralitex driver. HSM speaks only first open device on alert
 */
def HeDoorsClose()
	{
	def modeRequest = atomicState?.HeKeypadStatus
	armWindow=15
	if (globalForceSeconds)
		armWindow=globalForceSeconds
	if (settings.logDebugs) log.debug  "HeDoorsClose entered status: ${modeRequest}"
	def contactList
	def notifyOptions
//	When alert and disarmed it usually means request was issued from non-keypad source suchas as a dashboard
//	This is a true leap of faith, but it seems correct
	if (modeRequest == 'disarmed')
		{
		contactList=getChildDevices()			//use all child contacts
		notifyOptions=globalAwayNotify
/*		not needed but tried this. HSM will not change HSMstatus when there is an alert
		but when changing mode it remains in changed mode but status is unchanged
		sendEvent(name: "hsmStatus", value: 'disarmed')
		getChildApps().find
			{
			if (it.getLabel()=='Nyckelharpa ModeFix')
				{
				setLocationMode(it.offDefault)
				return true
				}
			else
				return false
			}
*/		}
	else
	if (modeRequest == 'armed away' && globalAwayContacts)
		{
		contactList=globalAwayContacts
		notifyOptions=globalAwayNotify
		}
	else
	if (modeRequest == 'armed home' && globalHomeContacts)
		{
		contactList=globalHomeContacts
		notifyOptions=globalHomeNotify
		}
	else
	if (modeRequest == 'armed night' && globalNightContacts)
		{
		contactList=globalNightContacts
		notifyOptions=globalNightNotify
		}
	else
		{
		contactList=getChildDevices()			//use all child contacts soemthing is broken
		notifyOptions=globalAwayNotify
		}
	def basemsg='Arming Canceled'
	def contactmsg=basemsg
	if (settings.logDebugs) log.debug  "HeDoorsClose entered $contactList $notifyOptions"
	contactList.each
		{
//		log.debug "${it} ${it.currentContact}"
		if (it.currentContact=="open")
			{
			if (it.displayName.startsWith(globalChildPrefix))
				{
				contactmsg += ', '+it.displayName
				it.close()													//close the child device
				}
			else
				{
				contactmsg += ', '+it.displayName
				getChildDevice("$globalChildPrefix${it.id}").close()		//close the child device
				}
			}
		}
	if (contactmsg != basemsg)
		{
//		atomicState.doorsdtim=0		//1.0.7 When using HE driver or non stops Modefix from executing checkOpenContacts
		contactmsg += " is open. Rearming within ${armWindow} seconds will force arming"
		notifyOptions.each
			{
			if (it=='Notification log')
				{
				sendNotificationEvent(contactmsg)
				}
			else
			if (it=='Push')
				{
				if (sendPushMessage)
					sendPushMessage.deviceNotification(contactmsg)
				}
			else
			if (it=='SMS' && globalPinPhone)
				{
				it.deviceNotification(contactmsg)
				}
			else
			if (it=='Talk')
				{
				def loceventcan = [name:"Nyckelharpatalk", value: "ArmCancel", isStateChange: true,
					displayed: true, descriptionText: "Issue exit delay talk event", linkText: "Issue exit delay talk event",
					data: contactmsg]
				sendLocationEvent(loceventcan)
				}
			}
		}
	}

//	V1.1.1 part of simulation for Smarthings command([delay:millis]) not supported by Hubitat
void delayCommand(data)
	{
	if (settings.logDebugs) log.debug "Nyckelharpa delayCommand entered ${data} ${data.inputSetting} ${data.i} ${data.command}"
	if (data.i)
		settings."${data.inputSetting}"[data.i]."${data.command}"()
	else
		settings."${data.inputSetting}"."${data.command}"()
	}

//V1.0.3
//Using HE drivers need to delay the doors open reset for unknown reasons or alarm triggers. No idea why since system is disarmed
//Not an issue when using Centralitex driver
def HeDoorsResetDelay(evt)
	{
	if (settings.logDebugs) log.debug  "entering HeDoorsResetDelay ${location.hsmStatus}"
	if (location.hsmStatus == 'disarmed')
		runIn(1,HeDoorsReset)
	}

def HeDoorsReset(evt=null)
	{
	if (settings.logDebugs) log.debug  "entering HeDoorsReset ${location.hsmStatus}"
	if (location.hsmStatus == 'disarmed')
		{
		globalAwayContacts.each
			{
//			if (settings.logDebugs) log.debug  "${it.displayName} ${it.currentValue('contact')}"
			if (it.currentValue('contact') == 'closed')
				getChildDevice("$globalChildPrefix${it.deviceId}").close()
			else
				getChildDevice("$globalChildPrefix${it.deviceId}").open()
			}
		}
	}

def solokeypad_delayBeep(Map data)
	{
//	cant pass the keypad object for beep and no device command delay for hubitat
//	so pass keypad id, find it then beep the device. Convoluted but it works
	if (settings.logDebugs) log.debug  "entering solokeypad_delayBeep keypadid: "+ data['keypad'] + " beeps: "+data['beeps']
	globalKeypadDevices.find
		{
		if (it.getId() == data['keypad'])
			{
			it.beep(data['beeps'])
			if (it.data.model.substring(0,3)=='URC')		//UEI keypad never stops beeping
				runIn(2,'solokeypad_delayOff', [data:['keypad':data['keypad']]])
			return true
			}
		else
			return false
		}
	}

def solokeypad_delayOff(Map data)
	{
//	Used with UEI keypad that never stops beeping on any beep command
//	so pass keypad id, find it then beep the device. Convoluted but it works
	if (settings.logDebugs) log.debug  "entering solokeypad_delayOff keypadid: "+ data['keypad']
	globalKeypadDevices.find
		{
		if (it.getId() == data['keypad'])
			{
			it.off()
			return true
			}
		else
			return false
		}
	}

def delayBeep()
	{
	globalKeypadDevices.beep(2)
	}

def delaysetDisarmed()
	{
//	globalKeypadDevices.off()
	globalKeypadDevices.each
		{
		if (it.typeName != 'Centralitex Keypad')
			{
			it.off()
			it.disarm()
			}
		else
			{
			it.off()
			}
		}
	}


/*	Highly modified Version check code from CobraMax
 *	https://github.com/CobraVmax/Hubitat/tree/master/Update%20Code
 *  Jul 16, 2020 change to use Hubitat Package Manager file and a bit of HPM logic
 */
def genVersionMsg(appVersions)
	{
	def jsonData
	def err=false
	def wkMsg=""
	def manifestMap = [:]
//	def paramsUD = [uri: "https://www.arnb.org/shmdelay/versions.json"]	//deprecated Jul 16, 2020
	def params = [
		uri: "https://raw.githubusercontent.com/arnbme/nyckelharpa/master/packageManifest.json",
		requestContentType: "application/json",
		contentType: "application/json",
		textParser: true,
		timeout: 300]
   	try {
        httpGet(params)
        	{ resp ->
        	jsonData = new groovy.json.JsonSlurper().parseText(resp.data.text)
			}
		}
	catch (e)
		{
		log.error "getJsonFile: Contact app author. Something went wrong: -  $e"
		err=e
		}

	if (err)
		wkMsg="\nError getting version file, please contact app author "+err
	else
		{
		for (app in jsonData.apps) {
			manifestMap << ["${app.name}":app.version]
			}
		for (driver in jsonData.drivers) {
			manifestMap << ["${driver.name}":driver.version]
			}
		appVersions.each
			{
			itstr=it as String
			part=itstr.split("[=]")
			wkMsg+=versionCheck(part[0], manifestMap, part[1])
			}
		}
	return wkMsg
	}

/*
 *	get app version based upon child apps and Json app names
 */
def getAppVersions()
	{
	def map = [Nyckelharpa: version()]	//name of this, the parent app
	def appMapName
	getChildApps().each
		{
		appMapName=it.getName()
		if (map."${appMapName}" <= "")
			map << ["$appMapName": it?.version()]
		}
	if (globalKeypadDevices)
		{
		globalKeypadDevices.find
			{
			if (it.typeName=='Centralitex Keypad')
				{
// fails		map << [Centralitex Keypad: it.version()]	//version returning null???
				it.version() 		//force refresh of device.Data.driverVersion
				map << [(it.typeName): it.getDataValue('driverVersion')]	//get stored data version
				return true
				}
			}
		}
	return map
	}

def versionCheck(moduleName, manifestMap, currentVer)
	{
	if (settings.logDebugs) log.debug "version check entered $moduleName $manifestMap $currentVer"
	def appText='Module'
	def newVer

//	fugly but I dont have the groovy chops to make this pretty, an it's not used in a mission critical area
//	and def newVer= manifestMap[moduleName] does not work, likely due to space in the module name

	manifestMap.find
		{
		itstr=it as String
		part=itstr.split("[=]")
		if (part[0]==moduleName)
			{
			newVer=part[1]
			return true
			}
		else
			return false
		}
	if (newVer==null)
		return "\n<b>$moduleName missing from HPM manifest, contact this app's author</b>"

	if (settings.logDebugs) log.debug  "versionsCheck processing module: $moduleName targetVer: $newVer actualVer: $currentVer"

	if(currentVer == null)
		return "\n<b>$moduleName null illogical condition</b>"

	if(newVer == "NLS")
		return "\n<b>$moduleName no longer supported</b>"

	if(currentVer == newVer)
		return ""					//module is current

	if(currentVer < newVer)
		return ("\n<b>${appText}: $moduleName, Version: $newVer available</b>")

	return ("\n<b>${appText}: $moduleName, beta version: $currentVer in use</b>")
	}
