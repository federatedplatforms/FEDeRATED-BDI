import axios from 'axios';
import { convertToConnectedObjects } from "$lib/util/jsonUtil.js";

import {
    data,
    selectedEndpoint
} from '$lib/stores';
import { get } from 'svelte/store'

const apiEndpoint = "http://localhost:5432"

// get user's annotations
function getEventsFromEndpoint(company, year, number) {

    const url = `${apiEndpoint}/events?company=${company}&year=${year}&number=${number}`;
    console.log(url)
    axios.get(url)
        .then((response) => response.data)
        .then(eventData => {
            const event = convertToConnectedObjects(eventData)
            data.set([event])
        })
}

export {
    getEventsFromEndpoint
}