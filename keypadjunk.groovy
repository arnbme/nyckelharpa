/*
	Iris V2 Keypad

	Copyright 2016, 2017, 2018, 2019 Hubitat Inc.  All Rights Reserved

    2019-04-02 2.0.9 maxwell
        -updates for countdown and confirmation sounds
	2019-03-05 2.0.7 maxwell
	    -initial pub
*/

import groovy.json.JsonSlurper
import groovy.json.JsonOutput

metadata {
    definition (name: "Iris V2 Keypad(DEV)", namespace: "Hubitat", author: "Mike Maxwell") {

        capability "Battery"
        capability "Configuration"
        capability "Motion Sensor"
        capability "Sensor"
        capability "Temperature Measurement"
        capability "Refresh"
        capability "Security Keypad"
        capability "Tamper Alert"
        capability "Alarm"
        capability "Tone"

        command "armNight"
        command "setArmNightDelay", ["number"]
        command "setArmHomeDelay", ["number"]
        command "entry" //fired from HSM on system entry

        attribute "armingIn", "NUMBER"

        command "setPartialFunction"

        fingerprint profileId: "0104", inClusters: "0000,0001,0003,0020,0402,0500,0501,0B05,FC04", outClusters: "0019,0501", manufacturer: "CentraLite", model: "3405-L", deviceJoinName: "Iris 3405-L Keypad"
        fingerprint profileId: "0104", inClusters: "0000,0001,0003,0020,0402,0500,0501,0B05,FC04,FC05", outClusters: "0019,0501", manufacturer: "CentraLite", model: "3405-L", deviceJoinName: "Iris 3405-L Keypad V2"

    }

    preferences{
        input name: "optEncrypt", type: "bool", title: "Enable lockCode encryption", defaultValue: false, description: ""
        input name: "pinOnArmAway", type: "bool", title: "Verify pin to arm away", defaultValue: false, description: ""
        input name: "pinOnArmHome", type: "bool", title: "Verify pin to arm home/night", defaultValue: false, description: ""
        input ("motionTime", "number", title: "Time in seconds for Motion to become Inactive (Default:10, 0=disabled)",	defaultValue: 10, displayDuringSetup: false)
        input name: "enablePanic", type: "bool", title: "Enable Panic", defaultValue: false, description: ""
        input "refTemp", "decimal", title: "Reference temperature", description: "Enter current reference temperature reading", range: "*..*"
        input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true, description: ""
        input name: "txtEnable", type: "bool", title: "Enable descriptionText logging", defaultValue: true, description: ""
    }
}

def logsOff(){
    log.warn "debug logging disabled..."
    device.updateSetting("logEnable",[value:"false",type:"bool"])
}

def installed(){
    log.warn "installed..."
    state.exitDelay = 0
    state.entryDelay = 0
    state.armNightDelay = 0
    state.armHomeDelay = 0
    state.armMode = "00"
    state.fnPartial = "01"
    sendEvent(name:"maxCodes", value:20)
    sendEvent(name:"codeLength", value:4)
    sendEvent(name:"alarm", value: "off")
    sendEvent(name:"securityKeypad", value: "disarmed")
}

def uninstalled(){
    return zigbee.command(0x0000,0x00)
}

// New code sgrayban

def motionON() {
    if (txtEnable) log.debug "--- Motion Detected (ON)"
    sendEvent(name: "motion", value: "active", displayed:true, isStateChange: true)
	def motionTimeRun = (settings.motionTime?:0).toInteger()
	if (motionTimeRun > 0) {
		if (txtEnable) log.debug "--- Will become inactive in $motionTimeRun seconds"
		runIn(motionTimeRun, "motionOFF")
	}
}

def motionOFF() {
	if (txtEnable) log.debug "--- Motion Inactive (OFF)"
    sendEvent(name: "motion", value: "inactive", displayed:true, isStateChange: true)
}

// End new code

def parse(String description) {
    def resp = []
    if (description.startsWith("zone status")) {
        def zoneStatus = zigbee.parseZoneStatus(description)
        getTamperResult(zoneStatus.tamper)
    } else if (description.startsWith("enroll request")) {
        resp.addAll(zigbee.enrollResponse(0))
    } else {
        def descMap = zigbee.parseDescriptionAsMap(description)
        if (logEnable) log.debug "descMap: ${descMap}"
        def clusterId = descMap.clusterId ?: descMap.cluster
        def cmd = descMap.command

        switch (clusterId) {
            case "0001":
                if (descMap.value) {
                    value = hexStrToUnsignedInt(descMap.value)
                    getBatteryResult(value)
                }
                break
            case "0501":
                if (cmd == "07" && descMap.data.size() == 0) { //get panel status client -> server
                    if (state.bin == -1) motionON() // sgrayban
                    resp.addAll(sendPanelResponse(false))
                } else if (cmd == "00") {
                    state.bin = -1
                    def armRequest = descMap.data[0]
                    def asciiPin = descMap.data[2..5].collect{ (char)Integer.parseInt(it, 16) }.join()
                    resp.addAll(sendArmResponse(armRequest,isValidPin(asciiPin, armRequest)))
                } else if (cmd == "04") { //panic client -> server
                    if (enablePanic) resp.addAll(togglePanic())
                } else {
                    if (logEnable) log.info "0501 skipped: ${descMap}"
                }
                break
            case "0402":
                if (descMap.value) {
                    def tempC = hexStrToSignedInt(descMap.value)
                    getTemperatureResult(tempC)
                }
                break
            default :
                if (logEnable) log.info "skipped: ${descMap}, description:${description}"
        }
        if (resp){
            sendHubCommand(new hubitat.device.HubMultiAction(resp, hubitat.device.Protocol.ZIGBEE))
        }
    }
}

//Security Keypad commands
def beep(){
	if (txtEnable) log.debug "Sending beeps"
    return "he raw 0x${device.deviceNetworkId} 1 1 0xFC04 {15 4E 10 00 00 00}"
}

def setEntryDelay(delay){
    log.trace "setEntryDelay(${delay})"
    state.entryDelay = delay != null ? delay.toInteger() : 0
}

def setExitDelay(Map delays){
    state.exitDelay = (delays?.awayDelay ?: 0).toInteger()
    state.armNightDelay = (delays?.nightDelay ?: 0).toInteger()
    state.armHomeDelay = (delays?.homeDelay ?: 0).toInteger()
}

def setExitDelay(delay){
    state.exitDelay = delay != null ? delay.toInteger() : 0
}

def setArmNightDelay(delay){
    state.armNightDelay = delay != null ? delay.toInteger() : 0
}

def setArmHomeDelay(delay){
    state.armHomeDelay = delay != null ? delay.toInteger() : 0
}

def setPartialFunction(mode = null) {
    if ( !(mode in ["armHome","armNight"]) ) {
        if (txtEnable) log.warn "custom command used by HSM"
    } else if (mode in ["armHome","armNight"]) {
        state.fnPartial = mode == "armHome" ? "01" : "02"
    }
}

def setCodeLength(length){
    def descriptionText = "${device.displayName} codeLength set to 4"
    if (txtEnable) log.info "${descriptionText}"
    sendEvent(name:"codeLength",value:"${4}",descriptionText:descriptionText)
}

def setCode(codeNumber, code, name = null) {
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

def entry(){
    def intDelay = state.entryDelay ? state.entryDelay.toInteger() : 0
    if (intDelay) return entry(intDelay)
}

def entry(entranceDelay){
    if (entranceDelay) {
        def ed = entranceDelay.toInteger()
        state.bin = 1
        state.delayExpire = now() + (ed * 1000)
        state.armingMode = "05" //entry delay
        def hexVal = intToHexStr(ed)
        runIn(ed + 5 ,clearPending)
        return [
                "he raw 0x${device.deviceNetworkId} 1 1 0x0501 {19 01 04 05 ${hexVal} 01 01}"
        ]
    }
}

def disarm(exitDelay = null) {
    state.armPending = false
    state.bin = 1
    if (exitDelay == null) sendArmResponse("00",getDefaultLCdata())
    else sendArmResponse("00",getDefaultLCdata(),exitDelay.toInteger())
}

def armHome(exitDelay = null) {
    if (logEnable) log.debug "armHome(${exitDelay}, armMode:${state.armMode}, armingMode:${state.armingMode})"
    if (state.armMode == "01") {
        sendPanelResponse(false)
        if (logEnable) log.trace "armHome(${exitDelay}) called, already armedHome"
        return
    }
    state.bin = 1
    if (exitDelay == null) sendArmResponse("01",getDefaultLCdata())
    else sendArmResponse("01",getDefaultLCdata(),exitDelay.toInteger())
}

def armNight(exitDelay = null) {
    if (logEnable) log.debug "armNight(${exitDelay}, armMode:${state.armMode}, armingMode:${state.armingMode})"
    if (state.armMode == "01") {
        sendPanelResponse(false)
        if (logEnable) log.trace "armNight(${exitDelay}) called, already armNight"
        return
    }
    state.bin = 1
    if (exitDelay == null) sendArmResponse("01",getDefaultLCdata())
    else sendArmResponse("01",getDefaultLCdata(),exitDelay.toInteger())
}

def armAway(exitDelay = null) {
    if (logEnable) log.debug "armAway(${exitDelay}, armMode:${state.armMode}, armingMode:${state.armingMode})"
    if (state.armMode == "03") {
        sendPanelResponse(false)
        if (logEnable) log.trace "armAway(${exitDelay}) called, already armAway"
        return
    }
    state.bin = 1
    if (exitDelay == null) sendArmResponse("03",getDefaultLCdata())
    else sendArmResponse("03",getDefaultLCdata(),exitDelay.toInteger())
}

//alarm commands
def off(){
    def value = "off"
    state.bin = -1
    def descriptionText = "${device.displayName} alarm was turned ${value}"
    if (txtEnable) log.info "${descriptionText}"
    state.panic = "inactive"
    sendEvent(name: "alarm",value: value,descriptionText: "${descriptionText}")
    return ["he raw 0x${device.deviceNetworkId} 1 1 0x0501 {19 01 04 00 00 01 01}"] //clear
}

private togglePanic(){
    def panicState = state.panic ?: "inactive"
    if (panicState == "inactive"){
        siren()
    } else {
        off()
    }
}

def siren(){
    state.panic = "active"
    def value = "siren"
    state.bin = 1
    def descriptionText = "${device.displayName} alarm set to ${value}"
    if (txtEnable) log.info "${descriptionText}"
    sendEvent(name: "alarm",value: value,descriptionText: "${descriptionText}")
    return ["he raw 0x${device.deviceNetworkId} 1 1 0x0501 {19 01 04 07 00 01 01}"]
}

def strobe(){
    log.warn "alarm strobe command is not supported on this device"
}

def both(){
    siren()
}

//private
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

private isValidPin(code, armRequest){
    def data = getDefaultLCdata()
    if ((pinOnArmAway && armRequest == "03") || (pinOnArmHome && armRequest == "01") || armRequest == "00") {
        //verify pin
        def lockCode = lockCodes.find{ it.value.code == "${code}" }
        if (lockCode) {
            data.codeNumber = lockCode.key
            data.name = lockCode.value.name
            data.code = code
        } else {
            data.isValid = false
            if (txtEnable) log.warn "Invalid pin entered [${code}] for arm command [${getArmCmd(armRequest)}]"
        }
    }
    return data
}

private sendPanelResponse(alert = false){
    def resp = []
    resp.addAll(["he raw 0x${device.deviceNetworkId} 1 1 0x0501 {19 01 05 ${state.armMode ?: "00"} 00 00 00}"])
    if (alert) resp.addAll(["delay 300", "he raw 0x${device.deviceNetworkId} 1 1 0x0501 {19 01 05 05 01 01 01}"])
    return resp
}

def clearPending(){
    if (state.armPending == false) return
    def resp = []
    state.armPending = false
    getArmResult()
    resp.addAll([
            "he raw 0x${device.deviceNetworkId} 1 1 0x0501 {09 01 00 ${state.armMode}}", "delay 200",
            "he raw 0x${device.deviceNetworkId} 1 1 0x0501 {09 01 04 05 01 01 01}"
    ])
    sendHubCommand(new hubitat.device.HubMultiAction(resp, hubitat.device.Protocol.ZIGBEE))

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

private sendArmResponse(armRequest,lcData, exitDelay = null) {
    def isInitiator = false
    if (exitDelay == null) {
        isInitiator = true
        switch (armRequest) {
            case "01": //armHome
                if (state.fnPartial == "02") {
                    exitDelay = (state.armNightDelay ?: 0).toInteger()
                } else {
                    exitDelay = (state.armHomeDelay ?: 0).toInteger()
                }
                break
            case "03": //armAway
                exitDelay = (state.exitDelay ?: 0).toInteger()
                break
            default :
                exitDelay = 0
                break
        }
    }
    lcData.isInitiator = isInitiator
    def cmds = []

    //all digital arm changes are valid
    def changeIsValid = true
    def changeText = "sucess"

    if (state.bin == -1) {
        if ( (armRequest == "00" && !lcData.isValid) || (armRequest == "01" && pinOnArmHome && !lcData.isValid) ||
                (armRequest == "03" && pinOnArmAway && !lcData.isValid)	) {
            cmds.addAll(["he raw 0x${device.deviceNetworkId} 1 1 0x0501 {09 01 00 04}"])
            changeIsValid = false
            changeText = "invalid pin code"
        }
    }
    if (logEnable) log.trace "sendArmResponse- ${changeText}, bin:${state.bin}, armMode:${state.armMode} -> armRequest:${armRequest}, exitDelay:${exitDelay}, isInitiator:${isInitiator}, lcData:${lcData}"

    if (changeIsValid) {
        state.armMode = armRequest
        state.lcData = encrypt(JsonOutput.toJson(lcData))
        def arming = (armRequest == "01") ? "08" : (armRequest == "02") ? "09" : (armRequest == "03") ? "0A" : "00"
        if (exitDelay && armRequest != "00") {
            def hexVal = intToHexStr(exitDelay)
            state.armingMode = arming
            runIn(exitDelay + 2 ,clearPending)
            state.armPending = true
            cmds.addAll([
                    "he raw 0x${device.deviceNetworkId} 1 1 0x0501 {09 01 00 ${armRequest}}", "delay 200",
                    "he raw 0x${device.deviceNetworkId} 1 1 0x0501 {19 01 04 05 ${hexVal} ${armRequest == "03" ? "01" : "00"} 01}" //count down on arm away only
            ])
        } else {
            state.armPending = false
            if (state.bin != 1) {
                cmds.addAll([
                        "he raw 0x${device.deviceNetworkId} 1 1 0x0501 {09 01 00 ${armRequest}}","delay 200",
                        "he raw 0x${device.deviceNetworkId} 1 1 0x0501 {09 01 04 ${arming} 00 00 01}","delay 1000",
                        "he raw 0x${device.deviceNetworkId} 1 1 0x0501 {09 01 04 ${armRequest} 00 00 01}"
                ])
            } else {
                cmds.addAll([
                        "he raw 0x${device.deviceNetworkId} 1 1 0x0501 {09 01 04 05 01 01 01}","delay 3500",
                        "he raw 0x${device.deviceNetworkId} 1 1 0x0501 {09 01 04 ${armRequest} 00 00 01}"
                ])
            }
            getArmResult()
        }
        if (isInitiator) {
            def value = armRequest == "00"  ? 0 : exitDelay
            if (state.fnPartial == "02" && armRequest != "00") {
                sendEvent(name:"armingIn", value: value,data:[armMode:getArmText("02"),armCmd:getArmCmd("02")], isStateChange:true)
            } else {
                sendEvent(name:"armingIn", value: value,data:[armMode:getArmText(armRequest),armCmd:getArmCmd(armRequest)], isStateChange:true)
            }
        }
    }
    return cmds
}

def updated(){
    log.info "updated..."
    log.warn "debug logging is: ${logEnable == true}"
    log.warn "description logging is: ${txtEnable == true}"
    log.warn "encryption is: ${optEncrypt == true}"
    updateEncryption()
    if (logEnable) runIn(1800,logsOff)

    def crntTemp = device?.currentValue("temperature")
    if (refTemp && crntTemp && state.sensorTemp) {
        def prevOffset = (state.tempOffset ?: 0).toFloat().round(2)
        def deviceTemp = state.sensorTemp.toFloat().round(2)
        def newOffset =  (refTemp.toFloat() - deviceTemp).round(2)
        def newTemp = (deviceTemp + newOffset).round(2)
        //send new event on offSet change
        if (newOffset.toString() != prevOffset.toString()){
            state.tempOffset = newOffset
            def map = [name: "temperature", value: "${newTemp}", descriptionText: "${device.displayName} temperature offset was set to ${newOffset}�${location.temperatureScale}"]
            if (txtEnable) log.info "${map.descriptionText}"
            sendEvent(map)
        }
        //clear refTemp so it doesn't get changed later...
        device.removeSetting("refTemp")
    }
}

def getArmCmd(armMode){
    switch (armMode){
        case "00": return "disarm"
        case "01": return "armHome"
        case "02": return "armNight" //arm sleep on Xfinity keypad
        case "03": return "armAway"
    }
}

def getArmText(armMode){
    def result
    switch (armMode){
        case "00":
            result = "disarmed"
            break
        case "01":
            if (state?.fnPartial == "02") result = "armed night"
            else result = "armed home"
            break
        case "02":
            if (state?.fnPartial == "01") result = "armed home"
            else result = "armed night" //arm sleep on Xfinity keypad
            break
        case "03":
            result = "armed away"
            break
    }
    return result
}

private getArmResult(){
    def value = getArmText(state.armMode)
    def type = state.bin == -1 ? "physical" : "digital"
    state.bin = -1
    state.armingMode = state.armMode

    def descriptionText = "${device.displayName} was ${value} [${type}]"
    def lcData = parseJson(decrypt(state.lcData))

    state.lcData = null
    def lockCode = JsonOutput.toJson(["${lcData.codeNumber}":["name":"${lcData.name}", "code":"${lcData.code}", "isInitiator":lcData.isInitiator]] )
    if (txtEnable) log.info "${descriptionText}"
    if (optEncrypt) {
        sendEvent(name:"securityKeypad", value: value, data:encrypt(lockCode), type: type, descriptionText: descriptionText)
    } else {
        sendEvent(name:"securityKeypad", value: value, data:lockCode, type: type, descriptionText: descriptionText)
    }
}

private getTamperResult(rawValue){
    def value = rawValue ? "detected" : "clear"
    if (logEnable) "getTamperResult: ${value}"
    def descriptionText = "${device.displayName} tamper is ${value}"
    if (txtEnable) log.info "${descriptionText}"
    sendEvent(name: "tamper",value: value,descriptionText: "${descriptionText}")
}

private getTemperatureResult(valueRaw){
    if (logEnable) log.debug "getTemperatureResult: ${valueRaw}"
    valueRaw = valueRaw / 100
    def value = convertTemperatureIfNeeded(valueRaw.toFloat(),"c",2)
    state.sensorTemp = value
    if (state.tempOffset) {
        value =  (value.toFloat() + state.tempOffset.toFloat()).round(2).toString()
    }
    def name = "temperature"
    def descriptionText = "${device.displayName} ${name} is ${value}�${location.temperatureScale}"
    if (txtEnable) log.info "${descriptionText}"
    sendEvent(name: name,value: value,descriptionText: descriptionText, unit: "�${location.temperatureScale}")
}

private getBatteryResult(rawValue) {
    if (rawValue == null) return
    if (logEnable) log.debug "getBatteryResult: ${rawValue}"
    def descriptionText
    def value
    def minVolts = 26
    def maxVolts = 30
    def pct = (((rawValue - minVolts) / (maxVolts - minVolts)) * 100).toInteger()
    value = Math.min(100, pct)
    descriptionText = "${device.displayName} battery is ${value}%"
    if (txtEnable) log.info "${descriptionText}"
    sendEvent(name:"battery", value:value, descriptionText:descriptionText, unit: "%")
}

/* Old maxwell code
private getMotionResult(value) {
    if (logEnable) log.debug "getMotionResult: ${value}"
    if (device.currentValue("motion") != "active") {
        runIn(30,motionOff)
        def descriptionText = "${device.displayName} is ${value}"
        if (txtEnable) log.info "${descriptionText}"
        sendEvent(name: "motion",value: value,descriptionText: "${descriptionText}")
    }
}

def motionOff(){
    def value = "inactive"
    def descriptionText = "${device.displayName} is ${value}"
    if (txtEnable) log.info "${descriptionText}"
    sendEvent(name: "motion",value: value,descriptionText: "${descriptionText}")
}
/* End old maxwell code */

def configure() {
    log.debug "configure"
    def cmd = zigbee.enrollResponse(1500) + [
            "zdo bind 0x${device.deviceNetworkId} 1 1 0x0001 {${device.zigbeeId}} {}", "delay 200",
            "zdo bind 0x${device.deviceNetworkId} 1 1 0x0402 {${device.zigbeeId}} {}", "delay 200",
            "zdo bind 0x${device.deviceNetworkId} 1 1 0x0500 {${device.zigbeeId}} {}", "delay 200",
            "zdo bind 0x${device.deviceNetworkId} 1 1 0x0501 {${device.zigbeeId}} {}", "delay 200",

            "he cmd 0x${device.deviceNetworkId} 1 0x0020 0x03 {04 00}","delay 200",  						//short poll interval
            "he cmd 0x${device.deviceNetworkId} 1 0x0020 0x02 {13 00 00 00}","delay 200", 					//long poll interval
            "he raw 0x${device.deviceNetworkId} 1 1 0x0020 {00 01 02 00 00 23 E0 01 00 00}","delay 200",	//check in interval

            //reporting
            "he cr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x0001 0x0020 0x20 1 86400 {01}","delay 200",//battery
            "he cr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x0402 0x0000 0x29 60 0xFFFE {3200}", "delay 500" //temp
    ] + refresh()
    return cmd
}

def refresh() {
    return [
            "he rattr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x0001 0x0020 {}","delay 200",  //battery
            "he rattr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x0402 0 {}"  //temp
    ] + sendPanelResponse(false)
}
