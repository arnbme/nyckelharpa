/**
 *  Update Url:   https://raw.githubusercontent.com/arnbme/nyckelharpa/master/Nyckelharpa%20Talker.groovy
 *
 *  Nyckelharpa Talker
 *  Supplements Big Talker adding speech when Nyckelharpa enters the Exit or Entry delay time period
 *		For LanNouncer Device: Chime, TTS text, Chime
 *		For speakers (such as Sonos)  TTS text
 *	Supports TTS devices and speakers
 *	When devices use differant messages, install multiple copies of this code
 *	When speakers need different volumes, install multiple copies of this code
 *
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
 *	Dec 03, 2019 v1.0.0 Change default entry message to Disarm system or Police will arrive shortly
 *	Dec 03, 2019 v0.1.0 Compensate for Hubitat Sonos driver missing command sendTextAndResume
 *	Dec 02, 2019 v0.0.9 Correct logic and coding error with Do not talk times.
 *                      Allow it to be totally ignored by new setting theQuiet so fields do not have to be erased/empty
 *	May 23, 2019 v0.0.8 Allow each message to have it's own optional set of target devices
 *	May 18, 2019 v0.0.7 Minor text change on page two heading
 *	May 03, 2019 v0.0.6 Move hsmAlert subscribe to Nyckelharpa to handle siren chirps and make this module truly optional
 *	May 02, 2019 v0.0.5 Fix error displaying Open and Close messages as Null
 *	Apr 28, 2019 v0.0.4 Add open contacts messages
 *	Apr 25, 2019 v0.0.3 When HSM cancels arming due to open doors set keypad to off (for 100% accuracy should be last state).
 *	Apr 24, 2019 v0.0.2 Restore ArmCancel message code.
 *	Apr 23, 2019 v0.0.1 Fix user device did not have chime caused error.
 *						verify all devices have chime during input editing
 *	Apr 21, 2019 v0.0.0 Fix missing NEXT Button on pageone, add nextPage: to dynamic page 1
 * 	Apr 21, 2019 v1.0.4AH set as single instance remove label
 * 	Apr 21, 2019 v1.0.4AH All keypad messages in one place for easy setup. Produce arm and disarm messages
 * 	Apr 20, 2019 v1.0.4AH subscribe to hsmAlert events for pin entry messages
 * 	Apr 20, 2019 v1.0.4H Modify for hubitat and fully tts
 * 	Dec 17, 2018 v1.0.4 Change speaker capability audioNotification to musicPlayer. Did not select Sonos speakers
 * 	Nov 04, 2018 v1.0.3 Add support for generic quiet time per user request on messages
 *						Delayed messages are super delayed by unknown cloud processing error, allow for no chime and instant speak
 * 	Oct 21, 2018 v1.0.2	Support Arming Canceled messages from SHM Delay
 * 	Jul 05, 2018 v1.0.1	correct non standard icon
 * 	Jul 04, 2018 v1.0.1	Check for non Lannouner TTS devices and when true eliminate chime command
 *	Jun 26, 2018 V1.0.0 Create from standalone module Keypad ExitDelay Talker
 */
definition(
    name: "Nyckelharpa Talker",
    namespace: "arnbme",
    author: "Arn Burkhoff",
    description: "(${version()}) Nyckelharpa Talker",
    category: "My Apps",
    parent: "arnbme:Nyckelharpa",
    iconUrl: "https://www.arnb.org/IMAGES/hourglass.png",
    iconX2Url: "https://www.arnb.org/IMAGES/hourglass@2x.png",
    iconX3Url: "https://www.arnb.org/IMAGES/hourglass@2x.png",
    singleInstance: true)

def version()
	{
	return "0.1.0";
	}

preferences {
	page(name: "pageZeroVerify")
	page(name: "pageZero", nextPage: "pageZeroVerify")
	page(name: "pageOne", nextPage: "pageOneVerify")
	page(name: "pageOneVerify")
	page(name: "pageTwo")		//recap page when everything is valid. No changes allowed.
	}

def pageZeroVerify()
//	Verify this is installed as a child
	{
	if (parent && parent.getInstallationState()=='COMPLETE')
		{
		pageOne()
		}
	else
		{
		pageZero()
		}
	}

def pageZero()
	{
	dynamicPage(name: "pageZero", title: "This App cannot be installed", uninstall: true, install:false)
		{
		section
			{
			paragraph "This SmartApp, Nyckelharpa Talker, must be used as a child app of Nyckelharpa."
			}
		}
	}

def pageOne()
	{
	dynamicPage(name: "pageOne", title: "Talker Messages and Devices", install: false, uninstall: true, nextPage: "pageOneVerify")
		{
		section("The Nyckelharpa Message Settings")
			{
			if (state.error_data)
				{
				paragraph "${state.error_data}"
				state.remove("error_data")
				}
			input "logDebugs", "bool", required:true, defaultValue:false,
				title: "Log debugging messages? Normally off/false"
			input "theContactOpenMsg", "string", required: false, title: "Contact Open message, issued when system is disarmed: %device replaced with device name",
				defaultValue: "%device is now open"
			input "theOpenMsgChimes", "bool", defaultValue: true, required: false,
				title: "Sound TTS Chimes with Open message Default: On/True"
			input "theContactClosedMsg", "string", required: false, title: "Contact Closed message, issued when system is disarmed: %device replaced with device name",
				defaultValue: "%device is now closed"
			input "theClosedMsgChimes", "bool", defaultValue: false, required: false,
				title: "Sound TTS Chimes with Close message Default: Off/False"
			input "theExitMsgKypd", "string", required: false, title: "Exit message: %nn replaced with delay seconds",
				defaultValue: "Alarm system is arming in %nn seconds. Please exit the facility"
			input "theEntryMsg", "string", required: false, title: "Entry message: %nn replaced with delay seconds",
				defaultValue: "Disarm system, or police will arrive shortly"
//Apr 02, 2020	defaultValue: "Please enter your pin on the keypad"
			input "theArmMsg", "string", required: false, title: "Armed message: %hsmStatus replaced with HSM Arm State",
					defaultValue: "Alarm System is now armed in %hsmStatus Mode"
			input "theDisarmMsg", "string", required: false, title: "Disarm message",
					defaultValue: "System Disarmed"
			input "theQuiet", defaultValue: false, "bool", required: false,  submitOnChange: true,
				title: "Do not talk during quiet time?  Default: Off - Talks all the time"
			if (theQuiet)
				{
				input(name: 'theStartTime', type: 'time', title: 'Do not talk: Start Time', required: false)
				input(name: 'theEndTime', type: 'time', title: 'Do not talk: End Time', required: false)
				}
			input "theSoundChimes", "bool", defaultValue: true, required: false,
				title: "Sound TTS Chimes with messages Default: On/True"
			input "theTTS", "capability.speechSynthesis", required: false, multiple: true, submitOnChange: true,
				title: "LanNouncer/DLNA TTS Devices"
			input "theSpeakers", "capability.musicPlayer", required: false, multiple: true, submitOnChange: true,
				title: "Speaker Devices?"
			input "theVolume", "number", required: true, range: "1..100", defaultValue: 40,
				title: "Speaker Volume Level from 1 to 100"
			if (parent.sendPushMessage)
				paragraph "Global Default Notification Devices including Hubitat Phone App and Pushover: ${parent.sendPushMessage}"
			else
				paragraph "No Default Notification Devices"
			}
		}
	}

def pageOneVerify() 				//edit page one info, go to pageTwo when valid
	{
	def error_data = ""
	if (theSoundChimes && theTTS)
		{
		theTTS.each
			{
			if (!it.hasCommand("chime"))
				error_data="All TTS devices do not have Chime command, set 'Sound TTS Chimes' Off"
			}
		}
	if (error_data != "")
		{}
	else
	if (!theQuiet)
		{}
	else
	if (theStartTime>"" && theEndTime>"")
		{}
	else
	if (theStartTime>"")
		error_data="Please set do not talk end time or clear do not talk start time"
	else
	if (theEndTime>"")
		error_data="Please set do not talk start time or clear do not talk end time"

	if (error_data!="")
		{
		state.error_data='<b>'+error_data.trim()+'</b>'
		pageOne()
		}
	else
		{
		pageTwo()
		}
	}

//	This page summarizes the data prior to save
def pageTwo(error_data)
	{
	dynamicPage(name: "pageTwo", title: "Verify settings then tap Done, or tap this device's &lt;, &#9665;, or 'back' icon to change settings", install: true, uninstall: true)
		{
		def chimes=false
		def chimetxt='(Chime) '
		try
			{chimes=theSoundChimes}
		catch(Exception e)
			{}
		if (!chimes)
			chimetxt=''
		section
			{
			if (theContactOpenMsg)
				{
				if (theOpenMsgChimes)
					paragraph "The Open Contact Message:\n(Chime) ${theContactOpenMsg}"
				else
					paragraph "The Open Contact Message:\n${theContactOpenMsg}"
				}
			else
				paragraph "The Open Contact Message is not defined"
			if (theContactClosedMsg)
				{
				if (theClosedMsgChimes)
					paragraph "The Closed Contact Message:\n(Chime) ${theContactClosedMsg}"
				else
					paragraph "The Closed Contact Message:\n${theContactClosedMsg}"
				}
			else
				paragraph "The Closed Contact Message is not defined"
			if (theExitMsgKypd)
				paragraph "The Exit Delay Message:\n${chimetxt}${theExitMsgKypd}"
			else
				paragraph "The Exit Delay Message is not defined"
			if (theEntryMsg)
				paragraph "The Entry Delay Message:\n${chimetxt}${theEntryMsg}"
			else
				paragraph "The Entry Delay Message is not defined"
			if (theArmMsg)
				paragraph "The Armed Message:\n${theArmMsg}"
			else
				paragraph "The Armed Message is not defined"
			if (theDisarmMsg)
				paragraph "The Disarm Message:\n${theDisarmMsg}"
			else
				paragraph "The Disarm Message is not defined"
			if (theQuiet && theStartTime>"" && theEndTime>"")
				paragraph "Quiet time active from ${theStartTime.substring(11,16)} to ${theEndTime.substring(11,16)}"
			else
				paragraph "Quiet time is inactive"

			if (!chimes)
				paragraph "Chimes do not sound with messages"
			if (theTTS)
				paragraph "The Text To Speech Devices are ${theTTS}"
			else
				paragraph "No Text To Speech Devices are defined"
			if (theSpeakers)
				{
				paragraph "The Text To Speech Devices are ${theSpeakers}"
				paragraph "The Speaker Volume Level is ${theVolume}"
				}
			else
				paragraph "No Speaker Devices are defined"
			if (theVolume)
				paragraph "Default Speaker Volume Level is $theVolume"
			if (parent.sendPushMessage)
				paragraph "Global Default Notification Devices including Hubitat Phone App and Pushover: ${parent.sendPushMessage}"
			else
				paragraph "No default Notification Devices"
			paragraph "Module Nyckelharpa Talker ${version()}"
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

def initialize() {
	subscribe(location, "Nyckelharpatalk", TalkerHandler)
	}

def TalkerHandler(evt)
	{
	logdebug("TalkerHandler entered, event: ${evt.value} ${evt?.data}")
	def delaydata=evt?.data			//get the delay time or whatever was passed for message
	def msgout

//	0.0.9 Dec 2, 2019 failing in Hubitat, totally revise code
//	1.0.3 Nov 4, 2018 check time values for quiet
/*	date formats
Date: Mon Dec 02 14:02:09 EST 2019
Time: 2019-12-02T00:00:00.000-0500
*/
	if (theQuiet && theStartTime>"" && theEndTime>"")
		{
		def String nowis = new Date()
		def timenow = nowis.substring(11,16)
		logdebug ("${nowis} ${timenow} ${theStartTime.substring(11,16)} ${theEndTime.substring(11,16)}")
		if (theEndTime.substring(11,16)>theStartTime.substring(11,16))
			{
//			end > start
			if (timenow>=theStartTime.substring(11,16) && timenow<theEndTime.substring(11,16))
				{
//				log.debug ("it is quiet time with end > start")
				return false
				}
			}
		else
//		end<start meaning: talk time is endtime to starttime
		if (timenow>=theEndTime.substring(11,16) && timenow<=theStartTime.substring(11,16))
			{
//			log.debug ("it is time to talk with end < start")
			}
		else
			{
//			log.debug ("it is quiet time with end < start")
			return false
			}
		}
//	else
//		logdebug ('quiet time not active')

	if (evt.value=="entryDelay" && theEntryMsg>"")
		{
		def delaydatax=delaydata as String		//throws casting error if not done
		if (delaydatax>"")
			msgout=theEntryMsg.replaceAll("%nn",delaydatax)
		else
			msgout=theEntryMsg
		if (theTTS)
			{
			if (theSoundChimes)
				theTTS.chime()
			runInMillis(1800, ttsDelay, [data: [tts: msgout]])
			}
		if (theSpeakers)
			{
//			theSpeakers.playTextAndResume(msgout,theVolume)
			theSpeakers.each
				{
				playTextAnd(msgout,theVolume,it)
				}
			}
		}
	else
	if (evt.value=="exitDelay" && theExitMsgKypd>"")
		{
		if (delaydata>"")
			msgout=theExitMsgKypd.replaceAll("%nn",delaydata)
		else
			msgout=theExitMsgKypd
		if (theTTS)
			{
			if (theSoundChimes)
				{
				theTTS.chime()
				runInMillis(1800, ttsDelay, [data: [tts: msgout]])
				}
			else
				{theTTS.speak(msgout)}
			}
		if (theSpeakers)
			{
//			theSpeakers.playTextAndResume(msgout,theVolume)
			theSpeakers.each
				{
				playTextAnd(msgout,theVolume,it)
				}
			}
		}
	else
	if (evt.value=="contactOpenMsg" && theContactOpenMsg>"")
		{
		msgout=theContactOpenMsg
		def delaydatax=delaydata as String		//throws casting error if not done
		msgout=msgout.replaceAll("%device",delaydatax)
		if (theTTS)
			{
			if (theOpenMsgChimes)
				{
				theTTS.chime()
				runInMillis(1800, ttsDelay, [data: [tts: msgout]])
				}
			else
				{theTTS.speak(msgout)}
			}
		if (theSpeakers)
			{
//			theSpeakers.playTextAndResume(msgout,theVolume)
			theSpeakers.each
				{
				playTextAnd(msgout,theVolume,it)
				}
			}
		}
	else
	if (evt.value=="contactClosedMsg" && theContactClosedMsg>"")
		{
		msgout=theContactClosedMsg
		def delaydatax=delaydata as String		//throws casting error if not done
		msgout=msgout.replaceAll("%device",delaydatax)
		if (theTTS)
			{
			if (theClosedMsgChimes)
				{
				theTTS.chime()
				runInMillis(1800, ttsDelay, [data: [tts: msgout]])
				}
			else
				{theTTS.speak(msgout)}
			}
		if (theSpeakers)
			{
//			theSpeakers.playTextAndResume(msgout,theVolume)
			theSpeakers.each
				{
				playTextAnd(msgout,theVolume,it)
				}
			}
		}
	else
	if (evt.value=="disarm" && theDisarmMsg>"")
		{
		if (theTTS)
			{theTTS.speak(theDisarmMsg)}
		if (theSpeakers)
			{
//			{theSpeakers.playTextAndResume(theDisarmMsg,theVolume)}
			theSpeakers.each
				{
				playTextAnd(theDisarmMsg,theVolume,it)
				}
			}
		}
	else
	if (evt.value=="armed" && theArmMsg>"")
		{
		def hsmState = [armedAway: "Away", armedHome: "Home", armedNight: "Night"][delaydata] ?: delaydata
		msgout=theArmMsg.replaceAll("%hsmStatus",hsmState)
		if (theTTS)
			{theTTS.speak(msgout)}
		if (theSpeakers)
			{
//			{theSpeakers.playTextAndResume(msgout,theVolume)}
			theSpeakers.each
				{
				playTextAnd(msgout,theVolume,it)
				}
			}
		}
	else
	if (evt.value=="ArmCancel" && delaydata>"")
		{
		if (theTTS)
			{theTTS.speak(delaydata)}
		if (theSpeakers)
			{
//			{theSpeakers.playTextAndResume(delaydata,theVolume)}
			theSpeakers.each
				{
				playTextAnd(delaydata,theVolume,it)
				}
			}
		}
	else
		{
		if (!['ArmCancel','armed','disarm','contactClosedMsg', 'contactOpenMsg', 'exitDelay', 'entryDelay'].contains(evt.value))
			log.warn "Nyckelharpa Talker, Unknown request: ${evt.value}"
		}
	}

//	added V0.1.0 Dec 03, 2019
//	Sonos driver from Hubitat does not have playTextAndResume, this is a workaround
def playTextAnd(msg,vlm,speaker)
	{
	if (speaker.hasCommand("playTextAndResume"))
		speaker.playTextAndResume(msg,vlm)
	else
		speaker.playTextAndRestore(msg)
	}

def ttsDelay(map)
	{
	theTTS.speak(map.tts)
	}

def logdebug(txt)
	{
   	if (logDebugs)
   		log.debug ("${txt}")
    }
