import { fetchWithAuth } from "./apiClient";

const BACKEND_API_BASE_URL = import.meta.env.VITE_BACKEND_API_BASE_URL;

/**
 * 로그아웃 처리
 * - 백엔드에 로그아웃 요청
 * - HttpOnly 쿠키는 백엔드에서 자동 삭제
 * 
 * @param redirectService 로그아웃 후 돌아갈 서비스 (예: "game", "chat")
 */
export async function logout(redirectService?: string): Promise<void> {
  try {
    // 현재 경로에서 서비스명 추출 (파라미터가 없으면)
    const service = redirectService || getServiceNameFromPath(window.location.pathname);
    
    const url = service && service !== "platform"
      ? `${BACKEND_API_BASE_URL}/logout?redirect_service=${service}`
      : `${BACKEND_API_BASE_URL}/logout`;

    const response = await fetch(url, {
      method: "POST",
      credentials: "include",
    });

    if (response.ok) {
      const data = await response.json();
      console.log("✅ 로그아웃 성공");
      
      // 백엔드에서 받은 redirectUrl로 이동
      window.location.href = data.redirectUrl;
    } else {
      throw new Error("로그아웃 실패");
    }
  } catch (err) {
    console.error("❌ 로그아웃 오류:", err);
    // 에러가 나도 로그인 페이지로 이동
    window.location.href = "/platform/login";
  }
}

/**
 * 현재 경로에서 서비스명 추출
 * 예: /game/login → "game"
 *     /chat/main → "chat"
 *     /platform/login → "platform"
 */
export function getServiceNameFromPath(pathname: string): string {
  const match = pathname.match(/^\/([^\/]+)/);
  return match ? match[1] : "platform";
}

/**
 * 소셜 로그인 URL 생성
 */
export function buildSocialLoginUrl(
  provider: string, 
  redirectService: string
): string {
  const baseUrl = `${BACKEND_API_BASE_URL}/auth/oauth2/authorization/${provider}`;
  
  if (redirectService && redirectService !== "platform") {
    return `${baseUrl}?redirect_service=${redirectService}`;
  }
  
  return baseUrl;
}

/**
 * 일반 로그인 URL 생성
 */
export function buildLoginUrl(redirectService: string): string {
  const baseUrl = `${BACKEND_API_BASE_URL}/login`;
  
  if (redirectService && redirectService !== "platform") {
    return `${baseUrl}?redirect_service=${redirectService}`;
  }
  
  return baseUrl;
}