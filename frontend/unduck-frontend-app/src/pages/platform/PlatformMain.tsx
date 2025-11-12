import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

const Main = () => {
  const navigate = useNavigate();
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  // 로그인 상태 확인
  useEffect(() => {
    const token = localStorage.getItem("accessToken");
    setIsLoggedIn(!!token); // 토큰이 있으면 true, 없으면 false
  }, []);

  // 게임 서비스 목록 (예시)
  const gameServices = [
    { name: "Catchmind", path: "/game/catchmind" },
    { name: "Typing Battle", path: "/game/typing-battle" },
    { name: "Quiz Arena", path: "/game/quiz-arena" },
  ];

  // 버튼 클릭 핸들러
  const handleLoginClick = () => navigate("/login");
  const handleProfileClick = () => navigate("/profile");

  return (
    <div>
      <h1>Unduck Platform Main</h1>

      {/* 게임 서비스 이동 버튼 */}
      <section>
        <h2>게임 서비스</h2>
        {gameServices.map((game) => (
          <button key={game.path} onClick={() => navigate(game.path)}>
            {game.name}
          </button>
        ))}
      </section>

      {/* 로그인 / 회원 정보 버튼 */}
      <section style={{ marginTop: "20px" }}>
        {isLoggedIn ? (
          <button onClick={handleProfileClick}>내 정보 보기</button>
        ) : (
          <button onClick={handleLoginClick}>로그인 하러 가기</button>
        )}
      </section>
    </div>
  );
};

export default Main;
