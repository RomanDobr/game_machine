package org.javaacademy.game_machine.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AlphabetGameService {
  private static final char[] ALPHABET = "AFD".toCharArray();
  private static final int OUTCOME = 15;
  private final JdbcTemplate jdbcTemplate;
  private final TransactionTemplate transactionTemplate;
  private ArrayList<Character> resultStepGame = new ArrayList<>();

  private static Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
    return rs.getInt("sum");
  }

  private char getRandomChar() {
    Random random = new Random();
    return ALPHABET[random.nextInt(ALPHABET.length)];
  }

  private int getResultGame() {
    resultStepGame.add(getRandomChar());
    resultStepGame.add(getRandomChar());
    resultStepGame.add(getRandomChar());
    if (resultStepGame.get(0) == 'A' && resultStepGame.get(1) == 'A' && resultStepGame.get(2) == 'A') {
        return 10;
    } else if (resultStepGame.get(0) == 'F' && resultStepGame.get(1) == 'F' && resultStepGame.get(2) == 'F') {
        return 20;
    } else if (resultStepGame.get(0) == 'D' && resultStepGame.get(1) == 'D' && resultStepGame.get(2) == 'D') {
        return 50;
    } else if (resultStepGame.get(0)  == 'A' && resultStepGame.get(1) == 'F' && resultStepGame.get(2) == 'D') {
        return 0;
    }
    return -1;
  }

  public String gamePlayResult() {
    int resultGame = getResultGame();
    String sql = ("INSERT INTO finance_result (income, outcome) values (%s, %s); " +
                "INSERT INTO game (sym_1, sym_2, sym_3) values ('%S', '%s', '%s');")
                .formatted(resultGame,
                        OUTCOME,
                        resultStepGame.get(0),
                        resultStepGame.get(1),
                        resultStepGame.get(2));
    String sqlZero = ("INSERT INTO finance_result (income, outcome) values (%s, %s); " +
                "INSERT INTO game (sym_1, sym_2, sym_3) values ('%S', '%s', '%s');")
                .formatted(0,
                        OUTCOME,
                        resultStepGame.get(0),
                        resultStepGame.get(1),
                        resultStepGame.get(2));
    transactionTemplate.executeWithoutResult(transactionStatus -> {
        Object savepoint = transactionStatus.createSavepoint();
        if (resultGame > 0) {
            jdbcTemplate.update(sql);
        }else if (resultGame == 0) {
            transactionStatus.rollbackToSavepoint(savepoint);
        } else if (resultGame == -1) {
            jdbcTemplate.update(sqlZero);
        }
    });
    resultStepGame.clear();
    return gamePlayHelperToResponse(resultGame);
  }

  public Map<String, String> historyGamePlay() {
    String sqlIncomeSumPlayer = "select sum(income) from finance_result;";
    String sqlOutComeSumPlayer = "select sum(outcome) from finance_result;";
    String resultPlayFiveGame = "select *  from (select * from finance_result order by id DESC Limit 5) AS t1 order by t1 ASC;";
    int incomePlayer = jdbcTemplate.queryForObject(sqlIncomeSumPlayer, AlphabetGameService::mapRow);
    int outcomePlayer = jdbcTemplate.queryForObject(sqlOutComeSumPlayer, AlphabetGameService::mapRow);
    List<Map<String, Object>> listResult = jdbcTemplate.queryForList(resultPlayFiveGame);
    Map<String, String> map = new LinkedHashMap<>();
    StringBuilder builder = new StringBuilder();
    map.put("playerIncome: ", Integer.toString(incomePlayer));
    map.put("playerOutcome: ", Integer.toString(outcomePlayer));
    map.put("game_history: ", builder.append(listResult).toString());
    return map;
  }

  private String gamePlayHelperToResponse(int resultGame) {
    if (resultGame > 0) {
        return ("Вы выиграли - " + resultGame);
    } else if (resultGame == 0) {
        return "Бесплатный ход";
    }
    return "Вы ничего не выиграли";
  }
}
