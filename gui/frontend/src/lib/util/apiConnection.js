import axios from 'axios';
import { convertToConnectedObjects } from "$lib/util/jsonUtil.js";

import {
    data,
    isLoading,
    selectedEndpoint
} from '$lib/stores';
import { get } from 'svelte/store'

const apiEndpoint = "http://localhost:5432"

// get user's annotations
function getEventsFromEndpoint(company, year, number) {
    data.set([])
    isLoading.set(true)
    const url = `${apiEndpoint}/events?company=${company}&year=${year}&number=${number}`;
    console.log(url)
    axios.get(url)
        .then((response) => response.data)
        .then(eventData => {
            console.log("raw data from endpoint", eventData)
            const event = convertToConnectedObjects(eventData)
            data.set([event])
            isLoading.set(false)
        })
}

export {
    getEventsFromEndpoint
}