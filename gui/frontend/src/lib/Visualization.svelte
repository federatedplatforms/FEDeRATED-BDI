<script>
    import { data, isLoading } from "$lib/stores.js";
    import NodePanel from "$lib/NodePanel.svelte";

    $: console.log("data", $data);
</script>

<div class="node-container">
    {#if $isLoading}
        <div class="message">Loading...</div>
    {/if}
    {#each $data as node}
        <NodePanel {node} />
    {/each}
</div>

<style>
    .node-container {
        margin: 20px;
    }
    .message {
        font-size: 0.9em;
    }
</style>
