package io.skullabs.undertow.standalone;

import io.skullabs.undertow.standalone.api.UndertowStandaloneException;

import java.io.IOException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class Main {

	private final Lock lock = new ReentrantLock();
	private UndertowServer undertowServer;

	public void start() throws UndertowStandaloneException {
		lock.lock();
		undertowServer = new UndertowServer( DefaultConfiguration.loadDefaultConfiguration() );
		undertowServer.start();
	}

	public void stop(){
		undertowServer.stop();
		lock.unlock();
	}

	public void mainloop() throws InterruptedException, UndertowStandaloneException {
		Condition newCondition = lock.newCondition();
		start();

		try {
			while( true )
				newCondition.awaitNanos(1);
		} catch ( InterruptedException cause ) {
			stop();
		}
	}

	public static void main(String[] args) throws InterruptedException, UndertowStandaloneException, IOException, ClassNotFoundException {
		val main = new Main();
		main.mainloop();
	}
}