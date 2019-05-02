<a name="top"></a>
# Nyckelharpa
![image nyckelharpa](images/nyckelharpa.jpg)<br /> 
**The buttons and levers controlling Hubitat Elevation's, Home Security Monitor's strings.** 
## Table of Contents 
[&ensp;1. Purpose](#purpose)<br />
[&ensp;2. Features](#features)<br />
[&ensp;3. Donate](#support)<br />
[&ensp;4. Installation](#install)<br />
[&ensp;5. Quick Setup Guide](#setup)<br />
[&ensp;6. Global Settings](#globals)<br />
[&ensp;7. Adjust HSM Settings](#adjustHSM)<br />
[&ensp;8. Modefix Setup and Usage](#modefix)<br />
[&ensp;9. Talker messages](#talker)<br />
[10. Contact Profiles](#contact)<br />
[11. Keypad Device Handler](#keypadDH)<br />
[12. User/Pin Profiles](#userpin)<br />
[13. Testing](#testing)<br />
[14. Get Help, report an issue, or contact information](#help)
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

[:arrow_up_small: Back to top](#top)

<a name="install"></a>
## 4. Installation

There are five modules and an optional Keypad Device Handler (DH) associated with this app  
 <table style="width:100%">
  <tr>
    <th>Module Name</th>
    <th>Function</th>
    <th>Required</th>
  </tr>
  <tr>
    <td>Nyckelharpa</td>
    <td>Parent module. Controls HSM forced arming from a keypad, and User pin verification</td>
    <td>Yes</td>
  </tr>
  <tr>
    <td>Nyckelharpa Modefix</td>
    <td>Adjusts HSM mode when HSM State changes, and forced arming from a non keypd source</td>
    <td>Yes</td>
  </tr>
  <tr>
    <td>Nyckelharpa Talker</td>
    <td>Creates security related output to TTS, speakers, Pushover, and SMS</td>
    <td>Optional</td>
  </tr>
  <tr>
    <td>Nyckelharpa Contact</td>
    <td>Controls some actions when a monitored contact sensor (door) opens</td>
    <td>Optional</td>
  </tr>
  <tr>
    <td>Nyckelharpa User</td>
    <td>Maintains User pin codes when using the app's Centralite Keypad DH</td>
    <td>Optional</td>
  </tr>
  <tr>
    <td>Centralite Keypad</td>
    <td>Keypad device handler for models: Centralite V2 and V3, and Iris V2. Created and converted to HE by Mitch Pond</td>
    <td>Optional</td>
  </tr>
</table> 
* Using the link below, let's begin by installing the Nyckelharpa parent module into Hubitat (HE) from this Github repository. OAuth is not required and should be skipped. Should you want to used the Install's Import button each module's Github raw address is availabe at the beginning of the module.<br />
https://docs.hubitat.com/index.php?title=How_to_Install_Custom_Apps <br />
Then then install Modefix, Talker, Contact and User, ignore OAuth, and do not add these modules as User Apps.

* Should you be using the user Centralite Keypad driver follow thise directions<br />
https://docs.hubitat.com/index.php?title=How_to_Install_Custom_Drivers

*  Next step: Quick Setup Guide

[:arrow_up_small: Back to top](#top)
<a name="setup"></a>
## 5. Quick Setup Guide
Detailed instuctions for each step follow the Quick Setup Guide. Begin by clicking on APPs in the HE menu, then click on Nyckelharpa
1. Setup [Global Settings](#globals), then click Next, then Done.

2. Adjust HSM's settings
3. Create the Modefix profile
4. Optionally create a Talker profile
5. Optionally create one or more Contact profiles
6. Optionally set a one or more existing keypad devices to use the user provided Centralite Driver, then add one or more User pin profiles 

[:arrow_up_small: Back to top](#top)

<a name="globals"></a>
## 6. Global Settings

[:arrow_up_small: Back to top](#top)

<a name="adjustHSM"></a>
## 7. Adjust HSM Settings. Includes information on Forced HSM Arming.
  
[:arrow_up_small: Back to top](#top)
<a name="modefix"></a>
## 8. Modefix setup and usage

[:arrow_up_small: Back to top](#top)
<a name="talker"></a>
## 9. Talker messages

[:arrow_up_small: Back to top](#top)
<a name="contact"></a>
## 10. Contact Profiles

[:arrow_up_small: Back to top](#top)
<a name="keypadDH"></a>
## 11. Keypad Device Handler

[:arrow_up_small: Back to top](#top)
<a name="userpin"></a>
## 12. User pin Profiles

[:arrow_up_small: Back to top](#top)
<a name="testing"></a>
## 13. Testing

[:arrow_up_small: Back to top](#top)
<a name="help"></a>
## 14. Get Help, report an issue, and contact information
* Post requests for assistance, or to report an issue, on the HE Community's Nyckelharpa forum. Private messages may be directed to @arnbme on the HE community site.


[:arrow_up_small: Back to top](#top)
