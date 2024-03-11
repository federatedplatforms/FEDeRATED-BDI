//replace each attribute that is of type array, to an object
//with attribute 'collapsed' and values that is the original array
function convertToConnectedObjects(event) {
    let newObject = {}
    Object.entries(event).forEach(([key, value]) => {
        if (value == null) {
            newObject[key] = "-"
        } else if (Array.isArray(value)) {
            newObject[key] = {
                _collapsed: true,
                _values: value.map(d => convertToConnectedObjects(d))
            }
        } else if (typeof value === "object") {
            console.log("key, value", key, value)
            newObject[key] = {
                _collapsed: true,
                _values: [convertToConnectedObjects(value)]
            }
        } else {
            newObject[key] = value
        }
    })
    return newObject
}

export {
    convertToConnectedObjects
}