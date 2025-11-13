import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";

// Common Pages
import JoinPage from "./pages/common/JoinPage";

// Platform Pages
import PlatformLoginPage from "./pages/platform/PlatformLoginPage";
import PlatformMainPage from "./pages/platform/PlatformMainPage"; // 추가 필요

// Game Pages (향후 추가)
import GameLoginPage from "./pages/game/GameLoginPage";
import GameMainPage from "./pages/game/GameMainPage"; // 추가 필요


function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* 루트 경로 - 플랫폼 메인으로 리다이렉트 */}
        <Route path="/" element={<Navigate to="/platform" replace />} />

        {/* 공통 페이지 */}
        <Route path="/join" element={<JoinPage />} />

        {/* 플랫폼 서비스 */}
        <Route path="/platform/login" element={<PlatformLoginPage />} />
        <Route path="/platform" element={<PlatformMainPage />} />

        {/* 게임 서비스 */}
        <Route path="/game/login" element={<GameLoginPage />} />
        <Route path="/game" element={<GameMainPage />} />

      </Routes>
    </BrowserRouter>
  );
}

export default App;