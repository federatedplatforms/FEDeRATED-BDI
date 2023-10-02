### Known issues

Since SpringDoc does not pick up the JsonSubTypes, @Schema has to be added to the generated LoadEventInvolvedCargoInner:

nl.tno.federated.api.model.LoadEventInvolvedCargoInner:

```
package nl.tno.federated.api.model;

...

// START: Manually added because the OpenAPI generator does not add the @Schema
@Schema(
    oneOf = {DangerousGoods.class, Goods.class, TransportEquipment.class }
)
// END: Manually added
```