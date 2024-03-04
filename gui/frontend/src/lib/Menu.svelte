<script>
    import { getEventsFromEndpoint } from "$lib/util/apiConnection.js";
    let company = 90;
    let year = 2023;
    let number = 192;
</script>

<div class="menu-bar">
    <label>Company <input type="text" bind:value={company} /></label>
    <label>Year <input type="text" bind:value={year} /></label>
    <label>Number <input type="text" bind:value={number} /></label>
    <button
        on:click={() => {
            getEventsFromEndpoint(company, year, number);
        }}>Load</button
    >
</div>

<style>
    .menu-bar {
        font-family: "Trebuchet MS", "Lucida Sans Unicode", "Lucida Grande", "Lucida Sans", Arial, sans-serif;
        font-size: 0.9em;
        background-color: #007bc6;
        padding: 8px;
        color: #ffffff;
    }
    .menu-bar input {
        border-radius: 3px;
        border: none;
    }
    .menu-bar label {
        margin-left: 10px;
    }
    .menu-bar button {
        background-color: #99cfff;
        border: none;
        padding: 5px;
        border-radius: 3px;
        cursor: pointer;
        margin-left: 20px;
    }
    .menu-bar button:hover {
        background-color: #c8e5ff;
    }
</style>
