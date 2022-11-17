package nl.tno.semantic.adapter

object TradelensMapper {


    // TODO what needs to be done here?
    fun createPreMappingEvents(jsonData: String): String {
        // TODO
        return jsonData
    }

    // TODO what needs to be done here?
    fun createPreMappingContainers(jsonData: String): String {
        // TODO
        return jsonData
    }

//# -*- coding: utf-8 -*-
//import json
//import uuid
//
//
//def createEventJson(eventType, milestone, dateTimeType, timestamp, identifier="",
//                    submissionTimestamp = "", digitalTwin = "", digitalTwin2 ="",
//                    infrastructure = "", businessService = ""):
//    output = dict()
//    if identifier:
//        output["id"] = identifier
//    else:
//        output["id"] = str(uuid.uuid1())
//    output["eventType"] = eventType
//    output["milestone"] = milestone
//    output["dateTimeType"] = dateTimeType
//    output["timestamp"] = timestamp
//    if submissionTimestamp:
//        output["submissionTimestamp"] = submissionTimestamp
//    if digitalTwin:
//        output["digitalTwin"] = digitalTwin
//    if digitalTwin2:
//        output["digitalTwin2"] = digitalTwin2 # It is assumed in default_rules that digitalTwin is a container and digitalTwin2 is a transport means
//    if infrastructure:
//        output["infrastructure"] = infrastructure
//    if businessService:
//        output["businessService"] = businessService
//
//    return output
//
//
//def createPremappingEvents(string):
//    # Create a premapping of events as input for RML mapper
//    result = list()
//
//    data = json.loads(string)
//    if type(data) is dict:
//        if "events" in data:
//            data_list = data["events"]
//        else:
//            data_list = [data]
//    else:
//        data_list = data
//
//    for event in data_list:
//
//        if not "eventType" in event:
//            continue
//
//        eventType = None
//        for t in ["Arrival","Departure","Load","Discharge"]:
//            if t.lower() in event["eventType"].lower():
//                eventType = t
//
//        milestone = "End" if eventType in ["Departure","Discharge"] else "Start"
//
//
//        dateTimeType = None
//        for t in ["Actual","Estimated","Expected","Planned","Requested"]:
//            if t.lower() in event["eventType"].lower():
//                dateTimeType = t
//
//
//        timestamp = event["eventOccurrenceTime8601"] if "eventOccurrenceTime8601" in event else None
//        identifier = event["eventTransactionId"]
//        submissionTimestamp = event["eventSubmissionTime8601"] if "eventSubmissionTime8601" in event else None
//        digitalTwin = event["transportEquipmentId"] if "transportEquipmentId" in event else None
//        infrastructure = event["location"]["unlocode"] if "location" in event and "unlocode" in event["location"] else None
//        businessService = event["consignmentId"] if "consignmentId" in event else None
//
//
//        if eventType and dateTimeType and timestamp:
//            result.append(createEventJson(eventType,milestone,dateTimeType,timestamp,identifier,submissionTimestamp,digitalTwin,None,infrastructure,businessService))
//
//    return json.dumps(result)
//
//def findProperty(obj,key):
//    # Recursively walk through dict or list to find the value of a property
//    if type(obj) is dict:
//        if key in obj:
//            return obj[key]
//        else:
//            for k, v in obj.items():
//                item = findProperty(v, key)
//                if item is not None:
//                    return item
//    elif type(obj) is list:
//        for v in obj:
//            item = findProperty(v, key)
//            if item is not None:
//                return item
//
//
//def createPremappingContainers(string):
//    # Create a premapping of container details as input for RML mapper
//    result = list()
//
//    data = json.loads(string)
//    if type(data) is dict:
//        data_list = [data]
//    else:
//        data_list = data
//
//    for container in data_list:
//        id = findProperty(container,"transportEquipmentId")
//        if id:
//            item = {"id": id}
//            transportEquipmentTypes = findProperty(container,"transportEquipmentTypes")
//            if type(transportEquipmentTypes) is list:
//                for t in transportEquipmentTypes:
//                    if "code" in t:
//                        item["containerType"] = t["code"]
//                        break
//
//            billOfLading = findProperty(container,"billsOfLading")
//            if billOfLading:
//                item["billOfLading"] = billOfLading[0] if type(billOfLading) is list else billOfLading # billsOfLading can be a list or string according to sample data
//
//            equipmentNumber = findProperty(container,"equipmentNumber")
//            if equipmentNumber:
//                item["equipmentNumber"] = equipmentNumber
//
//            verifiedGrossMass = findProperty(container,"verifiedGrossMass")
//            if verifiedGrossMass:
//                item["verifiedGrossMass"] = "".join(filter(str.isdigit,verifiedGrossMass))
//
//            harmonizedSystemCode = findProperty(container,"commodityHarmonizedCode")
//            if harmonizedSystemCode:
//                item["harmonizedSystemCode"] = harmonizedSystemCode
//
//            result.append(item)
//
//    return json.dumps(result)
}