<script>
    import NodePanel from "$lib/NodePanel.svelte";
    export let node;
    //sort key value entries
    let keyValuePairs;
    $: {
        keyValuePairs = [...Object.entries(node)];
        keyValuePairs.sort((kv1, kv2) => {
            const value1 = kv1[1];
            const value2 = kv2[1];
            if (value1.hasOwnProperty("_values") && !value2.hasOwnProperty("_values")) {
                return 1;
            } else if (value2.hasOwnProperty("_values") && !value1.hasOwnProperty("_values")) {
                return -1;
            } else {
                return 0;
            }
        });
    }
</script>

<div class="panel">
    {#each keyValuePairs as [key, value]}
        <div class="property">
            {#if value.hasOwnProperty("_values")}
                <button
                    class="node-label"
                    on:click={() => {
                        value._collapsed = !value._collapsed;
                    }}>{key}</button
                >
                {#if !value["_collapsed"]}
                    <div class="properties">
                        {#each value._values as childNode}
                            <NodePanel node={childNode} />
                        {/each}
                    </div>
                {/if}
            {:else}
                <div class="key-value-line">
                    <div class="key-label">{key}</div>
                    <div class="value-label">{value}</div>
                </div>
            {/if}
        </div>
    {/each}
    <br />
    <!-- {#if !node["@collapsed"]}
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
    {/if} -->
</div>

<style>
    .panel {
        font-family: "Trebuchet MS", "Lucida Sans Unicode", "Lucida Grande", "Lucida Sans", Arial, sans-serif;
        /* margin: 4px; */
        margin-bottom: 10px;
    }
    .property {
        margin: 3px;
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
        border-bottom: 0.5px solid #dddddd;
        width: 420px;
    }
</style>
