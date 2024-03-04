<script>
    import { getEventsFromEndpoint } from "$lib/util/apiConnection.js";
    let company = 90;
    let year = 2023;
    let number = 192;
</script>

<label>Company<input type="text" bind:value={company} /></label>
<label>Year<input type="text" bind:value={year} /></label>
<label>Number<input type="text" bind:value={number} /></label>
<button
    on:click={() => {
        getEventsFromEndpoint(company, year, number);
    }}>Load</button
>
