import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchWithAuth } from "../../utils/apiClient";
import { logout } from "../../utils/authUtils";

const BACKEND_API_BASE_URL = import.meta.env.VITE_BACKEND_API_BASE_URL;

interface User {
  username: string;
  nickname: string;
  email: string;
  social: boolean;  // ✅ isSocial → social로 변경
}

function PlatformMainPage() {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    loadUserInfo();
  }, []);

  const loadUserInfo = async () => {
    try {
      console.log("🔍 사용자 정보 조회 시작...");
      
      // ✅ HttpOnly 쿠키가 자동으로 전송됨
      const response = await fetchWithAuth(`${BACKEND_API_BASE_URL}/user`);

      console.log("📡 응답 상태:", response.status);

      if (!response.ok) {
        // 비로그인 상태
        console.log("⚠️ 비로그인 상태");
        setUser(null);
        setLoading(false);
        return;
      }

      const data = await response.json();
      console.log("✅ 사용자 정보 로드 성공:", data);
      setUser(data);
    } catch (err) {
      console.error("❌ 사용자 정보 로드 실패:", err);
      // ✅ 비로그인 상태
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = async () => {
    // ✅ 현재 서비스(platform)로 자동 리다이렉트
    await logout();
  };

  if (loading) {
    return <div>로딩 중...</div>;
  }

  return (
    <div>
      <h1>플랫폼 메인</h1>

      {/* ✅ 로그인 상태일 때만 사용자 정보 표시 */}
      {user && (
        <div style={{ marginBottom: "20px", padding: "10px", border: "1px solid #ccc" }}>
          <h2>환영합니다, {user.nickname}님!</h2>
          <p>아이디: {user.username}</p>
          <p>이메일: {user.email}</p>
          {user.social !== undefined && (
            <p>소셜 로그인: {user.social ? "예" : "아니오"}</p>
          )}
        </div>
      )}

      {/* ✅ 비로그인 상태일 때 안내 메시지 */}
      {!user && (
        <div style={{ marginBottom: "20px", padding: "10px", backgroundColor: "#f0f0f0" }}>
          <p>로그인하시면 더 많은 서비스를 이용하실 수 있습니다.</p>
        </div>
      )}

      {/* ✅ 서비스 목록 - 항상 표시 */}
      <div style={{ marginBottom: "20px" }}>
        <h3>서비스 선택</h3>
        <div style={{ display: "flex", gap: "10px", flexDirection: "column", maxWidth: "300px" }}>
          <button 
            onClick={() => navigate("/game")}
            style={{ padding: "10px", fontSize: "16px" }}
          >
            🎮 게임 서비스
          </button>
          <button 
            onClick={() => navigate("/chat")}
            style={{ padding: "10px", fontSize: "16px" }}
          >
            💬 채팅 서비스
          </button>
          <button 
            onClick={() => navigate("/community")}
            style={{ padding: "10px", fontSize: "16px" }}
          >
            👥 커뮤니티 서비스
          </button>
        </div>
      </div>

      {/* ✅ 로그인 상태에 따라 다른 버튼 표시 */}
      <div>
        {user ? (
          // 로그인 상태 - 로그아웃 버튼
          <button 
            onClick={handleLogout}
            style={{ padding: "10px 20px", fontSize: "16px", backgroundColor: "#ff4444", color: "white", border: "none", cursor: "pointer" }}
          >
            로그아웃
          </button>
        ) : (
          // 비로그인 상태 - 로그인 버튼
          <button 
            onClick={() => navigate("/platform/login")}
            style={{ padding: "10px 20px", fontSize: "16px", backgroundColor: "#4CAF50", color: "white", border: "none", cursor: "pointer" }}
          >
            로그인
          </button>
        )}
      </div>
    </div>
  );
}

export default PlatformMainPage;