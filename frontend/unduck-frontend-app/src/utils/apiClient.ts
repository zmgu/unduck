const BACKEND_API_BASE_URL = import.meta.env.VITE_BACKEND_API_BASE_URL;

/**
 * 인증이 필요한 API 호출
 * - HttpOnly 쿠키가 자동으로 전송됨
 * - 401 에러 시 자동 토큰 갱신
 */
export async function fetchWithAuth(
  url: string, 
  options: RequestInit = {}
): Promise<Response> {
  
  const response = await fetch(url, {
    ...options,
    credentials: "include", // ✅ HttpOnly 쿠키 자동 전송
    headers: {
      "Content-Type": "application/json",
      ...options.headers,
    },
  });

  // 401 Unauthorized - Access Token 만료
  if (response.status === 401) {
    console.log("⚠️ Access Token 만료, Refresh Token으로 재발급 시도...");

    try {
      // Refresh Token으로 Access Token 재발급
      const refreshRes = await fetch(`${BACKEND_API_BASE_URL}/jwt/refresh`, {
        method: "POST",
        credentials: "include",
      });

      if (refreshRes.ok) {
        console.log("✅ Access Token 재발급 성공!");
        
        // 원래 요청 재시도
        return fetch(url, {
          ...options,
          credentials: "include",
          headers: {
            "Content-Type": "application/json",
            ...options.headers,
          },
        });
      } else {
        // Refresh Token도 만료 - 로그인 필요
        console.error("❌ Refresh Token도 만료됨. 재로그인 필요.");
        throw new Error("Refresh Token도 만료됨");
      }
    } catch (err) {
      console.error("❌ 토큰 갱신 실패:", err);
      // ✅ 에러 시 자동 리다이렉트하지 않음 (페이지에서 처리)
      throw err;
    }
  }

  return response;
}

/**
 * 인증이 필요 없는 공개 API 호출
 */
export async function fetchPublic(
  url: string,
  options: RequestInit = {}
): Promise<Response> {
  return fetch(url, {
    ...options,
    credentials: "include", // 쿠키 기반이므로 포함
    headers: {
      "Content-Type": "application/json",
      ...options.headers,
    },
  });
}