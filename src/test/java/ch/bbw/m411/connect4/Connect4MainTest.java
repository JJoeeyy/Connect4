package ch.bbw.m411.connect4;

import java.util.List;

import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

class Connect4MainTest implements WithAssertions {

	protected Connect4ArenaMain newInstance() {
		return new Connect4ArenaMain();
	}

	Connect4ArenaMain.Stone[] fromString(String boardStr) {
		var board = boardStr.codePoints()
				.map(Character::toLowerCase)
				.filter(x -> List.of('x', 'o', '.')
						.contains((char) x))
				.mapToObj(x -> x == 'x' ? Connect4ArenaMain.Stone.RED : (x == 'o' ? Connect4ArenaMain.Stone.BLUE : null))
				.toArray(Connect4ArenaMain.Stone[]::new);
		assertThat(board).hasSize(Connect4ArenaMain.WIDTH * Connect4ArenaMain.HEIGHT);
		return board;
	}

	AbstractBooleanAssert<?> assertThatXWin(String boardStr) {
		var board = fromString(boardStr);
		return assertThat(newInstance().isWinning(board, Connect4ArenaMain.Stone.RED)).as(Connect4ArenaMain.toDebugString(board));
	}

	@Test
	void isWin() {
		assertThatXWin("xxxx... ....... ....... .......").isTrue();
		assertThatXWin(".xxxx.. ....... ....... .......").isTrue();
		assertThatXWin("..xxxx. ....... ....... .......").isTrue();
		assertThatXWin("...xxxx ....... ....... .......").isTrue();
		assertThatXWin("...x... ...x... ...x... ...x...").isTrue();
		assertThatXWin("......x ......x ......x ......x").isTrue();
		assertThatXWin("xooo... .xoo... ..xo... ...x...").isTrue();
		assertThatXWin(".ooxo.. .oxoo.. .xxxx.. .......").isTrue();
		assertThatXWin(".ooxo.x .oxoo.. .ooxx.. .xxxx..").isTrue();
		assertThatXWin("oooo... xxxx... ....... .......").isTrue();
	}

	@Test
	void noWin() {
		assertThatXWin("....... ....... ....... .......").isFalse();
		assertThatXWin("xxx.xx. ....... ....... .......").isFalse();
		assertThatXWin("xxx.xxx xxx.xxx xxx.xxx .......").isFalse();
		assertThatXWin("xx.x.xx xx.x.xx xx.x.xx .......").isFalse();
		assertThatXWin("ooo.ooo xxx.xxx xxx.xxx xxx.xxx").isFalse();
		assertThatXWin("oo.o.oo xx.x.xx xx.x.xx xx.x.xx").isFalse();
		assertThatXWin("oooo... ....... ....... .......").isFalse();
		assertThatXWin("xxx.xx. xxx.xx. xxx.... o......").isFalse();
		assertThatXWin("xxxo... x.x.... x.o.... o.x....").isFalse();
	}

	@Test
	void inAGreedyBattleTheFirstPlayerWillWin() {
		var red = new Connect4ArenaMain.GreedyPlayer();
		var blue = new Connect4ArenaMain.GreedyPlayer();
		assertThat(newInstance().play(red, blue)).isSameAs(red);
	}

	@Test
	void greedyVsMinMax() {
		var red = new Connect4ArenaMain.GreedyPlayer();
		var blue = new Connect4ArenaMain.MinMaxPlayer();
		assertThat(newInstance().play(red, blue)).isSameAs(blue);
	}

	@Test
	void MinMax10VsMinMax2() {
		var red = new Connect4ArenaMain.MinMaxPlayer(10);
		var blue = new Connect4ArenaMain.MinMaxPlayer(2);
		assertThat(newInstance().play(red, blue)).isSameAs(red);
	}

	@Test
	void MinMaxVsMinMaxEqual() {
		var red = new Connect4ArenaMain.MinMaxPlayer();
		var blue = new Connect4ArenaMain.MinMaxPlayer();
		assertThat(newInstance().play(red, blue)).isSameAs(red);
	}

	@Test
	void AlphaBeta8VsAlphaBeta4() {
		var red = new Connect4ArenaMain.AlphaBetaPlayer(8);
		var blue = new Connect4ArenaMain.AlphaBetaPlayer(4);
		assertThat(newInstance().play(red, blue)).isSameAs(red);
	}

	@Test
	void AlphaBeta5VsAlphaBeta11() {
		var red = new Connect4ArenaMain.AlphaBetaPlayer(5);
		var blue = new Connect4ArenaMain.AlphaBetaPlayer(11);
		assertThat(newInstance().play(red, blue)).isSameAs(blue);
	}

	@Test
	void AlphaBeta4VsAlphaBeta12() {
		var red = new Connect4ArenaMain.AlphaBetaPlayer(4);
		var blue = new Connect4ArenaMain.AlphaBetaPlayer(12);
		assertThat(newInstance().play(red, blue)).isSameAs(blue);
	}
}
