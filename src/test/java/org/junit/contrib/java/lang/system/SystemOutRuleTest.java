package org.junit.contrib.java.lang.system;

import static java.lang.System.out;
import static java.lang.System.setOut;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runners.model.Statement;

public class SystemOutRuleTest {
	private static final String ARBITRARY_TEXT = "arbitrary text";

	private final PrintStream originalOut = out;

	@After
	public void restoreSystemOut() {
		setOut(originalOut);
	}

	@Test
	public void restoresSystemOut() {
		final PrintStream otherOut = new PrintStream(new ByteArrayOutputStream());
		executeRuleWithStatement(new SystemOutRule(), new Statement() {
			@Override
			public void evaluate() throws Throwable {
				setOut(otherOut);
			}
		});
		assertThat(out, is(sameInstance(originalOut)));
	}

	@Test
	public void doesNotMuteSystemOutByDefault() {
		ByteArrayOutputStream captureOut = new ByteArrayOutputStream();
		setOut(new PrintStream(captureOut));
		SystemOutRule rule = new SystemOutRule();
		executeRuleWithStatement(rule, new WriteTextToSystemOut());
		assertThat(captureOut.toString(), is(equalTo(ARBITRARY_TEXT)));
	}

	@Test
	public void doesNotWriteToSystemOutIfMutedGlobally() {
		SystemOutRule rule = new SystemOutRule().mute();
		ByteArrayOutputStream captureOutputStream = new ByteArrayOutputStream();
		setOut(new PrintStream(captureOutputStream));
		executeRuleWithStatement(rule, new WriteTextToSystemOut());
		assertThat(captureOutputStream, hasToString(isEmptyString()));
	}

	@Test
	public void doesNotWriteToSystemOutIfMutedLocaly() {
		final SystemOutRule rule = new SystemOutRule();
		ByteArrayOutputStream captureOutputStream = new ByteArrayOutputStream();
		setOut(new PrintStream(captureOutputStream));
		executeRuleWithStatement(rule, new Statement() {
			@Override
			public void evaluate() throws Throwable {
				rule.mute();
				out.print(ARBITRARY_TEXT);
			}
		});
		assertThat(captureOutputStream, hasToString(isEmptyString()));
	}

	@Test
	public void doesNotLogByDefault() {
		SystemOutRule rule = new SystemOutRule();
		executeRuleWithStatement(rule, new WriteTextToSystemOut());
		assertThat(rule.getLog(), isEmptyString());
	}

	@Test
	public void logsIfEnabledGlobally() throws Throwable {
		SystemOutRule rule = new SystemOutRule().enableLog();
		executeRuleWithStatement(rule, new WriteTextToSystemOut());
		assertThat(rule.getLog(), is(equalTo(ARBITRARY_TEXT)));
	}

	@Test
	public void logsIfEnabledLocally() {
		final SystemOutRule rule = new SystemOutRule();
		executeRuleWithStatement(rule, new Statement() {
			@Override
			public void evaluate() throws Throwable {
				rule.enableLog();
				out.print(ARBITRARY_TEXT);
			}
		});
		assertThat(rule.getLog(), is(equalTo(ARBITRARY_TEXT)));
	}

	@Test
	public void collectsLogAfterClearing() {
		final SystemOutRule rule = new SystemOutRule().enableLog();
		executeRuleWithStatement(rule, new Statement() {
			@Override
			public void evaluate() throws Throwable {
				out.print(ARBITRARY_TEXT);
				rule.clearLog();
				out.print(ARBITRARY_TEXT);
			}
		});
		assertThat(rule.getLog(), is(equalTo(ARBITRARY_TEXT)));
	}

	@Test
	public void logsIfMuted() {
		SystemOutRule rule = new SystemOutRule().enableLog().mute();
		executeRuleWithStatement(rule, new WriteTextToSystemOut());
		assertThat(rule.getLog(), is(equalTo(ARBITRARY_TEXT)));
	}

	private void executeRuleWithStatement(TestRule rule, Statement statement) {
		try {
			rule.apply(statement, null).evaluate();
		} catch (Throwable ignored) {
		}
	}

	private class WriteTextToSystemOut extends Statement {
		@Override
		public void evaluate() throws Throwable {
			out.print(ARBITRARY_TEXT);
		}
	}
}
