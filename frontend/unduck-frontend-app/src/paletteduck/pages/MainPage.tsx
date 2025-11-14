import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchWithAuth } from "../../common/utils/apiClient";
import { logout } from "../../common/utils/authUtils";

const BACKEND_API_BASE_URL = import.meta.env.VITE_BACKEND_API_BASE_URL;

interface User {
  username: string;
  nickname: string;
  email: string;
  role: string;
}

function GameMainPage() {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    loadUserInfo();
  }, []);

  const loadUserInfo = async () => {
    try {
      const response = await fetchWithAuth(`${BACKEND_API_BASE_URL}/user`);

      if (!response.ok) {
        throw new Error("사용자 정보 조회 실패");
      }

      const data = await response.json();
      setUser(data);
    } catch (err) {
      console.error("사용자 정보 로드 실패:", err);
      // ✅ 비로그인 상태 - 게임 로그인 페이지로 이동
      navigate("/paletteduck/login");
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = async () => {
    // ✅ 게임 서비스 로그인 페이지로 리다이렉트
    await logout("paletteduck");
  };

  if (loading) {
    return <div>로딩 중...</div>;
  }

  if (!user) {
    return <div>사용자 정보를 불러올 수 없습니다.</div>;
  }

  return (
    <div>
      <h1>게임 서비스 메인</h1>
      
      <div>
        <h2>환영합니다, {user.nickname}님!</h2>
        <p>게임을 즐기세요!</p>
      </div>

      <div>
        <h3>게임 목록</h3>
        <button>게임 1 시작</button>
        <button>게임 2 시작</button>
        <button>게임 3 시작</button>
      </div>

      <div>
        <button onClick={() => navigate("/platform")}>플랫폼으로 돌아가기</button>
        <button onClick={handleLogout}>로그아웃</button>
      </div>
    </div>
  );
}

export default GameMainPage;