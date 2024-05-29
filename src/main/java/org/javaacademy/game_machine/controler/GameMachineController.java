package org.javaacademy.game_machine.controler;

import lombok.RequiredArgsConstructor;
import org.javaacademy.game_machine.service.AlphabetGameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping()
public class GameMachineController {
  private final AlphabetGameService alphabetGameService;
  @GetMapping("/play")
  public ResponseEntity<String> playGame() {
    return ResponseEntity.accepted().body(alphabetGameService.gamePlayResult());
  }

  @GetMapping("/history")
  public ResponseEntity<Map<String, String>> historyGame() {
    return ResponseEntity.accepted().body(alphabetGameService.historyGamePlay());
  }
}
