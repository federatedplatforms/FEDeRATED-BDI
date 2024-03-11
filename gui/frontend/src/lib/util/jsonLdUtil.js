//parses json-ld string and creates objects for each item
//nests objects when item has attribute referring to another item
function convertToConnectedObjects(jsonldData) {
    //jsonldData is an array with objects, some of which are graph objects
    //The graph objects need to be replaced by a node tree
    const objectList = jsonldData.map(node => "@graph" in node ? createObjectTreeFromJsonGraph(node["@graph"]) : node)
    return objectList


}

function getLabelFromUri(uri) {
    return uri.substring(uri.indexOf("#") + 1)
}

//construct a tree of nodes representing the jsonGraph
//returns the root object
function createObjectTreeFromJsonGraph(graphNodes) {
    //graph is an array of objects. Each object may reference another object
    //by id. We replace this id by the actual object
    graphNodes.forEach(node => {
        //console.log
        //keep track of whether this node is root or not
        node['@parent'] = null //to derive which node is the root of the tree
        Object.entries(node).forEach(([key, value]) => {
            //check if value is an object. If so, the object has one property '@id'
            //which is a reference to another object (this is not true in general,
            //but it is for the example in test.json)
            if (typeof value === 'object' && !key.startsWith("@")) {
                //find node corresponding with object's id
                node[key] = graphNodes.find(n => n['@id'] == value['@id'])
                node[key]['@parent'] = node
            }
        })
    })
    //we assume one root object for now
    return graphNodes.find(node => node['@parent'] == null)
}

export {
    convertToConnectedObjects,
    getLabelFromUri
}