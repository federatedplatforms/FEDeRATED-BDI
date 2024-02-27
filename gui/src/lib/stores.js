import { readable } from "svelte/store";
import { browser } from '$app/environment'
import { json, text } from 'd3-fetch'
import { convertToConnectedObjects } from "$lib/util/jsonUtil.js";
//import jsonld from "jsonld"


const data = readable([], function start(set) {
    if (browser) {
        json("data/test.json").then(async data => {
            const objects = convertToConnectedObjects(data)
            set(objects)
        })
        // json("data/test.json").then(str => {
        //     console.log("str", str)
        //     jsonld.compact(str, context).then(compacted => {
        //         console.log("compacted", compacted)
        //         set(compacted)
        //     })

        // })
    }
})

export {
    data
}