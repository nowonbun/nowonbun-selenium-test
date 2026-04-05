package com.nowonbun.selenium.controller;

import com.nowonbun.selenium.bean.NbCommandLine;
import com.nowonbun.selenium.bean.NbResponse;
import com.nowonbun.selenium.service.CommandService;
import com.nowonbun.selenium.service.LiamHubService;
import com.nowonbun.selenium.service.WebService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/")
public class EndPointController {

  private final CommandService commandService;
  private final WebService webService;
  private final LiamHubService liamHubService;

  // curl -X POST http://www.nowonbun.com:8089/execute
  @ResponseBody
  @PostMapping("/execute")
  public ResponseEntity<?> execute(
      @RequestParam(value = "script", required = false) String script,
      @RequestParam(value = "idx", required = false) Integer idx) {
    if (script == null && idx == null) {
      return NbResponse.failure("parameter error");
    }
    NbCommandLine cmd = null;
    try {
      if (idx != null) {
        // DB用
        cmd = commandService.getCommand(idx);
      } else if (!StringUtils.isEmpty(script)) {
        // Command用
        /*
         * curl -X POST "http://www.nowonbun.com:8089/execute"
         * --data-urlencode "script=
         * [
         *      [\"START\",\"https://www.nowonbun.com/\"],
         *      [\"INPUT_AND_SUBMIT\", \"#username\",\"soonyub.hwang\"],
         *      [\"WAIT\",\"10\"],
         *      [\"SCROLL_BOTTOM\"],
         *      [\"WAIT\",\"10\"],
         *      [\"SCREENSHOT\",\"C:/work/login.png\"],
         *      [\"CLOSE\"]
         * ]"
         * */
        cmd = commandService.decoding(script);
      }
    } catch (Throwable e) {
      return NbResponse.failure("JSON integrity is incorrect.", e);
    }

    if (cmd == null) {
      return NbResponse.failure("no command found");
    }
    commandService.execute(cmd);
    // TODO: Thread番号を作成しないと
    return NbResponse.success();
  }

  @ResponseBody
  @GetMapping("/test")
  public ResponseEntity<?> test() {
    webService.test();
    return NbResponse.success();
  }
}
/*
 * TODO:
 *  1. 処理は非同期で動くから、ログ処理をどうしょうかを考える必要がある。
 *  2. DBに保存する場合、コマンドの保存と実行結果の保存をどうしょうかを考える必要がある。
 *  想定するデータ定義
 *  - CommandList (複数のCommandを持つ)
 *  3. Cronバッチを無くして、LiamHubで制御するように修正
 *
 * 　--------------------------------------
 *  | id | commandName | isUse | contents |
 *  ---------------------------------------
 *  | 1  | 新しい申請    | true  | 何々      |
 *  | 2  | テスト用      | false | 何々。。  |
 *  ---------------------------------------
 * - Command (1つのCommandListに複数のCommandが紐づく)
 * --------------------------------------------------------------------------------------------------------
 * | id | commandListId | order | command            | target                             | value          |
 * --------------------------------------------------------------------------------------------------------
 * | 1  | 1             | 1     | START              | https://www.nowonbun.com/    |                |
 * | 2  | 1             | 2     | INPUT_AND_SUBMIT   | #username                          | soonyub.hwang  |
 * | 3  | 1             | 3     | SCROLL_BOTTOM      |                                    |                |
 * | 4  | 1             | 4     | WAIT               | 10                                 |                |
 * | 5  | 1             | 5     | SCREENSHOT         | C:/work/login.png                  |                |
 * --------------------------------------------------------------------------------------------------------
 *
 * - CommandExecutionThread (1つのCommandListの実行に対して、複数のCommandExecutionThreadが紐づく)
 * ----------------------------------------------------------------------------------------
 * | id | threadId      | commandListId | status   | startTime          | endTime            |
 * ----------------------------------------------------------------------------------------
 * | 1  | asdfa12v      | 1             | RUNNING  | 2024-01-01 10:00:00 |                    |
 * | 2  | xawrt34a      | 1             | SUCCESS  | 2024-01-01 09:00:00 | 2024-01-01 09:05:00 |
 * | 3  | hwersdfb      | 2             | FAILURE  | 2024-01-01 08:00:00 | 2024-01-01 08:05:00 |
 * -------------------------------------------------------------------------------------------
 *
 * - CommandExecutionLog (1つのCommandExecutionThreadに対して、複数のCommandExecutionLogが紐づく)
 * --------------------------------------------------------------------------------------------------------------
 * | id | commandExecutionThreadId | command        | target                             | value          | status   | logMessage         | startTime          | endTime            |
 * --------------------------------------------------------------------------------------------------------------
 * | 1  | asdfa12v                 | START          | https://www.nowonbun.com/    |                | SUCCESS  |                    | 2024-01-01 10:00:00 | 2024-01-01 10:00:05 |
 * | 2  | asdfa12v                 | INPUT_AND_SUBMIT| #username                          | soonyub.hwang  | SUCCESS  |                    | 2024-01-01 10:00:05 | 2024-01-01 10:00:10 |
 * | 3  | asdfa12v                 | SCROLL_BOTTOM  |                                    |                | SUCCESS  |                    | 2024-01-01 10:00:10 | 2024-01-01 10:00:15 |
 * | 4  | asdfa12v                 | WAIT           | 10                                 |                | SUCCESS  |                    | 2024-01-01 10:00:15 | 2024-01-01 10:00:25 |
 * | 5  | asdfa12v                 | SCREENSHOT     | C:/work/login.png                  |                | FAILURE  | File not found     | 2024-01-01 10:00:25 | 2024-01-01 10:00:30 |
 * --------------------------------------------------------------------------------------------------------------
 *
 *
 *
 *
 * */
