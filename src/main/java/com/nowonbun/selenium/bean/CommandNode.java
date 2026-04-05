package com.nowonbun.selenium.bean;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
@JsonFormat(shape = JsonFormat.Shape.ARRAY)
public class CommandNode {
  private final NbCommand command;
  private final String target;
  private final String value;

  @JsonIgnore
  public CommandNode(NbCommand command, String target, String value) {
    this.command = command;
    this.target = target;
    this.value = value;
  }

  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static CommandNode from(List<Object> tuple) {
    NbCommand c = NbCommand.from(String.valueOf(tuple.get(0)));
    String t = (tuple.size() > 1 && tuple.get(1) != null) ? String.valueOf(tuple.get(1)) : null;
    String v = (tuple.size() > 2 && tuple.get(2) != null) ? String.valueOf(tuple.get(2)) : null;
    return new CommandNode(c, t, v);
  }
}
