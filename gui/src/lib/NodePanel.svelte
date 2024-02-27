<script>
    import { getLabelFromUri } from "$lib/util/jsonUtil.js";
    import NodePanel from "$lib/NodePanel.svelte";
    export let node;
</script>

<div class="panel">
    <button
        class="node-label"
        on:click={() => {
            node["@collapsed"] = !node["@collapsed"];
        }}>{getLabelFromUri(node["@type"])}</button
    >
    {#if !node["@collapsed"]}
        <div class="properties">
            {#each Object.entries(node).filter(([key, value]) => !key.startsWith("@") && typeof value != "object") as [key, value]}
                <div class="key-value-line">
                    <div class="key-label">{getLabelFromUri(key)}</div>
                    <div class="value-label">{value}</div>
                </div>
            {/each}
            {#each Object.entries(node).filter(([key, value]) => !key.startsWith("@") && typeof value == "object") as [_, value]}
                <NodePanel node={value} />
            {/each}
        </div>
    {/if}
</div>

<style>
    .panel {
        font-family: "Trebuchet MS", "Lucida Sans Unicode", "Lucida Grande", "Lucida Sans", Arial, sans-serif;
        /* margin: 4px; */
        margin-bottom: 10px;
    }
    .properties {
        margin-left: 60px;
        border-left: 3px solid #99cfff;
        padding-left: 6px;
    }
    .node-label {
        background-color: #99cfff;
        color: #111111;
        padding: 4px;
        font-size: 0.9em;
        font-weight: bold;
        border-radius: 5px;
        border: none;
        cursor: pointer;
    }
    .node-label:hover {
        background-color: #c8e5ff;
    }
    .key-label {
        display: inline-block;
        width: 200px;
        font-size: 0.8em;
        color: #333333;
    }
    .value-label {
        display: inline-block;
        font-size: 1em;
        font-weight: bold;
        width: 200px;
        color: #333333;
    }
    .key-value-line {
        margin-bottom: 4px;
        border-bottom: 0.5px solid #dddddd;
        width: 420px;
    }
</style>
