package nl.tno.federated.api.event

val DUMMY_DATA_LOAD_EVENT = """    
        @prefix ns0: <https://ontology.tno.nl/logistics/federated/Event#> .

        _:0 a ns0:LoadEvent;
            ns0:hasDateTimeType "true";
            ns0:hasMilestone "true";
            ns0:hasSubmissionTimestamp "2023-05-02T11:06:30.602+02:00";
            ns0:hasTimestamp "2023-05-02T11:06:30.608+02:00";
            ns0:involvesDigitalTwin _:1 .
        
        _:1 a <https://ontology.tno.nl/logistics/federated/DigitalTwin#Goods>;
            <https://ontology.tno.nl/logistics/federated/DigitalTwin#goodsDescription> "goods";
            <https://ontology.tno.nl/logistics/federated/DigitalTwin#goodsWeight> "12" .
    """.trimIndent()