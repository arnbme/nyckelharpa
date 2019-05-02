<a name="top"></a>
# Nyckelharpa
![image nyckelharpa](images/nyckelharpa.jpg)<br /> 
**The buttons and levers controlling Hubitat Elevation's, Home Security Monitor's strings.** 
(This document is a work in progress, nothing beyond the beginning of section 4 is meaningful)
## Table of Contents 
[&ensp;1. Purpose](#purpose)<br />
[&ensp;2. Features](#features)<br />
[&ensp;3. Donate](#support)<br />
[&ensp;4. Installation](#install)<br />
[&ensp;5. Quick Setup Guide](#setup)<br />
[&ensp;6. Global Definitions](#globals)<br />
[&ensp;7. Preparing HSM Forced Arming](/t/release-shm-delay-version-2-0/121800/74#soundtts)<br />
[&ensp;8. Modefix Setup and Usage, Fix keypad out of sync with SHM Arming State](#modefix)<br />
[&ensp;9. Using Sounds and Text to Speach](/t/release-shm-delay-version-2-0/121800/74#soundtts)<br />
[10. Contact Profile Setup](#delayprofile)<br />
[11. User/Pin Profile Setup](#userprofile)<br />
[12. Using The User Keypad Device Handler](#howtousekeypads)<br />
[13. Adjusting the Home System Monitor (HSM)](#HSMsecurity)<br />
[14. Initial Testing](/t/release-shm-delay-version-2-0/121800/74#initialtesting)<br />
[15. Report An Issue or Contact](/t/release-shm-delay-version-2-0/121800/74#fixmotionentry)
<a name="purpose"></a>
## 1. Purpose
Nyckepharpa is a user created Hubitat Home Security Monitor (HSM) extension, providing features not available in HSM. Additionally, it simplifies setting up security related messaging. 

[:arrow_up_small: Back to top](#top)

<a name="features"></a>
## 2. Features

* Under user control, forces HSM arming when a contact is open.<br /> 
Why is this needed? HSM does not arm the system when a contact is open. Examples:<br />It's 1AM, you want to arm the system for night, but a contact is broken.<br /> 
You are away from home, forgot to arm the system, and when you try, oops the back door is open. 
* Adjusts HSM's mode when HSM's arm state changes. (HSM adjusts HSM's arm state when the mode changes)
* Provides an easy to use security related message control center with output to TTS, Speakers, Pushover, and SMS

* Keypads: Centralite V2 and V3, and Iris V2 devices may use a ported version of Mitch Pond's Keypad DH making he keypad function as it did in SmartThings with the SHM Delay App, and it uses an easy to use Pin maintenance module with available use count, time. and devices restrictions.

[:arrow_up_small: Back to top](#top)
<a name="support"></a>
## 3. Support this project
This app is free and very much beta code. However, if you like it, derived benefit from it, and want to express your support, donations are appreciated.
* Paypal: https://www.paypal.me/arnbme 

[:arrow_up_small: Back to top]
(#top)<a name="install"></a>
## 4. Installation

There are five modules and an optional Keypad Device Handler (DH) associated with this app  
	
1. Nyckelharpa. 	Parent module. Controls HSM forced arming from a keypad, and User pin verification. Required  

2. Nyckelharpa Modefix. Adjusts HSM mode when HSM State changes, and forced arming from a non keypd source. Required

3. Nyckelharpa Talker.  Creates security related output to TTS, speakers, Pushover, and SMS

4. Nyckelharpa Contact. Controls some actions when a monitored contact sensor (door) opens

5. Nyckelharpa User.    Maintains pins when using the user version of the Centralite Keypad DH.

6. Centralite Keypad.   Keypad device handler for models: Centralite V2 and V3, and Iris V2

* Using the link below, let's begin by installing the Nyckelharpa parent module into Hubitat (HE) from this Github repository. OAuth is not required and should be skipped. Should you want to used the Install's Import button each module's Github raw address is availabe at the beginning of the module.<br />
https://docs.hubitat.com/index.php?title=How_to_Install_Custom_Apps <br />
Then then install Modefix, Talker, Contact and User, ignore OAuth, and do not add these modules as User Apps.

* Should you be using the user Centralite Keypad driver follow thise directions
https://docs.hubitat.com/index.php?title=How_to_Install_Custom_Drivers

*  Next step: Quick Setup Guide 
[:arrow_up_small: Back to top](#top)
<a name="setup"></a>
## 5. Quick Setup Guide
1. In the IDE Devices [create one simulated contact sensor device](#createsim). Later this device is added to SHM Security "contact sensors to use"
2. When using a Keypad read and review section "Xfinity/Iris/Centralite Keypad Installation and TroubleShooting". [Install the suggested Keypad Device Handler (DTH)](https://raw.githubusercontent.com/miriad/Centralite-Keypad/master/devicetypes/mitchpond/centralite-keypad.src/centralite-keypad.groovy) then properly install the keypad into SmartThings. Take your time, [it is a multistep procedure](https://community.smartthings.com/t/release-lowes-iris-and-xfinity-centralite-keypad-dth-apps/58630/716). Note: The RBoy Apps DTH is also supported.
3. Install this smartApp into the IDE: follow directions in ["Installation"](#appinstall)
4. In SmartThings phone app, tap "Automations"  (bottom)--> tap Smartapps (top)--> scroll down then tap "+ Add a SmartApp"--> scroll down then tap "+ My Apps"--> scroll down then tap "SHM_Delay"
5. In this app, SHM_Delay, view the ["Global Application Settings"](#globals). Suggest keeping all the defaults, but define your 3 digit emergency and local police department telephone numbers. When using a keypad set "A real or simulated Keypad" is used to on/true, then click "Done". The app is now installed, but does nothing until a Delay Profile and User Profile (keypads only) are created.
6. Create one or more [Delay Profiles](#delayprofile)
7. When using a keypad, create one or more [User profiles](#userprofile) 
8. Adjust SHM Security monitored sensors, exclude SHM Delay monitored contacts and motion sensors used in step 6 above, and include the simulated sensor(s) defined in step 1.
9. Test your system in Away, and Stay/Night modes for correct operation. 
**_Stop here! When ready, add one or more of the following features documented below._**
10. In Global Settings Implement  "True Night Mode"  (all users) 
11. in Global Settings set  "Mode Fix" on then edit and save the Modefix profile.  Alarm Status and Mode changes stay syncronized. (all users) _This is important for Keypad users who want non keypad  SHM Alert Status and Mode changes to reflect on the keypad's status keys or icons._ 
12. Way Beyond the Basics (Xfinity and Centralite keypad users only. Not for Iris users) Utilize all  available armed keypad modes: Stay, Away, and Night
12. When Keypads are not installed and you want a True Exit Delay, meaning the system is not armed during the exit delay, smartapp [SHM Delay TrueExit](https://community.smartthings.com/t/release-shm-delay-trueexit-create-a-smarthome-true-exit-delay/98696) is suggested.

[:arrow_up_small: Back to top](#top)
<a name="appinstall"></a>

<a name="createsim"></a>
## 7. Creating a Simulated Contact Sensor
Each contact sensor defined in a Delay Profile must be paired with a "simulated contact sensor" device. A single simulated sensor is preferred and may used in multiple Delay Profiles, or if you insist it may be unique. The simulated sensor must be monitored by SHM. 

1. Login to the IDE at https://account.smartthings.com/ 
2. Click “My Devices”
3. Click the “New Device” Button
4. Enter a “Name” for the device, this can be whatever you want.
5. Enter a “Label” for the device, this is optional and can be whatever you want.
6. Enter a “Device Network Id” This can be anything you want, but it cannot duplicate other device ID’s. Example: SIMSEN01
7. “Zigbee” Id should be left blank
8. Select a “Type” from the dropdown, _this must be "Simulated Contact Sensor"_
9. “Version” should be published
10. “Location” should be your hub location, probably “Home”
11. “Hub” should be your hub name.
12. “Group” you won’t be able to select when creating, but these are Groups you’ve created in the Things page in the SmartThings app.
13. Click Create
  
Directions updated from, and originally posted [in this thread](https://community.smartthings.com/t/faq-creating-a-virtual-device/11282/2)
[:arrow_up_small: Back to top](#top)
<a name="SHMsecurity"></a>
## 8. Setting SHM Security for use with this app
In Both: Sensors When home is unoccupied,  and When home is occupied
* Use every open/close sensor: off/false
* Open/Closed Sensors:  _exclude Real Sensors monitored by this app, include Simulated contact sensor used by this app; include other real contact sensors not monitored by this app, **and be sure to exclude all keypads that show up as a sensors**_
* _Motion sensors unoccupied mode (Away): When an optional motion sensor is defined in a Delay Profile remove it from SHM Security monitoring_ 
* Motion sensors for occupied mode (Stay/Night) Exclude motions sensors where you would normally move around at during the night or while you are at home. If you don't know, set no motion sensors in this mode.

[:arrow_up_small: Back to top](#top)
<a name="globals"></a>
## 9. Global Preferences
Controls the operation of this SmartApp

1. _**Start by tapping "Global Settings"**_
2. Disable All Functions. Default: False/Off When on allows this smartapp to remain installed, however it does nothing.
3. Activate "A real or simulated Keypad is used". Default: False/Off Set On/True implements full operation of 3400 series Iris, Xfinity and  Centralite Keypads including User Pin control.
4. Issue intrusion message with name of sensor. Default: True/On Smart Home Monitor intrusion messages will have the name of the simulated sensor used in a delay profile for all SHM Delay issued intrusions. Stongly suggest this be set to True.
5. (Optional) Add 3 digit emergency number on SHM Ddelay intrusion message. 
6. (Optional) Include additional telephone numbers separated with a semicolon on intrusion message.
7. Multiple motions sensors in a Delay Profile. Default Off/False
8. Mode Fix Default: Off/False suggested On/True when using keypads
	* _Requires Modefix Profile be set and saved_
	* When alarm state changes, sets mode to follow alarm state
	* When mode changes, sets alarm state to follow mode
	* Sets any keypad status lights to follow the SHM alarm state and mode
9. True Night Flag When arming in Stay mode from a non-keypad device, or Partial from an Iris Keypad, and a SHM Delay monitored sensor triggers:
	* On - Creates an instant intrusion alert
	* Off - Creates an Entry Delay
10. (Keypad Control: True) One or more 3400 series Keypads used to arm and disarm Smart Home Monitor
11. (Keypad Control: True) True Exit Delay in seconds when arming in Away mode from any keypad. Range 0..90 Default: 30
	* This is a true exit delay. During this delay all keypads sound exit delay tones, then system arms in Away mode
12. (Keypad Control: True) Required Routines that execute when valid User pin is entered. Routines must set both Alarm State and Mode. Defaults follow
	* Off - I'm Back!
	* Stay/Partial - Good Night!
	* Night - Good Night!
	* Away/On - Goodbye
13. (Keypad Control: True) Panic Key/Pin is Monitored Default: On/True When Panic key is held or Panic pin is entered Issue instant intrusion alarm.
14. (Keypad Control: True) Log valid pin entries Default: On/True
	* additional settings show for routine pin messages to phoneapp notification log, Contactss, push and SMS
15. (Keypad Control: True) Log invalid pin entries Default: On/True
	* additional settings show for routine pin messages to phoneapp notification log, Contactss, push and SMS
16. Simulated sensors must be unique. Default: Off/False Normally one simulated sensor is needed with this SmartApp. If for some realon you want to use multiple simulated sensors, set this slider to On/True.
17. True Entry Delay. Default Off/False This is a last resort when adding multiple sensors to a delay profile does not correct a false intrusion alert during Entry Delay. _It manipulates the SmartThings Alarm State so the Dashboard or ActionTiles may not reflect the actual status._
18. _**Tap Save/Done to save these settings**_

[:arrow_up_small: Back to top](#top)

<a name="delayprofile"></a>
## 10. Delay Profiles
Define a Delay Profile for each real contact sensor where an entry or exit delay is desired. Generally this is a door that is actively used for access or egress. One or more delay profiles are required.
1. _**Start by tapping "Create A New Delay Profile" or tapping on an exisiting Delay Profile name**_
2. Select the real contact sensor associated with the door to be delayed 
3. Select the simulated contact device that was initially defined. This device may be used in multiple delay profiles.
4. (Optional) When a motion sensor triggers an intrusion during an entry or exit delay: select a motion sensor device
	When multiple motion sensors must be defined: set global Allow Multiple Motion Sensors to On/True
	Defined motion sensors are monitored during Alarm Status: Away
5. (Optional) When the Real Contact sensor is rejected as "Simulated" enter 4 to 8 alphanumeric characters from the IDE Device Type to force accept the device
6. Profile name is internally set to "Profile Delay: (real contact sensor name)" It may be modified, but must be unique.
7. Tap 'Next' on top of page
8. _**The 'Entry and Exit Data' page displays**_
9. Set the entry delay time in seconds from 0 to 90. Default:30
10. When arming from a non-keypad device set the exit delay time in seconds from 0 to 90. Default:30
	Keypad exit delay time is defined in Global Settings. 
11. (Optional) When armed in Away mode some motion sensors may react to a door movement before the contact sensor registers as open. Enter time in seconds to ignore the motion sensors defined in step 4 above before the contact sensor must register as open. Leave at 0 unless you encounter a problem.
12. (Optional _shows only when global "A real or simulated Keypad.." is Off_) set keypads where to sound the Entry Delay tones
13. (Optional) Beep these devices during entry delay. Note Keypads defined in global settins generate Entry Delay Tones
14. (Optional) "Chime" or beep these devices when the contact sensor defined in step 1 above opens and Alarm State is Off
15. Tap 'Next' on top of page
16. _**The 'Open Door Monitor and Notifications' page displays**_
17. Set the maximum number of open door warning messages. Default: 2, Minimum: 1
18. Set the number of minutes between open door messages from 1 to 15. Default: 1
19. Notifcation Settings for SHm Delay Open Door and SHM Delay Intrusion Alert messages 	
	* Log to phone app notifications log. Default: true (set to off when duplicated messages are issued)
	* Notify Contacts? (Shows when hidden ST Contact Book is enabled)
	* Should the message be issued as an application push notification. 
	* Should the message be sent by text (SMS) message. Default: false
		When set to true: Enter telephone number. Separate multiple numbers with a semicolon ; 
20. Tap 'Done/Save' on top of page
21. _**Adjust Smart Home Monitor**_
22. In phone app tap Dashboard-->Tap Smart Home Monitor-->tap Gear on top right-->tap Security
23. On sensors to monitor when home is unoccupied
	* In Open Closed Sensors: Remove sensor defined in paragraph 2 above
	* In Open Closed Sensors: If not already showing, add sensor defined in paragraph 3 above
	* In Motion Sensors: Remove motion sensors defined in paragraph 4 above
24. Tap "Next", sensors to be monitored when home is occupied displays
	* In Open Closed Sensors: Remove sensor defined in paragraph 2 above
	* In Open Closed Sensors: If not already showing, add sensor defined in paragraph 3 above
25.	Tap "Next"-->Tap "Save"	

[:arrow_up_small: Back to top](#top)
<a name="userprofile"></a>
## 11. User Profiles
When global "A real or simulated Keypad" is used is true, define one of more User Pin Profiles.
1. _**Start by tapping "Create A New User Profile" or tapping on an existing User Profile name**_
2. When pin has date, day or time restrictions: set Scheduled slider to On/True.
3. When pin has mode or device restrictions: set Restriction slider to On/True.
4. Override Global Valid Pin message routing: Default Off/False 
	* When on additional settings show for routing pin messages to phoneapp notification log, Contacts, push and SMS.
5. Enter a unique 4 digit pin number.
	* _Support for quick arming on Iris keypad without entering a pin using pin 0000.
Iris harware sends a 0000 pin code when single or double tapping the On or Partail key without entering a pin. The device firmware determines if it is single of double tapped.
To implement: add User or UserRoutinePiston pin type with pin 0000.  The Iris does not send the 0000 when the Off key is pressed. For security reasons, suggest setting the User Profile flag to ignore a real 0000 pin entry with the Off button.
6. Enter the user name or unique identifier associated with this pin. This will show in phone app Notifications log.
7. Set Pin usage
	* User - this is a person, pin arms or disarms SmartThings
	* UserRoutinePiston - this is a person, pin arms or disarms SmartThings. then executes optional Routines, then executes optional Pistons 
	* Ignore - pin is treated as undefined
	* Disabled - pin is accepted but does nothing
	* Routine - pin is accepted and executes a Routine. Executes a unique Routine for Off, Stay(Partial) or Away(On) mode   
	* Piston - pin is accepted and executes a WebCore Piston. Executes a unique Piston for Off, Stay(Partial) or Away(On) mode 
		requires the full WebCore Piston's external URL
	* Panic - pin is accepted and creates a SmartThings intrusion when global Panic is set to true
		Iris keypads and some Centralite keypads have a Panic key or setting
8. Maximum Times a pin may be used before it is rejected. Set to 0 (zero) for infinite. 
9. When Maximum Times is greater then 0, set the reset slider to true to reenable the pin/user. Use count is displayed
10. Profile name is internally set to "Profile User: (data entered in paragraph 4 above)" It may be modified, but must be unique.
11. Tap 'Next' on top of page
12. _**When Schedule slider is true: Scheduling Rules page appears. All fields optional**_
	* Set weekdays pin may be used
	* Set start and stop time the pin may be used
	* Set initial date pin may be used. Format MMM dd, yyyy where MMM is Jan thru Dec
	* Set last date pin may be used.
	* Click 'Next' on top of page
13. _**When Restriction slider is true: Restriction Rules page appears. All fields optional**_
	* Set Mode(s) when pin is allowed. Null = all modes
	* Select Real Devices where pin may be used
	* Select Simulated Devices where pin may be used
	* Click 'Next' on top of page
14. _**A User profile summary appears. Verify, then click 'Save'**_

[:arrow_up_small: Back to top](#top)
<a name="modefix"></a>
## 12. ModeFix, optional (Default: False/Off), but recommended when: 
* You want to syncronize the SHM Alarm State and SHM Mode settings

* There are user created Modes
* _**Keypads are in use and system is armed with something other than a Keypad**_

**How this functions**
Syncronizes the SHM Alarm State, SHM Mode, and any defined Keypad Light modes when changes are made to the SHM Alarm State, or SHM Mode. 
* When SHM Alarm State is changed, for example from the Dashbboard, sets the SHM Mode to follow when the current mode does not match a user defined mode for the Alarm State

* When SHM Mode is changed, for example from WebCore or ActionTiles, set the  SHM Alarm State to follow when the mode is defined in Modefix, and the defined Alarm state does not match the current Alarm State.

* When using a keypad, the keypad's arm state lights follow changes to the SHM alarm state and mode.

Activation requires two steps:
1. In Global Application Settings set the Fix Mode option on/true 
2. Create then save A ModeFix Profile (default profile initially shows in the child app)

Have you ever wondered why Mode restricted Routines, SmartApps, and Pistons sometimes fail to execute, or execute when they should not? I gave up trying to use mode for Core Pistons, now I know why it did not work.

Perhaps like me you conflated AlarmState and Mode, however they are separate and independent SmartThings settings. **_When Alarm State is directly changed using the SmartThings Dashboard Home Solutions, a Smartapp, or a Webcore Piston---surprise, Mode does not change!_**

SHM routines generally, but not always, have a defined SystemAlarm and Mode setting. Experienced SmartThings users seem to favor changing the AlarmState using SHM routines, avoiding use of the Dashboard's Home Solutions. 

If like me, you can't keep track of all this, or use the Dashboard for changing the AlarmState, or Action Tiles for changing the mode, the Mode Fix option may be helpful. For each AlarmState, set the Valid Mode states, and a Default Mode. Mode Fix attempts to correctly set the Mode by monitoring AlarmState and Mode for changes. 
[:arrow_up_small: Back to top](#top)


<a name="howtousekeypads"></a>
## 13. How use Keypads with this app.
1. In Global Settings set the slider for "A real or simulated Keypad..." to on/true. Set global keypad settings then Save.

3. Create a Delay Profile for each door you want delayed
4. Create one or more User Profiles (pin numbers)
5. Test your system

**Keypads (Installed indoors)  Verify the keypad is operating normally before using it with the SHM Delay app!**
* Global Settings --> Keypad Routines suggested: Away: Goodbye, Disarm: I'm Back, Stay and Night: Good Night 
_Selected routines must have a "Set Smart Home Monitor to "alarm state", and a "Change the mode to "valid mode" -**or the SHm Delay keypad control will malfunction**_.
Exit Delay: seconds 15 to 90. _This is the Exit Delay_ and sounds exit tones when arming from the keypad. While this delay is active SHM is not armed
* On each User: Set 4 digit pin code; 

SHM Delay Sensor Profile settings:
* Entry Delay: something greater than zero.
* Exit Delay: zero (0)
* Set keypads to sound entry delay tones 

**Smart Door Locks and outdoor Keypads**
When unlocking with smart door lock or entering a pin on an outdoor keypad with a contact sensor on the door, and the contact sensor reports an intrusion before the system sets the Alarm Status to off:
* set exit delay to 0
* set entry delay to > 0

[:arrow_up_small: Back to top](#top)

<a name="keypadtrouble"></a>
## 14. Supported Keypads, Installation and TroubleShooting

_**Before proceeding please insure your keypad is supported by the suggested Device Handler noted below, and is properly installed**_ on Smarthings: 
*  [How to install a keypad into SmartThings](https://community.smartthings.com/t/release-lowes-iris-and-xfinity-centralite-keypad-dth-apps/58630/716)
* Centralite/Xfinity (3400)- Supported [Centralite 3400 PDF information](http://www.centralite.com/downloads/DataSheet-3SeriesSecurityKeypad.pdf)  Xfinity is a rebadged Centralite
* Centralite V3 (3400-G) Panic supported by pressing _both_ "police badge" icons 
* _Iris V1 - not supported_
* [Iris V2 - Supported (3405-L)  Iris V2 Information](http://pdf.lowes.com/useandcareguides/812489023049_use.pdf) Add user pin profile 0000 for Instant On and Partial, plus user pins for off* 
* Iris V3 Supported (iMagic 1112-S) using RBoy Apps DTH Version 01.05.04,  SHM Delay V2.2.4 and  SHM Delay Child V2.1.2 or higher. Add user pin profile 0000 for Instant On and Partial, plus user pins for off* 
* UEI  keypad  Supported using RBoy Apps DTH 


This app was developed and tested with the DTH at miriad/Centralite-Keypad/master, or you may get it at this link: https://raw.githubusercontent.com/miriad/Centralite-Keypad/master/devicetypes/mitchpond/centralite-keypad.src/centralite-keypad.groovy  This DTH is for Centralite manufactured keypads only: the Centralite/Xfinity and Iris V2.
 
The [RBoy Apps Keypad DTH](https://community.smartthings.com/t/release-enhanced-zigbee-keypad-lock-centralite-lowe-iris-uei-and-xfinity-keypads-device-handler/124776) is also supported. Use version 01.05.04 or higher for best compatibility. in global settings set "I am using RBoy Apps DTH" flag to on/true. The UEI and Iris V3 keypads are supported when using this DTH.  When using  both RBoy's Lock User Management SmartApp and SHM Delay. use the RBoy Keypad DTH.

Other DTHs may function, but may not produce entry or exit tones. The Miriad DTH should look like  the following image: 
![image|301x500](upload://36gFgkLGwH8tLI0iCCZfxFst2qq.png)

* No entry or exit tones sounding: 

Press and hold the 8 button until you get a confirmation beep to toggle the chime on or off.

On the keypad press and hold the 2 key to raise the volume until you hear a tone, press 5 to lower the volume.
Then in the ST phone app go to My Home, select the keypad, then tap on settings. Set length of beep to 10, save it,  then press the 'Sound' icon. You should hear tones. 
If you hear tones continue on 
* Still no entry tone when door is opened. Make sure you are beyond the exit delay time, the keypad is selected in the real sensor's Delay profile, and entry delay is greater than 0.
* Still no exit tone: Make sure the global "true exit delay" time is greater than 0 
[More information](https://www.xfinity.com/support/home-security/wireless-keypad-faqs/) Note: Some information is specific to using an Xfinity keypad on an Xfinity system.
[:arrow_up_small: Back to top](#top)

## [Documentation Continued at this link](https://community.smartthings.com/t/release-shm-delay-version-2-0/121800/74)
