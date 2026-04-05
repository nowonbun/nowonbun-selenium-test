# nowonbun-selenium-test 이용 가이드

- 목적: NOWONBUN 관련 웹 시나리오를 Selenium으로 자동 실행하는 Spring Boot 서비스
- 실행 포트: 기본 `8089` (`local`, `alpha`, `beta`, `gamma`, `sandbox`), `real`은 `8083`
- 실행 방식: 비동기 실행. 응답은 즉시 반환되고 내부에서 작업이 계속 진행된다.

## 엔드포인트
- POST `/execute`
  - 스크립트 모드: `script` 파라미터에 URL 인코딩된 JSON 배열 전달
  - DB 모드: `idx` 파라미터(정수)로 DB에 저장된 시나리오 실행
  - 둘 중 하나는 필수. 둘 다 없으면 `parameter error`

## 스크립트 모드 예시
- 요청 형식: 각 원소는 `[COMMAND, TARGET?, VALUE?]`

```bash
curl -X POST "http://localhost:8089/execute" \
  --data-urlencode "script=
[
  [\"START\", \"https://nowonbun-alpha.linecorp.com/\"],
  [\"INPUT_AND_SUBMIT\", \"#username\", \"soonyub.hwang\"],
  [\"WAIT\", \"10\"],
  [\"SCROLL_BOTTOM\"],
  [\"WAIT\", \"10\"],
  [\"SCREENSHOT\", \"C:/work/login.png\"],
  [\"CLOSE\"]
]"
```

- 처리 흐름
  - JSON 디코딩
  - 유효성 검증 (`START`로 시작, `CLOSE`로 종료)
  - `START`의 URL로 이동
  - 나머지 명령 실행
  - `CLOSE`에서 종료

## DB 모드 예시

```bash
curl -X POST "http://localhost:8089/execute?idx=1"
```

- 동작: `idx`로 시나리오를 조회해 동일한 실행 파이프라인에 넣는 구조
- 구현 상태: **미구현**. `AbstractService#getCommand(int idx)`는 현재 `null` 반환
- 예상 스키마
  - `CommandList`: 여러 Command를 보관
  - `Command`: 개별 실행 명령
  - `CommandExecutionThread`: 실행 단위 추적
  - `CommandExecutionLog`: 명령별 실행 결과 로그

## 명령 목록 (`NbCommand`)
- `START(url)`: 시작 URL 지정. 반드시 첫 명령
- `CLOSE`: 종료. 반드시 마지막 명령
- `URL(url)`: 현재 창에서 URL 이동 후 로딩 완료 대기
- `CLICK(css)`: 요소 클릭
- `CLICK2(css)`: JS `arguments[0].click()` 실행
- `DBL_CLICK(css)`: 더블 클릭
- `INPUT(css, value)`: 입력
- `SUBMIT(css)`: 제출
- `INPUT_AND_SUBMIT(css, value)`: 입력 후 제출
- `SELECT(css, value)`: `<select>`를 value로 선택
- `SELECT_IDX(css, index)`: `<select>`를 index로 선택
- `SWITCH_FRAME(css)`: iframe 전환
- `SWITCH_PARENT_FRAME`: 상위 프레임 전환
- `SWITCH_DEFAULT_FRAME`: 최상위 프레임 전환
- `SWITCH_WINDOW`: 새 팝업/창 전환
- `SWITCH_PARENT_WINDOW`: 메인 창만 남기고 복귀
- `ALERT_ACCEPT`: alert 승인
- `ALERT_DISMISS`: alert 취소
- `SET_COOKIE(key)`: 현재 쿠키 저장
- `LOAD_COOKIE(key)`: 저장된 쿠키 로드
- `SCROLL_TO(y)`: 지정 Y 위치로 스크롤
- `SCROLL_TOP`: 맨 위로 스크롤
- `SCROLL_BOTTOM`: 맨 아래로 스크롤
- `WAIT(seconds)`: 지정 초 대기
- `SCREENSHOT(path)`: 스크린샷 저장

## 실행 환경
- 프로파일: `application.yml`의 `spring.profiles.active`
- 헤드리스: `local` 외 환경에서는 Chrome을 headless로 실행
- 창 크기: `app.chrome-size` 사용 예: `1920,2160`
- 스레드풀: `app.job-thread.size`, 기본 1

## 응답과 로깅
- `/execute`는 비동기로 접수하고 즉시 성공 응답을 반환
- 실행 식별자/스레드 ID는 추후 추가 예정
- 로깅은 `CommonsRequestLoggingFilter` 기준으로 처리

## 빌드
- 이 프로젝트는 단독 Gradle 프로젝트로 분리되었다.
- 필요 파일
  - `settings.gradle`
  - 독립형 `build.gradle`

### 빌드 명령

```powershell
cd D:\work\nowonbun-selenium-test
.\gradlew.bat clean build
```

### 빌드 결과물
- `build/libs/nowonbun-selenium-test-0.0.1-SNAPSHOT.jar`

### Gradle Wrapper
- 현재 저장소에는 `gradlew`, `gradlew.bat`, `gradle/wrapper/*`가 포함되어 있다.
- 따라서 전역 Gradle 없이 바로 빌드할 수 있다.

### 실행 명령

```powershell
java -jar .\build\libs\nowonbun-selenium-test-0.0.1-SNAPSHOT.jar
```

- 현재 설정은 `bootJar` 기준 단독 실행을 전제로 하며 WAR 패키징은 제거되었다.

## 주의사항
- `script`는 URL 인코딩이 필요하다.
- JSON 배열 구조가 정확해야 한다.
- `SCREENSHOT` 경로는 쓰기 권한이 있는 위치를 사용해야 한다.
- `SET_COOKIE` / `LOAD_COOKIE`의 `key`는 일관된 규칙으로 관리하는 것이 좋다.
