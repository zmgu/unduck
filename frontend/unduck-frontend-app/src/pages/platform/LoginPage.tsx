import { useState, useEffect } from "react";

// .env로 부터 백엔드 URL 받아오기
const BACKEND_API_BASE_URL = import.meta.env.VITE_BACKEND_API_BASE_URL;

function LoginPage() {

    // 자체 로그인시 username/password 변수
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");

    useEffect(() => {
        // ✅ 로그인 페이지 진입 시, redirectPath가 비어있으면 기본 "/" 저장
        if (!localStorage.getItem("redirectPath")) {
        localStorage.setItem("redirectPath", "/");
        }
    }, []);

    // 자체 로그인 이벤트
    const handleLogin = async (e) => {
        e.preventDefault();

        try {
        const res = await fetch(`${import.meta.env.VITE_BACKEND_API_BASE_URL}/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify({ username, password }),
        });

        if (!res.ok) throw new Error("로그인 실패");

        const data = await res.json();
        window.location.href = data.redirectUri; // /cookie로 이동
        } catch (err) {
        alert("아이디 또는 비밀번호가 틀렸습니다.");
        }
    };


    // 소셜 로그인 이벤트
    const handleSocialLogin = (provider) => {
        window.location.href = `http://localhost:8080/auth/oauth2/authorization/${provider}?target_uri=${encodeURIComponent(window.location.origin)}`;

    };



    // 페이지
    return (
        <div>
            <h1>로그인</h1>

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

            {error && <p>{error}</p>}

            <button type="submit">계속</button>
            </form>

            <div>
                <button onClick={() => handleSocialLogin("google")}>Google로 계속하기</button>
                <button onClick={() => handleSocialLogin("naver")}>Naver로 계속하기</button>
            </div>

        </div>
    );
}

export default LoginPage;