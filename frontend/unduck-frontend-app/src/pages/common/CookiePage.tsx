import { useEffect } from "react";
import { useNavigate } from "react-router-dom";

const BACKEND_API_BASE_URL = import.meta.env.VITE_BACKEND_API_BASE_URL;

function CookiePage() {
  const navigate = useNavigate();

  useEffect(() => {
    console.log("CookiePage 실행");
    const exchangeToken = async () => {
      try {
        const redirectPath = localStorage.getItem("redirectPath") || "/";

        const res = await fetch(`${BACKEND_API_BASE_URL}/jwt/exchange`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          credentials: "include",
        });

        if (!res.ok) throw new Error("토큰 교환 실패");

        const data = await res.json();

        localStorage.setItem("accessToken", data.accessToken);
        localStorage.setItem("refreshToken", data.refreshToken);

        localStorage.removeItem("redirectPath");
        navigate(redirectPath, { replace: true });
      } catch (err) {
        alert("로그인 처리 중 오류가 발생했습니다.");
        navigate("/login", { replace: true });
      }
    };

    exchangeToken();
  }, [navigate]);

  return <p>로그인 처리 중입니다...</p>;
}

export default CookiePage;
