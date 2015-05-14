package org.junit.contrib.java.lang.system.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.output.TeeOutputStream;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import static org.apache.commons.io.IOUtils.write;

public class PrintStreamRule implements TestRule {
	private final PrintStreamHandler printStreamHandler;
	private final MuteableLogStream muteableLogStream;

	public PrintStreamRule(PrintStreamHandler printStreamHandler) {
		this.printStreamHandler = printStreamHandler;
		try {
			this.muteableLogStream = new MuteableLogStream(printStreamHandler.getStream());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public void enableLog() {
		muteableLogStream.enableLog();
	}

	public Statement apply(final Statement base, final Description description) {
		return printStreamHandler.createRestoreStatement(new Statement() {
			@Override
			public void evaluate() throws Throwable {
				printStreamHandler.replaceCurrentStreamWithStream(muteableLogStream);
				base.evaluate();
			}
		});
	}

	public String getLog() {
		return muteableLogStream.getLog();
	}

	public void mute() {
		muteableLogStream.mute();
	}

	public void clearLog() {
		muteableLogStream.clearLog();
	}

	private static class MuteableLogStream extends PrintStream {
		private static final boolean AUTO_FLUSH = true;
		private static final String ENCODING = "UTF-8";
		private final ByteArrayOutputStream log;
		private final MutableOutputStream mutableOriginalStream;
		private final MutableOutputStream mutableLog;

		MuteableLogStream(OutputStream out) throws UnsupportedEncodingException {
			this(out, new ByteArrayOutputStream());
		}

		MuteableLogStream(OutputStream out, ByteArrayOutputStream log)
			throws UnsupportedEncodingException {
			this(new MutableOutputStream(out),
				log, new MutableOutputStream(log));
		}

		MuteableLogStream(MutableOutputStream mutableOriginalStream,
						  ByteArrayOutputStream log, MutableOutputStream mutableLog)
			throws UnsupportedEncodingException {
			super(new TeeOutputStream(
					new PrintStream(mutableOriginalStream),
					new PrintStream(mutableLog)),
				!AUTO_FLUSH, ENCODING);
			this.log = log;
			this.mutableOriginalStream = mutableOriginalStream;
			this.mutableLog = mutableLog;
			this.mutableLog.mute();
		}

		void mute() {
			mutableOriginalStream.mute();
		}

		void clearLog() {
			log.reset();
		}

		void enableLog() {
			mutableLog.turnOutputOn();
		}

		String getLog() {
			try {
				return log.toString(ENCODING);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static class MutableOutputStream extends OutputStream {
		private final OutputStream originalStream;
		private boolean mute = false;

		MutableOutputStream(OutputStream originalStream) {
			this.originalStream = originalStream;
		}

		void mute() {
			mute = true;
		}

		void turnOutputOn() {
			mute = false;
		}

		@Override
		public void write(int b) throws IOException {
			if (!mute)
				originalStream.write(b);
		}
	}
}
