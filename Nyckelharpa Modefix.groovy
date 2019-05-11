/**
 *  Update Url:   https://raw.githubusercontent.com/arnbme/nyckelharpa/master/Nyckelharpa%20Modefix.groovy
 * 
 *  Nyckelharpa ModeFix 
 *  Functions: Fix the mode when it is invalid, generally caused when using Dashboard to switch modes
 * 
 *  Copyright 2017 Arn Burkhoff
 * 
 *  Changes to Apache License
 *	4. Redistribution. Add paragraph 4e.
 *	4e. This software is free for Private Use. All derivatives and copies of this software must be free of any charges,
 *	 	and cannot be used for commercial purposes.
 *
 *  Licensed under the Apache License with changes noted above, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *	May 10, 2019 	v0.0.6	Do what 0.0.5 said it would do but did not
 *	May 02, 2019 	v0.0.5	Make all mode settings optional
 *	Apr 30, 2019 	v0.0.4	HSM hijacked command setExitDelay to send all HSM delay to keypad
 *								avoid confusion by changing ours to setExitAway
 *	Apr 29, 2019 	v0.0.3	adjust for newly added setExitNight and setExitStay keypad DTH commands
 *	Apr 28, 2019 	v0.0.2	fix forced arming for Home and Night modes
 *	Apr 25, 2019 	v0.0.1	Handle force arming from dashboard setting vs keypad, requires delay time be < 0 using 1
 *	Apr 22, 2019 	v0.0.0	Rename SHM Delay Modfix to Nyckelharpa Modefix change version to 0.0.0 prerelease
 *	renamed to Nyckelharpa from SHM Delay Modefix, version reset to 0.0.0
 *	Apr 20, 2019 	V0.1.8AH Move sendevent for arm and disarm message here, keypad control in one place
 *	Apr 20, 2019 	V0.1.8AH Move sendevent for exit message here, handle double arming issue with AtomicState
 *	Apr 17, 2019 	V0.1.8H Add code to run system off HSM armStates, caused double armingAway state, double TTS
 *								fixed using atomicState to track hsmState
 *								This module does armstate to mode change.
 *								Note issuing mode change causes a second armingMode, this is a HE BUG
 *	Apr 16, 2019 	V0.1.8H Modified: Add third HSM armed state
 *	Apr 12, 2019 	V0.1.7H Modified: To work in Hubitat
 *				 			need to add mode armedStay he has 4 arm modes
 *
 *	Mar 05, 2019 	V0.1.7  Added: Boolean flag for debug message logging, default false
 *
 *	Jan 06, 2019 	V0.1.6  Added: Support for 3400_G Centralite V3
 *
 * 	Oct 17, 2018	v0.1.5	Allow user to set if entry and exit delays occur for a state/mode combination
 *								
 * 	Apr 24, 2018	v0.1.4	For Xfinity and Centralite model 3400 keypad on armed (Home) modes 
 *								add device icon button to light Stay (Entry Delay) or Night (Instant Intrusion)
 *								
 * 	Mar 11, 2018    v0.1.3  add logging to notifications when mode is changed. 
 *								App issued changes are not showing in PhoneApp notifications
 *								Assumed system would log this but it does not
 * 	Sep 23, 2017    v0.1.2  Ignore alarm changes caused by True Entry Delay in SHM Delay Child
 * 	Sep 05, 2017    v0.1.1  minor code change to allow this module to run stand alone
 * 	Sep 02, 2017    v0.1.0  add code to fix bad alarmstate set by unmodified Keypad module
 * 	Sep 02, 2017    v0.1.0  Repackage logic that was in parent into this module for better reliability
 *					and control
 * 	Aug 26/27, 2017 v0.0.0  Create 
 *
 */

definition(
    name: "Nyckelharpa ModeFix",
    namespace: "arnbme",
    author: "Arn Burkhoff",
    description: "(${version()}) Change HE Mode when HE arm state changes",
    category: "My Apps",
	parent: "arnbme:Nyckelharpa",
    iconUrl: "https://www.arnb.org/IMAGES/hourglass.png",
    iconX2Url: "https://www.arnb.org/IMAGES/hourglass@2x.png",
    iconX3Url: "https://www.arnb.org/IMAGES/hourglass@2x.png",
    singleInstance: true)

preferences {
	page(name: "pageOne", nextPage: "pageOneVerify")
	page(name: "pageOneVerify", nextPage: "pageOne")
	page(name: "pageTwo")
	page(name: "aboutPage", nextPage: "pageOne")
}

def version()
	{
	return "0.0.6";
	}

def pageOne(error_msg)
	{
	dynamicPage(name: "pageOne", title: "For each alarm state, set valid modes and default modes.", install: false, uninstall: true)
		{
		section
			{
			if (error_msg instanceof String )
				{
				paragraph "<b>"+error_msg+"</b>"
				}
			else
				paragraph "Caution! Wrong settings may create havoc. If you don't fully understand Alarm States and Modes, leave the settings empty, but it must be saved"
			href(name: "href",
			title: "Introduction",
			required: false,
			page: "aboutPage")
			}
		section ("Debugging messages")
			{
			input "logDebugs", "bool", required: false, defaultValue:false,
				title: "Log debugging messages? Normally off/false"
			}
		section ("Alarm State: Disarmed / Off")
			{
			input "offModes", "mode", required: false, multiple: true, 
				title: "Valid Modes for: Disarmed"
			input "offDefault", "mode", required: false, 
				title: "Default Mode for: Disarmed"
			}	
		section ("Alarm State: Armed (Away)")
			{
			input "awayModes", "mode", required: false, multiple: true,
				title: "Valid modes for: Armed Away"
			input "awayDefault", "mode", required: false,
				title: "Default Mode for Armed Away"
			}	
		section ("Alarm State: Armed (Night)")
			{
			input "nightModes", "mode", required: false, multiple: true, 
				title: "Valid Modes for Armed Night"
			input "nightDefault", "mode", required: false, 
				title: "Default Mode for Armed Night"
			}	
		section ("Alarm State: Armed (Home) aka Stay")
			{
			input "homeModes", "mode", required: false, multiple: true, 
				title: "Valid Modes for Armed Home"
			input "homeDefault", "mode", required: false,
				title: "Default Mode for Armed Home"
			}	
		section
			{
			paragraph "Nyckelharpa Modefix ${version()}"
			}

		}	
	}	

def pageOneVerify() 				//edit page One
	{

	def off_error=null
	def home_error=null
	def night_error=null
	def away_error=null
	
//	Verify disarm/off data
	if (offModes || offDefault)
		{
		off_error="Disarmed / Off Default Mode not defined in Valid Modes"
		def children = offModes
		children.each
			{ child ->
			log.debug "$offDefault $child" 
			if (offDefault == child)
				{
				off_error=null
				}
			}
		}
	
//	Verify Away data
	if (awayModes || awayDefault)
		{
		away_error="Armed (Away) Default Mode not defined in Valid Modes"
		children = awayModes
		children.each
			{ child ->
			if (awayDefault == child)
				{
				away_error=null
				}
			}
		}

//	Verify Home (Stay) data
	if (homeModes || homeDefault)
		{
		home_error="Armed (Home) Default Mode not defined in Valid Modes"
		children = homeModes
		children.each
			{ child ->
			if (homeDefault == child)
				{
				home_error=null
				}
			}
		}
		
//	Verify Night data
	if (nightModes || nightDefault)
		{
		night_error="Armed (Night) Default Mode not defined in Valid Modes"
		children = nightModes
		children.each
			{ child ->
			if (nightDefault == child)
				{
				night_error=null
				}
			}
		}

	if (off_error == null && away_error == null && home_error == null && night_error == null)
		{
		pageTwo()
		}
	else	
		{
		log.debug "in error logic"
		def error_msg=""
		def newline=""
		if (off_error>"")
			{
			error_msg=off_error
			newline="\n"
			}
		if (away_error >"")
			{
			error_msg+=newline + away_error
			newline="\n"
			}	
		if (home_error >"")
			{
			error_msg+=newline + home_error
			newline="\n"
			}
		if (night_error >"")
			{
			error_msg+=newline + night_error
			newline="\n"
			}
		pageOne(error_msg)
		}
	}

def pageTwo()
	{
	dynamicPage(name: "pageTwo", title: "<b>Mode settings verified, press 'Done' to install, press '<' or Back to change</b>", install: true, uninstall: true)
		{
		section ("<b>Debugging messages</b>")
			{
			if (logDebugs)
				paragraph "Log Debug messages are generated"
			else
				paragraph "No Debugging messages" 
			}
		section ("<b>Alarm State: Disarmed / Off</b>")
			{
			if (offModes)
				paragraph "Valid Modes for Disarmed: $offModes"
			else	
				paragraph "Valid Modes for Disarmed: Not Set"
			if (offDefault)
				paragraph "Default Mode for Disarmed: $offDefault"
			else	
				paragraph "Default Mode for Disarmed: Not Set"
			}	
		section ("<b>Alarm State: Armed (Away)</b>")
			{
			if (awayModes)
				paragraph "Valid Modes for Away: $awayModes"
			else	
				paragraph "Valid Modes for Away: Not Set"
			if (awayDefault)
				paragraph "Default Mode for Away: $awayDefault"
			else	
				paragraph "Default Mode for Away: Not Set"
			}	
		section ("<b>Alarm State: Armed (Night)</b>")
			{
			if (nightModes)
				paragraph "Valid Modes for Night: $nightModes"
			else	
				paragraph "Valid Modes for Night: Not Set"
			if (awayDefault)
				paragraph "Default Mode for Night: $nightDefault"
			else	
				paragraph "Default Mode for Night: Not Set"
			}
		section ("<b>Alarm State: Armed (Home) aka Stay</b>")
			{
			if (homeModes)
				paragraph "Valid Modes for Home: $homeModes"
			else	
				paragraph "Valid Modes for Home: Not Set"
			if (homeDefault)
				paragraph "Default Mode for Home: $homeDefault"
			else	
				paragraph "Default Mode for Home: Not Set"
			}	

		section
			{
			paragraph "Nyckelharpa Modefix ${version()}"
			}
		}
	}	

	
def aboutPage()
	{
	dynamicPage(name: "aboutPage", title: "Introduction")
		{
		section 
			{
			paragraph "Have you ever wondered why Mode restriced Rule Machine rules sometimes fail to execute, or execute when they should not?\n\n"+
			"Perhaps you conflated HSM AlarmState and Mode, however they are separate and independent settings, "+
			"and when Alarm State is changed---surprise, Mode does not change!\n\n" +
			"This app changes the Mode when the HSM Alarm State changes. It also triggers most of the app's TTS messaging.\n\n"+
			"HSM changes the Alarm State when the Mode changes"
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
	subscribe(location, 'hsmStatus', alarmStatusHandler)
	}


def alarmStatusHandler(evt)
	{
	logdebug "alarmStatusHandler entered"
	def theAlarm = evt.value as String				//curent alarm state
	def lastAlarm = atomicState?.hsmstate
	atomicState.hsmstate=theAlarm
	def theMode = location.currentMode as String	//warning without string parameter it wont match
	lastDoorsDtim=parent.getAtomicdoorsdtim()			//set in Nyckelharpa checkOpenContacts
	logdebug "ModeFix alarmStatusHandler entered, HSM state: ${theAlarm}, lastAlarm: ${lastAlarm} Mode: ${theMode} lastDoorsDtim $lastDoorsDtim"
//	Fix the mode to match the Alarm State. 
	if (theAlarm=="disarmed" || theAlarm=="allDisarmed")
		{
		if (parent.globalKeypadDevices)
			parent.globalKeypadDevices.setDisarmed()
		ttsDisarmed()
		if (offModes && !offModes.contains(theMode))
			{
			setLocationMode(offDefault)
			}
		}
	else
	if (theAlarm=="armedAway" || theAlarm=="armingAway")
		{
		if (theAlarm=="armedAway")
			{
			if (parent.globalKeypadDevices)
				parent.globalKeypadDevices.setArmedAway()
			ttsArmed(theAlarm)
			}
		else 
		if (theAlarm != lastAlarm)
			{
			if (parent.globalAwayContacts)
				{
				if (lastDoorsDtim > 0 && !parent.checkOpenContacts(parent.globalAwayContacts, parent.globalAwayNotify, false))
					{
					sendLocationEvent(name: 'hsmSetArm', value: 'disarm')
					return
					}
				parent.killAtomicdoorsdtim()		
				}
			if (evt.jsonData.seconds)
				{
				if (parent.globalKeypadDevices)
					parent.globalKeypadDevices.setExitAway(evt.jsonData.seconds)
				ttsExit(evt.jsonData.seconds)
				}
			}	
		if (awayModes && awayDefault)
			{
			if (!awayModes.contains(theMode))
				{
				setLocationMode(awayDefault)
				}
			}
		}
	else
	if (theAlarm=="armedNight" || theAlarm=="armingNight")
		{
		if (theAlarm=="armedNight")
			{
			if (parent.globalKeypadDevices)
				{
				parent.globalKeypadDevices.each
					{
					if (['3400','3400-G'].contains(it.data.model))
						it.setArmedNight()
					else	
						it.setArmedStay()			//non Centralite keypads have 3 mode lights, light partial
					}
				}
			ttsArmed(theAlarm)
			}
		else
		if (theAlarm != lastAlarm)
			{
			if (parent.globalHomeContacts)
				{
				if (lastDoorsDtim > 0 && !parent.checkOpenContacts(parent.globalNightContacts, parent.globalNightNotify, false))
					{
					sendLocationEvent(name: 'hsmSetArm', value: 'disarm')
					return
					}
				parent.killAtomicdoorsdtim()		
				}
			if (evt.jsonData.seconds)
				{
				if (parent.globalKeypadDevices)
					{
					parent.globalKeypadDevices.each
						{
						if (['3400','3400-G'].contains(it.data.model))
							it.setExitNight(evt.jsonData.seconds)
						else	
							it.setExitStay(evt.jsonData.seconds) //non Centralite keypads have 3 mode lights, light partial
						}
					}
				ttsExit(evt.jsonData.seconds)
				}
			}	
		if (nightModes && nightDefault)
			{
			if (!nightModes.contains(theMode))
				{
				setLocationMode(nightDefault)
				}
			}
		}	
	else
//	This is equivalent to ST Stay mode		
	if (theAlarm=="armedHome" || theAlarm=="armingHome")
		{
		if (theAlarm=="armedHome")
			{
			if (parent.globalKeypadDevices)
				parent.globalKeypadDevices.setArmedStay()
			ttsArmed(theAlarm)
			}
		else 
		if (theAlarm != lastAlarm)
			{
			if (parent.globalHomeContacts)
				{
				if (lastDoorsDtim > 0 && !parent.checkOpenContacts(parent.globalHomeContacts, parent.globalHomeNotify, false))
					{
					sendLocationEvent(name: 'hsmSetArm', value: 'disarm')
					return
					}
				parent.killAtomicdoorsdtim()		
				}
			if (evt.jsonData.seconds)
				{
				if (parent.globalKeypadDevices)
					parent.globalKeypadDevices.setExitStay(evt.jsonData.seconds)
				ttsExit(evt.jsonData.seconds)
				}
			}
		if (homeModes && homeDefault)
			{
			if (!homeModes.contains(theMode))
				{
				setLocationMode(homeDefault)
				}
			}
		}	
	else
		{
		log.error "ModeFix alarmStatusHandler Unknown alarm mode: ${theAlarm}"
		return false
		}
	}
	
def ttsExit(delay)
	{
	logdebug "ttsExit delay: $delay"
	if (delay > 1)		//in order to get arming requests vs armed requests set mode time to 1 vs 0
		{
		def locevent = [name:"Nyckelharpatalk", value: "exitDelay", isStateChange: true,
			displayed: true, descriptionText: "Issue exit delay talk event", linkText: "Issue exit delay talk event",
			data: delay]	
		sendLocationEvent(locevent)
		}
	}	

def ttsDisarmed()
	{
	logdebug "ttsDisarmed"
	def locevent = [name:"Nyckelharpatalk", value: "disarm", isStateChange: true,
		displayed: true, descriptionText: "Issue disarm talk event", linkText: "Issue disarm delay talk event",
		data: 'none']	
	sendLocationEvent(locevent)
	}

def ttsArmed(theAlarm)
	{
	logdebug "ttsArmed"
	def locevent = [name:"Nyckelharpatalk", value: "armed", isStateChange: true,
		displayed: true, descriptionText: "Issue armed talk event", linkText: "Issue armed delay talk event",
		data: theAlarm]	
	sendLocationEvent(locevent)
	}
	
def logdebug(txt)
	{
   	if (logDebugs)
   		log.debug ("${txt}")
    }	