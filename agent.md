# nowonbun-selenium-test スクレイピングスクリプト作成ルール（nowonbun-selenium-mcp 対応）

この文書は、nowonbun-selenium-mcp を使った対話実行および nowonbun-selenium-test プロジェクトに登録するスクリプト（/execute 経由）を作成する際のガイドラインです。

## 基本原則（重要）
- スクリーンショット先行: ページ遷移や状態変化の直後は必ずスクリーンショットを取得し、その画像をもとに画面の状態を確認してから DOM を特定・操作する。
  - MCP 実行: `nowonbun-selenium-mcp__screenshot` を先に呼ぶ。
  - サーバスクリプト: `SCREENSHOT` コマンドを適切なパスで記録する。
- セレクタ方針: XPath は原則使用せず、CSS セレクタを第一優先で採用する。
  - 安定性重視: `id`/`name`/`data-*`（例: `data-testid`, `data-qa`）など、変更に強い属性を優先。
  - 自動生成クラスは避ける: 乱数やビルド時に変化するクラス名は使用しない。
  - 階層は最小限: 長い子孫セレクタや脆い `:nth-child()` の多用は避ける。
- プロジェクト整合: nowonbun-selenium-test のコマンド仕様（`[COMMAND, TARGET?, VALUE?]`）に準拠し、`START` を先頭、`CLOSE` を末尾に置く。

## nowonbun-selenium-mcp での作業フロー（推奨）
1. ブラウザ起動: `nowonbun-selenium-mcp__open_browser` → `nowonbun-selenium-mcp__navigate` で対象 URL へ遷移。
2. 直後に撮る: `nowonbun-selenium-mcp__screenshot` で画面保存（例: `./screens/<シナリオ>/00_landing.png`）。
3. 解析→DOM 特定: スクリーンショットを見ながら要素の候補を挙げ、`nowonbun-selenium-mcp__get_page_source` が必要な場合のみ取得して CSS を決定。
4. 操作は小さく検証: `nowonbun-selenium-mcp__click` / `nowonbun-selenium-mcp__type_text` などを最小単位で試し、効果を都度 `nowonbun-selenium-mcp__screenshot` で確認。
5. 待機は最小限: 固定 `WAIT` 乱用は避け、`timeout_sec` オプションや画面更新の節目でのスクショ確認で安定化。
6. フレーム/ウィンドウ: 切替が必要な場合はその直前直後でスクショを取得し、セレクタのスコープを誤らない。
7. 後処理: 作業完了後は `nowonbun-selenium-mcp__close_browser` を実行。

## nowonbun-selenium-test スクリプト規約
- 形式: JSON 配列の配列。各行は `[COMMAND, TARGET?, VALUE?]`。
- 必須: 先頭 `START(url)`、末尾 `CLOSE`。
- セレクタ: `TARGET` は CSS セレクタを原則とする（XPath 非推奨）。
- 代表コマンド（抜粋）: `URL`, `CLICK`, `DBL_CLICK`, `INPUT`, `SUBMIT`, `INPUT_AND_SUBMIT`, `SELECT`, `SWITCH_*`, `SCROLL_*`, `WAIT`, `SCREENSHOT`。
- 待機戦略: 長い固定待機は避け、画面の節目ごとに `SCREENSHOT` を組み合わせて進行確認。
- 例外系: アラート/新規ウィンドウ/iframe などの切替は、切替直前直後でスクショを取得しログの追跡性を確保。

### コマンド対応（MCP ↔ nowonbun-selenium-test）
- 起動/遷移: `open_browser`/`navigate` ↔ `START` or `URL`
- クリック: `click` ↔ `CLICK`
- 入力/送信: `type_text` ↔ `INPUT`、`SUBMIT`/`INPUT_AND_SUBMIT`
- スクロール: （MCP は JS 実行を内部で扱う）↔ `SCROLL_TO`/`SCROLL_TOP`/`SCROLL_BOTTOM`
- スクショ: `screenshot` ↔ `SCREENSHOT`
- 終了: `close_browser` ↔ `CLOSE`

## セレクタ作成ガイド
- 推奨順序: `#id` > `[data-testid="..."]`/`[data-qa="..."]` > `name` 属性 > 安定したクラスの単一指定 > ラベル-入力の関係を活用した近接指定。
- 避ける: 長い子孫チェーン、構造に強く依存する `:nth-child()` の連鎖、動的乱数クラス。
- 例（避ける → 良い）
  - 悪い: `div.page > div:nth-child(2) > ul > li:nth-child(3) > a`
  - 良い: `a[data-testid="menu-apply"]`、`#submitButton`、`form[name="login"] input[name="username"]`

## 命名/保存規約（任意推奨）
- スクショ保存パス: `./screens/<シナリオ名>/<2桁順序>_<説明>.png`
- シナリオ名: 英小文字+ハイフン（例: `new-application`, `login-flow`）
- 説明: 動詞_対象（例: `click_menu`, `filled_username`）。

## サンプル
- MCP 対話実行（概念例）
  - open_browser → navigate(`https://nowonbun-alpha.linecorp.com/`)
  - screenshot(`./screens/login/00_landing.png`)
  - type_text(css=`#username`, text=`soonyub.hwang`)
  - screenshot(`./screens/login/01_filled_username.png`)
  - click(css=`form[name="login"] button[type="submit"]`)
  - screenshot(`./screens/login/02_after_submit.png`)
  - close_browser

- nowonbun-selenium-test スクリプト（/execute 用）
```
[
  ["START", "https://nowonbun-alpha.linecorp.com/"],
  ["INPUT_AND_SUBMIT", "#username", "soonyub.hwang"],
  ["WAIT", "3"],
  ["SCREENSHOT", "./screens/login/02_after_submit.png"],
  ["CLOSE"]
]
```

## 禁則/注意
- XPath の使用は禁止（緊急時を除く）。CSS セレクタで表現できないかを必ず検討。
- 不要な長時間 `WAIT` は避ける。スクショで進行確認しながら短い待機で刻む。
- 状態変化（遷移/送信/切替）の前後では必ずスクショを取得し、トラブルシュート可能な証跡を残す。

以上の方針に従うことで、nowonbun-selenium-mcp の対話実行と nowonbun-selenium-test のバッチ/サーバ実行の双方で、再現性と保守性の高いスクリプトを作成できます。

