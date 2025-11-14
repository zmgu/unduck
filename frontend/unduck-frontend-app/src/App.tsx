import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";

import JoinPage from "./platform/pages/JoinPage";

import PlatformLoginPage from "./platform/pages/LoginPage";
import PlatformMainPage from "./platform/pages/MainPage";

import PaletteduckLoginPage from "./paletteduck/pages/LoginPage";
import PalettduckMainPage from "./paletteduck/pages/MainPage";


function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/platform" replace />} />

        <Route path="/join" element={<JoinPage />} />

        <Route path="/platform/login" element={<PlatformLoginPage />} />
        <Route path="/platform" element={<PlatformMainPage />} />

        <Route path="/paletteduck/login" element={<PaletteduckLoginPage />} />
        <Route path="/paletteduck" element={<PalettduckMainPage />} />

      </Routes>
    </BrowserRouter>
  );
}

export default App;