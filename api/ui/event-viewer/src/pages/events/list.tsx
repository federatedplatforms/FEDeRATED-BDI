import {DataGrid, GridColDef} from "@mui/x-data-grid";
import {DateField, List, useDataGrid,} from "@refinedev/mui";
import React from "react";

export const EventList = () => {
  const {dataGridProps} = useDataGrid({
    syncWithLocation: true,
  });

  const columns = React.useMemo<GridColDef[]>(
    () => {
      return [
        {
          field: "id",
          headerName: "ID",
          type: "string",
          minWidth: 150,
        },
        {
          field: "recordedTime",
          headerName: "Recorded",
          type: "string",
          minWidth: 150,
          renderCell: (params: any) => <DateField format="YYYY-MM-DD HH:mm" value={params.value}/>
        },
        {
          field: "data",
          flex: 1,
          headerName: "Data",
          type: "string",
          renderCell: (params: any) => JSON.stringify(params.value),
        },
        {
          field: "status",
          headerName: "Status",
          type: "string",
          minWidth: 130,
        },
        // {
        //   field: "actions",
        //   headerName: "Actions",
        //   sortable: false,
        //   type: "actions",
        //   renderCell: function render({row}) {
        //     return (
        //       <>
        //         <ShowButton hideText recordItemId={row.id}/>
        //       </>
        //     );
        //   },
        //   align: "center",
        //   headerAlign: "center",
        //   minWidth: 10,
        // },
      ];
    },
    []
  );

  return (
    <List>
      <DataGrid {...dataGridProps} columns={columns} autoHeight/>
    </List>
  );
};