import { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { fetchPublic } from "../../common/utils/apiClient";
import { buildSocialLoginUrl, buildLoginUrl, getServiceNameFromPath } from "../../common/utils/authUtils";

const BACKEND_API_BASE_URL = import.meta.env.VITE_BACKEND_API_BASE_URL;

function GameLoginPage() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();
  const location = useLocation();

  // 현재 경로에서 서비스명 추출 (예: /game/login → "game")
  const serviceName = getServiceNameFromPath(location.pathname);

  // 일반 로그인 핸들러
  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");

    try {
      const loginUrl = buildLoginUrl(serviceName);
      
      const res = await fetchPublic(loginUrl, {
        method: "POST",
        body: JSON.stringify({ username, password }),
      });

      if (!res.ok) throw new Error("로그인 실패");

      const data = await res.json();
      
      // ✅ 게임 서비스 메인으로 이동
      navigate(data.redirectUrl, { replace: true });
    } catch (err) {
      setError("아이디 또는 비밀번호가 틀렸습니다.");
    }
  };

  // 소셜 로그인 핸들러
  const handleSocialLogin = (provider: string) => {
    const socialLoginUrl = buildSocialLoginUrl(provider, serviceName);
    window.location.href = socialLoginUrl;
  };

  return (
    <div>
      <h1>게임 서비스 로그인</h1>

      <form onSubmit={handleLogin}>
        <label>아이디</label>
        <input
          type="text"
          placeholder="아이디"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          required
        />

        <label>비밀번호</label>
        <input
          type="password"
          placeholder="비밀번호"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />

        {error && <p style={{ color: "red" }}>{error}</p>}

        <button type="submit">로그인</button>
      </form>

      <div>
        <button onClick={() => handleSocialLogin("google")}>
          Google로 로그인
        </button>
        <button onClick={() => handleSocialLogin("naver")}>
          Naver로 로그인
        </button>
      </div>

      <div>
        <button onClick={() => navigate("/join")}>회원가입</button>
      </div>
    </div>
  );
}

export default GameLoginPage;