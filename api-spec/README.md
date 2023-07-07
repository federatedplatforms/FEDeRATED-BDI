### Known issues

Since SpringDoc does not pickup the JsonSubTypes, the @Schema was added to the generated LoadEventInvolvesDigitalTwin1:

@Schema(
anyOf = {DangerousGoods.class, Goods.class, TransportEquipment.class }
)