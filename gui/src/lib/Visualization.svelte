<script>
    import { data } from "$lib/stores.js";
    import NodePanel from "$lib/NodePanel.svelte";

    $: {
        $data.forEach((d) => collapse(d));
    }
    //set all objects to 'collapsed', recursively
    function collapse(node) {
        node["@collapsed"] = true;
        Object.entries(node).forEach(([key, value]) => {
            if (typeof value === "object" && !key.startsWith("@")) {
                collapse(value);
            }
        });
    }
    $: console.log("data", $data);
</script>

<div class="node-container">
    {#each $data as node}
        <NodePanel {node} />
    {/each}
</div>

<style>
    .node-container {
        margin: 20px;
    }
</style>
