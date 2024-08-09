import { Stack, Typography } from "@mui/material";
import { useOne, useShow } from "@refinedev/core";
import {
  DateField,
  MarkdownField,
  NumberField,
  Show,
  TextFieldComponent as TextField,
} from "@refinedev/mui";

export const EventShow = () => {
  const { queryResult } = useShow({});

  const { data, isLoading } = queryResult;

  const record = data?.data;

  return (
    <Show isLoading={isLoading}>
      <Stack gap={1}>
        <Typography variant="body1" fontWeight="bold">
          {"ID"}
        </Typography>
        <TextField value={record?.id ?? ""} />

        <Typography variant="body1" fontWeight="bold">
          {"Created"}
        </Typography>
        <TextField value={record?.recordedTime} />

        <Typography variant="body1" fontWeight="bold">
          {"Data"}
        </Typography>
        <TextField value={record?.data} />

        <Typography variant="body1" fontWeight="bold">
          {"Status"}
        </Typography>
        <TextField value={record?.status} />

      </Stack>
    </Show>
  );
};
