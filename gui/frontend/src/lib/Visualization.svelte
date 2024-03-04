<script>
    import { data } from "$lib/stores.js";
    import NodePanel from "$lib/NodePanel.svelte";

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
