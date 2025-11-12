import { BrowserRouter, Routes, Route } from "react-router-dom";

import JoinPage from "./pages/common/JoinPage";
import LoginPage from "./pages/platform/LoginPage";
import CookiePage from "./pages/common/CookiePage";
import UserPage from "./pages/platform/UserPage";
import PlatfromMain from "./pages/platform/PlatformMain";

import './App.css'

function App() {

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<PlatfromMain />} />
        <Route path="/join" element={<JoinPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/cookie" element={<CookiePage />} />
        <Route path="/user" element={<UserPage />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App