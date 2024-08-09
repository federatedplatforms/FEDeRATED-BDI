import {Refine} from "@refinedev/core";
import {DevtoolsPanel, DevtoolsProvider} from "@refinedev/devtools";
import {RefineKbar, RefineKbarProvider} from "@refinedev/kbar";
import {ErrorComponent, useNotificationProvider, RefineSnackbarProvider, ThemedLayoutV2, ThemedTitleV2} from "@refinedev/mui";
import CssBaseline from "@mui/material/CssBaseline";
import GlobalStyles from "@mui/material/GlobalStyles";
import routerBindings, {DocumentTitleHandler, UnsavedChangesNotifier} from "@refinedev/react-router-v6";
import dataProvider from "@refinedev/simple-rest";
import {HashRouter, Navigate, Outlet, Route, Routes} from "react-router-dom";
import {Header} from "./components";
import {ColorModeContextProvider} from "./contexts/color-mode";
import {EventList, EventShow} from "./pages/events";

function App() {
  return (
    <HashRouter>
      <RefineKbarProvider>
        <ColorModeContextProvider>
          <CssBaseline/>
          <GlobalStyles styles={{html: {WebkitFontSmoothing: "auto"}}}/>
          <RefineSnackbarProvider>
            <DevtoolsProvider>
              <Refine
                dataProvider={dataProvider("http://localhost:10050/api/corda/vault")}
                notificationProvider={useNotificationProvider}
                routerProvider={routerBindings}
                resources={[
                  {
                    name: "EventState",
                    list: "/events",
                    show: "/events/show/:id",
                    meta: {
                      label: "Events",
                      canDelete: false,
                    },
                  },
                  {
                    name: "DataPullState",
                    list: "/data-pull",
                    show: "/data-pull/show/:id",
                    meta: {
                      label: "Data pull",
                      canDelete: false,
                    },
                  },
                ]}
                options={{
                  syncWithLocation: true,
                  warnWhenUnsavedChanges: true,
                  useNewQueryKeys: true,
                  projectId: "Tkk5XX-Islica-4Vk2iq",
                }}
              >
                <Routes>
                  <Route index element={<Navigate to="/events"/>}/>
                  <Route path="/index.html" element={<Navigate to="/events"/>}/>
                  <Route element={
                    <ThemedLayoutV2
                      Header={() => <Header sticky/>}
                      Title={({collapsed}) => (
                        <ThemedTitleV2
                          // collapsed is a boolean value that indicates whether the <Sidebar> is collapsed or not
                          collapsed={collapsed}
                          // https://refine.dev/docs/ui-integrations/material-ui/components/themed-layout/#title
                          text="FEDeRATED"
                        />
                      )}
                    >
                      <Outlet/>
                    </ThemedLayoutV2>
                  }>
                    <Route path="/events">
                      <Route index element={<EventList/>}/>
                      <Route path="show/:id" element={<EventShow/>}/>
                    </Route>
                    <Route path="/data-pull">
                      <Route index element={<EventList/>}/>
                      <Route path="show/:id" element={<EventShow/>}/>
                    </Route>
                    <Route path="*" element={<ErrorComponent/>}/>
                  </Route>
                </Routes>
                <RefineKbar/>
                <UnsavedChangesNotifier/>
                <DocumentTitleHandler/>
              </Refine>
              <DevtoolsPanel/>
            </DevtoolsProvider>
          </RefineSnackbarProvider>
        </ColorModeContextProvider>
      </RefineKbarProvider>
    </HashRouter>
  );
}

export default App;
