<script>
    import { getLabelFromUri } from "$lib/util/jsonUtil.js";
    import NodePanel from "$lib/NodePanel.svelte";
    export let node;
</script>

<div class="panel">
    <div class="node-label">{getLabelFromUri(node["@type"])}</div>
    <div class="properties">
        {#each Object.entries(node).filter(([key, value]) => !key.startsWith("@") && typeof value != "object") as [key, value]}
            <div class="key-label">{getLabelFromUri(key)}</div>
            <div class="value-label">{value}</div>
            <br />
        {/each}
        {#each Object.entries(node).filter(([key, value]) => !key.startsWith("@") && typeof value == "object") as [_, value]}
            <NodePanel node={value} />
        {/each}
    </div>
</div>

<style>
    .panel {
        font-family: "Trebuchet MS", "Lucida Sans Unicode", "Lucida Grande", "Lucida Sans", Arial, sans-serif;
        /* margin: 4px; */
        margin-bottom: 20px;
    }
    .properties {
        margin-left: 60px;
        border-left: 6px solid #c8e5ff;
        padding: 4px;
    }
    .node-label {
        background-color: #c8e5ff;
        color: #111111;
        padding: 4px;
        font-size: 0.9em;
        font-weight: bold;
        border-radius: 5px;
        width: 200px;
    }
    .node-label:hover {
        background-color: #c8e5ff;
    }
    .key-label {
        display: inline-block;
        width: 200px;
        font-size: 0.8em;
    }
    .value-label {
        display: inline-block;
        font-size: 1em;
        font-weight: bold;
    }
</style>
