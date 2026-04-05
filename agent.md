# nowonbun-selenium-test 스크래핑 스크립트 작성 규칙 (nowonbun-selenium-mcp 대응)

이 문서는 `nowonbun-selenium-mcp`를 이용한 대화형 실행과, `nowonbun-selenium-test` 프로젝트에 등록할 스크립트(`/execute` 호출용)를 작성할 때 따를 기준을 정리한 문서다.

## 핵심 원칙
- 먼저 화면을 확인하고 그다음 조작한다.
  - DOM만 보고 추측하지 말고, 실제 화면 상태를 먼저 확인한다.
  - MCP 실행 시에는 `nowonbun-selenium-mcp__screenshot`를 우선 사용한다.
  - 배치 스크립트에서는 필요한 시점에 `SCREENSHOT`을 넣어 상태를 남긴다.
- 셀렉터는 가능한 한 CSS 기준으로 작성한다.
  - 우선순위: `id` / `name` / `data-*` (`data-testid`, `data-qa`) > 의미 있는 class > 구조 의존 선택자
  - `:nth-child()` 같은 구조 의존 선택자는 마지막 수단으로만 사용한다.
- 프로젝트 스크립트 형식은 `nowonbun-selenium-test` 규격을 따른다.
  - 기본 형식: `[COMMAND, TARGET?, VALUE?]`
  - 첫 명령은 `START(url)`
  - 마지막 명령은 `CLOSE`

## nowonbun-selenium-mcp 작업 흐름
1. 브라우저 실행
   - `nowonbun-selenium-mcp__open_browser`
   - `nowonbun-selenium-mcp__navigate`
2. 첫 화면 캡처
   - 예: `./screens/<시나리오>/00_landing.png`
3. 요소 후보 확인
   - 화면을 보고 셀렉터 후보를 좁힌다.
   - 정말 필요할 때만 `nowonbun-selenium-mcp__get_page_source`로 HTML을 확인한다.
4. 작은 단위로 조작
   - `click`, `type_text` 등을 한 단계씩 수행한다.
   - 각 단계 후 `screenshot`으로 결과를 확인한다.
5. 기다림은 최소화
   - 무조건 긴 `WAIT`을 넣지 말고 필요한 최소 시간만 사용한다.
   - 페이지 전환/팝업/프레임 전환 후에는 캡처로 상태를 다시 확인한다.
6. 종료
   - 작업 후 `nowonbun-selenium-mcp__close_browser`

## nowonbun-selenium-test 스크립트 규칙
- JSON 배열 형식
- 각 항목 형식:
  - `[COMMAND, TARGET?, VALUE?]`
- 시작/종료 규칙:
  - 첫 항목은 `START(url)`
  - 마지막 항목은 `CLOSE`
- 셀렉터:
  - `TARGET`에는 CSS 셀렉터를 넣는다.
  - XPath는 사용하지 않는다.
- 자주 쓰는 명령:
  - `URL`
  - `CLICK`
  - `DBL_CLICK`
  - `INPUT`
  - `SUBMIT`
  - `INPUT_AND_SUBMIT`
  - `SELECT`
  - `SWITCH_*`
  - `SCROLL_*`
  - `WAIT`
  - `SCREENSHOT`

## 명령 대응표 (MCP ↔ 프로젝트 스크립트)
- `open_browser` + `navigate` → `START` 또는 `URL`
- `click` → `CLICK`
- `type_text` → `INPUT`
- 입력 후 전송 → `SUBMIT` / `INPUT_AND_SUBMIT`
- JS 클릭 성격이 필요하면 → `CLICK2`
- 스크롤 관련 조작 → `SCROLL_TO` / `SCROLL_TOP` / `SCROLL_BOTTOM`
- 스크린샷 → `SCREENSHOT`
- 브라우저 종료 → `CLOSE`

## 셀렉터 작성 기준
- 권장 순서
  1. `#id`
  2. `[data-testid="..."]`, `[data-qa="..."]`
  3. `name` 기반
  4. 의미 있는 class
  5. 구조 의존 선택자
- 좋은 예
  - `a[data-testid="menu-apply"]`
  - `#submitButton`
  - `form[name="login"] input[name="username"]`
- 나쁜 예
  - `div > div > ul > li:nth-child(3) > a`
  - 구조가 조금만 바뀌어도 깨지는 선택자

## 스크린샷 규칙
- 저장 경로 예시:
  - `./screens/<시나리오>/<순번>_<설명>.png`
- 시나리오 이름 예시:
  - `login-flow`
  - `new-application`
- 파일명 예시:
  - `00_landing.png`
  - `01_filled_username.png`
  - `02_after_submit.png`

## 예시

### MCP 대화형 실행 예시
- `open_browser`
- `navigate("https://nowonbun-alpha.linecorp.com/")`
- `screenshot("./screens/login/00_landing.png")`
- `type_text("#username", "soonyub.hwang")`
- `screenshot("./screens/login/01_filled_username.png")`
- `click('form[name="login"] button[type="submit"]')`
- `screenshot("./screens/login/02_after_submit.png")`
- `close_browser`

### `/execute`용 스크립트 예시
```json
[
  ["START", "https://nowonbun-alpha.linecorp.com/"],
  ["INPUT_AND_SUBMIT", "#username", "soonyub.hwang"],
  ["WAIT", "3"],
  ["SCREENSHOT", "./screens/login/02_after_submit.png"],
  ["CLOSE"]
]
```

## 금지 사항
- XPath 사용 금지
- 화면 확인 없이 DOM만 보고 셀렉터를 확정하지 않는다.
- `WAIT`을 과하게 넣지 않는다.
- `:nth-child()` 남발 금지
- 프레임/팝업 전환이 필요한데 확인 없이 진행하지 않는다.

## 권장 사항
- 조작 전후로 캡처를 남긴다.
- 셀렉터는 최대한 의미 기반으로 작성한다.
- 실패 시 한 번에 긴 시나리오를 다시 돌리지 말고 작은 단계로 나눠 재확인한다.
- MCP에서 검증한 흐름을 `nowonbun-selenium-test` 스크립트로 옮길 때 동일한 의도를 유지한다.
